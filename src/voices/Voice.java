/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import java.util.ArrayList;
import java.util.Collections;

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
    protected Voice MyPhrase;
    double Phase, Cycles;// Cycles is the number of cycles we've rotated since the start of this voice. The fractional part is the phase information. 
    double Current_Time;
    double SubTime;// Subjective time.
    double Current_Octave, Current_Frequency;
    int Prev_Point_Dex, Next_Point_Dex;
    Point Cursor_Point = new Point();
    /* ********************************************************************************* */
    private Player_Head() {
      this.ParentPlayer = null;
      //this.Start();
    }
    /* ********************************************************************************* */
    @Override
    public void Start() {
      this.SubTime = 0.0;
      this.Current_Time = 0.0;
      this.Phase = 0.0;
      this.Cycles = 0.0;
      this.Prev_Point_Dex = 0;//this.Parent.CPoints.get(0);
      this.Next_Point_Dex = 1;
      //if (this.Parent != null) {
      Point ppnt = this.MyPhrase.CPoints.get(this.Prev_Point_Dex);
      this.Cursor_Point.CopyFrom(ppnt);
      //}
    }
    /* ********************************************************************************* */
    @Override
    public void Skip_To(double EndTime) {// ready for test
      Point Prev_Point, Next_Point;
      int len = this.MyPhrase.CPoints.size();
      if (len < 2) {
        return;
      }
      if (EndTime < Cursor_Point.RealTime) {
        EndTime = Cursor_Point.RealTime;// clip time
      }
      Point Final_Point = this.MyPhrase.CPoints.get(len - 1);
      if (EndTime > Final_Point.RealTime) {
        EndTime = Final_Point.RealTime;// clip time
      }
      Prev_Point = this.Cursor_Point;
      int pdex = this.Next_Point_Dex;
      Next_Point = this.MyPhrase.CPoints.get(pdex);
      while (Next_Point.RealTime < EndTime) {
        pdex++;
        Prev_Point = Next_Point;
        Next_Point = this.MyPhrase.CPoints.get(pdex);
      }
      this.Next_Point_Dex = pdex;
      this.Prev_Point_Dex = this.Next_Point_Dex - 1;

      // deal with loose end. 
      if (EndTime <= Next_Point.RealTime) {
        if (Prev_Point.RealTime <= EndTime) {// EndTime is inside this box. 
          this.Cursor_Point.CopyFrom(Prev_Point);// this section should always be executed, due to time clipping
          Interpolate_ControlPoint(Prev_Point, Next_Point, EndTime, this.Cursor_Point);
        }
      }
    }
    /* ********************************************************************************* */
    @Override
    public void Render_To(double EndTime, Wave wave) {// ready for test
      Point Prev_Point, Next_Point;
      int len = this.MyPhrase.CPoints.size();
      if (len < 2) {
        return;
      }
      if (EndTime < Cursor_Point.RealTime) {
        EndTime = Cursor_Point.RealTime;// clip time
      }
      Point Final_Point = this.MyPhrase.CPoints.get(len - 1);
      if (EndTime > Final_Point.RealTime) {
        EndTime = Final_Point.RealTime;// clip time
      }
      Prev_Point = this.Cursor_Point;
      int pdex = this.Next_Point_Dex;
      Next_Point = this.MyPhrase.CPoints.get(pdex);
      while (Next_Point.RealTime < EndTime) {
        Render_Segment_Integral(Prev_Point, Next_Point, wave);
        pdex++;
        // if (pdex >= len) { break; } // this line may not be necessary
        Prev_Point = Next_Point;
        Next_Point = this.MyPhrase.CPoints.get(pdex);
      }
      this.Next_Point_Dex = pdex;
      this.Prev_Point_Dex = this.Next_Point_Dex - 1;

      // render loose end. 
      if (EndTime <= Next_Point.RealTime) {
        if (Prev_Point.RealTime <= EndTime) {// EndTime is inside this box. 
          Point End_Cursor = new Point();// this section should always be executed, due to time clipping
          End_Cursor.CopyFrom(Prev_Point);
          Interpolate_ControlPoint(Prev_Point, Next_Point, EndTime, End_Cursor);
          Render_Segment_Integral(Prev_Point, End_Cursor, wave);
          this.Cursor_Point.CopyFrom(End_Cursor);
        }
      }
    }
    /* ********************************************************************************* */
    @Override
    public void Render_Range(int dex0, int dex1, Wave wave) {
      Point pnt0, pnt1;
      for (int pcnt = dex0; pcnt < dex1; pcnt++) {
        pnt0 = this.MyPhrase.CPoints.get(pcnt);
        pnt1 = this.MyPhrase.CPoints.get(pcnt + 1);
        Render_Segment_Integral(pnt0, pnt1, wave);
      }
    }
    /* ********************************************************************************* */
    public static void Interpolate_ControlPoint(Point pnt0, Point pnt1, double RealTime, Point PntMid) {// ready for test
      double FrequencyFactorStart = pnt0.GetFrequencyFactor();
      double TimeRange = pnt1.RealTime - pnt0.RealTime;
      double TimeAlong = RealTime - pnt0.RealTime;
      double OctaveRange = pnt1.Octave - pnt0.Octave;
      double OctaveRate = OctaveRange / TimeRange;// octaves per second
      double SubTimeLocal = Calculus(OctaveRate, TimeAlong);
      PntMid.RealTime = RealTime;
      PntMid.SubTime = pnt0.SubTime + (FrequencyFactorStart * SubTimeLocal);

      // not calculus here
      PntMid.Octave = pnt0.Octave + (TimeAlong * OctaveRate);
      double LoudRange = pnt1.Loudness - pnt0.Loudness;
      double LoudAlong = TimeAlong * LoudRange / TimeRange;
      PntMid.Loudness = pnt0.Loudness + LoudAlong;
    }
    /* ********************************************************************************* */
    @Override
    public void Render_Segment_Iterative(Point pnt0, Point pnt1, Wave wave0) {// stateful iterative approach
      double BaseFreq = Globals.BaseFreqC0;
      double SRate = Globals.SampleRate;
      BaseFreq = 1.0;
      //SRate = 100.0;
      //SRate = 1000.0;
      double TimeRange = pnt1.RealTime - pnt0.RealTime;
      double SampleDuration = 1.0 / SRate;
      double FrequencyFactorStart = pnt0.GetFrequencyFactor();
      double OctaveRange = pnt1.Octave - pnt0.Octave;
      if (OctaveRange == 0.0) {
        OctaveRange = Globals.Fudge;// Fudge to avoid div by 0 
      }
      double LoudnessRange = pnt1.Loudness - pnt0.Loudness;
      double OctaveRate = OctaveRange / TimeRange;// octaves per second
      double LoudnessRate = LoudnessRange / TimeRange;
      int NumSamples = (int) (TimeRange * SRate);

      double TimeAlong;
      double CurrentOctaveLocal, CurrentFrequency, CurrentFrequencyFactorAbsolute, CurrentFrequencyFactorLocal;
      double CurrentLoudness;
      double Amplitude;

      double SubTimeIterate = (pnt0.SubTime * BaseFreq * Globals.TwoPi);

      for (int scnt = 0; scnt < NumSamples; scnt++) {
        TimeAlong = scnt * SampleDuration;
        CurrentOctaveLocal = TimeAlong * OctaveRate;
        CurrentFrequencyFactorLocal = Math.pow(2.0, CurrentOctaveLocal); // to convert to absolute, do pnt0.SubTime + (FrequencyFactorStart * CurrentFrequencyFactorLocal);
        CurrentFrequencyFactorAbsolute = (FrequencyFactorStart * CurrentFrequencyFactorLocal);
        CurrentLoudness = pnt0.Loudness + (TimeAlong * LoudnessRate);

        CurrentFrequency = BaseFreq * CurrentFrequencyFactorAbsolute;// do we really need to include the base frequency in the summing?
        Amplitude = Math.sin(SubTimeIterate);
        wave0.Set(Amplitude * CurrentLoudness);
        //wave0.wave[scnt] = Amplitude * CurrentLoudness;
        SubTimeIterate += (CurrentFrequency * Globals.TwoPi) / SRate;
      }
    }
    /* ********************************************************************************* */
    @Override
    public void Render_Segment_Integral(Point pnt0, Point pnt1, Wave wave1) {// stateless calculus integral approach
      double BaseFreq = Globals.BaseFreqC0;
      double SRate = Globals.SampleRate;
      BaseFreq = 1.0;
      //SRate = 100.0;
      //SRate = 1000.0;
      double TimeRange = pnt1.RealTime - pnt0.RealTime;
      double SampleDuration = 1.0 / SRate;
      double FrequencyFactorStart = pnt0.GetFrequencyFactor();
      double OctaveRange = pnt1.Octave - pnt0.Octave;
      if (OctaveRange == 0.0) {
        OctaveRange = Globals.Fudge;// Fudge to avoid div by 0 
      }
      double LoudnessRange = pnt1.Loudness - pnt0.Loudness;
      double OctaveRate = OctaveRange / TimeRange;// octaves per second
      double LoudnessRate = LoudnessRange / TimeRange;
      double SubTimeLocal;
      double SubTimeAbsolute;
      int NumSamples = (int) (TimeRange * SRate);

      double TimeAlong;
      double CurrentLoudness;
      double Amplitude;

      for (int scnt = 0; scnt < NumSamples; scnt++) {
        TimeAlong = scnt * SampleDuration;
        CurrentLoudness = pnt0.Loudness + (TimeAlong * LoudnessRate);

        SubTimeLocal = Calculus(OctaveRate, TimeAlong);
        SubTimeAbsolute = pnt0.SubTime + (FrequencyFactorStart * SubTimeLocal);
        Amplitude = Math.sin(SubTimeAbsolute * BaseFreq * Globals.TwoPi);
        //wave1.wave[scnt] = Amplitude * CurrentLoudness;
        wave1.Set(Amplitude * CurrentLoudness);
      }
    }
  }
  /* ********************************************************************************* */
  double SineGenerator(double time, double frequency, int sampleRate) {// http://stackoverflow.com/questions/8566938/how-to-properly-bend-a-note-in-an-audio-synthesis-application
    return Math.sin(time += (frequency * 2 * Math.PI) / sampleRate);
  }
  /* ********************************************************************************* */
  public Voice() {
  }
  /* ********************************************************************************* */
  @Override
  public Player_Head_Base Spawn_Player() {
    return this.Spawn_My_Player();
  }
  /* ********************************************************************************* */
  public Player_Head Spawn_My_Player() {
    // Deliver one of my players while exposing specific object class. 
    // Handy if my parent's players know what class I am and want special access to my particular type of player.
    Player_Head ph = new Player_Head();
    ph.MyPhrase = this;
    return ph;
  }
  /* ********************************************************************************* */
  public void Recalc_Line_SubTime() {// ready for test
    double SubTimeLocal;// run this function whenever this voice instance is modified, e.g. control points moved, added, or removed. 
    int len = this.CPoints.size();
    if (len < 1) {
      return;
    }
    this.Sort_Me();
    Point Prev_Point, Next_Point, Dummy_First;
    Next_Point = this.CPoints.get(0);
    Dummy_First = new Point();
    Dummy_First.CopyFrom(Next_Point);
    Dummy_First.SubTime = Dummy_First.RealTime = 0.0;// Times must both start at 0, even though user may have put the first audible point at T greater than 0. 
    Next_Point = Dummy_First;
    for (int pcnt = 0; pcnt < len; pcnt++) {
      Prev_Point = Next_Point;
      Next_Point = this.CPoints.get(pcnt);
      double FrequencyFactorStart = Prev_Point.GetFrequencyFactor();
      double TimeRange = Next_Point.RealTime - Prev_Point.RealTime;
      double OctaveRange = Next_Point.Octave - Prev_Point.Octave;
      if (TimeRange == 0.0) {
        TimeRange = Globals.Fudge;// Fudge to avoid div by 0 
      }
      double OctaveRate = OctaveRange / TimeRange;// octaves per second
      SubTimeLocal = Calculus(OctaveRate, TimeRange);
      Next_Point.SubTime = Prev_Point.SubTime + (FrequencyFactorStart * SubTimeLocal);
    }
  }
  /* ********************************************************************************* */
  public static double Calculus(double OctaveRate, double TimeAlong) {// ready for test
    double SubTimeCalc;// given realtime passed and rate of octave change, use integration to get the sum of all subjective time passed.  
    if (OctaveRate == 0.0) {
      OctaveRate = Globals.Fudge;// Fudge to avoid div by 0 
    }
    double Denom = (Math.log(2) * OctaveRate);// returns the integral of (2 ^ (TimeAlong * OctaveRate))
    //SubTimeCalc = (Math.pow(2, (TimeAlong * OctaveRate)) / Denom) - (1.0 / Denom);
    SubTimeCalc = ((Math.pow(2, (TimeAlong * OctaveRate)) - 1.0) / Denom);
    return SubTimeCalc;
  }
}
