/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
//import voices.VoiceBase.Point;

/**
 *
 * @author MultiTool
 */
public class Voice implements ISonglet {//extends VoiceBase{
  // collection of control points, each one having a pitch and a volume. rendering morphs from one cp to another. 
  public ArrayList<Point> CPoints = new ArrayList<>();
  private Project MyProject;
  /* ********************************************************************************* */
  public static class VoiceOffsetBox extends OffsetBox {// location box to transpose in pitch, move in time, etc. 
    public Voice Content;
    /* ********************************************************************************* */
    @Override public ISonglet GetContent() {
      return Content;
    }
    /* ********************************************************************************* */
    @Override public Singer Spawn_Singer() {// always always always override this
      return this.Spawn_My_Singer();
    }
    /* ********************************************************************************* */
    public Player_Head Spawn_My_Singer() {// for render time
      Player_Head ph = this.Content.Spawn_My_Singer();
      ph.MyOffsetBox = this;// to do: also transfer all of this box's offsets to player head. 
      // also be sure to increment the player's offsets by the offsets that were handed down to me. 
      // or should each player head reach up its chain of parents? 
      // what about FxContainer class? all containers are FxContainers. 
      //ph.Compound(this);
      return ph;
      /*
       best pattern is
       in containing player {
       ChildSinger = ChildOffsetBox.Spawn_Singer();
       ChildSinger.Compound(total parent offsets);// inheritance
       ChildSinger.Compound(ChildOffsetBox);
       }
       */
    }
  }
  /* ********************************************************************************* */
  public static class Player_Head extends Singer {
    protected Voice MyPhrase;
    protected OffsetBox MyOffsetBox = OffsetBox.Identity;
    double Phase, Cycles;// Cycles is the number of cycles we've rotated since the start of this voice. The fractional part is the phase information. 
    double SubTime;// Subjective time.
    double Current_Octave, Current_Frequency;
    int Prev_Point_Dex, Next_Point_Dex;
    int Sample_Dex;
    Point Cursor_Point = new Point();
    /* ********************************************************************************* */
    private Player_Head() {
      this.ParentSinger = null;
      //this.Start();
    }
    /* ********************************************************************************* */
    @Override public void Start() {
      this.SubTime = 0.0;
      this.Phase = 0.0;
      this.Cycles = 0.0;
      this.Prev_Point_Dex = 0;//this.Parent.CPoints.get(0);
      this.Next_Point_Dex = 1;
      this.Sample_Dex = 0;
      this.IsFinished = false;
      //if (this.Parent != null) {
      Point ppnt = this.MyPhrase.CPoints.get(this.Prev_Point_Dex);
      this.Cursor_Point.CopyFrom(ppnt);
      //}
    }
    /* ********************************************************************************* */
    @Override public void Skip_To(double EndTime) {// ready for test
      Point Prev_Point, Next_Point;
      EndTime = this.MyOffsetBox.MapTime(EndTime);// EndTime is now time internal to voice's own coordinate system
      this.Sample_Dex = 0;
      int len = this.MyPhrase.CPoints.size();
      if (len < 2) {// this should really just throw an error
        this.IsFinished = true;
        return;
      }
      if (EndTime < Cursor_Point.RealTime) {
        EndTime = Cursor_Point.RealTime;// clip time
      }
      Point Final_Point = this.MyPhrase.CPoints.get(len - 1);
      if (EndTime > Final_Point.RealTime) {
        this.IsFinished = true;
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
    @Override public void Render_To(double EndTime, Wave wave) {// ready for test
      Point Prev_Point, Next_Point;
      EndTime = this.MyOffsetBox.MapTime(EndTime);// EndTime is now time internal to voice's own coordinate system
      double UnMapped_Prev_Time = this.MyOffsetBox.UnMapTime(this.Cursor_Point.RealTime);// get start time in parent coordinates
      this.Sample_Dex = 0;
      int len = this.MyPhrase.CPoints.size();
      if (len < 2) {// this should really just throw an error
        this.IsFinished = true;
        wave.Init(UnMapped_Prev_Time, UnMapped_Prev_Time, this.MyProject.SampleRate);
        return;
      }
      if (EndTime < Cursor_Point.RealTime) {
        EndTime = Cursor_Point.RealTime;// clip time
      }
      Point Final_Point = this.MyPhrase.CPoints.get(len - 1);
      if (EndTime > Final_Point.RealTime) {
        this.IsFinished = true;
        EndTime = Final_Point.RealTime;// clip time
      }
      double UnMapped_EndTime = this.MyOffsetBox.UnMapTime(EndTime);
      wave.Init(UnMapped_Prev_Time, UnMapped_EndTime, this.MyProject.SampleRate);// wave times are in parent coordinates because the parent will be reading the wave data.
      Prev_Point = this.Cursor_Point;
      int pdex = this.Next_Point_Dex;
      Next_Point = this.MyPhrase.CPoints.get(pdex);
      while (Next_Point.RealTime < EndTime) {
        Render_Segment_Integral(Prev_Point, Next_Point, wave);
        pdex++;
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
      wave.Amplify(this.MyOffsetBox.LoudnessFactor);
    }
    /* ********************************************************************************* */
    @Override public IOffsetBox Get_OffsetBox() {
      return this.MyOffsetBox;
    }
    /* ********************************************************************************* */
    public void Render_Range(int dex0, int dex1, Wave wave) {
      Point pnt0, pnt1;
      this.Sample_Dex = 0;
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
    public void Render_Segment_Iterative(Point pnt0, Point pnt1, Wave wave) {// stateful iterative approach
      double BaseFreq = Globals.BaseFreqC0;
      double SRate = this.MyProject.SampleRate;
      BaseFreq = 1.0;
      double TimeRange = pnt1.RealTime - pnt0.RealTime;
      double SampleDuration = 1.0 / SRate;
      double FrequencyFactorStart = pnt0.GetFrequencyFactor();
      FrequencyFactorStart *= Math.pow(2.0, this.Inherited_Octave);// inherit transposition 
      double Octave0 = this.Inherited_Octave + pnt0.Octave, Octave1 = this.Inherited_Octave + pnt1.Octave;
      double OctaveRange = Octave1 - Octave0;
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
        wave.Set(this.Sample_Dex, Amplitude * CurrentLoudness);
        //wave0.wave[scnt] = Amplitude * CurrentLoudness;
        SubTimeIterate += (CurrentFrequency * Globals.TwoPi) / SRate;
        this.Sample_Dex++;
      }
    }
    /* ********************************************************************************* */
    public void Render_Segment_Integral(Point pnt0, Point pnt1, Wave wave) {// stateless calculus integral approach
      double BaseFreq = Globals.BaseFreqC0;
      double SRate = this.MyProject.SampleRate;
      double TimeRange = pnt1.RealTime - pnt0.RealTime;
      double SampleDuration = 1.0 / SRate;
      double FrequencyFactorStart = pnt0.GetFrequencyFactor();
      double FrequencyFactorInherited = Math.pow(2.0, this.Inherited_Octave);// inherit transposition 

      // double Inherited_Time = 0.0, Inherited_Loudness = 1.0;// time, octave, and loudness context
      double Octave0 = this.Inherited_Octave + pnt0.Octave, Octave1 = this.Inherited_Octave + pnt1.Octave;
      double OctaveRange = Octave1 - Octave0;
      if (OctaveRange == 0.0) {
        OctaveRange = Globals.Fudge;// Fudge to avoid div by 0 
      }
      double LoudnessRange = pnt1.Loudness - pnt0.Loudness;
      double OctaveRate = OctaveRange / TimeRange;// octaves per second bend
      OctaveRate += this.Inherited_OctaveRate;// inherit note bend 
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
        SubTimeAbsolute = (pnt0.SubTime + (FrequencyFactorStart * SubTimeLocal)) * FrequencyFactorInherited;
        Amplitude = Math.sin(SubTimeAbsolute * BaseFreq * Globals.TwoPi);
        //wave.wave[scnt] = Amplitude * CurrentLoudness;
        wave.Set(this.Sample_Dex, Amplitude * CurrentLoudness);
        this.Sample_Dex++;
      }
    }
  }
  /* ********************************************************************************* */
  public static class Point {
    public double RealTime = 0.0, SubTime = 0.0;// SubTime is cumulative subjective time.
    public double Octave = 0.0;
    public double Loudness;
    public void CopyFrom(Point source) {
      this.RealTime = source.RealTime;
      this.SubTime = source.SubTime;
      this.Octave = source.Octave;
      this.Loudness = source.Loudness;
    }
    public double GetFrequencyFactor() {
      return Math.pow(2.0, this.Octave);
    }
  }
  /* ********************************************************************************* */
  public Voice() {
  }
  /* ********************************************************************************* */
  @Override public OffsetBox Spawn_OffsetBox() {// for compose time
    return this.Spawn_My_OffsetBox();
  }
  /* ********************************************************************************* */
  public VoiceOffsetBox Spawn_My_OffsetBox() {// for compose time
    VoiceOffsetBox lbox = new VoiceOffsetBox();// Deliver a OffsetBox specific to this type of phrase.
    lbox.Content = this;
    return lbox;
  }
  /* ********************************************************************************* */
  @Override public Singer Spawn_Singer() {// for render time
    return this.Spawn_My_Singer();
  }
  /* ********************************************************************************* */
  public Player_Head Spawn_My_Singer() {// for render time
    // Deliver one of my players while exposing specific object class. 
    // Handy if my parent's players know what class I am and want special access to my particular type of player.
    Player_Head ph = new Player_Head();
    ph.MyPhrase = this;
    ph.MyProject = this.MyProject;// inherit project
    return ph;
  }
  /* ********************************************************************************* */
  public void Add_Note(Point pnt) {
    this.CPoints.add(pnt);
  }
  /* ********************************************************************************* */
  public Point Add_Note(double RealTime, double Octave, double Loudness) {
    Point pnt = new Point();
    pnt.Octave = Octave;
    pnt.RealTime = RealTime;
    pnt.SubTime = 0.0;
    pnt.Loudness = Loudness;
    this.CPoints.add(pnt);
    return pnt;
  }
  /* ********************************************************************************* */
  @Override public int Get_Sample_Count(int SampleRate) {
    int len = this.CPoints.size();
    Point First_Point = this.CPoints.get(0);
    Point Final_Point = this.CPoints.get(len - 1);
    double TimeDiff = Final_Point.RealTime - First_Point.RealTime;
    return (int) (TimeDiff * SampleRate);
    // return (int) (Final_Point.RealTime * SampleRate);
  }
  /* ********************************************************************************* */
  @Override public double Get_Duration() {
    int len = this.CPoints.size();
    if (len <= 0) {
      return 0;
    }
    Point Final_Point = this.CPoints.get(len - 1);
    return Final_Point.RealTime;
  }
  /* ********************************************************************************* */
  @Override public double Update_Durations() {
    return this.Get_Duration();// this is not a container, so just return what we already know
  }
  /* ********************************************************************************* */
  @Override public void Update_Guts(MetricsPacket metrics) {
    this.Sort_Me();
    this.Recalc_Line_SubTime();
    metrics.MaxDuration = this.Get_Duration();
  }
  /* ********************************************************************************* */
  @Override public void Sort_Me() {// sorting by RealTime
    Collections.sort(this.CPoints, new Comparator<Point>() {
      @Override public int compare(Point note0, Point note1) {
        return Double.compare(note0.RealTime, note1.RealTime);
      }
    });
  }
  /* ********************************************************************************* */
  @Override public Project Get_Project() {
    return this.MyProject;
  }
  /* ********************************************************************************* */
  @Override public void Set_Project(Project project) {
    this.MyProject = project;
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
    // this.Update_Durations();
  }
  /* ********************************************************************************* */
  public static double Calculus(double OctaveRate, double TimeAlong) {// ready for test
    double SubTimeCalc;// given realtime passed and rate of octave change, use integration to get the sum of all subjective time passed.  
    if (OctaveRate == 0.0) {
      OctaveRate = Globals.Fudge;// Fudge to avoid div by 0 
    }
    // Yep calling log and pow functions for every sample generated is expensive. We will have to optimize later. 
    double Denom = (Math.log(2) * OctaveRate);// returns the integral of (2 ^ (TimeAlong * OctaveRate))
    //SubTimeCalc = (Math.pow(2, (TimeAlong * OctaveRate)) / Denom) - (1.0 / Denom);
    SubTimeCalc = ((Math.pow(2, (TimeAlong * OctaveRate)) - 1.0) / Denom);
    return SubTimeCalc;
  }
}
