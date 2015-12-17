/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import voices.Voice.Voice_Singer;
import voices.ISonglet.Singer;
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
    MainGui mg = new MainGui();
    mg.Init();
    if (false) {
      Test_Synthesis();
    }
  }
  /* ********************************************************************************* */
  public static Project Test_Synthesis() {
    //Globals.BaseFreqC0 = 1.0;
    Project prj = new Project();
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
