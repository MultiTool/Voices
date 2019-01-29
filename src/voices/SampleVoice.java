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
  public static double[] Ahh = {// voice waveform sample. first amp was 1.0, changed it to 0.98
    0.97, 0.992923, 0.962965, 0.907444, 0.836974, 0.754301, 0.659182, 0.562294, 0.480903, 0.417084, 0.371019, 0.34039, 0.321415, 0.310433, 0.293655, 0.274619, 0.243929, 0.207077, 0.169982, 0.127273,
    0.081208, 0.033679, 0.00183, -0.02648, -0.039841, -0.043502, -0.036852, -0.033191, -0.039963, -0.034899, -0.032093, -0.045272, -0.060769, -0.076205, -0.086516, -0.089811, -0.083771, -0.069189, -0.053508, -0.035754,
    -0.02227, -0.018426, -0.029713, -0.05241, -0.079317, -0.102807, -0.124161, -0.137706, -0.146187, -0.142953, -0.119402, -0.088957, -0.051007, -0.008664, 0.035509, 0.075961, 0.113728, 0.150885, 0.180232, 0.206223,
    0.233679, 0.25058, 0.26748, 0.284686, 0.298414, 0.320805, 0.349664, 0.380354, 0.41263, 0.439475, 0.463941, 0.478523, 0.480842, 0.475839, 0.458145, 0.435998, 0.413728, 0.392984, 0.380476, 0.364369,
    0.344966, 0.323612, 0.301525, 0.274741, 0.246858, 0.216779, 0.181025, 0.145577, 0.109884, 0.075778, 0.040513, 0.008847, -0.020134, -0.041855, -0.053264, -0.056132, -0.047529, -0.03191, -0.016595, -0.002929,
    0.003722, -0.000915, -0.02111, -0.050336, -0.083588, -0.113423, -0.139536, -0.150458, -0.153264, -0.150885, -0.139902, -0.130323, -0.125076, -0.127761, -0.135631, -0.150153, -0.166443, -0.18255, -0.196034, -0.210067,
    -0.223185, -0.225564, -0.228432, -0.228615, -0.219158, -0.205125, -0.185723, -0.164246, -0.143868, -0.12892, -0.115009, -0.10482, -0.094204, -0.078401, -0.054667, -0.028798, 0.007261, 0.052715, 0.103051, 0.151678,
    0.19219, 0.219707, 0.234533, 0.241855, 0.243075, 0.241367, 0.240024, 0.237523, 0.239231, 0.242221, 0.254057, 0.265467, 0.277059, 0.283344, 0.286394, 0.284747, 0.284869, 0.286577, 0.285052, 0.283954,
    0.282306, 0.282428, 0.28432, 0.285601, 0.286699, 0.286211, 0.281757, 0.278768, 0.27291, 0.269311, 0.263941, 0.252471, 0.243258, 0.232703, 0.227578, 0.226907, 0.228859, 0.228615, 0.226602, 0.221843,
    0.21446, 0.201708, 0.184442, 0.159671, 0.132154, 0.097621, 0.066138, 0.042953, 0.024283, 0.010555, 0.00061, -0.000244, 0.004576, 0.007932, 0.011897, 0.01086, 0.005979, 0.002868, -0.006467, -0.014216,
    -0.024405, -0.036486, -0.043868, -0.051251, -0.051861, -0.052593, -0.052105, -0.046919, -0.039109, -0.026602, -0.020256, -0.015314, -0.01385, -0.014887, -0.018914, -0.023124, -0.026419, -0.035265, -0.035265, -0.029774,
    -0.023856, -0.018609, -0.012935, -0.005186, 0.001647, 0.002379, 0.002685, 0.003966, -0.000061, -0.000732, 0.004271, 0.006345, 0.01086, 0.017999, 0.024588, 0.030872, 0.036181, 0.034289, 0.031361, 0.02709,
    0.017511, 0.008786, 0.000976, -0.005857, -0.012203, -0.017267, -0.016656, -0.021415, -0.024039, -0.024954, -0.032703, -0.040635, -0.053203, -0.066687, -0.082428, -0.09817, -0.117999, -0.134777, -0.151434, -0.168273,
    -0.176693, -0.183405, -0.188774, -0.189567, -0.194021, -0.198597, -0.204454, -0.216718, -0.231117, -0.249481, -0.26687, -0.282245, -0.294936, -0.303844, -0.305796, -0.309518, -0.308481, -0.303661, -0.301525, -0.299817,
    -0.300793, -0.307688, -0.315375, -0.326907, -0.339475, -0.35363, -0.366321, -0.379256, -0.390787, -0.398597, -0.40183, -0.408115, -0.411714, -0.419707, -0.423002, -0.429957, -0.439597, -0.448017, -0.459915, -0.469677,
    -0.480354, -0.491092, -0.502807, -0.512996, -0.523368, -0.530506, -0.532703, -0.535204, -0.533069, -0.528066, -0.522392, -0.519524, -0.509823, -0.497254, -0.487736, -0.477852, -0.466382, -0.45784, -0.44698, -0.43673,
    -0.43313, -0.435937, -0.443441, -0.447346, -0.456254, -0.463209, -0.46809, -0.470958, -0.46388, -0.453386, -0.438133, -0.416168, -0.397193, -0.382123, -0.373398, -0.37291, -0.376022, -0.385418, -0.397498, -0.419951,
    -0.445455, -0.467053, -0.490421, -0.500854, -0.49817, -0.488347, -0.473398, -0.449603, -0.422026, -0.394692, -0.374863, -0.368639, -0.379805, -0.40122, -0.422392, -0.452044, -0.463514, -0.464063, -0.465772, -0.457901,
    -0.444356, -0.432642, -0.405613, -0.383344, -0.374741, -0.366809, -0.349725, -0.325015, -0.296278, -0.26266, -0.228676, -0.186455, -0.126846, -0.058206, 0.019951, 0.100122, 0.192434, 0.287065, 0.364857, 0.446187,
    0.512081, 0.559304, 0.598963, 0.623734, 0.649969, 0.670836, 0.705979, 0.749725, 0.786089, 0.826236, 0.861501, 0.894326, 0.914948, 0.94 //0.936669
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
  public static Wave Create_Ahh_Sample() {
    Wave MySample = new Wave();// a default
    MySample.Ingest(Ahh, Globals.SampleRate);
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
  @Override public JsonParse.HashNode Export(CollisionLibrary HitTable) {// ITextable
    JsonParse.HashNode phrase = super.Export(HitTable);// to do: export my wave too
    phrase.ChildrenHash = this.SerializeMyContents(HitTable);
    phrase.AddSubPhrase(LoopedName, IFactory.Utils.PackField(this.Looped));
    if (!this.SamplePreset.trim().equals("")) {
      phrase.AddSubPhrase(SamplePresetName, IFactory.Utils.PackField(this.SamplePreset));
    }
    //this.MySample.Export();
    return phrase;
  }
  @Override public void ShallowLoad(JsonParse.HashNode phrase) {// ITextable
    super.ShallowLoad(phrase);
    HashMap<String, JsonParse.Node> Fields = phrase.ChildrenHash;
    this.Looped = Boolean.parseBoolean(IFactory.Utils.GetField(Fields, LoopedName, "true"));//Boolean.toString(this.Looped)));
    // Boolean.getBoolean(""); maybe use this instead. simpler, returns false if parse fails. 
    this.SamplePreset = IFactory.Utils.GetField(Fields, SamplePresetName, "");
    if (this.SamplePreset.equals(HornSampleName)) {
      this.Preset_Horn();
    }
  }
  @Override public void Consume(JsonParse.HashNode phrase, CollisionLibrary ExistingInstances) {// ITextable - Fill in all the values of an already-created object, including deep pointers.
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
    @Override public JsonParse.HashNode Export(CollisionLibrary HitTable) {// ITextable
      JsonParse.HashNode SelfPackage = super.Export(HitTable);// tested
      SelfPackage.AddSubPhrase(Globals.ObjectTypeName, IFactory.Utils.PackField(ObjectTypeName));
      return SelfPackage;
    }
    @Override public void ShallowLoad(JsonParse.HashNode phrase) {// ITextable
      super.ShallowLoad(phrase);
    }
    @Override public void Consume(JsonParse.HashNode phrase, CollisionLibrary ExistingInstances) {// ITextable - Fill in all the values of an already-created object, including deep pointers.
    }
    @Override public ISonglet Spawn_And_Attach_Songlet() {// reverse birth, use ONLY for deserialization
      SampleVoice songlet = new SampleVoice();
      this.Attach_Songlet(songlet);
      return songlet;
    }
    /* ********************************************************************************* */
    public static class Factory implements IFactory {// for serialization
      @Override public SampleVoice_OffsetBox Create(JsonParse.HashNode phrase, CollisionLibrary ExistingInstances) {
        SampleVoice_OffsetBox obox = new SampleVoice_OffsetBox();
        obox.Consume(phrase, ExistingInstances);
        return obox;
      }
//      @Override public Voice_OffsetBox Create(JsonParse.HashNode phrase, CollisionLibrary ExistingInstances) {
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
