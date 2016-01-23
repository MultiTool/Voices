/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import java.awt.Color;
import java.awt.geom.Point2D;

/**
 *
 * @author MultiTool
 */
/* ********************************************************************************* */
public class VoicePoint extends MonkeyBox {
  public double SubTime = 0.0;// SubTime is cumulative subjective time.

  // graphics support, will move to separate object
  double OctavesPerLoudness = 0.125;// to do: loudness will have to be mapped to screen. not a pixel value right?
  public LoudnessHandle UpHandle, DownHandle;
  /* ********************************************************************************* */
  public VoicePoint() {
    this.Create_Me();
    this.UpHandle = new LoudnessHandle();
    this.UpHandle.ParentPoint = this;
    this.DownHandle = new LoudnessHandle();
    this.DownHandle.ParentPoint = this;
  }
  /* ********************************************************************************* */
  public void CopyFrom(VoicePoint source) {
    super.Copy_From(source);
    this.SubTime = source.SubTime;
  }
  /* ********************************************************************************* */
  public double GetFrequencyFactor() {
    return Math.pow(2.0, this.OctaveY);
  }
  /* ********************************************************************************* */
  @Override public void Draw_Me(IDrawable.Drawing_Context ParentDC) {// IDrawable
    // Control points have the same space as their parent, so no need to create a local map.
    Point2D.Double pnt = ParentDC.To_Screen(this.TimeX, this.OctaveY);
    double RadiusPixels = Math.abs(ParentDC.GlobalOffset.ScaleY) * OctavesPerRadius;
    RadiusPixels = Math.ceil(RadiusPixels);
    double DiameterPixels = RadiusPixels * 2.0;
    this.UpHandle.Draw_Me(ParentDC);
    // ParentDC.gr.setColor(ToAlpha(Color.green, 200));
    ParentDC.gr.setColor(Globals.ToAlpha(Color.yellow, 200));// control point just looks like a dot
    ParentDC.gr.fillOval((int) (pnt.x - RadiusPixels), (int) (pnt.y - RadiusPixels), (int) DiameterPixels, (int) DiameterPixels);
    ParentDC.gr.setColor(Globals.ToAlpha(Color.darkGray, 200));
    ParentDC.gr.drawOval((int) (pnt.x - RadiusPixels), (int) (pnt.y - RadiusPixels), (int) DiameterPixels, (int) DiameterPixels);
  }
  /* ********************************************************************************* */
  @Override public CajaDelimitadora GetBoundingBox() {
    return this.MyBounds;
  }
  /* ********************************************************************************* */
  @Override public void UpdateBoundingBox() {// IDrawable
    this.UpHandle.UpdateBoundingBox();
    this.UpdateBoundingBoxLocal();// Points have no children, nothing else to do.
  }
  @Override public void UpdateBoundingBoxLocal() {// IDrawable
    double LoudnessHeight = LoudnessFactor * OctavesPerLoudness;// Map loudness to screen pixels.
    double MinX = TimeX - OctavesPerRadius;
    double MaxX = TimeX + OctavesPerRadius;
    double HeightRad = Math.max(OctavesPerRadius, LoudnessHeight);
    double MinY = OctaveY - HeightRad;
    double MaxY = OctaveY + HeightRad;
    this.MyBounds.Assign(MinX, MinY, MaxX, MaxY);

    this.MyBounds.Include(this.UpHandle.GetBoundingBox());// Don't have to UnMap in this case because my points are already in my internal coordinates.
  }
  /* ********************************************************************************* */
  @Override public void GoFishing(Grabber Scoop) {// IDrawable
    System.out.print(" Point GoFishing: ");
    if (Scoop.CurrentContext.SearchBounds.Intersects(MyBounds)) {
      System.out.print(" InBounds, ");
      this.UpHandle.GoFishing(Scoop);
      if (this.HitsMe(Scoop.CurrentContext.Loc.x, Scoop.CurrentContext.Loc.y)) {
        System.out.print(" Was Hit, ");
        Scoop.ConsiderLeaf(this);
      }
    }
    System.out.println();
  }
  @Override public boolean HitsMe(double XLoc, double YLoc) {// IDrawable.IMoveable
    System.out.print("** Point HitsMe:");
    boolean Hit = false;
    if (this.MyBounds.Contains(XLoc, YLoc)) {
      System.out.print(" InBounds ");
      double dist = Math.hypot(XLoc - this.TimeX, YLoc - this.OctaveY);
      if (dist <= this.OctavesPerRadius) {
        System.out.print(" Hit!");
        Hit = true;
      } else {
        System.out.print(" Missed!");
      }
    } else {
      System.out.print(" OutBounds ");
    }
    return Hit;
  }
  @Override public void MoveTo(double XLoc, double YLoc) {// IDrawable.IMoveable
    if (XLoc >= 0) {// don't go backward in time
      this.TimeX = XLoc;
    }
    this.OctaveY = YLoc;
  }
  @Override public void SetSelected(boolean Selected) {// IDrawable.IMoveable
  }
  /* ********************************************************************************* */
  @Override public VoicePoint Clone_Me() {// ICloneable
    VoicePoint child = new VoicePoint();
    child.Copy_From(this);
    return child;
  }
  /* ********************************************************************************* */
  @Override public VoicePoint Deep_Clone_Me() {// ICloneable
    VoicePoint child = new VoicePoint();
    child.Copy_From(this);
    (child.UpHandle = this.UpHandle.Deep_Clone_Me()).ParentPoint = this; //child.UpHandle.ParentPoint = child;
    (child.DownHandle = this.DownHandle.Deep_Clone_Me()).ParentPoint = this; //child.DownHandle.ParentPoint = child;
    return child;
  }
  /* ********************************************************************************* */
  public void Copy_From(VoicePoint donor) {
    this.SubTime = donor.SubTime;
    this.OctavesPerLoudness = donor.OctavesPerLoudness;
    // this.UpHandle = donor.UpHandle; this.DownHandle = donor.DownHandle;
  }
  /* ********************************************************************************* */
  @Override public boolean Create_Me() {// IDeletable
    return true;
  }
  @Override public void Delete_Me() {// IDeletable
    this.MyBounds.Delete_Me();
    this.MyBounds = null;
    this.UpHandle.Delete_Me();
    this.UpHandle = null;
    this.DownHandle.Delete_Me();
    this.DownHandle = null;
  }
  /* ********************************************************************************* */
  public static class LoudnessHandle implements IDrawable.IMoveable, IDeletable {
    public CajaDelimitadora MyBounds = new CajaDelimitadora();
    public VoicePoint ParentPoint;
    public double OctavesPerRadius = 0.02;
    /* ********************************************************************************* */
    public double GetX() {
      return this.ParentPoint.TimeX;
    }
    public double GetY() {
      double LoudnessHeight = this.ParentPoint.LoudnessFactor * this.ParentPoint.OctavesPerLoudness;// Map loudness to screen pixels.
      return this.ParentPoint.OctaveY + LoudnessHeight;
    }
    @Override public void MoveTo(double XLoc, double YLoc) {// IDrawable.IMoveable
      if (XLoc >= 0) {// don't go backward in time
        this.ParentPoint.TimeX = XLoc;
      }
      double RelativeY = YLoc - this.ParentPoint.OctaveY;
      if (RelativeY >= 0) {// non negative loudness
        RelativeY /= this.ParentPoint.OctavesPerLoudness;
        if (RelativeY <= 1.0) {// shouldn't amplify loudness above 1.0, so that we can keep wave clipping under control
          this.ParentPoint.LoudnessFactor = RelativeY;
        }
      }
    }
    @Override public boolean HitsMe(double XLoc, double YLoc) {// IDrawable.IMoveable
      System.out.print("** LoudnessHandle HitsMe:");
      boolean Hit = false;
      if (this.MyBounds.Contains(XLoc, YLoc)) {
        System.out.print(" InBounds ");
        double dist = Math.hypot(XLoc - this.GetX(), YLoc - (this.GetY() + this.OctavesPerRadius));
        if (dist <= this.OctavesPerRadius) {
          System.out.print(" Hit!");
          Hit = true;
        } else {
          System.out.print(" Missed!");
        }
      } else {
        System.out.print(" OutBounds ");
      }
      return Hit;
    }
    @Override public void SetSelected(boolean Selected) {// IDrawable.IMoveable
    }
    @Override public void Draw_Me(Drawing_Context ParentDC) {
      // Control points have the same space as their parent, so no need to create a local map.
      Point2D.Double pnt = ParentDC.To_Screen(this.ParentPoint.TimeX, this.ParentPoint.OctaveY);
      double RadiusPixels = Math.abs(ParentDC.GlobalOffset.ScaleY) * OctavesPerRadius;
      double LoudnessHgt = this.ParentPoint.LoudnessFactor * this.ParentPoint.OctavesPerLoudness;
      double YlocHigh = ParentDC.GlobalOffset.UnMapPitch(this.ParentPoint.OctaveY + LoudnessHgt) - RadiusPixels;// My handle rests *upon* the line I control, so I don't occlude my VoicePoint. 

      RadiusPixels = Math.ceil(RadiusPixels);
      double DiameterPixels = RadiusPixels * 2.0;
      // ParentDC.gr.setColor(ToAlpha(Color.green, 200));
      ParentDC.gr.setColor(Globals.ToAlpha(Color.lightGray, 100));// control point just looks like a dot
      ParentDC.gr.fillOval((int) (pnt.x - RadiusPixels), (int) (YlocHigh - RadiusPixels), (int) DiameterPixels, (int) DiameterPixels);
      ParentDC.gr.setColor(Globals.ToAlpha(Color.darkGray, 200));
      ParentDC.gr.drawOval((int) (pnt.x - RadiusPixels), (int) (YlocHigh - RadiusPixels), (int) DiameterPixels, (int) DiameterPixels);
    }
    @Override public CajaDelimitadora GetBoundingBox() {
      return this.MyBounds;
    }
    @Override public void UpdateBoundingBox() {
      this.UpdateBoundingBoxLocal();
    }
    @Override public void UpdateBoundingBoxLocal() {
      double XLoc = this.GetX();
      double YLoc = this.GetY() + this.OctavesPerRadius;// *upon* the line
      double MinX = XLoc - this.OctavesPerRadius;
      double MaxX = XLoc + this.OctavesPerRadius;
      double MinY = YLoc - this.OctavesPerRadius;
      double MaxY = YLoc + this.OctavesPerRadius;
      this.MyBounds.Assign(MinX, MinY, MaxX, MaxY);
    }
    @Override public void GoFishing(Grabber Scoop) {
      System.out.println();
      System.out.print(" LoudnessHandle GoFishing: ");
      if (Scoop.CurrentContext.SearchBounds.Intersects(MyBounds)) {
        System.out.print(" InBounds, ");
        if (this.HitsMe(Scoop.CurrentContext.Loc.x, Scoop.CurrentContext.Loc.y)) {
          System.out.print(" Was Hit, ");
          Scoop.ConsiderLeaf(this);
        }
      }
      System.out.println();
    }
    /* ********************************************************************************* */
    @Override public LoudnessHandle Clone_Me() {// ICloneable
      LoudnessHandle child = new LoudnessHandle();
      return child;
    }
    /* ********************************************************************************* */
    @Override public LoudnessHandle Deep_Clone_Me() {// ICloneable
      LoudnessHandle child = new LoudnessHandle();
      child.OctavesPerRadius = this.OctavesPerRadius;
      child.ParentPoint = this.ParentPoint;
      child.MyBounds.Copy_From(this.MyBounds);
      return child;
    }
    /* ********************************************************************************* */
    @Override public boolean Create_Me() {
      return true;
    }
    @Override public void Delete_Me() {
      this.ParentPoint = null;
      this.MyBounds.Delete_Me();
      this.MyBounds = null;
    }
  }
}
