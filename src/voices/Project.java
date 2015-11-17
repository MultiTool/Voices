/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import voices.ISonglet.Singer;
import static voices.Voices.SaveWave;

/**
 *
 * @author MultiTool
 */
public class Project {
  OffsetBox rootbox;
  public int SampleRate = 100;// Globals.SampleRate
  /* ********************************************************************************* */
  public Project() {
    this.Compose_Test();
  }
  /* ********************************************************************************* */
  public void Compose_Chorus_Test() {
    ChorusBox cbx = new ChorusBox();
    cbx.Set_Project(this);
    this.rootbox = cbx.Spawn_OffsetBox();

    Voice rootvoice = new Voice();
    cbx.Add_SubSong(rootvoice);
    {
      rootvoice.Add_Note(1, 4, 1);
      rootvoice.Add_Note(8, 1, 0.5);
      rootvoice.Add_Note(16, 4, 1);
    }
    rootvoice.Recalc_Line_SubTime();
    cbx.Update_Durations();
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
    rootvoice.Recalc_Line_SubTime();
  }
  /* ********************************************************************************* */
  public void Render_Test() {
    Singer RootPlayer = this.rootbox.Spawn_Singer();
    // RootPlayer.MyProject = this; // not needed
    // RootPlayer.Compound(this.rootbox);

    double FinalTime = this.rootbox.GetContent().Get_Duration();

//    int nsamps = this.rootbox.GetContent().Get_Sample_Count(this.SampleRate);
//    wave_render.Init(nsamps);
    Wave wave_render = new Wave();

    long StartTime, EndTime;
    RootPlayer.Start();
    StartTime = System.currentTimeMillis();
    ///RootPlayer.Skip_To(1.2);
    //RootPlayer.Render_To(4, wave_render);
    RootPlayer.Skip_To(4.29);
    RootPlayer.Render_To(0.5, wave_render);
    RootPlayer.Render_To(FinalTime - 4, wave_render);
    RootPlayer.Render_To(FinalTime - 0, wave_render);

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
    vc.Recalc_Line_SubTime();

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
