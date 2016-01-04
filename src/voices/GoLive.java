/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author MultiTool
 */
public class GoLive implements Runnable, IDeletable {
  private Thread thread;
  private final String ThreadName;
  public Project MyProject;
  //public double StartTime = 0;
  Audio audio;
  ISonglet.Singer RootPlayer;
  double TimeIncrement = (20.0 / 1000.0);// milliseconds
  double CurrentTime = 0, FinalTime;
  Wave wave_render = new Wave();
  boolean KeepGoing;
  /* ********************************************************************************* */
  public GoLive() {
    this.ThreadName = "GoLive";
    this.thread = null;
    this.audio = new Audio();
    this.CurrentTime = 0;
    this.Create_Me();
  }
  /* ********************************************************************************* */
  public void start() {
    System.out.println("Starting " + ThreadName);
    //this.CurrentTime = 0;
    //FinalTime = this.MyProject.AudioRoot.GetContent().Get_Duration();
    wave_render.Init(0, TimeIncrement, this.MyProject.SampleRate);

    RootPlayer = this.MyProject.AudioRoot.Spawn_Singer();
    RootPlayer.Compound(this.MyProject.AudioRoot);
    RootPlayer.Start();
    RootPlayer.Skip_To(this.CurrentTime);

    this.KeepGoing = true;

    this.audio.Start();
    if (thread == null) {
      thread = new Thread(this, ThreadName);
    }
    thread.start();
  }
  /* ********************************************************************************* */
  public void Play_All() {
    this.CurrentTime = 0;
    FinalTime = this.MyProject.AudioRoot.GetContent().Get_Duration();
    this.start();
  }
  /* ********************************************************************************* */
  public void Skip_To(double Time) {
    this.CurrentTime = Time;
  }
  /* ********************************************************************************* */
  public void Assign_Stop_Time(double Time) {
    //this.CurrentTime = Time;
    FinalTime = Math.max(this.CurrentTime, Time);
  }
  /* ********************************************************************************* */
  @Override public void run() {
    while (this.CurrentTime < this.FinalTime && this.KeepGoing) {
      this.wave_render.Rebase_Time(this.CurrentTime);
      this.RootPlayer.Render_To(CurrentTime, this.wave_render);
      this.wave_render.Amplify(0.2);
      audio.Feed(this.wave_render);
      this.CurrentTime += this.TimeIncrement;
      if (this.RootPlayer.IsFinished) {
        break;
      }
    }
    this.stop();
  }
  /* ********************************************************************************* */
  public boolean IsRunning() {
    return !this.KeepGoing;// do not use this it is broken
  }
  /* ********************************************************************************* */
  public void PleaseStop() {// polite stopping without interrupt
    this.KeepGoing = false;
  }
  /* ********************************************************************************* */
  public void stop() {
    thread = null;// is there no way to reset a thread without destroying it? 
    audio.Stop();
    this.RootPlayer.Delete_Me();
  }
  /* ********************************************************************************* */
  public void Audio_Test() {// using this for scrap
    RootPlayer = this.MyProject.AudioRoot.Spawn_Singer();
    RootPlayer.Compound(this.MyProject.AudioRoot);

    FinalTime = this.MyProject.AudioRoot.GetContent().Get_Duration();

    Wave wave_render = new Wave();
    wave_render.Init(0, FinalTime, this.MyProject.SampleRate);
    Wave wave_scratch = new Wave();

    long StartTime, EndTime;
    RootPlayer.Start();
    StartTime = System.currentTimeMillis();

    audio.Start();
    int NumSlices = 200;
    for (int cnt = 0; cnt < NumSlices; cnt++) {
      System.out.print("cnt:" + cnt + " ");
      double FractAlong = (((double) (cnt + 1)) / (double) NumSlices);
      RootPlayer.Render_To(FinalTime * FractAlong, wave_scratch);
      wave_scratch.Amplify(0.2);
      audio.Feed(wave_scratch);
      System.out.println(" done.");
    }
    audio.Stop();
    EndTime = System.currentTimeMillis();
    System.out.println("Audio_Test time:" + (EndTime - StartTime));// Render_To time: 150 milliseconds per 16 seconds. 
  }
  /* ********************************************************************************* */
  @Override public boolean Create_Me() {// IDeletable
    return true;
  }
  @Override public void Delete_Me() {// IDeletable
    this.stop();
    this.audio.Delete_Me();
  }
}
