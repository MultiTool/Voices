/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import java.util.ArrayList;

/**
 *
 * @author MultiTool
 */
public class LoopBox implements ISonglet, IDrawable {
  private double MyDuration = 1.0;// manually assigned duration, as loops are infinite otherwise
  private double Delay = 1.0;// time delay between loops
  private double Sustain = 1.0;// Opposite of Diminish. How much the loudness changes with each repeat. 
  private Project MyProject = null;
  private ISonglet Content = null;
  private OffsetBox ContentOBox = null;
  private CajaDelimitadora MyBounds = new CajaDelimitadora();
  /* ********************************************************************************* */
  public LoopBox() {
    MyBounds = new CajaDelimitadora();
  }
  /* ********************************************************************************* */
  @Override public OffsetBox Spawn_OffsetBox() {
    return this.Spawn_My_OffsetBox();
  }
  /* ********************************************************************************* */
  public Loop_OffsetBox Spawn_My_OffsetBox() {// for compose time
    Loop_OffsetBox lbox = new Loop_OffsetBox();// Deliver an OffsetBox specific to this type of songlet.
    lbox.Clear();
    lbox.Content = this;
    return lbox;
  }
  /* ********************************************************************************* */
  @Override public Singer Spawn_Singer() {
    return this.Spawn_My_Singer();
  }
  /* ********************************************************************************* */
  private Loop_Singer Spawn_My_Singer() {
    Loop_Singer LoopSinger = new Loop_Singer();
    LoopSinger.MySonglet = this;
    LoopSinger.MyProject = this.MyProject;// inherit project
    return LoopSinger;
  }
  /* ********************************************************************************* */
  @Override public int Get_Sample_Count(int SampleRate) {
    return (int) (this.MyDuration * SampleRate);
  }
  /* ********************************************************************************* */
  @Override public double Get_Duration() {
    return this.MyDuration;
  }
  /* ********************************************************************************* */
  public void Set_Duration(double duration) {
    this.MyDuration = duration;
  }
  /* ********************************************************************************* */
  @Override public double Get_Max_Amplitude() {
    throw new UnsupportedOperationException("Get_Max_Amplitude not supported yet.");
  }
  /* ********************************************************************************* */
  public void Set_Delay(double delay) {
    this.Delay = delay;
  }
  /* ********************************************************************************* */
  @Override public double Update_Durations() {// probably deprecated
    return this.Content.Update_Durations();
  }
  /* ********************************************************************************* */
  @Override public void Update_Guts(MetricsPacket metrics) {
    this.Set_Project(metrics.MyProject);
    metrics.MaxDuration = 0;
    this.Content.Update_Guts(metrics);
    metrics.MaxDuration = this.MyDuration;
  }
  /* ********************************************************************************* */
  @Override public void Sort_Me() {
    this.Content.Sort_Me();// not really the plan, but LoopBox doesn't have anything else to sort so why not
  }
  /* ********************************************************************************* */
  @Override public Project Get_Project() {
    return this.MyProject;
  }
  /* ********************************************************************************* */
  @Override public void Set_Project(Project project) {
    this.MyProject = project;
  }
  /* ********************************************************************************* */
  public OffsetBox Add_Content(ISonglet songlet) {
    songlet.Set_Project(this.MyProject);// child inherits project from me
    OffsetBox obox = songlet.Spawn_OffsetBox();
    this.ContentOBox = obox;
    this.Content = songlet;
    return obox;
  }
  /* ********************************************************************************* */
  @Override public void Draw_Me(Drawing_Context ParentDC) {// IDrawable
    //Drawing_Context ChildDC =  ParentDC
    // To do: Must clip or expand ParentDC's width to my own duration
    // To do: need fancy looped drawing code in here, this won't do
    this.ContentOBox.Draw_Me(ParentDC);
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  @Override public CajaDelimitadora GetBoundingBox() {// IDrawable
    return this.MyBounds;
  }
  @Override public void UpdateBoundingBox() {// IDrawable
    this.ContentOBox.UpdateBoundingBox();
    CajaDelimitadora ChildBBoxUnMapped = this.ContentOBox.GetBoundingBox();// project child limits into parent (my) space
    this.MyBounds.Include(ChildBBoxUnMapped);// Inefficient. We collect all the X information and then just throw it away. 
    this.MyBounds.Min.x = 0;
    this.MyBounds.Max.x = this.MyDuration;
  }
  /* ********************************************************************************* */
  public static class Loop_Singer extends Singer {
    protected LoopBox MySonglet;
    public ArrayList<Singer> NowPlaying = new ArrayList<>();// pool of currently playing voices
    double Prev_Time = 0;
    private Loop_OffsetBox MyOffsetBox;
    public int LoopCount;
    /* ********************************************************************************* */
    @Override public void Start() {
      IsFinished = false;
      Prev_Time = 0;
      LoopCount = 0;
      NowPlaying.clear();
    }
    /* ********************************************************************************* */
    @Override public void Skip_To(double EndTime) {
      EndTime = this.MyOffsetBox.MapTime(EndTime);// EndTime is now time internal to LoopBox's own coordinate system
      if (this.MySonglet.Content == null) {
        this.IsFinished = true;
        this.Prev_Time = EndTime;
        return;
      }
      double Clipped_EndTime = this.Tee_Up(EndTime);
      int NumPlaying = NowPlaying.size();
      int cnt = 0;
      while (cnt < NumPlaying) {// then play the whole pool
        Singer player = this.NowPlaying.get(cnt);
        player.Skip_To(Clipped_EndTime);
        cnt++;
      }
      cnt = 0;// now pack down the finished ones
      while (cnt < this.NowPlaying.size()) {
        Singer player = this.NowPlaying.get(cnt);
        if (player.IsFinished) {
          this.NowPlaying.remove(player);
        } else {
          cnt++;
        }
      }
      this.Prev_Time = EndTime;
    }
    /* ********************************************************************************* */
    @Override public void Render_To(double EndTime, Wave wave) {
      EndTime = this.MyOffsetBox.MapTime(EndTime);// EndTime is now time internal to LoopBox's own coordinate system
      double UnMapped_Prev_Time = this.MyOffsetBox.UnMapTime(this.Prev_Time);// get start time in parent coordinates
      if (this.MySonglet.Content == null) {
        this.IsFinished = true;
        wave.Init(UnMapped_Prev_Time, UnMapped_Prev_Time, this.MyProject.SampleRate);// wave times are in parent coordinates because the parent will be reading the wave data.
        this.Prev_Time = EndTime;
        return;
      }
      double Clipped_EndTime = this.Tee_Up(EndTime);
      double UnMapped_EndTime = this.MyOffsetBox.UnMapTime(Clipped_EndTime);
      wave.Init(UnMapped_Prev_Time, UnMapped_EndTime, this.MyProject.SampleRate);// wave times are in parent coordinates because the parent will be reading the wave data.
      Wave ChildWave = new Wave();
      int NumPlaying = NowPlaying.size();
      int cnt = 0;
      while (cnt < NumPlaying) {// then play the whole pool
        Singer player = this.NowPlaying.get(cnt);
        player.Render_To(Clipped_EndTime, ChildWave);
        ChildWave.Rebase_Time(this.MyOffsetBox.UnMapTime(ChildWave.StartTime));// shift child data to my parent's time base. hacky? 
        wave.Overdub(ChildWave);// sum/overdub the waves 
        cnt++;
      }
      cnt = 0;// now pack down the finished ones
      while (cnt < this.NowPlaying.size()) {
        Singer player = this.NowPlaying.get(cnt);
        if (player.IsFinished) {
          this.NowPlaying.remove(player);
        } else {
          cnt++;
        }
      }
      wave.Amplify(this.MyOffsetBox.LoudnessFactor);
      this.Prev_Time = EndTime;
    }
    /* ********************************************************************************* */
    private double Tee_Up(double EndTime) {// consolidating identical code 
      if (EndTime < 0) {
        EndTime = 0;// clip time
      }
      double Final_Time = this.MySonglet.Get_Duration();
      if (EndTime > Final_Time) {
        this.IsFinished = true;
        EndTime = Final_Time;// clip time
      }
      double Time;
      OffsetBox obox;
      while ((Time = this.LoopCount * this.MySonglet.Delay) <= EndTime) {// first find new songlets in this time range and add them to pool
        obox = MySonglet.Content.Spawn_OffsetBox();// problematic. may have to create a dedicated render time-only offset box
        obox.TimeOrg = Time;
        Singer singer = obox.Spawn_Singer();
        singer.Inherit(this);
        this.NowPlaying.add(singer);
        singer.Start();
        this.LoopCount++;
      }
      return EndTime;
    }
    /* ********************************************************************************* */
    @Override public IOffsetBox Get_OffsetBox() {
      return this.MyOffsetBox;
    }
  }
  /* ********************************************************************************* */
  public static class Loop_OffsetBox extends OffsetBox {// location box to transpose in pitch, move in time, etc. 
    public LoopBox Content;
    /* ********************************************************************************* */
    @Override public ISonglet GetContent() {
      return Content;
    }
    /* ********************************************************************************* */
    @Override public ISonglet.Singer Spawn_Singer() {// always always always override this
      return this.Spawn_My_Singer();
    }
    /* ********************************************************************************* */
    public Loop_Singer Spawn_My_Singer() {// for render time
      Loop_Singer ph = this.Content.Spawn_My_Singer();
      ph.MyOffsetBox = this;// to do: also transfer all of this box's offsets to player head. 
      return ph;
    }
    /* ********************************************************************************* */
    @Override public void Draw_Me(Drawing_Context ParentDC) {// IDrawable
      if (ParentDC.ClipBounds.Intersects(MyBounds)) {
        Drawing_Context ChildDC = new Drawing_Context(ParentDC, this);
        this.Content.Draw_Me(ChildDC);
      }
    }
    @Override public void UpdateBoundingBox() {// IDrawable
      this.Content.UpdateBoundingBox();
      this.Content.GetBoundingBox().UnMap(this, MyBounds);// project child limits into parent (my) space
    }
  }
}
