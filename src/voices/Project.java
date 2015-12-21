/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import java.awt.Graphics2D;
import voices.GraphicBox.Graphic_OffsetBox;
import voices.IDrawable.Drawing_Context;
import voices.ISonglet.MetricsPacket;
import voices.ISonglet.Singer;
import static voices.Voices.SaveWave;

/**
 *
 * @author MultiTool
 */
public class Project {
  OffsetBox AudioRoot;
  public Graphic_OffsetBox GraphicRoot;
  GraphicBox GBox;
  public int SampleRate = Globals.SampleRate;
  /* ********************************************************************************* */
  public Project() {
    //this.Compose_Test();
    this.GBox = new GraphicBox();
    this.GraphicRoot = this.GBox.Spawn_My_OffsetBox();
  }
  /* ********************************************************************************* */
  public void Draw_Me(Graphics2D g2d) {
    Drawing_Context dc = new Drawing_Context();
    dc.gr = g2d;
    dc.ClipBounds = new CajaDelimitadora();
    dc.ClipBounds.Assign(0, 0, 10000, 10000);// arbitrarily large
    dc.Offset = new OffsetBox();
    dc.GlobalOffset = new OffsetBox();
    //dc.GlobalOffset.ScaleX = 50; dc.GlobalOffset.ScaleY = 50;
    GraphicRoot.Draw_Me(dc);
  }
  /* ********************************************************************************* */
  public void Wrap_For_Graphics(OffsetBox obox) {
    this.AudioRoot = obox;
    this.GraphicRoot.Attach_Content(this.AudioRoot);
    this.Update_Guts();
    this.GraphicRoot.UpdateBoundingBox();
  }
  /* ********************************************************************************* */
  public void Create_For_Graphics() {// a test
    ISonglet song = null;
    OffsetBox obox = null;

    //song = Create_Simple_Note(0, 1, 5, 1);
    song = Create_Warble_Voice(0, 4, 1.0);
    song.Set_Project(this);
    obox = song.Spawn_OffsetBox();

    this.Wrap_For_Graphics(obox);
  }
  /* ********************************************************************************* */
  public void Update_Guts() {
    MetricsPacket metrics = new MetricsPacket();
    metrics.MyProject = this;
    AudioRoot.GetContent().Update_Guts(metrics);
  }
  /* ********************************************************************************* */
  public Voice Create_Warble_Voice(double TimeOffset, double OctaveOffset, double LoudnessOffset) {
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
  public Voice Create_Simple_Note(double TimeOffset, double Duration, double OctaveOffset, double LoudnessOffset) {// a test
    Voice voice = new Voice();
    voice.Add_Note(TimeOffset + 0, OctaveOffset + 0, LoudnessOffset * 1);
    voice.Add_Note(TimeOffset + Duration, OctaveOffset + 0.0, LoudnessOffset * 1);
    return voice;
  }
  /* ********************************************************************************* */
  public Voice Create_BowTie() {// a test
    Voice voice;
    voice = new Voice();
    voice.Set_Project(this);
    voice.Add_Note(1, 4, 1);
    voice.Add_Note(8, 1, 0.5);
    voice.Add_Note(16, 4, 1);
    return voice;
  }
  /* ********************************************************************************* */
  public Voice Create_Taper() {// a test
    Voice voice;
    voice = new Voice();
    voice.Set_Project(this);
    voice.Add_Note(0, 0, 1.0);
    voice.Add_Note(1, 5, 0.0);
    return voice;
  }
  /* ********************************************************************************* */
  public LoopBox Compose_Loop() {// a test
    LoopBox lbx = new LoopBox();
    lbx.Set_Project(this);
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
  public GroupBox Create_Chord(double TimeOffset, double OctaveOffset, double LoudnessOffset, int NumNotes) {// a test
    double OctaveChange, LoudnessChange;// for stress testing
    GroupBox cbx = new GroupBox();
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
  public GroupBox Create_Random_Chorus(double TimeOffset, double OctaveOffset, double LoudnessOffset) {// a test
    double TDiff, OctaveRand, LoudnessRand;// for fuzz testing
    GroupBox cbx = new GroupBox();
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
  public GroupBox Create_Nested_Chorus(double TimeOffset, double OctaveOffset, double LoudnessOffset, int BoxDepth) {
    Voice note = Create_Simple_Note(0, 1, 0, 1);// for stress testing
    ISonglet songlet0 = note;
    ISonglet songlet1 = note;
    GroupBox cbx = null;
    for (int cnt = 0; cnt < BoxDepth; cnt++) {
      cbx = new GroupBox();
      cbx.MyName = "Chord" + cnt;
      cbx.Set_Project(this);
      cbx.Add_SubSong(songlet0, 1, 1, LoudnessOffset * 1.0);
      cbx.Add_SubSong(songlet1, 0, 1, LoudnessOffset * 1.0);
      songlet0 = cbx;
    }
    cbx.MyName = "TopChord";
    return cbx;
  }
  /* ********************************************************************************* */
  public GroupBox Compose_Warble_Chorus() {
    GroupBox cbx = new GroupBox();
    cbx.Set_Project(this);
    this.AudioRoot = cbx.Spawn_OffsetBox();

    if (false) {
      Voice vc0 = Create_Voice(0, 0, 1);
      cbx.Add_SubSong(vc0, 0, 3, 0.2);

      Voice vc1 = Create_Voice(0, 0, 1);
      cbx.Add_SubSong(vc1, 2, 0, 1);
    }
    Voice vc2 = Create_Warble_Voice(0, 0, 1);
    cbx.Add_SubSong(vc2, 0, 1.3, 1);

    return cbx;
  }
  /* ********************************************************************************* */
  public void Compose_Test() {
    ISonglet song = null;
    OffsetBox obox = null;
    GroupBox CMinor, CMajor, DMajor, DMinor;
    switch (6) {
      case 0:
        song = Create_Random_Chorus(0, 0, 1.0);
        obox = song.Spawn_OffsetBox();
        obox.OctaveLoc_s(4);
        break;
      case 1:
        song = Create_Nested_Chorus(0, 0, 1.0, 6);
        obox = song.Spawn_OffsetBox();
        break;
      case 2:
        song = Create_Chord(0, 2, 1.0, 3);
        obox = song.Spawn_OffsetBox();
        break;
      case 3:
        //song = Create_Simple_Note(0, 2.3, 1);
        song = Create_Simple_Note(0, 1, 5, 1);
        //song = NoteMaker.Create_Simple_Note(0, 1, 5, 1);
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
        GroupBox cbx = new GroupBox();
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
        lbx.Set_Duration(30);

        song = lbx;
        song.Set_Project(this);
        obox = song.Spawn_OffsetBox();
        obox.OctaveLoc_s(4);
        break;
      case 6:
        song = Compose_Warble_Chorus();
        song.Set_Project(this);
        obox = song.Spawn_OffsetBox();
        obox.OctaveLoc_s(4);
    }

//    this.AudioRoot = obox;
//    this.GraphicRoot.Attach_Content(this.AudioRoot);
//    this.Update_Guts();
    Wrap_For_Graphics(obox);
    if (false) {
      Audio_Test();
    }
    if (false) {
      Render_Test();
    }
  }
  /* ********************************************************************************* */
  public void Audio_Test() {
    this.SampleRate = Globals.SampleRate;
    Singer RootPlayer = this.AudioRoot.Spawn_Singer();
    RootPlayer.Compound(this.AudioRoot);

    double FinalTime = this.AudioRoot.GetContent().Get_Duration();

    Wave wave_render = new Wave();
    wave_render.Init(0, FinalTime, SampleRate);
    Wave wave_scratch = new Wave();

    long StartTime, EndTime;
    RootPlayer.Start();
    StartTime = System.currentTimeMillis();

    Audio aud = new Audio();
    if (true) {
      aud.SaveAudioChunks("test.wav", this.AudioRoot);
    }
    aud.Start();
    int NumSlices = 200;
    for (int cnt = 0; cnt < NumSlices; cnt++) {
      System.out.print("cnt:" + cnt + " ");
      double FractAlong = (((double) (cnt + 1)) / (double) NumSlices);
      RootPlayer.Render_To(FinalTime * FractAlong, wave_scratch);
      //wave_scratch.Normalize();
      wave_scratch.Amplify(0.2);
      //wave_render.Append(wave_scratch);
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
    Singer RootPlayer = this.AudioRoot.Spawn_Singer();
    RootPlayer.Compound(this.AudioRoot);

    double FinalTime = this.AudioRoot.GetContent().Get_Duration();

//    int nsamps = this.AudioRoot.GetContent().Get_Sample_Count(this.SampleRate);
//    wave_render.Init(nsamps);
    Wave wave_render = new Wave();
    wave_render.Init(0, FinalTime, SampleRate);
    wave_render.Fill(Wave.Debug_Fill);
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

    if (false) {
      SaveWave(wave_render, "wave_render.csv");
    }
    boolean nop = true;
  }
}
