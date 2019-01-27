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
  public static SampleVoice Create_Horn() {
    SampleVoice sv = new SampleVoice();
    sv.Preset_Horn();
    //Wave MySample = Create_Horn_Sample();
    //Globals.BaseFreqC0 / 263.54581673306772913616450532531;// middle C-ish
    //sv.AttachWaveSample(MySample, Globals.BaseFreqC0 / 265.0);//265.663;//264.072;//572.727;//265.663;
    return sv;
  }
  /* ********************************************************************************* */
  public static PluckVoice Create_PluckVoice() {
    PluckVoice pv = new PluckVoice();
    return pv;
  }
  /* ********************************************************************************* */
  public static void Wave_Test() {
    double BaseFreq = Globals.MiddleC4Freq;
    double Duration = 6.0;
    Wave wave0 = new Wave();
    Wave wave1 = new Wave();
    Audio aud = new Audio();
    int SamplesPerCycle = (int) ((1.0 / BaseFreq) * Globals.SampleRate);
    Wave pattern = new Wave();
    pattern.Init(SamplesPerCycle, Globals.SampleRate);
    String FName = "Synth_Pluck_WhiteNoise6.wav";
    //NoteMaker.Synth_Pluck(wave0, BaseFreq, Duration, Globals.SampleRate);
    //NoteMaker.Generate_StackedSines(pattern, SamplesPerCycle, Globals.SampleRate);FName = "Synth_Pluck_StackedSines.wav";
    //pattern.Sawtooth_Fill();FName = "Synth_Pluck_Sawtooth.wav";
    NoteMaker.Generate_WhiteNoise(pattern, SamplesPerCycle, Globals.SampleRate);
    FName = "Synth_Pluck_WhiteNoise6.wav";
    //Generate_StackedSines(pattern, SamplesPerCycle, Globals.SampleRate);
    //NoteMaker.Synth_Pluck_Decay(wave0, pattern, Duration);
    NoteMaker.Repeat_Pattern(wave0, pattern, Duration);
    FName = "Repeat_WhiteNoise.wav";
    
    wave0.Normalize();
    
    if (true) {
      wave0.DownSampleDecimated(Math.PI, wave1);
      wave0.Copy_From(wave1);
    }
    ArrayList<Wave> results = new ArrayList<Wave>();
    Extract_Periodic_Samples(wave0, 32, SamplesPerCycle, results);
    
    aud.Start();
    aud.Feed(wave0);
    aud.Save(FName, wave0.GetWave());
    //aud.Save("Synth_Pluck_Sawtooth.wav", wave0.GetWave());
    //aud.Save("Synth_Pluck_WhiteNoise6.wav", wave0.GetWave());
    //aud.Save("Synth_Pluck_Chirp_Middle.wav", wave0.GetWave());
    //aud.Save("StackedSines.wav", wave0.GetWave());
    //aud.Save("Synth_Pluck_StackedSines.wav", wave0.GetWave());
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
    double FractAlong, subtime, Frequency;// frequency is cycles per whole length of pattern
    int NumHarmonics = 34;
    for (int SampCnt = 0; SampCnt < SampleSize; SampCnt++) {
      FractAlong = ((double) SampCnt) / ((double) SampleSize);
      val = 0.0;
      for (Frequency = 2.0; Frequency < NumHarmonics; Frequency++) {// once for each harmonic
        subtime = FractAlong * Frequency;
        val += Math.sin(subtime * Globals.TwoPi);
      }
      pattern.Set(SampCnt, val);
    }
    pattern.Center();
    pattern.Normalize();
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
  public static void Repeat_Pattern(Wave ResultWave, Wave pattern, double Duration) {
    int SamplesPerCycle = pattern.NumSamples;
    int ResultSize = (int) (Duration * (double) pattern.SampleRate);
    ResultWave.Init(ResultSize, pattern.SampleRate);
    double val;
    int DexNow;
    for (int SampCnt = 0; SampCnt < ResultSize; SampCnt++) {
      DexNow = SampCnt % SamplesPerCycle;
      val = pattern.Get(DexNow);
      ResultWave.Set(SampCnt, val);
    }
  }
  /* ********************************************************************************* */
  public static void Synth_Pluck_Decay(Wave ResultWave, Wave pattern, double Duration) {
    int SamplesPerCycle = pattern.NumSamples;
    double WaveLength = ((double) pattern.NumSamples) / (double) pattern.SampleRate;
    double BaseFreq = 1.0 / WaveLength;
    int ResultSize = (int) (Duration * (double) pattern.SampleRate);
    ResultWave.Init(ResultSize, pattern.SampleRate);
    double val;
    int DexNow;
    double ValAvg;
    double ValPrev = 0;
    for (int SampCnt = 0; SampCnt < ResultSize; SampCnt++) {
      DexNow = SampCnt % SamplesPerCycle;
      val = pattern.Get(DexNow);
      ResultWave.Set(SampCnt, val);
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
    GroupSong CMinor, CMajor, DMajor, DMinor;
    double Delay = 1.5;
    GroupSong gbx;
    NoteMaker nm;
    LoopBox song;
    
    double offx = NoteMaker.OffsetTime;
    gbx = new GroupSong();
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
  }
  /* ********************************************************************************* */
  public static void Create_Tapered_Voice(Voice voice, double TimeOffset, double Duration, double OctaveOffset, double LoudnessOffset, int numsteps) {
    double AttackTime = 0.01;
    Duration -= AttackTime;
    double midfrac;
    voice.Add_Note(TimeOffset, OctaveOffset, 0);
    for (int cnt = 0; cnt <= numsteps; cnt++) {
      midfrac = ((double) cnt) / (double) numsteps;
      voice.Add_Note(TimeOffset + AttackTime + (Duration * midfrac), OctaveOffset, LoudnessOffset * (1.0 - midfrac));
    }
  }
  /* ********************************************************************************* */
  public static void Create_Dummy_Voice(Voice voice) {// silent empty stub just for marking a place
    voice.Add_Note(NoteMaker.OffsetTime, 0.0, 0.0);
    voice.Add_Note(NoteMaker.OffsetTime * 2, 0.0, 0.0);
  }
  /* ********************************************************************************* */
  public static GroupSong.Group_OffsetBox Create_Dummy_Group(double TimeLength) {// silent empty stub just for marking a place
    GroupSong gbx = new GroupSong();
    Voice voice = new Voice();
    gbx.Add_SubSong(voice, TimeLength, 0.0, 1.0);
    voice.Add_Note(NoteMaker.OffsetTime, 0.0, 0.0);
    voice.Add_Note(NoteMaker.OffsetTime * 2, 0.0, 0.0);
    return gbx.Spawn_OffsetBox();
  }
  /* ********************************************************************************* */
  public static GroupSong.Group_OffsetBox Create_Palette() {
    double TimeStep;
    TimeStep = 0.33333 / 2.0;//0.125;

    int NumBeats = 6;
    Voice voz, pvoz;
    voz = new Voice();
    pvoz = NoteMaker.Create_PluckVoice();//PluckVoice
    SampleVoice svoz = NoteMaker.Create_Horn();//SampleVoice

    NoteMaker.Create_Tapered_Voice(voz, NoteMaker.OffsetTime, TimeStep, 0, 1.0, 3);
    NoteMaker.Create_Tapered_Voice(pvoz, NoteMaker.OffsetTime, TimeStep, 0, 1.0, 3);
    NoteMaker.Create_Tapered_Voice(svoz, NoteMaker.OffsetTime, TimeStep, 0, 1.0, 3);
    
    GroupSong ChildGbx = new GroupSong();
    ChildGbx.MyName = "ChildGbx";
    int cnt = 0;
    for (int iter = 0; iter < 2; iter++) {
      ChildGbx.Add_SubSong(voz, (TimeStep * (double) cnt++) + NoteMaker.OffsetTime, 0, 1.0);
      ChildGbx.Add_SubSong(pvoz, (TimeStep * (double) cnt++) + NoteMaker.OffsetTime, 0, 1.0);
      ChildGbx.Add_SubSong(svoz, (TimeStep * (double) cnt++) + NoteMaker.OffsetTime, 0, 1.0);
    }
    
    GroupSong MainGbx;
    double MetaTimeStep = TimeStep * ((double) NumBeats);
    MainGbx = Create_Group_Loop(ChildGbx, 3, MetaTimeStep);// was 6
    MainGbx.MyName = "MainGbx";
    GroupSong.Group_OffsetBox MainGobx;
    MainGobx = MainGbx.Spawn_OffsetBox();
    return MainGobx;
  }
  /* ********************************************************************************* */
  public static GroupSong Create_Group_Loop(ISonglet Songlet, int Iterations, double TimeStep) {
    OffsetBox ChildObx;
    GroupSong MainGbx = new GroupSong();
    double OffsetTime = NoteMaker.OffsetTime * 1;
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
  public GroupSong Create_Triad(ISonglet s0, ISonglet s1, ISonglet s2) {
    GroupSong grpbx = new GroupSong();
    for (int cnt = 0; cnt < NumNotes; cnt++) {
      grpbx.Add_SubSong(s0, 0, 0, 1.0);
      grpbx.Add_SubSong(s1, 0, 0, 1.0);
      grpbx.Add_SubSong(s2, 0, 0, 1.0);
    }
    grpbx.Sort_Me();
    return grpbx;
  }
  /* ********************************************************************************* */
  public GroupSong Create_Triad(int NoteDex0, int NoteDex1, int NoteDex2) {
    double Loudness = 1.0;// NoteDex0 is usually the key
    double Duration = 2.0;
    ISonglet note;
    GroupSong gbx = new GroupSong();
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
    GroupSong grpbx = new GroupSong();
    
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
  public GroupSong Create_Seventh(int NoteDex0, int NoteDex1, int NoteDex2, int NoteDex3) {
    double Loudness = 1.0;// NoteDex0 is usually the key
    double Duration = 2.0;
    ISonglet note;
    GroupSong cbx = Create_Triad(NoteDex0, NoteDex1, NoteDex2);
    note = Create_Bent_Note(0, Duration, 0, Loudness);
    cbx.Add_SubSong(note, 0, SemitoneFraction * NoteDex3, Loudness);
    return cbx;
  }
  /* ********************************************************************************* */
  public GroupSong MakeMajor() {
    return Create_Triad(0, (0 + 4), (0 + 7));
  }
  /* ********************************************************************************* */
  public GroupSong MakeMajor(int Key) {
    return Create_Triad(Key, (Key + 4), (Key + 7));
  }
  /* ********************************************************************************* */
  public OffsetBox MakeMajor_OBox(int Key) {
    OffsetBox obx = Create_Triad_OBox(4, 7);
    obx.OctaveY = SemitoneFraction * Key;
    return obx;
  }
  /* ********************************************************************************* */
  public GroupSong MakeMinor() {
    return Create_Triad(0, (0 + 3), (0 + 7));
  }
  /* ********************************************************************************* */
  public GroupSong MakeMinor(int Key) {
    return Create_Triad(Key, (Key + 3), (Key + 7));
  }
  /* ********************************************************************************* */
  public OffsetBox MakeMinor_OBox(int Key) {
    OffsetBox obx = Create_Triad_OBox(3, 7);
    obx.OctaveY = SemitoneFraction * Key;
    return obx;
  }
  /* ********************************************************************************* */
  public GroupSong MakeAugmented(int Key) {
    return Create_Triad(Key, (Key + 4), (Key + 8));
  }
  /* ********************************************************************************* */
  public GroupSong MakeDiminished(int Key) {
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
