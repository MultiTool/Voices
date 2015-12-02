/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import java.util.Random;

/**
 *
 * @author MultiTool
 */
public class Globals {
  public static int SampleRate = 44100;
  public static int SampleRateTest = 100;
  public static double BaseFreqC0 = 16.3516;// hz
  public static double BaseFreqA0 = 27.5000;// hz
  public static double TwoPi = Math.PI * 2.0;// hz
  public static double Fudge = 0.00000000001;
  public static Random RandomGenerator = new Random();

  public class Notes {
    public double Cn, Cs, Dn, Ds, En, Fn, Fs, Gn, Gs, An, As, Bn;// naturals and sharps
    public int NumNotes = 12;
    public double[] NoteRatios;
    public void Init() {
      NoteRatios = new double[NumNotes];
      for (int notecnt = 0; notecnt < NumNotes; notecnt++) {
        NoteRatios[notecnt] = Math.pow(2.0, ((double) notecnt) / ((double) NumNotes));
      }
      this.Cn = NoteRatios[0];
      this.Cs = NoteRatios[1];
      this.Dn = NoteRatios[2];
      this.Ds = NoteRatios[3];
      this.En = NoteRatios[4];
      this.Fn = NoteRatios[5];
      this.Fs = NoteRatios[6];
      this.Gn = NoteRatios[7];
      this.Gs = NoteRatios[8];
      this.An = NoteRatios[9];
      this.As = NoteRatios[10];
      this.Bn = NoteRatios[11];
    }
  }
}
