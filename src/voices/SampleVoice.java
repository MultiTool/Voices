package voices;

/**
 *
 * @author MultiTool
 */
public class SampleVoice extends Voice {
  public Wave MySample = null;
  /* ********************************************************************************* */
  public SampleVoice() {
//    this.MySample = new Wave();// a default
//    this.MySample.Ingest(Horn, Globals.SampleRate);
//    this.BaseFreq = Globals.BaseFreqC0 / 265.0;//265.663;//264.072;//572.727;//265.663;
    //Globals.BaseFreqC0 / 263.54581673306772913616450532531;// middle C-ish
  }
  /* ********************************************************************************* */
  public void AttachWaveSample(Wave Sample, double BaseFrequency) {
    MySample = Sample;
    this.BaseFreq = BaseFrequency;
  }
  /* ********************************************************************************* */
  @Override public SampleVoice_OffsetBox Spawn_OffsetBox() {// for compose time
    SampleVoice_OffsetBox lbox = new SampleVoice_OffsetBox();// Deliver an OffsetBox specific to this type of phrase.
    lbox.Attach_Songlet(this);
    return lbox;
  }
  /* ********************************************************************************* */
  @Override public SampleVoice_Singer Spawn_My_Singer() {// for render time
    // Deliver one of my singers while exposing specific object class. 
    // Handy if my parent's singers know what class I am and want special access to my particular type of singer.
    SampleVoice_Singer singer = new SampleVoice_Singer();
    singer.MyVoice = singer.MySampleVoice = this;
    singer.MyProject = this.MyProject;// inherit project
    singer.BaseFreq = this.BaseFreq;
    singer.MySample = this.MySample;
    return singer;
  }
  /* ********************************************************************************* */
  @Override public SampleVoice Clone_Me() {// ICloneable
    SampleVoice child = new SampleVoice();
    child.Copy_From(this);
    return child;
  }
  /* ********************************************************************************* */
  @Override public SampleVoice Deep_Clone_Me(ITextable.CollisionLibrary HitTable) {// ICloneable
    SampleVoice child = new SampleVoice();
    child.Copy_From(this);
    child.Copy_Children(this, HitTable);
    return child;
  }
  /* ********************************************************************************* */
  public void Copy_From(SampleVoice donor) {
    super.Copy_From(donor);
    this.MySample = donor.MySample;// maybe we should clone the sample too? 
  }
  /* ********************************************************************************* */
  public static class SampleVoice_OffsetBox extends Voice_OffsetBox {// location box to transpose in pitch, move in time, etc. 
    public SampleVoice SampleVoiceContent;
    /* ********************************************************************************* */
    public SampleVoice_OffsetBox() {
      super();
    }
    /* ********************************************************************************* */
    @Override public Voice GetContent() {
      return VoiceContent;
    }
    /* ********************************************************************************* */
    public void Attach_Songlet(SampleVoice songlet) {// for serialization
      this.SampleVoiceContent = songlet;
      this.VoiceContent = this.SampleVoiceContent = songlet;
      songlet.Ref_Songlet();
    }
    /* ********************************************************************************* */
    @Override public SampleVoice_Singer Spawn_Singer() {// always always always override this
      return this.Spawn_My_Singer();
    }
    /* ********************************************************************************* */
    @Override public SampleVoice_Singer Spawn_My_Singer() {// for render time
      SampleVoice_Singer ph = this.SampleVoiceContent.Spawn_My_Singer();
      ph.MyOffsetBox = this;
      return ph;
    }
    /* ********************************************************************************* */
    @Override public SampleVoice_OffsetBox Clone_Me() {// always override this thusly
      SampleVoice_OffsetBox child = new SampleVoice_OffsetBox();
      child.Copy_From(this);
      child.VoiceContent = child.SampleVoiceContent = this.SampleVoiceContent;// iffy, remove?
      return child;
    }
    /* ********************************************************************************* */
    @Override public SampleVoice_OffsetBox Deep_Clone_Me(ITextable.CollisionLibrary HitTable) {// ICloneable
      SampleVoice_OffsetBox child = this.Clone_Me();
      child.VoiceContent = child.SampleVoiceContent = this.SampleVoiceContent.Deep_Clone_Me(HitTable);
      return child;
    }
    /* ********************************************************************************* */
    @Override public void BreakFromHerd(ITextable.CollisionLibrary HitTable) {// for compose time. detach from my songlet and attach to an identical but unlinked songlet
      SampleVoice clone = this.SampleVoiceContent.Deep_Clone_Me(HitTable);
      if (this.SampleVoiceContent.UnRef_Songlet() <= 0) {
        this.SampleVoiceContent.Delete_Me();
      }
      this.SampleVoiceContent = clone;
      this.SampleVoiceContent.Ref_Songlet();
    }
    /* ********************************************************************************* */
    public static class Factory implements IFactory {// for serialization
      @Override public SampleVoice_OffsetBox Create(JsonParse.Phrase phrase, CollisionLibrary ExistingInstances) {// under construction, this does not do anything yet
        SampleVoice_OffsetBox obox = new SampleVoice_OffsetBox();
        obox.Consume(phrase, ExistingInstances);
        return obox;
      }
    }
  }
  /* ********************************************************************************* */
  public static class SampleVoice_Singer extends Voice_Singer {
    public SampleVoice MySampleVoice;
    public Wave MySample = null;
    /* ********************************************************************************* */
    @Override public double GetWaveForm(double SubTimeAbsolute) {
      return this.MySample.GetResampleLooped(SubTimeAbsolute * BaseFreq);
    }
  }
}
