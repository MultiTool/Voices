package voices;

/**
 *
 * @author MultiTool
 */
public class SampleVoice extends Voice {
  public Wave MySample = null;
  /* ********************************************************************************* */
  public void AttachWaveSample(Wave Sample, double BaseFrequency) {
    MySample = Sample;
    this.BaseFreq = BaseFrequency;
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
  @Override public SampleVoice Deep_Clone_Me() {// ICloneable
    SampleVoice child = new SampleVoice();
    child.Copy_From(this);
    child.Copy_Children(this);
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
    @Override public SampleVoice_OffsetBox Deep_Clone_Me() {// ICloneable
      SampleVoice_OffsetBox child = this.Clone_Me();
      child.VoiceContent = child.SampleVoiceContent = this.SampleVoiceContent.Deep_Clone_Me();
      return child;
    }
    /* ********************************************************************************* */
    @Override public void BreakFromHerd() {// for compose time. detach from my songlet and attach to an identical but unlinked songlet
      SampleVoice clone = this.SampleVoiceContent.Deep_Clone_Me();
      this.SampleVoiceContent.UnRef_Songlet();
      this.SampleVoiceContent = clone;
      this.SampleVoiceContent.Ref_Songlet();
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
