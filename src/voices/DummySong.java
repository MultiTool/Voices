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
//public class DummySong {}
///* ********************************************************************************* */
//class Dummy_OffsetBox_Base extends OffsetBox {// used like a forward
//  public int MyIndex;
//};
//
///* ********************************************************************************* */
//class LoopSong_Base extends GroupSong {// used like a forward
//  /* virtual */ void Update_Rhythm(Dummy_OffsetBox_Base mbox) {};
//};

public class DummySong implements ISonglet.IContainer {
  /* ********************************************************************************* */
  public static class Dummy_OffsetBox extends OffsetBox {
    LoopSong ParentLoop;
    DummySong ContentSonglet;
    public int MyIndex;
    @Override public double Get_Max_Amplitude() {
      return this.GetContent().Get_Max_Amplitude() * this.LoudnessFactor;
    }
    /* ********************************************************************************* */
    @Override public DummySong GetContent() {// always always override this
      return this.ContentSonglet;
    }
    @Override public void MoveTo(double XLoc, double YLoc) {
      if (XLoc >= 0) { this.TimeX = XLoc; }// don't go backward in time
      this.OctaveY = YLoc;// should just make this 0.
      //this.ParentLoop.Refresh_Me_From_Beneath(this);
      this.ParentLoop.Update_Rhythm(this);
    }
    /* ********************************************************************************* */
    @Override public Singer Spawn_Singer() {// always always always override this
      Singer Singer = this.ContentSonglet.Spawn_Singer();// for render time
      Singer.MyOffsetBox = this;// Transfer all of this box's offsets to singer.
      return Singer;
    }
  };// end of Dummy_OffsetBox
  /* ********************************************************************************* */
  LoopSong GrandParentLoop;
  OffsetBox ChildObox = null;// any child obox.songlet
  ISonglet ChildSong;
  int RefCount = 0;
  /* ********************************************************************************* */
  DummySong(){this.Create_Me();}
  //~DummySong(){this.Delete_Me();}
  /* ********************************************************************************* */
  @Override public Dummy_OffsetBox Spawn_OffsetBox() {// for compose time
    Dummy_OffsetBox child = new Dummy_OffsetBox();
    child.MySonglet = child.ContentSonglet = this;
    child.ParentLoop = this.GrandParentLoop;
    return child;
  }
  /* ********************************************************************************* */
  @Override public Singer Spawn_Singer() {// for render time
    //Singer singer = ChildSong.Spawn_Singer();// totally punt this to the child
    Singer singer = ChildObox.Spawn_Singer();// totally punt this to the child. snox spawn from child or child obox?
    return singer;
  }
  /* ********************************************************************************* */
  @Override public double Get_Duration() { return ChildSong.Get_Duration(); }
  /* ********************************************************************************* */
  @Override public double Get_Max_Amplitude() { 
    return ChildSong.Get_Max_Amplitude(); 
  }
  /* ********************************************************************************* */
  @Override public void Update_Guts(ISonglet.MetricsPacket metrics) {
    ChildSong.Update_Guts(metrics);
  }
  /* ********************************************************************************* */
  @Override public void Refresh_Me_From_Beneath(IDrawable.IMoveable mbox) { /*ChildSong.Refresh_Me_From_Beneath(mbox);*/ }// should be just for IContainer types, but aren't all songs containers?
  @Override public void Remove_SubNode(MonkeyBox mbx){}
  /* ********************************************************************************* */
  // IDeletable
  @Override public boolean Create_Me() { return true; }
  @Override public void Delete_Me() {
    this.ChildObox.Delete_Me();
    this.ChildObox = null; this.GrandParentLoop = null;
    this.ChildSong = null;
  }
  /* ********************************************************************************* */
  // Below is all the crap that comes from inheriting ISonglet.  
  // to do: Clean up ISonglet so there is less inherited junk. Also really implement these remaining methods. 
  /* ********************************************************************************* */
  // ISonglet
  @Override public int Ref_Songlet(){return ++this.RefCount;}// Reference Counting: increment ref counter and return new value just for kicks
  @Override public int UnRef_Songlet(){return --this.RefCount;}// Reference Counting: decrement ref counter and return new value just for kicks
  @Override public int GetRefCount(){return this.RefCount;}// Reference Counting: get number of references for serialization
  @Override public void Set_Project(AudProject project){ChildSong.Set_Project(project);}
  /* ********************************************************************************* */
  // IDrawable
  @Override public void Draw_Me(DrawingContext ParentDC){ChildObox.Draw_Me(ParentDC);}
  @Override public CajaDelimitadora GetBoundingBox(){return ChildObox.GetBoundingBox();}
  @Override public void UpdateBoundingBox(){ChildObox.UpdateBoundingBox();}
  @Override public void UpdateBoundingBoxLocal(){ChildObox.UpdateBoundingBoxLocal();}
  @Override public void GoFishing(Grabber Scoop){ChildObox.GoFishing(Scoop);}
  /* ********************************************************************************* */
  // ICloneable 
  @Override public IDrawable Clone_Me(){return ChildSong.Clone_Me();}
  @Override public ISonglet Deep_Clone_Me(ITextable.CollisionLibrary HitTable){return ChildSong.Deep_Clone_Me(HitTable);}// should return DummySong
  /* ********************************************************************************* */
  // ITextable  
  @Override public JsonParse.HashNode Export(CollisionLibrary HitTable){return ChildObox.Export(HitTable);}
  @Override public void ShallowLoad(JsonParse.HashNode phrase){ChildObox.ShallowLoad(phrase);}
  @Override public void Consume(JsonParse.HashNode phrase, CollisionLibrary ExistingInstances){ChildObox.Consume(phrase, ExistingInstances);}
}// end of DummySong
