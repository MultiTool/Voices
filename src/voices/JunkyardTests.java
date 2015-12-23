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
public class JunkyardTests {
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
}
