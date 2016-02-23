package voices;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 *
 * @author MultiTool
 */
public class GroupBox implements ISonglet, IDrawable {
  public ArrayList<OffsetBox> SubSongs = new ArrayList<OffsetBox>();
  public double Duration = 0.0;
  private AudProject MyProject;
  public String MyName;// for debugging
  private double MaxAmplitude = 0.0;
  public CajaDelimitadora MyBounds;
  private int FreshnessTimeStamp;
  public String TraceText = "";// for debugging
  private int RefCount = 0;
  private boolean HighlightSpine = false;
  /* ********************************************************************************* */
  public GroupBox() {
    MyBounds = new CajaDelimitadora();
    RefCount = 0;
  }
  /* ********************************************************************************* */
  public OffsetBox Add_SubSong(ISonglet songlet, double TimeOffset, double OctaveOffset, double LoudnessFactor) {
    songlet.Set_Project(this.MyProject);// child inherits project from me
    OffsetBox obox = songlet.Spawn_OffsetBox();
    obox.MyParentSong = this;
    obox.TimeX = (TimeOffset);
    obox.OctaveY = (OctaveOffset);
    obox.LoudnessFactor = (LoudnessFactor);
    SubSongs.add(obox);
    return obox;
  }
  /* ********************************************************************************* */
  public void Add_SubSong(OffsetBox obox, double TimeOffset, double OctaveOffset, double LoudnessFactor) {// Add a songlet with its offsetbox already created.
    obox.GetContent().Set_Project(this.MyProject);// child inherits project from me
    obox.MyParentSong = this;
    obox.TimeX = (TimeOffset);
    obox.OctaveY = (OctaveOffset);
    obox.LoudnessFactor = (LoudnessFactor);
    SubSongs.add(obox);
  }
  /* ********************************************************************************* */
  public void Add_SubSong(OffsetBox obox) {// Add a songlet with its offsetbox already created and filled out.
    obox.GetContent().Set_Project(this.MyProject);// child inherits project from me
    obox.MyParentSong = this;
    SubSongs.add(obox);
  }
  /* ********************************************************************************* */
  public void Remove_SubSong(OffsetBox obox) {// Remove a songlet from my list.
    SubSongs.remove(obox);
  }
  /* ********************************************************************************* */
  @Override public double Get_Duration() {
    return this.Duration;
  }
  /* ********************************************************************************* */
  @Override public double Get_Max_Amplitude() {
    return this.MaxAmplitude;
  }
  /* ********************************************************************************* */
  public void Update_Max_Amplitude() {
    int len = this.SubSongs.size();
    OffsetBox pnt;
    double MaxAmp = 0.0;
    for (int pcnt = 0; pcnt < len; pcnt++) {
      pnt = this.SubSongs.get(pcnt);
      double Amp = pnt.Get_Max_Amplitude();
      MaxAmp += Amp;// this is overkill, we need to only sum those subsongs that overlap.
    }
    this.MaxAmplitude = MaxAmp;
  }
  /* ********************************************************************************* */
  @Override public double Update_Durations() {
    double MaxDuration = 0.0;
    double DurBuf = 0.0;
    int NumSubSongs = this.SubSongs.size();
    for (int cnt = 0; cnt < NumSubSongs; cnt++) {
      OffsetBox ob = this.SubSongs.get(cnt);
      ISonglet vb = ob.GetContent();
      //if (MaxDuration < (DurBuf = (ob.UnMapTime(vb.Update_Durations())))) {
      if (MaxDuration < (DurBuf = (ob.TimeX + vb.Update_Durations()))) {
        MaxDuration = DurBuf;
      }
    }
    this.Duration = MaxDuration;
    return MaxDuration;
  }
  /* ********************************************************************************* */
  @Override public void Update_Guts(MetricsPacket metrics) {
    if (this.FreshnessTimeStamp >= metrics.FreshnessTimeStamp) {// don't hit the same songlet twice on one update
      return;
    }
    this.Set_Project(metrics.MyProject);
    this.Sort_Me(); // to do: also recursively update all children guts without running update_durations more than once for each
    this.Update_Max_Amplitude();
    metrics.MaxDuration = 0.0;// redundant
    double MyMaxDuration = 0.0;
    double DurBuf = 0.0;
    int NumSubSongs = this.SubSongs.size();
    for (int cnt = 0; cnt < NumSubSongs; cnt++) {
      OffsetBox obx = this.SubSongs.get(cnt);
      ISonglet songlet = obx.GetContent();
      metrics.MaxDuration = 0.0;
      songlet.Update_Guts(metrics);
      if (MyMaxDuration < (DurBuf = (obx.TimeX + metrics.MaxDuration))) {
        MyMaxDuration = DurBuf;
      }
    }
    this.Duration = MyMaxDuration;
    metrics.MaxDuration = MyMaxDuration;
    this.FreshnessTimeStamp = metrics.FreshnessTimeStamp;
  }
  /* ********************************************************************************* */
  @Override public void Sort_Me() {// sorting by RealTime
    Collections.sort(this.SubSongs, new Comparator<OffsetBox>() {
      @Override public int compare(OffsetBox voice0, OffsetBox voice1) {
        return Double.compare(voice0.TimeX, voice1.TimeX);
      }
    });
  }
  /* ********************************************************************************* */
  @Override public Group_OffsetBox Spawn_OffsetBox() {// for compose time
    return this.Spawn_My_OffsetBox();
  }
  /* ********************************************************************************* */
  public Group_OffsetBox Spawn_My_OffsetBox() {// for compose time
    Group_OffsetBox lbox = new Group_OffsetBox();// Deliver an OffsetBox specific to this type of songlet.
    lbox.Clear();
    lbox.Content = this;
    lbox.Content.Ref_Songlet();
    return lbox;
  }
  /* ********************************************************************************* */
  @Override public ISonglet.Singer Spawn_Singer() {
    return this.Spawn_My_Singer();
  }
  /* ********************************************************************************* */
  public Group_Singer Spawn_My_Singer() {
    Group_Singer GroupPlayer = new Group_Singer();
    GroupPlayer.MySonglet = this;
    GroupPlayer.MyProject = this.MyProject;// inherit project
    return GroupPlayer;
  }
  /* ************************************************************************************************************************ */
  public int Tree_Search(double Time, int minloc, int maxloc) {// finds place where time would be inserted or replaced
    int medloc;
    while (minloc < maxloc) {
      medloc = (minloc + maxloc) >> 1; // >>1 is same as div 2, only faster.
      if (Time <= this.SubSongs.get(medloc).TimeX) {
        maxloc = medloc;
      }/* has to go through here to be found. */ else {
        minloc = medloc + 1;
      }
    }
    return minloc;
  }
  /* ********************************************************************************* */
  @Override public int Get_Sample_Count(int SampleRate) {
    return SampleRate * (int) this.Get_Duration();
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
  public void SetSpineHighlight(boolean Highlight) {
    this.HighlightSpine = Highlight;
  }
  /* ********************************************************************************* */
  @Override public void Draw_Me(DrawingContext ParentDC) {// IDrawable
    OffsetBox ChildOffsetBox;
    int len = this.SubSongs.size();

    int StartDex = 0;// not sure how to get the first within clip box without just iterating from 0. 
    int EndDex = len;

    // Draw Group spine
    Point2D.Double pntprev, pnt;
    Stroke oldStroke = ParentDC.gr.getStroke();
    BasicStroke bs;
    if (this.HighlightSpine) {
      ParentDC.gr.setColor(Color.yellow);
      // thinner lines for more distal sub-branches
      //BasicStroke bs = new BasicStroke((1.0f / ParentDC.RecurseDepth) * 40.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
      bs = new BasicStroke((1.0f / ParentDC.RecurseDepth) * 20.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    } else {
      ParentDC.gr.setColor(Color.darkGray);
      // thinner lines for more distal sub-branches
      //BasicStroke bs = new BasicStroke((1.0f / ParentDC.RecurseDepth) * 40.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
      bs = new BasicStroke((1.0f / ParentDC.RecurseDepth) * 10.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }
    ParentDC.gr.setStroke(bs);
    int pcnt = StartDex;
    if (false) {// #cleanme
      ChildOffsetBox = this.SubSongs.get(pcnt++);
      pntprev = ParentDC.To_Screen(ChildOffsetBox.TimeX, ChildOffsetBox.OctaveY);
    } else {
      pntprev = ParentDC.To_Screen(0, 0);
    }
    while (pcnt < EndDex) {//for (int pcnt = StartDex; pcnt < EndDex; pcnt++) {
      ChildOffsetBox = this.SubSongs.get(pcnt);
      pnt = ParentDC.To_Screen(ChildOffsetBox.TimeX, ChildOffsetBox.OctaveY);
      ParentDC.gr.drawLine((int) pntprev.x, (int) pntprev.y, (int) pnt.x, (int) pnt.y);
      pntprev = pnt;
      if (ParentDC.ClipBounds.Max.x < ChildOffsetBox.TimeX) {// break from loop if subsong starts after MaxX. 
        EndDex = pcnt;
        break;
      }
      pcnt++;
    }
    ParentDC.gr.setStroke(oldStroke);// restore line stroke

    // Draw children
    for (pcnt = StartDex; pcnt < EndDex; pcnt++) {
      ChildOffsetBox = this.SubSongs.get(pcnt);
      ChildOffsetBox.Draw_Me(ParentDC);
    }
  }
  @Override public CajaDelimitadora GetBoundingBox() {// IDrawable
    return this.MyBounds;
  }
  @Override public void UpdateBoundingBox() {// IDrawable
    OffsetBox ChildOffsetBox;
//    CajaDelimitadora ChildBBoxUnMapped;
//    this.MyBounds.Reset();
    int len = this.SubSongs.size();
    for (int pcnt = 0; pcnt < len; pcnt++) {
      ChildOffsetBox = this.SubSongs.get(pcnt);
      ChildOffsetBox.UpdateBoundingBox();
//      ChildBBoxUnMapped = ChildOffsetBox.GetBoundingBox();// project child limits into parent (my) space
//      this.MyBounds.Include(ChildBBoxUnMapped);
    }
    this.UpdateBoundingBoxLocal();
  }
  @Override public void UpdateBoundingBoxLocal() {// IDrawable
    OffsetBox ChildOffsetBox;
    CajaDelimitadora ChildBBoxUnMapped;
    this.MyBounds.Reset();
    int len = this.SubSongs.size();
    for (int pcnt = 0; pcnt < len; pcnt++) {
      ChildOffsetBox = this.SubSongs.get(pcnt);
      ChildBBoxUnMapped = ChildOffsetBox.GetBoundingBox();// project child limits into parent (my) space
      this.MyBounds.Include(ChildBBoxUnMapped);
    }
  }
  /* ********************************************************************************* */
  @Override public void GoFishing(Grabber Scoop) {// IDrawable
    if (Scoop.CurrentContext.SearchBounds.Intersects(MyBounds)) {// current search bounds are in parent coords
      int len = this.SubSongs.size();
      OffsetBox child;
      for (int pcnt = 0; pcnt < len; pcnt++) {
        child = this.SubSongs.get(pcnt);
        child.GoFishing(Scoop);
      }
    }
  }
  /* ********************************************************************************* */
  @Override public GroupBox Clone_Me() {// ICloneable
    GroupBox child = new GroupBox();
    child.Copy_From(this);
    return child;
  }
  /* ********************************************************************************* */
  @Override public GroupBox Deep_Clone_Me() {// ICloneable
    GroupBox child = new GroupBox();
    child.TraceText = "I am a clone";
    child.Copy_From(this);
    OffsetBox SubSongHandle;
    int len = this.SubSongs.size();
    for (int cnt = 0; cnt < len; cnt++) {
      SubSongHandle = this.SubSongs.get(cnt);
      child.Add_SubSong(SubSongHandle.Deep_Clone_Me());
    }
    return child;
  }
  /* ********************************************************************************* */
  public void Copy_From(GroupBox donor) {
    this.Duration = donor.Duration;
    this.Set_Project(donor.MyProject);
    this.MyName = donor.MyName;// for debugging
    this.MaxAmplitude = donor.MaxAmplitude;
    this.FreshnessTimeStamp = 0;// donor.FreshnessTimeStamp;
    this.MyBounds.Copy_From(donor.MyBounds);
  }
  /* ********************************************************************************* */
  public double DotProduct(double X0, double Y0, double X1, double Y1) {
    return X0 * X1 + Y0 * Y1;// length of projection from one vector onto another
  }
  /* ********************************************************************************* */
  public double DistanceFromLine(double LineX0, double LineY0, double LineX1, double LineY1, double XPnt, double YPnt) {// work in progress for drag and drop support
    double XDif = LineX1 - LineX0, YDif = LineY1 - LineY0;
    double DotProd = this.DotProduct(XDif, YDif, XPnt - LineX0, YPnt - LineY0);
    double XLoc = LineX0 + (XDif * DotProd);// point of intersection
    double YLoc = LineY0 + (YDif * DotProd);
    // to do: at this point we would like to test if the intersection is between the line's endpoints. 
//    double XLoc = (XDif * DotProd);// point of intersection
//    double YLoc = (YDif * DotProd);
//    double PntDX = (XLoc - XPnt); double PntDY = (YLoc - YPnt);
    double Distance = Math.hypot(XLoc - XPnt, YLoc - YPnt);
    return Distance;
  }
  /* ********************************************************************************* */
  public double HeightFromLine(double LineX0, double LineY0, double LineX1, double LineY1, double XPnt, double YPnt) {// work in progress for drag and drop support
    double YCross = LineYCross(LineX0, LineY0, LineX1, LineY1, XPnt);
    double Distance = YPnt - YCross;
    return Distance;
  }
  /* ********************************************************************************* */
  public double LineYCross(double LineX0, double LineY0, double LineX1, double LineY1, double XPnt) {// work in progress for drag and drop support
    double XDif = LineX1 - LineX0;// given a line and an X point, return the Y location of the intersect along that line
    double YCross;
    if (XDif == 0) {// If slope is infinite, just return the halfway point between the top and bottom of the line.
      YCross = (LineY0 + LineY1) / 2.0;
    } else {
      double YDif = LineY1 - LineY0;
      double Slope = YDif / XDif;
      double PointXOffset = (XPnt - LineX0);
      YCross = LineY0 + (PointXOffset * Slope);
    }
    return YCross;
  }
  /* ********************************************************************************* */
  public boolean HitsMyRunway(double XPnt, double YPnt) {// work in progress for drag and drop support
    double Limit = 0.1;// octaves
    int len = this.SubSongs.size();
    OffsetBox OBox, ClosestPoint = null;
    double XPrev = 0, YPrev = 0, YCross, YDist, MinDist = Double.POSITIVE_INFINITY;// will lose on first comparison
    int MinDex = Integer.MIN_VALUE;// meant to explode if we use it
    if (false) {
      for (int cnt = 0; cnt < len; cnt++) {
        OBox = this.SubSongs.get(cnt);
        if (XPrev <= XPnt && XPnt <= OBox.TimeX) {
          YCross = LineYCross(XPrev, YPrev, OBox.TimeX, OBox.OctaveY, XPnt);
          YDist = Math.abs(YPnt - YCross);
          if (MinDist > YDist) {
            MinDist = YDist;
            MinDex = cnt;
          }
        }
        XPrev = OBox.TimeX;
        YPrev = OBox.OctaveY;
      }
      if (MinDist < Limit) {// then we found one
        ClosestPoint = this.SubSongs.get(MinDex);
        return true;
      }
      return false;
    }
    // d'oh, better way
    OffsetBox LastBox = this.SubSongs.get(len - 1);
    if (0.0 <= XPnt && XPnt <= LastBox.TimeX) {// or this.MyBounds.Max.x) {
      int FoundDex = Tree_Search(XPnt, 0, len);
      if (FoundDex == 0) {
        XPrev = YPrev = 0;
      } else {
        OffsetBox PrevBox = this.SubSongs.get(FoundDex - 1);
        XPrev = PrevBox.TimeX;
        YPrev = PrevBox.OctaveY;
      }
      // to do: need condition if FoundDex is greater than len. beyond-end insertion would be nice.
      OBox = this.SubSongs.get(FoundDex);
      YCross = LineYCross(XPrev, YPrev, OBox.TimeX, OBox.OctaveY, XPnt);
      YDist = Math.abs(YPnt - YCross);
      if (YDist < Limit) {// then we found one
        ClosestPoint = this.SubSongs.get(FoundDex);
        return true;
      }
    }
    return false;
  }
  /* ********************************************************************************* */
  @Override public boolean Create_Me() {// IDeletable
    return true;
  }
  @Override public void Delete_Me() {// IDeletable
    this.MyBounds.Delete_Me();
    this.MyBounds = null;
    int len = this.SubSongs.size();
    for (int cnt = 0; cnt < len; cnt++) {
      this.SubSongs.get(cnt).Delete_Me();
    }
    this.SubSongs.clear();
    this.SubSongs = null;
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
  public static class Group_Singer extends Singer {
    protected GroupBox MySonglet;
    public ArrayList<Singer> NowPlaying = new ArrayList<Singer>();// pool of currently playing voices
    public int Current_Dex = 0;
    double Prev_Time = 0;
    //private Group_OffsetBox MyOffsetBox;
    /* ********************************************************************************* */
    @Override public void Start() {
      IsFinished = false;
      Current_Dex = 0;
      Prev_Time = 0;
      NowPlaying.clear();
    }
    /* ********************************************************************************* */
    @Override public void Skip_To(double EndTime) {
      if (this.IsFinished) {
        return;
      }
      EndTime = this.MyOffsetBox.MapTime(EndTime);// EndTime is now time internal to GroupBox's own coordinate system
      if (this.MySonglet.SubSongs.size() <= 0) {
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
          player.Delete_Me();
        } else {
          cnt++;
        }
      }
      this.Prev_Time = EndTime;
    }
    /* ********************************************************************************* */
    @Override public void Render_To(double EndTime, Wave wave) {
      if (this.IsFinished) {
        return;
      }
      EndTime = this.MyOffsetBox.MapTime(EndTime);// EndTime is now time internal to GroupBox's own coordinate system
      double UnMapped_Prev_Time = this.MyOffsetBox.UnMapTime(this.Prev_Time);// get start time in parent coordinates
      if (this.MySonglet.SubSongs.size() <= 0) {
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
        Singer player = null;
        try {
          player = this.NowPlaying.get(cnt);
        } catch (Exception ex) {
          boolean nop = true;
        }
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
      int NumSonglets = MySonglet.SubSongs.size();
      double Final_Start = this.MySonglet.SubSongs.get(NumSonglets - 1).TimeX;
      Final_Start = Math.min(Final_Start, EndTime);
      double Final_Time = this.MySonglet.Get_Duration();
      if (EndTime > Final_Time) {
        this.IsFinished = true;
        EndTime = Final_Time;// clip time
      }
      OffsetBox obox;
      while (this.Current_Dex < NumSonglets) {// first find new songlets in this time range and add them to pool
        obox = MySonglet.SubSongs.get(this.Current_Dex);
        if (Final_Start < obox.TimeX) {// repeat until cb start time overtakes EndTime
          break;
        }
        Singer singer = obox.Spawn_Singer();
        singer.Inherit(this);
        this.NowPlaying.add(singer);
        singer.Start();
        this.Current_Dex++;
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
        this.NowPlaying.get(cnt).Delete_Me();
      }
      this.NowPlaying.clear();
    }
  }

  /* ********************************************************************************* */
  public static class ScaleXHandle implements IDrawable.IMoveable, IDeletable {
    public CajaDelimitadora MyBounds = new CajaDelimitadora();
    public Group_OffsetBox ParentPoint;
    public double OctavesPerRadius = 0.007;
    private boolean IsSelected = false;
    /* ********************************************************************************* */
    public double GetUnitX() {
      return this.ParentPoint.MyBounds.GetWidth();
    }
    /* ********************************************************************************* */
    public double GetX() {
      double TimeScaleWidth = this.ParentPoint.ScaleX * this.GetUnitX();// Map timescale to screen pixels.
      return TimeScaleWidth;
    }
    public double GetY() {
      return this.ParentPoint.MyBounds.Max.y;
    }
    @Override public void MoveTo(double XLoc, double YLoc) {// IDrawable.IMoveable
      if (XLoc >= 0) {// don't go backward in time
        this.ParentPoint.ScaleX = XLoc / GetUnitX();
      }
      // ignore YLoc for now
    }
    @Override public boolean HitsMe(double XLoc, double YLoc) {// IDrawable.IMoveable
      System.out.print("** ScaleXHandle HitsMe:");
      boolean Hit = false;
      if (this.MyBounds.Contains(XLoc, YLoc)) {
        System.out.print(" InBounds ");
        double dist = Math.hypot(XLoc - this.GetX(), YLoc - (this.GetY() + this.OctavesPerRadius));
        if (dist <= this.OctavesPerRadius) {
          System.out.print(" Hit!");
          Hit = true;
        } else {
          System.out.print(" Missed!");
        }
      } else {
        System.out.print(" OutBounds ");
      }
      return Hit;
    }
    @Override public void SetSelected(boolean Selected) {// IDrawable.IMoveable
      this.IsSelected = Selected;
    }
    @Override public void Draw_Me(DrawingContext ParentDC) {
      // this is all nonsense at the moment, will have to fill it in with better stuff
      // Control points have the same space as their parent, so no need to create a local map.
      /*
       Point2D.Double pnt = ParentDC.To_Screen(this.ParentPoint.MyBounds.Max.x, this.ParentPoint.MyBounds.Max.y);
       double RadiusPixels = Math.abs(ParentDC.GlobalOffset.ScaleY) * OctavesPerRadius;
       double LoudnessHgt = this.ParentPoint.LoudnessFactor * this.ParentPoint.OctavesPerLoudness;
       double XLoc = ParentDC.GlobalOffset.UnMapPitch(this.ParentPoint.OctaveY + LoudnessHgt) - RadiusPixels;// My handle rests *upon* the line I control, so I don't occlude my VoicePoint. 
       RadiusPixels = Math.ceil(RadiusPixels);
       MonkeyBox.Draw_Dot2(ParentDC, XLoc, pnt.y, OctavesPerRadius, this.IsSelected, Globals.ToAlpha(Color.lightGray, 100));
       */
    }
    @Override public CajaDelimitadora GetBoundingBox() {
      return this.MyBounds;
    }
    @Override public void UpdateBoundingBox() {
      this.UpdateBoundingBoxLocal();
    }
    @Override public void UpdateBoundingBoxLocal() {
      double XLoc = this.GetX();
      double YLoc = this.GetY();
      double MinX = XLoc - this.OctavesPerRadius;
      double MaxX = XLoc + this.OctavesPerRadius;
      double MinY = YLoc - this.OctavesPerRadius;
      double MaxY = YLoc + this.OctavesPerRadius;
      this.MyBounds.Assign(MinX, MinY, MaxX, MaxY);
    }
    @Override public void GoFishing(Grabber Scoop) {
      System.out.println();
      System.out.print(" ScaleXHandle GoFishing: ");
      if (Scoop.CurrentContext.SearchBounds.Intersects(MyBounds)) {
        System.out.print(" InBounds, ");
        Scoop.ConsiderLeaf(this);
      }
      System.out.println();
    }
    /* ********************************************************************************* */
    @Override public ScaleXHandle Clone_Me() {// ICloneable
      ScaleXHandle child = new ScaleXHandle();
      return child;
    }
    /* ********************************************************************************* */
    @Override public ScaleXHandle Deep_Clone_Me() {// ICloneable
      ScaleXHandle child = new ScaleXHandle();
      child.OctavesPerRadius = this.OctavesPerRadius;
      child.ParentPoint = this.ParentPoint;
      child.MyBounds.Copy_From(this.MyBounds);
      return child;
    }
    /* ********************************************************************************* */
    @Override public boolean Create_Me() {
      return true;
    }
    @Override public void Delete_Me() {
      this.ParentPoint = null;
      this.MyBounds.Delete_Me();
      this.MyBounds = null;
    }
  }
  /* ********************************************************************************* */
  public static class Group_OffsetBox extends OffsetBox {// location box to transpose in pitch, move in time, etc. 
    public GroupBox Content = null;
    /* ********************************************************************************* */
    public Group_OffsetBox() {
      super();
      MyBounds = new CajaDelimitadora();
      this.Create_Me();
    }
    /* ********************************************************************************* */
    @Override public GroupBox GetContent() {
      return Content;
    }
    /* ********************************************************************************* */
    @Override public void Draw_Me(DrawingContext ParentDC) {// IDrawable
      super.Draw_Me(ParentDC);
      // here draw the Rescale_TimeX handle
    }
    /* ********************************************************************************* */
    @Override public Group_Singer Spawn_Singer() {// always always always override this
      return this.Spawn_My_Singer();
    }
    /* ********************************************************************************* */
    public Group_Singer Spawn_My_Singer() {// for render time
      Group_Singer ph = this.Content.Spawn_My_Singer();
      ph.MyOffsetBox = this;// Transfer all of this box's offsets to singer. 
      return ph;
    }
    /* ********************************************************************************* */
    @Override public Group_OffsetBox Clone_Me() {// always override this thusly
      Group_OffsetBox child = new Group_OffsetBox();
      child.Copy_From(this);
      child.Content = this.Content;
      return child;
    }
    /* ********************************************************************************* */
    @Override public Group_OffsetBox Deep_Clone_Me() {// ICloneable
      Group_OffsetBox child = new Group_OffsetBox();
      child.Copy_From(this);
      child.Content = this.Content.Deep_Clone_Me();
      return child;
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
}
