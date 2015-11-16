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
  /* ********************************************************************************* */
  public static class Player_Head_ChorusBox extends Singer {
    protected ChorusBox MyPhrase;
    public ArrayList<Singer> NowPlaying = new ArrayList<>();// pool of currently playing voices
    public int Current_Dex = 0;
    private Chorus_CoordBox MyCoordBox;
    /* ********************************************************************************* */
    @Override public void Start() {
      IsFinished = false;
      Current_Dex = 0;
      NowPlaying.clear();
    }
    /* ********************************************************************************* */
    @Override public void Skip_To(double EndTime) {
      this.Current_Time = EndTime = this.MyCoordBox.MapTime(EndTime);// EndTime is now time internal to ChorusBox's own coordinate system
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
      this.Current_Time = EndTime = this.MyCoordBox.MapTime(EndTime);// EndTime is now time internal to ChorusBox's own coordinate system
      if (this.MyPhrase.SubSongs.size() <= 0) {
        this.IsFinished = true;
        return;
      }
      //EndTime -= this.TimeLoc_g();//.RealTimeAbsolute;// ???   // first, have to convert EndTime to local time offset. 
      if (EndTime < 0) {
        EndTime = 0;// clip time
      }
      double Final_Time = this.MyPhrase.Get_Duration();
      if (EndTime > Final_Time) {
        this.IsFinished = true;
        EndTime = Final_Time;// clip time
      }
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
        player.Render_To(EndTime, wave);
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
  public void Add_SubSong(ISonglet voice) {
    CoordBox cb = voice.Spawn_CoordBox();
    SubSongs.add(cb);
  }
  /* ********************************************************************************* */
  @Override public double Get_Duration() {// this is wrong. the last song started may not be the last one playing. we need to scan the whole tree at compose time. 
    int NumSubSongs = this.SubSongs.size();
    CoordBox Final_Voice = this.SubSongs.get(NumSubSongs - 1);
    ISonglet vb = Final_Voice.GetContent();
    return vb.Get_Duration();
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
}
