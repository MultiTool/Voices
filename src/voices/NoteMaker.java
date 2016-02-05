/*
 *
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

/**
 *
 * @author MultiTool
 */
public class NoteMaker {
  public static int NumNotes = 12;
  public static double SemitoneFraction = (1.0 / (double) NumNotes);
  public static double OffsetTime = 0.03;
  /* ********************************************************************************* */
  public double Cn, Cs, Dn, Ds, En, Fn, Fs, Gn, Gs, An, As, Bn;// naturals and sharps
  public double[] NoteRatios;
  /* ********************************************************************************* */
  public static class Note {
    public int Dex;
    public double Octave;
    public String Name;
  }
  /* ********************************************************************************* */
  public NoteMaker() {
    this.Init();
  }
  /* ********************************************************************************* */
  public static void Synth_Vibe_Spectrum(Wave wave, int NumSamples, int SampleRate) {
    int MegaSamples = NumSamples * 300;
    wave.Init(MegaSamples, SampleRate);
    int NumFreqs = 50;
    double FractAlong = 0;
    double val, sum, FreqDecayRate = 0.999999, DecayDecay = 0.999999, FreqDecay = 1.0;
    for (int SampCnt = 0; SampCnt < MegaSamples; SampCnt++) {
      FractAlong = ((double) (SampCnt % NumSamples)) / (double) NumSamples;
      sum = 0;
      FreqDecay = 1.0;
      for (int FreqCnt = 1; FreqCnt <= NumFreqs; FreqCnt++) {
        val = Math.sin(FractAlong * Globals.TwoPi * (double) FreqCnt);
        val *= FreqDecay;
        sum += val;
        FreqDecay *= FreqDecayRate;
      }
      FreqDecayRate *= DecayDecay;
      wave.Set(SampCnt, sum);
    }
  }
  /* ********************************************************************************* */
  public static void Synth_Pluck(Wave wave, double BaseFreq, double Duration, int SampleRate) {
    int SamplesPerCycle = (int) ((1.0 / BaseFreq) * SampleRate);
    int MegaSamples = (int) (Duration * (double) SampleRate);
    wave.Init(MegaSamples, SampleRate);
    Wave pattern = new Wave();
    pattern.Init(SamplesPerCycle, SampleRate);

    double val, avg = 0.0;
    double subtime = 0.0, timerate = 1, FractAlong = 0;
    for (int SampCnt = 0; SampCnt < SamplesPerCycle; SampCnt++) {
      FractAlong = ((double) SampCnt) / ((double) SamplesPerCycle);
      subtime = FractAlong * timerate;
      val = Math.sin(subtime * Globals.TwoPi);// chirp
      //val = Globals.RandomGenerator.nextDouble() * 2.0 - 1.0;// white noise
      timerate += 0.1;
      pattern.Set(SampCnt, val);
      avg += val;
    }
    avg /= SamplesPerCycle;
    for (int SampCnt = 0; SampCnt < SamplesPerCycle; SampCnt++) {// make average be 0
      val = pattern.Get(SampCnt);
      pattern.Set(SampCnt, val - avg);
    }

    int DexNow;
    double ValAvg;
    double ValPrev = 0;
    for (int SampCnt = 0; SampCnt < MegaSamples; SampCnt++) {
      DexNow = SampCnt % SamplesPerCycle;
      val = pattern.Get(DexNow);
      wave.Set(SampCnt, val);
      ValAvg = (ValPrev + val) / 2.0;
      pattern.Set(DexNow, ValAvg);
      ValPrev = val;
    }
  }
  /* ********************************************************************************* */
  public static void Synth_Pluck_Flywheel(Wave wave, double BaseFreq, double Duration, int SampleRate) {
    int SamplesPerCycle = (int) ((1.0 / BaseFreq) * SampleRate);
    int MegaSamples = (int) (Duration * (double) SampleRate);
    wave.Init(MegaSamples, SampleRate);
    Wave pattern = new Wave();
    pattern.Init(SamplesPerCycle, SampleRate);
    double val;
    double avg = 0.0;
    double subtime = 0.0, timerate = 1, FractAlong = 0;
    for (int SampCnt = 0; SampCnt < SamplesPerCycle; SampCnt++) {
      FractAlong = ((double) SampCnt) / ((double) SamplesPerCycle);
      subtime = FractAlong * timerate;
      val = Math.sin(subtime * Globals.TwoPi);// chirp
      //val = Globals.RandomGenerator.nextDouble() * 2.0 - 1.0;// white noise
      timerate += 0.1;
      pattern.Set(SampCnt, val);
      avg += val;
    }
    avg /= SamplesPerCycle;
    for (int SampCnt = 0; SampCnt < SamplesPerCycle; SampCnt++) {// make average be 0
      val = pattern.Get(SampCnt);
      pattern.Set(SampCnt, val - avg);
    }

    double Flywheel = 0.0;
    double Inertia = 0.94, InertiaPersist = 0.9999;
    Inertia = 0.8;
    Inertia = 0.1;// lower value is more local -> only limits higher frequencies. bigger value wipes more frequencies. 
    Inertia = 0.0;
    double Numerator = 0.0, Denominator = 0.0;
    int WindowSize = 2;
    double ValPrev = 0, ValNext = 0, WindowAvg;
    double WindowSum = 0;
    for (int SampCnt = 0; SampCnt < MegaSamples; SampCnt++) {
      val = pattern.Get(SampCnt % SamplesPerCycle);
      ValPrev = pattern.Get(SampCnt % SamplesPerCycle);
      ValNext = pattern.Get((SampCnt + WindowSize) % SamplesPerCycle);
      WindowSum -= ValPrev;
      WindowSum += ValNext;
      WindowAvg = WindowSum / (double) WindowSize;
      // Denominator = (Denominator + 1.0) * Inertia;
      Denominator = (Denominator * Inertia) + Inertia;
      Numerator = (Numerator * Inertia) + (val * Inertia);
      val = (Numerator) / (Denominator);
      // val = WindowAvg;
      wave.Set(SampCnt, val);
      //Inertia *= InertiaPersist;
//      Flywheel = (Flywheel * Inertia) + (val * (1.0 - Inertia));
//      wave.Set(SampCnt, Flywheel);
      Inertia = 1.0 - ((1.0 - Inertia) * InertiaPersist);
      WindowSize = (int) Math.ceil((((double) SampCnt) / (double) MegaSamples) * (double) SamplesPerCycle);
      //WindowSize++;
      boolean nop = true;
    }
  }
  /* ********************************************************************************* */
  public static ISonglet Create_Unbound_Triad_Rythm() {
    OffsetBox obox = null;
    GroupBox CMinor, CMajor, DMajor, DMinor;
    double Delay = 1.5;
    GroupBox gbx;
    NoteMaker nm;
    LoopBox song;

    double offx = NoteMaker.OffsetTime;
    gbx = new GroupBox();
    nm = new NoteMaker();
    song = new LoopBox();
    CMajor = nm.MakeMajor();// C major
    gbx.Add_SubSong(CMajor, 0 + offx, 0, 1.0);
    CMinor = nm.MakeMinor();// C minor
    gbx.Add_SubSong(CMinor, Delay * 1 + offx, 0, 1.0);
    DMajor = nm.MakeMajor();// D major
    gbx.Add_SubSong(DMajor, Delay * 2 + offx, 2.0 * NoteMaker.SemitoneFraction, 1.0);
    DMinor = nm.MakeMinor();// D minor
    gbx.Add_SubSong(DMinor, Delay * 3 + offx, 2.0 * NoteMaker.SemitoneFraction, 1.0);

    song.Add_Content(gbx);
    song.Set_Delay(Delay * 4);
    song.Set_Duration(30);

//    obox = song.Spawn_OffsetBox();
//    obox.TimeX += NoteMaker.OffsetTime;
//    obox.OctaveY = (4);
    return song;
  }
  /* ********************************************************************************* */
  public static Voice Create_Bent_Note(double TimeOffset, double Duration, double OctaveOffset, double LoudnessOffset) {
    Voice voice = new Voice();
    double midfrac0 = 0.03, midfrac1 = 0.5;
    voice.Add_Note(TimeOffset + 0, OctaveOffset + 0, LoudnessOffset * 0);
    voice.Add_Note(TimeOffset + Duration * midfrac0, OctaveOffset + 0, LoudnessOffset * 1);
    voice.Add_Note(TimeOffset + Duration * midfrac1, OctaveOffset - 0.07, LoudnessOffset * midfrac1);
    voice.Add_Note(TimeOffset + Duration, OctaveOffset + 0.0, LoudnessOffset * 0.0);
    //voice.Add_Note(TimeOffset + Duration, OctaveOffset + 0.1, LoudnessOffset * 0);
    return voice;
  }
  /* ********************************************************************************* */
  public static Voice Create_Tapered_Note(double TimeOffset, double Duration, double OctaveOffset, double LoudnessOffset, int numsteps) {
    double AttackTime = 0.01;
    Duration -= AttackTime;
    Voice voice;
    if (true) {
      Wave wave = new Wave();
      if (false) {
        NoteMaker.Synth_Vibe_Spectrum(wave, 2699, Globals.SampleRate);//  44100 / 16.3516 = 2696.9837814036546882262286259449
        voice = TestJunkyard.Create_SampleVoice_Stub(wave, 1);// 16.3516);
      } else {
        //NoteMaker.Synth_Pluck(wave, Globals.MiddleC4Freq, 1.0, Globals.SampleRate);
        NoteMaker.Synth_Pluck_Flywheel(wave, Globals.MiddleC4Freq, 1.0, Globals.SampleRate);
        voice = TestJunkyard.Create_SampleVoice_Stub(wave, 1.0 / Globals.BaseFreqC0);// 16.3516);
      }
    } else if (false) {
      voice = new Voice();
    } else {
      voice = TestJunkyard.Create_SampleVoice_Stub(2);
    }
    double midfrac;
    voice.Add_Note(TimeOffset, OctaveOffset, 0);
    for (int cnt = 0; cnt <= numsteps; cnt++) {
      midfrac = ((double) cnt) / (double) numsteps;
      voice.Add_Note(TimeOffset + AttackTime + (Duration * midfrac), OctaveOffset, LoudnessOffset * (1.0 - midfrac));
    }
    return voice;
  }
  /* ********************************************************************************* */
  public static GroupBox Create_Note_Chain(int TotalNotes, double TimeStep) {
    double Loudness = 1.0;
    //double TimeStep = 1.0;
    double Duration = TimeStep;
    double Octave = 0, OctaveOffset = 0;
    int NoteBreaks = 3;
    ISonglet note;
    GroupBox gbx = new GroupBox();
    for (int cnt = 0; cnt < TotalNotes; cnt++) {
      note = Create_Tapered_Note(NoteMaker.OffsetTime, Duration, Octave, Loudness, NoteBreaks);
      gbx.Add_SubSong(note, (TimeStep * (double) cnt) + NoteMaker.OffsetTime, OctaveOffset, Loudness);
    }
    return gbx;
  }
  /* ********************************************************************************* */
  public GroupBox Create_Triad(ISonglet s0, ISonglet s1, ISonglet s2) {
    GroupBox grpbx = new GroupBox();
    for (int cnt = 0; cnt < NumNotes; cnt++) {
      grpbx.Add_SubSong(s0, 0, 0, 1.0);
      grpbx.Add_SubSong(s1, 0, 0, 1.0);
      grpbx.Add_SubSong(s2, 0, 0, 1.0);
    }
    grpbx.Sort_Me();
    return grpbx;
  }
  /* ********************************************************************************* */
  public GroupBox Create_Triad(int NoteDex0, int NoteDex1, int NoteDex2) {
    double Loudness = 1.0;// NoteDex0 is usually the key
    double Duration = 2.0;
    ISonglet note;
    GroupBox gbx = new GroupBox();
    note = Create_Bent_Note(0 + NoteMaker.OffsetTime, Duration, 0, Loudness);
    gbx.Add_SubSong(note, 0 + NoteMaker.OffsetTime, SemitoneFraction * NoteDex0, Loudness);
    if (true) {
      note = Create_Bent_Note(0 + NoteMaker.OffsetTime, Duration, 0, Loudness);
      gbx.Add_SubSong(note, 0 + NoteMaker.OffsetTime, SemitoneFraction * NoteDex1, Loudness);
      note = Create_Bent_Note(0 + NoteMaker.OffsetTime, Duration, 0, Loudness);
      gbx.Add_SubSong(note, 0 + NoteMaker.OffsetTime, SemitoneFraction * NoteDex2, Loudness);
    }
    return gbx;
  }
  /* ********************************************************************************* */
  public OffsetBox Create_Triad_OBox(int KeyOffset0, int KeyOffset1) {// int Key,
    double Loudness = 1.0;
    double Duration = 2.0;
    ISonglet note;
    OffsetBox obox;
    GroupBox grpbx = new GroupBox();

    note = Create_Bent_Note(0, Duration, 0, Loudness);

    obox = note.Spawn_OffsetBox();
    grpbx.Add_SubSong(obox, 0, 0, Loudness);// key note, needs no offset

    obox = note.Spawn_OffsetBox();
    grpbx.Add_SubSong(obox, 0, SemitoneFraction * KeyOffset0, Loudness);

    obox = note.Spawn_OffsetBox();
    grpbx.Add_SubSong(obox, 0, SemitoneFraction * KeyOffset1, Loudness);

    obox = grpbx.Spawn_OffsetBox();
    //obox.OctaveY = SemitoneFraction * Key;
    return obox;
  }
  /* ********************************************************************************* */
  public GroupBox Create_Seventh(int NoteDex0, int NoteDex1, int NoteDex2, int NoteDex3) {
    double Loudness = 1.0;// NoteDex0 is usually the key
    double Duration = 2.0;
    ISonglet note;
    GroupBox cbx = Create_Triad(NoteDex0, NoteDex1, NoteDex2);
    note = Create_Bent_Note(0, Duration, 0, Loudness);
    cbx.Add_SubSong(note, 0, SemitoneFraction * NoteDex3, Loudness);
    return cbx;
  }
  /* ********************************************************************************* */
  public GroupBox MakeMajor() {
    return Create_Triad(0, (0 + 4), (0 + 7));
  }
  /* ********************************************************************************* */
  public GroupBox MakeMajor(int Key) {
    return Create_Triad(Key, (Key + 4), (Key + 7));
  }
  /* ********************************************************************************* */
  public OffsetBox MakeMajor_OBox(int Key) {
    OffsetBox obx = Create_Triad_OBox(4, 7);
    obx.OctaveY = SemitoneFraction * Key;
    return obx;
  }
  /* ********************************************************************************* */
  public GroupBox MakeMinor() {
    return Create_Triad(0, (0 + 3), (0 + 7));
  }
  /* ********************************************************************************* */
  public GroupBox MakeMinor(int Key) {
    return Create_Triad(Key, (Key + 3), (Key + 7));
  }
  /* ********************************************************************************* */
  public OffsetBox MakeMinor_OBox(int Key) {
    OffsetBox obx = Create_Triad_OBox(3, 7);
    obx.OctaveY = SemitoneFraction * Key;
    return obx;
  }
  /* ********************************************************************************* */
  public GroupBox MakeAugmented(int Key) {
    return Create_Triad(Key, (Key + 4), (Key + 8));
  }
  /* ********************************************************************************* */
  public GroupBox MakeDiminished(int Key) {
    return Create_Triad(Key, (Key + 3), (Key + 6));
  }
  /* ********************************************************************************* */
  public static class Indexes {
    public static int Cn = 0, Cs = 1, Dn = 2, Ds = 3, En = 4, Fn = 5, Fs = 6, Gn = 7, Gs = 8, An = 9, As = 10, Bn = 11;// naturals and sharps' indexes
  }
  /* ********************************************************************************* */
  public void Init() {
    NoteRatios = new double[NumNotes];
    for (int notecnt = 0; notecnt < NumNotes; notecnt++) {
      NoteRatios[notecnt] = Math.pow(2.0, ((double) notecnt) / ((double) NumNotes));
    }
    this.Cn = NoteRatios[0];
    this.Cs = NoteRatios[1];
    this.Dn = NoteRatios[2];
    this.Ds = NoteRatios[3];
    this.En = NoteRatios[4];
    this.Fn = NoteRatios[5];
    this.Fs = NoteRatios[6];
    this.Gn = NoteRatios[7];
    this.Gs = NoteRatios[8];
    this.An = NoteRatios[9];
    this.As = NoteRatios[10];
    this.Bn = NoteRatios[11];
  }
}
