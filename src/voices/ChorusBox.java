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
public class ChorusBox implements ISonglet {
  public ArrayList<CoordBox> SubSongs = new ArrayList<>();
  public double Duration = 0.0;
  private Project MyProject;
  /* ********************************************************************************* */
  public static class Player_Head_ChorusBox extends Singer {
    protected ChorusBox MyPhrase;
    public ArrayList<Singer> NowPlaying = new ArrayList<>();// pool of currently playing voices
    public int Current_Dex = 0;
    double Prev_Time = 0;
    private Chorus_CoordBox MyCoordBox;
    /* ********************************************************************************* */
    @Override public void Start() {
      IsFinished = false;
      Current_Dex = 0;
      Prev_Time = 0;
      NowPlaying.clear();
    }
    /* ********************************************************************************* */
    @Override public void Skip_To(double EndTime) {
      EndTime = this.MyCoordBox.MapTime(EndTime);// EndTime is now time internal to ChorusBox's own coordinate system
      if (this.MyPhrase.SubSongs.size() <= 0) {
        this.IsFinished = true;
        return;
      }
      //EndTime -= this.TimeLoc_g();//.RealTimeAbsolute;// ???   // first, have to convert EndTime to local time offset. 
      // what kind of coordinates does the caller pass?  the caller is the parent playerhead. the parent playerhead should be passing its own internal coords. 
      // that means it's up to me (player) to remove my offset coords from EndTime. 
      if (EndTime < 0) {
        EndTime = 0;// clip time
      }
      double Final_Time = this.MyPhrase.Get_Duration();
      if (EndTime > Final_Time) {
        this.IsFinished = true;
        EndTime = Final_Time;// clip time
      }
      CoordBox cb = MyPhrase.SubSongs.get(Current_Dex);// repeat until cb start time > EndTime
      while (cb.TimeOrg < EndTime) {// first find new voices in this time range and add them to pool
        ISonglet vb = cb.GetContent();// SHOULD EndTime BE INCLUSIVE???
        Singer player = vb.Spawn_Player();
        this.NowPlaying.add(player);
        player.Start();
        Current_Dex++;
        cb = MyPhrase.SubSongs.get(Current_Dex);
      }
      int NumPlaying = NowPlaying.size();
      int cnt = 0;
      while (cnt < NumPlaying) {// then play the whole pool
        Singer player = this.NowPlaying.get(cnt);
        player.Skip_To(EndTime);
        cnt++;
      }
      cnt = 0;// now pack down the finished ones
      while (cnt < NumPlaying) {
        Singer player = this.NowPlaying.get(cnt);
        if (player.IsFinished) {
          this.NowPlaying.remove(player);
        } else {
          cnt++;
        }
      }
    }
    /* ********************************************************************************* */
    @Override public void Render_To(double EndTime, Wave wave) {
      EndTime = this.MyCoordBox.MapTime(EndTime);// EndTime is now time internal to ChorusBox's own coordinate system
      double UnMapped_Prev_Time = this.MyCoordBox.UnMapTime(this.Prev_Time);// get start time in parent coordinates
      if (this.MyPhrase.SubSongs.size() <= 0) {
        this.IsFinished = true;
        wave.Init(UnMapped_Prev_Time, UnMapped_Prev_Time, this.MyProject.SampleRate);// wave times are in parent coordinates because the parent will be reading the wave data.
        this.Prev_Time = EndTime;
        return;
      }
      if (EndTime < 0) {
        EndTime = 0;// clip time
      }
      double Final_Time = this.MyPhrase.Get_Duration();
      if (EndTime > Final_Time) {
        this.IsFinished = true;
        EndTime = Final_Time;// clip time
      }
      wave.Init(UnMapped_Prev_Time, this.MyCoordBox.UnMapTime(EndTime), this.MyProject.SampleRate);// wave times are in parent coordinates because the parent will be reading the wave data.
      Wave ChildWave = new Wave();
      CoordBox cb = MyPhrase.SubSongs.get(this.Current_Dex);// repeat until cb start time > EndTime
      while (cb.TimeOrg < EndTime) {// first find new voices in this time range and add them to pool
        ISonglet vb = cb.GetContent();// SHOULD EndTime BE INCLUSIVE???
        Singer player = vb.Spawn_Player();
        this.NowPlaying.add(player);
        player.Start();
        this.Current_Dex++;
        cb = MyPhrase.SubSongs.get(this.Current_Dex);
      }
      int NumPlaying = NowPlaying.size();
      int cnt = 0;
      while (cnt < NumPlaying) {// then play the whole pool
        Singer player = this.NowPlaying.get(cnt);
        player.Render_To(EndTime, ChildWave);
        wave.SumIn(ChildWave);// sum/overdub the waves 
        cnt++;
      }
      cnt = 0;// now pack down the finished ones
      while (cnt < NumPlaying) {
        Singer player = this.NowPlaying.get(cnt);
        if (player.IsFinished) {
          this.NowPlaying.remove(player);
        } else {
          cnt++;
        }
      }
      this.Prev_Time = EndTime;
    }
  }
  /* ********************************************************************************* */
  public static class Chorus_CoordBox extends CoordBox {// location box to transpose in pitch, move in time, etc. 
    public ChorusBox Content;
    /* ********************************************************************************* */
    @Override public ISonglet GetContent() {
      return Content;
    }
    /* ********************************************************************************* */
    public Player_Head_ChorusBox Spawn_My_Player() {// for render time
      Player_Head_ChorusBox ph = this.Content.Spawn_My_Player();
      ph.MyCoordBox = this;// to do: also transfer all of this box's offsets to player head. 
      return ph;
    }
  }
  /* ********************************************************************************* */
  public void Add_SubSong(ISonglet songlet) {
    songlet.Set_Project(this.MyProject);// inherit project
    CoordBox cb = songlet.Spawn_CoordBox();
    SubSongs.add(cb);
  }
  /* ********************************************************************************* */
  @Override public double Get_Duration() {
    return this.Duration;
  }
  /* ********************************************************************************* */
  @Override public double Update_Durations() {
    double MaxDuration = 0.0;
    double DurBuf = 0.0;
    int NumSubSongs = this.SubSongs.size();
    for (int cnt = 0; cnt < NumSubSongs; cnt++) {
      ISonglet vb = this.SubSongs.get(cnt).GetContent();
      if (MaxDuration < (DurBuf = vb.Update_Durations())) {
        MaxDuration = DurBuf;
      }
    }
    this.Duration = MaxDuration;
    return MaxDuration;
  }
  /* ********************************************************************************* */
  @Override public void Sort_Me() {// sorting by RealTime
    Collections.sort(this.SubSongs, new Comparator<CoordBox>() {
      @Override public int compare(CoordBox voice0, CoordBox voice1) {
        return Double.compare(voice0.TimeOrg, voice1.TimeOrg);
      }
    });
  }
  /* ********************************************************************************* */
  @Override public CoordBox Spawn_CoordBox() {// for compose time
    return this.Spawn_My_CoordBox();
  }
  /* ********************************************************************************* */
  public Chorus_CoordBox Spawn_My_CoordBox() {// for compose time
    Chorus_CoordBox lbox = new Chorus_CoordBox();// Deliver a CoordBox specific to this type of phrase.
    lbox.Content = this;
    return lbox;
  }
  /* ********************************************************************************* */
  @Override public ISonglet.Singer Spawn_Player() {
    return this.Spawn_My_Player();
  }
  /* ********************************************************************************* */
  public Player_Head_ChorusBox Spawn_My_Player() {
    Player_Head_ChorusBox ChorusPlayer = new Player_Head_ChorusBox();
    ChorusPlayer.MyPhrase = this;
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
}
