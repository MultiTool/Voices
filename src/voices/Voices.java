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
    if (false) {
      PortAll();
      return;
    }
    if (false) {
      Wave wav = new Wave();
      String flpath = new File("").getAbsolutePath();
      Audio.Read(flpath + "\\..\\samples\\trombone_C4_15_pianissimo_normal_shortloop.wav", wav);
      wav.Normalize();
      Wave.SaveWaveToCsv("horn.csv", wav);
      return;
    }
    if (false) {
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
  public static void RegisterFactories() {
    // Because you can't count on Java static variables being initialized EVER, we are initializing these explicitly here.
    Globals.FactoryLUT.put(GraphicBox.Graphic_OffsetBox.ObjectTypeName, new GraphicBox.Graphic_OffsetBox.Factory());
    Globals.FactoryLUT.put(Voice.Voice_OffsetBox.ObjectTypeName, new Voice.Voice_OffsetBox.Factory());
    Globals.FactoryLUT.put(SampleVoice.SampleVoice_OffsetBox.ObjectTypeName, new SampleVoice.SampleVoice_OffsetBox.Factory());
    Globals.FactoryLUT.put(GroupBox.Group_OffsetBox.ObjectTypeName, new GroupBox.Group_OffsetBox.Factory());
    Globals.FactoryLUT.put(LoopBox.Loop_OffsetBox.ObjectTypeName, new LoopBox.Loop_OffsetBox.Factory());
    //Globals.FactoryLUT.put("OffsetBox", new OffsetBox.Factory());
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
  /* ********************************************************************************* */
  public static void PortAll() {
    String fdir = new File("").getAbsolutePath();
    String fpath;
    fpath = fdir + "\\src\\voices\\Voices.java";
    fpath = fdir + "\\src\\voices\\MonkeyBox.java";
    fpath = fdir + "\\src\\voices\\GroupBox.java";
    fpath = fdir + "\\src\\voices\\JavaParse.java";
//      fpath = fdir + "\\src\\voices\\MainGui.java";
    fpath = fdir + "\\src\\voices\\Voice.java";

    String inpath = fdir + "\\src\\voices\\";
    String outpath = fdir + "\\..\\VM\\";

    JavaParse.MetaProject mp = new JavaParse.MetaProject();
    mp.PortAll();
    if (false) {
      /*
       so the rules are:
       get all inheritances in a file.
       for each inheritance, see if there is another file by that name.
       if so, add an include ""
       better to look up inheritance in a global list of classes - then get the file name, and use that.
       for all other classes referenced in a file, use forward declarations at the top.
       */
      File fl;
      ArrayList<String> FullPath = new ArrayList<String>();
      String OnePath = "";
      // read in all file info in the directory
      File dir = new File(inpath);
      File[] FileList = dir.listFiles(new FilenameFilter() {
        public boolean accept(File dir, String filename) {
          return filename.endsWith(".java");
        }
      });
      // reduce to a list of just the file names
      int len = FileList.length;
      for (int fcnt = 0; fcnt < len; fcnt++) {
        fl = FileList[fcnt];
        try {
          OnePath = fl.getCanonicalPath();
          FullPath.add(OnePath);
        } catch (Exception ex) {
          boolean nop = true;
        }
      }
      // parse and create the meta file objects
      ArrayList<JavaParse.MetaFile> MetaFileList = new ArrayList<JavaParse.MetaFile>();
      for (int fcnt = 0; fcnt < len; fcnt++) {
        OnePath = FullPath.get(fcnt);
        JavaParse.MetaFile JavaFile = JavaParse.ParseFile(OnePath);
        MetaFileList.add(JavaFile);
      }
      // convert to cpp/hpp
      JavaParse.CppLuggage Luggage = new JavaParse.CppLuggage();
      Luggage.Files = MetaFileList;
      // Luggage.Chunks = Chunks; // to do: preserve chunks and pass it here.
      for (int fcnt = 0; fcnt < len; fcnt++) {
        JavaParse.MetaFile JavaFile = MetaFileList.get(fcnt);
        JavaFile.ConvertToCpp(Luggage);
      }
      boolean nop = true;
    }
    if (false) {
      byte[] encoded = null;
      String JavaTxt = "";
      try {
        encoded = Files.readAllBytes(Paths.get(fpath));
        JavaTxt = new String(encoded, StandardCharsets.UTF_8);
      } catch (Exception ex) {
        boolean nop = true;
      }
      String CppTxt;
      Path p = Paths.get(fpath);
      String FName = p.getFileName().toString();
      FName = FName.replace(".java", "");// remove extension
      CppTxt = JavaParse.Parse(JavaTxt, FName);
      try {
        File file = new File(fdir + "\\src\\voices\\test1.hpp");
        //"\\..\\VM"
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(CppTxt);
        fileWriter.flush();
        fileWriter.close();
      } catch (IOException e) {
        boolean nop = true;
      }
    }
  }
}
