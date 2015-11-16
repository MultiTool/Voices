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
  public void Init(int SizeInit) {
    this.numsamples = SizeInit;
    wave = new double[SizeInit];
    this.Current_Index = 0;
  }
  public void Init(double StartTime0, double EndTime0, double SampleRate0) {
    this.StartTime = StartTime0;// wave start time is the offset of wave[0] from time 0. 
    double TimeSpan = EndTime0 - StartTime0;
    int nsamps = (int) (TimeSpan * SampleRate0);
    this.Init(nsamps);
  }
  public void Diff(Wave other, Wave result) {
    result.Init(this.numsamples);
    for (int cnt = 0; cnt < this.numsamples; cnt++) {
      result.wave[cnt] = this.wave[cnt] - other.wave[cnt];
    }
  }
  public void Append(Wave other) {
    int prevsize = this.numsamples;
    int nextsize = prevsize + other.numsamples;
    // double[] tempwave = new double[nextsize];
    //double[] tempwave = Arrays.copyOf(this.wave, nextsize); 
    this.wave = Arrays.copyOf(this.wave, nextsize);
    System.arraycopy(other.wave, 0, this.wave, prevsize, other.numsamples);
    //System.arraycopy(this.wave, 0, tempwave, 0, prevsize);
    //System.arraycopy(other.wave, prevsize, tempwave, prevsize, nextsize - prevsize);
    //this.Init(nextsize);
    this.numsamples = nextsize;
  }
  public double Get(int dex) {
    return this.wave[dex];
  }
  public void Set(double value) {
    this.wave[Current_Index] = value;
    Current_Index++;
  }
}
