package voices;

import java.awt.List;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import javafx.scene.effect.Light;

/**
 *
 * @author MultiTool

 Grabber is basically a spatial query which also carries back all the results data.
 *
 */
public class Grabber { // to do: rename this class to Grabber
  public double XHit, YHit;// exact mouse click point
  //public CajaDelimitadora SearchBounds = new CajaDelimitadora();
  public StackItem CurrentContext = null;
  public int Stack_Depth = 0, Stack_Depth_Best = 0;
  public ArrayList<StackItem> Explore_Stack = new ArrayList<StackItem>();
  public ArrayList<StackItem> Best_Stack = new ArrayList<StackItem>();
  public IDrawable.IMoveable Leaf;// thing we hit and are going to move or copy or whatever
  double Radius = 5;
  /* ********************************************************************************* */
  public void ConsiderLeaf(IDrawable.IMoveable CandidateLeaf) {
    if (CandidateLeaf.HitsMe(this.CurrentContext.Loc.x, this.CurrentContext.Loc.y)) {
      System.out.print(" Was Hit, ");
      if (this.Stack_Depth_Best <= this.Stack_Depth) {// prefer the most distal
        this.Stack_Depth_Best = this.Stack_Depth;// or if equal, prefer the last drawn (most recent hit)
        this.Leaf = CandidateLeaf;
        Copy_Stack(this.Explore_Stack, this.Best_Stack);
      }
      // this.Compare(this.Leaf, leaf);
    }
  }
  /* ********************************************************************************* */
  public void Init(OffsetBox starter, double XLoc, double YLoc) {// add first space map at start of search
    OffsetBox child = new OffsetBox();// first layer place holder.  not a great solution. 
    child.MyBounds.Assign(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    StackItem next = new StackItem();
    next.OBox = child;
    next.Loc.x = XLoc;// Next's location values exist in the space above it. At the top they are mouse coordinates. 
    next.Loc.y = YLoc;
    next.SearchBounds.Assign(XLoc - Radius, YLoc - Radius, XLoc + Radius, YLoc + Radius);
    this.Explore_Stack.add(next);
    this.CurrentContext = next;
    Stack_Depth = 1;// Now we have one element, whee!
  }
  /* ********************************************************************************* */
  public void AddFirstBox(OffsetBox starter, double XLoc, double YLoc) {// add first space map at start of search
    this.Leaf = null;
    this.Stack_Depth_Best = 0;
    this.Stack_Depth = 0;
    TruncateStack(this.Explore_Stack, 0);
    StackItem next = new StackItem();
    next.OBox = starter;
//    next.Loc.x = XLoc;// Next's location values exist in the space above it. At the top they are mouse coordinates. 
//    next.Loc.y = YLoc;
//    next.SearchBounds.Assign(XLoc - Radius, YLoc - Radius, XLoc + Radius, YLoc + Radius);

    // map to child space
    CajaDelimitadora SearchBoundsTemp = new CajaDelimitadora();
    SearchBoundsTemp.Assign(XLoc - Radius, YLoc - Radius, XLoc + Radius, YLoc + Radius);
    starter.MapTo(SearchBoundsTemp, next.SearchBounds);// prev.SearchBounds.Map(child, next.SearchBounds);
    SearchBoundsTemp.Delete_Me();
    starter.MapTo(new Point2D.Double(XLoc, YLoc), next.Loc);

    this.Explore_Stack.add(next);
    this.CurrentContext = next;
    Stack_Depth = 1;// Now we have one element, whee!
  }
  /* ********************************************************************************* */
  public void AddBoxToStack(OffsetBox child) {
    StackItem prev = this.CurrentContext;
    StackItem next = new StackItem();
    next.OBox = child;

    // map to child space
    child.MapTo(prev.SearchBounds, next.SearchBounds);// prev.SearchBounds.Map(child, next.SearchBounds);
    child.MapTo(prev.Loc, next.Loc);
    //Fresh_Parent.ClipBounds.Map(this.Offset, this.ClipBounds);// map to child (my) internal coordinates

    this.Explore_Stack.add(next);
    this.CurrentContext = next;
    Stack_Depth++;
  }
  public void DecrementStack() {
    int resize = this.Stack_Depth - 1;
    if (resize >= 0) {
      TruncateStack(this.Explore_Stack, resize);
      this.Stack_Depth--;
      if (resize > 0) {
        this.CurrentContext = this.Explore_Stack.get(resize - 1);
      } else {
        this.CurrentContext = null;
      }
    }
  }
  public static void TruncateStack(ArrayList<StackItem> Stack, int resize) {
    int len = Stack.size();
    for (int cnt = resize; cnt < len; cnt++) {
      Stack.get(cnt).Delete_Me();
    }
    Stack.subList(resize, len).clear();// does this really work? 
  }
  /* ********************************************************************************* */
  public int Compare(IDrawable.IMoveable thing0, IDrawable.IMoveable thing1) {// always override this
    // if thing0<thing1 then return -1,  if thing0>thing1 then return 1.
    return 0;// compare for which one is the best match
    /*
     Who usually wins?  1. it must be a direct hit, and 2. more distal wins over proximal (voice cpoints over oboxes)
     proximal cpoints are generally bigger but in the background. 
     for special cases you can also filter for only obox targets, or targets within a certain branch. 
     so we can filter out leaves, but also whole branches?  LeafFilter() and BranchFilter()? 
     I can be a direct hit of two things, leaf and obox. winner is more distal. equally distal, break tie by who is plotted last. 
     . go forward through tree, and all other things being equal replace the incumbent with any new find. 
     */
  }
  /* ********************************************************************************* */
  public static void Copy_Stack(ArrayList<StackItem> StackPrev, ArrayList<StackItem> StackNext) {
    TruncateStack(StackNext, 0);
    int len = StackPrev.size();
    StackItem siprev, sinext;
    for (int cnt = 0; cnt < len; cnt++) {
      siprev = StackPrev.get(cnt);
      sinext = new StackItem();
      sinext.Copy_From(siprev);
      StackNext.add(sinext);
    }
  }
  /* ********************************************************************************* */
  public void MapThroughStack(double XLoc, double YLoc, Point2D.Double results) {
    int len = this.Best_Stack.size();
    Point2D.Double pntfrom = new Point2D.Double(), pntto = new Point2D.Double();
    pntfrom.setLocation(XLoc, YLoc);
    pntto.setLocation(pntfrom);// in case of no mapping at all, default to original coordinates
    StackItem si;
    for (int cnt = 0; cnt < len; cnt++) {
      si = this.Best_Stack.get(cnt);
      si.OBox.MapTo(pntfrom, pntto);
      pntfrom.setLocation(pntto);
    }
    results.setLocation(pntto);
  }
  /* ********************************************************************************* */
  public void MapThroughStack(double XLoc, double YLoc, MonkeyBox startplace, Point2D.Double results) {
    int len = this.Best_Stack.size();
    StackItem si;
    Point2D.Double pntfrom = new Point2D.Double(), pntto = new Point2D.Double();
    pntfrom.setLocation(XLoc, YLoc);
    pntto.setLocation(pntfrom);// in case of no mapping at all, default to original coordinates
    int cnt = 0;
    while (cnt < len) {
      si = this.Best_Stack.get(cnt);
      if (si.OBox == startplace) {// start at startplace
        break;
      }
      cnt++;
    }
    while (cnt < len) {
      si = this.Best_Stack.get(cnt);
      si.OBox.MapTo(pntfrom, pntto);
      pntfrom.setLocation(pntto);
      cnt++;
    }
    results.setLocation(pntto);
  }
  /* ********************************************************************************* */
  public void CompoundStack(MonkeyBox startplace, MonkeyBox results) {
    results.Clear();// crush all transformations to one obox
    int len = this.Best_Stack.size();
    StackItem si;
    int cnt = 0;
    while (cnt < len) {
      si = this.Best_Stack.get(cnt);
      if (si.OBox == startplace) {// start at startplace
        break;
      }
      cnt++;
    }
    while (cnt < len) {
      si = this.Best_Stack.get(cnt);
      results.Compound(si.OBox);// this becomes a transformation from screen down to object.
      cnt++;
    }
  }
  /* ********************************************************************************* */
  public void UpdateBoundingBoxes() {
    this.Leaf.UpdateBoundingBoxLocal();
    int lastitem = this.Best_Stack.size() - 1;
    StackItem si;
    for (int cnt = lastitem; cnt >= 0; cnt--) {
      si = this.Best_Stack.get(cnt);
      si.OBox.UpdateBoundingBoxLocal();
    }
  }
  /* ********************************************************************************* */
  public static class StackItem implements IDeletable {
    public CajaDelimitadora SearchBounds = new CajaDelimitadora();
    public OffsetBox OBox;
    public int HitDex;
    /* so do we pass HitDex to all songlets, or to all oboxes? only songlets can use it directly. 
     and when do we use it? we use it when we move the thing we grabbed, in MapThroughStack
     we would have to pass it through every mapto.  ack.  
     why not just give 
     */
    Point2D.Double Loc = new Point2D.Double();
    public void Copy_From(StackItem donor) {
      this.SearchBounds.Copy_From(donor.SearchBounds);
      this.OBox = donor.OBox;
      this.HitDex = donor.HitDex;
      this.Loc.setLocation(donor.Loc);
    }
    @Override public boolean Create_Me() {// IDeletable
      return true;
    }
    @Override public void Delete_Me() {// IDeletable
      this.SearchBounds.Delete_Me();
      this.SearchBounds = null;
      this.OBox = null;
      this.Loc = null;
    }
  }
  /* ********************************************************************************* */
  public static class DestinationGrabber extends Grabber {
    // this class searches for containers in which to drop a floating, copied songlet
    public MonkeyBox Floater = null;
    public GroupBox PossibleDestination = null;
    private double ClosestDistance = Double.POSITIVE_INFINITY;
    @Override public void AddFirstBox(OffsetBox starter, double XLoc, double YLoc) {// add first space map at start of search
      this.PossibleDestination = null;
      this.ClosestDistance = Double.POSITIVE_INFINITY;
      super.AddFirstBox(starter, XLoc, YLoc);
    }
    /* ********************************************************************************* */
    @Override public void ConsiderLeaf(IDrawable.IMoveable CandidateLeaf) {
      if (CandidateLeaf instanceof LoopBox.Ghost_OffsetBox) {
        boolean nop = true;
      }
      //this.PossibleDestination = null;
      if (CandidateLeaf instanceof OffsetBox) {// only one that works so far
        OffsetBox obx = (OffsetBox) CandidateLeaf;// other cast!
        ISonglet songlet = obx.GetContent();
        if (songlet instanceof GroupBox) {
          GroupBox gbx = (GroupBox) songlet;// other cast!
          Point.Double results = new Point.Double();
          obx.MapTo(this.CurrentContext.Loc.x, this.CurrentContext.Loc.y, results);// we're hitting the songlet, not its offsetbox, so we have to map to obox child coordinates.
          double FoundDistance = gbx.HitsMyVine(results.x, results.y);// WIP, does not work yet
          //FoundDistance = true;
          // to do: need to protect against self-inclusion
//          if ((FoundDistance < this.ClosestDistance) && this.Stack_Depth_Best <= this.Stack_Depth) {// prefer the most distal
          //if ((FoundDistance < this.ClosestDistance) || ((FoundDistance == this.ClosestDistance) && this.Stack_Depth_Best <= this.Stack_Depth)) {// prefer the most distal
          if (FoundDistance < this.ClosestDistance) {// prefer the closest
            this.Stack_Depth_Best = this.Stack_Depth;// or if equal, prefer the last drawn (most recent hit)
            this.Leaf = CandidateLeaf;
            PossibleDestination = gbx;
            this.ClosestDistance = FoundDistance;
            Copy_Stack(this.Explore_Stack, this.Best_Stack);
          }
        }
      }
      if (false && CandidateLeaf instanceof GroupBox.Group_OffsetBox) {
        GroupBox.Group_OffsetBox gobx = (GroupBox.Group_OffsetBox) CandidateLeaf;// other cast!
        GroupBox gbx = gobx.GetContent();//.Content;
        double FoundDistance = gbx.HitsMyVine(this.CurrentContext.Loc.x, this.CurrentContext.Loc.y);// WIP, does not do anything yet
        if ((FoundDistance < this.ClosestDistance) && this.Stack_Depth_Best <= this.Stack_Depth) {// prefer the most distal
          this.Stack_Depth_Best = this.Stack_Depth;// or if equal, prefer the last drawn (most recent hit)
          this.Leaf = CandidateLeaf;
          this.ClosestDistance = FoundDistance;
          Copy_Stack(this.Explore_Stack, this.Best_Stack);
        }
      }
    }
  }
}
