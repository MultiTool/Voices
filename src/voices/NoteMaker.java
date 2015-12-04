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
public class NoteMaker {
  public static int NumNotes = 12;
  public static double SemitoneFraction = (1.0 / (double) NumNotes);
  /* ********************************************************************************* */
  public double Cn, Cs, Dn, Ds, En, Fn, Fs, Gn, Gs, An, As, Bn;// naturals and sharps
  public double[] NoteRatios;
  /* ********************************************************************************* */
  public static class Note {
    public int Dex;
    public double Octave;
    public String Name;
  }
  /* ********************************************************************************* */
  public NoteMaker() {
    this.Init();
  }
  /* ********************************************************************************* */
  public Voice Create_Simple_Note(double TimeOffset, double Duration, double OctaveOffset, double LoudnessOffset) {
    Voice voice = new Voice();
    voice.Add_Note(TimeOffset + 0, OctaveOffset + 0, LoudnessOffset * 1);
    voice.Add_Note(TimeOffset + Duration, OctaveOffset - 0.1, LoudnessOffset * 1);
    return voice;
  }
  /* ********************************************************************************* */
  public ChorusBox Create_Triad(ISonglet s0, ISonglet s1, ISonglet s2) {
    ChorusBox cbx = new ChorusBox();
    for (int cnt = 0; cnt < NumNotes; cnt++) {
      cbx.Add_SubSong(s0, 0, 0, 1.0);
      cbx.Add_SubSong(s1, 0, 0, 1.0);
      cbx.Add_SubSong(s2, 0, 0, 1.0);
    }
    cbx.Sort_Me();
    return cbx;
  }
  /* ********************************************************************************* */
  public ChorusBox Create_Triad(int NoteDex0, int NoteDex1, int NoteDex2) {
    double Loudness = 1.0;// NoteDex0 is usually the key
    double Duration = 1.0;
    ISonglet note;
//    NoteDex0 %= NumNotes;
//    NoteDex1 %= NumNotes;
//    NoteDex2 %= NumNotes;
    ChorusBox cbx = new ChorusBox();
    note = Create_Simple_Note(0, Duration, 0, Loudness);
    cbx.Add_SubSong(note, 0, SemitoneFraction * NoteDex0, Loudness);
    note = Create_Simple_Note(0, Duration, 0, Loudness);
    cbx.Add_SubSong(note, 0, SemitoneFraction * NoteDex1, Loudness);
    note = Create_Simple_Note(0, Duration, 0, Loudness);
    cbx.Add_SubSong(note, 0, SemitoneFraction * NoteDex2, Loudness);
    return cbx;
  }
  /* ********************************************************************************* */
  public ChorusBox MakeMajor(int Key) {
    // int FirstNote = Key; int SecondNote = (Key + 4) % NumNotes; int ThirdNote = (Key + 7) % NumNotes;
//    ISonglet songlet = Create_Triad(Key, (Key + 4), (Key + 7));
//    OffsetBox obx = songlet.Spawn_OffsetBox();
//    obx.OctaveLoc_s(Octave);
//    return songlet;
    return Create_Triad(Key, (Key + 4), (Key + 7));
  }
  /* ********************************************************************************* */
  public ChorusBox MakeMinor(int Key) {
    return Create_Triad(Key, (Key + 3), (Key + 7));
  }
  /* ********************************************************************************* */
  public static class Indexes {
    public static int Cn = 0, Cs = 1, Dn = 2, Ds = 3, En = 4, Fn = 5, Fs = 6, Gn = 7, Gs = 8, An = 9, As = 10, Bn = 11;// naturals and sharps' indexes
  }
  /* ********************************************************************************* */
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
