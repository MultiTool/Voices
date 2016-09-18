package voices;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author MultiTool
 */
public class GroupBox implements ISonglet, IDrawable {
  public ArrayList<OffsetBox> SubSongs = new ArrayList<OffsetBox>();
  public static String SubSongsName = "SubSongs";
  public double Duration = 0.0;
  private AudProject MyProject;
  public String MyName;// for debugging
  private double MaxAmplitude = 0.0;
  public CajaDelimitadora MyBounds;
  private int FreshnessTimeStamp;
  public String TraceText = "";// for debugging
  private int RefCount = 0;
  private boolean HighlightSpine = false;
  /* ********************************************************************************* */
  public GroupBox() {
    MyBounds = new CajaDelimitadora();
    RefCount = 0;
  }
  /* ********************************************************************************* */
  public OffsetBox Add_SubSong(ISonglet songlet, double TimeOffset, double OctaveOffset, double LoudnessFactor) {
    OffsetBox obox = songlet.Spawn_OffsetBox();
    if (false) {// test serialization - gibberish right now, not expected to work
      if (obox instanceof Voice.Voice_OffsetBox) {
        Voice.Voice_OffsetBox vobx = (Voice.Voice_OffsetBox) obox;// another cast!
        ITextable.CollisionLibrary HitTable = new ITextable.CollisionLibrary();
        JsonParse.Node phrase = vobx.Export(HitTable);
        HitTable.Wipe_Songlets();
        vobx.VoiceContent = null;
        obox = vobx = new Voice.Voice_OffsetBox();// #hacky: stupid non-factory way to regenerate obox
        //obox.Delete_Me();// should be enough unrefs, right?
        //obox = new WHAT??();// hmm need the right factory here
        obox.Clear();
        obox.Consume(phrase, HitTable);
      }
    }
    this.Add_SubSong(obox, TimeOffset, OctaveOffset, LoudnessFactor);
    return obox;
  }
  /* ********************************************************************************* */
  public void Add_SubSong(OffsetBox obox, double TimeOffset, double OctaveOffset, double LoudnessFactor) {// Add a songlet with its offsetbox already created.
    obox.TimeX = (TimeOffset);
    obox.OctaveY = (OctaveOffset);
    obox.LoudnessFactor = (LoudnessFactor);
    this.Add_SubSong(obox);
  }
  /* ********************************************************************************* */
  public void Add_SubSong(OffsetBox obox) {// Add a songlet with its offsetbox already created and filled out.
    obox.GetContent().Set_Project(this.MyProject);// child inherits project from me
    obox.MyParentSong = this;
    SubSongs.add(obox);
  }
  /* ********************************************************************************* */
  public void Remove_SubSong(OffsetBox obox) {// Remove a songlet from my list.
    SubSongs.remove(obox);
  }
  /* ********************************************************************************* */
  @Override public double Get_Duration() {
    return this.Duration;
  }
  /* ********************************************************************************* */
  @Override public double Get_Max_Amplitude() {
    return this.MaxAmplitude;
  }
  /* ********************************************************************************* */
  public void Update_Max_Amplitude() {
    int len = this.SubSongs.size();
    OffsetBox pnt;
    double MaxAmp = 0.0;
    for (int pcnt = 0; pcnt < len; pcnt++) {
      pnt = this.SubSongs.get(pcnt);
      double Amp = pnt.Get_Max_Amplitude();
      MaxAmp += Amp;// this is overkill, we need to only sum those subsongs that overlap.
    }
    this.MaxAmplitude = MaxAmp;
  }
  /* ********************************************************************************* */
  @Override public double Update_Durations() {
    double MaxDuration = 0.0;
    double DurBuf = 0.0;
    int NumSubSongs = this.SubSongs.size();
    for (int cnt = 0; cnt < NumSubSongs; cnt++) {
      OffsetBox ob = this.SubSongs.get(cnt);
      ISonglet child = ob.GetContent();
      //if (MaxDuration < (DurBuf = (ob.UnMapTime(vb.Update_Durations())))) {
      if (MaxDuration < (DurBuf = (ob.TimeX + child.Update_Durations()))) {
        MaxDuration = DurBuf;
      }
    }
    this.Duration = MaxDuration;
    return MaxDuration;
  }
  /* ********************************************************************************* */
  @Override public void Update_Guts(MetricsPacket metrics) {
    if (this.FreshnessTimeStamp < metrics.FreshnessTimeStamp) {// don't hit the same songlet twice on one update
      this.Set_Project(metrics.MyProject);
      this.Sort_Me();
      this.Update_Max_Amplitude();
      metrics.MaxDuration = 0.0;// redundant
      double MyMaxDuration = 0.0;
      double DurBuf = 0.0;
      int NumSubSongs = this.SubSongs.size();
      for (int cnt = 0; cnt < NumSubSongs; cnt++) {
        OffsetBox obx = this.SubSongs.get(cnt);
        ISonglet songlet = obx.GetContent();
        metrics.MaxDuration = 0.0;
        songlet.Update_Guts(metrics);
        if (MyMaxDuration < (DurBuf = (obx.TimeX + metrics.MaxDuration))) {
          MyMaxDuration = DurBuf;
        }
      }
      this.Duration = MyMaxDuration;
      this.FreshnessTimeStamp = metrics.FreshnessTimeStamp;
    }
    metrics.MaxDuration = this.Duration;
  }
  /* ********************************************************************************* */
  @Override public void Sort_Me() {// sorting by RealTime
    Collections.sort(this.SubSongs, new Comparator<OffsetBox>() {
      @Override public int compare(OffsetBox voice0, OffsetBox voice1) {
        return Double.compare(voice0.TimeX, voice1.TimeX);
      }
    });
  }
  /* ********************************************************************************* */
  @Override public Group_OffsetBox Spawn_OffsetBox() {// for compose time
    Group_OffsetBox lbox = new Group_OffsetBox();// Deliver an OffsetBox specific to this type of songlet.
    lbox.Attach_Songlet(this);
    return lbox;
  }
  /* ********************************************************************************* */
  @Override public ISonglet.Singer Spawn_Singer() {
    return this.Spawn_My_Singer();
  }
  /* ********************************************************************************* */
  public Group_Singer Spawn_My_Singer() {
    Group_Singer GroupPlayer = new Group_Singer();
    GroupPlayer.MySonglet = this;
    GroupPlayer.MyProject = this.MyProject;// inherit project
    return GroupPlayer;
  }
  /* ************************************************************************************************************************ */
  public int Tree_Search(double Time, int minloc, int maxloc) {// finds place where time would be inserted or replaced
    int medloc;
    while (minloc < maxloc) {
      medloc = (minloc + maxloc) >> 1; // >>1 is same as div 2, only faster.
      if (Time <= this.SubSongs.get(medloc).TimeX) {
        maxloc = medloc;
      } else {
        minloc = medloc + 1;/* has to go through here to be found. */

      }
    }
    return minloc;
  }
  /* ********************************************************************************* */
  @Override public int Get_Sample_Count(int SampleRate) {
    return SampleRate * (int) this.Get_Duration();
  }
  /* ********************************************************************************* */
  @Override public AudProject Get_Project() {
    return this.MyProject;
  }
  /* ********************************************************************************* */
  @Override public void Set_Project(AudProject project) {
    this.MyProject = project;
  }
  /* ********************************************************************************* */
  @Override public void SetMute(boolean Mute) {
  }
  /* ********************************************************************************* */
  @Override public int FreshnessTimeStamp_g() {// ISonglet
    return this.FreshnessTimeStamp;
  }
  @Override public void FreshnessTimeStamp_s(int TimeStampNew) {// ISonglet
    this.FreshnessTimeStamp = TimeStampNew;
  }
  /* ********************************************************************************* */
  public void SetSpineHighlight(boolean Highlight) {
    this.HighlightSpine = Highlight;
  }
  /* ********************************************************************************* */
  @Override public void Draw_Me(DrawingContext ParentDC) {// IDrawable
    OffsetBox ChildOffsetBox;
    int len = this.SubSongs.size();

    int StartDex = 0;// not sure how to get the first within clip box without just iterating from 0. 
    int EndDex = len;

    // Draw Group spine
    Point2D.Double pntprev, pnt;
    Stroke oldStroke = ParentDC.gr.getStroke();
    BasicStroke bs;
    if (this.HighlightSpine) {
      ParentDC.gr.setColor(Color.yellow);
      // thinner lines for more distal sub-branches
      //BasicStroke bs = new BasicStroke((1.0f / ParentDC.RecurseDepth) * 40.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
      bs = new BasicStroke((1.0f / ParentDC.RecurseDepth) * 20.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    } else {
      ParentDC.gr.setColor(Color.darkGray);
      // thinner lines for more distal sub-branches
      //BasicStroke bs = new BasicStroke((1.0f / ParentDC.RecurseDepth) * 40.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
      bs = new BasicStroke((1.0f / ParentDC.RecurseDepth) * 10.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }
    ParentDC.gr.setStroke(bs);
    int pcnt = StartDex;
    if (false) {// #cleanme
      ChildOffsetBox = this.SubSongs.get(pcnt++);
      pntprev = ParentDC.To_Screen(ChildOffsetBox.TimeX, ChildOffsetBox.OctaveY);
    } else {
      pntprev = ParentDC.To_Screen(0, 0);
    }
    while (pcnt < EndDex) {//for (int pcnt = StartDex; pcnt < EndDex; pcnt++) {
      ChildOffsetBox = this.SubSongs.get(pcnt);
      pnt = ParentDC.To_Screen(ChildOffsetBox.TimeX, ChildOffsetBox.OctaveY);
      ParentDC.gr.drawLine((int) pntprev.x, (int) pntprev.y, (int) pnt.x, (int) pnt.y);
      pntprev = pnt;
      if (ParentDC.ClipBounds.Max.x < ChildOffsetBox.TimeX) {// break from loop if subsong starts after MaxX. 
        EndDex = pcnt;
        break;
      }
      pcnt++;
    }
    ParentDC.gr.setStroke(oldStroke);// restore line stroke

    // Draw children
    for (pcnt = StartDex; pcnt < EndDex; pcnt++) {
      ChildOffsetBox = this.SubSongs.get(pcnt);
      ChildOffsetBox.Draw_Me(ParentDC);
    }
  }
  @Override public CajaDelimitadora GetBoundingBox() {// IDrawable
    return this.MyBounds;
  }
  @Override public void UpdateBoundingBox() {// IDrawable
    OffsetBox ChildOffsetBox;
//    CajaDelimitadora ChildBBoxUnMapped;
//    this.MyBounds.Reset();
    int len = this.SubSongs.size();
    for (int pcnt = 0; pcnt < len; pcnt++) {
      ChildOffsetBox = this.SubSongs.get(pcnt);
      ChildOffsetBox.UpdateBoundingBox();
//      ChildBBoxUnMapped = ChildOffsetBox.GetBoundingBox();// project child limits into parent (my) space
//      this.MyBounds.Include(ChildBBoxUnMapped);
    }
    this.UpdateBoundingBoxLocal();
  }
  @Override public void UpdateBoundingBoxLocal() {// IDrawable
    OffsetBox ChildOffsetBox;
    CajaDelimitadora ChildBBoxUnMapped;
    this.MyBounds.Reset();
    int len = this.SubSongs.size();
    if (len == 0) {
      this.MyBounds.ClearZero();
    } else {
      for (int pcnt = 0; pcnt < len; pcnt++) {
        ChildOffsetBox = this.SubSongs.get(pcnt);
        ChildBBoxUnMapped = ChildOffsetBox.GetBoundingBox();// project child limits into parent (my) space
        this.MyBounds.Include(ChildBBoxUnMapped);
      }
    }
  }
  /* ********************************************************************************* */
  @Override public void GoFishing(Grabber Scoop) {// IDrawable
    if (Scoop.CurrentContext.SearchBounds.Intersects(MyBounds)) {// current search bounds are in parent coords
      int len = this.SubSongs.size();
      OffsetBox child;
      for (int pcnt = 0; pcnt < len; pcnt++) {
        child = this.SubSongs.get(pcnt);
        child.GoFishing(Scoop);
      }
    }
  }
  /* ********************************************************************************* */
  @Override public GroupBox Clone_Me() {// ICloneable
    GroupBox child = new GroupBox();
    child.Copy_From(this);
    return child;
  }
  /* ********************************************************************************* */
  @Override public GroupBox Deep_Clone_Me(ITextable.CollisionLibrary HitTable) {// ICloneable
    GroupBox child;
    CollisionItem ci = HitTable.GetItem(this);
    if (ci == null) {
      child = new GroupBox();
      ci = HitTable.InsertUniqueInstance(this);
      ci.Item = child;
      child.TraceText = "I am a clone";
      child.Copy_From(this);
      OffsetBox SubSongHandle;
      int len = this.SubSongs.size();
      for (int cnt = 0; cnt < len; cnt++) {
        SubSongHandle = this.SubSongs.get(cnt);
        OffsetBox obox = SubSongHandle.Deep_Clone_Me(HitTable);
        child.Add_SubSong(obox);
      }
    } else {// pre exists
      child = (GroupBox) ci.Item;// another cast! 
    }
    return child;
  }
  /* ********************************************************************************* */
  public void Copy_From(GroupBox donor) {
    this.Duration = donor.Duration;
    this.Set_Project(donor.MyProject);
    this.MyName = donor.MyName;// for debugging
    this.MaxAmplitude = donor.MaxAmplitude;
    this.FreshnessTimeStamp = 0;// donor.FreshnessTimeStamp;
    this.MyBounds.Copy_From(donor.MyBounds);
  }
  /* ********************************************************************************* */
  public void RescaleGroupTimeX(double Factor) {
    int len = this.SubSongs.size();
    for (int cnt = 0; cnt < len; cnt++) {
      OffsetBox obox = this.SubSongs.get(cnt);
      obox.TimeX *= Factor;
    }
  }
  /* ********************************************************************************* */
  public void swap(AtomicInteger a, AtomicInteger b) {// http://stackoverflow.com/questions/3624525/how-to-write-a-basic-swap-function-in-java
    a.set(b.getAndSet(a.get()));// look mom, no tmp variables needed
  }
  /* ********************************************************************************* */
  public double DotProduct(double X0, double Y0, double X1, double Y1) {
    return X0 * X1 + Y0 * Y1;// length of projection from one vector onto another
  }
  /* ********************************************************************************* */
  public void LineClosestPoint(double LineX0, double LineY0, double LineX1, double LineY1, double XPnt, double YPnt, Point.Double Intersection) {// Find dnd destination using dot product on line segments.
    double Temp;
    if (LineX1 < LineX0 || (LineX1 == LineX0 && LineY1 < LineY0)) {// sort endpoints
      Temp = LineX0;// swap X
      LineX0 = LineX1;
      LineX1 = Temp;

      Temp = LineY0;// swap Y
      LineY0 = LineY1;
      LineY1 = Temp;
    }
    double XDif = LineX1 - LineX0, YDif = LineY1 - LineY0;
    double Shrink = Globals.Fudge;// shrink is a cheat so consecutive lines have slightly separate endpoints. this makes mouse distance from their endpoints unequal. 
    double ShrinkX = XDif * Shrink, ShrinkY = YDif * Shrink;
    LineX0 += ShrinkX;
    LineY0 += ShrinkY;
    LineX1 -= ShrinkX;
    LineY1 -= ShrinkY;
    XDif -= ShrinkX * 2;
    YDif -= ShrinkY * 2;
    double Magnitude = Math.hypot(XDif, YDif);

    double DotProd = this.DotProduct(XPnt - LineX0, YPnt - LineY0, XDif, YDif);
    DotProd /= Magnitude;// now dotprod is the full length of the projection
    double XLoc = ((XDif / Magnitude) * DotProd);// scale separate dimensions to length of shadow
    double YLoc = ((YDif / Magnitude) * DotProd);
    if (XLoc > XDif) {// Test if the intersection is between the line's endpoints and cap them.
      //System.out.println("XLoc:" + XLoc + ", YLoc:" + YLoc);
      XLoc = XDif;
      YLoc = YDif;
    } else if (XLoc < 0) {
      XLoc = YLoc = 0;
    }
    XLoc += LineX0 + ShrinkX;
    YLoc += LineY0 + ShrinkY;
    Intersection.setLocation(XLoc, YLoc);
  }
  /* ********************************************************************************* */
  public double DistanceFromLine(double LineX0, double LineY0, double LineX1, double LineY1, double XPnt, double YPnt) {// work in progress for drag and drop support
    double XDif = LineX1 - LineX0, YDif = LineY1 - LineY0;
    double DotProd = this.DotProduct(XDif, YDif, XPnt - LineX0, YPnt - LineY0);
    double XLoc = LineX0 + (XDif * DotProd);// point of intersection
    double YLoc = LineY0 + (YDif * DotProd);
    // to do: at this point we would like to test if the intersection is between the line's endpoints. 
//    double XLoc = (XDif * DotProd);// point of intersection
//    double YLoc = (YDif * DotProd);
//    double PntDX = (XLoc - XPnt); double PntDY = (YLoc - YPnt);
    double Distance = Math.hypot(XLoc - XPnt, YLoc - YPnt);
    return Distance;
  }
  /* ********************************************************************************* */
  public double HeightFromLine(double LineX0, double LineY0, double LineX1, double LineY1, double XPnt, double YPnt) {// work in progress for drag and drop support
    double YCross = LineYCross(LineX0, LineY0, LineX1, LineY1, XPnt);
    double Distance = YPnt - YCross;
    return Distance;
  }
  /* ********************************************************************************* */
  public double LineYCross(double LineX0, double LineY0, double LineX1, double LineY1, double XPnt) {// work in progress for drag and drop support
    double XDif = LineX1 - LineX0;// given a line and an X point, return the Y location of the intersect along that line
    double YCross;
    if (XDif == 0) {// If slope is infinite, just return the halfway point between the top and bottom of the line.
      YCross = (LineY0 + LineY1) / 2.0;
    } else {
      double YDif = LineY1 - LineY0;
      double Slope = YDif / XDif;
      double PointXOffset = (XPnt - LineX0);
      YCross = LineY0 + (PointXOffset * Slope);
    }
    return YCross;
  }
  /* ********************************************************************************* */
  public double HitsMyVine(double XPnt, double YPnt) {// work in progress for drag and drop support
    double Limit = 0.1;// octaves
    int len = this.SubSongs.size();
    OffsetBox OBox, ClosestPoint = null;
    double XPrev = 0, YPrev = 0, YCross, YDist, Dist;
    OffsetBox LastBox = this.SubSongs.get(len - 1);
    Point.Double Intersection = new Point.Double();
    if (0.0 <= XPnt && XPnt <= LastBox.TimeX) {// or this.MyBounds.Max.x) {
//      int FoundDex = Tree_Search(XPnt - Limit, 0, len);
      int FoundDex = Tree_Search(XPnt, 0, len);
      if (FoundDex == 0) {
        XPrev = YPrev = 0;
      } else {
        OffsetBox PrevBox = this.SubSongs.get(FoundDex - 1);
        XPrev = PrevBox.TimeX;
        YPrev = PrevBox.OctaveY;
      }
      // to do: need condition if FoundDex is greater than len. beyond-end insertion would be nice.
      OBox = this.SubSongs.get(FoundDex);

      if (true) {
        LineClosestPoint(XPrev, YPrev, OBox.TimeX, OBox.OctaveY, XPnt, YPnt, Intersection);
        Dist = Math.hypot(XPnt - Intersection.x, YPnt - Intersection.y);
        System.out.println("Dist:" + Dist + ", Intersection.x:" + Intersection.x + ", Intersection.y:" + Intersection.y);
        if (Dist < Limit) {// then we found one
          ClosestPoint = this.SubSongs.get(FoundDex);
          return Dist;
        }
      } else {
        YCross = LineYCross(XPrev, YPrev, OBox.TimeX, OBox.OctaveY, XPnt);
        YDist = Math.abs(YPnt - YCross);
        if (YDist < Limit) {// then we found one
          ClosestPoint = this.SubSongs.get(FoundDex);
          return YDist;
        }
      }
    }
    return Double.POSITIVE_INFINITY;// infinite if not found
  }
  /* ********************************************************************************* */
  @Override public boolean Create_Me() {// IDeletable
    return true;
  }
  @Override public void Delete_Me() {// IDeletable
    this.MyBounds.Delete_Me();
    this.MyBounds = null;// wreck everything to prevent accidental re-use
    this.MyProject = null;
    this.Wipe_SubSongs();
    this.SubSongs = null;
    this.Duration = Double.NEGATIVE_INFINITY;
    this.FreshnessTimeStamp = Integer.MAX_VALUE;
  }
  public void Wipe_SubSongs() {
    int len = this.SubSongs.size();
    for (int cnt = 0; cnt < len; cnt++) {
      this.SubSongs.get(cnt).Delete_Me();
    }
    this.SubSongs.clear();
  }
  /* ********************************************************************************* */
  @Override public int Ref_Songlet() {// ISonglet Reference Counting: increment ref counter and return new value just for kicks
    return ++this.RefCount;
  }
  @Override public int UnRef_Songlet() {// ISonglet Reference Counting: decrement ref counter and return new value just for kicks
    return --this.RefCount;
  }
  @Override public int GetRefCount() {// ISonglet Reference Counting: get number of references for serialization
    return this.RefCount;
  }
  /* ********************************************************************************* */
  @Override public JsonParse.Node Export(CollisionLibrary HitTable) {// ITextable
    JsonParse.Node phrase = new JsonParse.Node();
    phrase.ChildrenHash = new HashMap<String, JsonParse.Node>();
    phrase.AddSubPhrase("MyName", IFactory.Utils.PackField(this.MyName));

    if (false) {
      phrase.AddSubPhrase("MaxAmplitude", IFactory.Utils.PackField(this.MaxAmplitude));// can be calculated
      phrase.AddSubPhrase("MyBounds", MyBounds.Export(HitTable));// can be calculated
    }

    // Save my array of songlets.
    JsonParse.Node CPointsPhrase = new JsonParse.Node();
    CPointsPhrase.ChildrenArray = IFactory.Utils.MakeArray(HitTable, this.SubSongs);
    phrase.AddSubPhrase(this.SubSongsName, CPointsPhrase);

    return phrase;
  }
  @Override public void ShallowLoad(JsonParse.Node phrase) {// ITextable
    HashMap<String, JsonParse.Node> Fields = phrase.ChildrenHash;
    this.MyName = IFactory.Utils.GetField(Fields, "MyName", "GroupBoxName");
    // this.MaxAmplitude = Double.parseDouble(IFactory.Utils.GetField(Fields, "MaxAmplitude", "0.125")); can be calculated
  }
  @Override public void Consume(JsonParse.Node phrase, CollisionLibrary ExistingInstances) {// ITextable - Fill in all the values of an already-created object, including deep pointers.
    if (phrase == null) {// ready for test
      return;
    }
    this.ShallowLoad(phrase);
    JsonParse.Node ChildPhraseList = IFactory.Utils.LookUpField(phrase.ChildrenHash, this.SubSongsName);// array of subsongs object
    if (ChildPhraseList != null && ChildPhraseList.ChildrenArray != null) {
      this.Wipe_SubSongs();
      OffsetBox obox;
      JsonParse.Node ChildPhrase;
      int len = ChildPhraseList.ChildrenArray.size();
      for (int pcnt = 0; pcnt < len; pcnt++) {// iterate through the array
        ChildPhrase = ChildPhraseList.ChildrenArray.get(pcnt);
        String TypeName = IFactory.Utils.GetField(ChildPhrase.ChildrenHash, Globals.ObjectTypeName, "null");
        IFactory factory = Globals.FactoryLUT.get(TypeName);// use factories to deal with polymorphism
        ITextable child = factory.Create(ChildPhrase, ExistingInstances);// child.Consume(ChildPhrase, ExistingInstances);
        obox = (OffsetBox) child;// another cast!
        this.Add_SubSong(obox);
      }
    }
    if (this.SubSongs.isEmpty()) {
      boolean nop = true;
    }
  }
  /* ********************************************************************************* */
  public static class Group_Singer extends Singer {
    protected GroupBox MySonglet;
    public ArrayList<Singer> NowPlaying = new ArrayList<Singer>();// pool of currently playing voices
    public int Current_Dex = 0;
    double Prev_Time = 0;
    //private Group_OffsetBox MyOffsetBox;
    /* ********************************************************************************* */
    @Override public void Start() {
      IsFinished = false;
      Current_Dex = 0;
      Prev_Time = 0;
      NowPlaying.clear();
    }
    /* ********************************************************************************* */
    @Override public void Skip_To(double EndTime) {
      if (this.IsFinished) {
        return;
      }
      EndTime = this.MyOffsetBox.MapTime(EndTime);// EndTime is now time internal to GroupBox's own coordinate system
      if (this.MySonglet.SubSongs.size() <= 0) {
        this.IsFinished = true;
        this.Prev_Time = EndTime;
        return;
      }
      double Clipped_EndTime = this.Tee_Up(EndTime);
      int NumPlaying = NowPlaying.size();
      Singer player = null;
      int cnt = 0;
      while (cnt < NumPlaying) {// then play the whole pool
        player = this.NowPlaying.get(cnt);
        player.Skip_To(Clipped_EndTime);
        cnt++;
      }
      cnt = 0;// now pack down the finished ones
      while (cnt < this.NowPlaying.size()) {
        player = this.NowPlaying.get(cnt);
        if (player.IsFinished) {
          this.NowPlaying.remove(player);
          player.Delete_Me();
        } else {
          cnt++;
        }
      }
      this.Prev_Time = EndTime;
    }
    /* ********************************************************************************* */
    @Override public void Render_To(double EndTime, Wave wave) {
      if (this.IsFinished) {
        return;
      }
      EndTime = this.MyOffsetBox.MapTime(EndTime);// EndTime is now time internal to GroupBox's own coordinate system
      double UnMapped_Prev_Time = this.InheritedMap.UnMapTime(this.Prev_Time);// get start time in parent coordinates
      if (this.MySonglet.SubSongs.size() <= 0) {
        this.IsFinished = true;
        wave.Init(UnMapped_Prev_Time, UnMapped_Prev_Time, this.MyProject.SampleRate);// wave times are in parent coordinates because the parent will be reading the wave data.
        this.Prev_Time = EndTime;
        return;
      }
      double Clipped_EndTime = this.Tee_Up(EndTime);
      double UnMapped_EndTime = this.InheritedMap.UnMapTime(Clipped_EndTime);
      wave.Init(UnMapped_Prev_Time, UnMapped_EndTime, this.MyProject.SampleRate);// wave times are in parent coordinates because the parent will be reading the wave data.
      Wave ChildWave = new Wave();
      int NumPlaying = NowPlaying.size();
      Singer player = null;
      int cnt = 0;
      while (cnt < NumPlaying) {// then play the whole pool
        player = this.NowPlaying.get(cnt);
        player.Render_To(Clipped_EndTime, ChildWave);
        wave.Overdub(ChildWave);// sum/overdub the waves 
        cnt++;
      }
      cnt = 0;// now pack down the finished ones
      while (cnt < this.NowPlaying.size()) {
        player = this.NowPlaying.get(cnt);
        if (player.IsFinished) {
          this.NowPlaying.remove(player);
          player.Delete_Me();
        } else {
          cnt++;
        }
      }
      wave.Amplify(this.MyOffsetBox.LoudnessFactor);
      this.Prev_Time = EndTime;
    }
    /* ********************************************************************************* */
    private double Tee_Up(double EndTime) {// consolidating identical code 
      if (EndTime < 0) {
        EndTime = 0;// clip time
      }
      int NumSonglets = MySonglet.SubSongs.size();
      int FinalSongletDex = NumSonglets - 1;
      double Final_Start = this.MySonglet.SubSongs.get(FinalSongletDex).TimeX;
      Final_Start = Math.min(Final_Start, EndTime);
      double Final_Time = this.MySonglet.Get_Duration();
      if (EndTime > Final_Time) {
        this.IsFinished = true;
        EndTime = Final_Time;// clip time
      }
      OffsetBox obox;
      while (this.Current_Dex < NumSonglets) {// first find new songlets in this time range and add them to pool
        obox = MySonglet.SubSongs.get(this.Current_Dex);
        if (Final_Start < obox.TimeX) {// repeat until obox start time overtakes EndTime
          break;
        }
        Singer singer = obox.Spawn_Singer();
        singer.Inherit(this);
        this.NowPlaying.add(singer);
        singer.Start();
        this.Current_Dex++;
      }
      return EndTime;
    }
    /* ********************************************************************************* */
    @Override public OffsetBox Get_OffsetBox() {
      return this.MyOffsetBox;
    }
    /* ********************************************************************************* */
    @Override public boolean Create_Me() {// IDeletable
      super.Create_Me();
      return true;
    }
    @Override public void Delete_Me() {// IDeletable
      super.Delete_Me();
      int len = this.NowPlaying.size();
      for (int cnt = 0; cnt < len; cnt++) {
        this.NowPlaying.get(cnt).Delete_Me();
      }
      this.NowPlaying.clear();
    }
  }
  /* ********************************************************************************* */
  public static class ScaleXHandle implements IDrawable.IMoveable, IDeletable {
    public CajaDelimitadora MyBounds = new CajaDelimitadora();
    public Group_OffsetBox ParentPoint;
    public double OctavesPerRadius = 0.007;
    private boolean IsSelected = false;
    /* ********************************************************************************* */
    public double GetUnitX() {
      return this.ParentPoint.MyBounds.GetWidth();
    }
    /* ********************************************************************************* */
    public double GetX() {
      double TimeScaleWidth = this.ParentPoint.ScaleX * this.GetUnitX();// Map timescale to screen pixels.
      return TimeScaleWidth;
    }
    public double GetY() {
      return this.ParentPoint.MyBounds.Max.y;
    }
    @Override public void MoveTo(double XLoc, double YLoc) {// IDrawable.IMoveable
      if (XLoc >= 0) {// don't go backward in time
        this.ParentPoint.ScaleX = XLoc / GetUnitX();
      }
      // ignore YLoc for now
    }
    @Override public boolean HitsMe(double XLoc, double YLoc) {// IDrawable.IMoveable
      System.out.print("** ScaleXHandle HitsMe:");
      boolean Hit = false;
      if (this.MyBounds.Contains(XLoc, YLoc)) {
        System.out.print(" InBounds ");
        double dist = Math.hypot(XLoc - this.GetX(), YLoc - (this.GetY() + this.OctavesPerRadius));
        if (dist <= this.OctavesPerRadius) {
          System.out.print(" Hit!");
          Hit = true;
        } else {
          System.out.print(" Missed!");
        }
      } else {
        System.out.print(" OutBounds ");
      }
      return Hit;
    }
    @Override public void SetSelected(boolean Selected) {// IDrawable.IMoveable
      this.IsSelected = Selected;
    }
    @Override public void Draw_Me(DrawingContext ParentDC) {
      // this is all nonsense at the moment, will have to fill it in with better stuff
      // Control points have the same space as their parent, so no need to create a local map.
      /*
       Point2D.Double pnt = ParentDC.To_Screen(this.ParentPoint.MyBounds.Max.x, this.ParentPoint.MyBounds.Max.y);
       double RadiusPixels = Math.abs(ParentDC.InheritedMap.ScaleY) * OctavesPerRadius;
       double LoudnessHgt = this.ParentPoint.LoudnessFactor * this.ParentPoint.OctavesPerLoudness;
       double XLoc = ParentDC.InheritedMap.UnMapPitch(this.ParentPoint.OctaveY + LoudnessHgt) - RadiusPixels;// My handle rests *upon* the line I control, so I don't occlude my VoicePoint. 
       RadiusPixels = Math.ceil(RadiusPixels);
       MonkeyBox.Draw_Dot2(ParentDC, XLoc, pnt.y, OctavesPerRadius, this.IsSelected, Globals.ToAlpha(Color.lightGray, 100));
       */
    }
    @Override public CajaDelimitadora GetBoundingBox() {
      return this.MyBounds;
    }
    @Override public void UpdateBoundingBox() {
      this.UpdateBoundingBoxLocal();
    }
    @Override public void UpdateBoundingBoxLocal() {
      double XLoc = this.GetX();
      double YLoc = this.GetY();
      double MinX = XLoc - this.OctavesPerRadius;
      double MaxX = XLoc + this.OctavesPerRadius;
      double MinY = YLoc - this.OctavesPerRadius;
      double MaxY = YLoc + this.OctavesPerRadius;
      this.MyBounds.Assign(MinX, MinY, MaxX, MaxY);
    }
    @Override public void GoFishing(Grabber Scoop) {
      System.out.println();
      System.out.print(" ScaleXHandle GoFishing: ");
      if (Scoop.CurrentContext.SearchBounds.Intersects(MyBounds)) {
        System.out.print(" InBounds, ");
        Scoop.ConsiderLeaf(this);
      }
      System.out.println();
    }
    /* ********************************************************************************* */
    @Override public ScaleXHandle Clone_Me() {// ICloneable
      ScaleXHandle child = new ScaleXHandle();
      return child;
    }
    /* ********************************************************************************* */
    @Override public ScaleXHandle Deep_Clone_Me(ITextable.CollisionLibrary HitTable) {// ICloneable
      ScaleXHandle child = new ScaleXHandle();
      child.OctavesPerRadius = this.OctavesPerRadius;
      child.ParentPoint = this.ParentPoint;
      child.MyBounds.Copy_From(this.MyBounds);
      return child;
    }
    /* ********************************************************************************* */
    @Override public boolean Create_Me() {
      return true;
    }
    @Override public void Delete_Me() {
      this.ParentPoint = null;
      this.MyBounds.Delete_Me();
      this.MyBounds = null;
    }
  }
  /* ********************************************************************************* */
  public static class Group_OffsetBox extends OffsetBox {// location box to transpose in pitch, move in time, etc. 
    public GroupBox Content = null;
    public double GroupScaleX = 1.0;
    public static String GroupScaleXName = "GroupScaleX";// for serialization
    public static String ObjectTypeName = "Group_OffsetBox";
    /* ********************************************************************************* */
    public Group_OffsetBox() {
      super();
      this.Clear();
      MyBounds = new CajaDelimitadora();
      this.Create_Me();
    }
    /* ********************************************************************************* */
    @Override public GroupBox GetContent() {
      return Content;
    }
    /* ********************************************************************************* */
    public void Attach_Songlet(GroupBox songlet) {// for serialization
      this.Content = songlet;
      songlet.Ref_Songlet();
    }
    /* ********************************************************************************* */
    public void RescaleGroupTimeX(double Factor) {
      this.Content.RescaleGroupTimeX(Factor);
    }
    /* ********************************************************************************* */
    @Override public void Draw_Me(DrawingContext ParentDC) {// IDrawable
      super.Draw_Me(ParentDC);
      // here draw the Rescale_TimeX handle
    }
    /* ********************************************************************************* */
    @Override public Group_Singer Spawn_Singer() {// always always always override this
      return this.Spawn_My_Singer();
    }
    /* ********************************************************************************* */
    public Group_Singer Spawn_My_Singer() {// for render time
      Group_Singer Singer = this.Content.Spawn_My_Singer();
      Singer.MyOffsetBox = this;// Transfer all of this box's offsets to singer. 
      return Singer;
    }
    /* ********************************************************************************* */
    @Override public Group_OffsetBox Clone_Me() {// always override this thusly
      Group_OffsetBox child = new Group_OffsetBox();
      child.Copy_From(this);
      child.Content = this.Content;
      return child;
    }
    /* ********************************************************************************* */
    @Override public Group_OffsetBox Deep_Clone_Me(ITextable.CollisionLibrary HitTable) {// ICloneable
      Group_OffsetBox child = new Group_OffsetBox();
      child.Copy_From(this);
      child.Attach_Songlet(this.Content.Deep_Clone_Me(HitTable));
      return child;
    }
    /* ********************************************************************************* */
    @Override public void BreakFromHerd(ITextable.CollisionLibrary HitTable) {// for compose time. detach from my songlet and attach to an identical but unlinked songlet
      GroupBox clone = this.Content.Deep_Clone_Me(HitTable);
      if (this.Content.UnRef_Songlet() <= 0) {
        this.Content.Delete_Me();
      }
      this.Content = clone;
      this.Content.Ref_Songlet();
    }
    /* ********************************************************************************* */
    @Override public boolean Create_Me() {// IDeletable
      return true;
    }
    @Override public void Delete_Me() {// IDeletable
      super.Delete_Me();
      this.GroupScaleX = Double.NEGATIVE_INFINITY;
      if (this.Content != null) {
        if (this.Content.UnRef_Songlet() <= 0) {
          this.Content.Delete_Me();
          this.Content = null;
        }
      }
    }
    /* ********************************************************************************* */
    @Override public JsonParse.Node Export(CollisionLibrary HitTable) {// ITextable
      JsonParse.Node SelfPackage = super.Export(HitTable);// ready for test?
      //HashMap<String, JsonParse.Node> Fields = SelfPackage.ChildrenHash;
      SelfPackage.AddSubPhrase(Globals.ObjectTypeName, IFactory.Utils.PackField(ObjectTypeName));
      SelfPackage.AddSubPhrase(GroupScaleXName, IFactory.Utils.PackField(this.GroupScaleX));
      if (false) {
        JsonParse.Node ChildPackage;
        if (this.Content.GetRefCount() != 1) {// songlet exists in more than one place, use a pointer to library
          ChildPackage = new JsonParse.Node();// multiple references, use a pointer to library instead
          CollisionItem ci;// songlet is already in library, just create a child phrase and assign its textptr to that entry key
          if ((ci = HitTable.GetItem(this.Content)) == null) {
            ci = HitTable.InsertUniqueInstance(this.Content);// songlet is NOT in library, serialize it and add to library
            ci.JsonPhrase = this.Content.Export(HitTable);
          }
          ChildPackage.Literal = ci.ItemTxtPtr;
        } else {// songlet only exists in one place, make it inline.
          ChildPackage = this.Content.Export(HitTable);
        }
        SelfPackage.AddSubPhrase(OffsetBox.ContentName, ChildPackage);
      }
      return SelfPackage;
    }
    @Override public void ShallowLoad(JsonParse.Node phrase) {// ITextable
      super.ShallowLoad(phrase);
      this.GroupScaleX = Double.parseDouble(IFactory.Utils.GetField(phrase.ChildrenHash, "GroupScaleX", "1.0"));
    }
    @Override public void Consume(JsonParse.Node phrase, CollisionLibrary ExistingInstances) {// ITextable - Fill in all the values of an already-created object, including deep pointers.
      if (phrase == null) {// ready for test?
        return;
      }
      this.ShallowLoad(phrase);
      JsonParse.Node SongletPhrase = phrase.ChildrenHash.get(OffsetBox.ContentName);// value of songlet field
      String ContentTxt = SongletPhrase.Literal;
      GroupBox songlet;
      if (Globals.IsTxtPtr(ContentTxt)) {// if songlet content is just a pointer into the library
        CollisionItem ci = ExistingInstances.GetItem(ContentTxt);// look up my songlet in the library
        if (ci == null) {// then null reference even in file - the json is corrupt
          throw new RuntimeException("CollisionItem is null in " + ObjectTypeName);
          //return;
        }
        if ((songlet = (GroupBox) ci.Item) == null) {// another cast!
          ci.Item = songlet = new GroupBox();// if not instantiated, create one and save it
          if (ci.JsonPhrase == null) {
            boolean nop = true;
          }
          songlet.Consume(ci.JsonPhrase, ExistingInstances);
        } else {
          boolean nop = true;
        }
      } else {
        songlet = new GroupBox();// songlet is inline, inside this one offsetbox
        songlet.Consume(SongletPhrase, ExistingInstances);
      }
      this.Attach_Songlet(songlet);
    }
    @Override public ISonglet Spawn_And_Attach_Songlet() {// reverse birth, use ONLY for deserialization
      GroupBox songlet = new GroupBox();
      this.Attach_Songlet(songlet);
      return songlet;
    }
    /* ********************************************************************************* */
    public static class Factory implements IFactory {// for serialization
      @Override public Group_OffsetBox Create(JsonParse.Node phrase, CollisionLibrary ExistingInstances) {// under construction, this does not do anything yet
        Group_OffsetBox obox = new Group_OffsetBox();
        obox.Consume(phrase, ExistingInstances);
        return obox;
      }
    }
  }
}
