/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import voices.GraphicBox.Graphic_OffsetBox;
import voices.IDrawable.Drawing_Context;
import voices.ISonglet.MetricsPacket;
import voices.ISonglet.Singer;
import static voices.Voices.SaveWave;

/**
 *
 * @author MultiTool
 */
public class Project implements IDeletable {
  OffsetBox AudioRoot;
  public Graphic_OffsetBox GraphicRoot;
  GraphicBox GBox;
  public int SampleRate = Globals.SampleRate;
  /* ********************************************************************************* */
  public Project() {
    this.GBox = new GraphicBox();
    this.GraphicRoot = this.GBox.Spawn_My_OffsetBox();
  }
  /* ********************************************************************************* */
  public void Wrap_For_Graphics(OffsetBox obox) {
    obox.GetContent().Set_Project(this);
    this.AudioRoot = obox;
    this.GraphicRoot.Attach_Content(this.AudioRoot);
    this.Update_Guts();
    this.GraphicRoot.UpdateBoundingBox();
  }
  /* ********************************************************************************* */
  public void Update_Guts() {
    MetricsPacket metrics = new MetricsPacket();
    metrics.MyProject = this;
    AudioRoot.GetContent().Update_Guts(metrics);
  }
  /* ********************************************************************************* */
  public void Create_For_Graphics() {// a test
    //song = Create_Simple_Note(0, 1, 5, 1);
    ISonglet song = JunkyardTests.Create_Warble_Voice(0, 4, 1.0);
    song.Set_Project(this);
    OffsetBox obox = song.Spawn_OffsetBox();
    this.Wrap_For_Graphics(obox);
  }
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
      song = JunkyardTests.Create_Random_Chorus(0, 0, 1.0);
      obox = song.Spawn_OffsetBox();
      obox.OctaveLoc_s(4);
      break;
    case 1:
      song = JunkyardTests.Create_Nested_Chorus(0, 0, 1.0, 6);
      obox = song.Spawn_OffsetBox();
      break;
    case 2:
      song = JunkyardTests.Create_Chord(0, 2, 1.0, 3);
      obox = song.Spawn_OffsetBox();
      break;
    case 3:
      //song = Create_Simple_Note(0, 2.3, 1);
      song = JunkyardTests.Create_Simple_Note(0, 1, 5, 1);
      //song = NoteMaker.Create_Simple_Note(0, 1, 5, 1);
      song.Set_Project(this);
      obox = song.Spawn_OffsetBox();
      break;
    case 4:
      song = JunkyardTests.Compose_Loop();
      obox = song.Spawn_OffsetBox();
      break;
    case 5:
      Delay = 1.5;
      //Delay = 3;
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
      song.Set_Project(this);
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
      song.Set_Project(this);
      obox = song.Spawn_OffsetBox();
      obox.OctaveLoc_s(4);
      break;
    case 7:
      obox = JunkyardTests.Compose_Warble_Chorus();
      obox.OctaveLoc_s(4);
    case 8:
      obox = JunkyardTests.Compose_Ribbon_Chorus();
      obox.OctaveLoc_s(4);
      break;
    }
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
  }
  /* ********************************************************************************* */
  @Override public boolean Create_Me() {// IDeletable
    return true;
  }
  @Override public void Delete_Me() {// IDeletable
    this.GraphicRoot.Delete_Me();
  }
}
