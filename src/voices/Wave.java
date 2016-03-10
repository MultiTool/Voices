package voices;

import java.util.Arrays;

/**
 *
 * @author MultiTool
 */
public class Wave implements IDeletable {
  public int NumSamples;
  private int Current_Index;
  public int StartDex = 0;
  public int SampleRate;
  public double StartTime = 0;
  public double EndTime = 0;// for debugging
  private double[] wave;
  public static boolean Debugging = false;
  public static double Debug_Start_Mark = 7;
  public static double Debug_End_Mark = 10;
  public static double Debug_Fill = 4;
  /* ********************************************************************************* */
  public Wave() {
    this.NumSamples = 0;
    wave = new double[this.NumSamples];
    this.StartTime = 0.0;
    this.StartDex = 0;
    this.Current_Index = 0;
    this.Create_Me();
  }
  /* ********************************************************************************* */
  public void Rebase_Time(double TimeBase) {
    double TimeRange = this.EndTime - this.StartTime;
    this.StartTime = TimeBase;// wave start time is the offset of wave[0] from time 0. 
    this.EndTime = this.StartTime + TimeRange;
    this.StartDex = (int) (this.StartTime * this.SampleRate);// StartDex is the number of empty samples from Time=0 to wave[0]
  }
  /* ********************************************************************************* */
  public void Shift_Timebase(double TimeDif) {
    this.StartTime += TimeDif;// wave start time is the offset of wave[0] from time 0. 
    this.EndTime += TimeDif;
    this.StartDex = (int) (this.StartTime * this.SampleRate);// StartDex is the number of empty samples from Time=0 to wave[0]
  }
  /* ********************************************************************************* */
  public void Init(int SizeInit, int SampleRate0) {
    this.NumSamples = SizeInit;
    this.SampleRate = SampleRate0;
    wave = new double[SizeInit];
    this.StartTime = 0.0;
    this.StartDex = 0;
    this.Current_Index = 0;
    if (Debugging && this.wave.length > 0) {
      this.wave[0] = Debug_Start_Mark;
      //this.wave[this.wave.length - 1] = Debug_End_Mark;
    }
  }
  /* ********************************************************************************* */
  public void Init(double StartTime0, double EndTime0, int SampleRate0) {
    this.StartTime = StartTime0;// wave start time is the offset of wave[0] from time 0. 
    this.EndTime = EndTime0;
    this.SampleRate = SampleRate0;
    double TimeSpan = EndTime0 - StartTime0;
    int nsamps = (int) Math.ceil(TimeSpan * SampleRate0);
    if (nsamps < 0) {
      boolean nop = true;
    }
    this.StartDex = (int) (this.StartTime * SampleRate0);// StartDex is the number of empty samples from Time=0 to wave[0]
    this.NumSamples = nsamps;
    wave = new double[nsamps + 1];// plus 1 because converting from double to int truncates. 
    if (Debugging && this.wave.length > 0) {
      //this.Fill(Debug_Fill);
      this.wave[0] = Debug_Start_Mark;
      //this.wave[this.wave.length - 1] = Debug_End_Mark;
    }
    this.Current_Index = 0;
  }
  /* ********************************************************************************* */
  public void Overdub(Wave other) {
    int MeStart, YouStart, MeStop, YouStop;
    if (other.StartDex > this.StartDex) {
      MeStart = other.StartDex - this.StartDex;
      YouStart = 0;
    } else {
      MeStart = 0;
      YouStart = this.StartDex - other.StartDex;
    }
    double TestMeStop, TestYouStop;
    TestMeStop = this.StartDex + this.NumSamples;
    TestYouStop = other.StartDex + other.NumSamples;
    if (TestMeStop < TestYouStop) {
      MeStop = this.NumSamples;
      YouStop = (this.StartDex + this.NumSamples) - other.StartDex;
    } else {
      MeStop = (other.StartDex + other.NumSamples) - this.StartDex;
      YouStop = other.NumSamples;
    }
    int ocnt = YouStart;
    for (int cnt = MeStart; cnt < MeStop; cnt++) {
      this.wave[cnt] += other.wave[ocnt++];
    }
  }
  /* ********************************************************************************* */
  public void Amplify(double LoudnessFactor) {
    for (int cnt = 0; cnt < this.wave.length; cnt++) {
      this.wave[cnt] *= LoudnessFactor;
    }
  }
  /* ********************************************************************************* */
  public void Fill(double Stuffing) {
    int len = this.wave.length;
    for (int cnt = 0; cnt < len; cnt++) {
      this.wave[cnt] = Stuffing;
    }
  }
  /* ********************************************************************************* */
  public void ZeroCheck() { // for debugging
    int len = this.wave.length;
    for (int cnt = 0; cnt < len; cnt++) {
      if (this.wave[cnt] == 0.0) {
        this.wave[cnt] = Debug_Start_Mark;
      }
    }
  }
  /* ********************************************************************************* */
  public void Diff(Wave other, Wave result) {
    result.Init(this.NumSamples, this.SampleRate);
    for (int cnt = 0; cnt < this.NumSamples; cnt++) {
      result.wave[cnt] = this.wave[cnt] - other.wave[cnt];
    }
  }
  /* ********************************************************************************* */
  public double GetMaxAmp() {
    int len = this.wave.length;
    double MaxAmp = 0.0;
    double AbsVal;
    for (int cnt = 0; cnt < len; cnt++) {
      if (MaxAmp < (AbsVal = Math.abs(this.wave[cnt]))) {
        MaxAmp = AbsVal;
      }
    }
    return MaxAmp;
  }
  /* ********************************************************************************* */
  public void Normalize() {
    double MaxAmp = 0.0;
    double AbsVal;
    int len = this.wave.length;
    for (int cnt = 0; cnt < len; cnt++) {
      if (MaxAmp < (AbsVal = Math.abs(this.wave[cnt]))) {
        MaxAmp = AbsVal;
      }
    }
    this.Amplify(1.0 / MaxAmp);
  }
  /* ********************************************************************************* */
  public void Center() {// Center wave vertically on amplitude 0, so wave average is 0.
    double Avg = 0.0, Sum = 0.0;
    int len = this.wave.length;
    for (int cnt = 0; cnt < len; cnt++) {
      Sum += this.wave[cnt];
    }
    Avg = Sum / (double) len;
    for (int cnt = 0; cnt < len; cnt++) {
      this.wave[cnt] -= Avg;
    }
  }
  /* ********************************************************************************* */
  public void Append(Wave other) {
    int StartPlace = other.StartDex;
    int nextsize = StartPlace + other.NumSamples;
    this.wave = Arrays.copyOf(this.wave, nextsize);
    // StartPlace = StartPlace > 0 ? StartPlace - 1 : StartPlace;
    System.arraycopy(other.wave, 0, this.wave, StartPlace, other.NumSamples);
    if (false && Debugging && this.wave.length > 0) {
      this.wave[0] = Debug_Start_Mark;
      this.wave[this.wave.length - 1] = Debug_End_Mark;
    }
    this.NumSamples = nextsize;
  }
  /* ********************************************************************************* */
  public void Append_Crude(Wave other) {
    int StartPlace = this.NumSamples;
    int nextsize = StartPlace + other.NumSamples;
    this.wave = Arrays.copyOf(this.wave, nextsize);
    System.arraycopy(other.wave, 0, this.wave, StartPlace, other.NumSamples);
    if (false && Debugging && this.wave.length > 0) {
      this.wave[0] = Debug_Start_Mark;
      this.wave[this.wave.length - 1] = Debug_End_Mark;
    }
    this.NumSamples = nextsize;
  }
  /* ********************************************************************************* */
  public double Get(int dex) {
    return this.wave[dex];
  }
  public void Set(double value) {
    this.wave[Current_Index] = value;
    Current_Index++;
  }
  public void Set(int dex, double value) {
    if (dex < this.wave.length) {
      this.wave[dex] = value;
    } else {
      boolean nop = true;// range check
    }
  }
  /* ******************************************************************* */
  public double GetResample(double TimeSeconds) { // linear interpolation between points. FlexDex is fractional index is in seconds, not samples
    TimeSeconds *= this.SampleRate;
    int Dex0 = (int) Math.floor(TimeSeconds);
    int Dex1 = Dex0 + 1;
    double amp0 = this.wave[Dex0];
    double amp1 = this.wave[Dex1];
    double FullAmpDelta = amp1 - amp0;
    double Fraction = (TimeSeconds - Dex0);// always in the range of 0<=Fraction<1 
    return amp0 + (Fraction * FullAmpDelta);
  }
  /* ******************************************************************* */
  public double GetResampleLooped(double TimeSeconds) { // linear interpolation between points. FlexDex is fractional index is in seconds, not samples
    double SampleDex = TimeSeconds * this.SampleRate;
    if (SampleDex > this.NumSamples) {
      boolean nop = true;
    }
    double SampleDexFlat = Math.floor(SampleDex);
    int Dex0 = ((int) SampleDexFlat) % this.NumSamples;
    int Dex1 = (Dex0 + 1) % this.NumSamples;
    double amp0 = this.wave[Dex0];
    double amp1 = this.wave[Dex1];
    double FullAmpDelta = amp1 - amp0;
    double Fraction = (SampleDex - SampleDexFlat);// always in the range of 0<=Fraction<1 
    return amp0 + (Fraction * FullAmpDelta);
  }
  /* ******************************************************************* */
  public double GetResample(Wave wave, double FlexDex) {// throw this away
    // linear interpolation between points. FlexDex fractional index is in SAMPLES, not seconds
    int Dex0 = (int) Math.floor(FlexDex);
    int Dex1 = Dex0 + 1;
    double amp0 = wave.Get(Dex0);
    double amp1 = wave.Get(Dex1);
    double FullAmpDelta = amp1 - amp0;

    double Fraction0 = Dex1 - FlexDex;
    double Fraction1 = FlexDex - Dex0;

    double InterpAmp;
    InterpAmp = amp0 + (Fraction1 * FullAmpDelta);
    InterpAmp = (Fraction0 * amp0) + (Fraction1 * amp1);
    return InterpAmp;
  }
  /* ******************************************************************* */
  public void MorphToWave(Wave other, double Factor, Wave results) {
    double val0, val1;
    double InvFactor = 1.0 - Factor;
    int MinSamples = Math.min(this.NumSamples, Math.min(other.NumSamples, results.NumSamples));
    for (int cnt = 0; cnt < MinSamples; cnt++) {
      val0 = this.wave[cnt];
      val1 = other.wave[cnt];
      results.wave[cnt] = (val0 * InvFactor) + (val1 * Factor);
    }
  }
  /* ********************************************************************************* */
  public void WhiteNoise_Fill() {
    double val;
    for (int SampCnt = 0; SampCnt < this.NumSamples; SampCnt++) {
      val = Globals.RandomGenerator.nextDouble() * 2.0 - 1.0;// white noise
      this.wave[SampCnt] = val;
    }
    this.Center();
  }
  /* ********************************************************************************* */
  public double[] GetWave() {// just for testing. remove later
    return this.wave;
  }
  /* ********************************************************************************* */
  @Override public boolean Create_Me() {// IDeletable
    return true;
  }
  @Override public void Delete_Me() {// IDeletable
    this.wave = null;// without GC we would just free this memory
  }
}
