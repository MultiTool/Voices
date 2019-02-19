package voices;

import java.util.ArrayList;
import java.util.List;
import voices.DummySong.*;

/* ********************************************************************************* */
/* ********************************************************************************* */

class LoopSong extends GroupSong {
/*
general plan:
create a groupsong where all children are the same songlet, spaced evenly in time.
any update to the location of one child triggers a callback to keep them all evenly spaced/rhythmic.
DummySong is the most rigorous approach but inherits too much interface junk to fill in.  Is it needed, can we strip it out?
*/
  /* ********************************************************************************* */
  public static class Loop_OffsetBox extends Group_OffsetBox {
    public static final String ObjectTypeName = "Loop_OffsetBox";
  };
  /* ********************************************************************************* */
  DummySong SingleSong = new DummySong();// the one child song that gets repeated, which in turn owns the custom song we are given.
  double Interval = 0.25;// time spacing
  /* ********************************************************************************* */
  LoopSong() {
    super();
    this.SingleSong.GrandParentLoop = this;
    this.Create_Me();
  }
  //~LoopSong(){ this.Delete_Me(); }
  /* ********************************************************************************* */
  void Set_Interval(double Interval0){
    this.Interval = Interval0;
    OffsetBox obox;
    int NumKids = this.SubSongs.size();
    for (int cnt=0;cnt<NumKids;cnt++){
      obox = this.SubSongs.get(cnt);
      obox.TimeX = this.Interval * (double)cnt;
    }
  }
  /* ********************************************************************************* */
  void Set_Beats(int NumBeats){
    DummySong.Dummy_OffsetBox dsobox;
    OffsetBox RepeatHandle;
    int PrevSize = this.SubSongs.size();
    if (NumBeats>PrevSize){// add beats
      //this.SubSongs.resize(NumBeats);
      this.SubSongs.ensureCapacity(NumBeats);
      for (int cnt=PrevSize;cnt<NumBeats;cnt++){
        dsobox = this.SingleSong.Spawn_OffsetBox();
        dsobox.MyIndex = cnt;
        dsobox.TimeX = Interval * (double)cnt;// to do: Assign dsobox OctaveY and TimeX here
        dsobox.OctaveY = 0.0;// snox is this good enough?
        //this.SubSongs.set(cnt, dsobox);
        this.SubSongs.add(dsobox);
      }
    } else if (NumBeats<PrevSize){// remove beats
      for (int cnt=NumBeats;cnt<PrevSize;cnt++){
        RepeatHandle = this.SubSongs.get(cnt);
        RepeatHandle.Delete_Me();
      }
      Globals.ShrinkList(this.SubSongs, NumBeats);//this.SubSongs.resize(NumBeats);
      //this.SubSongs.subList(NumBeats, PrevSize).clear();
      //this.SubSongs = new ArrayList<OffsetBox>(this.SubSongs.subList(0, NumBeats));
    }
    Refresh_Splines();
  }
  /* ********************************************************************************* */
  @Override public int Add_SubSong(OffsetBox obox) {
    int dex = 0;// When someone gives me an OffsetBox, I own it.  I must delete it.
    if (this.SingleSong.ChildObox != null) {
      this.SingleSong.ChildObox.Delete_Me();// It is my responsibility to delete the handle I was given.
    }
    this.SingleSong.ChildObox = obox;// Now assign the new handle.
    obox.MyParentSong = this.SingleSong;
    this.SingleSong.ChildSong = obox.GetContent();
    return dex;
  }
  /* ********************************************************************************* */
  public void Update_Rhythm(Dummy_OffsetBox mbox) {
    int Index = mbox.MyIndex;// like Refresh_Me_From_Beneath but specific for Dummy_OffsetBox.
    this.Interval = mbox.TimeX / (double)Index;
    OffsetBox obox;// = this.SubSongs.get(Index);
    for (int cnt=0;cnt<Index;cnt++){
      obox = this.SubSongs.get(cnt);
      obox.OctaveY = mbox.OctaveY;
      obox.TimeX = this.Interval * (double)cnt;
    }
    Index++;
    for (int cnt=Index;cnt<this.SubSongs.size();cnt++){
      obox = this.SubSongs.get(cnt);
      obox.OctaveY = mbox.OctaveY;
      obox.TimeX = this.Interval * (double)cnt;
    }
  }
  /* ********************************************************************************* */
  @Override public void Refresh_Me_From_Beneath(IDrawable.IMoveable mbox) {
    double XLoc = mbox.GetX();
    double YLoc = mbox.GetY();
    int NumSubSongs = this.SubSongs.size();
    for (int cnt = 0; cnt < NumSubSongs; cnt++) {
      OffsetBox obx = this.SubSongs.get(cnt);
      ISonglet songlet = obx.GetContent();
      //obx.TimeX =
      obx.OctaveY = YLoc;
    }
  }
  /* ********************************************************************************* */
  @Override public Loop_OffsetBox Spawn_OffsetBox() {
    Loop_OffsetBox lbox = new Loop_OffsetBox();// Spawn an OffsetBox specific to this type of songlet.
    lbox.Attach_Songlet(this);
    return lbox;
  }
  /* ********************************************************************************* */
  @Override public void Delete_Me(){
    super.Delete_Me();// multiple references to SingleSong will unref and delete SingleSong when they are all gone.
    //this.SingleSong.Delete_Me();
  }
};
