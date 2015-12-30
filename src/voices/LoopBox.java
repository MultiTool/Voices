/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
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
  Ghost_OffsetBox ghost = new Ghost_OffsetBox();// oy, this has to be refcounted because it may be used after recursion is done. 
  /* ********************************************************************************* */
  public LoopBox() {
    MyBounds = new CajaDelimitadora();
    ghost.Assign_Parent_Songlet(this);
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
    ghost.Copy_From(this.ContentOBox);
    ghost.Assign_Parent_Songlet(this);
    return obox;
  }
  /* ********************************************************************************* */
  @Override public void Draw_Me(Drawing_Context ParentDC) {// IDrawable
    double LeftBound = ParentDC.ClipBounds.Min.x - this.ContentOBox.GetBoundingBox().GetWidth();
    LeftBound = Math.max(0, LeftBound);
    int IterationStart = (int) Math.ceil(LeftBound / this.Delay);
    double RightBound = Math.min(ParentDC.ClipBounds.Max.x, this.MyDuration);
    Ghost_OffsetBox ghost = new Ghost_OffsetBox();
    ghost.Copy_From(this.ContentOBox);
    ghost.Assign_Parent_Songlet(this);
    int loopcnt = IterationStart;
    double Time;
    while ((Time = (loopcnt * this.Delay)) <= RightBound) {// keep drawing until child song's start is beyond our max X. 
      ghost.TimeOrg = Time;
      ghost.MyIteration = loopcnt;
      ghost.MyBounds.Rebase_Time(Time);
      ghost.Draw_Me(ParentDC);
      loopcnt++;
    }
    ghost.Delete_Me();
  }
  /* ********************************************************************************* */
  // @Override 
  public void Draw_Me_old(Drawing_Context ParentDC) {// IDrawable
    double LeftBound = ParentDC.ClipBounds.Min.x - this.ContentOBox.GetBoundingBox().GetWidth();
    LeftBound = Math.max(0, LeftBound);
    int IterationStart = (int) Math.ceil(LeftBound / this.Delay);
    double RightBound = Math.min(ParentDC.ClipBounds.Max.x, this.MyDuration);
    OffsetBox obox = this.ContentOBox.Clone_Me();// problematic. may have to create a dedicated render time-only offset box
    int loopcnt = IterationStart;
    double Time;
    while ((Time = (loopcnt * this.Delay)) <= RightBound) {// keep drawing until child song's start is beyond our max X. 
      obox.TimeOrg = Time;
      obox.MyBounds.Rebase_Time(Time);
      obox.Draw_Me(ParentDC);
      loopcnt++;
    }
    obox.Delete_Me();
  }
  @Override public CajaDelimitadora GetBoundingBox() {// IDrawable
    return this.MyBounds;
  }
  @Override public void UpdateBoundingBox() {// IDrawable
    this.ContentOBox.UpdateBoundingBox();
    this.UpdateBoundingBoxLocal();
  }
  @Override public void UpdateBoundingBoxLocal() {// IDrawable
    CajaDelimitadora ChildBBoxUnMapped = this.ContentOBox.GetBoundingBox();// project child limits into parent (my) space
    this.MyBounds.Include(ChildBBoxUnMapped);// Inefficient. We collect all the X information and then just throw it away. 
    this.MyBounds.Min.x = 0;
    this.MyBounds.Max.x = this.MyDuration;// #kludgey
    this.MyBounds.Sort_Me();
    //this.MyBounds.Assign(0, miny, this.MyDuration, maxy);
  }
  /* ********************************************************************************* */
  @Override public void GoFishing(HookAndLure Scoop) {// IDrawable
    if (Scoop.CurrentContext.SearchBounds.Intersects(MyBounds)) {
      int IterationNum = (int) Math.floor(Scoop.CurrentContext.Loc.x / this.Delay);
      ghost.Copy_From(this.ContentOBox);
      ghost.MyBounds.Min.x = 0;
      ghost.MyBounds.Max.x = this.MyDuration;// #kludgey
      ghost.Assign_Parent_Songlet(this);
      ghost.MyIteration = IterationNum;
      ghost.TimeOrg = IterationNum * this.Delay;
      ghost.GoFishing(Scoop);
    }
  }
  /* ********************************************************************************* */
  @Override public boolean Create_Me() {// IDeletable
    return true;
  }
  @Override public void Delete_Me() {// IDeletable
    this.MyBounds.Delete_Me();
    this.ghost.Delete_Me();
    this.ContentOBox.Delete_Me();
  }
  /* ********************************************************************************* */
  public static class Loop_Singer extends Singer {
    protected LoopBox MySonglet;
    //private Loop_OffsetBox MyOffsetBox;
    public ArrayList<Singer> NowPlaying = new ArrayList<>();// pool of currently playing voices
    double Prev_Time = 0;
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
          player.Get_OffsetBox().Delete_Me();// this offsetbox was temporary and not part of the composition
          player.Delete_Me();
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
          player.Get_OffsetBox().Delete_Me();// this offsetbox was temporary and not part of the composition
          player.Delete_Me();
        } else {
          cnt++;
        }
      }
      wave.Amplify(this.MyOffsetBox.LoudnessFactor);
      this.Prev_Time = EndTime;
    }
    /* ********************************************************************************* */
    private double Tee_Up_2(double EndTime) {// consolidating identical code 
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
      while ((Time = (this.LoopCount * this.MySonglet.Delay)) <= EndTime) {// first find new songlets in this time range and add them to pool
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
      Ghost_OffsetBox ghost;
      while ((Time = (this.LoopCount * this.MySonglet.Delay)) <= EndTime) {// first find new songlets in this time range and add them to pool
        ghost = new Ghost_OffsetBox();
        ghost.Assign_Parent_Songlet(this.MySonglet);
        ghost.MyIteration = this.LoopCount;
        ghost.TimeOrg = Time;
        Singer singer = ghost.Spawn_Singer();
        singer.Inherit(this);
        this.NowPlaying.add(singer);
        singer.Start();
        this.LoopCount++;
      }
      return EndTime;
    }
    /* ********************************************************************************* */
    @Override public OffsetBox Get_OffsetBox() {
      return this.MyOffsetBox;
    }
    /* ********************************************************************************* */
    @Override public boolean Create_Me() {// IDeletable
      super.Create_Me();
      return true;
    }
    @Override public void Delete_Me() {// IDeletable
      super.Delete_Me();
      int len = this.NowPlaying.size();
      for (int cnt = 0; cnt < len; cnt++) {
        try {
          this.NowPlaying.get(cnt).Delete_Me();
        } catch (Exception ex) {
          boolean nop = true;
        }
      }
      this.NowPlaying.clear();
    }
  }
  /* ********************************************************************************* */
  public static class Loop_OffsetBox extends OffsetBox {// location box to transpose LoopBox in pitch, move in time, etc. 
    public LoopBox Content;
    /* ********************************************************************************* */
    public Loop_OffsetBox() {
      this.MyBounds = new CajaDelimitadora();
    }
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
    @Override public OffsetBox Clone_Me() {// always override this thusly
      Loop_OffsetBox child = new Loop_OffsetBox();
      child.Copy_From(this);
      child.Content = this.Content;
      return child;
    }
    /* ********************************************************************************* */
    @Override public void Draw_Me(Drawing_Context ParentDC) {// for debugging. 
      super.Draw_Me(ParentDC);
      this.Draw_My_Bounds(ParentDC);
    }
    /* ********************************************************************************* */
    public void Draw_My_Bounds(Drawing_Context ParentDC) {// for debugging. break glass in case of emergency
      OffsetBox GlobalOffset = ParentDC.GlobalOffset;
      Graphics2D gr = ParentDC.gr;
      this.MyBounds.Sort_Me();
      int rx0 = (int) GlobalOffset.UnMapTime(this.MyBounds.Min.x);
      int rx1 = (int) GlobalOffset.UnMapTime(this.MyBounds.Max.x);
      int ry0 = (int) GlobalOffset.UnMapPitch(this.MyBounds.Min.y);
      int ry1 = (int) GlobalOffset.UnMapPitch(this.MyBounds.Max.y);
      if (ry1 < ry0) {// swap
        int temp = ry1;
        ry1 = ry0;
        ry0 = temp;
      }

      // thinner lines for more distal sub-branches
      double extra = (2.0 / (double) ParentDC.RecurseDepth);

      int buf = (int) Math.ceil(extra * 2);
      rx0 -= buf;
      rx1 += buf;
      ry0 -= buf;
      ry1 += buf;
      int wdt = rx1 - rx0;
      int hgt = ry1 - ry0;
      int cint = Globals.RandomGenerator.nextInt() % 256;
      Color col = new Color(cint);

      Stroke oldStroke = gr.getStroke();
      gr.setColor(Globals.ToAlpha(Color.red, 100));

      BasicStroke bs = new BasicStroke((float) (10.0), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
      gr.setStroke(bs);

      //ParentDC.gr.setColor(col);//Color.magenta
      gr.drawRect(rx0, ry0, wdt, hgt);

      gr.setStroke(oldStroke);
    }
  }
  /* ********************************************************************************* */
  private static class Ghost_OffsetBox extends OffsetBox {// TEMPORARY location box to shift child in time
    // Ghost obox is a filmy wrapper around LoopBox's child's real offset box. Ghost simply translates outside-world coordinates into looped coordinates and back.
    // Ghost wrapper is needed in LoopBox for 1) MoveTo, 2) Draw_Me (optional), and hopefully for 3) Singer. 
    public LoopBox Parent;
    public OffsetBox ContentLayer;
    OffsetBox TempObox = null;
    public int MyIteration = 0;// which loop iteration I represent
    /* ********************************************************************************* */
    public void Assign_Parent_Songlet(LoopBox Parent) {
      this.MyParentSong = this.Parent = Parent;
      this.ContentLayer = this.Parent.ContentOBox;
    }
    /* ********************************************************************************* */
    @Override public void Draw_Me(Drawing_Context ParentDC) {// IDrawable
      if (ParentDC.ClipBounds.Intersects(MyBounds)) {// MyBounds keep moving
        super.Draw_Dot(ParentDC, Color.magenta);
        Drawing_Context ChildDC = new Drawing_Context(ParentDC, this);// In C++ ChildDC will be a local variable from the stack, not heap. 
        // to do: map the real-time movements to our parent's Delay value, then reset the child obox's values 
        if (false) {// do we include the child's personal offset or skip around it? 
          this.ContentLayer.Draw_Me(ParentDC);
        } else {
          ISonglet songlet = this.GetContent();// skip around real offset box
          songlet.Draw_Me(ChildDC);
        }
        ChildDC.Delete_Me();
      }
    }
    /* ********************************************************************************* */
    @Override public ISonglet GetContent() {
      return ContentLayer.GetContent();
    }
    /* ********************************************************************************* */
    @Override public ISonglet.Singer Spawn_Singer() {// always always always override this
      Singer singer;
      if (false) {// darn. maybe here is where we could spawn the offset, assign the offset, then spawn its singer? eeeee. 
        this.TempObox = this.ContentLayer.GetContent().Spawn_OffsetBox();
        singer = this.TempObox.Spawn_Singer();
      } else {
        singer = this.ContentLayer.Spawn_Singer();
        singer.MyOffsetBox = this;// Point the singer's offset ref to me, the ghost, and shift the ghost as needed. 
      }
      return singer;
    }
    @Override public void MoveTo(double XLoc, double YLoc) {// IDrawable.IMoveable
      if (XLoc >= 0) {// don't go backward in time
        this.TimeOrg = XLoc;// do we use and keep these coordinates or are we just a wrapper around ContentLayer?
        this.OctaveLoc = YLoc;// no we have the coordinates but our child has different ones
        // to do: map the real-time movements to our parent's Delay value, then reset the child obox's values 
        double TempDelay = XLoc / this.MyIteration;
        this.Parent.Delay = TempDelay;
        if (false) {// maybe we don't really need to save Delay as part of our child's obox. 
          this.ContentLayer.MoveTo(TempDelay, YLoc);
        }
      }
    }
    /* ********************************************************************************* */
    @Override public OffsetBox Clone_Me() {// always override this thusly
      Ghost_OffsetBox child = new Ghost_OffsetBox();
      child.Copy_From(this);
      child.ContentLayer = this.ContentLayer;
      return child;
    }
  }
}
