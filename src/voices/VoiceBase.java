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
  public static double BaseFreqC0 = 16.3516;// hz
  public static double TwoPi = Math.PI * 2.0;// hz
  // collection of control points, each one having a pitch and a volume. rendering morphs from one cp to another. 
  public ArrayList<Point> CPoints = new ArrayList<>();
  public static class Point {
    public double Time;
    public double Octave;
    public double Loudness;
  }
  public static class Player_Head_Base {
    //protected VoiceBase Parent;
    public void Start() {
    }
    public void Skip_To(double Time) {
    }
    public void Render_To(double Time, Wave wave) {
    }
  }
  public VoiceBase() {
  }
  public void Add_Note(Point pnt) {
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
