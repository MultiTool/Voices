/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

/**
 *
 * @author MultiTool
 */
public class TestJunkyard {
  /* ********************************************************************************* */
  public void Compose_Test() {
    ISonglet song = null;
    OffsetBox obox = null;
    GroupBox CMinor, CMajor, DMajor, DMinor;
    OffsetBox CMinorObx, CMajorObx, DMajorObx, DMinorObx;
    double Delay;
    GroupBox cbx;
    NoteMaker nm;
    LoopBox lbx;
    switch (6) {
    case 0:
      song = TestJunkyard.Create_Random_Chorus(0, 0, 1.0);
      obox = song.Spawn_OffsetBox();
      obox.OctaveLoc_s(4);
      break;
    case 1:
      song = TestJunkyard.Create_Nested_Chorus(0, 0, 1.0, 6);
      obox = song.Spawn_OffsetBox();
      break;
    case 2:
      song = TestJunkyard.Create_Chord(0, 2, 1.0, 3);
      obox = song.Spawn_OffsetBox();
      break;
    case 3:
      song = TestJunkyard.Create_Simple_Note(0, 1, 5, 1);
      obox = song.Spawn_OffsetBox();
      break;
    case 4:
      song = TestJunkyard.Compose_Loop();
      obox = song.Spawn_OffsetBox();
      break;
    case 5:
      Delay = 1.5;
      cbx = new GroupBox();
      nm = new NoteMaker();
      lbx = new LoopBox();
      CMajor = nm.MakeMajor(0);// C major
      cbx.Add_SubSong(CMajor, 0, 0, 1.0);
      CMinor = nm.MakeMinor(0);// C minor
      cbx.Add_SubSong(CMinor, Delay * 1, 0, 1.0);
      DMajor = nm.MakeMajor(2);// D major
      cbx.Add_SubSong(DMajor, Delay * 2, 0, 1.0);
      DMinor = nm.MakeMinor(2);// D minor
      cbx.Add_SubSong(DMinor, Delay * 3, 0, 1.0);

      lbx.Add_Content(cbx);
      lbx.Set_Delay(Delay * 4);
      lbx.Set_Duration(30);

      song = lbx;
      obox = song.Spawn_OffsetBox();
      obox.OctaveLoc_s(4);
      break;
    case 6:
      Delay = 1.5;
      cbx = new GroupBox();
      nm = new NoteMaker();
      lbx = new LoopBox();
      CMajorObx = nm.MakeMajor_OBox(0);// C major
      cbx.Add_SubSong(CMajorObx, Delay * 0, CMajorObx.OctaveLoc, 1.0);
      CMinorObx = nm.MakeMinor_OBox(0);// C minor
      cbx.Add_SubSong(CMinorObx, Delay * 1, CMinorObx.OctaveLoc, 1.0);// yuck, redundant
      DMajorObx = nm.MakeMajor_OBox(2);// D major
      cbx.Add_SubSong(DMajorObx, Delay * 2, DMajorObx.OctaveLoc, 1.0);
      DMinorObx = nm.MakeMinor_OBox(2);// D minor
      cbx.Add_SubSong(DMinorObx, Delay * 3, DMinorObx.OctaveLoc, 1.0);

      lbx.Add_Content(cbx);
      lbx.Set_Delay(Delay * 4);
      //lbx.Set_Duration(9.5);
      lbx.Set_Duration(20.5);

      song = lbx;
      obox = song.Spawn_OffsetBox();
      obox.TimeOrg += NoteMaker.OffsetTime;
      obox.OctaveLoc_s(4);
      break;
    case 7:
      obox = TestJunkyard.Compose_Warble_Chorus();
      obox.TimeOrg += NoteMaker.OffsetTime;
      obox.OctaveLoc_s(4);
    case 8:
      obox = TestJunkyard.Compose_Ribbon_Chorus().Spawn_OffsetBox();
      obox.TimeOrg += NoteMaker.OffsetTime;
      obox.OctaveLoc_s(4);
      break;
    }
  }
  /* ********************************************************************************* */
  public static Voice Create_Voice_Ribbon(double TimeOffset, double OctaveOffset, double LoudnessOffset) {
    Voice voice = new Voice();// for fuzz testing
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

    Voice vc0 = Create_Voice_Ribbon(0, 0, 1);
    gbx.Add_SubSong(vc0, NoteMaker.OffsetTime, 0, 0.2);

    Voice vc1 = Create_Voice_Ribbon(0, 0, 1);
    gbx.Add_SubSong(vc1, NoteMaker.OffsetTime, 1, 1);

    Voice vc2 = Create_Voice_Ribbon(0, 0, 1);
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
