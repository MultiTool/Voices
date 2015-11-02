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
    @Override
    public void Start() {
      this.SubTime = 0.0;
      this.Current_Time = 0.0;
      this.Phase = 0.0;
      this.Cycles = 0.0;
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
      double SubTimeLocal = Calculus(pnt0.Octave, pnt0.RealTime, pnt1.Octave, pnt1.RealTime, OctaveRate, RealTime);
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
    public void Render_Line(Point pnt0, Point pnt1) {
      double TimeRange = pnt1.RealTime - pnt0.RealTime;
      double SampleDuration = 1.0 / this.SampleRate;
      double Time = pnt0.RealTime;
      double TimeAlong = Time - pnt0.RealTime;
      double FractAlong = TimeAlong / TimeRange;
      double FrequencyFactor0 = pnt0.GetFrequencyFactor();
      double OctaveRange = pnt1.Octave - pnt0.Octave;
      double CurrentPitch = pnt0.Octave + (OctaveRange * FractAlong);
      double LoudnessRange = pnt1.Loudness - pnt0.Loudness;
      double CurrentLoudness = pnt0.Loudness + (LoudnessRange * FractAlong);

      double OctaveRate = OctaveRange / TimeRange;// octaves per second
      double SubTimeLocal, Denom;
      double CurrentSubTime;
      int NumSamples = (int) (TimeRange * SampleRate);
      for (int scnt = 0; scnt < NumSamples; scnt++) {
        TimeAlong = scnt * SampleDuration;
        FractAlong = TimeAlong / TimeRange;
        Current_Octave = pnt0.Octave + (OctaveRange * FractAlong);
        Time = pnt0.RealTime + TimeAlong;
        Current_Frequency = BaseFreqC0 * Math.pow(2.0, Current_Octave);
        this.SubTime += (Current_Frequency * TwoPi) / this.SampleRate;

        // Whoops!  looks like we need the integral of (2^(t*z)) ???
        double gral = Math.pow(2, (TimeAlong * OctaveRate));// integral of this formula
        double snoo = Math.pow(2, (TimeAlong * OctaveRate)) / (Math.log(2) * OctaveRate) - 1 / (Math.log(2) * OctaveRate);
        Denom = (Math.log(2) * OctaveRate);
        SubTimeLocal = (Math.pow(2, (TimeAlong * OctaveRate)) / Denom) - (1.0 / Denom);
        // 2^z/(log(2)*z)-1/(log(2)*z)
        // 2^(2*z)/(log(2)*z)-1/(log(2)*z)
        // 2^(3*z)/(log(2)*z)-1/(log(2)*z)
        // 2^(4*z)/(log(2)*z)-1/(log(2)*z)
        // 2^(5*z)/(log(2)*z)-1/(log(2)*z)
        // 2^(6*z)/(log(2)*z)-1/(log(2)*z)
        // 2^(7*z)/(log(2)*z)-1/(log(2)*z)

        // sin( (pi*2)*(2^(x*0.0001)/(ln(2)*0.0001) - 1/(ln(2)*0.0001)) )  bend at near 0 octave rate
        // sin( (pi*2)*(2^(x*0.5)/(ln(2)*0.5) - 1/(ln(2)*0.5)) )  bend at 0.5 octave rate
        // sin( (pi*2)*(2^(x*1.0)/(ln(2)*1.0) - 1/(ln(2)*1.0)) )  bend at 1.0 octave rate (1 octave per second)
        // http://www.fooplot.com/#W3sidHlwZSI6MCwiZXEiOiIoMl4oeCowLjUpKSIsImNvbG9yIjoiIzAwMDAwMCJ9LHsidHlwZSI6MCwiZXEiOiIyXih4KjAuNSkvKGxuKDIpKjAuNSktMS8obG4oMikqMC41KSIsImNvbG9yIjoiI0ZGMDAwMCJ9LHsidHlwZSI6MCwiZXEiOiIyXih4KjAuMDAwMSkvKGxuKDIpKjAuMDAwMSktMS8obG4oMikqMC4wMDAxKSIsImNvbG9yIjoiIzExRkYwMCJ9LHsidHlwZSI6MCwiZXEiOiJzaW4oKHBpKjIpKigyXih4KjEuMCkvKGxuKDIpKjEuMCktMS8obG4oMikqMS4wKSkpIiwiY29sb3IiOiIjMDAwMDAwIn0seyJ0eXBlIjoxMDAwLCJ3aW5kb3ciOlsiLTQuNTY5MTM0Njc1ODAwMjk3IiwiMi40MTAxODcxODAxOTk2NTEzIiwiLTIuMTY2NTM3MzY5NzU5ODIwNSIsIjIuMTI4NDI5OTI2MjQwMTUyMyJdfV0-
        // 2^(x*1)/(ln(2)*1) - 1/(ln(2)*1)
        // SubTimeCalc = 2^(x*0.0001)/(ln(2)*0.0001) - 1/(ln(2)*0.0001) where 0.0001 is octave rate and x is realtime. 
        // so after this, SubTimeCalc will be the ratio of total subjective time elapsed to total real time elapsed since pnt0.Time (TimeAlong)
        // to get actual SubTime, we will have to add? it to pnt0.SubTime. 
        // no no no we multiply SubTimeCalc times pnt0.FrequencyFactor (2^pnt0.Octave) and then add it to pnt0.SubTime.  
        // SubTimeCalc IS a frequency factor. 
        //CurrentSubTime = pnt0.SubTime + (FrequencyFactor0 * SubTimeCalc);// absolute subjective time.  maybe this ????????? 
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
      SubTimeLocal = Calculus(Prev_Point.Octave, Prev_Point.RealTime, Next_Point.Octave, Next_Point.RealTime, OctaveRate, Next_Point.RealTime);
      Next_Point.SubTime = Prev_Point.SubTime + (FrequencyFactorPrev * SubTimeLocal);
    }
  }
  /* ********************************************************************************* */
  public static double Calculus(double Octave0, double RealTime0, double Octave1, double RealTime1, double OctaveRate, double TimeAlong) {// ready for test
    double SubTimeCalc;// given realtime passed and rate of octave change, use integration to get the sum of all subjective time passed.  
    double Denom = (Math.log(2) * OctaveRate);
    //SubTimeCalc = (Math.pow(2, (TimeAlong * OctaveRate)) / Denom) - (1.0 / Denom);
    SubTimeCalc = ((Math.pow(2, (TimeAlong * OctaveRate)) - 1.0) / Denom);
    return SubTimeCalc;
  }
}
