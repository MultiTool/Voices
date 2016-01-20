package voices;

import java.awt.Color;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 * @author MultiTool
 */
public class Voice implements ISonglet, IDrawable {
  // collection of control points, each one having a pitch and a volume. rendering morphs from one cp to another. 
  public ArrayList<VoicePoint> CPoints = new ArrayList<>();
  protected AudProject MyProject;
  private double MaxAmplitude;
  private int FreshnessTimeStamp;
  protected double BaseFreq = Globals.BaseFreqC0;
  // graphics support
  CajaDelimitadora MyBounds = new CajaDelimitadora();
  /* ********************************************************************************* */
  public Voice() {
    this.MaxAmplitude = 1.0;
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
  public Voice_Singer Spawn_My_Singer() {// for render time
    // Deliver one of my singers while exposing specific object class. 
    // Handy if my parent's singers know what class I am and want special access to my particular type of singer.
    Voice_Singer ph = new Voice_Singer();
    ph.MyVoice = this;
    ph.MyProject = this.MyProject;// inherit project
    ph.BaseFreq = this.BaseFreq;
    return ph;
  }
  /* ********************************************************************************* */
  public void Add_Note(VoicePoint pnt) {
    this.CPoints.add(pnt);
  }
  /* ********************************************************************************* */
  public VoicePoint Add_Note(double RealTime, double Octave, double Loudness) {
    VoicePoint pnt = new VoicePoint();
    pnt.OctaveY = Octave;
    pnt.TimeX = RealTime;
    pnt.SubTime = 0.0;
    pnt.LoudnessFactor = Loudness;
    this.CPoints.add(pnt);
    return pnt;
  }
  /* ************************************************************************************************************************ */
  public int Tree_Search(double Time, int minloc, int maxloc) {// finds place where time would be inserted or replaced
    int medloc;
    while (minloc < maxloc) {
      medloc = (minloc + maxloc) >> 1; // >>1 is same as div 2, only faster.
      if (Time <= this.CPoints.get(medloc).TimeX) {
        maxloc = medloc;
      }/* has to go through here to be found. */ else {
        minloc = medloc + 1;
      }
    }
    return minloc;
  }
  /* ********************************************************************************* */
  @Override public int Get_Sample_Count(int SampleRate) {
    int len = this.CPoints.size();
    VoicePoint First_Point = this.CPoints.get(0);
    VoicePoint Final_Point = this.CPoints.get(len - 1);
    double TimeDiff = Final_Point.TimeX - First_Point.TimeX;
    return (int) (TimeDiff * SampleRate);
    // return (int) (Final_Point.TimeX * SampleRate);
  }
  /* ********************************************************************************* */
  @Override public double Get_Duration() {
    int len = this.CPoints.size();
    if (len <= 0) {
      return 0;
    }
    VoicePoint Final_Point = this.CPoints.get(len - 1);
    return Final_Point.TimeX;
  }
  /* ********************************************************************************* */
  @Override public double Update_Durations() {
    return this.Get_Duration();// this is not a container, so just return what we already know
  }
  /* ********************************************************************************* */
  @Override public double Get_Max_Amplitude() {
    return this.MaxAmplitude;
  }
  /* ********************************************************************************* */
  public void Update_Max_Amplitude() {
    int len = this.CPoints.size();
    VoicePoint pnt;
    double MaxAmp = 0.0;
    for (int pcnt = 0; pcnt < len; pcnt++) {
      pnt = this.CPoints.get(pcnt);
      if (MaxAmp < pnt.LoudnessFactor) {
        MaxAmp = pnt.LoudnessFactor;
      }
    }
    this.MaxAmplitude = MaxAmp;
  }
  /* ********************************************************************************* */
  @Override public void Update_Guts(MetricsPacket metrics) {
    if (this.FreshnessTimeStamp >= metrics.FreshnessTimeStamp) {// don't hit the same songlet twice on one update
      return;
    }
    this.Set_Project(metrics.MyProject);
    this.Sort_Me();
    this.Recalc_Line_SubTime();
    this.Update_Max_Amplitude();
    metrics.MaxDuration = this.Get_Duration();
    this.FreshnessTimeStamp = metrics.FreshnessTimeStamp;
  }
  /* ********************************************************************************* */
  @Override public void Sort_Me() {// sorting by TimeX
    Collections.sort(this.CPoints, new Comparator<VoicePoint>() {
      @Override public int compare(VoicePoint note0, VoicePoint note1) {
        return Double.compare(note0.TimeX, note1.TimeX);
      }
    });
  }
  /* ********************************************************************************* */
  @Override public AudProject Get_Project() {
    return this.MyProject;
  }
  /* ********************************************************************************* */
  @Override public void Set_Project(AudProject project) {
    this.MyProject = project;
  }
  /* ********************************************************************************* */
  @Override public int FreshnessTimeStamp_g() {// ISonglet
    return this.FreshnessTimeStamp;
  }
  @Override public void FreshnessTimeStamp_s(int TimeStampNew) {// ISonglet
    this.FreshnessTimeStamp = TimeStampNew;
  }
  /* ********************************************************************************* */
  public void Recalc_Line_SubTime() {// ready for test
    double SubTimeLocal;// run this function whenever this voice instance is modified, e.g. control points moved, added, or removed. 
    int len = this.CPoints.size();
    if (len < 1) {
      return;
    }
    this.Sort_Me();
    VoicePoint Prev_Point, Next_Point, Dummy_First;
    Next_Point = this.CPoints.get(0);
    Dummy_First = new VoicePoint();
    Dummy_First.CopyFrom(Next_Point);
    Dummy_First.SubTime = Dummy_First.TimeX = 0.0;// Times must both start at 0, even though user may have put the first audible point at T greater than 0. 
    Next_Point = Dummy_First;
    for (int pcnt = 0; pcnt < len; pcnt++) {
      Prev_Point = Next_Point;
      Next_Point = this.CPoints.get(pcnt);
      double FrequencyFactorStart = Prev_Point.GetFrequencyFactor();
      double TimeRange = Next_Point.TimeX - Prev_Point.TimeX;
      double OctaveRange = Next_Point.OctaveY - Prev_Point.OctaveY;
      if (TimeRange == 0.0) {
        TimeRange = Globals.Fudge;// Fudge to avoid div by 0 
      }
      double OctaveRate = OctaveRange / TimeRange;// octaves per second
      SubTimeLocal = Integral(OctaveRate, TimeRange);
      Next_Point.SubTime = Prev_Point.SubTime + (FrequencyFactorStart * SubTimeLocal);
    }
  }
  /* ********************************************************************************* */
  public static double Integral(double OctaveRate, double TimeAlong) {// to do: optimize this!
    double SubTimeCalc;// given realtime passed and rate of octave change, use integration to get the sum of all subjective time passed.  
    if (OctaveRate == 0.0) {// do we really have to check this for every sample? more efficient to do it once up front.
      return TimeAlong;
    }
    // Yep calling log and pow functions for every sample generated is expensive. We will have to optimize later. 
    double Denom = (Math.log(2) * OctaveRate);// returns the integral of (2 ^ (TimeAlong * OctaveRate))
    //SubTimeCalc = (Math.pow(2, (TimeAlong * OctaveRate)) / Denom) - (1.0 / Denom);
    SubTimeCalc = ((Math.pow(2, (TimeAlong * OctaveRate)) - 1.0) / Denom);
    return SubTimeCalc;
  }
  /* ********************************************************************************* */
//  public double GetWaveForm(double SubTimeAbsolute) {
//    return Math.sin(SubTimeAbsolute * this.BaseFreq * Globals.TwoPi);
//  }
  /* ********************************************************************************* */
  @Override public void Draw_Me(Drawing_Context ParentDC) {// IDrawable
    CajaDelimitadora ChildrenBounds = ParentDC.ClipBounds;// parent is already transformed by my offsetbox
    VoicePoint pnt;
    int len = this.CPoints.size();
    Path2D.Double pgon = new Path2D.Double();
    double Xloc, Yloc, YlocLow, YlocHigh;

    // work in progress. to do: make a ribbon-shaped polygon whose width is based on point loudness.
    // do we have to go around and then go backward? if pgon were a real array we could fill both sides at once.
    // will probably have to use the x array, y array API. bleh. 
    int StartDex, EndDex, Range;
    StartDex = 0;
    EndDex = len;
    Range = EndDex - StartDex;
    int NumDrawPoints = Range * 2;
    int[] OutlineX = new int[NumDrawPoints];
    int[] OutlineY = new int[NumDrawPoints];
    int[] SpineX = new int[Range];
    int[] SpineY = new int[Range];
    double LoudnessHgt;
    int CntUp = Range, CntDown = Range - 1, CntSpine = 0;
//    pnt = this.CPoints.get(StartDex);
//    double LoudnessHgt = pnt.LoudnessFactor * pnt.OctavesPerLoudness;
//    Xloc = ParentDC.GlobalOffset.UnMapTime(pnt.TimeX);
//    Yloc = ParentDC.GlobalOffset.UnMapPitch(pnt.OctaveY);
//    YlocLow = ParentDC.GlobalOffset.UnMapPitch(pnt.OctaveY - LoudnessHgt);
//    YlocHigh = ParentDC.GlobalOffset.UnMapPitch(pnt.OctaveY + LoudnessHgt);
//    pgon.moveTo(Xloc, Yloc);
    for (int pcnt = StartDex; pcnt < EndDex; pcnt++) {
      pnt = this.CPoints.get(pcnt);
      LoudnessHgt = pnt.LoudnessFactor * pnt.OctavesPerLoudness;
      Xloc = ParentDC.GlobalOffset.UnMapTime(pnt.TimeX);// map to pixels
      Yloc = ParentDC.GlobalOffset.UnMapPitch(pnt.OctaveY);// map to pixels
      SpineX[CntSpine] = (int) Xloc;
      SpineY[CntSpine] = (int) Yloc;
      YlocLow = ParentDC.GlobalOffset.UnMapPitch(pnt.OctaveY - LoudnessHgt);
      YlocHigh = ParentDC.GlobalOffset.UnMapPitch(pnt.OctaveY + LoudnessHgt);
      OutlineX[CntUp] = (int) Xloc;
      OutlineY[CntUp] = (int) YlocLow;
      //OutlineY[CntUp] = (int) Yloc;// should the loudness envelope just be flat on the bottom, with one control point? 
      OutlineX[CntDown] = (int) Xloc;
      OutlineY[CntDown] = (int) YlocHigh;
      //pgon.lineTo(Xloc, Yloc);
      CntUp++;
      CntDown--;
      CntSpine++;
    }
    //int colorToSet = Color.argb(alpha, red, green, blue); 
    ParentDC.gr.setColor(Globals.ToAlpha(Color.cyan, 60));// Color.yellow
    ParentDC.gr.fillPolygon(OutlineX, OutlineY, NumDrawPoints);// voice fill

    ParentDC.gr.setColor(Globals.ToAlpha(Color.darkGray, 100));
    //ParentDC.gr.drawPolygon(OutlineX, OutlineY, NumDrawPoints);// voice outline
    // pgon.closePath(); ParentDC.gr.fill(pgon);

    Color Emerald = new Color(0, 0.5f, 0);// rgb
    ParentDC.gr.setColor(Globals.ToAlpha(Emerald, 200));
//    ParentDC.gr.setColor(Globals.ToAlpha(Color.green, 200));
    ParentDC.gr.drawPolyline(SpineX, SpineY, Range);

    for (int pcnt = 0; pcnt < len; pcnt++) {
      pnt = this.CPoints.get(pcnt);
      if (ChildrenBounds.Intersects(pnt.GetBoundingBox())) {
        pnt.Draw_Me(ParentDC);
      }
    }
  }
  /* ********************************************************************************* */
  @Override public CajaDelimitadora GetBoundingBox() {// IDrawable
    return this.MyBounds;
  }
  /* ********************************************************************************* */
  @Override public void UpdateBoundingBox() {// IDrawable
    VoicePoint pnt;
    //this.MyBounds.Reset();
    int len = this.CPoints.size();
    for (int pcnt = 0; pcnt < len; pcnt++) {
      pnt = this.CPoints.get(pcnt);
      pnt.UpdateBoundingBox();
      //this.MyBounds.Include(pnt.MyBounds);// Don't have to UnMap in this case because my points are already in my internal coordinates.
    }
    this.UpdateBoundingBoxLocal();
  }
  @Override public void UpdateBoundingBoxLocal() {// IDrawable
    VoicePoint pnt;
    this.MyBounds.Reset();
    int len = this.CPoints.size();
    for (int pcnt = 0; pcnt < len; pcnt++) {
      pnt = this.CPoints.get(pcnt);
      this.MyBounds.Include(pnt.MyBounds);// Don't have to UnMap in this case because my points are already in my internal coordinates.
    }
  }
  /* ********************************************************************************* */
  @Override public void GoFishing(Grabber Scoop) {// IDrawable
    System.out.print(" Voice GoFishing: ");
    if (Scoop.CurrentContext.SearchBounds.Intersects(MyBounds)) {
      int len = this.CPoints.size();
      VoicePoint pnt;
      for (int pcnt = 0; pcnt < len; pcnt++) {
        System.out.print("" + pcnt + ", ");
        pnt = this.CPoints.get(pcnt);
        pnt.GoFishing(Scoop);
      }
    }
    System.out.println();
  }
  /* ********************************************************************************* */
  @Override public boolean Create_Me() {// IDeletable
    return true;
  }
  @Override public void Delete_Me() {// IDeletable
    this.MyBounds.Delete_Me();
    int len = this.CPoints.size();
    for (int cnt = 0; cnt < len; cnt++) {
      this.CPoints.get(cnt).Delete_Me();
    }
    this.CPoints.clear();
  }
  /* ********************************************************************************* */
  public static class VoiceOffsetBox extends OffsetBox {// location box to transpose in pitch, move in time, etc. 
    public Voice Content;
    /* ********************************************************************************* */
    public VoiceOffsetBox() {
      super();
      MyBounds = new CajaDelimitadora();
      this.Clear();
    }
    /* ********************************************************************************* */
    @Override public ISonglet GetContent() {
      return Content;
    }
    /* ********************************************************************************* */
    @Override public Singer Spawn_Singer() {// always always always override this
      return this.Spawn_My_Singer();
    }
    /* ********************************************************************************* */
    public Voice_Singer Spawn_My_Singer() {// for render time
      Voice_Singer ph = this.Content.Spawn_My_Singer();
      ph.MyOffsetBox = this;
      return ph;
    }
    /* ********************************************************************************* */
    @Override public OffsetBox Clone_Me() {// always override this thusly
      VoiceOffsetBox child = new VoiceOffsetBox();
      child.Copy_From(this);
      child.Content = this.Content;
      return child;
    }
  }
  /* ********************************************************************************* */
  public static class Voice_Singer extends Singer {
    protected Voice MyVoice;
    double Phase, Cycles;// Cycles is the number of cycles we've rotated since the start of this voice. The fractional part is the phase information. 
    double SubTime;// Subjective time.
    double Current_Octave, Current_Frequency;
    int Prev_Point_Dex, Next_Point_Dex;
    int Render_Sample_Count;
    VoicePoint Cursor_Point = new VoicePoint();
    protected int Bone_Sample_Mark = 0;
    double BaseFreq;
    /* ********************************************************************************* */
    protected Voice_Singer() {
      this.Create_Me();
      this.ParentSinger = null;
    }
    /* ********************************************************************************* */
    @Override public void Start() {
      this.SubTime = 0.0;
      this.Phase = 0.0;
      this.Cycles = 0.0;
      this.Prev_Point_Dex = 0;//this.Parent.CPoints.get(0);
      this.Next_Point_Dex = 1;
      this.Render_Sample_Count = 0;
      this.Bone_Sample_Mark = 0;
      this.IsFinished = false;
      if (this.MyVoice.CPoints.size() > 0) {
        VoicePoint pnt = this.MyVoice.CPoints.get(0);
        this.Bone_Sample_Mark = (int) (pnt.TimeX * this.MyProject.SampleRate);
      }
      //if (this.Parent != null) {
      VoicePoint ppnt = this.MyVoice.CPoints.get(this.Prev_Point_Dex);
      this.Cursor_Point.CopyFrom(ppnt);
      //}
    }
    /* ********************************************************************************* */
    @Override public void Skip_To(double EndTime) {      // ready for test
      VoicePoint Prev_Point;
      VoicePoint Next_Point;
      EndTime = this.MyOffsetBox.MapTime(EndTime);// EndTime is now time internal to voice's own coordinate system
      this.Render_Sample_Count = 0;
      int NumPoints = this.MyVoice.CPoints.size();
      if (NumPoints < 2) {// this should really just throw an error
        this.IsFinished = true;
        return;
      }
      EndTime = this.ClipTime(EndTime);
      Prev_Point = this.Cursor_Point;
      int pdex = this.Next_Point_Dex;
      Next_Point = this.MyVoice.CPoints.get(pdex);
      while (Next_Point.TimeX < EndTime) {
        pdex++;
        Prev_Point = Next_Point;
        Next_Point = this.MyVoice.CPoints.get(pdex);
      }
      this.Next_Point_Dex = pdex;
      this.Prev_Point_Dex = this.Next_Point_Dex - 1;

      // deal with loose end. 
      if (EndTime <= Next_Point.TimeX) {
        if (Prev_Point.TimeX <= EndTime) {// EndTime is inside this box. 
          this.Cursor_Point.CopyFrom(Prev_Point);// this section should always be executed, due to time clipping
          Interpolate_ControlPoint(Prev_Point, Next_Point, EndTime, this.Cursor_Point);
        }
      }
      //int EndSample = (int) (pnt1.TimeX * SRate);// absolute
      int EndSample = (int) (EndTime * this.MyProject.SampleRate);// absolute
      this.Bone_Sample_Mark = EndSample;
    }
    /* ********************************************************************************* */
    @Override public void Render_To(double EndTime, Wave wave) {      // ready for test
      VoicePoint Prev_Point = null;
      VoicePoint Next_Point = null;
      EndTime = this.MyOffsetBox.MapTime(EndTime);// EndTime is now time internal to voice's own coordinate system

      double UnMapped_Prev_Time = this.MyOffsetBox.UnMapTime(this.Cursor_Point.TimeX);// get start time in parent coordinates
      this.Render_Sample_Count = 0;
      int NumPoints = this.MyVoice.CPoints.size();
      if (NumPoints < 2) {// this should really just throw an error
        this.IsFinished = true;
        wave.Init(UnMapped_Prev_Time, UnMapped_Prev_Time, this.MyProject.SampleRate);
        return;
      }
      EndTime = this.ClipTime(EndTime);
      double UnMapped_EndTime = this.MyOffsetBox.UnMapTime(EndTime);
      wave.Init(UnMapped_Prev_Time, UnMapped_EndTime, this.MyProject.SampleRate);// wave times are in parent coordinates because the parent will be reading the wave data.
      Prev_Point = this.Cursor_Point;
      int pdex = this.Next_Point_Dex;

      if (true) {
        Next_Point = this.MyVoice.CPoints.get(pdex);
        while (Next_Point.TimeX < EndTime) {
          Render_Segment_Integral(Prev_Point, Next_Point, wave);
          pdex++;
          Prev_Point = Next_Point;
          Next_Point = this.MyVoice.CPoints.get(pdex);
        }
        this.Next_Point_Dex = pdex;
      } else {
        while (this.Next_Point_Dex < NumPoints) {
          Next_Point = this.MyVoice.CPoints.get(this.Next_Point_Dex);
          if (EndTime < Next_Point.TimeX) {// repeat until control point time overtakes EndTime
            break;
          }
          this.Prev_Point_Dex = this.Next_Point_Dex - 1;
          Prev_Point = this.MyVoice.CPoints.get(this.Prev_Point_Dex);
          Render_Segment_Integral(Prev_Point, Next_Point, wave);
          this.Next_Point_Dex++;
        }
      }

      this.Prev_Point_Dex = this.Next_Point_Dex - 1;

      // render loose end. 
      if (EndTime <= Next_Point.TimeX) {
        if (Prev_Point.TimeX <= EndTime) {// EndTime is inside this box. 
          VoicePoint End_Cursor = new VoicePoint();// this section should always be executed, due to time clipping
          End_Cursor.CopyFrom(Prev_Point);
          Interpolate_ControlPoint(Prev_Point, Next_Point, EndTime, End_Cursor);
          Render_Segment_Integral(Prev_Point, End_Cursor, wave);
          this.Cursor_Point.CopyFrom(End_Cursor);
        }
      }
      wave.Amplify(this.MyOffsetBox.LoudnessFactor);
      if (false) {
        this.Distortion_Effect(wave, 4.0);
      }
      wave.NumSamples = this.Render_Sample_Count;
    }
    /* ********************************************************************************* */
    public double GetWaveForm(double SubTimeAbsolute) {// not used currently
      return Math.sin(SubTimeAbsolute * this.MyVoice.BaseFreq * Globals.TwoPi);
    }
    /* ********************************************************************************* */
    public void Distortion_Effect(Wave wave, double gain) {
      double power = 2.0;// sigmoid clipping 
      int len = wave.NumSamples;
      for (int cnt = 0; cnt < len; cnt++) {
        double val = wave.Get(cnt) * gain;
        val = val / Math.pow(1 + Math.abs(Math.pow(val, power)), 1.0 / power);
        wave.Set(val);
      }
    }
    /* ********************************************************************************* */
    public double ClipTime(double EndTime) {
      if (EndTime < Cursor_Point.TimeX) {
        EndTime = Cursor_Point.TimeX;// clip time
      }
      int FinalIndex = this.MyVoice.CPoints.size() - 1;
      VoicePoint Final_Point = this.MyVoice.CPoints.get(FinalIndex);
      if (EndTime > Final_Point.TimeX) {
        this.IsFinished = true;
        EndTime = Final_Point.TimeX;// clip time
      }
      return EndTime;
    }
    /* ********************************************************************************* */
    @Override public OffsetBox Get_OffsetBox() {
      return this.MyOffsetBox;
    }
    /* ********************************************************************************* */
    public void Render_Range(int dex0, int dex1, Wave wave) {
      VoicePoint pnt0;
      VoicePoint pnt1;
      this.Render_Sample_Count = 0;
      for (int pcnt = dex0; pcnt < dex1; pcnt++) {
        pnt0 = this.MyVoice.CPoints.get(pcnt);
        pnt1 = this.MyVoice.CPoints.get(pcnt + 1);
        Render_Segment_Integral(pnt0, pnt1, wave);
      }
    }
    /* ********************************************************************************* */
    public static void Interpolate_ControlPoint(VoicePoint pnt0, VoicePoint pnt1, double RealTime, VoicePoint PntMid) {
      double FrequencyFactorStart = pnt0.GetFrequencyFactor();
      double TimeRange = pnt1.TimeX - pnt0.TimeX;
      double TimeAlong = RealTime - pnt0.TimeX;
      double OctaveRange = pnt1.OctaveY - pnt0.OctaveY;
      double OctaveRate = OctaveRange / TimeRange;// octaves per second
      double SubTimeLocal = Integral(OctaveRate, TimeAlong);
      PntMid.TimeX = RealTime;
      PntMid.SubTime = pnt0.SubTime + (FrequencyFactorStart * SubTimeLocal);

      // not calculus here
      PntMid.OctaveY = pnt0.OctaveY + (TimeAlong * OctaveRate);
      double LoudRange = pnt1.LoudnessFactor - pnt0.LoudnessFactor;
      double LoudAlong = TimeAlong * LoudRange / TimeRange;
      PntMid.LoudnessFactor = pnt0.LoudnessFactor + LoudAlong;
    }
    /* ********************************************************************************* */
    public void Render_Segment_Iterative(VoicePoint pnt0, VoicePoint pnt1, Wave wave) {// stateful iterative approach
      double BaseFreq = this.MyVoice.BaseFreq;
      double SRate = this.MyProject.SampleRate;
      double TimeRange = pnt1.TimeX - pnt0.TimeX;
      double SampleDuration = 1.0 / SRate;
      double FrequencyFactorStart = pnt0.GetFrequencyFactor();
      FrequencyFactorStart *= Math.pow(2.0, this.Inherited_Octave);// inherit transposition 
      double Octave0 = this.Inherited_Octave + pnt0.OctaveY, Octave1 = this.Inherited_Octave + pnt1.OctaveY;
      double OctaveRange = Octave1 - Octave0;
      if (OctaveRange == 0.0) {
        OctaveRange = Globals.Fudge;// Fudge to avoid div by 0 
      }
      double LoudnessRange = pnt1.LoudnessFactor - pnt0.LoudnessFactor;
      double OctaveRate = OctaveRange / TimeRange;// octaves per second
      double LoudnessRate = LoudnessRange / TimeRange;
      int NumSamples = (int) Math.ceil(TimeRange * SRate);

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
        CurrentLoudness = pnt0.LoudnessFactor + (TimeAlong * LoudnessRate);
        CurrentFrequency = BaseFreq * CurrentFrequencyFactorAbsolute;// do we really need to include the base frequency in the summing?
        Amplitude = Math.sin(SubTimeIterate);
        wave.Set(this.Render_Sample_Count, Amplitude * CurrentLoudness);
        SubTimeIterate += (CurrentFrequency * Globals.TwoPi) / SRate;
        this.Render_Sample_Count++;
      }
    }
    /* ********************************************************************************* */
    public void Render_Segment_Integral(VoicePoint pnt0, VoicePoint pnt1, Wave wave) {// stateless calculus integral approach
      //double BaseFreq = Globals.BaseFreqC0;
      double SRate = this.MyProject.SampleRate;
      double TimeRange = pnt1.TimeX - pnt0.TimeX;
      double FrequencyFactorStart = pnt0.GetFrequencyFactor();
      double FrequencyFactorInherited = Math.pow(2.0, this.Inherited_Octave);// inherit transposition 
      double Octave0 = this.Inherited_Octave + pnt0.OctaveY, Octave1 = this.Inherited_Octave + pnt1.OctaveY;
      double OctaveRange = Octave1 - Octave0;
//      if (OctaveRange == 0.0) {
//        OctaveRange = Globals.Fudge;// Fudge to avoid div by 0 
//      }
      double LoudnessRange = pnt1.LoudnessFactor - pnt0.LoudnessFactor;
      double OctaveRate = OctaveRange / TimeRange;// octaves per second bend
      OctaveRate += this.Inherited_OctaveRate;// inherit note bend 
      double LoudnessRate = LoudnessRange / TimeRange;
      double SubTimeLocal;
      double SubTimeAbsolute;
      int EndSample = (int) (pnt1.TimeX * SRate);// absolute
      double TimeAlong;
      double CurrentLoudness;
      double Amplitude;
      int SampleCnt;
      for (SampleCnt = this.Bone_Sample_Mark; SampleCnt < EndSample; SampleCnt++) {
        TimeAlong = (SampleCnt / SRate) - pnt0.TimeX;
        CurrentLoudness = pnt0.LoudnessFactor + (TimeAlong * LoudnessRate);
        SubTimeLocal = Integral(OctaveRate, TimeAlong);
        SubTimeAbsolute = (pnt0.SubTime + (FrequencyFactorStart * SubTimeLocal)) * FrequencyFactorInherited;
        //Amplitude = this.MyVoice.GetWaveForm(SubTimeAbsolute);
        Amplitude = this.GetWaveForm(SubTimeAbsolute);
        wave.Set(this.Render_Sample_Count, Amplitude * CurrentLoudness);
        this.Render_Sample_Count++;
      }
      this.Bone_Sample_Mark = EndSample;
    }
    /* ********************************************************************************* */
    @Override public boolean Create_Me() {// IDeletable
      return true;
    }
    @Override public void Delete_Me() {// IDeletable
      super.Delete_Me();
      this.Cursor_Point.Delete_Me();
    }
  }
}
