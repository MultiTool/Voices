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
public class OffsetBox implements IOffsetBox {// location box to transpose in pitch, move in time, etc. 
  public double TimeOrg, OctaveLoc, LoudnessFactor;
  public static OffsetBox Identity = OffsetBox.CreateIdentity();
  /* ********************************************************************************* */
  public void Clear() {// set all coordinates to identity, no transformation for content
    TimeOrg = OctaveLoc = 0.0;
    LoudnessFactor = 1.0;
  }
  /* ********************************************************************************* */
  public static OffsetBox CreateIdentity() {
    OffsetBox cb = new OffsetBox();
    cb.TimeOrg = 0;
    cb.OctaveLoc = 0;
    cb.LoudnessFactor = 1.0;
    return cb;
  }
  /* ********************************************************************************* */
  public double Get_Max_Amplitude() {
    return this.GetContent().Get_Max_Amplitude() * this.LoudnessFactor;
  }
  /* ********************************************************************************* */
  @Override public void Compound(IOffsetBox donor) {
    this.TimeLoc_s(TimeOrg + donor.TimeLoc_g());
    this.OctaveLoc_s(OctaveLoc + donor.OctaveLoc_g());
    this.LoudnessFactor_s(LoudnessFactor + donor.LoudnessLoc_g());
    //this.TimeLoc += donor.TimeLoc; this.OctaveLoc += donor.OctaveLoc; this.LoudnessFactor *= donor.LoudnessFactor;
  }
  /* ********************************************************************************* */
  @Override public Singer Spawn_Singer() {// always always always override this
    throw new UnsupportedOperationException("Not supported yet.");
  }
  /* ********************************************************************************* */
  @Override public double MapTime(double ParentTime) {// convert time coordinate from my parent's frame to my child's frame
    return ParentTime - this.TimeOrg;
  }
  /* ********************************************************************************* */
  @Override public double UnMapTime(double ChildTime) {// convert time coordinate from my child's frame to my parent's frame
    return this.TimeOrg + ChildTime;
  }
  /* ********************************************************************************* */
  @Override public ISonglet GetContent() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  @Override public double TimeLoc_g() {
    return TimeOrg;
  }
  @Override public void TimeLoc_s(double value) {
    TimeOrg = value;
  }
  @Override public double OctaveLoc_g() {
    return OctaveLoc;
  }
  @Override public void OctaveLoc_s(double value) {
    OctaveLoc = value;
  }
  @Override public double LoudnessLoc_g() {
    return LoudnessFactor;
  }
  @Override public void LoudnessFactor_s(double value) {
    LoudnessFactor = value;
  }
}
