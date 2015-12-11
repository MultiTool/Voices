/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
//import javafx.geometry.BoundingBox;
//import voices.VoiceBase.Point;

/**
 *
 * @author MultiTool
 */
public class Voice implements ISonglet, IDrawable {
  // collection of control points, each one having a pitch and a volume. rendering morphs from one cp to another. 
  public ArrayList<Point> CPoints = new ArrayList<>();
  private Project MyProject;
  private double MaxAmplitude;
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
    // Deliver one of my players while exposing specific object class. 
    // Handy if my parent's players know what class I am and want special access to my particular type of player.
    Voice_Singer ph = new Voice_Singer();
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
  @Override public double Get_Max_Amplitude() {
    return this.MaxAmplitude;
  }
  /* ********************************************************************************* */
  public void Update_Max_Amplitude() {
    int len = this.CPoints.size();
    Point pnt;
    double MaxAmp = 0.0;
    for (int pcnt = 0; pcnt < len; pcnt++) {
      pnt = this.CPoints.get(pcnt);
      if (MaxAmp < pnt.Loudness) {
        MaxAmp = pnt.Loudness;
      }
    }
    this.MaxAmplitude = MaxAmp;
  }
  /* ********************************************************************************* */
  @Override public void Update_Guts(MetricsPacket metrics) {
    this.Set_Project(metrics.MyProject);
    this.Sort_Me();
    this.Recalc_Line_SubTime();
    this.Update_Max_Amplitude();
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
      SubTimeLocal = Integral(OctaveRate, TimeRange);
      Next_Point.SubTime = Prev_Point.SubTime + (FrequencyFactorStart * SubTimeLocal);
    }
  }
  /* ********************************************************************************* */
  public static double Integral(double OctaveRate, double TimeAlong) {// to do: optimize this!
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
  /* ********************************************************************************* */
  @Override public void Draw_Me(Drawing_Context ParentDC) {
    Point pnt;
    CajaDelimitadora ChildrenBounds = new CajaDelimitadora();
    OffsetBox obx = ParentDC.Offset;
    ParentDC.Bounds.Map(obx, ChildrenBounds);// map to child (my) internal coordinates
    int len = this.CPoints.size();
    for (int pcnt = 0; pcnt < len; pcnt++) {
      pnt = this.CPoints.get(pcnt);
      // do we have to map bounds before Intersects? 
      // probably cheaper to map the dc to the children rather than vice versa
      // but, the children will have to be mapped all the way up to the screen for actual drawing
      if (ChildrenBounds.Intersects(pnt.GetBoundingBox())) {
        pnt.Draw_Me(ParentDC);
      }
    }
  }
  /* ********************************************************************************* */
  @Override public CajaDelimitadora GetBoundingBox() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  /* ********************************************************************************* */
  @Override public void UpdateBoundingBox() {
    /* ew. just realized that every bounding box would have to be in absolute drawing coordinates
     but since a songlet can exist in multiple places the bounds will probably have be in local coords and then mapped to parent coordinates
     do this on paper before creating a bunch of interface contracts
     */
    Point pnt;
    this.MyBounds.Reset();
    int len = this.CPoints.size();
    for (int pcnt = 0; pcnt < len; pcnt++) {
      pnt = this.CPoints.get(pcnt);
      pnt.UpdateBoundingBox();
      this.MyBounds.Include(pnt.MyBounds);// don't have to UnMap in this case because my points are already in my internal coordinates.
    }
  }
  /* ********************************************************************************* */
  public static class Point implements IDrawable {
    public double RealTime = 0.0, SubTime = 0.0;// SubTime is cumulative subjective time.
    public double Octave = 0.0;
    public double Loudness = 1.0;

    // graphics support
    double Radius = 5, Diameter = Radius * 2.0;
    CajaDelimitadora MyBounds = new CajaDelimitadora();
    /* ********************************************************************************* */
    public void CopyFrom(Point source) {
      this.RealTime = source.RealTime;
      this.SubTime = source.SubTime;
      this.Octave = source.Octave;
      this.Loudness = source.Loudness;
    }
    /* ********************************************************************************* */
    public double GetFrequencyFactor() {
      return Math.pow(2.0, this.Octave);
    }
    /* ********************************************************************************* */
    @Override public void Draw_Me(Drawing_Context ParentDC) {// IDrawable
      IDrawable.Drawing_Context mydc = new IDrawable.Drawing_Context(ParentDC, this);
      Point2D.Double pnt = mydc.To_Screen(mydc.Absolute_X, mydc.Absolute_Y);
      ParentDC.gr.setColor(Color.green);
      mydc.gr.fillOval((int) (pnt.x) - (int) Radius, (int) (pnt.y) - (int) Radius, (int) Diameter, (int) Diameter);
      /* 
       IDrawable.Drawing_Context mydc = new IDrawable.Drawing_Context(dc, this);
       
       // Point2D.Double pnt = mydc.To_Screen(this.Start_Time_G(),this.Get_Pitch());
       Point2D.Double pnt = mydc.To_Screen(mydc.Absolute_X, mydc.Absolute_Y);
       mydc.gr.fillOval((int) (pnt.x) - 5, (int) (pnt.y) - 5, 10, 10);
       //mydc.gr.fillOval((int) (mydc.Absolute_X * xscale) - 5, (int) (mydc.Absolute_Y * yscale) - 5, 10, 10);
       My_Note.Draw_Me(mydc);
       */
      /*
       Drawing_Context mydc = new Drawing_Context(dc, this);
       Point2D.Double pnt = mydc.To_Screen(mydc.Absolute_X, mydc.Absolute_Y);
       mydc.gr.fillOval((int) (pnt.x) - (int) Radius, (int) (pnt.y) - (int) Radius, (int) Diameter, (int) Diameter);
       //mydc.gr.fillOval((int) (mydc.Absolute_X * xscale) - 5, (int) (mydc.Absolute_Y * yscale) - 5, 10, 10);
       // fill triangle here.
       double amphgt = 0.2;
       Polygon pgon = new Polygon();
       int[] xpoints = new int[3];
       int[] ypoints = new int[3];

       Point2D.Double endpnt = mydc.To_Screen(mydc.Absolute_X + this.Duration_G(), mydc.Absolute_Y);
       Point2D.Double pnt0 = mydc.To_Screen(mydc.Absolute_X, mydc.Absolute_Y - amphgt);
       Point2D.Double pnt1 = mydc.To_Screen(mydc.Absolute_X, mydc.Absolute_Y + amphgt);

       xpoints[0] = (int) pnt0.x;
       ypoints[0] = (int) pnt0.y;

       xpoints[1] = (int) pnt1.x;
       ypoints[1] = (int) pnt1.y;

       xpoints[2] = (int) endpnt.x;
       ypoints[2] = (int) endpnt.y;

       mydc.gr.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));

       Color color = Color.cyan;
       //color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.1f); //Red

       //Color color = new Color(1, 0, 0, 0.5); //Red
       //color.getAlpha();  mydc.gr.setPaint(color);

       mydc.gr.setColor(color);
       //mydc.gr.drawPolygon(xpoints, ypoints, 3);
       mydc.gr.fillPolygon(xpoints, ypoints, 3);

       mydc.gr.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
       */
    }
    /* ********************************************************************************* */
    @Override public CajaDelimitadora GetBoundingBox() {
      return this.MyBounds;
    }
    /* ********************************************************************************* */
    @Override public void UpdateBoundingBox() {
      /* ew. just realized that every bounding box would have to be in absolute drawing coordinates
       but since a songlet can exist in multiple places the bounds will probably have be in local coords and then mapped to parent coordinates
       do this on paper before creating a bunch of interface contracts
       */
//      this.MyBounds.Reset();
//      this.MyBounds.IncludePoint(RealTime - Radius, Octave - Radius);
//      this.MyBounds.IncludePoint(RealTime + Radius, Octave + Radius);
      double LoudnessPixels = Loudness * 10;// to do: loudness will have to be mapped to screen. not a pixel value right?
      this.MyBounds.Min.setLocation(RealTime - Radius, Octave - Math.min(Radius, LoudnessPixels));
      this.MyBounds.Max.setLocation(RealTime + Radius, Octave + Math.max(Radius, LoudnessPixels));
    }
  }
  /* ********************************************************************************* */
  public static class VoiceOffsetBox extends OffsetBox {// location box to transpose in pitch, move in time, etc. 
    public Voice Content;
    /* ********************************************************************************* */
    public VoiceOffsetBox() {
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
  }
  /* ********************************************************************************* */
  public static class Voice_Singer extends Singer {
    protected Voice MyPhrase;
    protected OffsetBox MyOffsetBox = new OffsetBox();
    double Phase, Cycles;// Cycles is the number of cycles we've rotated since the start of this voice. The fractional part is the phase information. 
    double SubTime;// Subjective time.
    double Current_Octave, Current_Frequency;
    int Prev_Point_Dex, Next_Point_Dex;
    int Render_Sample_Count, Origin_Sample_Count_Prev, Origin_Sample_Count;
    Point Cursor_Point = new Point();
    /* ********************************************************************************* */
    private Voice_Singer() {
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
      this.Render_Sample_Count = 0;
      this.Origin_Sample_Count_Prev = 0;
      this.Origin_Sample_Count = 0;
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
      this.Origin_Sample_Count_Prev = this.Origin_Sample_Count;
      this.Origin_Sample_Count = (int) Math.floor(EndTime);
      this.Render_Sample_Count = 0;
      // this.Origin_Sample_Count += (EndTime-this.Prev_Time);// 
      int NumPoints = this.MyPhrase.CPoints.size();
      if (NumPoints < 2) {// this should really just throw an error
        this.IsFinished = true;
        return;
      }
      if (EndTime < Cursor_Point.RealTime) {
        EndTime = Cursor_Point.RealTime;// clip time
      }
      Point Final_Point = this.MyPhrase.CPoints.get(NumPoints - 1);
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
      Point Prev_Point = null, Next_Point = null;
      EndTime = this.MyOffsetBox.MapTime(EndTime);// EndTime is now time internal to voice's own coordinate system
      this.Origin_Sample_Count_Prev = this.Origin_Sample_Count;
      this.Origin_Sample_Count = (int) Math.floor(EndTime * (double) this.MyProject.SampleRate);// to do: use this to make sure generated wave is perfectly aligned.

      double UnMapped_Prev_Time = this.MyOffsetBox.UnMapTime(this.Cursor_Point.RealTime);// get start time in parent coordinates
      this.Render_Sample_Count = 0;
      int NumPoints = this.MyPhrase.CPoints.size();
      if (NumPoints < 2) {// this should really just throw an error
        this.IsFinished = true;
        wave.Init(UnMapped_Prev_Time, UnMapped_Prev_Time, this.MyProject.SampleRate);
        return;
      }
      if (EndTime < Cursor_Point.RealTime) {
        EndTime = Cursor_Point.RealTime;// clip time
      }
      Point Final_Point = this.MyPhrase.CPoints.get(NumPoints - 1);
      if (EndTime > Final_Point.RealTime) {
        this.IsFinished = true;
        EndTime = Final_Point.RealTime;// clip time
      }
      double UnMapped_EndTime = this.MyOffsetBox.UnMapTime(EndTime);
      wave.Init(UnMapped_Prev_Time, UnMapped_EndTime, this.MyProject.SampleRate);// wave times are in parent coordinates because the parent will be reading the wave data.
      //wave.Fill(777.0);
      Prev_Point = this.Cursor_Point;
      int pdex = this.Next_Point_Dex;

      if (true) {
        Next_Point = this.MyPhrase.CPoints.get(pdex);
        while (Next_Point.RealTime < EndTime) {
          Render_Segment_Integral(Prev_Point, Next_Point, wave);
          pdex++;
          Prev_Point = Next_Point;
          Next_Point = this.MyPhrase.CPoints.get(pdex);
        }
        this.Next_Point_Dex = pdex;
      } else {
        while (this.Next_Point_Dex < NumPoints) {
          Next_Point = this.MyPhrase.CPoints.get(this.Next_Point_Dex);
          if (EndTime < Next_Point.RealTime) {// repeat until control point time overtakes EndTime
            break;
          }
          this.Prev_Point_Dex = this.Next_Point_Dex - 1;
          Prev_Point = this.MyPhrase.CPoints.get(this.Prev_Point_Dex);
          Render_Segment_Integral(Prev_Point, Next_Point, wave);
          this.Next_Point_Dex++;
        }
      }
      if (Next_Point.RealTime == EndTime) {
        boolean nop = true;
      }

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
      this.Render_Sample_Count = 0;
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
      double SubTimeLocal = Integral(OctaveRate, TimeAlong);
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
        CurrentLoudness = pnt0.Loudness + (TimeAlong * LoudnessRate);
        CurrentFrequency = BaseFreq * CurrentFrequencyFactorAbsolute;// do we really need to include the base frequency in the summing?
        Amplitude = Math.sin(SubTimeIterate);
        wave.Set(this.Render_Sample_Count, Amplitude * CurrentLoudness);
        SubTimeIterate += (CurrentFrequency * Globals.TwoPi) / SRate;
        this.Render_Sample_Count++;
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
      int NumSamples = (int) Math.ceil(TimeRange * SRate);
      // NumSamples = this.Origin_Sample_Count - this.Origin_Sample_Count_Prev;// working on fix for alignment error
      double TimeAlong;
      double CurrentLoudness;
      double Amplitude;
      for (int scnt = 0; scnt < NumSamples; scnt++) {
        TimeAlong = scnt * SampleDuration;
        CurrentLoudness = pnt0.Loudness + (TimeAlong * LoudnessRate);
        SubTimeLocal = Integral(OctaveRate, TimeAlong);
        SubTimeAbsolute = (pnt0.SubTime + (FrequencyFactorStart * SubTimeLocal)) * FrequencyFactorInherited;
        Amplitude = Math.sin(SubTimeAbsolute * BaseFreq * Globals.TwoPi);
        wave.Set(this.Render_Sample_Count, Amplitude * CurrentLoudness);
        this.Render_Sample_Count++;
      }
    }
  }
}
