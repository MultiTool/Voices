package voices;

import java.util.HashMap;
import static voices.Voice.Voice_OffsetBox.ObjectTypeName;

/**
 *
 * @author MultiTool
 */
public class SampleVoice extends Voice {
  private Wave MySample = null;
  public boolean Looped = true;
  public String SamplePreset = "Horn";

  public static String HornSampleName = "Horn";
  public static String SamplePresetName = "SamplePreset";
  public static String LoopedName = "Looped";// for serialization
    public static double[] Horn = {// instrument waveform sample
    -0.030499, 0.019834, 0.073192, 0.130249, 0.191767, 0.257594, 0.32553, 0.394627, 0.465558, 0.5371, 0.607145, 0.676915, 0.74375, 0.806002, 0.861286, 0.909969, 0.950431, 0.978822, 0.995569, 1,
    0.991229, 0.968828, 0.932309, 0.882678, 0.819418, 0.745462, 0.661971, 0.569922, 0.472098, 0.372532, 0.273027, 0.174072, 0.078021, -0.011949, -0.094432, -0.168969, -0.235621, -0.29228, -0.338457, -0.374152,
    -0.399456, -0.41495, -0.420207, -0.416631, -0.404529, -0.384634, -0.35826, -0.326722, -0.290019, -0.25029, -0.209125, -0.168052, -0.126245, -0.084775, -0.046299, -0.01308, 0.016411, 0.041104, 0.05999, 0.071389,
    0.076248, 0.075118, 0.068394, 0.056109, 0.039179, 0.018611, -0.005256, -0.031508, -0.059012, -0.086547, -0.112982, -0.138806, -0.161023, -0.179696, -0.194579, -0.206192, -0.212731, -0.21429, -0.21212, -0.206497,
    -0.19684, -0.182263, -0.165363, -0.146629, -0.126398, -0.1047, -0.082636, -0.060785, -0.038537, -0.017756, 0.000428, 0.016014, 0.030285, 0.044435, 0.056078, 0.065002, 0.07142, 0.075729, 0.079213, 0.08071,
    0.081474, 0.081077, 0.079916, 0.078724, 0.076279, 0.074048, 0.070809, 0.067202, 0.062832, 0.058554, 0.054917, 0.051372, 0.047277, 0.042021, 0.037253, 0.032364, 0.027229, 0.01962, 0.011491, 0.003087,
    -0.005501, -0.015464, -0.02671, -0.039576, -0.053573, -0.068578, -0.083491, -0.099383, -0.11616, -0.133396, -0.150908, -0.168694, -0.186358, -0.202158, -0.218324, -0.234338, -0.249862, -0.261964, -0.271835, -0.280667,
    -0.288277, -0.29387, -0.297476, -0.299676, -0.301601, -0.303007, -0.302243, -0.300073, -0.297934, -0.297476, -0.297017, -0.295489, -0.293839, -0.291608, -0.289561, -0.286841, -0.28354, -0.278834, -0.271683, -0.261109,
    -0.246806, -0.228165, -0.204969, -0.177067, -0.144582, -0.108918, -0.069097
  };
  /* ********************************************************************************* */
  public SampleVoice() {
    super();
  }
  /* ********************************************************************************* */
  public void AttachWaveSample(Wave Sample, double BaseFrequency) {
    MySample = Sample;
    this.BaseFreq = BaseFrequency;
  }
  /* ********************************************************************************* */
  public static Wave Create_Horn_Sample() {
    Wave MySample = new Wave();// a default
    MySample.Ingest(Horn, Globals.SampleRate);
    return MySample;
  }
  /* ********************************************************************************* */
  public void Preset_Horn() {
    this.SamplePreset = HornSampleName;
    this.AttachWaveSample(Create_Horn_Sample(), Globals.BaseFreqC0 / 265.0);
  }
  /* ********************************************************************************* */
  @Override public SampleVoice_OffsetBox Spawn_OffsetBox() {// for compose time
    SampleVoice_OffsetBox lbox = new SampleVoice_OffsetBox();// Deliver an OffsetBox specific to this type of phrase.
    lbox.Attach_Songlet(this);
    return lbox;
  }
  /* ********************************************************************************* */
  @Override public SampleVoice_Singer Spawn_Singer() {// for render time
    // Deliver one of my singers while exposing specific object class. 
    // Handy if my parent's singers know what class I am and want special access to my particular type of singer.
    SampleVoice_Singer singer;
    if (this.Looped) {
      singer = new SampleVoice_Looped_Singer();
    } else {
      singer = new SampleVoice_Singer();
    }
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
    SampleVoice child;
    CollisionItem ci = HitTable.GetItem(this);
    if (ci == null) {
      child = new SampleVoice();
      ci = HitTable.InsertUniqueInstance(this);
      ci.Item = child;
      child.Copy_From(this);
      child.Copy_Children(this, HitTable);
    } else {// pre exists
      child = (SampleVoice) ci.Item;// another cast! 
    }
    return child;
  }
  /* ********************************************************************************* */
  public void Copy_From(SampleVoice donor) {
    super.Copy_From(donor);
    this.MySample = donor.MySample;// maybe we should clone the sample too? 
  }
  /* ********************************************************************************* */
  @Override public JsonParse.Node Export(CollisionLibrary HitTable) {// ITextable
    JsonParse.Node phrase = super.Export(HitTable);// to do: export my wave too
    phrase.ChildrenHash = this.SerializeMyContents(HitTable);
    phrase.AddSubPhrase(LoopedName, IFactory.Utils.PackField(this.Looped));
    if (!this.SamplePreset.trim().equals("")) {
      phrase.AddSubPhrase(SamplePresetName, IFactory.Utils.PackField(this.SamplePreset));
    }
    //this.MySample.Export();
    return phrase;
  }
  @Override public void ShallowLoad(JsonParse.Node phrase) {// ITextable
    super.ShallowLoad(phrase);
    HashMap<String, JsonParse.Node> Fields = phrase.ChildrenHash;
    this.Looped = Boolean.parseBoolean(IFactory.Utils.GetField(Fields, LoopedName, "true"));//Boolean.toString(this.Looped)));
    // Boolean.getBoolean(""); maybe use this instead. simpler, returns false if parse fails. 
    this.SamplePreset = IFactory.Utils.GetField(Fields, SamplePresetName, "");
    if (this.SamplePreset.equals(HornSampleName)) {
      this.Preset_Horn();
    }
  }
  @Override public void Consume(JsonParse.Node phrase, CollisionLibrary ExistingInstances) {// ITextable - Fill in all the values of an already-created object, including deep pointers.
    if (phrase == null) {// to do: consume my wave too
      return;
    }
    super.Consume(phrase, ExistingInstances);
    HashMap<String, JsonParse.Node> Fields = phrase.ChildrenHash;
  }
  /* ********************************************************************************* */
  public static class SampleVoice_OffsetBox extends Voice_OffsetBox {// location box to transpose in pitch, move in time, etc. 
    public SampleVoice SampleVoiceContent;
    public static String ObjectTypeName = "SampleVoice_OffsetBox";
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
      this.VoiceContent = this.SampleVoiceContent = songlet;
      songlet.Ref_Songlet();
    }
    /* ********************************************************************************* */
    @Override public SampleVoice_Singer Spawn_Singer() {// always always always override this
      SampleVoice_Singer Singer = this.SampleVoiceContent.Spawn_Singer();
      Singer.MyOffsetBox = this;
      return Singer;
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
      child.Attach_Songlet(this.SampleVoiceContent.Deep_Clone_Me(HitTable));
      return child;
    }
    /* ********************************************************************************* */
    @Override public void BreakFromHerd(ITextable.CollisionLibrary HitTable) {// for compose time. detach from my songlet and attach to an identical but unlinked songlet
      SampleVoice clone = this.SampleVoiceContent.Deep_Clone_Me(HitTable);
      if (this.SampleVoiceContent.UnRef_Songlet() <= 0) {
        this.SampleVoiceContent.Delete_Me();
      }
      this.Attach_Songlet(clone);
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
      if (phrase == null) {
        return;
      }
      this.ShallowLoad(phrase);
      JsonParse.Node SongletPhrase = phrase.ChildrenHash.get(OffsetBox.ContentName);// value of songlet field
      String ContentTxt = SongletPhrase.Literal;
      SampleVoice songlet;
      if (Globals.IsTxtPtr(ContentTxt)) {// if songlet content is just a pointer into the library
        CollisionItem ci = ExistingInstances.GetItem(ContentTxt);// look up my songlet in the library
        if (ci == null) {// then null reference even in file - the json is corrupt
          throw new RuntimeException("CollisionItem is null in " + ObjectTypeName);
        }
        if ((songlet = (SampleVoice) ci.Item) == null) {// another cast!
          ci.Item = songlet = new SampleVoice();// if not instantiated, create one and save it
          songlet.Consume(ci.JsonPhrase, ExistingInstances);
        }
      } else {
        songlet = new SampleVoice();// songlet is inline, inside this one offsetbox
        songlet.Consume(SongletPhrase, ExistingInstances);
      }
      this.Attach_Songlet(songlet);
    }
    @Override public ISonglet Spawn_And_Attach_Songlet() {// reverse birth, use ONLY for deserialization
      SampleVoice songlet = new SampleVoice();
      this.Attach_Songlet(songlet);
      return songlet;
    }
    /* ********************************************************************************* */
    public static class Factory implements IFactory {// for serialization
      @Override public SampleVoice_OffsetBox Create(JsonParse.Node phrase, CollisionLibrary ExistingInstances) {
        SampleVoice_OffsetBox obox = new SampleVoice_OffsetBox();
        obox.Consume(phrase, ExistingInstances);
        return obox;
      }
//      @Override public Voice_OffsetBox Create(JsonParse.Node phrase, CollisionLibrary ExistingInstances) {
//        Voice_OffsetBox obox = new Voice_OffsetBox();// hack to load sample voices as voices
//        obox.Consume(phrase, ExistingInstances);
//        return obox;
//      }
    }
  }
  /* ********************************************************************************* */
  public static class SampleVoice_Singer extends Voice_Singer {
    public SampleVoice MySampleVoice;
    public Wave MySample = null;
    /* ********************************************************************************* */
    @Override public double GetWaveForm(double SubTimeAbsolute) {
      return this.MySample.GetResample(SubTimeAbsolute * BaseFreq);
    }
  }
  /* ********************************************************************************* */
  public static class SampleVoice_Looped_Singer extends SampleVoice_Singer {
    /* ********************************************************************************* */
    @Override public double GetWaveForm(double SubTimeAbsolute) {
      return this.MySample.GetResampleLooped(SubTimeAbsolute * BaseFreq);
    }
  }
}
