package voices;

import java.util.ArrayList;

/**
 * @author MultiTool
 */
public class WTVoice extends Voice {// Wave Table Voice
  /* ********************************************************************************* */
  public ArrayList<Wave> WaveTable = new ArrayList<Wave>();
  /* ********************************************************************************* */
  public static class WT_Singer extends Voice_Singer {
    public WTVoice MyWTVoice = null;
    /* ********************************************************************************* */
    @Override public void Render_Segment_Integral(VoicePoint pnt0, VoicePoint pnt1, Wave wave) {// stateless calculus integral approach
      ArrayList<Wave> WT = MyWTVoice.WaveTable;
      double SRate = this.SampleRate;
      double Time0 = this.InheritedMap.UnMapTime(pnt0.TimeX);
      double Time1 = this.InheritedMap.UnMapTime(pnt1.TimeX);
      double FrequencyFactorInherited = this.InheritedMap.GetFrequencyFactor();// inherit transposition
      int EndSample = (int) (Time1 * (double) SRate);// absolute
      double SampleRange = EndSample - this.Sample_Start;
      wave.Init_Sample(this.Sample_Start, EndSample, this.SampleRate, 0.9);

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
      double Amplitude, amp0, amp1;
      int PatternDex0 = 0, PatternDex1 = WT.size() - 1;// inclusive on both ends for now - both indexes will be hit.
      double PatternRange = PatternDex1 - PatternDex0;
      double FadeSampleRange = SampleRange / PatternRange;

      int PatDex = PatternDex0;
      double PatFractAlong = 0;
      double FadeFractAlong = 0, FadeFractAlongComp = 0;
      int PatSampleStart, PatEndSample;

      Wave PrevPat, NextPat;
      if (true) {
        int SampleDex = this.Sample_Start;
        int FadeCnt;
        PatSampleStart = this.Sample_Start;
        PrevPat = WT.get(PatDex++);
        while (PatDex <= PatternDex1) {// linear sweep across patterns of wave table
          PatFractAlong = (((double) PatDex) - (double) PatternDex0) / (double) PatternRange;
          PatEndSample = this.Sample_Start + (int) (PatFractAlong * (double) SampleRange);
          NextPat = WT.get(PatDex);
          FadeSampleRange = PatEndSample - SampleDex;
          while (SampleDex < PatEndSample) {// to do: clean up this mess. reduce to 1 counter. 
            FadeCnt = SampleDex - PatSampleStart;
            FadeFractAlong = ((double) FadeCnt) / (double) FadeSampleRange;
            FadeFractAlongComp = 1.0 - FadeFractAlong;
            TimeAlong = (SampleDex / SRate) - Time0;
            CurrentLoudness = pnt0.LoudnessFactor + (TimeAlong * LoudnessRate);
            SubTimeLocal = Voice.Integral(OctaveRate, TimeAlong);
            SubTimeAbsolute = (SubTime0 + (FrequencyFactorStart * SubTimeLocal)) * FrequencyFactorInherited;
            amp0 = PrevPat.GetResampleLooped(SubTimeAbsolute);// to do: merge these, redundant calc
            amp1 = NextPat.GetResampleLooped(SubTimeAbsolute);
            Amplitude = (amp0 * FadeFractAlongComp) + (amp1 * FadeFractAlong);// crossfade
            wave.Set_Abs(SampleDex, Amplitude * CurrentLoudness);
            SampleDex++;
          }
          PatSampleStart = PatEndSample;
          PrevPat = NextPat;// drag
          PatDex++;
        }
      } else {// reduced to 1 counter
        int PatCnt = 0;
        int SampleCnt = 0;
        PatFractAlong = 0.0;
        PatSampleStart = this.Sample_Start;
        PrevPat = WT.get(PatternDex0 + PatCnt);
        PatCnt++;
        while (PatCnt <= PatternRange) {// linear sweep across patterns of wave table
          NextPat = WT.get(PatternDex0 + PatCnt);
          SampleCnt = 0;
          while (SampleCnt < FadeSampleRange) {
            FadeFractAlong = ((double) SampleCnt) / (double) FadeSampleRange;
            FadeFractAlongComp = 1.0 - FadeFractAlong;
            TimeAlong = (SampleCnt / SRate);
            CurrentLoudness = pnt0.LoudnessFactor + (TimeAlong * LoudnessRate);
            SubTimeLocal = Voice.Integral(OctaveRate, TimeAlong);
            SubTimeAbsolute = (SubTime0 + (FrequencyFactorStart * SubTimeLocal)) * FrequencyFactorInherited;
            amp0 = PrevPat.GetResampleLooped(SubTimeAbsolute);
            amp1 = NextPat.GetResampleLooped(SubTimeAbsolute);
            Amplitude = (amp0 * FadeFractAlongComp) + (amp1 * FadeFractAlong);// crossfade
            wave.Set_Abs(PatSampleStart + SampleCnt, Amplitude * CurrentLoudness);
            SampleCnt++;
          }
          PatFractAlong = ((double) PatCnt) / (double) PatternRange;
          PatSampleStart = this.Sample_Start + (int) (PatFractAlong * (double) SampleRange);
          PrevPat = NextPat;// drag
          PatCnt++;
        }
      }
      this.Sample_Start = EndSample;
    }
  }
  /* ********************************************************************************* */
  public static class WT_OffsetBox extends Voice_OffsetBox {// location box to transpose in pitch, move in time, etc. 
    public WTVoice WTVoiceContent;
    public static String ObjectTypeName = "WT_OffsetBox";// for serialization
    /* ********************************************************************************* */
    public WT_OffsetBox() {
      super();
      this.Create_Me();
      this.Clear();
    }
    /* ********************************************************************************* */
    @Override public WTVoice GetContent() {
      return WTVoiceContent;
    }
    /* ********************************************************************************* */
    public void Attach_Songlet(WTVoice songlet) {// for serialization
      this.VoiceContent = this.WTVoiceContent = songlet;
      songlet.Ref_Songlet();
    }
    /* ********************************************************************************* */
    @Override public WT_Singer Spawn_Singer() {// for render time.  always always always override this
      WT_Singer Singer = this.WTVoiceContent.Spawn_Singer();
      Singer.MyOffsetBox = this;
      Singer.SampleRate = this.VoiceContent.MyProject.SampleRate;
      return Singer;
    }
  }
  /* ********************************************************************************* */
  public WTVoice() {
    super();
    this.BaseFreq = Globals.MiddleC4Freq;
    Fill_WaveTable();
  }
  /* ********************************************************************************* */
  public void Fill_WaveTable() {// as a test, fill wave table with a pluck simulation
    double Duration = 6.0;
    double WaveLength = 1.0 / this.BaseFreq;
    int SamplesPerCycle = (int) (WaveLength * Globals.SampleRate);

    Wave pattern = new Wave();
    pattern.Init(SamplesPerCycle, Globals.SampleRate);
    pattern.WhiteNoise_Fill();
    //pattern.Fractal_Fill();
    //NoteMaker.Generate_StackedSines(pattern, pattern.NumSamples, Globals.SampleRate);

    int WaveSpanCycles = 10;//10;// number of pattern cycles represented by one wavetable sample.
//    Build_Pluck_WaveTable(pattern, Duration, WaveSpanCycles, this.WaveTable);
    Build_Sines_WaveTable(30, this.WaveTable);
    if (false) {
      Wave Result = new Wave();
      //WaveSpanCycles = 2;//100;// expand or contract duration of sound without changing pitch
      Reconstitute_Pluck(this.WaveTable, Duration, WaveSpanCycles, Result);
      Result.EndTime = Duration;
      Result.SampleRate = Globals.SampleRate;
      Audio aud = new Audio();
      aud.Save("RawPluck.wav", Result.GetWave());
    }
  }
  /* ********************************************************************************* */
  public void WaveTable_Test(Wave Result) {
    double Duration = 6.0;
    double WaveLength = 1.0 / this.BaseFreq;
    int SamplesPerCycle = (int) (WaveLength * Globals.SampleRate);

    Wave pattern = new Wave();
    pattern.Init(SamplesPerCycle, Globals.SampleRate);
    //PluckVoice.Generate_SquareWave(pattern, SamplesPerCycle, Globals.SampleRate);
    PluckVoice.Generate_WhiteNoise(pattern, SamplesPerCycle, Globals.SampleRate);

    int WaveSpanCycles = 10;//10;// number of pattern cycles represented by one wavetable sample.
    Build_Pluck_WaveTable(pattern, Duration, WaveSpanCycles, this.WaveTable);
    //WaveSpanCycles = 2;//100;// expand or contract duration of sound without changing pitch
    Reconstitute_Pluck(this.WaveTable, Duration, WaveSpanCycles, Result);
    Result.EndTime = Duration;
    Result.SampleRate = Globals.SampleRate;
    Audio aud = new Audio();
    aud.Save("RawPluck2.wav", Result.GetWave());
  }
  /* ********************************************************************************* */
  public static void Build_Pluck_WaveTable(Wave pattern, double Duration, int WaveSpanCycles, ArrayList<Wave> ResultWaves) {
    // experiment to create a simple wave table from a pluck
    ResultWaves.clear();
    int SamplesPerCycle = pattern.NumSamples;
    Audio aud = new Audio();
    double PatternLengthSeconds = ((double) pattern.NumSamples) / (double) pattern.SampleRate;
    int PluckLengthSamples = (int) (Duration * (double) pattern.SampleRate);
    int NumCycles = (int) (PluckLengthSamples / SamplesPerCycle);
    int NumWaves = NumCycles / WaveSpanCycles;
    double ValNow, ValAvg;
    int Dex0 = 0, Dex1 = 0, Dex2 = 0;
    double ValPrev = 0, ValNext = 0;
    for (int PatternCnt = 0; PatternCnt < NumCycles; PatternCnt++) {
      if ((PatternCnt % WaveSpanCycles) == 0) {// so every 10 patterns save a sample
        Wave wav = new Wave();
        wav.Copy_From(pattern);// snapshot
        ResultWaves.add(wav);
//        String formatted = String.format("%03d", WavCnt);
//        aud.Save("pattern_" + formatted + ".wav", wav.GetWave());
      }
      double Inertia = 0.75;
      double AntInertia = (1.0 - Inertia) / 2.0;
      for (int SampCnt = 0; SampCnt < SamplesPerCycle; SampCnt++) {// more symmetrical blurring
        Dex0 = Dex1;// dragging 3 indexes
        Dex1 = Dex2;
        Dex2 = SampCnt;
        ValPrev = pattern.Get(Dex0);
        ValNow = pattern.Get(Dex1);
        ValNext = pattern.Get(Dex2);
        ValAvg = (ValPrev * AntInertia) + (ValNow * Inertia) + (ValNext * AntInertia);
        pattern.Set(Dex1, ValAvg);// decaying pattern
      }
    }
    NumWaves = ResultWaves.size();
    for (int cnt = 0; cnt < NumWaves; cnt++) {
      Wave wav = ResultWaves.get(cnt);
      double MaxAmp = wav.GetMaxAmp();
      System.out.println("MaxAmp:" + MaxAmp);
    }
  }
  /* ********************************************************************************* */
  public static void Build_Sines_WaveTable(int NumPatterns, ArrayList<Wave> ResultWaves) {
    // experiment to create a simple wave table from harmonic sines
    ResultWaves.clear();
    int PatternSize = 200;
    for (int PatternCnt = 0; PatternCnt < NumPatterns; PatternCnt++) {
      Wave wav = new Wave();
      wav.Init_Sample(0, PatternSize, Globals.SampleRate, Filler);
      wav.Harmonic_Fill(PatternCnt + 1);
      ResultWaves.add(wav);
    }
  }
  /* ********************************************************************************* */
  public static void Reconstitute_Pluck(ArrayList<Wave> WaveTable, double Duration, int WaveSpanCycles, Wave Result) {
    int PatternSizeSamples = WaveTable.get(0).NumSamples;
    Result.Init(0, Result.SampleRate);
    int WaveSpanSamples = WaveSpanCycles * PatternSizeSamples;
    int NumPatterns = WaveTable.size();
    Wave PatternPrev = WaveTable.get(0);
    Wave PatternNext;
    Wave PrevRun = new Wave(), NextRun = new Wave();
    Wave Span = new Wave();
    for (int pcnt = 1; pcnt < NumPatterns; pcnt++) {
      PatternNext = WaveTable.get(pcnt);
      // Build 2 waves that are repeats of prev and next, then fade them and mix them.
      PrevRun.Repeat_Pattern_Samples(PatternPrev, WaveSpanSamples);
      NextRun.Repeat_Pattern_Samples(PatternNext, WaveSpanSamples);
      PrevRun.Fade(1.0, 0.0);// crossfade
      NextRun.Fade(0.0, 1.0);
      Span.Copy_From(NextRun);
      Span.Overdub(PrevRun);
      Result.Append2(Span);
      PatternPrev = PatternNext;
    }
  }
  /* ********************************************************************************* */
  @Override public WT_OffsetBox Spawn_OffsetBox() {// for compose time
    WT_OffsetBox vbox = new WT_OffsetBox();// Spawn an OffsetBox specific to this type of phrase.
    vbox.Attach_Songlet(this);
    return vbox;
  }
  /* ********************************************************************************* */
  @Override public WT_Singer Spawn_Singer() {// for render time
    WT_Singer singer = new WT_Singer();// Spawn a singer specific to this type of phrase.
    singer.MyVoice = singer.MyWTVoice = this;
    singer.Set_Project(this.MyProject);// inherit project
    singer.BaseFreq = this.BaseFreq;
    return singer;
  }
}

/*
 start with VoicePoint0, VoicePoint1, and also their indexes.
 double WtAlong0 = index0/LastVoicePoint;
 WtAlong0 *= wavetable.size();
 double WtAlong1 = index1/LastVoicePoint;
 WtAlong1 *= wavetable.size();

 WtDex0 = Trunc(WtAlong0); WtFract0 = Fraction(WtAlong0);
 WtDex1 = Trunc(WtAlong1); WtFract1 = Fraction(WtAlong1);


 PatternDex = 
 prevpat = wt[PatternDex0];
 for (int patcnt=PatternDex0+1; patcnt<=PatternDex1; patcnt++){
 nextpat = wt[patcnt];
 for (samplenum = start to finish){
 }
 prevpat = nextpat;// drag
 }

 short linear sweep between 2 patterns:
 first parameters are: wavetable, controlpoint0(with time0), PatternDex0, controlpoint1(with time1), PatternDex1  
 could also derive both PatternDexes from times, by taking PatternDex0 = NumPatterns * (time0/Voice.Duration); 
 map time0 to sample0, time1 to sample1 ?
 NumPats = PatternDex1 - PatternDex0;
 prevpat = wt[PatternDex0];
 for (int patcnt=PatternDex0+1; patcnt<=PatternDex1; patcnt++){
 need to calc time length or sample length of span between patterns here. 
 nextpat = wt[patcnt];
 double Fade0, Fade1;
 for (subtime=start to finish){// from prevpat to nextpat. 
 amp0 = prevpat.GetResampleLooped(SubTimeAbsolute);// to do: merge these, redundant calc
 amp1 = nextpat.GetResampleLooped(SubTimeAbsolute);
 // either put amp0, amp1 in separate waves and crossfade them later, or do it now.
 Fade1 = (SubTimeAbsolute - starttime)/DeltaSubTimeAbsolute;
 Fade0 = 1.0-Fade1;
 amp0 *= Fade0; amp1 *= Fade1;
 amp = amp0+amp1;
 }
 if (false) {
 GenerateRun(prevpat, Run0);// over how much time? 
 GenerateRun(nextpat, Run1);
 Resample(Run0, RRun0);// here we need subtime
 Resample(Run1, RRun1);// here we need subtime
 }
 prevpat = nextpat;// drag
 map time0 to 
 now how to calculate number of samples?
 crossfade(Wave run0, Wave run1, Wave result, int Sample0, int Sample1);//  must specifiy number of samples. but wave sizes are the same so maybe not. 
 overdub result onto master wave. 

 }

 */
