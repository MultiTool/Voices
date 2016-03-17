package voices;

import java.util.ArrayList;

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
  public static void Wave_Test() {
    double BaseFreq = Globals.MiddleC4Freq / 4;
    double Duration = 2.0;
    Wave wave0 = new Wave();
    Wave wave1 = new Wave();
    Audio aud = new Audio();
    NoteMaker.Synth_Pluck(wave0, BaseFreq, Duration, Globals.SampleRate);
    //NoteMaker.Generate_StackedSines(wave0, 200, Globals.SampleRate);
    wave0.Normalize();

    int SamplesPerCycle = (int) ((1.0 / BaseFreq) * Globals.SampleRate);
    ArrayList<Wave> results = new ArrayList<Wave>();
    Extract_Periodic_Samples(wave0, 32, SamplesPerCycle, results);

    aud.Start();
    aud.Feed(wave0);
    //aud.Save("Synth_Pluck_WhiteNoise.wav", wave0.GetWave());
    //aud.Save("Synth_Pluck_Chirp_Middle.wav", wave0.GetWave());
    //aud.Save("StackedSines.wav", wave0.GetWave());
    aud.Save("Synth_Pluck_StackedSines.wav", wave0.GetWave());
    aud.Stop();

    NoteMaker.Synth_Pluck_Flywheel(wave1, BaseFreq, Duration, Globals.SampleRate);
    wave1.Normalize();
    aud.Start();
    aud.Feed(wave1);
    aud.Save("Synth_Pluck_Flywheel.wav", wave1.GetWave());
    aud.Stop();

    aud.Delete_Me();
  }
  /* ********************************************************************************* */
  public static void Generate_WhiteNoise(Wave pattern, int SampleSize, int SampleRate) {
    pattern.Init(SampleSize, SampleRate);
    double val;
    for (int SampCnt = 0; SampCnt < SampleSize; SampCnt++) {
      val = Globals.RandomGenerator.nextDouble() * 2.0 - 1.0;// white noise
      pattern.Set(SampCnt, val);
    }
    pattern.Center();
  }
  /* ********************************************************************************* */
  public static void Generate_Chirp(Wave pattern, int SampleSize, int SampleRate) {
    pattern.Init(SampleSize, SampleRate);
    double val;
    int wrapfactor = 1;
    int SampleSizePlus = (SampleSize * wrapfactor) + 1;
    int SampWrapped;
    double subtime = 0.0, timerate = 1.0, FractAlong = 0, FractRemaining = 0;
    for (int SampCnt = 0; SampCnt < SampleSize * wrapfactor; SampCnt++) {
      SampWrapped = SampCnt % SampleSize;
      FractAlong = ((double) SampCnt) / ((double) SampleSizePlus);
      FractRemaining = 1.0 - FractAlong;
      //timerate = 1.0 / FractRemaining;
      subtime = FractAlong * timerate;
      val = Math.sin(subtime * Globals.TwoPi);// chirp
      timerate += 0.125;
      double temp = pattern.Get(SampWrapped);
      pattern.Set(SampWrapped, temp + val);
      //pattern.Set(SampCnt, val);
    }
    pattern.Center();
  }
  /* ********************************************************************************* */
  public static void Generate_HashChirp(Wave pattern, int SampleSize, int SampleRate) {// not finished yet
    pattern.Init(SampleSize, SampleRate);
    double prevamp, amp = Globals.Fudge;
    double CrossX, BaseX = 0, RelativeTime;
    double RealTime = 0, FractAlong;
    double WaveLen, freq = 1.0, newfreq = 1.0;
    double NumCycles;
    /*
     A big problem with this so far is that long wavelengths will win most of the territory.
     we need a way to give short wavelengths more space. could bias the probability, but nah.
     could only switch frequencies if NumCycles * WaveLen is greater than some value. 
     should base at higher frequency than 1 for this, or freq 1 will own the entire span.
     */
    for (int SampCnt = 0; SampCnt < SampleSize; SampCnt++) {
      RealTime = ((double) SampCnt) / (double) SampleSize;// do we want FractAlong or actual time? 
      RelativeTime = RealTime - BaseX;
      prevamp = amp;
      amp = Math.sin(RelativeTime * freq);
      //if (prevamp * amp <= 0) { // crosses 0
      //if ((prevamp * amp <= 0) && (amp - prevamp > 0)) { // crosses 0 and rising
      if ((prevamp <= 0) && (0 <= amp)) { // crosses 0 and rising
        NumCycles = Math.round(RelativeTime * freq);
        WaveLen = 1.0 / freq;
        CrossX = WaveLen * NumCycles + BaseX;
        BaseX = CrossX;
        freq = newfreq;// random range 1.0 to maybe (timespan*samplerate)/8
        amp = Math.sin(RelativeTime * freq);
      }
      pattern.Set(SampCnt, amp);
    }
    pattern.Center();
  }
  /* ********************************************************************************* */
  public static void Generate_StackedSines(Wave pattern, int SampleSize, int SampleRate) {
    pattern.Init(SampleSize, SampleRate);
    double val;
    double FractAlong, subtime, timerate;
    for (int SampCnt = 0; SampCnt < SampleSize; SampCnt++) {
      FractAlong = ((double) SampCnt) / ((double) SampleSize);
      val = 0.0;
      for (timerate = 4.0; timerate < 34; timerate++) {
        subtime = FractAlong * timerate;
        val += Math.sin(subtime * Globals.TwoPi);
      }
      pattern.Set(SampCnt, val);
    }
    pattern.Center();
  }
  /* ********************************************************************************* */
  public static void Morph_Synth(ArrayList<Wave> WaveTable, Wave wave) {
    Wave chunkprev, chunk, results;// none of this works yet, it is unfinished
    chunk = WaveTable.get(0);
    results = new Wave(); // results.Init(ChunkSize, SampleRate);
    double Factor = 1.0;
    for (int cnt = 1; cnt < WaveTable.size(); cnt++) {
      chunkprev = chunk;
      chunk = WaveTable.get(cnt);
      for (int scnt = 0; scnt < 100; scnt++) {
        Factor = ((double) scnt) / (double) 100;
        chunk.MorphToWave(chunkprev, Factor, results);
        wave.Overdub(results);
      }
    }
  }
  /* ********************************************************************************* */
  public static void Extract_Periodic_Samples(Wave wave, int NumChunks, int ChunkSize, ArrayList<Wave> results) {
    double[] wavin = wave.GetWave();// wrong!  needs to be more sparse
    Wave waveout;
    int ChunkSpacing = wave.NumSamples / NumChunks;
    for (int cnt = 0; cnt < wave.NumSamples; cnt += ChunkSpacing) {
      if (cnt + ChunkSize >= wave.NumSamples) {
        break;
      }
      waveout = new Wave();
      waveout.Init(ChunkSize, wave.SampleRate);
      Extract_Sample(wave, cnt, cnt + ChunkSize, waveout);
      results.add(waveout);
    }
  }
  /* ********************************************************************************* */
  public static void Extract_Sample(Wave wave, int StartPlace, int EndPlace, Wave results) {
    double[] wavin = wave.GetWave();
    double[] wavout = results.GetWave();
    int cntout = 0;
    for (int cnt = StartPlace; cnt < EndPlace; cnt++) {
      try {
        results.Set(cntout++, wavin[cnt]);// wavout[cntout++] = wavin[cnt];
      } catch (Exception ex) {
        boolean nop = true;
      }
    }
  }
  /* ********************************************************************************* */
  public static void Synth_Pluck(Wave wave, double BaseFreq, double Duration, int SampleRate) {
    int SamplesPerCycle = (int) ((1.0 / BaseFreq) * SampleRate);
    int MegaSamples = (int) (Duration * (double) SampleRate);
    wave.Init(MegaSamples, SampleRate);
    Wave pattern = new Wave();
    double val, avg = 0.0;
    if (false) {
      NoteMaker.Generate_StackedSines(pattern, SamplesPerCycle, SampleRate);
    } else if (true) {
      Generate_WhiteNoise(pattern, SamplesPerCycle, SampleRate);
    } else {
      Generate_Chirp(pattern, SamplesPerCycle, SampleRate);
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
      val = Globals.RandomGenerator.nextDouble() * 2.0 - 1.0;// white noise
      timerate += 0.1;
      pattern.Set(SampCnt, val);
      avg += val;
    }
    avg /= SamplesPerCycle;
    for (int SampCnt = 0; SampCnt < SamplesPerCycle; SampCnt++) {// make average be 0
      val = pattern.Get(SampCnt);
      pattern.Set(SampCnt, val - avg);
    }
    if (true) {
      if (false) {
        Generate_WhiteNoise(pattern, SamplesPerCycle, SampleRate);
      } else {
        Generate_Chirp(pattern, SamplesPerCycle, SampleRate);
      }
    }
    double Flywheel = 0.0;
    double Inertia = 0.94, InertiaPersist = 0.9999;
    Inertia = 0.8;// http://www.dynamicnotions.net/2014/01/cleaning-noisy-time-series-data-low.html
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
  public static void Create_Block_Voice(Voice voice, double Duration, int numsteps) {
    double midfrac;
    voice.Add_Note(0, 0, 0.0);
    for (int cnt = 0; cnt <= numsteps; cnt++) {
      midfrac = ((double) cnt) / (double) numsteps;
      voice.Add_Note((Duration * midfrac), 0, 1.0);
    }
    //return voice;
  }
  /* ********************************************************************************* */
  public static void Create_Tapered_Voice(Voice voice, double TimeOffset, double Duration, double OctaveOffset, double LoudnessOffset, int numsteps) {
    double AttackTime = 0.01;
    Duration -= AttackTime;
//    if (true) {
//      Wave wave = new Wave();
//      if (false) {
//        NoteMaker.Synth_Vibe_Spectrum(wave, 2699, Globals.SampleRate);//  44100 / 16.3516 = 2696.9837814036546882262286259449
//        voice = TestJunkyard.Create_SampleVoice_Stub(wave, 1);// 16.3516);
//      } else {
//        //NoteMaker.Synth_Pluck(wave, Globals.MiddleC4Freq, 1.0, Globals.SampleRate);
//        NoteMaker.Synth_Pluck_Flywheel(wave, Globals.MiddleC4Freq, 1.0, Globals.SampleRate);
//        voice = TestJunkyard.Create_SampleVoice_Stub(wave, 1.0 / Globals.BaseFreqC0);// 16.3516);
//      }
//    } else if (false) {
//      voice = new Voice();
//    } else {
//      voice = TestJunkyard.Create_SampleVoice_Stub(2);
//    }
    double midfrac;
    voice.Add_Note(TimeOffset, OctaveOffset, 0);
    for (int cnt = 0; cnt <= numsteps; cnt++) {
      midfrac = ((double) cnt) / (double) numsteps;
      voice.Add_Note(TimeOffset + AttackTime + (Duration * midfrac), OctaveOffset, LoudnessOffset * (1.0 - midfrac));
    }
    //return voice;
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
      Voice voz = Generate_Voice(2);
      Create_Tapered_Voice(voz, NoteMaker.OffsetTime, Duration, Octave, Loudness, NoteBreaks);
      gbx.Add_SubSong(voz, (TimeStep * (double) cnt) + NoteMaker.OffsetTime, OctaveOffset, Loudness);
    }
    return gbx;
  }
  /* ********************************************************************************* */
  public static GroupBox Create_Note_Chain(ISonglet Songlet, int TotalNotes, double TimeStep) {
    double Loudness = 1.0;
    double OctaveOffset = 0;
    GroupBox gbx = new GroupBox();
    double OffsetTime = NoteMaker.OffsetTime * 0;
    for (int cnt = 0; cnt < TotalNotes; cnt++) {
      gbx.Add_SubSong(Songlet, (TimeStep * (double) cnt) + OffsetTime, OctaveOffset, Loudness);
    }
    return gbx;
  }
  /* ********************************************************************************* */
  public static GroupBox.Group_OffsetBox Create_Group_Loop(double TimeStep) {
    int NumBeats = 8;
    double Duration = 30;

    Voice voz = new Voice();
    if (false) {
      NoteMaker.Create_Block_Voice(voz, TimeStep, 3);
    } else {
      NoteMaker.Create_Tapered_Voice(voz, NoteMaker.OffsetTime, TimeStep, 0, 1.0, 3);
    }
    GroupBox ChildGbx = NoteMaker.Create_Note_Chain(voz, NumBeats, TimeStep);
    ChildGbx.MyName = "ChildGbx";

    //GroupBox ChildGbx = NoteMaker.Create_Note_Chain(NumBeats, TimeStep);
    GroupBox MainGbx;
    double MetaTimeStep = TimeStep * ((double) NumBeats);
    MainGbx = Create_Group_Loop(ChildGbx, 2, MetaTimeStep);// was 6
    MainGbx.MyName = "MainGbx";
    GroupBox.Group_OffsetBox MainGobx;
    MainGobx = MainGbx.Spawn_OffsetBox();
    return MainGobx;
  }
  /* ********************************************************************************* */
  public static GroupBox Create_Group_Loop(ISonglet Songlet, int Iterations, double TimeStep) {
    OffsetBox ChildObx;
    GroupBox MainGbx = new GroupBox();
    double OffsetTime = NoteMaker.OffsetTime * 0;
    double TimeBase = 0.0;
    int Counter = 0;
    for (Counter = 0; Counter < Iterations; Counter++) {
      TimeBase = ((double) Counter) * TimeStep;
      ChildObx = Songlet.Spawn_OffsetBox();
      ChildObx.TimeX = TimeBase + OffsetTime;
      MainGbx.Add_SubSong(ChildObx);
    }
    return MainGbx;
  }
  /* ********************************************************************************* */
  public static Voice Generate_Voice(int choice) {
    //double AttackTime = 0.01;
    Voice voice = null;
    //Duration -= AttackTime;
    Wave wave = new Wave();
    switch (choice) {
    case 0:
      NoteMaker.Synth_Vibe_Spectrum(wave, 2699, Globals.SampleRate);//  44100 / 16.3516 = 2696.9837814036546882262286259449
      voice = TestJunkyard.Create_SampleVoice_Stub(wave, 1);// 16.3516);
      break;
    case 1:
      //NoteMaker.Synth_Pluck(wave, Globals.MiddleC4Freq, 1.0, Globals.SampleRate);
      NoteMaker.Synth_Pluck_Flywheel(wave, Globals.MiddleC4Freq, 1.0, Globals.SampleRate);
      voice = TestJunkyard.Create_SampleVoice_Stub(wave, 1.0 / Globals.BaseFreqC0);// 16.3516);
      break;
    case 2:
      voice = new Voice();
      break;
    case 3:
      voice = TestJunkyard.Create_SampleVoice_Stub(3);
      break;
    }
    return voice;
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
