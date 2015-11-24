/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import java.util.Random;

/**
 *
 * @author MultiTool
 */
public class Globals {
  public static int SampleRate = 44100;
  public static int SampleRateTest = 100;
  public static double BaseFreqC0 = 16.3516;// hz
  public static double TwoPi = Math.PI * 2.0;// hz
  public static double Fudge = 0.00000000001;
  public static Random RandomGenerator = new Random();
}
