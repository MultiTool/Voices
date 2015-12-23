/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import java.awt.Color;
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
public class OffsetBox implements IDrawable, IDeletable {// location box to transpose in pitch, move in time, etc.  //IOffsetBox, 
  public double TimeOrg = 0, OctaveLoc = 0, LoudnessFactor = 1.0;// all of these are in parent coordinates
  double ScaleX = 1.0, ScaleY = 1.0;// to be used for pixels per second, pixels per octave
  double ChildXorg = 0, ChildYorg = 0;// These are only non-zero for graphics. Audio origins are always 0,0. 
  public CajaDelimitadora MyBounds;
  /* ********************************************************************************* */
  public OffsetBox() {
    //this.Clear();
    this.Create_Me();
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
  public void Compound(OffsetBox donor) {
    //double DonorOffset = donor.TimeLoc_g(); double DonorScale = donor.ScaleX_g();
    this.TimeLoc_s(TimeOrg + (this.ScaleX * donor.TimeLoc_g()));// to do: combine matrices here. 
    this.OctaveLoc_s(OctaveLoc + (this.ScaleY * donor.OctaveLoc_g()));
    this.LoudnessFactor_s(LoudnessFactor + donor.LoudnessLoc_g());
    this.ScaleX *= donor.ScaleX_g();
    this.ScaleY *= donor.ScaleY_g();
    //this.TimeLoc += donor.TimeLoc; this.OctaveLoc += donor.OctaveLoc; this.LoudnessFactor *= donor.LoudnessFactor;
  }
  /* ********************************************************************************* */
  private void CombineTransform1D(double FirstScale, double FirstOffset, double SecondScale, double SecondOffset) {// note to self
    SecondOffset += (SecondScale * FirstOffset);
    SecondScale *= FirstScale;
  }
  /* ********************************************************************************* */
  private double ShrinkTransform1D(double FirstOffset, double SecondScale, double SecondOffset) {// note to self
    SecondOffset += (SecondScale * FirstOffset);
    return SecondOffset;
  }
  /* ********************************************************************************* */
  public Singer Spawn_Singer() {// always always always override this
    throw new UnsupportedOperationException("Not supported yet.");
  }
  /* ********************************************************************************* */
  public double MapTime(double ParentTime) {// convert time coordinate from my parent's frame to my child's frame
    return ((ParentTime - this.TimeOrg) / ScaleX) + ChildXorg; // in the long run we'll probably use a matrix
//    childX = ((parentX - parentXorg) * ScaleX) + childXorg; // full mapping 
//    parentX = ((childX - childXorg) / ScaleX) + parentXorg;
  }
  /* ********************************************************************************* */
  public double UnMapTime(double ChildTime) {// convert time coordinate from my child's frame to my parent's frame
    return this.TimeOrg + ((ChildTime - ChildXorg) * ScaleX);
  }
  /* ********************************************************************************* */
  public double MapPitch(double ParentPitch) {// convert octave coordinate from my parent's frame to my child's frame
    return ((ParentPitch - this.OctaveLoc) / ScaleY) + ChildYorg;
  }
  /* ********************************************************************************* */
  public double UnMapPitch(double ChildPitch) {// convert octave coordinate from my child's frame to my parent's frame
    return this.OctaveLoc + ((ChildPitch - ChildYorg) * ScaleY);
  }
  /* ********************************************************************************* */
  public ISonglet GetContent() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  public double TimeLoc_g() {
    return TimeOrg;
  }
  public void TimeLoc_s(double value) {
    TimeOrg = value;
  }
  public double OctaveLoc_g() {
    return OctaveLoc;
  }
  public void OctaveLoc_s(double value) {
    OctaveLoc = value;
  }
  public double LoudnessLoc_g() {
    return LoudnessFactor;
  }
  public void LoudnessFactor_s(double value) {
    LoudnessFactor = value;
  }
  public double ScaleX_g() {
    return ScaleX;
  }
  public void ScaleX_s(double value) {
    ScaleX = value;
  }
  public double ScaleY_g() {
    return ScaleY;
  }
  public void ScaleY_s(double value) {
    ScaleX = value;
  }
  OffsetBox Clone_Me() {
    OffsetBox child = new OffsetBox();
    child.TimeOrg = this.TimeOrg;
    child.OctaveLoc = this.OctaveLoc;
    child.LoudnessFactor = this.LoudnessFactor;
    child.ScaleX = this.ScaleX;
    child.ScaleY = this.ScaleY;
    return child;
  }
  /* ********************************************************************************* */
  @Override public void Draw_Me(Drawing_Context ParentDC) {// IDrawable
    if (false) {
      this.MyBounds.Sort_Me();
      int rx0 = (int) ParentDC.GlobalOffset.UnMapTime(this.MyBounds.Min.x);
      int rx1 = (int) ParentDC.GlobalOffset.UnMapTime(this.MyBounds.Max.x);
      int ry0 = (int) ParentDC.GlobalOffset.UnMapPitch(this.MyBounds.Min.y);
      int ry1 = (int) ParentDC.GlobalOffset.UnMapPitch(this.MyBounds.Max.y);
      if (ry1 < ry0) {// swap
        int temp = ry1;
        ry1 = ry0;
        ry0 = temp;
      }
      int wdt = rx1 - rx0;
      int hgt = ry1 - ry0;
      int cint = Globals.RandomGenerator.nextInt() % 256;
      Color col = new Color(cint);
      ParentDC.gr.setColor(col);//Color.magenta
      ParentDC.gr.drawRect(rx0, ry0, wdt, hgt);
    }
    if (ParentDC.ClipBounds.Intersects(MyBounds)) {// If we make ISonglet also drawable then we can stop repeating this code and put it all in OffsetBox.
      Drawing_Context ChildDC = new Drawing_Context(ParentDC, this);
      ISonglet Content = this.GetContent();
      Content.Draw_Me(ChildDC);
    }
  }
  @Override public CajaDelimitadora GetBoundingBox() {// IDrawable
    return this.MyBounds;
  }
  @Override public void UpdateBoundingBox() {// IDrawable
    ISonglet Content = this.GetContent();
    Content.UpdateBoundingBox();
    Content.GetBoundingBox().UnMap(this, MyBounds);// project child limits into parent (my) space
    this.MyBounds.Sort_Me();// almost never needed
  }
  /* ********************************************************************************* */
  @Override public boolean Create_Me() {// IDeletable
    return true;
  }
  @Override public void Delete_Me() {// IDeletable
    this.MyBounds.Delete_Me();
  }
}
