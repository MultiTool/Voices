/*
 *
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 * @author MultiTool
 */
public class GroupBox implements ISonglet, IDrawable {
  public ArrayList<OffsetBox> SubSongs = new ArrayList<>();
  public double Duration = 0.0;
  private AudProject MyProject;
  public String MyName;// for debugging
  private double MaxAmplitude = 0.0;
  public CajaDelimitadora MyBounds;
  /* ********************************************************************************* */
  public GroupBox() {
    MyBounds = new CajaDelimitadora();
  }
  /* ********************************************************************************* */
  public OffsetBox Add_SubSong(ISonglet songlet, double TimeOffset, double OctaveOffset, double LoudnessFactor) {
    songlet.Set_Project(this.MyProject);// child inherits project from me
    OffsetBox obox = songlet.Spawn_OffsetBox();
    obox.TimeX = (TimeOffset);
    obox.OctaveY =(OctaveOffset);
    obox.LoudnessFactor = (LoudnessFactor);
    SubSongs.add(obox);
    return obox;
  }
  /* ********************************************************************************* */
  public void Add_SubSong(OffsetBox obox, double TimeOffset, double OctaveOffset, double LoudnessFactor) {// Add a songlet with its offsetbox already created.
    obox.GetContent().Set_Project(this.MyProject);// child inherits project from me
    obox.TimeX = (TimeOffset);
    obox.OctaveY =(OctaveOffset);
    obox.LoudnessFactor = (LoudnessFactor);
    SubSongs.add(obox);
  }
  /* ********************************************************************************* */
  public void Add_SubSong(OffsetBox obox) {// Add a songlet with its offsetbox already created and filled out.
    obox.GetContent().Set_Project(this.MyProject);// child inherits project from me
    SubSongs.add(obox);
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
  @Override public OffsetBox Spawn_OffsetBox() {// for compose time
    return this.Spawn_My_OffsetBox();
  }
  /* ********************************************************************************* */
  public Group_OffsetBox Spawn_My_OffsetBox() {// for compose time
    Group_OffsetBox lbox = new Group_OffsetBox();// Deliver an OffsetBox specific to this type of songlet.
    lbox.Clear();
    lbox.Content = this;
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
  @Override public void Draw_Me(Drawing_Context ParentDC) {// IDrawable
    OffsetBox ChildOffsetBox;
    int len = this.SubSongs.size();

    int StartDex = 0;// not sure how to get the first within clip box without just iterating from 0. 
    int EndDex = len;

    // Draw Group spine
    Point2D.Double pntprev, pnt;
    Stroke oldStroke = ParentDC.gr.getStroke();
    ParentDC.gr.setColor(Color.darkGray);

    // thinner lines for more distal sub-branches
    //BasicStroke bs = new BasicStroke((1.0f / ParentDC.RecurseDepth) * 40.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    BasicStroke bs = new BasicStroke((1.0f / ParentDC.RecurseDepth) * 10.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
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
  @Override public void GoFishing(HookAndLure Scoop) {// IDrawable
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
  @Override public boolean Create_Me() {// IDeletable
    return true;
  }
  @Override public void Delete_Me() {// IDeletable
    this.MyBounds.Delete_Me();
    int len = this.SubSongs.size();
    for (int cnt = 0; cnt < len; cnt++) {
      this.SubSongs.get(cnt).Delete_Me();
    }
    this.SubSongs.clear();
  }
  /* ********************************************************************************* */
  public static class Group_Singer extends Singer {
    protected GroupBox MySonglet;
    public ArrayList<Singer> NowPlaying = new ArrayList<>();// pool of currently playing voices
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
  public static class Group_OffsetBox extends OffsetBox {// location box to transpose in pitch, move in time, etc. 
    public GroupBox Content = null;
    /* ********************************************************************************* */
    public Group_OffsetBox() {
      super();
      MyBounds = new CajaDelimitadora();
      this.Create_Me();
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
    public Group_Singer Spawn_My_Singer() {// for render time
      Group_Singer ph = this.Content.Spawn_My_Singer();
      ph.MyOffsetBox = this;// Transfer all of this box's offsets to singer. 
      return ph;
    }
    /* ********************************************************************************* */
    @Override public OffsetBox Clone_Me() {// always override this thusly
      Group_OffsetBox child = new Group_OffsetBox();
      child.Copy_From(this);
      child.Content = this.Content;
      return child;
    }
  }
}
