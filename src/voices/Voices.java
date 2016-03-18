package voices;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
//import voices.VoiceBase.Point;

/**
 *
 * @author MultiTool
 * Someday I'd like to port this whole project to C++.  
 * Java is my pseudocode, easy to write, portable and reliable media I/O, but not as potentially fast as C/C++. 
 * 
 */
public class Voices {
  /**
   * @param args the command line arguments
   */
  /* ********************************************************************************* */
  public static void main(String[] args) {
    if (false){
      Wave wav = new Wave();
      String flpath = new File("").getAbsolutePath();
      Audio.Read(flpath + "\\..\\samples\\trombone_C4_15_pianissimo_normal_shortloop.wav", wav);
      wav.Normalize();
      Wave.SaveWaveToCsv("horn.csv", wav);
      return;
    }
    if (false){
      NoteMaker.Wave_Test();
      return;
    }
    Point2D.Double pnt = new Point2D.Double(12.3, 45.6);
    String txt = pnt.toString();
    if (false) {
      String fname = "ahh_looped.wav";
      Audio.Load(fname, null);
    }
    MainGui mg = new MainGui();
    mg.Init();
    if (false) {
      Test_Synthesis();
    }
  }
  /* ********************************************************************************* */
  public static AudProject Test_Synthesis() {
    //Globals.BaseFreqC0 = 1.0;
    AudProject prj = new AudProject();
    // prj.Compose_Warble_Chorus();
    prj.Compose_Test();

//    prj.Compose_Chorus_Test();
//    prj.Render_Test();
    return prj;
  }
  /* ********************************************************************************* */
  public static void SaveWave3(Wave wave0, Wave wave1, Wave wave2, String FileName) {
    try {
      PrintWriter out = new PrintWriter(FileName);
      for (int cnt = 0; cnt < wave0.NumSamples; cnt++) {
        out.println(wave0.Get(cnt) + ", " + wave1.Get(cnt) + ", " + wave2.Get(cnt) + "");
      }
      out.close();
    } catch (FileNotFoundException ex) {
    }
  }
  /* ********************************************************************************* */
  public static void SaveWave(Wave wave, String FileName) {
    try {
      PrintWriter out = new PrintWriter(FileName);
      for (int cnt = 0; cnt < wave.NumSamples; cnt++) {
        //out.println(cnt + ", " + wave.wave[cnt] + "");
        out.println(wave.Get(cnt) + "");
      }
      out.close();
    } catch (FileNotFoundException ex) {
    }
  }
}
