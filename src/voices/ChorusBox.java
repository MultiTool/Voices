/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 * @author MultiTool
 */
public class ChorusBox implements ISonglet, IDrawable {
  public ArrayList<OffsetBox> SubSongs = new ArrayList<>();
  public double Duration = 0.0;
  private Project MyProject;
  public String MyName;// for debugging
  private double MaxAmplitude = 0.0;
  public CajaDelimitadora MyBounds;
  /* ********************************************************************************* */
  public static class ChorusBox_Singer extends Singer {
    protected ChorusBox MySonglet;
    public ArrayList<Singer> NowPlaying = new ArrayList<>();// pool of currently playing voices
    public int Current_Dex = 0;
    double Prev_Time = 0;
    private Chorus_OffsetBox MyOffsetBox;
    /* ********************************************************************************* */
    @Override public void Start() {
      IsFinished = false;
      Current_Dex = 0;
      Prev_Time = 0;
      NowPlaying.clear();
    }
    /* ********************************************************************************* */
    @Override public void Skip_To(double EndTime) {
      EndTime = this.MyOffsetBox.MapTime(EndTime);// EndTime is now time internal to ChorusBox's own coordinate system
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
        } else {
          cnt++;
        }
      }
      this.Prev_Time = EndTime;
    }
    /* ********************************************************************************* */
    @Override public void Render_To(double EndTime, Wave wave) {
      EndTime = this.MyOffsetBox.MapTime(EndTime);// EndTime is now time internal to ChorusBox's own coordinate system
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
        Singer player = this.NowPlaying.get(cnt);
        player.Render_To(Clipped_EndTime, ChildWave);
        //ChildWave.Shift_Timebase(this.MyOffsetBox.TimeOrg);// shift child data to my parent's time base. hacky? 
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
      int NumSonglets = MySonglet.SubSongs.size();
      double Final_Start = this.MySonglet.SubSongs.get(NumSonglets - 1).TimeOrg;
      Final_Start = Math.min(Final_Start, EndTime);
      double Final_Time = this.MySonglet.Get_Duration();
      if (EndTime > Final_Time) {
        this.IsFinished = true;
        EndTime = Final_Time;// clip time
      }
      OffsetBox obox;
      while (this.Current_Dex < NumSonglets) {// first find new songlets in this time range and add them to pool
        obox = MySonglet.SubSongs.get(this.Current_Dex);
        if (Final_Start < obox.TimeOrg) {// repeat until cb start time overtakes EndTime
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
    @Override public IOffsetBox Get_OffsetBox() {
      return this.MyOffsetBox;
    }
  }
  /* ********************************************************************************* */
  public static class Chorus_OffsetBox extends OffsetBox {// location box to transpose in pitch, move in time, etc. 
    public ChorusBox Content;
    /* ********************************************************************************* */
    @Override public ISonglet GetContent() {
      return Content;
    }
    /* ********************************************************************************* */
    @Override public ISonglet.Singer Spawn_Singer() {// always always always override this
      return this.Spawn_My_Singer();
    }
    /* ********************************************************************************* */
    public ChorusBox_Singer Spawn_My_Singer() {// for render time
      ChorusBox_Singer ph = this.Content.Spawn_My_Singer();
      ph.MyOffsetBox = this;// to do: also transfer all of this box's offsets to player head. 
      return ph;
    }
  }
  /* ********************************************************************************* */
  public OffsetBox Add_SubSong(ISonglet songlet, double TimeOffset, double OctaveOffset, double LoudnessFactor) {
    songlet.Set_Project(this.MyProject);// child inherits project from me
    OffsetBox obox = songlet.Spawn_OffsetBox();
    obox.TimeLoc_s(TimeOffset);
    obox.OctaveLoc_s(OctaveOffset);
    obox.LoudnessFactor_s(LoudnessFactor);
    SubSongs.add(obox);
    return obox;
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
      if (MaxDuration < (DurBuf = (ob.TimeOrg + vb.Update_Durations()))) {
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
      if (MyMaxDuration < (DurBuf = (obx.TimeOrg + metrics.MaxDuration))) {
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
        return Double.compare(voice0.TimeOrg, voice1.TimeOrg);
      }
    });
  }
  /* ********************************************************************************* */
  @Override public OffsetBox Spawn_OffsetBox() {// for compose time
    return this.Spawn_My_OffsetBox();
  }
  /* ********************************************************************************* */
  public Chorus_OffsetBox Spawn_My_OffsetBox() {// for compose time
    Chorus_OffsetBox lbox = new Chorus_OffsetBox();// Deliver an OffsetBox specific to this type of songlet.
    lbox.Clear();
    lbox.Content = this;
    return lbox;
  }
  /* ********************************************************************************* */
  @Override public ISonglet.Singer Spawn_Singer() {
    return this.Spawn_My_Singer();
  }
  /* ********************************************************************************* */
  public ChorusBox_Singer Spawn_My_Singer() {
    ChorusBox_Singer ChorusPlayer = new ChorusBox_Singer();
    ChorusPlayer.MySonglet = this;
    ChorusPlayer.MyProject = this.MyProject;// inherit project
    return ChorusPlayer;
  }
  /* ********************************************************************************* */
  @Override public int Get_Sample_Count(int SampleRate) {
    return SampleRate * (int) this.Get_Duration();
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
  @Override public void Draw_Me(Drawing_Context ParentDC) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  @Override public CajaDelimitadora GetBoundingBox() {
    return this.MyBounds;
  }
  @Override public void UpdateBoundingBox() {
    /* ew. just realized that every bounding box would have to be in absolute drawing coordinates
     but since a songlet can exist in multiple places the bounds will probably have be in local coords and then mapped to parent coordinates
     do this on paper before creating a bunch of interface contracts
     */
    OffsetBox ChildOffsetBox;
    ISonglet song;
    IDrawable DrawableChild = null;
    CajaDelimitadora ChildBBoxUnMapped = new CajaDelimitadora();
    this.MyBounds.Reset();
    int len = this.SubSongs.size();
    for (int pcnt = 0; pcnt < len; pcnt++) {
      ChildOffsetBox = this.SubSongs.get(pcnt);
      song = ChildOffsetBox.GetContent();
      // DrawableChild = song; // are we going to have to make all ISonglets also IDrawable?
      DrawableChild.UpdateBoundingBox();
      DrawableChild.GetBoundingBox().UnMap(ChildOffsetBox, ChildBBoxUnMapped);// project child limits into parent (my) space
      this.MyBounds.Include(ChildBBoxUnMapped);
    }
  }
}
