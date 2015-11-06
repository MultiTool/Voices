/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

/**
 *
 * @author MultiTool
 */
public class Wave {
  public int numsamples, Current_Index;
  private double[] wave;
  public void Init(int SizeInit) {
    this.numsamples = SizeInit;
    wave = new double[SizeInit];
    this.Current_Index = 0;
  }
  public void Diff(Wave other, Wave result) {
    result.Init(this.numsamples);
    for (int cnt = 0; cnt < this.numsamples; cnt++) {
      result.wave[cnt] = this.wave[cnt] - other.wave[cnt];
    }
  }
  public double Get(int dex) {
    return this.wave[dex];
  }
  public void Set(double value) {
    this.wave[Current_Index] = value;
    Current_Index++;
  }
}
