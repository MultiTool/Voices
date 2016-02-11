package voices;

import java.awt.Color;
import java.util.Random;

/**
 *
 * @author MultiTool
// C++ stuff
#define boolean bool 
#define jpublic 
#define jprivate
#define implements :
#define extends :

 */
public class Globals {
  public static int SampleRate = 44100;
  public static int SampleRateTest = 100;
  public static double BaseFreqC0 = 16.3516;// hz
  public static double BaseFreqA0 = 27.5000;// hz
  public static double MiddleC4Freq = 261.626;// hz
  public static double TwoPi = Math.PI * 2.0;// hz
  public static double Fudge = 0.00000000001;
  public static Random RandomGenerator = new Random();
  /* ********************************************************************************* */
  public static Color ToAlpha(Color col, int Alpha) {
    return new Color(col.getRed(), col.getGreen(), col.getBlue(), Alpha);// rgba 
  }
  /* ********************************************************************************* */
  public static Color ToRainbow(double Fraction) {
    if (Fraction < 0.5) {
      Fraction *= 2;
      return new Color((float) (1.0 - Fraction), (float) Fraction, 0);
    } else {
      Fraction = Math.min((Fraction - 0.5) * 2, 1.0);
      return new Color(0, (float) (1.0 - Fraction), (float) Fraction);
    }
  }
}
