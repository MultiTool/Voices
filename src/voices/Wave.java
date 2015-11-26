/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import java.util.Arrays;

/**
 *
 * @author MultiTool
 */
public class Wave {
  public int NumSamples;
  private int Current_Index;
  public int StartDex = 0;
  public int SampleRate;
  public double StartTime = 0;
  public double EndTime = 0;// for debugging
  private double[] wave;
  public static boolean Debugging = false;
  /* ********************************************************************************* */
  public Wave() {
    this.NumSamples = 0;
    wave = new double[this.NumSamples];
    this.StartTime = 0.0;
    this.StartDex = 0;
    this.Current_Index = 0;
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
  public void Init(int SizeInit) {
    this.NumSamples = SizeInit;
    wave = new double[SizeInit];
    this.StartTime = 0.0;
    this.StartDex = 0;
    this.Current_Index = 0;
    if (Debugging && this.wave.length > 0) {
      this.wave[0] = 123.0;
      this.wave[this.wave.length - 1] = 999.0;
    }
  }
  /* ********************************************************************************* */
  public void Init(double StartTime0, double EndTime0, int SampleRate0) {
    this.StartTime = StartTime0;// wave start time is the offset of wave[0] from time 0. 
    this.EndTime = EndTime0;
    this.SampleRate = SampleRate0;
    double TimeSpan = EndTime0 - StartTime0;
    int nsamps = (int) Math.ceil(TimeSpan * SampleRate0);
    this.StartDex = (int) (this.StartTime * SampleRate0);// StartDex is the number of empty samples from Time=0 to wave[0]
    this.NumSamples = nsamps;
    wave = new double[nsamps + 1];// plus 1 because converting from double to int truncates. 
    if (Debugging && this.wave.length > 0) {
      this.wave[0] = 123.0;
      this.wave[this.wave.length - 1] = 999.0;
      this.Fill(777.0);
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
    for (int cnt = 0; cnt < this.NumSamples; cnt++) {
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
  public void Diff(Wave other, Wave result) {
    result.Init(this.NumSamples);
    for (int cnt = 0; cnt < this.NumSamples; cnt++) {
      result.wave[cnt] = this.wave[cnt] - other.wave[cnt];
    }
  }
  /* ********************************************************************************* */
  public void Append(Wave other) {
    int StartPlace = other.StartDex;
    int nextsize = StartPlace + other.NumSamples;
    this.wave = Arrays.copyOf(this.wave, nextsize);
    System.arraycopy(other.wave, 0, this.wave, StartPlace, other.NumSamples);
    if (Debugging && this.wave.length > 0) {
      this.wave[0] = 123.0;
      this.wave[this.wave.length - 1] = 999.0;
    }
    this.NumSamples = nextsize;
  }
  /* ********************************************************************************* */
  public void Append_Crude(Wave other) {
    int StartPlace = this.NumSamples;
    int nextsize = StartPlace + other.NumSamples;
    this.wave = Arrays.copyOf(this.wave, nextsize);
    System.arraycopy(other.wave, 0, this.wave, StartPlace, other.NumSamples);
    if (Debugging && this.wave.length > 0) {
      this.wave[0] = 123.0;
      this.wave[this.wave.length - 1] = 999.0;
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
    if (this.wave.length <= dex) {
      boolean nop = true;
    } else {
      this.wave[dex] = value;
    }
  }
}
