package voices;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.HashMap;

/**
 *
 * @author MultiTool
 * 
 // * MonkeyBox will be a base class for both OffsetBox and Voice Point. 
 // * It is any movable point that can contain other movable points. 
 // * The name is a place holder until something better comes to mind.
 * 
 */
public class MonkeyBox implements IDrawable.IMoveable, IDeletable, ITextable {// location box to transpose in pitch, move in time, etc.  //IMonkeyBox, 
  public double TimeX = 0, OctaveY = 0, LoudnessFactor = 1.0;// all of these are in parent coordinates
  double ScaleX = 1.0, ScaleY = 1.0; // to be used for pixels per second, pixels per octave
  public CajaDelimitadora MyBounds = new CajaDelimitadora();
  public ISonglet MyParentSong;// can do this but not used yet

  // graphics support, will move to separate object
  //double OctavesPerRadius = 0.03;
  double OctavesPerRadius = 0.01;
  boolean IsSelected = false;

  /* ********************************************************************************* */
  public MonkeyBox() {
    //this.Clear();
    this.Create_Me();
    // MyBounds = new CajaDelimitadora();
    this.MyBounds.Reset();
  }
  /* ********************************************************************************* */
  @Override public MonkeyBox Clone_Me() {// ICloneable
    MonkeyBox child = new MonkeyBox();
    child.Copy_From(this);
    return child;
  }
  /* ********************************************************************************* */
  @Override public MonkeyBox Deep_Clone_Me() {// ICloneable
    MonkeyBox child = this.Clone_Me();
    return child;
  }
  /* ********************************************************************************* */
  public void Copy_From(MonkeyBox donor) {
    this.TimeX = donor.TimeX;
    this.OctaveY = donor.OctaveY;
    this.LoudnessFactor = donor.LoudnessFactor;
    this.ScaleX = donor.ScaleX;
    this.ScaleY = donor.ScaleY;
    this.MyParentSong = donor.MyParentSong;
    this.OctavesPerRadius = donor.OctavesPerRadius;
    this.MyBounds.Copy_From(donor.MyBounds);
  }
  /* ********************************************************************************* */
  public void Clear() {// set all coordinates to identity, no transformation for content
    TimeX = OctaveY = 0.0;
    LoudnessFactor = 1.0;
    ScaleX = ScaleY = 1.0;
  }
  /* ********************************************************************************* */
  public double Get_Max_Amplitude() {
    return this.LoudnessFactor;
  }
  /* ********************************************************************************* */
  public void Compound(MonkeyBox donor) {
    this.TimeX += (this.ScaleX * donor.TimeX);// to do: combine matrices here. 
    this.OctaveY += (this.ScaleY * donor.OctaveY);
    this.LoudnessFactor *= donor.LoudnessFactor;
    this.ScaleX *= donor.ScaleX;
    this.ScaleY *= donor.ScaleY;
  }
  /* ********************************************************************************* */
  public void RescaleTimeX(double Factor) {
    this.ScaleX = Factor;
  }
  /* ********************************************************************************* */
  public void Rebase_Time(double Time) {
    this.TimeX = Time;
    double RelativeMinBound = this.MyBounds.Min.x;// preserve the relative relationship of my bounds and my origin.
    this.MyBounds.Rebase_Time(Time + RelativeMinBound);
  }
  // <editor-fold defaultstate="collapsed" desc="Mappings and Unmappings">
  /* ********************************************************************************* */
  public double MapTime(double ParentTime) {// convert time coordinate from my parent's frame to my child's frame
    return ((ParentTime - this.TimeX) / ScaleX); // in the long run we'll probably use a matrix
  }
  /* ********************************************************************************* */
  public double UnMapTime(double ChildTime) {// convert time coordinate from my child's frame to my parent's frame
    return this.TimeX + ((ChildTime) * ScaleX);
  }
  /* ********************************************************************************* */
  public double MapPitch(double ParentPitch) {// convert octave coordinate from my parent's frame to my child's frame
    return ((ParentPitch - this.OctaveY) / ScaleY);
  }
  /* ********************************************************************************* */
  public double UnMapPitch(double ChildPitch) {// convert octave coordinate from my child's frame to my parent's frame
    return this.OctaveY + ((ChildPitch) * ScaleY);
  }
  /* ********************************************************************************* */
  public Point2D.Double MapTo(double XLoc, double YLoc) {
    Point2D.Double pnt = new Point2D.Double(this.MapTime(XLoc), this.MapPitch(YLoc));
    return pnt;
  }
  /* ********************************************************************************* */
  public Point2D.Double UnMap(double XLoc, double YLoc) {
    Point2D.Double pnt = new Point2D.Double(this.UnMapTime(XLoc), this.UnMapPitch(YLoc));
    return pnt;
  }
  /* ********************************************************************************* */
  public void MapTo(double XLoc, double YLoc, Point2D.Double results) {
    results.setLocation(this.MapTime(XLoc), this.MapPitch(YLoc));
  }
  /* ********************************************************************************* */
  public void UnMap(double XLoc, double YLoc, Point2D.Double results) {
    results.setLocation(this.UnMapTime(XLoc), this.UnMapPitch(YLoc));
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
  // </editor-fold>
  /* ********************************************************************************* */
  @Override public void Draw_Me(DrawingContext ParentDC) {// IDrawable
  }
  /* ********************************************************************************* */
  public void Draw_Dot(DrawingContext ParentDC, Color col) {
    Point2D.Double pnt = ParentDC.To_Screen(this.TimeX, this.OctaveY);
    double extra = (1.0 / (double) ParentDC.RecurseDepth);
//    double RadiusPixels = Math.abs(ParentDC.GlobalOffset.ScaleY) * (OctavesPerRadius + extra * 0.02);
    double RadiusPixels = Math.abs(ParentDC.GlobalOffset.ScaleY) * (OctavesPerRadius);
    RadiusPixels = Math.ceil(RadiusPixels);
    double DiameterPixels = RadiusPixels * 2.0;
    ParentDC.gr.setColor(Globals.ToAlpha(col, 200));// control point just looks like a dot
    ParentDC.gr.fillOval((int) (pnt.x - RadiusPixels), (int) (pnt.y - RadiusPixels), (int) DiameterPixels, (int) DiameterPixels);
    ParentDC.gr.setColor(Globals.ToAlpha(Color.darkGray, 200));
    ParentDC.gr.drawOval((int) (pnt.x - RadiusPixels), (int) (pnt.y - RadiusPixels), (int) DiameterPixels, (int) DiameterPixels);
  }
  /* ********************************************************************************* */
  public static void Draw_Dot2(DrawingContext DC, double XCtr, double YCtr, double OctavesPerRadius, boolean Selected, Color col) {
    // #kludgey, hacky.  Need to create the gradient only once I guess, rather than every time a point is moved. 
    Paint oldpaint = DC.gr.getPaint();
    Point2D.Double pnt = DC.To_Screen(XCtr, YCtr);
    double RadiusPixels = Math.abs(DC.GlobalOffset.ScaleY) * (OctavesPerRadius);
    RadiusPixels = Math.ceil(RadiusPixels);
    double DiameterPixels = RadiusPixels * 2.0;
    if (Selected) {// add glow for selected objects
      float[] dist = {0.0f, 0.49f, 0.5f, 1.0f};
      Color[] colors = {Globals.ToAlpha(Color.red, 0), Globals.ToAlpha(Color.red, 0), Color.red, Globals.ToAlpha(Color.red, 1)};
      double GradRadius = RadiusPixels * 2;
      double GradDiameter = GradRadius * 2;
      pnt.setLocation(XCtr, YCtr);
      RadialGradientPaint paint = new RadialGradientPaint(pnt, (int) GradRadius, dist, colors);
      DC.gr.setPaint(paint);
      DC.gr.fillOval((int) (XCtr - GradRadius), (int) (YCtr - GradRadius), (int) GradDiameter, (int) GradDiameter);
      DC.gr.setPaint(oldpaint);
    }
    DC.gr.setColor(Globals.ToAlpha(col, 200));// control point just looks like a dot
    DC.gr.fillOval((int) (XCtr - RadiusPixels), (int) (YCtr - RadiusPixels), (int) DiameterPixels, (int) DiameterPixels);
    DC.gr.setColor(Globals.ToAlpha(Color.darkGray, 200));
    DC.gr.drawOval((int) (XCtr - RadiusPixels), (int) (YCtr - RadiusPixels), (int) DiameterPixels, (int) DiameterPixels);
//    DC.gr.fillOval((int) (pnt.x - RadiusPixels), (int) (pnt.y - RadiusPixels), (int) DiameterPixels, (int) DiameterPixels);
//    DC.gr.setColor(Globals.ToAlpha(Color.darkGray, 200));
//    DC.gr.drawOval((int) (pnt.x - RadiusPixels), (int) (pnt.y - RadiusPixels), (int) DiameterPixels, (int) DiameterPixels);
  }
  /* ********************************************************************************* */
  @Override public CajaDelimitadora GetBoundingBox() {// IDrawable
    return this.MyBounds;
  }
  @Override public void UpdateBoundingBox() {// IDrawable
  }
  @Override public void UpdateBoundingBoxLocal() {// IDrawable
  }
  @Override public void GoFishing(Grabber Scoop) {// IDrawable
  }
  @Override public void MoveTo(double XLoc, double YLoc) {// IDrawable.IMoveable
    if (XLoc >= 0) {// don't go backward in time
      this.TimeX = XLoc;
    }
    this.OctaveY = YLoc;
  }
  @Override public boolean HitsMe(double XLoc, double YLoc) {// IDrawable.IMoveable
    System.out.print("HitsMe:");
    if (this.MyBounds.Contains(XLoc, YLoc)) {// redundant test
      double dist = Math.hypot(XLoc - this.TimeX, YLoc - this.OctaveY);
      if (dist <= this.OctavesPerRadius) {
        System.out.println("true");
        return true;
      }
    }
    System.out.println("false");
    return false;
  }
  @Override public void SetSelected(boolean Selected) {// IDrawable.IMoveable
    this.IsSelected = Selected;
  }
  /* ********************************************************************************* */
  @Override public boolean Create_Me() {// IDeletable
    return true;
  }
  @Override public void Delete_Me() {// IDeletable
    this.MyBounds.Delete_Me();
    // also uref my contents
  }
  /* ********************************************************************************* */
  @Override public void ShallowLoad(JsonParse.Phrase phrase) {// ITextable
    HashMap<String, JsonParse.Phrase> Fields = phrase.ChildrenHash;
    this.TimeX = Double.parseDouble(IFactory.Utils.GetField(Fields, "TimeX", "0"));
    this.OctaveY = Double.parseDouble(IFactory.Utils.GetField(Fields, "OctaveY", "0"));
    this.LoudnessFactor = Double.parseDouble(IFactory.Utils.GetField(Fields, "LoudnessFactor", "1.0"));
    this.ScaleX = Double.parseDouble(IFactory.Utils.GetField(Fields, "ScaleX", "1.0"));
    this.ScaleY = Double.parseDouble(IFactory.Utils.GetField(Fields, "ScaleY", "1.0"));
    if (false) {
      this.OctavesPerRadius = Double.parseDouble(IFactory.Utils.GetField(Fields, "OctavesPerRadius", "0.01"));
      this.IsSelected = Boolean.parseBoolean(IFactory.Utils.GetField(Fields, "IsSelected", "false"));
    }
  }
  @Override public void Textify(StringBuilder sb) {// ITextable
    // or maybe we'd rather export to a Phrase tree first? might be easier, less redundant { and } code. 
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  @Override public JsonParse.Phrase Export(CollisionTable CTable) {// ITextable
    JsonParse.Phrase phrase = new JsonParse.Phrase();
    if (CTable.table.containsKey(this)) {
      phrase.ItemPtr = CTable.table.get(this).ItemPtr;
      return phrase;
    }
    CTable.InsertNewItem(this);
    phrase.ItemPtr = CTable.table.get(this).ItemPtr;

    phrase.ChildrenHash = new HashMap<String, JsonParse.Phrase>();
    HashMap<String, JsonParse.Phrase> Fields = phrase.ChildrenHash;
    Fields.put("TimeX", IFactory.Utils.PackField(this.TimeX));
    Fields.put("OctaveY", IFactory.Utils.PackField(this.OctaveY));
    Fields.put("LoudnessFactor", IFactory.Utils.PackField(this.LoudnessFactor));
    Fields.put("ScaleX", IFactory.Utils.PackField(this.ScaleX));
    Fields.put("ScaleY", IFactory.Utils.PackField(this.ScaleY));
    if (false) {
      Fields.put("OctavesPerRadius", IFactory.Utils.PackField(this.OctavesPerRadius));
      Fields.put("IsSelected", IFactory.Utils.PackField(this.IsSelected));
    }
    /*
     One issue is how to automagically convert pointers?  Those should be generated by the collision table. 
     */
    // Fields.put("MyBounds", MyBounds.Export(CTable));
    return phrase;
  }
}
