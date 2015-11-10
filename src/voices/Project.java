/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import voices.VoiceBase.Player_Head_Base;

/**
 *
 * @author MultiTool
 */
public class Project extends VoiceBase.CoordBoxBase {
  CoordBox rootbox;
  public void Start() {
    Voice vc = new Voice();
    this.rootbox = vc.Spawn_CoordBox();
    Player_Head_Base phb = this.rootbox.Spawn_Player();
    phb.Compound(this);
    phb.Compound(this.rootbox);
  }
}
