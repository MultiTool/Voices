/*
 *
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import java.io.File;

/**
 *
 * @author MultiTool
 */
public class TestJunkyard {
  /* ********************************************************************************* */
  public static double Load_Sample_File(int SampleNum, Wave wav) {
    String flpath = new File("").getAbsolutePath();
    String fname = null;
    double BaseFrequency = 0;
    switch (SampleNum) {// put any droney looped wav file here
    case 0:
      fname = flpath + "\\..\\samples\\Plane_Tile_2756pnt250.wav";
      BaseFrequency = Globals.BaseFreqC0 / 150.0;
      break;
    case 1:
      fname = flpath + "\\..\\samples\\Violin_G_Loop.wav";
      BaseFrequency = Globals.BaseFreqC0 / 192.576;
      break;
    case 2:
      fname = flpath + "\\..\\samples\\PluckC4.wav";
      BaseFrequency = Globals.BaseFreqC0 / 261.6;// middle C
      break;
    case 3:
      fname = flpath + "\\..\\samples\\violin_Gs3_025_mezzo-piano_arco-normal_loop.wav";
      BaseFrequency = Globals.BaseFreqC0 / 208.01886792452830188679245283019;// G3-ish
      break;
    case 4:
      fname = flpath + "\\..\\samples\\trombone_C4_15_pianissimo_normal_loop.wav";
      BaseFrequency = Globals.BaseFreqC0 / 263.54581673306772913616450532531;// middle C-ish
      break;
    }
    Audio.Read(fname, wav);
    return BaseFrequency;
  }
  /* ********************************************************************************* */
  public static SampleVoice Create_SampleVoice_Stub(int SampleNum) {
    SampleVoice voice = new SampleVoice();
    Wave wav = new Wave();
    double BaseFrequency = 0;
    BaseFrequency = Load_Sample_File(SampleNum, wav);
    voice.AttachWaveSample(wav, BaseFrequency);
    return voice;
  }
  /* ********************************************************************************* */
  public static SampleVoice Create_SampleVoice_Stub(Wave wav, double BaseFrequency) {
    SampleVoice voice = new SampleVoice();
    voice.AttachWaveSample(wav, BaseFrequency);
    return voice;
  }
  /* ********************************************************************************* */
  public static SampleVoice Create_SampleVoice_Ribbon(double TimeOffset, double OctaveOffset, double LoudnessOffset) {
    SampleVoice voice;
    voice = Create_SampleVoice_Stub(1);
    double TDiff;
    double TimeScale = 0.125;
    double TimeCnt = 0;
    for (int cnt = 0; cnt < 20; cnt++) {
      TDiff = TimeScale;
      voice.Add_Note(NoteMaker.OffsetTime + TimeCnt, OctaveOffset, LoudnessOffset);
      TimeCnt += TDiff;
    }
    return voice;
  }
  /* ********************************************************************************* */
  public static Voice Create_Voice_Ribbon(double TimeOffset, double OctaveOffset, double LoudnessOffset) {
    Voice voice = new Voice();
    double TDiff;
    double TimeScale = 0.125;
    double TimeCnt = 0;
    for (int cnt = 0; cnt < 20; cnt++) {
      TDiff = TimeScale;
      voice.Add_Note(NoteMaker.OffsetTime + TimeCnt, OctaveOffset, LoudnessOffset);
      TimeCnt += TDiff;
    }
    return voice;
  }
  /* ********************************************************************************* */
  public static Voice Create_Warble_Voice(double TimeOffset, double OctaveOffset, double LoudnessOffset) {
    Voice voice = new Voice();// for fuzz testing
    double TDiff, OctaveRand, LoudnessRand = 1.0;
    double TimeScale = 2.0;
    double TimeCnt = 0;
    for (int cnt = 0; cnt < 12; cnt++) {
      TDiff = (Globals.RandomGenerator.nextDouble() * TimeScale) + TimeScale / 2.0;
      OctaveRand = Globals.RandomGenerator.nextDouble() * 2;
      LoudnessRand = (Globals.RandomGenerator.nextDouble() * 0.5) + 0.5;
      voice.Add_Note(TimeCnt, OctaveOffset + OctaveRand, LoudnessOffset * LoudnessRand);
      TimeCnt += TDiff;
    }
    return voice;
  }
  /* ********************************************************************************* */
  public static Voice Create_Voice(double TimeOffset, double OctaveOffset, double LoudnessOffset) {
    Voice voice = new Voice();
    {
      voice.Add_Note(TimeOffset + 0, OctaveOffset + 0, LoudnessOffset * 1);
      voice.Add_Note(TimeOffset + 8, OctaveOffset + 1, LoudnessOffset * 0.5);
      voice.Add_Note(TimeOffset + 16, OctaveOffset + 4, LoudnessOffset * 1);
    }
    return voice;
  }
  /* ********************************************************************************* */
  public static Voice Create_Simple_Note(double TimeOffset, double Duration, double OctaveOffset, double LoudnessOffset) {// a test
    Voice voice = new Voice();
    voice.Add_Note(TimeOffset + 0, OctaveOffset + 0, LoudnessOffset * 1);
    voice.Add_Note(TimeOffset + Duration, OctaveOffset + 0.0, LoudnessOffset * 1);
    return voice;
  }
  /* ********************************************************************************* */
  public static Voice Create_BowTie() {// a test
    Voice voice;
    voice = new Voice();
    voice.Add_Note(1, 4, 1);
    voice.Add_Note(8, 1, 0.5);
    voice.Add_Note(16, 4, 1);
    return voice;
  }
  /* ********************************************************************************* */
  public static Voice Create_Taper() {// a test
    Voice voice;
    voice = new Voice();
    voice.Add_Note(0, 0, 1.0);
    voice.Add_Note(1, 5, 0.0);
    return voice;
  }
  /* ********************************************************************************* */
  public static LoopBox Compose_Loop() {// a test
    LoopBox lbx = new LoopBox();
    Voice note = Create_Taper();
    lbx.Add_Content(note);
    //lbx.Set_Delay(0.75);
    //lbx.Set_Duration(8.0);
    lbx.Set_Delay(0.1);
    lbx.Set_Duration(0.9);
    lbx.Sort_Me();
    return lbx;
  }
  /* ********************************************************************************* */
  public static GroupBox Create_Chord(double TimeOffset, double OctaveOffset, double LoudnessOffset, int NumNotes) {// a test
    double OctaveChange, LoudnessChange;// for stress testing
    GroupBox cbx = new GroupBox();
    for (int cnt = 0; cnt < NumNotes; cnt++) {
      Voice note = Create_Simple_Note(0, 1, 0, 1);
      OctaveChange = cnt * 3;
      LoudnessChange = 1.0 / (1.0 + cnt);
      cbx.Add_SubSong(note, TimeOffset, OctaveOffset + OctaveChange, LoudnessOffset * LoudnessChange);
    }
    return cbx;
  }
  /* ********************************************************************************* */
  public static GroupBox Create_Random_Chorus(double TimeOffset, double OctaveOffset, double LoudnessOffset) {// a test
    double TDiff, OctaveRand, LoudnessRand;// for fuzz testing
    GroupBox cbx = new GroupBox();
    for (int cnt = 0; cnt < 4; cnt++) {
      Voice note = Create_Simple_Note(0, 1, 0, 1);
      TDiff = (Globals.RandomGenerator.nextDouble() * 5);
      OctaveRand = Globals.RandomGenerator.nextDouble() * 4;
      LoudnessRand = 1.0;//(Globals.RandomGenerator.nextDouble() * 0.5) + 0.5;
      cbx.Add_SubSong(note, TimeOffset + TDiff, OctaveOffset + OctaveRand, LoudnessOffset * LoudnessRand);
    }
    return cbx;
  }
  /* ********************************************************************************* */
  public static GroupBox Create_Nested_Chorus(double TimeOffset, double OctaveOffset, double LoudnessOffset, int BoxDepth) {
    Voice note = Create_Simple_Note(0, 1, 0, 1);// for stress testing
    ISonglet songlet0 = note;
    ISonglet songlet1 = note;
    GroupBox cbx = null;
    for (int cnt = 0; cnt < BoxDepth; cnt++) {
      cbx = new GroupBox();
      cbx.MyName = "Chord" + cnt;
      cbx.Add_SubSong(songlet0, 1, 1, LoudnessOffset * 1.0);
      cbx.Add_SubSong(songlet1, 0, 1, LoudnessOffset * 1.0);
      songlet0 = cbx;
    }
    cbx.MyName = "TopChord";
    return cbx;
  }
  /* ********************************************************************************* */
  public static OffsetBox Compose_Warble_Chorus() {
    GroupBox cbx = new GroupBox();
    OffsetBox obox = cbx.Spawn_OffsetBox();

    if (false) {
      Voice vc0 = Create_Voice(0, 0, 1);
      cbx.Add_SubSong(vc0, 0, 3, 0.2);

      Voice vc1 = Create_Voice(0, 0, 1);
      cbx.Add_SubSong(vc1, 2, 0, 1);
    }
    Voice vc2 = Create_Warble_Voice(0, 0, 1);
    cbx.Add_SubSong(vc2, 0, 1.3, 1);
    return obox;
  }
  /* ********************************************************************************* */
  public static GroupBox Compose_Ribbon_Chorus() {
    GroupBox gbx = new GroupBox();
    // OffsetBox obox = gbx.Spawn_OffsetBox();

    Voice vc0 = Create_SampleVoice_Ribbon(0, 0, 1);
    gbx.Add_SubSong(vc0, NoteMaker.OffsetTime, 0, 0.2);

    Voice vc1 = Create_SampleVoice_Ribbon(0, 0, 1);
    gbx.Add_SubSong(vc1, NoteMaker.OffsetTime, 1, 1);

    Voice vc2 = Create_SampleVoice_Ribbon(0, 0, 1);
    gbx.Add_SubSong(vc2, NoteMaker.OffsetTime, 2, 1);
    return gbx;
  }
  /* ********************************************************************************* */
  public static class GetterSetterTest {// Playing with syntax to find a better getter setter. 
    public abstract class GetSet<T> {
      public abstract T Get();
      public abstract void Set(T mt);
    }
    public GetSet<Double> MyVar0 = new GetSet<Double>() {
      @Override public Double Get() {
        return innerval;
      }
      @Override public void Set(Double mt) {
        innerval = mt;
      }
    };
    public abstract class GetSetDouble {
      public abstract double Get();
      public abstract void Set(double mt);
    }
    double innerval;
    public GetSetDouble MyVar1 = new GetSetDouble() {
      @Override public double Get() {
        return innerval;
      }
      @Override public void Set(double mt) {
        innerval = mt;
      }
    };
    public void test() {
      this.MyVar1.Get();
    }
    public interface ISoup {
      GetSet<Double> MyValPtr();
    }
    public class Onions implements ISoup {
      double OnionInnerVal;
      GetSet<Double> gs = new GetSet<Double>() {
        @Override public Double Get() {
          return OnionInnerVal;
        }
        @Override public void Set(Double mt) {
          OnionInnerVal = mt;
        }
      };
      @Override public GetSet<Double> MyValPtr() {// too much code for one property!
        return this.gs;
      }
      public void Runner() {
// ideally we want 'variables' that are like: variable.get() and variable.set(val), 
// so we get the compactness of a C# property while not hiding the fact that it is calling functions.
        this.MyValPtr().Set(12345.6);
        double fred = this.MyValPtr().Get();
        System.out.println(fred);
      }
    }
  }
}
