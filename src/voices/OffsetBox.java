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
 * 
 * OffsetBox handles all affine transformations of your songlet. 
 * For audio, the only transformations handled are X and Y offsets of timing and pitch. No scaling or shearing etc. 
 * We do scale for graphics though. 
 * 
 */
public class OffsetBox implements IOffsetBox, IDrawable {// location box to transpose in pitch, move in time, etc. 
  public double TimeOrg = 0, OctaveLoc = 0, LoudnessFactor = 1.0;// all of these are in parent coordinates
  double ScaleX = 1.0, ScaleY = 1.0;// to be used for pixels per second, pixels per octave
  double ChildXorg = 0, ChildYorg = 0;// These are only non-zero for graphics. Audio origins are always 0,0. 
  public CajaDelimitadora MyBounds;
  /* ********************************************************************************* */
  public OffsetBox() {
    //this.Clear();
  }
  /* ********************************************************************************* */
  public void Clear() {// set all coordinates to identity, no transformation for content
    TimeOrg = OctaveLoc = 0.0;
    LoudnessFactor = 1.0;
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
    this.ScaleX *= donor.ScaleX_g();
    this.ScaleY *= donor.ScaleY_g();
    //this.TimeLoc += donor.TimeLoc; this.OctaveLoc += donor.OctaveLoc; this.LoudnessFactor *= donor.LoudnessFactor;
  }
  /* ********************************************************************************* */
  @Override public Singer Spawn_Singer() {// always always always override this
    throw new UnsupportedOperationException("Not supported yet.");
  }
  /* ********************************************************************************* */
  @Override public double MapTime(double ParentTime) {// convert time coordinate from my parent's frame to my child's frame
    return ((ParentTime - this.TimeOrg) / ScaleX) + ChildXorg; // in the long run we'll probably use a matrix
//    childX = ((parentX - parentXorg) * ScaleX) + childXorg; // full mapping 
//    parentX = ((childX - childXorg) / ScaleX) + parentXorg;
  }
  /* ********************************************************************************* */
  @Override public double UnMapTime(double ChildTime) {// convert time coordinate from my child's frame to my parent's frame
    return this.TimeOrg + ((ChildTime - ChildXorg) * ScaleX);
  }
  /* ********************************************************************************* */
  @Override public double MapPitch(double ParentPitch) {// convert octave coordinate from my parent's frame to my child's frame
    return ((ParentPitch - this.OctaveLoc) / ScaleY) + ChildYorg;
  }
  /* ********************************************************************************* */
  @Override public double UnMapPitch(double ChildPitch) {// convert octave coordinate from my child's frame to my parent's frame
    return this.OctaveLoc + ((ChildPitch - ChildYorg) * ScaleY);
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
  @Override public double ScaleX_g() {
    return ScaleX;
  }
  @Override public void ScaleX_s(double value) {
    ScaleX = value;
  }
  @Override public double ScaleY_g() {
    return ScaleY;
  }
  @Override public void ScaleY_s(double value) {
    ScaleX = value;
  }
  OffsetBox Clone_Me() {
    OffsetBox child = new OffsetBox();
    child.TimeOrg = this.TimeOrg;
    child.OctaveLoc = this.OctaveLoc;
    child.LoudnessFactor = this.LoudnessFactor;
    return child;
  }
  /* ********************************************************************************* */
  @Override public void Draw_Me(Drawing_Context ParentDC) {// IDrawable
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  @Override public CajaDelimitadora GetBoundingBox() {// IDrawable
    return this.MyBounds;
  }
  @Override public void UpdateBoundingBox() {// IDrawable
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
}
