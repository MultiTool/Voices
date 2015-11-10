/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import voices.Voice.Player_Head;
import voices.VoiceBase.Player_Head_Base;
import voices.VoiceBase.Point;

/**
 *
 * @author MultiTool
 */
public class Voices {

  /**
   * @param args the command line arguments
   */
  /* ********************************************************************************* */
  public static void main(String[] args) {
    Test1();
    if (false) {
      Test0();
    }
  }
  /* ********************************************************************************* */
  public static void Test0() {
    Voice vc = new Voice();
    Point pnt0 = new Point(), pnt1 = new Point();
    Wave wave0 = new Wave(), wave1 = new Wave();
    Wave wave_diff = new Wave();

    int TDiff = 16;
    int nsamps = TDiff * 1000;// 100;
    nsamps = TDiff * Globals.SampleRate;
    pnt0.Octave = 1.0;
    pnt1.Octave = 4.0;
    pnt0.Loudness = 1.0;
    pnt1.Loudness = 1.0;
    pnt0.RealTime = 0.7;
    pnt1.RealTime = pnt0.RealTime + TDiff;
    pnt0.SubTime = 0.0;// pnt1.SubTime=1.0;

    vc.Add_Note(pnt0);
    vc.Add_Note(pnt1);
    vc.Recalc_Line_SubTime();

    wave0.Init(nsamps);
    wave1.Init(nsamps);

    Player_Head_Base hd = vc.Spawn_Player();

    long StartTime, EndTime;

    StartTime = System.currentTimeMillis();
    hd.Render_Segment_Iterative(pnt0, pnt1, wave0);
    EndTime = System.currentTimeMillis();
    System.out.println("Render_Segment_Iterative time:" + (EndTime - StartTime));

    StartTime = System.currentTimeMillis();
    hd.Render_Segment_Integral(pnt0, pnt1, wave1);
    EndTime = System.currentTimeMillis();
    System.out.println("Render_Segment_Integral time:" + (EndTime - StartTime));

    wave0.Diff(wave1, wave_diff);

    SaveWave3(wave_diff, wave0, wave1, "wave_all.csv");
//    SaveWave(wave0, "wave0.csv");
//    SaveWave(wave1, "wave1.csv");
//    SaveWave(wave_diff, "wave_diff.csv");
    boolean nop = true;
  }
  /* ********************************************************************************* */
  public static void Test1() {
    Globals.SampleRate = 100;
    Globals.BaseFreqC0 = 1.0;
    Voice vc = new Voice();
    Wave wave_render = new Wave();
    //Wave wave_diff = new Wave();

    int TDiff = 16;// seconds
    int nsamps;
    //nsamps = TDiff * Globals.SampleRate;

    {
//      vc.Add_Note(0, 4, 1);
//      vc.Add_Note(8, 1, 0.5);
//      vc.Add_Note(TDiff, 4, 1);
    }
    {
      vc.Add_Note(1, 4, 1);
      vc.Add_Note(8, 1, 0.5);
      vc.Add_Note(TDiff, 4, 1);
    }
    vc.Recalc_Line_SubTime();

    nsamps = vc.Get_Sample_Count(Globals.SampleRate);

    wave_render.Init(nsamps);

    Player_Head_Base hd = vc.Spawn_Player();

    long StartTime, EndTime;

    hd.Start();
    StartTime = System.currentTimeMillis();
    ///hd.Skip_To(1.2);
    //hd.Render_To(4, wave_render);
    //hd.Skip_To(4.29);
    hd.Render_To(0.5, wave_render);
    hd.Render_To(TDiff - 4, wave_render);
    hd.Render_To(TDiff - 0, wave_render);
    //hd.Render_Range(0, 2, wave_render);
    EndTime = System.currentTimeMillis();
    System.out.println("Render_To time:" + (EndTime - StartTime));// Render_To time: 150 milliseconds per 16 seconds. 
    //System.out.println("Render_Range time:" + (EndTime - StartTime));

    SaveWave(wave_render, "wave_render.csv");
    boolean nop = true;
  }
  /* ********************************************************************************* */
  public static void SaveWave3(Wave wave0, Wave wave1, Wave wave2, String FileName) {
    try {
      PrintWriter out = new PrintWriter(FileName);
      for (int cnt = 0; cnt < wave0.numsamples; cnt++) {
        out.println(wave0.Get(cnt) + ", " + wave1.Get(cnt) + ", " + wave2.Get(cnt) + "");
      }
      out.close();
    } catch (FileNotFoundException ex) {
    }
  }
  /* ********************************************************************************* */
  public static void SaveWave(Wave wave, String FileName) {
    try {
      PrintWriter out = new PrintWriter(FileName);
      for (int cnt = 0; cnt < wave.numsamples; cnt++) {
        //out.println(cnt + ", " + wave.wave[cnt] + "");
        out.println(wave.Get(cnt) + "");
      }
      out.close();
    } catch (FileNotFoundException ex) {
    }
  }
  /*
   Junkyard
  
   ln(2) = 0.69314718 = 0.69314718055994530941723212145818
    
   next?
   long composition? 
   graphics?
   serialize and save/load? 
   moving frame of reference?
   vibrato effect?
   loop effect? - need this
   audio output? - need this, or at the very least save as raw file
   voice type made of sample loops? - stretch goal

   how vibrato?  Render_To_Bent(EndTime, EndOctave);
   could just add the parent OctaveRate to our own, for the whole child render() span 

   Render_To(){
   double ParentOctaveOffset, ParentOctaveRate;
   this.ParentPlayer.CurrentOctaveOffset;
   this.ParentPlayer.CurrentOctaveRate;
   }
   static double Frequency_Integral_Bent_Octave(double slope, double ybase, double xval) {// http://www.quickmath.com   bent note math
   double frequency_from_octave_integral = Math.pow(2.0, (ybase + slope * xval)) / (slope * Math.log(2.0));// returns the number of cycles since T0, assuming linear change to octave.
   return frequency_from_octave_integral;
   }
  
   ********************************************************************************* 
   double SineGenerator(double time, double frequency, int sampleRate) {// http://stackoverflow.com/questions/8566938/how-to-properly-bend-a-note-in-an-audio-synthesis-application
   return Math.sin(time += (frequency * 2 * Math.PI) / sampleRate);
   }
  
  so is every coordbox also an fxbox? and/or a container? 
  
  coordboxes were created so that a single instance of a voice would not carry its own offset coords everywhere it was reparented, or double-parented. 
  but, a voice could be double-parented to an fxbox that had no coordinates of its own. (eventually you need a parent with coordinates though. 
  you're always 0,0 from the inside of any parent that does not contain your coordboxes. 
  
  so should every voice spawn a coordbox to be attached to something?  doesn't seem like always. 
  should I make my own coordbox if I am a voice?  
  
   */
}
