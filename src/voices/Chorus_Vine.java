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
public class Chorus_Vine extends VoiceBase {
  public ArrayList<CoordBox> SubVoices = new ArrayList<>();
  /* ********************************************************************************* */
  public static class Player_Head_Chorus_Vine extends Player_Head_Base {
    protected Chorus_Vine MyPhrase;
    public ArrayList<Player_Head_Base> NowPlaying = new ArrayList<>();// pool of currently playing voices
    public int Current_Dex = 0;
    /* ********************************************************************************* */
    @Override
    public void Start() {
      IsFinished = false;
      Current_Dex = 0;
    }
    /* ********************************************************************************* */
    @Override
    public void Skip_To(double EndTime) {
      if (this.MyPhrase.SubVoices.size() <= 0) {
        this.IsFinished = true;
        return;
      }
      double Final_Time = this.MyPhrase.Get_Final_Time();
      if (EndTime > Final_Time) {
        EndTime = Final_Time;// clip time
      }
      CoordBox cb = MyPhrase.SubVoices.get(Current_Dex);// repeat until cb start time > EndTime
      while (cb.TimeLoc < EndTime) {// first find new voices in this time range and add them to pool
        VoiceBase vb = cb.GetContent();// SHOULD EndTime BE INCLUSIVE???
        Player_Head_Base player = vb.Spawn_Player();
        this.NowPlaying.add(player);
        player.Start();
        Current_Dex++;
        cb = MyPhrase.SubVoices.get(Current_Dex);
      }
      int NumPlaying = NowPlaying.size();
      int cnt = 0;
      while (cnt < NumPlaying) {// then play the whole pool
        Player_Head_Base player = this.NowPlaying.get(cnt);
        player.Skip_To(EndTime);
        cnt++;
      }
      cnt = 0;// now pack down the finished ones
      while (cnt < NumPlaying) {
        Player_Head_Base player = this.NowPlaying.get(cnt);
        if (player.IsFinished) {
          this.NowPlaying.remove(player);
        } else {
          cnt++;
        }
      }
    }
    /* ********************************************************************************* */
    @Override
    public void Render_To(double EndTime, Wave wave) {
      if (this.MyPhrase.SubVoices.size() <= 0) {
        this.IsFinished = true;
        return;
      }
      double Final_Time = this.MyPhrase.Get_Final_Time();
      if (EndTime > Final_Time) {
        EndTime = Final_Time;// clip time
      }
    }
  }
  /* ********************************************************************************* */
  public void Add_Voice(VoiceBase voice) {
    CoordBox cb = voice.Spawn_CoordBox();
    SubVoices.add(cb);
  }
  /* ********************************************************************************* */
  @Override
  public double Get_Final_Time() {
    int NumVoices = this.SubVoices.size();
    CoordBox Final_Voice = this.SubVoices.get(NumVoices - 1);
    VoiceBase vb = Final_Voice.GetContent();
    return vb.Get_Final_Time();
  }
  /* ********************************************************************************* */
  @Override
  public void Sort_Me() {// sorting by RealTime
    Collections.sort(this.SubVoices, new Comparator<CoordBox>() {
      @Override
      public int compare(CoordBox voice0, CoordBox voice1) {
        return Double.compare(voice0.TimeLoc, voice1.TimeLoc);
      }
    });
  }
}
