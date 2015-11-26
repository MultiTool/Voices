/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import voices.ISonglet.MetricsPacket;
import voices.ISonglet.Singer;
import static voices.Voices.SaveWave;

/**
 *
 * @author MultiTool
 */
public class Project {
  OffsetBox rootbox;
  // public int SampleRate = 100;// Globals.SampleRate;
  public int SampleRate = Globals.SampleRate;
  /* ********************************************************************************* */
  public Project() {
    //this.Compose_Test();
  }
  /* ********************************************************************************* */
  public Voice Create_Warble_Voice(double TimeOffset, double OctaveOffset, double LoudnessOffset) {
    Voice voice = new Voice();// for fuzz testing
    double TDiff, OctaveRand, LoudnessRand;
    double TimeCnt = 0;
    for (int cnt = 0; cnt < 12; cnt++) {
      TDiff = (Globals.RandomGenerator.nextDouble() * 2) + 1;
      OctaveRand = Globals.RandomGenerator.nextDouble() * 2;
      LoudnessRand = (Globals.RandomGenerator.nextDouble() * 0.5) + 0.5;
      voice.Add_Note(TimeCnt, OctaveOffset + OctaveRand, LoudnessOffset * LoudnessRand);
      TimeCnt += TDiff;
    }
    MetricsPacket metrics = new MetricsPacket();
    voice.Update_Guts(metrics);
    return voice;
  }
  /* ********************************************************************************* */
  public Voice Create_Voice(double TimeOffset, double OctaveOffset, double LoudnessOffset) {
    Voice voice = new Voice();
    {
      voice.Add_Note(TimeOffset + 0, OctaveOffset + 0, LoudnessOffset * 1);
      voice.Add_Note(TimeOffset + 8, OctaveOffset + 1, LoudnessOffset * 0.5);
      voice.Add_Note(TimeOffset + 16, OctaveOffset + 4, LoudnessOffset * 1);
    }
    MetricsPacket metrics = new MetricsPacket();
    voice.Update_Guts(metrics);
    return voice;
  }
  /* ********************************************************************************* */
  public Voice Create_Simple_Note(double TimeOffset, double OctaveOffset, double LoudnessOffset) {
    Voice voice = new Voice();
    voice.Add_Note(TimeOffset + 0, OctaveOffset + 0, LoudnessOffset * 1);
    voice.Add_Note(TimeOffset + 1, OctaveOffset + 0, LoudnessOffset * 1);
    MetricsPacket metrics = new MetricsPacket();
    voice.Update_Guts(metrics);
    return voice;
  }
  /* ********************************************************************************* */
  public ChorusBox Create_Chord(double TimeOffset, double OctaveOffset, double LoudnessOffset, int NumNotes) {
    double OctaveChange, LoudnessChange;// for stress testing
    ChorusBox cbx = new ChorusBox();
    cbx.Set_Project(this);
    for (int cnt = 0; cnt < NumNotes; cnt++) {
      Voice note = Create_Simple_Note(0, 0, 1);
      OctaveChange = cnt * 3;
      LoudnessChange = 1.0 / (1.0 + cnt);
      cbx.Add_SubSong(note, TimeOffset, OctaveOffset + OctaveChange, LoudnessOffset * LoudnessChange);
    }
    cbx.Sort_Me();
    MetricsPacket metrics = new MetricsPacket();
    cbx.Update_Guts(metrics);
    return cbx;
  }
  /* ********************************************************************************* */
  public ChorusBox Create_Random_Chorus(double TimeOffset, double OctaveOffset, double LoudnessOffset) {
    double TDiff, OctaveRand, LoudnessRand;// for fuzz testing
    ChorusBox cbx = new ChorusBox();
    cbx.Set_Project(this);
    for (int cnt = 0; cnt < 4; cnt++) {
      Voice note = Create_Simple_Note(0, 0, 1);
      TDiff = (Globals.RandomGenerator.nextDouble() * 5);
      OctaveRand = Globals.RandomGenerator.nextDouble() * 4;
      LoudnessRand = 1.0;//(Globals.RandomGenerator.nextDouble() * 0.5) + 0.5;
      cbx.Add_SubSong(note, TimeOffset + TDiff, OctaveOffset + OctaveRand, LoudnessOffset * LoudnessRand);
    }
    cbx.Sort_Me();
    MetricsPacket metrics = new MetricsPacket();
    cbx.Update_Guts(metrics);
    return cbx;
  }
  /* ********************************************************************************* */
  public ChorusBox Create_Nested_Chorus(double TimeOffset, double OctaveOffset, double LoudnessOffset, int BoxDepth) {
    Voice note = Create_Simple_Note(0, 0, 1);// for stress testing
    ISonglet songlet = note;
    ChorusBox cbx = null;
    for (int cnt = 0; cnt < BoxDepth; cnt++) {
      cbx = new ChorusBox();
      cbx.MyName = "Chord" + cnt;
      cbx.Set_Project(this);
      cbx.Add_SubSong(songlet, 1, 1, LoudnessOffset * 1.0);
      cbx.Sort_Me();
      MetricsPacket metrics = new MetricsPacket();
      cbx.Update_Guts(metrics);
      songlet = cbx;
    }
    cbx.MyName = "TopChord";
    return cbx;
  }
  /* ********************************************************************************* */
  public void Compose_Chorus_Test2() {
    ISonglet cbx = null;
    switch (2) {
      case 0:
        cbx = Create_Random_Chorus(0, 0, 1.0);
        break;
      case 1:
        cbx = Create_Nested_Chorus(0, 0, 1.0, 4);
        break;
      case 2:
        cbx = Create_Chord(0, 0, 1.0, 2);
        break;
      case 3:
        cbx = Create_Simple_Note(0, 2.3, 1);
        cbx.Set_Project(this);
        break;
    }
    OffsetBox obox = cbx.Spawn_OffsetBox();
    this.rootbox = obox;

    MetricsPacket metrics = new MetricsPacket();
    cbx.Update_Guts(metrics);

    Render_Test();
  }
  /* ********************************************************************************* */
  public void Compose_Chorus_Test1() {
    ChorusBox cbx = new ChorusBox();
    cbx.Set_Project(this);
    this.rootbox = cbx.Spawn_OffsetBox();

    Voice vc0 = Create_Voice(0, 0, 1);
    cbx.Add_SubSong(vc0, 0, 3, 0.2);

    Voice vc1 = Create_Voice(0, 0, 1);
    cbx.Add_SubSong(vc1, 2, 0, 1);

    Voice vc2 = Create_Warble_Voice(0, 0, 1);
    cbx.Add_SubSong(vc2, 0, 1.3, 1);

    MetricsPacket metrics = new MetricsPacket();
    cbx.Update_Guts(metrics);

    Render_Test();
  }
  /* ********************************************************************************* */
  public void Compose_Chorus_Test() {
    ChorusBox cbx = new ChorusBox();
    cbx.Set_Project(this);
    this.rootbox = cbx.Spawn_OffsetBox();

    Voice rootvoice = new Voice();
    cbx.Add_SubSong(rootvoice, 0, 0, 0);
    rootvoice.Add_Note(1, 0, 1);
    rootvoice.Add_Note(4, 1, 1);
    //rootvoice.Add_Note(1, 4, 1);
//      rootvoice.Add_Note(8, 1, 0.5);
//      rootvoice.Add_Note(16, 4, 1);

    MetricsPacket metrics = new MetricsPacket();
    cbx.Update_Guts(metrics);
  }
  /* ********************************************************************************* */
  public void Compose_Test() {
    Voice rootvoice;
    rootvoice = new Voice();
    rootvoice.Set_Project(this);
    rootbox = rootvoice.Spawn_OffsetBox();
    rootbox.Clear();
    {
      rootvoice.Add_Note(1, 4, 1);
      rootvoice.Add_Note(8, 1, 0.5);
      rootvoice.Add_Note(16, 4, 1);
    }
    MetricsPacket metrics = new MetricsPacket();
    rootvoice.Update_Guts(metrics);
  }
  /* ********************************************************************************* */
  public void Render_Test() {
    this.SampleRate = Globals.SampleRateTest;
    //this.SampleRate = Globals.SampleRate;
    Singer RootPlayer = this.rootbox.Spawn_Singer();
    RootPlayer.Compound(this.rootbox);

    double FinalTime = this.rootbox.GetContent().Get_Duration();

//    int nsamps = this.rootbox.GetContent().Get_Sample_Count(this.SampleRate);
//    wave_render.Init(nsamps);
    Wave wave_render = new Wave();
    wave_render.Init(0, FinalTime, SampleRate);
    wave_render.Fill(777.0);
    Wave wave_scratch = new Wave();

    long StartTime, EndTime;
    RootPlayer.Start();
    StartTime = System.currentTimeMillis();
    ///RootPlayer.Skip_To(1.2);
    //RootPlayer.Render_To(4, wave_render);
    //RootPlayer.Skip_To(4.29);

    //RootPlayer.Skip_To(FinalTime / 2);
//    RootPlayer.Render_To(FinalTime / 2, wave_scratch);
//    wave_render.Append(wave_scratch);
//    RootPlayer.Render_To(FinalTime, wave_scratch);
//    wave_render.Append(wave_scratch);
    int NumSlices = 8;
    for (int cnt = 0; cnt < NumSlices; cnt++) {
      double FractAlong = (((double) (cnt + 1)) / (double) NumSlices);
      RootPlayer.Render_To(FinalTime * FractAlong, wave_scratch);
      wave_render.Append(wave_scratch);
    }

    EndTime = System.currentTimeMillis();
    System.out.println("Render_To time:" + (EndTime - StartTime));// Render_To time: 150 milliseconds per 16 seconds. 

    SaveWave(wave_render, "wave_render.csv");
    boolean nop = true;
  }
  /* ********************************************************************************* */
  public static void Test1() {
    Globals.SampleRate = 100;
    Globals.BaseFreqC0 = 1.0;
    Voice vc = new Voice();
    Wave wave_render = new Wave();

    int TDiff = 16;// seconds
    int nsamps;
    //nsamps = TDiff * Globals.SampleRate;

    {
      vc.Add_Note(1, 4, 1);
      vc.Add_Note(8, 1, 0.5);
      vc.Add_Note(TDiff, 4, 1);
    }
    MetricsPacket metrics = new MetricsPacket();
    vc.Update_Guts(metrics);

    nsamps = vc.Get_Sample_Count(Globals.SampleRate);

    wave_render.Init(nsamps);

    Singer hd = vc.Spawn_Singer();

    long StartTime, EndTime;

    hd.Start();
    StartTime = System.currentTimeMillis();
    ///hd.Skip_To(1.2);
    //hd.Render_To(4, wave_render);
    //hd.Skip_To(4.29);
    hd.Render_To(0.5, wave_render);
    hd.Render_To(TDiff - 4, wave_render);
    hd.Render_To(TDiff - 0, wave_render);
    //hd.Render_Range(0, 2, wave_render);
    EndTime = System.currentTimeMillis();
    System.out.println("Render_To time:" + (EndTime - StartTime));// Render_To time: 150 milliseconds per 16 seconds. 
    //System.out.println("Render_Range time:" + (EndTime - StartTime));

    SaveWave(wave_render, "wave_render.csv");
    boolean nop = true;
  }
}
