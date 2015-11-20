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
  public int numsamples;
  private int Current_Index;
  public int StartDex = 0;
  public double StartTime = 0;
  private double[] wave;
  /* ********************************************************************************* */
  public Wave() {
    this.numsamples = 0;
    wave = new double[this.numsamples];
    this.StartTime = 0.0;
    this.StartDex = 0;
    this.Current_Index = 0;
  }
  /* ********************************************************************************* */
  public void Init(int SizeInit) {
    this.numsamples = SizeInit;
    wave = new double[SizeInit];
    this.StartTime = 0.0;
    this.StartDex = 0;
    this.Current_Index = 0;
  }
  /* ********************************************************************************* */
  public void Init(double StartTime0, double EndTime0, double SampleRate0) {
    this.StartTime = StartTime0;// wave start time is the offset of wave[0] from time 0. 
    double TimeSpan = EndTime0 - StartTime0;
    int nsamps = (int) (TimeSpan * SampleRate0);
    this.StartDex = (int) (this.StartTime * SampleRate0);// StartDex is the number of empty samples from Time=0 to wave[0]
    this.numsamples = nsamps;
    wave = new double[nsamps];
    if (this.wave.length > 0) {
      //this.wave[0] = 123.0;
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
    MeStop = this.StartDex + this.numsamples;
    YouStop = other.StartDex + other.numsamples;
    if (MeStop < YouStop) {
      MeStop = this.numsamples;
      YouStop = (this.StartDex + this.numsamples) - other.StartDex;
    } else {
      MeStop = (other.StartDex + other.numsamples) - this.StartDex;
      YouStop = other.numsamples;
    }
    int ocnt = YouStart;
    for (int cnt = MeStart; cnt < MeStop; cnt++) {
      this.wave[cnt] += other.wave[ocnt++];
    }
  }
  /* ********************************************************************************* */
  public void Amplify(double LoudnessFactor) {
    for (int cnt = 0; cnt < this.numsamples; cnt++) {
      this.wave[cnt] *= LoudnessFactor;
    }
  }
  /* ********************************************************************************* */
  public void Diff(Wave other, Wave result) {
    result.Init(this.numsamples);
    for (int cnt = 0; cnt < this.numsamples; cnt++) {
      result.wave[cnt] = this.wave[cnt] - other.wave[cnt];
    }
  }
  /* ********************************************************************************* */
  public void Append(Wave other) {
    int StartPlace = other.StartDex;
    int nextsize = StartPlace + other.numsamples;
    this.wave = Arrays.copyOf(this.wave, nextsize);
    System.arraycopy(other.wave, 0, this.wave, StartPlace, other.numsamples);
    if (this.wave.length > 0) {
      //this.wave[0] = 123.0;
    }
    this.numsamples = nextsize;
  }
  /* ********************************************************************************* */
  public void Append_Crude(Wave other) {
    int StartPlace = this.numsamples;
    int nextsize = StartPlace + other.numsamples;
    this.wave = Arrays.copyOf(this.wave, nextsize);
    System.arraycopy(other.wave, 0, this.wave, StartPlace, other.numsamples);
    if (this.wave.length > 0) {
      //this.wave[0] = 123.0;
    }
    this.numsamples = nextsize;
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
    this.wave[dex] = value;
  }
}
