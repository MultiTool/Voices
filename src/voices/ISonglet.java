package voices;

/**
 *
 * @author MultiTool
 */
public interface ISonglet extends IDrawable, IDeletable, ITextable {// Cancionita
  /* ********************************************************************************* */
  public abstract static class Singer implements IDeletable {// Cantante
    // public static class Singer extends OffsetBox { // Cantante
    public AudProject MyProject;
    double Inherited_Time = 0.0, Inherited_Octave = 0.0, Inherited_Loudness = 1.0;// time, octave, and loudness context
    double Inherited_ScaleX = 1.0;// tempo rescale context
    double Inherited_ScaleY = 1.0;// 'temper' context, which we will NEVER use unless we want to make ugly anharmonic noise.
    double Inherited_OctaveRate = 0.0;// bend context, change dyanimcally while rendering
    public MonkeyBox InheritedMap = new OffsetBox();// Global Offset is transformation to and from samples. Possible replacement for Inherited_* 
    public boolean IsFinished = false;
    public Singer ParentSinger;
    protected OffsetBox MyOffsetBox = null;
    // public boolean exists = Create_Me();
    /* ********************************************************************************* */
    public Singer() {
      this.Create_Me();
    }
    /* ********************************************************************************* */
    public void Start() {
      IsFinished = false;
    }
    /* ********************************************************************************* */
    public void Skip_To(double EndTime) {
    }
    /* ********************************************************************************* */
    public void Render_To(double EndTime, Wave wave) {
    }
    /* ********************************************************************************* */
    public void Inherit(Singer parent) {// accumulate transformations
      this.ParentSinger = parent;
      Inherited_Time = parent.Inherited_Time;
      Inherited_Octave = parent.Inherited_Octave;
      Inherited_Loudness = parent.Inherited_Loudness;
      Inherited_ScaleX = parent.Inherited_ScaleX;
      Inherited_ScaleY = parent.Inherited_ScaleY;
      this.InheritedMap.Copy_From(parent.InheritedMap);
      this.Compound();
    }
    /* ********************************************************************************* */
    public void Compound() {// accumulate my own transformation
      this.Compound(this.Get_OffsetBox());
    }
    /* ********************************************************************************* */
    public void Compound(MonkeyBox donor) {// accumulate my own transformation
      this.Inherited_Time += (this.Inherited_ScaleX * donor.TimeX);// to do: combine matrices here. 
      this.Inherited_Octave += (this.Inherited_ScaleY * donor.OctaveY);
      this.Inherited_Loudness *= donor.LoudnessFactor;
      this.Inherited_ScaleX *= donor.ScaleX;
      this.Inherited_ScaleY *= donor.ScaleY;
      this.InheritedMap.Compound(donor);
    }
    /* ********************************************************************************* */
    public abstract OffsetBox Get_OffsetBox();
    /* ********************************************************************************* */
    @Override public boolean Create_Me() {// IDeletable
      return true;
    }
    @Override public void Delete_Me() {// IDeletable
    }
  }
  /* ********************************************************************************* */
  public OffsetBox Spawn_OffsetBox();// for compose time
  /* ********************************************************************************* */
  public ISonglet.Singer Spawn_Singer();// for render time
  /* ********************************************************************************* */
  public int Get_Sample_Count(int SampleRate);
  /* ********************************************************************************* */
  public double Get_Duration();
  /* ********************************************************************************* */
  public double Get_Max_Amplitude();
  /* ********************************************************************************* */
  public double Update_Durations();
  /* ********************************************************************************* */
  public void Update_Guts(MetricsPacket metrics);
  /* ********************************************************************************* */
  public void Sort_Me();
  /* ********************************************************************************* */
  public AudProject Get_Project();
  /* ********************************************************************************* */
  public void Set_Project(AudProject project);
  /* ********************************************************************************* */
  public int FreshnessTimeStamp_g();
  public void FreshnessTimeStamp_s(int TimeStampNew);
  /* ********************************************************************************* */
  @Override
  ISonglet Deep_Clone_Me();
  /* ********************************************************************************* */
  int Ref_Songlet();// Reference Counting: increment ref counter and return new value just for kicks
  int UnRef_Songlet();// Reference Counting: decrement ref counter and return new value just for kicks
  int GetRefCount();// Reference Counting: get number of references for serialization
  //  Possible RefCount pattern:
  //  (MyPointer = DeletableObject).Ref_Songlet();// ref pattern 
  //  MyPointer = MyPointer.UnRef_Songlet();// unref pattern, unref returns null?
  /* ********************************************************************************* */
  public static class MetricsPacket {
    public double MaxDuration = 0.0;
    public AudProject MyProject = null;
    public int FreshnessTimeStamp = 1;
  }
}
