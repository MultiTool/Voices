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
  public ArrayList<OffsetBox> SubSongs = new ArrayList<>();
  public double Duration = 0.0;
  private Project MyProject;
  /* ********************************************************************************* */
  public static class Player_Head_ChorusBox extends Singer {
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
        return;
      }
      if (EndTime < 0) {
        EndTime = 0;// clip time
      }
      double Final_Time = this.MySonglet.Get_Duration();
      if (EndTime > Final_Time) {
        this.IsFinished = true;
        EndTime = Final_Time;// clip time
      }
      int Last_Song_Dex = MySonglet.SubSongs.size() - 1;
      OffsetBox cb = MySonglet.SubSongs.get(Current_Dex);// repeat until cb start time > EndTime
      while (cb.TimeOrg < EndTime) {// first find new voices in this time range and add them to pool
        Singer singer = cb.Spawn_Singer();// SHOULD EndTime BE INCLUSIVE???
        singer.Inherit(this);
        this.NowPlaying.add(singer);
        singer.Start();
        if (Current_Dex >= Last_Song_Dex) {
          break;
        }
        Current_Dex++;
        cb = MySonglet.SubSongs.get(Current_Dex);
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
      EndTime = this.MyOffsetBox.MapTime(EndTime);// EndTime is now time internal to ChorusBox's own coordinate system
      double UnMapped_Prev_Time = this.MyOffsetBox.UnMapTime(this.Prev_Time);// get start time in parent coordinates
      if (this.MySonglet.SubSongs.size() <= 0) {
        this.IsFinished = true;
        wave.Init(UnMapped_Prev_Time, UnMapped_Prev_Time, this.MyProject.SampleRate);// wave times are in parent coordinates because the parent will be reading the wave data.
        this.Prev_Time = EndTime;
        return;
      }
      if (EndTime < 0) {
        EndTime = 0;// clip time
      }
      double Final_Time = this.MySonglet.Get_Duration();
      if (EndTime > Final_Time) {
        this.IsFinished = true;
        EndTime = Final_Time;// clip time
      }
      wave.Init(UnMapped_Prev_Time, this.MyOffsetBox.UnMapTime(EndTime), this.MyProject.SampleRate);// wave times are in parent coordinates because the parent will be reading the wave data.
      Wave ChildWave = new Wave();
      int Last_Song_Dex = MySonglet.SubSongs.size() - 1;
      OffsetBox cb = MySonglet.SubSongs.get(this.Current_Dex);// repeat until cb start time > EndTime
      while (cb.TimeOrg < EndTime) {// first find new voices in this time range and add them to pool
        Singer singer = cb.Spawn_Singer();// SHOULD EndTime BE INCLUSIVE???
        singer.Inherit(this);
        this.NowPlaying.add(singer);
        singer.Start();
        if (Current_Dex >= Last_Song_Dex) {
          break;
        }
        this.Current_Dex++;
        cb = MySonglet.SubSongs.get(this.Current_Dex);
      }
      int NumPlaying = NowPlaying.size();
      int cnt = 0;
      while (cnt < NumPlaying) {// then play the whole pool
        Singer player = this.NowPlaying.get(cnt);
        player.Render_To(EndTime, ChildWave);
        wave.Overdub(ChildWave);// sum/overdub the waves 
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
    public Player_Head_ChorusBox Spawn_My_Singer() {// for render time
      Player_Head_ChorusBox ph = this.Content.Spawn_My_Singer();
      ph.MyOffsetBox = this;// to do: also transfer all of this box's offsets to player head. 
      return ph;
    }
  }
  /* ********************************************************************************* */
  public void Add_SubSong(ISonglet songlet) {
    songlet.Set_Project(this.MyProject);// inherit project
    OffsetBox cb = songlet.Spawn_OffsetBox();
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
    Chorus_OffsetBox lbox = new Chorus_OffsetBox();// Deliver a OffsetBox specific to this type of phrase.
    lbox.Content = this;
    return lbox;
  }
  /* ********************************************************************************* */
  @Override public ISonglet.Singer Spawn_Singer() {
    return this.Spawn_My_Singer();
  }
  /* ********************************************************************************* */
  public Player_Head_ChorusBox Spawn_My_Singer() {
    Player_Head_ChorusBox ChorusPlayer = new Player_Head_ChorusBox();
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
}
