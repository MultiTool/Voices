package voices;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.*;
//import sun.audio.ContinuousAudioDataStream;

/**
  
 @author MultiTool
   
 Audio I/O, work in progress, not testable yet.  
   
 The plan is for the main program to start this up like a wood chipper, asynchronously feed chunks of wave data to it while it's running, and shut this down when the main is finished. 
   
 Derived from http://www.cs.princeton.edu/introcs/stdlib/StdAudio.java.html 
 For additional documentation, see <a href="http://www.cs.princeton.edu/introcs/15inout">Section 1.5</a> of
 Introduction to Programming in Java: An Interdisciplinary Approach by Robert Sedgewick and Kevin Wayne.
 Also see 
 https://docs.oracle.com/javase/6/docs/api/javax/sound/sampled/SourceDataLine.html
 https://docs.oracle.com/javase/6/docs/api/javax/sound/sampled/DataLine.html
   
 */
public class Audio implements IDeletable {
  public int SampleRate = 44100;
  public int BitsPerSample = 16;
  int BytesPerSample;
  double HalfSample;// 127.0 for 8 bit, 32767.0 for 16 bit
  SourceDataLine source = null;
  /* ********************************************************************************* */
  public Audio() {
    this.Create_Me();
  }
  /* ********************************************************************************* */
  public void Start() {
    this.BytesPerSample = BitsPerSample / Byte.SIZE;
    // this.HalfSample = (2 ^ (BitsPerSample - 1)) - 1;// 127.0 for 8 bit, 32767.0 for 16 bit

    this.HalfSample = (1 << (BitsPerSample - 1)) - 1;// 127.0 for 8 bit, 32767.0 for 16 bit

    if (source != null) {
      source.write(new byte[0], 0, 0);
      source.stop();
      source.close();
    }
    AudioFormat af = new AudioFormat((float) SampleRate, BitsPerSample, 1, true, true);
    DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
    try {
      source = (SourceDataLine) AudioSystem.getLine(info);
      source.open(af);
      source.start();

      // http://www.javaworld.com/article/2077521/learn-java/java-tip-24--how-to-play-audio-in-applications.html
      // ContinuousAudioDataStream cas = new ContinuousAudioDataStream (data);
      // http://web.deu.edu.tr/doc/oreily/java/awt/ch14_05.htm
      // ByteArrayInputStream
    } catch (Exception e) {
      System.out.println(e);
    }
  }
  /* ********************************************************************************* */
  public void Stop() {
    source.drain();
    source.stop();
    source.close();
  }
  /* ********************************************************************************* */
  public void Feed(Wave wave) {
    double seconds;
    if (wave == null) {
      return;
    }
    seconds = ((double) wave.NumSamples) / (double) SampleRate;
    byte[] buf = new byte[(wave.NumSamples * BytesPerSample)];
    double Damper = 1.0;// 1.0;// 0.5;//0.75
    double amplitude;
    int iamp, StartBit = BitsPerSample - Byte.SIZE;
    int bufcnt = 0;
    for (int scnt = 0; scnt < wave.NumSamples; scnt++) {
      amplitude = (wave.Get(scnt) * Damper * HalfSample);// wave is assumed to be within the range of -1.0 to +1.0 before we start playing it. 
      iamp = (int) amplitude;
      if (true) {
        buf[bufcnt] = (byte) (iamp >> Byte.SIZE);// most significant byte
        bufcnt++;
        buf[bufcnt] = (byte) (iamp & 0xff);// least significant byte
        bufcnt++;
      } else {
        for (int bcnt = StartBit; bcnt >= 0; bcnt -= Byte.SIZE) {
          buf[bufcnt] = (byte) ((iamp >> bcnt) & 0xff);// most significant byte to least significant byte
          bufcnt++;
        }
      }
    }
    source.write(buf, 0, buf.length);
    //source.drain(); source.stop(); source.close();
  }
  /* ********************************************************************************* */
  public void Sing_Wave(Wave wave) {
    // SampleRate = 8000; BitsPerSample = 8;
    this.BytesPerSample = BitsPerSample / Byte.SIZE;
    this.HalfSample = (2 ^ (BitsPerSample - 1)) - 1;// 127.0 for 8 bit, 32767.0 for 16 bit
    double seconds;
    if (wave == null) {
      return;
    }
    seconds = ((double) wave.NumSamples) / (double) SampleRate;
    if (source != null) {
      source.write(new byte[0], 0, 0);
      source.stop();
      source.close();
    }
    try {
      AudioFormat af = new AudioFormat((float) SampleRate, BitsPerSample, 1, true, true);
      DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
      source = (SourceDataLine) AudioSystem.getLine(info);
      source.open(af);
      source.start();
      byte[] buf = new byte[(int) (SampleRate * seconds * BytesPerSample)];
      double Damper = 0.5;//0.75
      double amplitude = 1.0;
      int iamp;
      int bufcnt = 0;
      for (int scnt = 0; scnt < wave.NumSamples; scnt++) {
        amplitude = (wave.Get(scnt) * Damper * HalfSample);// wave is assumed to be within the range of -1.0 to +1.0 before we start playing it. 
        iamp = (int) amplitude;
//        buf[bufcnt] = (byte) (iamp >> Byte.SIZE );// most significant byte
//        bufcnt++;
//        buf[bufcnt] = (byte) (iamp & 0xff);// least significant byte
//        bufcnt++;
        for (int bcnt = BitsPerSample - Byte.SIZE; bcnt <= 0; bcnt -= Byte.SIZE) {
          buf[bufcnt] = (byte) ((iamp >> bcnt) & 0xff);// most significant byte to least significant byte
          bufcnt++;
        }
      }
      source.write(buf, 0, buf.length);
      source.drain();
      source.stop();
      source.close();
    } catch (Exception e) {
      System.out.println(e);
    }
  }
  /* ********************************************************************************* */
  public void SaveAudioChunks(String filename, OffsetBox SongHandle) {
    ISonglet.Singer RootPlayer = SongHandle.Spawn_Singer();
    RootPlayer.Compound(SongHandle);

    double FinalTime = SongHandle.GetContent().Get_Duration();
    Wave wave_render = new Wave();
    wave_render.Init(0, FinalTime, SampleRate);
    wave_render.Fill(Wave.Debug_Fill);
    Wave wave_scratch = new Wave();

    long StartTime, EndTime;
    RootPlayer.Start();
    StartTime = System.currentTimeMillis();

    int NumSlices = 16;
    for (int cnt = 0; cnt < NumSlices; cnt++) {
      double FractAlong = (((double) (cnt + 1)) / (double) NumSlices);
      RootPlayer.Render_To(FinalTime * FractAlong, wave_scratch);
      wave_render.Append(wave_scratch);
    }
    wave_render.ZeroCheck();
    wave_render.Normalize();
    wave_render.Amplify(0.99);
    this.Save(filename, wave_render.GetWave());
  }
  /* ********************************************************************************* */
  public void SaveAudio(String filename, OffsetBox SongHandle) {
    ISonglet.Singer RootPlayer = SongHandle.Spawn_Singer();
    RootPlayer.Compound(SongHandle);

    double FinalTime = SongHandle.GetContent().Get_Duration();

    Wave wave_render = new Wave();
    wave_render.Init(0, FinalTime, SampleRate);

    RootPlayer.Start();
    RootPlayer.Render_To(FinalTime, wave_render);
    wave_render.Normalize();
    wave_render.Amplify(0.99);
    this.Save(filename, wave_render.GetWave());
  }
  /* ********************************************************************************* */
  /**
   * Read audio samples from a file (in .wav or .au format) and return them as a double array
   * with values between -1.0 and +1.0.
   */
  public static void Read(String filename, Wave wave) {
    //byte[] data = ReadBytes(filename);
    byte[] data = null;
    float FrameRate = -1;
    AudioInputStream ais = null;
    try {
      //URL url = Audio.class.getResource(filename);
      File file = new File(filename);
      ais = AudioSystem.getAudioInputStream(file);

      AudioFormat fmt = ais.getFormat();
      FrameRate = fmt.getFrameRate();

      data = new byte[ais.available()];
      ais.read(data);
    } catch (Exception e) {
      System.out.println(e.getMessage());
      throw new RuntimeException("Could not read " + filename);
    }
    int NumBytes = data.length;
    int NumSamples = NumBytes / 2;
    wave.Init(NumSamples, (int) FrameRate);
    // wave.SampleRate = (int) FrameRate;

    double val;
    for (int scnt = 0; scnt < NumSamples; scnt++) {
      val = ((short) (((data[2 * scnt + 1] & 0xFF) << 8) + (data[2 * scnt] & 0xFF))) / ((double) Short.MAX_VALUE);
      wave.Set(val);
    }
  }
  /* ********************************************************************************* */
  private static byte[] ReadBytes(String filename) {// return data as a byte array
    byte[] data = null;
    AudioInputStream ais = null;
    try {
      URL url = Audio.class.getResource(filename);
      ais = AudioSystem.getAudioInputStream(url);

      AudioFormat fmt = ais.getFormat();
      float FrameRate = fmt.getFrameRate();

      data = new byte[ais.available()];
      ais.read(data);
    } catch (Exception e) {
      System.out.println(e.getMessage());
      throw new RuntimeException("Could not read " + filename);
    }
    return data;
  }
  /* ********************************************************************************* */
  public static void Load(String filename, Wave wave) {
    // wave.GetWave() = Audio.Read(filename);
    int totalFramesRead = 0;// http://stackoverflow.com/questions/3297749/java-reading-manipulating-and-writing-wav-files
    File fileIn = new File(filename);// filename is a pre-existing string whose value was based on a user selection.
    try {
      AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(fileIn);
      AudioFormat fmt = audioInputStream.getFormat();
      int bytesPerFrame = fmt.getFrameSize();
      if (bytesPerFrame == AudioSystem.NOT_SPECIFIED) {
        // some audio formats may have unspecified frame size
        // in that case we may Read any amount of bytes
        bytesPerFrame = 1;
      }
      // Set an arbitrary buffer size of 1024 frames.
      int numBytes = 1024 * bytesPerFrame;
      byte[] audioBytes = new byte[numBytes];
      try {
        int numBytesRead = 0;
        int numFramesRead = 0;
        // Try to Read numBytes bytes from the file.
        while ((numBytesRead = audioInputStream.read(audioBytes)) != -1) {
          // Calculate the number of frames actually Read.
          numFramesRead = numBytesRead / bytesPerFrame;
          totalFramesRead += numFramesRead;
          // Here, do something useful with the audio data that's 
          // now in the audioBytes array...
        }
      } catch (Exception ex) {
        // Handle the error...
        System.out.println("nop");
      }
    } catch (Exception e) {
      // Handle the error...
      System.out.println("nop");
    }
  }
  /* ********************************************************************************* */
  /**
   * Save the double array as a sound file (using .wav or .au format).
   * 
   */
  public void Save(String filename, double[] input) {
    //double[] input;
    // assumes 44,100 samples per second
    // use 16-bit audio, mono, signed PCM, little Endian
    double MAX_16_BIT = Short.MAX_VALUE;     // 32,767
    AudioFormat format = new AudioFormat(this.SampleRate, 16, 1, true, false);
    byte[] data = new byte[2 * input.length];
    for (int scnt = 0; scnt < input.length; scnt++) {
      int temp = (short) (input[scnt] * MAX_16_BIT);
      data[2 * scnt + 0] = (byte) temp;
      data[2 * scnt + 1] = (byte) (temp >> Byte.SIZE);
    }
    try {// now Save the file
      ByteArrayInputStream bais = new ByteArrayInputStream(data);
      AudioInputStream ais = new AudioInputStream(bais, format, input.length);
      if (filename.toLowerCase().endsWith(".wav")) {
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(filename));
      } else if (filename.toLowerCase().endsWith(".au")) {
        AudioSystem.write(ais, AudioFileFormat.Type.AU, new File(filename));
      } else {
        throw new RuntimeException("File format not supported: " + filename);
      }
    } catch (Exception e) {
      System.out.println(e);
      System.exit(1);
    }
  }
  public void Launch(Wave wave0) {
    Thread_Sounder ss = new Thread_Sounder();
    ss.wave = wave0;
    ss.start();
  }
  /* ********************************************************************************* */
  @Override public boolean Create_Me() {// IDeletable
    return true;
  }
  @Override public void Delete_Me() {// IDeletable
    if (this.source != null) {
      this.source.close();
      this.source = null;
    }
  }
  /* ********************************************************************************* */
  public class Thread_Sounder extends Thread {
    public Wave wave;
    public void Launch(Wave wave0) {
      this.wave = wave0;
      this.start();
    }
    @Override public void run() {
      Sing_Wave(wave);
    }
  }
}
