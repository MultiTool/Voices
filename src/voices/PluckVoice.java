package voices;

import java.util.HashMap;

/**
 *
 * @author MultiTool

 Experimental - THIS SOUNDS TERRIBLE

 Plan is:
 generate a time unit (wavelength of BaseFreqC0) of white noise.
 1/time unit length = base frequency for this object
 
 */
public class PluckVoice extends Voice {
  private Wave MySample = null;
  /* ********************************************************************************* */
  public PluckVoice() {
    this.GenerateSample();
  }
  /* ********************************************************************************* */
  public void GenerateSample() {
    int SizeInit = 0, SampleRate = Globals.SampleRate;
    double WaveLen, Freq;
    //MySample = new Wave();
    switch (2) {
    case 0: {
      SizeInit = NoteMaker.Horn.length;
      MySample.Ingest(NoteMaker.Horn, SampleRate);
      WaveLen = (((double) SizeInit) / (double) SampleRate);
      this.BaseFreq = 1.0 / WaveLen;
      break;
    }
    case 1: {
      Freq = Globals.BaseFreqC0;
      //this.BaseFreq = Globals.BaseFreqC0 * Math.pow(2.0, 8.0);
      this.BaseFreq = Globals.BaseFreqC0 / Freq;
      WaveLen = (1.0 / Freq);
      SizeInit = (int) (WaveLen * SampleRate);
      MySample.Init(SizeInit, SampleRate);
      //MySample.WhiteNoise_Fill();
      MySample.Sawtooth_Fill();
      break;
    }
    case 2:
      MySample = GeneratePluck();
      break;
    }
  }
  /* ********************************************************************************* */
  public Wave GeneratePluck() {
    double BaseFreq = Globals.MiddleC4Freq;
    //BaseFreq = Globals.MiddleC4Freq / 4;
    //BaseFreq = Globals.MiddleC4Freq * 8;
    //BaseFreq = Globals.MiddleC4Freq * 4;
    //BaseFreq = Globals.MiddleC4Freq * 2;
    
    this.BaseFreq = Globals.BaseFreqC0 / BaseFreq;// ??? 
    double Duration = 6.0;
    Wave wave0 = new Wave();
    int SamplesPerCycle = (int) ((1.0 / BaseFreq) * Globals.SampleRate);
    Wave pattern = new Wave();
    pattern.Init(SamplesPerCycle, Globals.SampleRate);
    NoteMaker.Generate_WhiteNoise(pattern, SamplesPerCycle, Globals.SampleRate);
    NoteMaker.Synth_Pluck_Decay(wave0, pattern, Duration);
    wave0.Normalize();
    return wave0;
  }
  /* ********************************************************************************* */
  public void PluckLab() {
    double BaseFreq = Globals.MiddleC4Freq / 4;
    double Duration = 6.0;
    Wave wave0 = new Wave();
    Audio aud = new Audio();
    int SamplesPerCycle = (int) ((1.0 / BaseFreq) * Globals.SampleRate);
    //NoteMaker.Synth_Pluck(wave0, BaseFreq, Duration, Globals.SampleRate);
    //NoteMaker.Generate_StackedSines(wave0, 200, Globals.SampleRate);
    Wave pattern = new Wave();
    pattern.Init(SamplesPerCycle, Globals.SampleRate);
    //pattern.Sawtooth_Fill();
    NoteMaker.Generate_WhiteNoise(pattern, SamplesPerCycle, Globals.SampleRate);
    //Generate_StackedSines(pattern, SamplesPerCycle, Globals.SampleRate);
    NoteMaker.Synth_Pluck_Decay(wave0, pattern, Duration);
    wave0.Normalize();
  }
  /* ********************************************************************************* */
  @Override public PluckVoice_OffsetBox Spawn_OffsetBox() {// for compose time
    PluckVoice_OffsetBox lbox = new PluckVoice_OffsetBox();// Deliver an OffsetBox specific to this type of phrase.
    lbox.Attach_Songlet(this);
    return lbox;
  }
  /* ********************************************************************************* */
  @Override public PluckVoice_Singer Spawn_Singer() {// for render time
    PluckVoice_Singer singer;
    singer = new PluckVoice_Singer();
    singer.MyVoice = singer.MyPluckVoice = this;
    singer.MyProject = this.MyProject;// inherit project
    singer.BaseFreq = this.BaseFreq;
    singer.MySample = this.MySample;
    return singer;
  }
  /* ********************************************************************************* */
  @Override public PluckVoice Clone_Me() {// ICloneable
    PluckVoice child = new PluckVoice();
    child.Copy_From(this);
    return child;
  }
  /* ********************************************************************************* */
  @Override public PluckVoice Deep_Clone_Me(ITextable.CollisionLibrary HitTable) {// ICloneable
    PluckVoice child = new PluckVoice();
    child.Copy_From(this);
    child.Copy_Children(this, HitTable);
    return child;
  }
  /* ********************************************************************************* */
  @Override public void Delete_Me() {// IDeletable
    super.Delete_Me();
    this.MySample.Delete_Me();
    this.MySample = null;
  }
  /* ********************************************************************************* */
  public void Copy_From(PluckVoice donor) {
    super.Copy_From(donor);
    //this.MySample.Copy_From(donor.MySample);
    //this.MySample = donor.MySample;// maybe we should clone the sample too? 
    // my white noise is already created. although it is different it is still white noise of the same length.
  }
  /* ********************************************************************************* */
  @Override public JsonParse.Node Export(CollisionLibrary HitTable) {// ITextable
    JsonParse.Node phrase = super.Export(HitTable);// to do: export my wave too
    phrase.ChildrenHash = this.SerializeMyContents(HitTable);
    //this.MySample.Export();
    return phrase;
  }
  @Override public void ShallowLoad(JsonParse.Node phrase) {// ITextable
    super.ShallowLoad(phrase);
    HashMap<String, JsonParse.Node> Fields = phrase.ChildrenHash;
    // Boolean.getBoolean(""); maybe use this instead. simpler, returns false if parse fails. 
  }
  @Override public void Consume(JsonParse.Node phrase, CollisionLibrary ExistingInstances) {// ITextable - Fill in all the values of an already-created object, including deep pointers.
    if (phrase == null) {
      return;
    }
    super.Consume(phrase, ExistingInstances);
    HashMap<String, JsonParse.Node> Fields = phrase.ChildrenHash;
    // my white noise is already created. although it is different it is still white noise of the same length.
  }
  /* ********************************************************************************* */
  public static class PluckVoice_OffsetBox extends Voice_OffsetBox {// location box to transpose in pitch, move in time, etc. 
    public PluckVoice PluckVoiceContent;
    public static String ObjectTypeName = "PluckVoice_OffsetBox";
    /* ********************************************************************************* */
    public PluckVoice_OffsetBox() {
      super();
    }
    /* ********************************************************************************* */
    @Override public Voice GetContent() {
      return PluckVoiceContent;
    }
    /* ********************************************************************************* */
    public void Attach_Songlet(PluckVoice songlet) {// for serialization
      this.VoiceContent = this.PluckVoiceContent = songlet;
      songlet.Ref_Songlet();
    }
    /* ********************************************************************************* */
    @Override public PluckVoice_Singer Spawn_Singer() {// always always always override this
      PluckVoice_Singer Singer = this.PluckVoiceContent.Spawn_Singer();
      Singer.MyOffsetBox = this;
      return Singer;
    }
    /* ********************************************************************************* */
    @Override public PluckVoice_OffsetBox Clone_Me() {// always override this thusly
      PluckVoice_OffsetBox child = new PluckVoice_OffsetBox();
      child.Copy_From(this);
      child.VoiceContent = child.PluckVoiceContent = this.PluckVoiceContent;// iffy, remove?
      return child;
    }
    /* ********************************************************************************* */
    @Override public PluckVoice_OffsetBox Deep_Clone_Me(ITextable.CollisionLibrary HitTable) {// ICloneable
      PluckVoice_OffsetBox child = this.Clone_Me();
      child.VoiceContent = child.PluckVoiceContent = this.PluckVoiceContent.Deep_Clone_Me(HitTable);
      return child;
    }
    /* ********************************************************************************* */
    @Override public void BreakFromHerd(ITextable.CollisionLibrary HitTable) {// for compose time. detach from my songlet and attach to an identical but unlinked songlet
      PluckVoice clone = this.PluckVoiceContent.Deep_Clone_Me(HitTable);
      if (this.PluckVoiceContent.UnRef_Songlet() <= 0) {
        this.PluckVoiceContent.Delete_Me();
      }
      this.VoiceContent = this.PluckVoiceContent = clone;
      this.PluckVoiceContent.Ref_Songlet();
    }
    /* ********************************************************************************* */
    @Override public JsonParse.Node Export(CollisionLibrary HitTable) {// ITextable
      JsonParse.Node SelfPackage = super.Export(HitTable);// tested
      SelfPackage.AddSubPhrase(Globals.ObjectTypeName, IFactory.Utils.PackField(ObjectTypeName));
      return SelfPackage;
    }
    @Override public void ShallowLoad(JsonParse.Node phrase) {// ITextable
      super.ShallowLoad(phrase);
    }
    @Override public void Consume(JsonParse.Node phrase, CollisionLibrary ExistingInstances) {// ITextable - Fill in all the values of an already-created object, including deep pointers.
      if (phrase == null) {// ready for test?
        return;
      }
      this.ShallowLoad(phrase);
      JsonParse.Node SongletPhrase = phrase.ChildrenHash.get(OffsetBox.ContentName);// value of songlet field
      String ContentTxt = SongletPhrase.Literal;
      PluckVoice songlet;
      if (Globals.IsTxtPtr(ContentTxt)) {// if songlet content is just a pointer into the library
        CollisionItem ci = ExistingInstances.GetItem(ContentTxt);// look up my songlet in the library
        if (ci == null) {// then null reference even in file - the json is corrupt
          throw new RuntimeException("CollisionItem is null in " + ObjectTypeName);
        }
        if ((songlet = (PluckVoice) ci.Item) == null) {// another cast!
          ci.Item = songlet = new PluckVoice();// if not instantiated, create one and save it
          songlet.Consume(ci.JsonPhrase, ExistingInstances);
        }
      } else {
        songlet = new PluckVoice();// songlet is inline, inside this one offsetbox
        songlet.Consume(SongletPhrase, ExistingInstances);
      }
      this.Attach_Songlet(songlet);
    }
    @Override public ISonglet Spawn_And_Attach_Songlet() {// reverse birth, use ONLY for deserialization
      PluckVoice songlet = new PluckVoice();
      this.Attach_Songlet(songlet);
      return songlet;
    }
    /* ********************************************************************************* */
    public static class Factory implements IFactory {// for serialization
      @Override public PluckVoice_OffsetBox Create(JsonParse.Node phrase, CollisionLibrary ExistingInstances) {
        PluckVoice_OffsetBox obox = new PluckVoice_OffsetBox();
        obox.Consume(phrase, ExistingInstances);
        return obox;
      }
    }
  }
  /* ********************************************************************************* */
  public static class PluckVoice_Singer extends Voice_Singer {
    public PluckVoice MyPluckVoice;
    public Wave MySample = null;
    /* ********************************************************************************* */
    @Override public double GetWaveForm(double SubTimeAbsolute) {
      return this.MySample.GetResample(SubTimeAbsolute * BaseFreq);
    }
  }
  /* ********************************************************************************* */
  public static class PluckVoice_Looped_Singer extends Voice_Singer {
    public PluckVoice MyPluckVoice;
    public Wave MySample = null;
    /* ********************************************************************************* */
    @Override public double GetWaveForm(double SubTimeAbsolute) {
      return this.MySample.GetResampleLooped(SubTimeAbsolute * BaseFreq);
    }
  }
}
