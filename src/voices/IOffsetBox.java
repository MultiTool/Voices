/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import voices.ISonglet.Singer;

/**
 *
 * @author MultiTool
 */
public interface IOffsetBox {// location box to transpose in pitch, move in time, etc. 
  double TimeLoc_g();
  void TimeLoc_s(double value);
  double OctaveLoc_g();
  void OctaveLoc_s(double value);
  double LoudnessLoc_g();
  void LoudnessFactor_s(double value);
  double ScaleX_g();
  void ScaleX_s(double value);
  double ScaleY_g();
  void ScaleY_s(double value);
  // double TimeLoc, OctaveLoc, LoudnessLoc;
//  public VoiceBase Content;
  /* ********************************************************************************* */
  public ISonglet GetContent();
  /* ********************************************************************************* */
  public void Compound(IOffsetBox donor);
  /* ********************************************************************************* */
  public double MapTime(double ParentTime);// convert time coordinate from my parent's frame to my child's frame
  /* ********************************************************************************* */
  public double UnMapTime(double ChildTime);// convert time coordinate from my child's frame to my parent's frame
  /* ********************************************************************************* */
  public double MapPitch(double ParentPitch);// convert octave coordinate from my parent's frame to my child's frame
  /* ********************************************************************************* */
  public double UnMapPitch(double ChildPitch);// convert octave coordinate from my child's frame to my parent's frame
  /* ********************************************************************************* */
  public Singer Spawn_Singer();
}
