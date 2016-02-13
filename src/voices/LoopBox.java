package voices;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 *
 * @author MultiTool
 */
public class LoopBox implements ISonglet, IDrawable {
  public ArrayList<Ghost_OffsetBox> SubSongs = new ArrayList<Ghost_OffsetBox>();
  private double MyDuration = 1.0;// manually assigned duration, as loops are infinite otherwise
  private double Delay = 1.0;// time delay between loops
  private double Sustain = 1.0;// Opposite of Diminish. How much the loudness changes with each repeat. 
  private AudProject MyProject = null;
  private ISonglet Content = null;
  private OffsetBox ContentOBox = null;
  private CajaDelimitadora MyBounds = new CajaDelimitadora();
  Ghost_OffsetBox ghost = new Ghost_OffsetBox();// oy, this has to be refcounted because it may be used after recursion is done. 
  private int FreshnessTimeStamp;
  private int RefCount = 0;
  /* ********************************************************************************* */
  public LoopBox() {
    MyBounds = new CajaDelimitadora();
    ghost.Assign_Parent_Songlet(this);
    RefCount = 0;
  }
  /* ********************************************************************************* */
  @Override public Loop_OffsetBox Spawn_OffsetBox() {
    return this.Spawn_My_OffsetBox();
  }
  /* ********************************************************************************* */
  public Loop_OffsetBox Spawn_My_OffsetBox() {// for compose time
    Loop_OffsetBox lbox = new Loop_OffsetBox();// Deliver an OffsetBox specific to this type of songlet.
    lbox.Clear();
    lbox.Content = this;
    lbox.Content.Ref_Songlet();
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
    return this.Content.Get_Max_Amplitude();// does not really work when content overlaps itself
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
    if (this.FreshnessTimeStamp >= metrics.FreshnessTimeStamp) {// don't hit the same songlet twice on one update
      return;
    }
    this.Set_Project(metrics.MyProject);
    metrics.MaxDuration = 0;
    this.Content.Update_Guts(metrics);
    metrics.MaxDuration = this.MyDuration;
    this.FreshnessTimeStamp = metrics.FreshnessTimeStamp;
  }
  /* ********************************************************************************* */
  @Override public void Sort_Me() {
    this.Content.Sort_Me();// not really the plan, but LoopBox doesn't have anything else to sort so why not
  }
  /* ********************************************************************************* */
  @Override public AudProject Get_Project() {
    return this.MyProject;
  }
  /* ********************************************************************************* */
  @Override public void Set_Project(AudProject project) {
    this.MyProject = project;
  }
  /* ********************************************************************************* */
  @Override public int FreshnessTimeStamp_g() {// ISonglet
    return this.FreshnessTimeStamp;
  }
  @Override public void FreshnessTimeStamp_s(int TimeStampNew) {// ISonglet
    this.FreshnessTimeStamp = TimeStampNew;
  }
  /* ********************************************************************************* */
  public OffsetBox Add_Content(ISonglet songlet) {
    songlet.Set_Project(this.MyProject);// child inherits project from me
    OffsetBox obox = songlet.Spawn_OffsetBox();
    this.ContentOBox = obox;
    this.Content = songlet;
    this.SubSongs.clear();
    ghost.Copy_From(this.ContentOBox);
    ghost.Assign_Parent_Songlet(this);
    return obox;
  }
  /* ********************************************************************************* */
  @Override public void Draw_Me(DrawingContext ParentDC) {// IDrawable

    if (true) {
      // clip to loop duration
      CajaDelimitadora tempbounds = new CajaDelimitadora();
      ParentDC.GlobalOffset.UnMap(this.MyBounds, tempbounds);
      Rectangle2D rect = new Rectangle2D.Float();
      double YBuf = 200;
      rect.setRect(tempbounds.Min.x, tempbounds.Min.y - YBuf, tempbounds.Max.x - tempbounds.Min.x, (tempbounds.Max.y - tempbounds.Min.y) + YBuf * 2);
      tempbounds.Delete_Me();
      ParentDC.gr.clip(rect);
    }

    double LeftBound = ParentDC.ClipBounds.Min.x - this.ContentOBox.GetBoundingBox().GetWidth();
    LeftBound = Math.max(0, LeftBound);
    int IterationStart = (int) Math.ceil(LeftBound / this.Delay);
    double RightBound = Math.min(ParentDC.ClipBounds.Max.x, this.MyDuration);
    Ghost_OffsetBox LGhost = new Ghost_OffsetBox();
    LGhost.Copy_From(this.ghost);
    LGhost.Assign_Parent_Songlet(this);
    int loopcnt = IterationStart;
    double Time;
    if (loopcnt == 0) {// first iteration does not show ghost's grab circle
      LGhost.Rebase_Time(0);
      LGhost.MyIteration = loopcnt;
      LGhost.Draw_Me_Zero(ParentDC);
      loopcnt++;
    }
    while ((Time = (loopcnt * this.Delay)) <= RightBound) {// keep drawing until child song's start is beyond our max X. 
      LGhost.TimeX = Time;
      LGhost.MyIteration = loopcnt;
      LGhost.MyBounds.Rebase_Time(Time + this.ghost.MyBounds.Min.x);
      LGhost.Draw_Me(ParentDC);
      loopcnt++;
    }
    LGhost.Delete_Me();
    ParentDC.gr.setClip(null);
  }
  /* ********************************************************************************* */
  @Override public CajaDelimitadora GetBoundingBox() {// IDrawable
    return this.MyBounds;
  }
  @Override public void UpdateBoundingBox() {// IDrawable
    this.ghost.UpdateBoundingBox();
    this.ContentOBox.UpdateBoundingBoxLocal();// This will go away after we ditch all dependencies on ContentOBox. 
    this.UpdateBoundingBoxLocal();
  }
  @Override public void UpdateBoundingBoxLocal() {// IDrawable
    // CajaDelimitadora ChildBBoxUnMapped = this.ContentOBox.GetBoundingBox();// project child limits into parent (my) space
    CajaDelimitadora ChildBBoxUnMapped = this.ghost.GetBoundingBox();// project child limits into parent (my) space
    this.MyBounds.Include(ChildBBoxUnMapped);// Inefficient. We collect all the X information and then just throw it away. 
    this.MyBounds.Max.x = this.MyDuration;// #kludgey
    this.MyBounds.Sort_Me();
    //this.MyBounds.Assign(minx, miny, this.MyDuration, maxy);
  }
  /* ********************************************************************************* */
  @Override public void GoFishing(Grabber Scoop) {// IDrawable
    CajaDelimitadora SearchBounds = Scoop.CurrentContext.SearchBounds;
    if (SearchBounds.Intersects(MyBounds)) {
      double LeftBound = SearchBounds.Min.x - this.ghost.GetBoundingBox().GetWidth();
      LeftBound = Math.max(0, LeftBound);
      int IterationStart = (int) Math.ceil(LeftBound / this.Delay);
      double RightBound = Math.min(SearchBounds.Max.x, this.MyDuration);
      Ghost_OffsetBox LGhost;
      int loopcnt = IterationStart;
      double Time, MovingLeftBound;
      if (loopcnt == 0) {// first iteration does not show ghost's grab circle
        LGhost = new Ghost_OffsetBox();// #kludgey, this would be a memory leak in C++. 
        LGhost.Copy_From(this.ghost);
        LGhost.Assign_Parent_Songlet(this);
        LGhost.Rebase_Time(0);
        LGhost.MyIteration = loopcnt;
        LGhost.GoFishing_Zero(Scoop);
        loopcnt++;
      }
      double RelativeMinBound = this.ghost.MyBounds.Min.x;// ghost graphic left bound is a little bit negative due to its grab circle.
      while ((MovingLeftBound = (Time = (loopcnt * this.Delay)) + RelativeMinBound) <= RightBound) {// keep looking until child song's start is beyond our max X. 
        LGhost = new Ghost_OffsetBox();// #kludgey, this would be a memory leak in C++. 
        LGhost.Copy_From(this.ghost);
        LGhost.Assign_Parent_Songlet(this);
        LGhost.TimeX = Time;
        LGhost.MyBounds.Rebase_Time(MovingLeftBound);
        LGhost.MyIteration = loopcnt;
        LGhost.GoFishing(Scoop);
        loopcnt++;
      }
    }
  }
  /* ********************************************************************************* */
  @Override public LoopBox Clone_Me() {// ICloneable
    LoopBox child = new LoopBox();
    return child;
  }
  /* ********************************************************************************* */
  @Override public LoopBox Deep_Clone_Me() {// ICloneable
    LoopBox child = this.Clone_Me();
    child.Copy_From(this);
    child.Add_Content(this.Content.Deep_Clone_Me());
    //child.Add_Content(this.ContentOBox.GetContent().Deep_Clone_Me());
    //child.ContentOBox = this.ContentOBox.Deep_Clone_Me();
    //child.Content = this.Content.Deep_Clone_Me();
    return child;
  }
  /* ********************************************************************************* */
  public void Copy_From(LoopBox donor) {
    this.MyDuration = donor.MyDuration;// manually assigned duration, as loops are infinite otherwise
    this.Delay = donor.Delay;// time delay between loops
    this.Sustain = donor.Sustain;// Opposite of Diminish. How much the loudness changes with each repeat. 
    this.MyProject = donor.MyProject;
    this.MyBounds.Copy_From(donor.MyBounds);
    this.FreshnessTimeStamp = 0;
    //this.SubSongs;
    //this. Content = null;
    //this. ContentOBox = null;
    //this.ghost = new Ghost_OffsetBox();// oy, this has to be refcounted because it may be used after recursion is done. 
  }
  /* ********************************************************************************* */
  @Override public boolean Create_Me() {// IDeletable
    return true;
  }
  @Override public void Delete_Me() {// IDeletable
    this.MyBounds.Delete_Me();
    this.MyBounds = null;
    this.ghost.Delete_Me();
    this.ghost = null;
    if (this.ContentOBox != null) {
      this.ContentOBox.Delete_Me();
      this.ContentOBox = null;
    }
  }
  /* ********************************************************************************* */
  @Override public int Ref_Songlet() {// ISonglet Reference Counting: increment ref counter and return new value just for kicks
    return ++this.RefCount;
  }
  @Override public int UnRef_Songlet() {// ISonglet Reference Counting: decrement ref counter and return new value just for kicks
    return --this.RefCount;
  }
  @Override public int GetRefCount() {// ISonglet Reference Counting: get number of references for serialization
    return this.RefCount;
  }
  /* ********************************************************************************* */
  public static class Loop_Singer extends Singer {
    protected LoopBox MySonglet;
    //private Loop_OffsetBox MyOffsetBox;
    public ArrayList<Singer> NowPlaying = new ArrayList<Singer>();// pool of currently playing voices
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
        ghost.TimeX = Time;
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
    @Override public Loop_OffsetBox Clone_Me() {// ICloneable always override this thusly
      Loop_OffsetBox child = new Loop_OffsetBox();
      child.Copy_From(this);
      child.Content = this.Content;
      return child;
    }
    /* ********************************************************************************* */
    @Override public Loop_OffsetBox Deep_Clone_Me() {// ICloneable
      Loop_OffsetBox child = this.Clone_Me();
      child.Content = this.Content.Deep_Clone_Me();
      return child;
    }
    /* ********************************************************************************* */
    @Override public void Draw_Me(DrawingContext ParentDC) {
      if (false) {
        // clip to loop duration
        CajaDelimitadora tempbounds = new CajaDelimitadora();
        ParentDC.GlobalOffset.UnMap(this.MyBounds, tempbounds);
        Rectangle2D rect = new Rectangle2D.Float();
        rect.setRect(tempbounds.Min.x, tempbounds.Min.y, tempbounds.Max.x, tempbounds.Max.y);
        tempbounds.Delete_Me();
        ParentDC.gr.clip(rect);
      }

      super.Draw_Me(ParentDC);
      this.Draw_My_Bounds(ParentDC);
      ParentDC.gr.setClip(null);
    }
    /* ********************************************************************************* */
    @Override public void Draw_My_Bounds(DrawingContext ParentDC) {// for debugging. break glass in case of emergency
      OffsetBox GlobalOffset = ParentDC.GlobalOffset;
      Graphics2D gr = ParentDC.gr;
      this.MyBounds.Sort_Me();
      int rx0 = (int) GlobalOffset.UnMapTime(this.MyBounds.Min.x);
      int rx1 = (int) GlobalOffset.UnMapTime(this.MyBounds.Max.x);
      int ry0 = (int) GlobalOffset.UnMapPitch(this.MyBounds.Min.y);
      int ry1 = (int) GlobalOffset.UnMapPitch(this.MyBounds.Max.y);

      //gr.drawLine(rx0, ry0, rx0, ry1);
      gr.drawLine(rx1, ry0, rx1, ry1);// Just draw the ending cutoff line for the loop set.

      ParentDC.gr.setClip(null);
    }
    /* ********************************************************************************* */
    @Override public boolean Create_Me() {// IDeletable
      return true;
    }
    @Override public void Delete_Me() {// IDeletable
      super.Delete_Me();
      if (this.Content != null) {
        if (this.Content.UnRef_Songlet() <= 0) {
          this.Content.Delete_Me();
          this.Content = null;
        }
      }
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
    public void Draw_Me_Zero(DrawingContext ParentDC) {// iteration 0 of child song does not use ghost box handle
      if (ParentDC.ClipBounds.Intersects(MyBounds)) {// MyBounds keep moving
        DrawingContext ChildDC = new DrawingContext(ParentDC, this);// In C++ ChildDC will be a local variable from the stack, not heap. 
        // Map the real-time movements to our parent's Delay value 
        ISonglet songlet = this.GetContent();// skip around the child's personal offset
        songlet.Draw_Me(ChildDC);
        ChildDC.Delete_Me();
      }
    }
    public void GoFishing_Zero(Grabber Scoop) {// iteration 0 of child song does not use ghost box handle
      if (Scoop.CurrentContext.SearchBounds.Intersects(MyBounds)) {
        Scoop.AddBoxToStack(this);
        this.GetContent().GoFishing(Scoop);
        Scoop.DecrementStack();
      }
    }
    /* ********************************************************************************* */
    @Override public void Draw_Me(DrawingContext ParentDC) {// IDrawable
      if (ParentDC.ClipBounds.Intersects(MyBounds)) {// MyBounds keep moving
        super.Draw_Dot(ParentDC, Color.magenta);
        // this.Draw_My_Bounds(ParentDC);
        DrawingContext ChildDC = new DrawingContext(ParentDC, this);// In C++ ChildDC will be a local variable from the stack, not heap. 
        // Map the real-time movements to our parent's Delay value 
        ISonglet songlet = this.GetContent();// skip around the child's personal offset
        songlet.Draw_Me(ChildDC);
        ChildDC.Delete_Me();
      }
    }
    /* ********************************************************************************* */
    @Override public void UpdateBoundingBox() {// IDrawable
      ISonglet Content = this.GetContent();
      Content.UpdateBoundingBox();
      this.UpdateBoundingBoxLocal();
    }
    @Override public void UpdateBoundingBoxLocal() {// IDrawable
      ISonglet Content = this.GetContent();
      Content.UpdateBoundingBoxLocal();// either this
      this.UnMap(Content.GetBoundingBox(), MyBounds);// project child limits into parent (my) space

      if (false) {
        this.MyBounds.Min.x = 0;
        this.MyBounds.Max.x = this.Parent.MyDuration;// #kludgey
      }
      this.MyIteration = 0;
      this.TimeX = 0;

      // include my bubble in bounds
      this.MyBounds.IncludePoint(this.TimeX - OctavesPerRadius, this.OctaveY - OctavesPerRadius);
      this.MyBounds.IncludePoint(this.TimeX + OctavesPerRadius, this.OctaveY + OctavesPerRadius);
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
        this.TimeX = XLoc;// do we use and keep these coordinates or are we just a wrapper around ContentLayer?
      }
      this.OctaveY = YLoc;// no we have the coordinates but our child has different ones
      // to do: map the real-time movements to our parent's Delay value, then reset the child obox's values 
      double TempDelay = XLoc / this.MyIteration;
      this.Parent.Delay = TempDelay;
      if (false) {// maybe we don't really need to save Delay as part of our child's obox. 
        this.ContentLayer.MoveTo(TempDelay, YLoc);
      }
    }
    /* ********************************************************************************* */
    @Override public Ghost_OffsetBox Clone_Me() {// ICloneable always override this thusly
      Ghost_OffsetBox child = new Ghost_OffsetBox();
      child.Copy_From(this);
      child.ContentLayer = this.ContentLayer;
      return child;
    }
    /* ********************************************************************************* */
    @Override public Ghost_OffsetBox Deep_Clone_Me() {// ICloneable
      Ghost_OffsetBox child = this.Clone_Me();
      child.ContentLayer = this.ContentLayer.Deep_Clone_Me();// ??? 
      //throw new UnsupportedOperationException("Never clone a ghost!");
      return child;
    }
  }
}
