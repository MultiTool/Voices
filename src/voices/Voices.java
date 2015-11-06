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
    //Globals.SampleRate = 100;
    Voice vc = new Voice();
    Wave wave_render = new Wave();
    //Wave wave_diff = new Wave();

    int TDiff = 16;
    int nsamps;
    nsamps = TDiff * Globals.SampleRate;

    vc.Add_Note(0, 4, 1);
    vc.Add_Note(8, 1, 0.5);
    vc.Add_Note(TDiff, 4, 1);
    vc.Recalc_Line_SubTime();

    wave_render.Init(nsamps);

    Player_Head_Base hd = vc.Spawn_Player();

    long StartTime, EndTime;

    hd.Start();
    StartTime = System.currentTimeMillis();
    //hd.Render_To(TDiff, wave_render);
    hd.Render_Range(0, 2, wave_render);
    EndTime = System.currentTimeMillis();
    System.out.println("Render_Range time:" + (EndTime - StartTime));

    SaveWave(wave_render, "wave_render.csv");
    boolean nop = true;
  }
  /* ********************************************************************************* */
  public static void SaveWave3(Wave wave0, Wave wave1, Wave wave2, String FileName) {
    try {
      PrintWriter out = new PrintWriter(FileName);
      for (int cnt = 0; cnt < wave0.numsamples; cnt++) {
        //out.println(cnt + ", " + wave.wave[cnt] + "");
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
  public static void Circus(double Time) {
    double Cycles = 0.0;
    //double StartPhase;
    Point pnt0 = new Point();
    pnt0.RealTime = 0;
    pnt0.Octave = 2;
    pnt0.Loudness = 1;
    Point pnt1 = new Point();
    pnt1.RealTime = 1;// 1 second later? 
    pnt1.Octave = 3;
    pnt1.Loudness = 1;

    // C0 16.35 
    // A0 27.50 
    double TimeRange = pnt1.RealTime - pnt0.RealTime;
    double TimeAlong = Time = pnt0.RealTime;
    double FractAlong = TimeAlong / TimeRange;

    double PitchRange = pnt1.Octave - pnt0.Octave;
    double CurrentPitch = pnt0.Octave + (PitchRange * FractAlong);
    double LoudnessRange = pnt1.Loudness - pnt0.Loudness;
    double CurrentLoudness = pnt0.Loudness + (LoudnessRange * FractAlong);

    double freq = Globals.BaseFreqC0 * Math.pow(2.0, pnt0.Octave);
    double Amplitude;

    double integrate = Math.pow(2.0, pnt0.Octave) / Math.log(2);// integral without range
    double Integral_Ranged = (Math.pow(2.0, pnt0.Octave) - 1.0) / Math.log(2);// integral with range from 0 to pnt0

    // do all from end to end? or start in the middle and hope the phase was done correctly?
    // end to end for starters
    // we need samples per second, what are our time units, 
    double SamplesPerSecond = 44100.0;
    double SecondsPerSample = 1.0 / SamplesPerSecond;
    // Freq is in hertz, must convert cycles to angles for sine 

    double Radians = Cycles * Math.PI * 2.0;
    // the most important thing in the world is to bend notes, and then bend them some more to other notes.
    double RealTime, SubTime;
    double OctavePrev = 1, OctaveNext = 2, OctaveRange, OctaveNow;
    OctaveRange = OctaveNext - OctavePrev;
    RealTime = 0.0;
    int NumSamples = 100;
    double Freq = 60.0; // hz
    for (int tcnt = 0; tcnt < NumSamples; tcnt++) {
      FractAlong = ((double) tcnt) / (double) NumSamples;
      RealTime = FractAlong;// so 0 to 1 seconds
      OctaveNow = OctavePrev + (OctaveRange * FractAlong);
      SubTime = (Math.pow(2.0, OctaveNow) - 1.0) / Math.log(2);// integral with range from 0 to pnt0
      Cycles = SubTime * Freq;
      Radians = Cycles * Math.PI * 2.0;
      Amplitude = Math.sin(Radians);
    }

    /*
     so we don't really need the integral of frequency to get current cycle and phase. 
     we can just count them over the number of samples (slower but works for general case?)
     are we really counting number of cycles, or more like subjective time, as time compresses?
    
     so integral with range will be (2^endval)-1)/ln(2);
     ln(2) = 0.69314718 = 0.69314718055994530941723212145818
    
     http://rtcmix.org/
    
     http://quickmath.com/webMathematica3/quickmath/calculus/integrate/advanced.jsp#c=integrate_advancedintegrate&v1=t*%282^%28t*z%29%29&v2=t&v3=0&v4=7

     t*(2^(t*z))  steady octave change. t is real time, z is octave change rate assuming t starts at 0.
     (log(2)*z-1)*%e^(log(2)*z)/(log(2)^2*z^2)+1/(log(2)^2*z^2)  range 0 to 1
     (2*log(2)*z-1)*%e^(2*log(2)*z)/(log(2)^2*z^2)+1/(log(2)^2*z^2)  0 to 2
     (3*log(2)*z-1)*%e^(3*log(2)*z)/(log(2)^2*z^2)+1/(log(2)^2*z^2)  to 3
     (4*log(2)*z-1)*%e^(4*log(2)*z)/(log(2)^2*z^2)+1/(log(2)^2*z^2)  to 4
     (5*log(2)*z-1)*%e^(5*log(2)*z)/(log(2)^2*z^2)+1/(log(2)^2*z^2)  to 5

     (7*log(2)*z-1)*%e^(7*log(2)*z)/(log(2)^2*z^2)+1/(log(2)^2*z^2)  to 7

     %e seems to be e=2.718281828459045

     t*((t*z))  steady frequency change.
     */
  }
}
