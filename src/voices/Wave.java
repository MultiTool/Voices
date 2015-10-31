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
  public long numsamples;
  public double[] wave;
  public void Init(int SizeInit) {
    wave = new double[SizeInit];
  }
}
