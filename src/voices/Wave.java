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
    this.Current_Index = 0;
  }
  /* ********************************************************************************* */
  public void SumIn(Wave other) {
    int StartPlace = other.StartDex;
    int limit = Math.min(this.numsamples, other.StartDex + other.numsamples);
    int ocnt = 0;
    for (int cnt = StartPlace; cnt < limit; cnt++) {
      this.wave[cnt] += other.wave[ocnt++];
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
}
