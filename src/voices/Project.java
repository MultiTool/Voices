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
  public void Update_Guts() {
    MetricsPacket metrics = new MetricsPacket();
    metrics.MyProject = this;
    rootbox.GetContent().Update_Guts(metrics);
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
    return voice;
  }
  /* ********************************************************************************* */
  public Voice Create_Simple_Note(double TimeOffset, double Duration, double OctaveOffset, double LoudnessOffset) {
    Voice voice = new Voice();
    voice.Add_Note(TimeOffset + 0, OctaveOffset + 0, LoudnessOffset * 1);
    voice.Add_Note(TimeOffset + Duration, OctaveOffset + 0, LoudnessOffset * 1);
    return voice;
  }
  /* ********************************************************************************* */
  public Voice Create_BowTie() {
    Voice voice;
    voice = new Voice();
    voice.Set_Project(this);
    voice.Add_Note(1, 4, 1);
    voice.Add_Note(8, 1, 0.5);
    voice.Add_Note(16, 4, 1);
    return voice;
  }
  /* ********************************************************************************* */
  public Voice Create_Taper() {
    Voice voice;
    voice = new Voice();
    voice.Set_Project(this);
    voice.Add_Note(0, 0, 1.0);
    voice.Add_Note(1, 5, 0.0);
    return voice;
  }
  /* ********************************************************************************* */
  public LoopBox Compose_Loop() {
    LoopBox lbx = new LoopBox();
    lbx.Set_Project(this);
    Voice note = Create_Taper();
    lbx.Add_Content(note);
    //lbx.Set_Delay(0.75);
    //lbx.Set_Duration(8.0);
    lbx.Set_Delay(0.001);
    lbx.Set_Duration(0.9);
    lbx.Sort_Me();
    return lbx;
  }
  /* ********************************************************************************* */
  public ChorusBox Create_Chord(double TimeOffset, double OctaveOffset, double LoudnessOffset, int NumNotes) {
    double OctaveChange, LoudnessChange;// for stress testing
    ChorusBox cbx = new ChorusBox();
    cbx.Set_Project(this);
    for (int cnt = 0; cnt < NumNotes; cnt++) {
      Voice note = Create_Simple_Note(0, 1, 0, 1);
      OctaveChange = cnt * 3;
      LoudnessChange = 1.0 / (1.0 + cnt);
      cbx.Add_SubSong(note, TimeOffset, OctaveOffset + OctaveChange, LoudnessOffset * LoudnessChange);
    }
    return cbx;
  }
  /* ********************************************************************************* */
  public ChorusBox Create_Random_Chorus(double TimeOffset, double OctaveOffset, double LoudnessOffset) {
    double TDiff, OctaveRand, LoudnessRand;// for fuzz testing
    ChorusBox cbx = new ChorusBox();
    cbx.Set_Project(this);
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
  public ChorusBox Create_Nested_Chorus(double TimeOffset, double OctaveOffset, double LoudnessOffset, int BoxDepth) {
    Voice note = Create_Simple_Note(0, 1, 0, 1);// for stress testing
    ISonglet songlet = note;
    ChorusBox cbx = null;
    for (int cnt = 0; cnt < BoxDepth; cnt++) {
      cbx = new ChorusBox();
      cbx.MyName = "Chord" + cnt;
      cbx.Set_Project(this);
      cbx.Add_SubSong(songlet, 1, 1, LoudnessOffset * 1.0);
      songlet = cbx;
    }
    cbx.MyName = "TopChord";
    return cbx;
  }
  /* ********************************************************************************* */
  public ChorusBox Compose_Warble_Chorus() {
    ChorusBox cbx = new ChorusBox();
    cbx.Set_Project(this);
    this.rootbox = cbx.Spawn_OffsetBox();

    Voice vc0 = Create_Voice(0, 0, 1);
    cbx.Add_SubSong(vc0, 0, 3, 0.2);

    Voice vc1 = Create_Voice(0, 0, 1);
    cbx.Add_SubSong(vc1, 2, 0, 1);

    Voice vc2 = Create_Warble_Voice(0, 0, 1);
    cbx.Add_SubSong(vc2, 0, 1.3, 1);

    return cbx;
  }
  /* ********************************************************************************* */
  public void Compose_Chorus_Test2() {
    ISonglet song = null;
    OffsetBox obox = null;
    ChorusBox CMinor, CMajor, DMajor, DMinor;
    switch (5) {
    case 0:
      song = Create_Random_Chorus(0, 0, 1.0);
      obox = song.Spawn_OffsetBox();
      break;
    case 1:
      song = Create_Nested_Chorus(0, 0, 1.0, 4);
      obox = song.Spawn_OffsetBox();
      break;
    case 2:
      song = Create_Chord(0, 2, 1.0, 3);
      obox = song.Spawn_OffsetBox();
      break;
    case 3:
      //song = Create_Simple_Note(0, 2.3, 1);
      song = Create_Simple_Note(0, 1, 5, 1);
      song.Set_Project(this);
      obox = song.Spawn_OffsetBox();
      break;
    case 4:
      song = Compose_Loop();
      obox = song.Spawn_OffsetBox();
      break;
    case 5:
      double Delay = 1.5;
      //Delay = 3;
      ChorusBox cbx = new ChorusBox();
      NoteMaker nm = new NoteMaker();
      LoopBox lbx = new LoopBox();
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
      lbx.Set_Duration(100);

      song = lbx;
      song.Set_Project(this);
      obox = song.Spawn_OffsetBox();
      obox.OctaveLoc_s(4);
      break;
    }

    this.rootbox = obox;

    this.Update_Guts();

    Audio_Test();
    Render_Test();
  }
  /* ********************************************************************************* */
  public void Audio_Test() {
    this.SampleRate = Globals.SampleRate;
    Singer RootPlayer = this.rootbox.Spawn_Singer();
    RootPlayer.Compound(this.rootbox);

    double FinalTime = this.rootbox.GetContent().Get_Duration();

    Wave wave_render = new Wave();
    wave_render.Init(0, FinalTime, SampleRate);
    Wave wave_scratch = new Wave();

    long StartTime, EndTime;
    RootPlayer.Start();
    StartTime = System.currentTimeMillis();

    Audio aud = new Audio();
    // aud.SaveAudio("test.wav", this.rootbox);

    aud.Start();
    int NumSlices = 100;
    for (int cnt = 0; cnt < NumSlices; cnt++) {
      System.out.print("cnt:" + cnt + " ");
      double FractAlong = (((double) (cnt + 1)) / (double) NumSlices);
      RootPlayer.Render_To(FinalTime * FractAlong, wave_scratch);
      // wave_scratch.Normalize();
      wave_scratch.Amplify(0.2);
      wave_render.Append(wave_scratch);
      aud.Feed(wave_scratch);
      System.out.println(" done.");
    }
    aud.Stop();
    EndTime = System.currentTimeMillis();
    System.out.println("Audio_Test time:" + (EndTime - StartTime));// Render_To time: 150 milliseconds per 16 seconds. 
  }
  /* ********************************************************************************* */
  public void Render_Test() {
    //this.SampleRate = Globals.SampleRateTest;
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
      wave_scratch.Normalize();
      wave_render.Append(wave_scratch);
    }

    EndTime = System.currentTimeMillis();
    System.out.println("Render_To time:" + (EndTime - StartTime));// Render_To time: 150 milliseconds per 16 seconds. 

    SaveWave(wave_render, "wave_render.csv");
    boolean nop = true;
  }
}
