/*
 *
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

/**
 *
 * @author MultiTool
 */
public interface ISonglet extends IDrawable, IDeletable {// Cancionita
  /* ********************************************************************************* */
  public abstract static class Singer implements IDeletable {// Cantante
    // public static class Singer extends OffsetBox { // Cantante
    public AudProject MyProject;
    double Inherited_Time = 0.0, Inherited_Octave = 0.0, Inherited_Loudness = 1.0;// time, octave, and loudness context
    double Inherited_OctaveRate = 0.0;// bend context, change dyanimcally while rendering
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
      this.Compound();
    }
    /* ********************************************************************************* */
    public void Compound() {// accumulate my own transformation
      this.Compound(this.Get_OffsetBox());
    }
    /* ********************************************************************************* */
    public void Compound(OffsetBox donor) {// accumulate my own transformation
      Inherited_Time += donor.TimeX;
      Inherited_Octave += donor.OctaveY;
      Inherited_Loudness *= donor.LoudnessFactor;
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
  @Override ISonglet Deep_Clone_Me();
  /* ********************************************************************************* */
  public static class MetricsPacket {
    public double MaxDuration = 0.0;
    public AudProject MyProject = null;
    public int FreshnessTimeStamp = 1;
  }
}
