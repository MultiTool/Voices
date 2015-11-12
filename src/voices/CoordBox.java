/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import voices.IPlayable.Player_Head_Base;

/**
 *
 * @author MultiTool
 */
public abstract class CoordBox implements ICoordBox {// location box to transpose in pitch, move in time, etc. 
  public double TimeLoc, OctaveLoc, LoudnessLoc;
  /* ********************************************************************************* */
  public void Clear() {// set all coordinates to identity, no transformation for content
    TimeLoc = OctaveLoc = 0.0;
    LoudnessLoc = 1.0;
  }
  /* ********************************************************************************* */
  @Override public void Compound(ICoordBox donor) {
    this.TimeLoc_s(TimeLoc + donor.TimeLoc_g());
    this.OctaveLoc_s(OctaveLoc + donor.OctaveLoc_g());
    this.LoudnessLoc_s(LoudnessLoc + donor.LoudnessLoc_g());
    //this.TimeLoc += donor.TimeLoc; this.OctaveLoc += donor.OctaveLoc; this.LoudnessLoc *= donor.LoudnessLoc;
  }
  /* ********************************************************************************* */
  @Override public Player_Head_Base Spawn_Player() {
    return null;
  }
  /* ********************************************************************************* */
  @Override public IPlayable GetContent() {
    return null;
  }
  @Override public double TimeLoc_g() {
    return TimeLoc;
  }
  @Override public void TimeLoc_s(double value) {
    TimeLoc = value;
  }
  @Override public double OctaveLoc_g() {
    return OctaveLoc;
  }
  @Override public void OctaveLoc_s(double value) {
    OctaveLoc = value;
  }
  @Override public double LoudnessLoc_g() {
    return LoudnessLoc;
  }
  @Override public void LoudnessLoc_s(double value) {
    LoudnessLoc = value;
  }
}
/*
 Big picture, all the good effects depend on containers.  transposition, looping, bending, etc.

 How to do this neatly?

 voice spawns player
 phrase spawns player

 player runs, midwifes new players from phrase's children. 
 maybe the player should just get CoordBox's coordinates separately and apply them to the playerhead children. 

 problem is CoordBox is generic, can't see its type of child. 

 ergo, each phrase would be better off having its own unique list of pointers to children phrases. 
 but, need to associate transformation with each child.  

 double collections, one for boxes and one for children?

 or special CoordBoxes, each owned by each type of phrase?  that is each phrase owns its own type of CoordBox, or each phrase owns its children's type of CoordBox? 
 better if each phrase owns its own type of CoordBox. if a parent knows its children, then it will know their CoordBoxes.  

 still weird.  but if every phrase type owns its CoordBox, then each phrase can expand the number of coordinates that can manipulate the child.  

 what about octaverate/bending 'coords'?  kinda like coords but not displayed as such. 

 so: each parent player has free control over its children players, including arbitrary new dimensions of coordinates. 

 does bending count as a coordinate?  only if you want to bend the whole phrase. not very useful, do not include. 

 */
