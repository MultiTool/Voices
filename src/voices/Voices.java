/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import voices.Voice.Player_Head;
import voices.ISonglet.Singer;
//import voices.VoiceBase.Point;

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
    Test2();
  }
  /* ********************************************************************************* */
  public static void Test2() {
    Globals.BaseFreqC0 = 1.0;
    Project prj = new Project();
    prj.Compose_Chorus_Test();
    prj.Render_Test();
  }
  /* ********************************************************************************* */
  public static void Test1() {
    Globals.SampleRate = 100;
    Globals.BaseFreqC0 = 1.0;
    Voice vc = new Voice();
    Wave wave_render = new Wave();
    Wave wave_scratch = new Wave();
    Project proj = new Project();
    proj.SampleRate = 100;
    vc.Set_Project(proj);

    int TDiff = 16;// seconds
    
    {
      vc.Add_Note(1, 4, 1);
      vc.Add_Note(8, 1, 0.5);
      vc.Add_Note(TDiff, 4, 1);
    }
    vc.Recalc_Line_SubTime();

    wave_render.Init(0);

    Singer hd = vc.Spawn_Singer();

    long StartTime, EndTime;

    hd.Start();
    StartTime = System.currentTimeMillis();
    ///hd.Skip_To(1.2);
    //hd.Render_To(4, wave_scratch);
    //hd.Skip_To(4.29);
    hd.Render_To(3.0, wave_scratch);
    wave_render.Append(wave_scratch);
    hd.Render_To(TDiff - 4, wave_scratch);
    wave_render.Append(wave_scratch);
    hd.Render_To(TDiff - 0, wave_scratch);
    wave_render.Append(wave_scratch);
    //hd.Render_Range(0, 2, wave_scratch);
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
  
   so is every OffsetBox also an fxbox? and/or a container? 
  
   OffsetBoxes were created so that a single instance of a voice would not carry its own offset coords everywhere it was reparented, or double-parented. 
   but, a voice could be double-parented to an fxbox that had no coordinates of its own. (eventually you need a parent with coordinates though. 
   you're always 0,0 from the inside of any parent that does not contain your OffsetBoxes. 
  
   so should every voice spawn a OffsetBox to be attached to something?  doesn't seem like always. 
   should I make my own OffsetBox if I am a voice?  
  
   */
}
