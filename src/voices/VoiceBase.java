/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author MultiTool
 */
public class VoiceBase {
  // collection of control points, each one having a pitch and a volume. rendering morphs from one cp to another. 
  public ArrayList<Point> CPoints = new ArrayList<>();
  public static class Point {
    public double RealTime = 0.0, SubTime = 0.0;// SubTime is cumulative subjective time.
    public double Octave = 0.0;
    public double Loudness;
    public void CopyFrom(Point source) {
      this.RealTime = source.RealTime;
      this.SubTime = source.SubTime;
      this.Octave = source.Octave;
      this.Loudness = source.Loudness;
    }
    public double GetFrequencyFactor() {
      return Math.pow(2.0, this.Octave);
    }
  }
  public static class Player_Head_Base {
    //protected VoiceBase Parent;
    /* ********************************************************************************* */
    public void Start() {
    }
    /* ********************************************************************************* */
    public void Skip_To(double EndTime) {
    }
    /* ********************************************************************************* */
    public void Render_To(double EndTime, Wave wave) {
    }
    /* ********************************************************************************* */
    public void Render_Range(int dex0, int dex1, Wave wave) {
    }
    /* ********************************************************************************* */
    public void Render_Segment_Iterative(Point pnt0, Point pnt1, Wave wave0) {
    }
    /* ********************************************************************************* */
    public void Render_Segment_Integral(Point pnt0, Point pnt1, Wave wave1) {
    }
  }
  public VoiceBase() {
  }
  public void Add_Note(Point pnt) {
    this.CPoints.add(pnt);
  }
  public Point Add_Note(double RealTime, double Octave, double Loudness) {
    Point pnt = new Point();
    pnt.Octave = Octave;
    pnt.RealTime = RealTime;
    pnt.SubTime = 0.0;
    pnt.Loudness = Loudness;
    this.CPoints.add(pnt);
    return pnt;
  }
  public Player_Head_Base Spawn_Player() {
    return null;
//    Player_Head_Base ph = new Player_Head_Base();
//    ph.Parent = this;
//    return ph;
  }

//  public interface IFace {
//    public interface IFaceChild {
//    }
//    public static class haha {
//      public void test() {
//        System.out.println("hello");
//      }
//    }
//  }
//  public abstract class AbstractTest {
//
//    // all subclasses have access to these classes
//    public class PublicInner {
//    }
//    protected class ProtectedInner {
//    }
//
//    // subclasses in the same package have access to this class
//    class PackagePrivateInner {
//    }
//
//    // subclasses do not have access to this class
//    private class PrivateClass {
//      public void test() {
//        System.out.println("hello");
//      }
//    }
//
//  }
}
