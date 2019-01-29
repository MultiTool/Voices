package voices;

import java.awt.Color;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 *
 * @author MultiTool
 */
public class Voice implements ISonglet.IContainer {
  // collection of control points, each one having a pitch and a volume. rendering morphs from one cp to another. 
  public ArrayList<VoicePoint> CPoints = new ArrayList<VoicePoint>();
  public static String CPointsName = "ControlPoints", BaseFreqName = "BaseFreq";// for serialization
  public AudProject MyProject;
  public int SampleRate;
  private double MaxAmplitude;
  private int FreshnessTimeStamp;
  protected double BaseFreq = Globals.BaseFreqC0;
  double ReverbDelay = 0.125 / 4.0;// delay in seconds
  private int RefCount = 0;
  Voice_Editor MyEditor = null;
  // graphics support
  CajaDelimitadora MyBounds = new CajaDelimitadora();
  Color FillColor;
  static double Filler = 0.3;// to diagnose/debug dropouts in output wave
  public static boolean Voice_Iterative = false;
  /* ********************************************************************************* */
  public static class Voice_Singer extends Singer {
    protected Voice MyVoice;
    double Phase, Cycles;// Cycles is the number of cycles we've rotated since the start of this voice. The fractional part is the phase information. 
    double SubTime;// Subjective time.
    double Current_Octave, Current_Frequency;
    int Next_Point_Dex;
    VoicePoint Cursor_Point = new VoicePoint();
    double Prev_Time_Absolute = Double.NEGATIVE_INFINITY;//  Initialize to broken value.
    int Sample_Start = Integer.MIN_VALUE;// Number of samples since time 0 absolute, not local. Initialize to broken value.
    //int SampleRate;// to do: move int GetSampleRate to Singer
    double BaseFreq;
    /* ********************************************************************************* */
    protected Voice_Singer() {
      this.Current_Frequency = 440;
      this.Create_Me();
      this.ParentSinger = null;
    }
    /* ********************************************************************************* */
    @Override public void Start() {
      this.SubTime = 0.0;
      this.Phase = 0.0;
      this.Cycles = 0.0;
      this.Next_Point_Dex = 1;
      this.Sample_Start = 0;
      this.Prev_Time_Absolute = 0;
      if (this.MyVoice.CPoints.size() < 2 || this.InheritedMap.LoudnessFactor == 0.0) {
        this.IsFinished = true;// muted, so don't waste time rendering
      } else {
        this.IsFinished = false;
        VoicePoint pnt = this.MyVoice.CPoints.get(0);
        this.Cursor_Point.CopyFrom(pnt);
        this.Prev_Time_Absolute = this.InheritedMap.UnMapTime(pnt.TimeX);// get start time in global coordinates
        //this.Sample_Start = (int)(Prev_Time_Absolute * (double)this.SampleRate);
      }
    }
    /* ********************************************************************************* */
    @Override public void Skip_To(double EndTime) {// to do: rewrite this to match bug-fixed render_to
      if (this.IsFinished) {
        return;
      }
      VoicePoint Prev_Point = null, Next_Point = null;

      EndTime = this.MyOffsetBox.MapTime(EndTime);// EndTime is now time internal to voice's own coordinate system
      EndTime = this.ClipTime(EndTime);
      double EndTime_Absolute = this.InheritedMap.UnMapTime(EndTime);
      //this.Sample_Start = EndTime_Absolute * (double)this.SampleRate;
      this.Prev_Time_Absolute = EndTime_Absolute;

      Prev_Point = this.Cursor_Point;
      int pdex = this.Next_Point_Dex;
      Next_Point = this.MyVoice.CPoints.get(pdex);
      if (false) {// to do: put treesearch here
        pdex = this.MyVoice.Tree_Search(EndTime, this.Next_Point_Dex, this.MyVoice.CPoints.size());
        Next_Point = this.MyVoice.CPoints.get(pdex);
        // what to do with Prev_Point?
      } else {
        while (Next_Point.TimeX < EndTime) {// this loop ends with Prev_Point before EndTime and Next_Point after it.
          pdex++;
          Prev_Point = Next_Point;
          Next_Point = this.MyVoice.CPoints.get(pdex);
        }
      }
      this.Next_Point_Dex = pdex;

      if (EndTime <= Next_Point.TimeX) {// deal with loose end.
        if (Prev_Point.TimeX <= EndTime) {// EndTime is inside this box. 
          VoicePoint End_Cursor = new VoicePoint();// this section should always be executed, due to time clipping
          End_Cursor.CopyFrom(Prev_Point);
          Interpolate_ControlPoint(Prev_Point, Next_Point, EndTime, End_Cursor);
          this.Cursor_Point.CopyFrom(End_Cursor);
        }
      }
    }
    /* ********************************************************************************* */
    @Override public void Render_To(double EndTime, Wave wave) {      // ready for test
      int SampleRateLocal = this.SampleRate = wave.SampleRate;
      this.Sample_Start = (int) (this.Prev_Time_Absolute * (double) SampleRateLocal);
      System.out.printf("Voice.Render_To EndTime:%f,wave.StartDex:%d,wave.NumSamples:%d %n", EndTime, wave.StartDex, wave.NumSamples);
      if (this.IsFinished) {
        wave.Init_Sample(this.Sample_Start, this.Sample_Start, SampleRateLocal, Filler);// we promise to return a blank wave
        return;
      }
      VoicePoint Prev_Point = null, Next_Point = null;
      EndTime = this.MyOffsetBox.MapTime(EndTime);// EndTime is now time internal to voice's own coordinate system
      EndTime = this.ClipTime(EndTime);
      double EndTime_Absolute = this.InheritedMap.UnMapTime(EndTime);
      int Sample_End = (int) (EndTime_Absolute * (double) SampleRateLocal);
      wave.Init_Sample(this.Sample_Start, Sample_End, SampleRateLocal, Filler);// wave times are in global coordinates because samples are always real time

      Prev_Point = this.Cursor_Point;
      int pdex = this.Next_Point_Dex;
      Next_Point = this.MyVoice.CPoints.get(pdex);
      while (Next_Point.TimeX < EndTime) {// this loop ends with Prev_Point before EndTime and Next_Point after it.
        Render_Segment(Prev_Point, Next_Point, wave);
        pdex++;
        Prev_Point = Next_Point;
        Next_Point = this.MyVoice.CPoints.get(pdex);
      }
      this.Next_Point_Dex = pdex;

      if (EndTime <= Next_Point.TimeX) {// render loose end.
        if (Prev_Point.TimeX <= EndTime) {// EndTime is inside this box. 
          VoicePoint End_Cursor = new VoicePoint();// this section should always be executed, due to time clipping
          End_Cursor.CopyFrom(Prev_Point);
          Interpolate_ControlPoint(Prev_Point, Next_Point, EndTime, End_Cursor);
          Render_Segment(Prev_Point, End_Cursor, wave);
          this.Cursor_Point.CopyFrom(End_Cursor);
        }
      }
      wave.Amplify(this.MyOffsetBox.LoudnessFactor);
      this.Sample_Start = Sample_End;
      this.Prev_Time_Absolute = EndTime_Absolute;// get end time in absolute universal
      if (false) {
        this.Distortion_Effect(wave, 10.0);
//        this.Noise_Effect(wave);
        Reverb_Effect(wave);
      }
    }
    /* ********************************************************************************* */
    public double GetWaveForm(double SubTimeAbsolute) {// not used currently
      return Math.sin(SubTimeAbsolute * this.MyVoice.BaseFreq * Globals.TwoPi);
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
    void Render_Range(int dex0, int dex1, Wave wave) {// written for testing
      VoicePoint pnt0, pnt1;
      for (int pcnt = dex0; pcnt < dex1; pcnt++) {
        pnt0 = this.MyVoice.CPoints.get(pcnt);
        pnt1 = this.MyVoice.CPoints.get(pcnt + 1);
        Render_Segment(pnt0, pnt1, wave);
      }
    }
    /* ********************************************************************************* */
    public static void Interpolate_ControlPoint(VoicePoint pnt0, VoicePoint pnt1, double RealTime, VoicePoint PntMid) {
      double FrequencyFactorStart = pnt0.GetFrequencyFactor();
      double TimeRange = pnt1.TimeX - pnt0.TimeX;
      double TimeAlong = RealTime - pnt0.TimeX;
      double OctaveRange = pnt1.OctaveY - pnt0.OctaveY;
      double OctaveRate = OctaveRange / TimeRange;// octaves per second
      double SubTimeLocal;
      if (OctaveRate == 0.0) {
        SubTimeLocal = TimeAlong;
      } else {
        SubTimeLocal = Voice.Integral(OctaveRate, TimeAlong);
      }
      PntMid.TimeX = RealTime;
      PntMid.SubTime = pnt0.SubTime + (FrequencyFactorStart * SubTimeLocal);

      // not calculus here
      PntMid.OctaveY = pnt0.OctaveY + (TimeAlong * OctaveRate);
      double LoudRange = pnt1.LoudnessFactor - pnt0.LoudnessFactor;
      double LoudAlong = TimeAlong * LoudRange / TimeRange;
      PntMid.LoudnessFactor = pnt0.LoudnessFactor + LoudAlong;
    }
    /* ********************************************************************************* */
    void Render_Segment(VoicePoint pnt0, VoicePoint pnt1, Wave wave) {// Render a straight pitch line between two points (bend/chirp).
      if (Voice_Iterative) {// In the long run, if we use Render_Segment_Iterative at all, it will be to fill in spaces for the integral approach.
        Render_Segment_Iterative(pnt0, pnt1, wave);
      } else {
        Render_Segment_Integral(pnt0, pnt1, wave);
      }
    }
    /* ********************************************************************************* */
    void Render_Segment_Iterative(VoicePoint pnt0, VoicePoint pnt1, Wave wave) {// stateful iterative approach, ready for test
      double SRate = this.SampleRate;
      double Time0 = this.InheritedMap.UnMapTime(pnt0.TimeX);
      double Time1 = this.InheritedMap.UnMapTime(pnt1.TimeX);
      double FrequencyFactorInherited = this.InheritedMap.GetFrequencyFactor();// inherit transposition
      int EndSample = (int) (Time1 * (double) SRate);// absolute

      double SubTime0 = pnt0.SubTime * this.InheritedMap.ScaleX;// tempo rescale
      double TimeRange = pnt1.TimeX - pnt0.TimeX;
      double Octave0 = this.InheritedMap.OctaveY + pnt0.OctaveY;
      double Octave1 = this.InheritedMap.OctaveY + pnt1.OctaveY;

      double OctaveRange = Octave1 - Octave0;
      double OctaveRate = OctaveRange / TimeRange;// octaves per second bend
      OctaveRate += this.Inherited_OctaveRate;// inherit note bend
      double LoudnessRange = pnt1.LoudnessFactor - pnt0.LoudnessFactor;
      double LoudnessRate = LoudnessRange / TimeRange;
      int NumSamples = EndSample - this.Sample_Start;

      double TimeAlong, CurrentLoudness, Amplitude;

      double CurrentOctaveLocal, CurrentFrequency, CurrentFrequencyFactorAbsolute, CurrentFrequencyFactorLocal;
      double SubTimeIterate;

      double FrequencyFactor0 = MonkeyBox.OctaveToFrequencyFactor(Octave0);
      double FrequencyFactor1 = MonkeyBox.OctaveToFrequencyFactor(Octave1);
      double FrequencyRatio = FrequencyFactor1 / FrequencyFactor0;
      double Root = Math.pow(FrequencyRatio, 1.0 / (double) NumSamples);

      SubTimeIterate = pnt0.SubTime * FrequencyFactorInherited * this.InheritedMap.ScaleX;// tempo rescale
      double Snowball = 1.0;// frequency, pow(anything, 0)
      for (int SampleCnt = this.Sample_Start; SampleCnt < EndSample; SampleCnt++) {
        TimeAlong = (SampleCnt / SRate) - Time0;
        CurrentLoudness = pnt0.LoudnessFactor + (TimeAlong * LoudnessRate);
        CurrentFrequencyFactorAbsolute = (FrequencyFactor0 * Snowball);
        Amplitude = this.GetWaveForm(SubTimeIterate);
        wave.Set_Abs(SampleCnt, Amplitude * CurrentLoudness);
        SubTimeIterate += CurrentFrequencyFactorAbsolute / SRate;
        Snowball *= Root;
      }
      this.Sample_Start = EndSample;
    }
    /* ********************************************************************************* */
    public void Render_Segment_Integral(VoicePoint pnt0, VoicePoint pnt1, Wave wave) {// stateless calculus integral approach
      double SRate = this.SampleRate;
      double Time0 = this.InheritedMap.UnMapTime(pnt0.TimeX);
      double Time1 = this.InheritedMap.UnMapTime(pnt1.TimeX);
      double FrequencyFactorInherited = this.InheritedMap.GetFrequencyFactor();// inherit transposition
      int EndSample = (int) (Time1 * (double) SRate);// absolute

      double SubTime0 = pnt0.SubTime * this.InheritedMap.ScaleX;// tempo rescale
      double TimeRange = Time1 - Time0;
      double FrequencyFactorStart = pnt0.GetFrequencyFactor();
      double Octave0 = this.InheritedMap.OctaveY + pnt0.OctaveY, Octave1 = this.InheritedMap.OctaveY + pnt1.OctaveY;

      double OctaveRange = Octave1 - Octave0;
      double OctaveRate = OctaveRange / TimeRange;// octaves per second bend
      OctaveRate += this.Inherited_OctaveRate;// inherit note bend
      double LoudnessRange = pnt1.LoudnessFactor - pnt0.LoudnessFactor;
      double LoudnessRate = LoudnessRange / TimeRange;
      double SubTimeLocal, SubTimeAbsolute;

      double TimeAlong;
      double CurrentLoudness;
      double Amplitude;
      int SampleCnt;
      if (OctaveRate == 0.0) {// no bends, don't waste time on calculus
        for (SampleCnt = this.Sample_Start; SampleCnt < EndSample; SampleCnt++) {
          TimeAlong = (SampleCnt / SRate) - Time0;
          CurrentLoudness = pnt0.LoudnessFactor + (TimeAlong * LoudnessRate);
          SubTimeAbsolute = (SubTime0 + (FrequencyFactorStart * TimeAlong)) * FrequencyFactorInherited;
          Amplitude = this.GetWaveForm(SubTimeAbsolute);
          wave.Set_Abs(SampleCnt, Amplitude * CurrentLoudness);
        }
      } else {
        double PreCalc0 = (SubTime0 * FrequencyFactorInherited);// evaluate this outside the loop to optimize
        double PreCalc1 = (FrequencyFactorStart * FrequencyFactorInherited);
        for (SampleCnt = this.Sample_Start; SampleCnt < EndSample; SampleCnt++) {
          TimeAlong = (SampleCnt / SRate) - Time0;
          CurrentLoudness = pnt0.LoudnessFactor + (TimeAlong * LoudnessRate);
          SubTimeLocal = Voice.Integral(OctaveRate, TimeAlong);
          SubTimeAbsolute = (SubTime0 + (FrequencyFactorStart * SubTimeLocal)) * FrequencyFactorInherited;
          //SubTimeAbsolute = (PreCalc0 + (PreCalc1 * SubTimeLocal));// optimized, hardly notice the difference
          Amplitude = this.GetWaveForm(SubTimeAbsolute);
          wave.Set_Abs(SampleCnt, Amplitude * CurrentLoudness);
        }
      }
      this.Sample_Start = EndSample;
    }
    /* ********************************************************************************* */
    double flywheel = 0.0;
    double drag = 0.9, antidrag = 1.0 - drag;
    /* ********************************************************************************* */
    public void Noise_Effect(Wave wave) {
      int len = wave.NumSamples;
      for (int cnt = 0; cnt < len; cnt++) {
        double val = wave.Get(cnt);
        double rand = (Globals.RandomGenerator.nextDouble()) * antidrag + flywheel;
        flywheel = rand * drag;
        val = rand * val * 0.5 + val * 0.5;
        wave.Set(cnt, val);
      }
    }
    /* ********************************************************************************* */
    public void Distortion_Effect(Wave wave, double gain) {
      double power = 2.0;// sigmoid clipping 
      int len = wave.NumSamples;
      for (int cnt = 0; cnt < len; cnt++) {
        double val = wave.Get(cnt) * gain;
        val = val / Math.pow(1 + Math.abs(Math.pow(val, power)), 1.0 / power);
        wave.Set(cnt, val);
      }
    }
    /* ********************************************************************************* */
    public void Reverb_Effect(Wave wave) {// not finished, need to extend the length of the sample 'wave' object
      double Delay = this.MyVoice.ReverbDelay;// delay in seconds
      double gain = 0.95;// diminish
      int SampleDelay = (int) Math.round(Delay * (double) wave.SampleRate);
      int PrevDex = 0;
      double PrevVal, NowVal;
      int len = wave.NumSamples;
      for (int cnt = SampleDelay; cnt < len; cnt++) {
        PrevVal = wave.Get(PrevDex);
        NowVal = wave.Get(cnt);
        NowVal = NowVal + (PrevVal * gain);
        wave.Set(cnt, NowVal);
        PrevDex++;
      }
    }
    /* ********************************************************************************* */
    @Override public boolean Create_Me() {// IDeletable
      return true;
    }
    @Override public void Delete_Me() {// IDeletable
      super.Delete_Me();
      this.Cursor_Point.Delete_Me();
      this.Cursor_Point = null;
      this.MyVoice = null;// wreck everything so we crash if we try to use a dead object
      this.Phase = this.Cycles = this.SubTime = Double.NEGATIVE_INFINITY;
      this.Current_Octave = this.Current_Frequency = Double.NEGATIVE_INFINITY;
      this.BaseFreq = Double.NEGATIVE_INFINITY;
      this.Next_Point_Dex = Integer.MIN_VALUE;
      this.Sample_Start = Integer.MIN_VALUE;
    }
  }
  /* ********************************************************************************* */
  public static class Voice_OffsetBox extends OffsetBox {// location box to transpose in pitch, move in time, etc. 
    public Voice VoiceContent;
    public static String ObjectTypeName = "Voice_OffsetBox";// for serialization
    /* ********************************************************************************* */
    public Voice_OffsetBox() {
      super();
      this.Create_Me();
      this.Clear();
    }
    /* ********************************************************************************* */
    @Override public Voice GetContent() {
      return VoiceContent;
    }
    /* ********************************************************************************* */
    public void Attach_Songlet(Voice songlet) {// for serialization
      this.VoiceContent = songlet;
      songlet.Ref_Songlet();
    }
    /* ********************************************************************************* */
    @Override public Voice_Singer Spawn_Singer() {// for render time.  always always always override this
      Voice_Singer Singer = this.VoiceContent.Spawn_Singer();
      Singer.MyOffsetBox = this;
      Singer.SampleRate = this.VoiceContent.MyProject.SampleRate;
      return Singer;
    }
    /* ********************************************************************************* */
    @Override public Voice_OffsetBox Clone_Me() {// always this thusly
      Voice_OffsetBox child = new Voice_OffsetBox();// clone
      child.Copy_From(this);
      child.VoiceContent = this.VoiceContent;// iffy 
      return child;
    }
    /* ********************************************************************************* */
    @Override public Voice_OffsetBox Deep_Clone_Me(ITextable.CollisionLibrary HitTable) {// ICloneable
      Voice_OffsetBox child = this.Clone_Me();
      child.Attach_Songlet(this.VoiceContent.Deep_Clone_Me(HitTable));
      return child;
    }
    /* ********************************************************************************* */
    @Override public void BreakFromHerd(ITextable.CollisionLibrary HitTable) {// for compose time. detach from my songlet and attach to an identical but unlinked songlet
      Voice clone = this.VoiceContent.Deep_Clone_Me(HitTable);
      if (this.VoiceContent.UnRef_Songlet() <= 0) {
        this.VoiceContent.Delete_Me();
        this.VoiceContent = null;
      }
      this.Attach_Songlet(clone);
    }
    /* ********************************************************************************* */
    @Override public boolean Create_Me() {// IDeletable
      return true;
    }
    @Override public void Delete_Me() {// IDeletable
      super.Delete_Me();
      if (this.VoiceContent != null) {
        if (this.VoiceContent.UnRef_Songlet() <= 0) {
          this.VoiceContent.Delete_Me();
          this.VoiceContent = null;
        }
      }
    }
    /* ********************************************************************************* */
    @Override public JsonParse.HashNode Export(CollisionLibrary HitTable) {// ITextable
      JsonParse.HashNode SelfPackage = super.Export(HitTable);// ready for test?
      SelfPackage.AddSubPhrase(Globals.ObjectTypeName, IFactory.Utils.PackField(ObjectTypeName));
      return SelfPackage;
    }
    @Override public void ShallowLoad(JsonParse.HashNode phrase) {// ITextable
      super.ShallowLoad(phrase);
    }
    @Override public void Consume(JsonParse.HashNode phrase, CollisionLibrary ExistingInstances) {// ITextable - Fill in all the values of an already-created object, including deep pointers.
    }
    @Override public ISonglet Spawn_And_Attach_Songlet() {// reverse birth, use ONLY for deserialization
      Voice songlet = new Voice();
      this.Attach_Songlet(songlet);
      return songlet;
    }
    /* ********************************************************************************* */
    public static class Factory implements IFactory {// for serialization
      @Override public Voice_OffsetBox Create(JsonParse.HashNode phrase, CollisionLibrary ExistingInstances) {// under construction, this does not do anything yet
        Voice_OffsetBox obox = new Voice_OffsetBox();
        obox.Consume(phrase, ExistingInstances);
        return obox;
      }
    }
  }
  /* ********************************************************************************* */
  public Voice() {
    this.MaxAmplitude = 1.0;
    RefCount = 0;
    this.FillColor = Globals.ToColorWheel(Globals.RandomGenerator.nextDouble());
    FreshnessTimeStamp = 0;
  }
  /* ********************************************************************************* */
  public void Attach_Editor(Voice_Editor editor) {// If a GUI forms editor has been designed, attach it using this.
    this.MyEditor = editor;
    editor.MyVoice = this;
  }
  /* ********************************************************************************* */
  public void Add_Note(VoicePoint pnt) {
    pnt.RefParent(this);
    this.CPoints.add(pnt);
  }
  /* ********************************************************************************* */
  public VoicePoint Add_Note(double RealTime, double Octave, double Loudness) {
    VoicePoint pnt = new VoicePoint();
    pnt.TimeX = RealTime;
    pnt.OctaveY = Octave;
    pnt.SubTime = 0.0;
    pnt.LoudnessFactor = Loudness;
    this.Add_Note(pnt);
    return pnt;
  }
  /* ********************************************************************************* */
  public void Remove_Note(VoicePoint pnt) {
    this.CPoints.remove(pnt);
  }
  /* ************************************************************************************************************************ */
  public int Tree_Search(double Time, int minloc, int maxloc) {// finds place where time would be inserted or replaced
    int medloc;
    while (minloc < maxloc) {
      medloc = (minloc + maxloc) >> 1; // >>1 is same as div 2, only faster.
      if (Time <= this.CPoints.get(medloc).TimeX) {
        maxloc = medloc;
      } else {
        minloc = medloc + 1;/* has to go through here to be found. */
      }
    }
    return minloc;
  }
  /* ********************************************************************************* */
  @Override public double Get_Duration() {
    int len = this.CPoints.size();
    if (len <= 0) {
      return 0;
    }
    VoicePoint Final_Point = this.CPoints.get(len - 1);
    return Final_Point.TimeX + this.ReverbDelay;
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
    if (this.FreshnessTimeStamp != metrics.FreshnessTimeStamp) {// don't hit the same songlet twice on one update
      this.Set_Project(metrics.MyProject);
      this.Sort_Me();
      this.Recalc_Line_SubTime();
      this.Update_Max_Amplitude();
      this.FreshnessTimeStamp = metrics.FreshnessTimeStamp;
    }
    metrics.MaxDuration = this.Get_Duration();
  }
  /* ********************************************************************************* */
  @Override public void Refresh_Me_From_Beneath(IDrawable.IMoveable mbox) {
  }// IContainer
  /* ********************************************************************************* */
  @Override public void Remove_SubNode(MonkeyBox obox) {// Remove a songlet from my list.
    this.CPoints.remove(obox);
  }
  /* ********************************************************************************* */
  public void Sort_Me() {// @Override // sorting by TimeX
    Collections.sort(this.CPoints, new Comparator<VoicePoint>() {
      @Override public int compare(VoicePoint note0, VoicePoint note1) {
        return Double.compare(note0.TimeX, note1.TimeX);
      }
    });
  }
  /* ********************************************************************************* */
//  @Override public AudProject Get_Project() {
//    return this.MyProject;
//  }
  /* ********************************************************************************* */
  @Override public void Set_Project(AudProject project) {// ISonglet
    this.MyProject = project;
//    this.SampleRate = MyProject.SampleRate; // snox, need to enable this and make it work
  }
  /* ********************************************************************************* */
  public void Recalc_Line_SubTime() {
    double SubTimeLocal;// run this function whenever this voice instance is modified, e.g. control points moved, added, or removed. 
    int len = this.CPoints.size();
    if (len < 1) {
      return;
    }
    this.Sort_Me();
    VoicePoint Prev_Point, Next_Point, Dummy_First = new VoicePoint();
    Next_Point = this.CPoints.get(0);
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
      if (OctaveRate == 0.0) {
        SubTimeLocal = TimeRange;// snox is using TimeRange right?
      } else {
        SubTimeLocal = Voice.Integral(OctaveRate, TimeRange);
      }
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
    double Denom = (Math.log(2.0) * OctaveRate);// returns the integral of (2 ^ (TimeAlong * OctaveRate))
    //SubTimeCalc = (Math.pow(2, (TimeAlong * OctaveRate)) / Denom) - (1.0 / Denom);
    SubTimeCalc = ((Math.pow(2, (TimeAlong * OctaveRate)) - 1.0) / Denom);
    return SubTimeCalc;
  }
  /* ********************************************************************************* */
  @Override public void Draw_Me(DrawingContext ParentDC) {// IDrawable
    CajaDelimitadora ChildrenBounds = ParentDC.ClipBounds;// parent is already transformed by my offsetbox
    VoicePoint pnt;
    int len = 0;
    try {
      len = this.CPoints.size();
    } catch (Exception ex) {
      boolean nop = true;
    }
    Path2D.Double pgon = new Path2D.Double();
    double Xloc, Yloc, YlocLow, YlocHigh;

    // work in progress. to do: make a ribbon-shaped polygon whose width is based on point loudness.
    // do we have to go around and then go backward? if pgon were a real array we could fill both sides at once.
    // will probably have to use the x array, y array API. bleh. 
    int StartDex, EndDex, Range, SpineRange;
    StartDex = 0;
    EndDex = len;
    Range = EndDex - StartDex;
    int NumDrawPoints = Range * 2;
    int[] OutlineX = new int[NumDrawPoints];
    int[] OutlineY = new int[NumDrawPoints];
    SpineRange = Range + 1;
    int[] SpineX = new int[SpineRange];
    int[] SpineY = new int[SpineRange];
    Xloc = ParentDC.GlobalOffset.UnMapTime(0);// map to pixels
    Yloc = ParentDC.GlobalOffset.UnMapPitch(0);// map to pixels
    SpineX[0] = (int) Xloc;
    SpineY[0] = (int) Yloc;
    double LoudnessHgt;
    int CntUp = Range, CntDown = Range - 1, CntSpine = 1;
//    pnt = this.CPoints.get(StartDex);
//    double LoudnessHgt = pnt.LoudnessFactor * pnt.OctavesPerLoudness;
//    Xloc = ParentDC.InheritedMap.UnMapTime(pnt.TimeX);
//    Yloc = ParentDC.InheritedMap.UnMapPitch(pnt.OctaveY);
//    YlocLow = ParentDC.InheritedMap.UnMapPitch(pnt.OctaveY - LoudnessHgt);
//    YlocHigh = ParentDC.InheritedMap.UnMapPitch(pnt.OctaveY + LoudnessHgt);
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
    //ParentDC.gr.setColor(Globals.ToAlpha(Color.cyan, 60));// Color.yellow
    ParentDC.gr.setColor(Globals.ToAlpha(this.FillColor, 60));// Color.yellow
    ParentDC.gr.fillPolygon(OutlineX, OutlineY, NumDrawPoints);// voice fill

    ParentDC.gr.setColor(Globals.ToAlpha(Color.darkGray, 100));
    //ParentDC.gr.drawPolygon(OutlineX, OutlineY, NumDrawPoints);// voice outline
    // pgon.closePath(); ParentDC.gr.fill(pgon);

    Color Emerald = new Color(0, 0.5f, 0);// rgb
    ParentDC.gr.setColor(Globals.ToAlpha(Emerald, 200));
//    ParentDC.gr.setColor(Globals.ToAlpha(Color.green, 200));

    ParentDC.gr.drawPolyline(SpineX, SpineY, SpineRange);// draw spine

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
    if (Scoop.Intersects(MyBounds)) {
      int len = this.CPoints.size();
      VoicePoint pnt;
      for (int pcnt = 0; pcnt < len; pcnt++) {// search my children
        System.out.print("" + pcnt + ", ");
        pnt = this.CPoints.get(pcnt);
        pnt.GoFishing(Scoop);
      }
    }
    System.out.println();
  }
  /* ********************************************************************************* */
  @Override public Voice Clone_Me() {// ICloneable
    Voice child = new Voice();
    child.Copy_From(this);
    return child;
  }
  /* ********************************************************************************* */
  @Override public Voice Deep_Clone_Me(ITextable.CollisionLibrary HitTable) {// ICloneable
    Voice child;
    /*
     the idea is for hit table to be keyed by my actual identity (me), while the value
     is my first and only clone. 
     */
    CollisionItem ci = HitTable.GetItem(this);
    if (ci == null) {
      child = new Voice();
      ci = HitTable.InsertUniqueInstance(this);
      ci.Item = child;
      child.Copy_From(this);
      child.Copy_Children(this, HitTable);
    } else {// pre exists
      child = (Voice) ci.Item;// another cast! 
    }
    return child;
  }
  /* ********************************************************************************* */
  public void Copy_Children(Voice donor, ITextable.CollisionLibrary HitTable) {
    VoicePoint vpnt;
    int len = donor.CPoints.size();
    for (int cnt = 0; cnt < len; cnt++) {
      vpnt = donor.CPoints.get(cnt);
      this.Add_Note(vpnt.Deep_Clone_Me(HitTable));
    }
  }
  /* ********************************************************************************* */
  public void Copy_From(Voice donor) {
    this.BaseFreq = donor.BaseFreq;
    this.MyProject = donor.MyProject;
    this.MaxAmplitude = donor.MaxAmplitude;
    this.FreshnessTimeStamp = 0;
    this.MyBounds.Copy_From(donor.MyBounds); //this.CPoints = new ArrayList<>();
  }
  /* ********************************************************************************* */
  @Override public boolean Create_Me() {// IDeletable
    return true;
  }
  @Override public void Delete_Me() {// IDeletable
    this.BaseFreq = Double.NEGATIVE_INFINITY;
    this.MyProject = null;
    this.MaxAmplitude = Double.NEGATIVE_INFINITY;
    this.FreshnessTimeStamp = Integer.MIN_VALUE;
    if (this.MyBounds != null) {
      this.MyBounds.Delete_Me();
      this.MyBounds = null;
    }
    if (this.CPoints != null) {
      this.Wipe_CPoints();
      this.CPoints = null;
    }
    this.ReverbDelay = Double.NEGATIVE_INFINITY;
    this.RefCount = Integer.MIN_VALUE;
    this.FillColor = null;
  }
  public void Wipe_CPoints() {
    int len = this.CPoints.size();
    for (int cnt = 0; cnt < len; cnt++) {
      this.CPoints.get(cnt).Delete_Me();
    }
    this.CPoints.clear();
  }
  /* ********************************************************************************* */
  @Override public int Ref_Songlet() {// ISonglet Reference Counting: increment ref counter and return new value just for kicks
    return ++this.RefCount;
  }
  @Override public int UnRef_Songlet() {// ISonglet Reference Counting: decrement ref counter and return new value just for kicks
    if (this.RefCount < 0) {
      throw new RuntimeException("Voice: Negative RefCount:" + this.RefCount);
    }
    return --this.RefCount;
  }
  @Override public int GetRefCount() {// ISonglet Reference Counting: get number of references for serialization
    return this.RefCount;
  }
  /* ********************************************************************************* */
  @Override public JsonParse.HashNode Export(CollisionLibrary HitTable) {// ITextable
    JsonParse.HashNode phrase = new JsonParse.HashNode();
    phrase.ChildrenHash = this.SerializeMyContents(HitTable);
    return phrase;
  }
  @Override public void ShallowLoad(JsonParse.HashNode phrase) {// ITextable
    HashMap<String, JsonParse.Node> Fields = phrase.ChildrenHash;
    this.BaseFreq = Double.parseDouble(IFactory.Utils.GetField(Fields, "BaseFreq", Double.toString(Globals.BaseFreqC0)));
    // this.MaxAmplitude = Double.parseDouble(IFactory.Utils.GetField(Fields, "MaxAmplitude", "0.125")); can be calculated
  }
  @Override public void Consume(JsonParse.HashNode phrase, CollisionLibrary ExistingInstances) {// ITextable - Fill in all the values of an already-created object, including deep pointers.
  }
  /* ********************************************************************************* */
  public HashMap<String, JsonParse.Node> SerializeMyContents(CollisionLibrary HitTable) {// sort of the counterpart to ShallowLoad
    HashMap<String, JsonParse.Node> Fields = new HashMap<String, JsonParse.Node>();
    Fields.put("BaseFreq", IFactory.Utils.PackField(this.BaseFreq));
    Fields.put("MaxAmplitude", IFactory.Utils.PackField(this.MaxAmplitude));
    // Fields.put("MyBounds", MyBounds.Export(HitTable)); // can be calculated
    JsonParse.ArrayNode CPointsPhrase = new JsonParse.ArrayNode();// Save my array of control points.
    CPointsPhrase.ChildrenArray = IFactory.Utils.MakeArray(HitTable, this.CPoints);
    Fields.put(Voice.CPointsName, CPointsPhrase);
    return Fields;
  }
  /* ********************************************************************************* */
  public static class Voice_Editor {// Form editor API, should probably be an interface
    public Voice MyVoice = null;// an editor has access to any variables in its songlet, but a songlet must call an editor's update function to propagate changes the other way.
    public void UpdateWhatever() {// override this. voice will force its editor to check for voice changes. we can write other more specific functions later. 
    }
  }
  /* ********************************************************************************* */
  @Override public Voice_OffsetBox Spawn_OffsetBox() {// for compose time
    Voice_OffsetBox vbox = new Voice_OffsetBox();// Spawn an OffsetBox specific to this type of phrase.
    vbox.Attach_Songlet(this);
    return vbox;
  }
  /* ********************************************************************************* */
  @Override public Voice_Singer Spawn_Singer() {// for render time
    // Deliver one of my singers while exposing specific object class. 
    // Handy if my parent's singers know what class I am and want special access to my particular type of singer.
    Voice_Singer singer = new Voice_Singer();// Spawn a singer specific to this type of phrase.
    singer.MyVoice = this;
    singer.Set_Project(this.MyProject);// inherit project
    singer.BaseFreq = this.BaseFreq;
    return singer;
  }
}
