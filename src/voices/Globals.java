package voices;

import java.awt.Color;
import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 *
 * @author MultiTool
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
  public static String PtrPrefix = "ptr:";// for serialization
  public static String ObjectTypeName = "ObjectTypeName";// for serialization
  public static HashMap<String, ITextable.IFactory> FactoryLUT = new HashMap<String, ITextable.IFactory>();// for serialization
  /* ********************************************************************************* */
  public static void ShrinkList(List list, int NextSize){
    int PrevSize = list.size();
    if (NextSize<PrevSize){// https://stackoverflow.com/questions/1184636/shrinking-an-arraylist-to-a-new-size
      list.subList(NextSize, PrevSize).clear();
      //list.removeRange(NextSize, PrevSize);// https://docs.oracle.com/javase/6/docs/api/java/util/ArrayList.html#removeRange(int,%20int)
    }else{
      // expand somehow
    }
  }
  /* ********************************************************************************* */
  public static boolean IsTxtPtr(String ContentTxt) {// for serialization
    if (ContentTxt == null) {
      return false;
    }
    int strloc;
    return ((strloc = ContentTxt.indexOf(Globals.PtrPrefix)) >= 0);
  }
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
  /* ********************************************************************************* */
  public static Color ToColorWheel(double Fraction) {
    Fraction = Fraction - Math.floor(Fraction); // remove whole number part if any
    if (Fraction < (1.0 / 3.0)) {// red to green
      Fraction *= 6.0;
      return new Color((float) Math.min(2.0 - Fraction, 1.0), (float) Math.min(Fraction, 1.0), 0);
    } else if (Fraction < (2.0 / 3.0)) {// green to blue
      Fraction = (Fraction - (1.0 / 3.0)) * 6.0;
      return new Color(0, (float) Math.min(2.0 - Fraction, 1.0), (float) Math.min(Fraction, 1.0));
    } else {// blue to red
      Fraction = (Fraction - (2.0 / 3.0)) * 6.0;
      return new Color((float) Math.min(2.0 - Fraction, 1.0), 0, (float) Math.min(Fraction, 1.0));
    }
  }
    /* ********************************************************************************* */
  public static class PointX extends Point.Double {
    //public double x, y;
    public static PointX Zero = new PointX(0, 0);
    public PointX() {
    }
    public PointX(PointX donor) {
      this.CopyFrom(donor);
    }
    public PointX(double XLoc, double YLoc) {
      this.x = XLoc;
      this.y = YLoc;
    }
    public void Assign(double XLoc, double YLoc) {
      this.x = XLoc;
      this.y = YLoc;
    }
    public final void CopyFrom(PointX donor) {
      this.x = donor.x;
      this.y = donor.y;
    }
    public void Add(PointX other) {
      this.x += other.x;
      this.y += other.y;
    }
    public void Subtract(PointX other) {
      this.x -= other.x;
      this.y -= other.y;
    }
    public void Multiply(double factor) {
      this.x *= factor;
      this.y *= factor;
    }
    public void Normalize() {
      if (this.x != 0 || this.y != 0) {
        double magnitude = Math.sqrt((this.x * this.x) + (this.y * this.y));
        this.x /= magnitude;
        this.y /= magnitude;
      }
    }
    public double GetMagnitude() {
      if (this.x == 0 && this.y == 0) {
        return 0;
      }
      return Math.sqrt((this.x * this.x) + (this.y * this.y));
    }
  }
}
