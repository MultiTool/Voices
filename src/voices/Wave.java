package voices;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.Arrays;

/**
 *
 * @author MultiTool
 */
public class Wave implements IDeletable {
  public int NumSamples;
  private int Current_Index;
  public int StartDex = 0;// startdex is offset of 'virtual samples' before our wave array begins
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
    this.StartTime = 0;// defaults
    this.EndTime = StartTime + (((double) this.NumSamples) / (double) this.SampleRate);
    if (Debugging && this.wave.length > 0) {
      this.wave[0] = Debug_Start_Mark;
      //this.wave[this.wave.length - 1] = Debug_End_Mark;
    }
  }
  /* ********************************************************************************* */
  public void Init_Time(double StartTime0, double EndTime0, int SampleRate0) {
    this.StartTime = StartTime0;// wave start time is the offset of wave[0] from time 0. 
    this.EndTime = EndTime0;
    this.SampleRate = SampleRate0;
    double TimeSpan = EndTime0 - StartTime0;
    int nsamps = (int) Math.ceil(TimeSpan * SampleRate0);
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
  void Init_Sample(int SampleStart, int SampleEnd, int SampleRate0, double Filler) {
    this.StartTime = ((double) SampleStart) / (double) SampleRate0;// wave start time is the offset of wave[0] from time 0.
    this.EndTime = ((double) SampleEnd) / (double) SampleRate0;
    this.SampleRate = SampleRate0;
    int nsamps = SampleEnd - SampleStart;

    this.StartDex = SampleStart;// StartDex is the number of empty samples from Time=0 to wave[0]
    this.NumSamples = nsamps;
    this.Resize(nsamps);
    Arrays.fill(this.wave, Filler);
  }
  /* ********************************************************************************* */
  public void Resize(int NextSize) {
    double[] NextWave = new double[NextSize];
    Arrays.fill(NextWave, 0.7);
    int CopyLen = Math.min(NextSize, this.wave.length);
    System.arraycopy(this.wave, 0, NextWave, 0, CopyLen);
    this.wave = NextWave;
    this.NumSamples = this.wave.length;
  }
  /* ********************************************************************************* */
  public void Ingest(double[] Sample, int SampleRate0) {
    int len = Sample.length;
    double Duration = ((double) len) / (double) SampleRate0;
    this.Init_Time(0, Duration, SampleRate0);
    for (int cnt = 0; cnt < len; cnt++) {
      this.wave[cnt] = Sample[cnt];
    }
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
  public void Append2(Wave other) {// simple append for testing, ignores time offsets
    int MySize = this.wave.length;
    int OtherSize = other.wave.length;
    int StartPlace = MySize;
    int nextsize = MySize + OtherSize;
    this.Resize(nextsize);
    System.arraycopy(other.wave, 0, this.wave, StartPlace, OtherSize);
    this.NumSamples = this.wave.length;
  }
  /* ********************************************************************************* */
  public void Append(Wave other) {
    int StartPlace = other.StartDex;
    StartPlace = this.wave.length;
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
    try {
      return this.wave[dex];
    } catch (Exception ex) {
      return 0;
    }
  }
  public void Set(double value) {
    this.wave[Current_Index] = value;
    Current_Index++;
  }
  public void Set(int dex, double value) {
//    if (dex < this.wave.length) {
    this.wave[dex] = value;
//    } else {
//      boolean nop = true;// range check
//    }
  }
  void Set_Abs(int dex, double value) {// set with index based on absolute time == 0 (beginning of whole composition)
    int sz = wave.length;
    dex -= this.StartDex;// to do: replace this approach with something more efficient
    if (dex < 0 || sz - 1 < dex) {
      //printf("Set_Abs out of range! size:%zu, dex:%i\n\n", wave.length, dex);
      //this->wave.insert(this->wave.begin()+dex, value); this->NumSamples = this->wave.size();
    } else {
      this.wave[dex] = value;
    }
  }
  /* ******************************************************************* */
  public double GetDownSampleDecimated(double PrevTimeSeconds, double CtrTimeSeconds, double NextTimeSeconds) {// not ready for test or even fully thought out yet.
    PrevTimeSeconds *= this.SampleRate;// convert to fractional sample index
    CtrTimeSeconds *= this.SampleRate;
    NextTimeSeconds *= this.SampleRate;
    if (PrevTimeSeconds >= this.wave.length || CtrTimeSeconds >= this.wave.length || NextTimeSeconds >= this.wave.length) {
      return 0.0;
    }
    double Range = CtrTimeSeconds - PrevTimeSeconds;
    double amp, dist, weight;
    double Sum = 0.0, SumWgt = 0.0;
    int SampDex = (int) Math.ceil(PrevTimeSeconds);
    while (SampDex < NextTimeSeconds) {
      dist = Math.max(Globals.Fudge, Math.abs(SampDex - CtrTimeSeconds) / Range);
      weight = 1.0 / dist;
      SumWgt += weight;
      amp = this.wave[SampDex];
      Sum += amp * weight;
      SampDex++;
    }
    return Sum / SumWgt;
  }
  /* ******************************************************************* */
  public void DownSampleDecimated(double Ratio, Wave result) {// not ready for test or even fully thought out yet.
    int len = this.NumSamples;
    int cnt = 0;
    result.Init(len, this.SampleRate);
    double amp;
    double PrevTimeSeconds = cnt++ * Ratio, CtrTimeSeconds = cnt++ * Ratio, NextTimeSeconds = cnt++ * Ratio;
    while (cnt < len) {
      amp = GetDownSampleDecimated(PrevTimeSeconds / this.SampleRate, CtrTimeSeconds / this.SampleRate, NextTimeSeconds / this.SampleRate);
      result.Set(amp);
      PrevTimeSeconds = CtrTimeSeconds;
      CtrTimeSeconds = NextTimeSeconds;
      NextTimeSeconds = cnt * Ratio;
      cnt++;
    }
  }
  /* ******************************************************************* */
  public double GetResample(double TimeSeconds) { // linear interpolation between points. TimeSeconds is fractional index is in seconds, not samples
    TimeSeconds *= this.SampleRate;// convert to fractional sample index
    int Dex0 = (int) Math.floor(TimeSeconds);
    int Dex1 = Dex0 + 1;
    if (Dex1 >= this.wave.length) {
      return 0.0;
    }
    double amp0 = this.wave[Dex0];
    double amp1 = this.wave[Dex1];
    double FullAmpDelta = amp1 - amp0;
    double Fraction = (TimeSeconds - Dex0);// always in the range of 0<=Fraction<1 
    return amp0 + (Fraction * FullAmpDelta);
  }
  /* ******************************************************************* */
  public double GetResampleLooped(double TimeSeconds) { // linear interpolation between points. TimeSeconds is fractional index is in seconds, not samples
    double SampleDex = TimeSeconds * this.SampleRate;
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
  /* ********************************************************************************* */
  void Repeat_Pattern_Time(Wave pattern, double Duration) {
    int ResultSizeSamples = (int) (Duration * (double) pattern.SampleRate);
    Repeat_Pattern_Samples(pattern, ResultSizeSamples);
  }
  /* ********************************************************************************* */
  void Repeat_Pattern_Samples(Wave pattern, int ResultSizeSamples) {
    int SamplesPerCycle = pattern.NumSamples;
    this.Init_Sample(0, ResultSizeSamples, pattern.SampleRate, 0.7);
    double val;
    int DexNow;
    for (int SampCnt = 0; SampCnt < ResultSizeSamples; SampCnt++) {
      DexNow = SampCnt % SamplesPerCycle;// this could be more efficient with an array copy
      val = pattern.Get(DexNow);
      this.Set(SampCnt, val);
    }
  }
  /* ******************************************************************* */
  void Fade(double StartFactor, double EndFactor) {
    double val0, DeltaFactor = EndFactor - StartFactor;
    double Factor, FractAlong;
    for (int cnt = 0; cnt < this.NumSamples; cnt++) {
      val0 = this.wave[cnt];
      FractAlong = ((double) cnt) / (double) this.NumSamples;
      Factor = StartFactor + (DeltaFactor * FractAlong);
      this.wave[cnt] = (val0 * Factor);
    }
  }
  /* ******************************************************************* */
  void CrossFade(Wave other, double StartFactor, double EndFactor, Wave results) {
    double val0, val1, DeltaFactor = EndFactor - StartFactor;
    double Factor, CompFactor, FractAlong;
    int MinSamples = Math.min(this.NumSamples, other.NumSamples);
    results.Init(MinSamples, this.SampleRate);// does not respect this.StartTime, time 0 is sample 0.
    for (int cnt = 0; cnt < MinSamples; cnt++) {
      val0 = this.wave[cnt];
      val1 = other.wave[cnt];
      FractAlong = ((double) cnt) / (double) MinSamples;
      Factor = StartFactor + (DeltaFactor * FractAlong);
      CompFactor = 1.0 - Factor;
      results.wave[cnt] = (val0 * Factor) + (val1 * CompFactor);
    }
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
  public void Sawtooth_Fill() {// one cycle of a sawtooth wave for this whole wave
    double val;
    double FractAlong = 0;
    for (int SampCnt = 0; SampCnt < this.NumSamples; SampCnt++) {
      FractAlong = ((double) SampCnt) / (double) this.NumSamples;
      val = 1.0 - (FractAlong * 2.0);
      this.wave[SampCnt] = val;
    }
    this.Center();
  }
  /* ********************************************************************************* */
  public void SquareWave_Fill() {// one cycle of a square wave for this whole wave
    double Amplitude = 0.999;
    int SampleSize = this.NumSamples = this.wave.length;
    int HalfWay = SampleSize / 2;
    for (int SampCnt = 0; SampCnt < HalfWay; SampCnt++) {
      this.Set(SampCnt, Amplitude);
    }
    for (int SampCnt = HalfWay; SampCnt < SampleSize; SampCnt++) {
      this.Set(SampCnt, -Amplitude);
    }
  }
  /* ********************************************************************************* */
  public double[] GetWave() {// just for testing. remove later
    return this.wave;
  }
  /* ********************************************************************************* */
  public static void SaveWaveToCsv(String filename, Wave wave) {// export wave as csv
    double val;
    DecimalFormat df = new DecimalFormat("#.######");
    try {
      FileWriter writer = new FileWriter(filename, true);
      for (int SampCnt = 0; SampCnt < wave.NumSamples; SampCnt++) {
        val = wave.wave[SampCnt];
        writer.write(df.format(val) + ", ");
        //writer.write(String.format("%.06f", val) + ", ");
        if ((SampCnt + 1) % 20 == 0) {
          writer.write("\r\n");   // write new line
        }
      }
      writer.close();
    } catch (Exception ex) {
      //System.exit(1);
    }
  }
  /* ********************************************************************************* */
  @Override public boolean Create_Me() {// IDeletable
    return true;
  }
  @Override public void Delete_Me() {// IDeletable
    this.wave = null;// without GC we would just free this memory
    this.NumSamples = this.Current_Index = this.StartDex = this.SampleRate = Integer.MIN_VALUE;// wreck everything
    this.StartTime = this.EndTime = Double.NEGATIVE_INFINITY;
  }
  /* ********************************************************************************* */
  public Wave Clone_Me() {
    Wave child = new Wave();
    child.Copy_From(this);
    return child;
  }
  public void Copy_From(Wave donor) {
    this.Init_Sample(donor.StartDex, donor.StartDex + donor.NumSamples, donor.SampleRate, 0.7);
    System.arraycopy(donor.wave, 0, this.wave, 0, donor.wave.length);
  }
  public String Export() {
    StringBuilder sb = new StringBuilder();
    int len = this.wave.length;
    sb.append("[");
    if (len > 0) {
      sb.append(this.wave[0]);
      for (int cnt = 1; cnt < len; cnt++) {
        sb.append("," + this.wave[cnt]);
      }
    }
    sb.append("]");
    return sb.toString();
  }
  public void Consume(String text) {
    text = text.replace("[", "");// #hacky 
    text = text.replace("]", "");
    String[] chunks = text.split(",");
    int len = chunks.length;
    this.Init(len, SampleRate);// gotta include this
    for (int cnt = 0; cnt < len; cnt++) {
      this.wave[cnt] = Double.parseDouble(chunks[len]);
    }
  }
}
