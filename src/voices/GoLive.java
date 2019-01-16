package voices;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author MultiTool
 */
public class GoLive implements Runnable, IDeletable {
  private Thread thread;
  private String ThreadName;
  public AudProject MyProject;
  //public double StartTime = 0;
  Audio audio;
  ISonglet.Singer RootPlayer;
//  double TimeIncrement = (20.0 / 1000.0);// milliseconds
  double TimeIncrement = (50 / 1000.0);// milliseconds
  //double TimeIncrement = (250.0 / 1000.0);// milliseconds
  //double TimeIncrement = (500.0 / 1000.0);// milliseconds
  double CurrentTime = 0, FinalTime;
  Wave wave_render = new Wave();
  double Current_Amp_Factor = 0.2;
  boolean KeepGoing;
  /* ********************************************************************************* */
  //public boolean IsRunning = false;
  public int NumRunning = 0;
  private LivePlayerCallbacks MyCallback = null;
//  {
//    return !this.KeepGoing;// do not use this it is broken
//  }
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

    this.PleaseStop();
    while (this.RootPlayer != null) {// block until the singer is deleted
      System.out.println("Blocking until RootPlayer is null");
    }
    //this.CurrentTime = 0;
    //FinalTime = this.MyProject.AudioRoot.GetContent().Get_Duration();
    wave_render.Init_Time(0, TimeIncrement, this.MyProject.SampleRate);

    RootPlayer = this.MyProject.AudioRoot.Spawn_Singer();
    RootPlayer.Compound(this.MyProject.AudioRoot);
    RootPlayer.Start();
    RootPlayer.Skip_To(this.CurrentTime);
    this.audio.Start();
    thread = new Thread(this, ThreadName + Globals.RandomGenerator.nextDouble());
    this.KeepGoing = true;
    try {
      thread.start();
    } catch (Exception ex) {
      boolean nop = true;
    }
  }
  /* ********************************************************************************* */
  public void start(OffsetBox obx, double StartTime, double EndTime) {
    //public void start(OffsetBox obx) {
    System.out.println("Starting " + ThreadName);
    this.PleaseStop();
    while (this.RootPlayer != null) {// block until the singer is deleted
      System.out.println("Blocking until RootPlayer is null");
    }

    this.CurrentTime = StartTime;// obx.UnMapTime(0);
    this.FinalTime = EndTime;// obx.UnMapTime(obx.GetContent().Get_Duration());

    wave_render.Init_Time(0, TimeIncrement, this.MyProject.SampleRate);
    RootPlayer = obx.Spawn_Singer();
    RootPlayer.Compound(obx);
    RootPlayer.Start();
    RootPlayer.Skip_To(StartTime);
    this.Current_Amp_Factor = 0.2;

    this.audio.Start();
    thread = new Thread(this, ThreadName + Globals.RandomGenerator.nextDouble());
    System.out.println("thread.getName():" + thread.getName());
    this.KeepGoing = true;
    try {
      thread.start();
    } catch (Exception ex) {
      boolean nop = true;
    }
  }
  /* ********************************************************************************* */
  public void Play_Branch(OffsetBox obx) {
    double StartTime = obx.UnMapTime(0);// obx is assumed to map between songlet and global
    double EndTime = obx.UnMapTime(obx.GetContent().Get_Duration());
    this.start(obx, StartTime, EndTime);
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
  void CheckMax() {// quiet the sound to keep it from clipping during live playback
    double Amplitude = this.wave_render.GetMaxAmp();// to do: make this transition smoothly to each new de-amplification
    if (this.Current_Amp_Factor * Amplitude > 1.0) {
      this.Current_Amp_Factor = 1.0 / Amplitude;
    }
    this.wave_render.Amplify(this.Current_Amp_Factor);
  }
  /* ********************************************************************************* */
  @Override public void run() {
    while (this.NumRunning > 0) {// block until existing threads have finished
      System.out.println("Blocking on threads:" + this.NumRunning);
    }
//    Wave master = new Wave();
//    master.Init_Sample(0, 0, this.MyProject.SampleRate, 0.3);
    this.wave_render.SampleRate = this.MyProject.SampleRate;
    this.NumRunning++;
    while (this.CurrentTime < this.FinalTime && this.KeepGoing) {
//      System.out.println("CurrentTime:" + CurrentTime + " KeepGoing:" + this.KeepGoing);
      this.wave_render = new Wave();
      this.wave_render.Init_Sample(0, 0, this.MyProject.SampleRate, 0.7);
      this.RootPlayer.Render_To(CurrentTime, this.wave_render);
      System.out.println("GoLive CurrentTime:" + CurrentTime + ", wave_render.NumSamples:" + this.wave_render.NumSamples);
//      master.Append2(this.wave_render);
      this.CheckMax();
      audio.Feed(this.wave_render);
      if (this.MyCallback != null) {
        this.MyCallback.LivePlayerUpdate(this.CurrentTime, this.CurrentTime + this.TimeIncrement);
      }
      this.CurrentTime += this.TimeIncrement;
      //this.CurrentTime = Math.min(this.CurrentTime, FinalTime - Globals.Fudge);// #kludgey
      if (this.RootPlayer.IsFinished) {
        break;
      }
    }
    if (this.KeepGoing) {
      this.wave_render = new Wave();
      this.wave_render.Init_Sample(0, 0, this.MyProject.SampleRate, 0.7);
      this.RootPlayer.Render_To(this.FinalTime, this.wave_render);// play last little bit, if any
//      master.Append2(this.wave_render);
      this.CheckMax();
      audio.Feed(this.wave_render);
    }
    this.stop();
    this.MyCallback.LivePlayerFinished();
    this.NumRunning--;
//    Audio aud = new Audio();
//    aud.Save("VPlay.wav", master.GetWave());
  }
  /* ********************************************************************************* */
  public void PleaseStop() {// polite stopping without interrupt
    this.KeepGoing = false;
  }
  /* ********************************************************************************* */
  public void stop() {
    this.PleaseStop();
    thread = null;// is there no way to reset a thread without destroying it? 
    audio.Stop();
    if (this.RootPlayer != null) {
      this.RootPlayer.Delete_Me();
      this.RootPlayer = null;
    }
  }
  /* ********************************************************************************* */
  @Override public boolean Create_Me() {// IDeletable
    return true;
  }
  @Override public void Delete_Me() {// IDeletable
    this.stop();
    this.audio.Delete_Me();
    this.thread = null;
    this.ThreadName = null;
    this.MyProject = null;
    if (this.RootPlayer != null) {
      this.RootPlayer.Delete_Me();
      this.RootPlayer = null;
    }
    this.TimeIncrement = this.CurrentTime = this.FinalTime = Double.NEGATIVE_INFINITY;
    if (this.wave_render != null) {
      this.wave_render.Delete_Me();
      this.wave_render = null;
    }
    this.KeepGoing = false;
  }
  /* ********************************************************************************* */
  public void AttachCallback(LivePlayerCallbacks cb) {
    this.MyCallback = cb;
  }
  public interface LivePlayerCallbacks {
    public void LivePlayerUpdate(double Time, double NextTime);
    public void LivePlayerFinished();
  }
}
