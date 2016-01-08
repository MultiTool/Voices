/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Point2D;
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
public class OffsetBox implements IDrawable.IMoveable, IDeletable {// location box to transpose in pitch, move in time, etc.  //IOffsetBox, 
  public double TimeOrg = 0, OctaveLoc = 0, LoudnessFactor = 1.0;// all of these are in parent coordinates
  double ScaleX = 1.0, ScaleY = 1.0;// to be used for pixels per second, pixels per octave
  double ChildXorg = 0, ChildYorg = 0;// These are only non-zero for graphics. Audio origins are always 0,0. 
  public CajaDelimitadora MyBounds;
  public ISonglet MyParentSong;// can do this but not used yet

  // graphics support, will move to separate object
  double OctavesPerRadius = 0.025;
  /* ********************************************************************************* */
  public OffsetBox() {
    //this.Clear();
    this.Create_Me();
    MyBounds = new CajaDelimitadora();
    this.MyBounds.Reset();
  }
  /* ********************************************************************************* */
  public OffsetBox Clone_Me() {
    OffsetBox child = new OffsetBox();
    child.Copy_From(this);
    return child;
  }
  /* ********************************************************************************* */
  public void Copy_From(OffsetBox donor) {
    this.TimeOrg = donor.TimeOrg;
    this.OctaveLoc = donor.OctaveLoc;
    this.LoudnessFactor = donor.LoudnessFactor;
    this.ScaleX = donor.ScaleX;
    this.ScaleY = donor.ScaleY;
    this.ChildXorg = donor.ChildXorg;
    this.ChildYorg = donor.ChildYorg;
    this.MyParentSong = donor.MyParentSong;
    this.OctavesPerRadius = donor.OctavesPerRadius;
    this.MyBounds.Copy_From(donor.MyBounds);
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
  public void Zoom(double XCtr, double YCtr, double Scale) {
    double XMov = XCtr - (Scale * XCtr);
    double YMov = YCtr - (Scale * YCtr);

    this.TimeOrg = XMov + (Scale * this.TimeOrg);
    this.OctaveLoc = YMov + (Scale * this.OctaveLoc);

    this.ScaleX *= Scale;
    this.ScaleY *= Scale;
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
  public void Rebase_Time(double Time) {
    this.TimeOrg = Time;
    double RelativeMinBound = this.MyBounds.Min.x;// preserve the relative relationship of my bounds and my origin.
    this.MyBounds.Rebase_Time(Time + RelativeMinBound);
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
  public Point2D.Double MapTo(double XLoc, double YLoc) {
    Point2D.Double pnt = new Point2D.Double(this.UnMapTime(XLoc), this.UnMapPitch(YLoc));
    return pnt;
  }
  /* ********************************************************************************* */
  public Point2D.Double UnMap(double XLoc, double YLoc) {
    Point2D.Double pnt = new Point2D.Double(this.MapTime(XLoc), this.MapPitch(YLoc));
    return pnt;
  }
  /* ********************************************************************************* */
  public void MapTo(Point2D.Double pnt, Point2D.Double results) {
    results.x = this.MapTime(pnt.x);
    results.y = this.MapPitch(pnt.y);
  }
  /* ********************************************************************************* */
  public void UnMap(Point2D.Double pnt, Point2D.Double results) {
    results.x = this.UnMapTime(pnt.x);
    results.y = this.UnMapPitch(pnt.y);
  }
  /* ********************************************************************************* */
  public void MapTo(CajaDelimitadora source, CajaDelimitadora results) {
    this.MapTo(source.Min, results.Min);
    this.MapTo(source.Max, results.Max);
    results.Sort_Me();
  }
  /* ********************************************************************************* */
  public void UnMap(CajaDelimitadora source, CajaDelimitadora results) {
    this.UnMap(source.Min, results.Min);
    this.UnMap(source.Max, results.Max);
    results.Sort_Me();
  }
  /* ********************************************************************************* */
  public ISonglet GetContent() {// always always override this
    throw new UnsupportedOperationException("Not supported yet.");//  public abstract ISonglet GetContent(); ? 
  }
  /* ********************************************************************************* */
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
  /* ********************************************************************************* */
  @Override public void Draw_Me(Drawing_Context ParentDC) {// IDrawable
    //Draw_My_Bounds(ParentDC);
    if (ParentDC.ClipBounds.Intersects(MyBounds)) {// If we make ISonglet also drawable then we can stop repeating this code and put it all in OffsetBox.

      Point2D.Double pnt = ParentDC.To_Screen(this.TimeOrg, this.OctaveLoc);
      double extra = (1.0 / (double) ParentDC.RecurseDepth);
      //extra *= 0.02;
      double RadiusPixels = Math.abs(ParentDC.GlobalOffset.ScaleY) * (OctavesPerRadius + extra * 0.02);
      RadiusPixels = Math.ceil(RadiusPixels);
      double DiameterPixels = RadiusPixels * 2.0;
      Color col = Globals.ToRainbow(extra);
      if (false) {
        ParentDC.gr.setColor(Globals.ToAlpha(col, 200));// control point just looks like a dot
        ParentDC.gr.fillOval((int) (pnt.x - RadiusPixels), (int) (pnt.y - RadiusPixels), (int) DiameterPixels, (int) DiameterPixels);
        ParentDC.gr.setColor(Globals.ToAlpha(Color.darkGray, 200));
        ParentDC.gr.drawOval((int) (pnt.x - RadiusPixels), (int) (pnt.y - RadiusPixels), (int) DiameterPixels, (int) DiameterPixels);
      }
      this.Draw_Dot(ParentDC, col);
      // maybe reduce ChildDC's clipbounds to intersect with my own bounds? after all none of my children should go outside my bounds
      // eh, that would conflict with dragging my child outside of my bounds - it would stop being drawn during the drag. 
      Drawing_Context ChildDC = new Drawing_Context(ParentDC, this);// In C++ ChildDC will be a local variable from the stack, not heap. 
      ISonglet Content = this.GetContent();
      Content.Draw_Me(ChildDC);
      ChildDC.Delete_Me();
    }
  }
  /* ********************************************************************************* */
  public void Draw_Dot(Drawing_Context ParentDC, Color col) {
    Point2D.Double pnt = ParentDC.To_Screen(this.TimeOrg, this.OctaveLoc);
    double extra = (1.0 / (double) ParentDC.RecurseDepth);
//    double RadiusPixels = Math.abs(ParentDC.GlobalOffset.ScaleY) * (OctavesPerRadius + extra * 0.02);
    double RadiusPixels = Math.abs(ParentDC.GlobalOffset.ScaleY) * (OctavesPerRadius);
    RadiusPixels = Math.ceil(RadiusPixels);
    double DiameterPixels = RadiusPixels * 2.0;
//    Color col = Globals.ToRainbow(extra);
//    col = Color.MAGENTA;
    ParentDC.gr.setColor(Globals.ToAlpha(col, 200));// control point just looks like a dot
    ParentDC.gr.fillOval((int) (pnt.x - RadiusPixels), (int) (pnt.y - RadiusPixels), (int) DiameterPixels, (int) DiameterPixels);
    ParentDC.gr.setColor(Globals.ToAlpha(Color.darkGray, 200));
    ParentDC.gr.drawOval((int) (pnt.x - RadiusPixels), (int) (pnt.y - RadiusPixels), (int) DiameterPixels, (int) DiameterPixels);
  }
  /* ********************************************************************************* */
  public void Draw_My_Bounds(Drawing_Context ParentDC) {// for debugging. break glass in case of emergency
    OffsetBox GlobalOffset = ParentDC.GlobalOffset;
    Graphics2D gr = ParentDC.gr;
    this.MyBounds.Sort_Me();
    int rx0 = (int) GlobalOffset.UnMapTime(this.MyBounds.Min.x);
    int rx1 = (int) GlobalOffset.UnMapTime(this.MyBounds.Max.x);
    int ry0 = (int) GlobalOffset.UnMapPitch(this.MyBounds.Min.y);
    int ry1 = (int) GlobalOffset.UnMapPitch(this.MyBounds.Max.y);
    if (ry1 < ry0) {// swap
      int temp = ry1;
      ry1 = ry0;
      ry0 = temp;
    }

    // thinner lines for more distal sub-branches
    double extra = (2.0 / (double) ParentDC.RecurseDepth);

    int buf = (int) Math.ceil(extra * 2);
    rx0 -= buf;
    rx1 += buf;
    ry0 -= buf;
    ry1 += buf;
    int wdt = rx1 - rx0;
    int hgt = ry1 - ry0;
    int cint = Globals.RandomGenerator.nextInt() % 256;
    Color col = new Color(cint);

    Stroke oldStroke = gr.getStroke();
    gr.setColor(Globals.ToAlpha(col, 100));

    BasicStroke bs = new BasicStroke((float) (1.0 + extra), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    gr.setStroke(bs);

    //ParentDC.gr.setColor(col);//Color.magenta
    gr.drawRect(rx0, ry0, wdt, hgt);

    gr.setStroke(oldStroke);
  }
  /* ********************************************************************************* */
  @Override public CajaDelimitadora GetBoundingBox() {// IDrawable
    return this.MyBounds;
  }
  @Override public void UpdateBoundingBox() {// IDrawable
    ISonglet Content = this.GetContent();
    Content.UpdateBoundingBox();
    this.UpdateBoundingBoxLocal();
  }
  @Override public void UpdateBoundingBoxLocal() {// IDrawable
    ISonglet Content = this.GetContent();
    Content.UpdateBoundingBoxLocal();// either this
    Content.GetBoundingBox().UnMap(this, MyBounds);// project child limits into parent (my) space
    // include my bubble in bounds
    this.MyBounds.IncludePoint(this.TimeOrg - OctavesPerRadius, this.OctaveLoc - OctavesPerRadius);
    this.MyBounds.IncludePoint(this.TimeOrg + OctavesPerRadius, this.OctaveLoc + OctavesPerRadius);
  }
  @Override public void GoFishing(HookAndLure Scoop) {// IDrawable
    if (Scoop.CurrentContext.SearchBounds.Intersects(MyBounds)) {
      if (this.HitsMe(Scoop.CurrentContext.Loc.x, Scoop.CurrentContext.Loc.y)) {
        Scoop.ConsiderLeaf(this);
      }
      Scoop.AddBoxToStack(this);
      this.GetContent().GoFishing(Scoop);
      Scoop.DecrementStack();
    }
  }
  @Override public void MoveTo(double XLoc, double YLoc) {// IDrawable.IMoveable
    if (XLoc >= 0) {// don't go backward in time
      this.TimeOrg = XLoc;
      this.OctaveLoc = YLoc;
    }
  }
  @Override public boolean HitsMe(double XLoc, double YLoc) {// IDrawable.IMoveable
    System.out.print("HitsMe:");
    if (this.MyBounds.Contains(XLoc, YLoc)) {// redundant test
      double dist = Math.hypot(XLoc - this.TimeOrg, YLoc - this.OctaveLoc);
      if (dist <= this.OctavesPerRadius) {
        System.out.println("true");
        return true;
      }
    }
    System.out.println("false");
    return false;
  }
  /* ********************************************************************************* */
  @Override public boolean Create_Me() {// IDeletable
    return true;
  }
  @Override public void Delete_Me() {// IDeletable
    this.MyBounds.Delete_Me();
  }
}
