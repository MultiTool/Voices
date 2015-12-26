/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

/**
 *
 * @author MultiTool
 *
 * HookAndLure is basically a spatial query which also carries back all the results data.
 *
 */
public class HookAndLure {
  public CajaDelimitadora ClipBounds;
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
}
