/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import java.util.ArrayList;
import java.util.Collections;
import static voices.VoiceBase.BaseFreqC0;

/**
 *
 * @author MultiTool
 */
public class Voice extends VoiceBase {
  // collection of control points, each one having a pitch and a volume. rendering morphs from one cp to another. 
  //public ArrayList<Point> CPoints = new ArrayList<>();
  /*
   is the first point the defining frequency of this voice? 
   with no relative containers, all points are absolute and none need a parent.
   however in the future we may want to transpose. we will keep a separate parent coordinate for a voice and the points can be relative to that. 
   */
  public static class Player_Head extends Player_Head_Base {
    protected Voice Parent;
    double Phase, Cycles;// Cycles is the number of cycles we've rotated since the start of this voice. The fractional part is the phase information. 
    double Current_Time;
    double SubTime;// Subjective time.
    double Current_Octave, Current_Frequency;
    public double SampleRate;
    Point Prev_Point, Next_Point;
    Point Cursor_Point = new Point();
    /* ********************************************************************************* */
    @Override
    public void Start() {
      this.SubTime = 0.0;
      this.Current_Time = 0.0;
      this.Phase = 0.0;
      this.Cycles = 0.0;
      this.Prev_Point = this.Parent.CPoints.get(0);
      this.Cursor_Point.CopyFrom(Prev_Point);
    }
    /* ********************************************************************************* */
    @Override
    public void Skip_To(double Time) {// ready for test
      int len = this.Parent.CPoints.size();
      int tcnt = 0;
      Next_Point = null;
      do {
        Prev_Point = Next_Point;
        if (tcnt >= len) {
          break;
        }
        Next_Point = this.Parent.CPoints.get(tcnt);
        tcnt++;
      } while (Next_Point.RealTime < Time);

      if (Time <= Next_Point.RealTime) {
        if (Prev_Point != null) {
          if (Prev_Point.RealTime <= Time) {// only sandwiched here. 
            Cursor_Point.CopyFrom(Prev_Point);
            Interpolate_ControlPoint(Prev_Point, Next_Point, Time, Cursor_Point);
          }
        }
      }
    }
    /* ********************************************************************************* */
    public static void Interpolate_ControlPoint(Point pnt0, Point pnt1, double RealTime, Point PntMid) {// ready for test
      double FrequencyFactorPrev = pnt0.GetFrequencyFactor();
      double TimeRange = pnt1.RealTime - pnt0.RealTime;
      double TimeAlong = RealTime - pnt0.RealTime;
      double OctaveRange = pnt1.Octave - pnt0.Octave;
      double OctaveRate = OctaveRange / TimeRange;// octaves per second
      double SubTimeLocal = Calculus(OctaveRate, TimeAlong);
      PntMid.RealTime = RealTime;
      PntMid.SubTime = pnt0.SubTime + (FrequencyFactorPrev * SubTimeLocal);

      // not calculus here
      PntMid.Octave = pnt0.Octave + (TimeAlong * OctaveRate);
      double LoudRange = pnt1.Loudness - pnt0.Loudness;
      double LoudAlong = TimeAlong * LoudRange / TimeRange;
      PntMid.Loudness = pnt0.Loudness + LoudAlong;
    }
    /* ********************************************************************************* */
    @Override
    public void Render_To(double Time, Wave wave) {
      Math.sin(Time);
      /*
       set prev point to wherever cursor is.
       (in start, cursor is set to point 0 by default)
       iterate to the point where next_point.realtime <= time.  do while? 
       Next_Point = this.Cursor_Point;
       loop {
       Prev_Point = Next_Point;
       Next_Point = this.Parent.CPoints.get(cnt);
       Render_Line(Prev_Point, Next_Point, wave);
       }
	  
       Interpolate_ControlPoint(Prev_Point, Next_Point, Time, Cursor_Point);//  render loose end. 
       Render_Line(Prev_Point, Cursor_Point, wave);
	  
       */
    }
    /* ********************************************************************************* */
    public void Skip_Line() {

    }
    /* ********************************************************************************* */
    static double Frequency_Integral_Bent_Octave(double slope, double ybase, double xval) {// http://www.quickmath.com   bent note math
      double frequency_from_octave_integral = Math.pow(2.0, (ybase + slope * xval)) / (slope * Math.log(2.0));// returns the number of cycles since T0, assuming linear change to octave.
      return frequency_from_octave_integral;
    }
    /* ********************************************************************************* */
    public void Render_Line(Point pnt0, Point pnt1, Wave wave0, Wave wave1) {
      double TimeRange = pnt1.RealTime - pnt0.RealTime;
      double SampleDuration = 1.0 / this.SampleRate;
      double RealTime = pnt0.RealTime;
      double FrequencyFactorPrev = pnt0.GetFrequencyFactor();
      double OctaveRange = pnt1.Octave - pnt0.Octave;
      if (OctaveRange == 0.0) {
        OctaveRange = 0.00000000001;// Fudge to avoid div by 0 
      }
      double LoudnessRange = pnt1.Loudness - pnt0.Loudness;
      double OctaveRate = OctaveRange / TimeRange;// octaves per second
      double LoudnessRate = LoudnessRange / TimeRange;
      double SubTimeLocal, Denom;
      double SubTimeAbsolute;
      int NumSamples = (int) (TimeRange * this.SampleRate);

      double FractAlong;
      double TimeAlong;
      double CurrentOctaveAbsolute, CurrentOctaveLocal, CurrentLoudness, CurrentFrequency, CurrentFrequencyFactorAbsolute, CurrentFrequencyFactorLocal;
      double Amplitude;

      double SubTimeIterate = 0.0, SubTimeCalc = 0.0;
      SubTimeAbsolute = 0.0;

      for (int scnt = 0; scnt < NumSamples; scnt++) {
        TimeAlong = scnt * SampleDuration;// good
        RealTime = pnt0.RealTime + TimeAlong;
        FractAlong = TimeAlong / TimeRange;
        CurrentOctaveLocal = TimeAlong * OctaveRate;
        CurrentFrequencyFactorLocal = Math.pow(2.0, CurrentOctaveLocal); // to convert to absolute, do pnt0.SubTime + (FrequencyFactorPrev * CurrentFrequencyFactorLocal);
        CurrentFrequencyFactorAbsolute = pnt0.SubTime + (FrequencyFactorPrev * CurrentFrequencyFactorLocal);

        if (false) {
          CurrentOctaveAbsolute = pnt0.Octave + CurrentOctaveLocal;// good to here
          CurrentFrequencyFactorAbsolute = Math.pow(2.0, CurrentOctaveAbsolute);
        }
        // SubTimeLocal = Math.pow(2.0, CurrentOctaveLocal);
        SubTimeIterate += CurrentFrequencyFactorAbsolute / this.SampleRate;

        CurrentFrequency = BaseFreqC0 * SubTimeIterate;// do we really need to include the base frequency in the summing?

        SubTimeIterate += (CurrentFrequency * TwoPi) / this.SampleRate;
        Amplitude = Math.sin(SubTimeAbsolute * BaseFreqC0 * TwoPi);
        wave0.wave[scnt] = Amplitude;

        {
          SubTimeLocal = Calculus(OctaveRate, TimeAlong);
          SubTimeAbsolute = pnt0.SubTime + (FrequencyFactorPrev * SubTimeLocal);
          Amplitude = Math.sin(SubTimeAbsolute * BaseFreqC0 * TwoPi);
          wave1.wave[scnt] = Amplitude;
        }

        CurrentLoudness = pnt0.Loudness + (TimeAlong * LoudnessRate);
      }
    }
    /* ********************************************************************************* */
    public void Interp_Points(Point pnt0, Point pnt1, double StartPhase, double Time) {
      double freq = BaseFreqC0 * Math.pow(2.0, pnt0.Octave);

      // C0 16.35 
      // A0 27.50 
      double TimeRange = pnt1.RealTime - pnt0.RealTime;
      double TimeAlong = Time - pnt0.RealTime;
      double FractAlong = TimeAlong / TimeRange;

      double PitchRange = pnt1.Octave - pnt0.Octave;
      double CurrentPitch = pnt0.Octave + (PitchRange * FractAlong);
      double LoudnessRange = pnt1.Loudness - pnt0.Loudness;
      double CurrentLoudness = pnt0.Loudness + (LoudnessRange * FractAlong);

      // do all from end to end? or start in the middle and hope the phase was done correctly?
      // end to end for starters
      // we need samples per second, what are our time units, 
      double SamplesPerSecond = 44100.0;
      double SecondsPerSample = 1.0 / SamplesPerSecond;
      // Freq is in hertz, must convert cycles to angles for sine 

      double Radians = Cycles * Math.PI * 2.0;
      // the most important thing in the world is to bend notes, and then bend them some more to other notes.

      double Integral_Ranged = (Math.pow(2.0, pnt0.Octave) - 1.0) / Math.log(2);// integral with range from 0 to pnt0
      this.SubTime = Integral_Ranged;
      this.Cycles = BaseFreqC0 * this.SubTime;
    }
    /*
     so how do we render from one point to the next? 
     first we 
     */
  }
  double SineGenerator(double time, double frequency, int sampleRate) {// http://stackoverflow.com/questions/8566938/how-to-properly-bend-a-note-in-an-audio-synthesis-application
    return Math.sin(time += (frequency * 2 * Math.PI) / sampleRate);
  }

  public Voice() {
  }
  @Override
  public void Add_Note(Point pnt) {
  }
  @Override
  public Player_Head_Base Spawn_Player() {
    Player_Head ph = new Player_Head();
    ph.Parent = this;
    return ph;
  }
  /* ********************************************************************************* */
  public void Recalc_Line_SubTime() {// ready for test
    double SubTimeLocal;
    int len = this.CPoints.size();
    if (len <= 0) {
      return;
    }
    Point Prev_Point, Next_Point, Dummy_First;
    Next_Point = this.CPoints.get(0);
    Dummy_First = new Point();
    Dummy_First.CopyFrom(Next_Point);
    Dummy_First.RealTime = 0.0;// Times must both start at 0, even though user may have put the first audible point at T greater than 0. 
    Dummy_First.SubTime = 0.0;
    Next_Point = Dummy_First;
    for (int pcnt = 0; pcnt < len; pcnt++) {
      Prev_Point = Next_Point;
      Next_Point = this.CPoints.get(pcnt);
      double FrequencyFactorPrev = Prev_Point.GetFrequencyFactor();
      double TimeRange = Next_Point.RealTime - Prev_Point.RealTime;
      double OctaveRange = Next_Point.Octave - Prev_Point.Octave;
      double OctaveRate = OctaveRange / TimeRange;// octaves per second
      SubTimeLocal = Calculus(OctaveRate, TimeRange);
      Next_Point.SubTime = Prev_Point.SubTime + (FrequencyFactorPrev * SubTimeLocal);
    }
  }
  /* ********************************************************************************* */
  public static double Calculus(double OctaveRate, double TimeAlong) {// ready for test
    double SubTimeCalc;// given realtime passed and rate of octave change, use integration to get the sum of all subjective time passed.  
    double Denom = (Math.log(2) * OctaveRate);// returns the integral of (2 ^ (TimeAlong * OctaveRate))
    //SubTimeCalc = (Math.pow(2, (TimeAlong * OctaveRate)) / Denom) - (1.0 / Denom);
    SubTimeCalc = ((Math.pow(2, (TimeAlong * OctaveRate)) - 1.0) / Denom);
    return SubTimeCalc;
  }
}
