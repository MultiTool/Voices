/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import java.awt.List;
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
  public CajaDelimitadora SearchBounds;
  public int Stack_Depth = 0;
  public ArrayList<StackItem> Explore_Stack;
  public ArrayList<StackItem> Best_Stack;
  public IDrawable.IMoveable Leaf;// thing we hit and are going to move or copy or whatever
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
  public CajaDelimitadora AddBoxToStack(OffsetBox target) {
    StackItem prev = this.Explore_Stack.get(Stack_Depth - 1);// ack, possible range check error
    StackItem si = new StackItem();
    si.Target = target;
    prev.SearchBounds.Map(target, si.SearchBounds);
    this.Explore_Stack.add(si);
    Stack_Depth++;
    return this.SearchBounds;
  }
  public void TruncateStack(int resize) {
    int len = this.Explore_Stack.size();
    for (int cnt = resize; cnt < len; cnt++) {
      this.Explore_Stack.get(cnt).Delete_Me();
    }
    this.Explore_Stack.subList(resize, len).clear();// does this really work? 
  }
  public IDrawable.IMoveable PickAWinner(IDrawable.IMoveable thing0, IDrawable.IMoveable thing1) {// always override this
    return null;// compare for which one is the best match
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
  public class StackItem implements IDeletable {
    public CajaDelimitadora SearchBounds = new CajaDelimitadora();
    public IDrawable.IMoveable Target;
    @Override public boolean Create_Me() {// IDeletable
      return true;
    }
    @Override public void Delete_Me() {// IDeletable
      this.SearchBounds.Delete_Me();
      this.SearchBounds = null;
      this.Target = null;
    }
  }
}
