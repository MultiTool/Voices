package voices;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.swing.filechooser.FileNameExtensionFilter;
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

    RegisterFactories();
    switch (4) {
    case 0: {
      JavaParse.MetaProject mp = new JavaParse.MetaProject();
      mp.PortAll();
      break;
    }
    case 1: {
      Wave wav = new Wave();
      String flpath = new File("").getAbsolutePath();
      Audio.Read(flpath + "\\..\\samples\\trombone_C4_15_pianissimo_normal_shortloop.wav", wav);
      wav.Normalize();
      Wave.SaveWaveToCsv("horn.csv", wav);
      break;
    }
    case 2: {
      NoteMaker.Wave_Test();
      break;
    }
    case 3: {
      String fname = "ahh_looped.wav";
      Audio.Load(fname, null);
      break;
    }
    case 4:
      MainGui mg = new MainGui();
      mg.Init();
      break;
    case 5: {
      Test_Synthesis();
      break;
    }
    case 6:
      BenchTest();
      break;
    case 7: {
      Wave wav = new Wave();
      String flpath = new File("").getAbsolutePath();
      Audio.Read(flpath + "\\..\\samples\\Ahh_Waveform01.wav", wav);
      wav.Normalize();
      Wave.SaveWaveToCsv("Ahh_Waveform01.csv", wav);
      break;
    }
    case 8: {
      Wave wav = new Wave();
      WTVoice wt = new WTVoice();
      double TimeStep = 6.0;// seconds
      NoteMaker.Create_Block_Voice(wt, TimeStep, 1);
      WTVoice.WT_OffsetBox wobx = wt.Spawn_OffsetBox();
      wobx.OctaveY = 4.0;
      AudProject project = new AudProject();
      project.AudioRoot = wobx;
      project.Update_Guts();
      
      WTVoice.WT_Singer  sing = wobx.Spawn_Singer();
      int LastVpDex = wt.CPoints.size()-1;
      sing.Start(); //sing.Render_To(2.0, wav);
      sing.Render_Segment_Integral(wt.CPoints.get(1), wt.CPoints.get(LastVpDex), wav);
      
      Audio aud = new Audio();
      aud.Save("WTVoice100.wav", wav.GetWave());
      if (false){
        wt.WaveTable_Test(wav);
      }
      break;
    }
    }
  }
  /* ********************************************************************************* */
  public static void RegisterFactories() {
    // Because you can't count on Java static variables being initialized EVER, we are initializing these explicitly here.
    Globals.FactoryLUT.put(GraphicBox.Graphic_OffsetBox.ObjectTypeName, new GraphicBox.Graphic_OffsetBox.Factory());
    Globals.FactoryLUT.put(Voice.Voice_OffsetBox.ObjectTypeName, new Voice.Voice_OffsetBox.Factory());
    Globals.FactoryLUT.put(SampleVoice.SampleVoice_OffsetBox.ObjectTypeName, new SampleVoice.SampleVoice_OffsetBox.Factory());
    Globals.FactoryLUT.put(PluckVoice.PluckVoice_OffsetBox.ObjectTypeName, new PluckVoice.PluckVoice_OffsetBox.Factory());
    Globals.FactoryLUT.put(GroupSong.Group_OffsetBox.ObjectTypeName, new GroupSong.Group_OffsetBox.Factory());
    Globals.FactoryLUT.put(LoopBox.Loop_OffsetBox.ObjectTypeName, new LoopBox.Loop_OffsetBox.Factory());
    //Globals.FactoryLUT.put("OffsetBox", new OffsetBox.Factory());
  }
  /* ********************************************************************************* */
  public static void BenchTest() {
    Date dt = new Date();
    Calendar cal = Calendar.getInstance();
    int iter = 100000000;
    double res = 0;
    long start, finish;
    start = System.currentTimeMillis();
    for (double cnt = 0; cnt < iter; cnt++) {
      res = Math.pow(cnt, 2);
    }
    finish = System.currentTimeMillis();
    System.out.println(res + " time:" + (finish - start));
    start = System.currentTimeMillis();
    for (double cnt = 0; cnt < iter; cnt++) {
      res = cnt * cnt;
    }
    finish = System.currentTimeMillis();
    System.out.println(res + " time:" + (finish - start));
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
