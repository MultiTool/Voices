/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import java.awt.List;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 *
 * @author MultiTool
 *
 * HookAndLure is basically a spatial query which also carries back all the results data.
 *
 */
public class HookAndLure {
  public double XHit, YHit;// exact mouse click point
  //public CajaDelimitadora SearchBounds = new CajaDelimitadora();
  public StackItem CurrentContext = null;
  public int Stack_Depth = 0, Stack_Depth_Best = 0;
  public ArrayList<StackItem> Explore_Stack = new ArrayList<StackItem>();
  public ArrayList<StackItem> Best_Stack = new ArrayList<StackItem>();
  public IDrawable.IMoveable Leaf;// thing we hit and are going to move or copy or whatever
  double Radius = 5;
  /* 
   to do: put a hit stack here for best item found,
   or a list of everything hit, each with a compressed affine transform
   or a bunch of hit stacks for everything found (yuck)
   or even a single item found with only a compressed affien transform
  
   public Object PickBest(Candidate incumbent, Candidate challenger){
   }
   PickBest(this.CurrentBest, challenger);
  
   Candidate is just an IDrawable?  
   one way to compare unknown types is with enum attributes:
   candidate.GetType() == Types.VoicePoint; 
  
   */
  public void ConsiderLeaf(IDrawable.IMoveable CandidateLeaf) {
    if (this.Stack_Depth_Best <= this.Stack_Depth) {// prefer the most distal
      this.Stack_Depth_Best = this.Stack_Depth;// or if equal, prefer the last drawn (most recent hit)
      this.Leaf = CandidateLeaf;
      Copy_Stack(this.Explore_Stack, this.Best_Stack);
    }
    // this.Compare(this.Leaf, leaf);
  }
  public void Init(OffsetBox starter, double XLoc, double YLoc) {// add first space map at start of search
    OffsetBox child = new OffsetBox();// first layer place holder.  not a great solution. 
    child.MyBounds.Assign(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    StackItem next = new StackItem();
    next.PossibleLeaf = child;
    next.Loc.x = XLoc;// Next's location values exist in the space above it. At the top they are mouse coordinates. 
    next.Loc.y = YLoc;
    next.SearchBounds.Assign(XLoc - Radius, YLoc - Radius, XLoc + Radius, YLoc + Radius);
    this.Explore_Stack.add(next);
    this.CurrentContext = next;
    Stack_Depth = 1;// Now we have one element, whee!
  }
  public void AddFirstBox(OffsetBox starter, double XLoc, double YLoc) {// add first space map at start of search
    this.Leaf = null;
    TruncateStack(this.Explore_Stack, 0);
    StackItem next = new StackItem();
    next.PossibleLeaf = starter;
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
  public void AddBoxToStack(OffsetBox child) {
    StackItem prev = this.CurrentContext;
    StackItem next = new StackItem();
    next.PossibleLeaf = child;

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
    StackItem si;
    for (int cnt = 0; cnt < len; cnt++) {
      si = this.Best_Stack.get(cnt);
      si.PossibleLeaf.MapTo(pntfrom, pntto);
      pntfrom.setLocation(pntto);
    }
    results.setLocation(pntto);
  }
  /* ********************************************************************************* */
  public static class StackItem implements IDeletable {
    public CajaDelimitadora SearchBounds = new CajaDelimitadora();
    public OffsetBox PossibleLeaf;
    Point2D.Double Loc = new Point2D.Double();
    public void Copy_From(StackItem donor) {
      this.SearchBounds.Copy_From(donor.SearchBounds);
      this.PossibleLeaf = donor.PossibleLeaf;
      this.Loc.setLocation(donor.Loc);
    }
    @Override public boolean Create_Me() {// IDeletable
      return true;
    }
    @Override public void Delete_Me() {// IDeletable
      this.SearchBounds.Delete_Me();
      this.SearchBounds = null;
      this.PossibleLeaf = null;
      this.Loc = null;
    }
  }
}
