/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import java.awt.geom.Point2D;

/**
 *
 * @author MultiTool
 */

/* ********************************************************************************* */
public class CajaDelimitadora implements IDeletable {// DIY BoundingBox
  public Point2D.Double Min = new Point2D.Double(), Max = new Point2D.Double();
  public Point2D.Double[] Limits;
  /* ********************************************************************************* */
  public CajaDelimitadora() {
    this.Limits = new Point2D.Double[]{Min, Max};
  }
  /* ********************************************************************************* */
  public void Assign(double MinX, double MinY, double MaxX, double MaxY) {
    this.Min.setLocation(MinX, MinY);
    this.Max.setLocation(MaxX, MaxY);
    this.Sort_Me();
  }
  /* ********************************************************************************* */
  public void AssignCorner(int CornerNum, double MinLimit, double MaxLimit) {
    Point2D.Double pnt = this.Limits[CornerNum];
    pnt.setLocation(MinLimit, MaxLimit);
    this.Sort_Me();
    this.ZeroCheck();
  }
  /* ********************************************************************************* */
  public boolean Intersects(CajaDelimitadora other) {
    try {
      if (!this.LineFramed(this.Min.x, this.Max.x, other.Min.x, other.Max.x)) {
        return false;
      } else if (!this.LineFramed(this.Min.y, this.Max.y, other.Min.y, other.Max.y)) {
        return false;
      }
    } catch (Exception ex) {
      boolean nop = true;
    }
    return true;
  }
  /* ********************************************************************************* */
  public boolean LineFramed(double MyMin, double MyMax, double YourMin, double YourMax) {
    if (YourMax < MyMin) {// if my min is less than your max AND my max is greater than your min
      return false;
    } else if (MyMax < YourMin) {
      return false;
    }
    return true;
  }
  /* ********************************************************************************* */
  public void Rebase_Time(double TimeBase) {
    double TimeRange = this.Max.x - this.Min.x;
    this.Min.x = TimeBase;
    this.Max.x = this.Min.x + TimeRange;
  }
  /* ********************************************************************************* */
  public void Copy_From(CajaDelimitadora donor) {
    this.Min.setLocation(donor.Min);
    this.Max.setLocation(donor.Max);
  }
  /* ********************************************************************************* */
  public CajaDelimitadora Clone_Me() {
    CajaDelimitadora child = new CajaDelimitadora();
    child.Copy_From(this);
    return child;
  }
  /* ********************************************************************************* */
  public boolean ZeroCheck() { // for debugging
    if (this.Min.x == 0.0) {
      if (this.Min.y == 0.0) {
        if (this.Max.x == 0.0) {
          if (this.Max.y == 0.0) {
            return true;
          }
        }
      }
    }
    return false;
  }
  /* ********************************************************************************* */
  public void Sort_Me() {
    double temp;
    if (this.Max.x < this.Min.x) {
      temp = this.Max.x;// swap
      this.Max.x = this.Min.x;
      this.Min.x = temp;
    }
    if (this.Max.y < this.Min.y) {
      temp = this.Max.y;// swap
      this.Max.y = this.Min.y;
      this.Min.y = temp;
    }
    this.ZeroCheck();
  }
  /* ********************************************************************************* */
  public double GetWidth() {
    return Math.abs(this.Max.x - this.Min.x);
  }
  /* ********************************************************************************* */
  public double GetHeight() {
    return Math.abs(this.Max.y - this.Min.y);
  }
  /* ********************************************************************************* */
  public void Reset() {// reset for min, max comparisons
    this.Min.setLocation(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    this.Max.setLocation(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
    this.ZeroCheck();
  }
  /* ********************************************************************************* */
  public void Include(CajaDelimitadora other) {// for aggregating with all of my child boxes
    this.Min.x = Math.min(this.Min.x, other.Min.x);
    this.Min.y = Math.min(this.Min.y, other.Min.y);
    this.Max.x = Math.max(this.Max.x, other.Max.x);
    this.Max.y = Math.max(this.Max.y, other.Max.y);
    this.ZeroCheck();
  }
  /* ********************************************************************************* */
  public void IncludePoint(Point2D.Double other) {// for aggregating with vertices
    IncludePoint(other.x, other.y);
    this.ZeroCheck();
  }
  /* ********************************************************************************* */
  public void IncludePoint(double OtherX, double OtherY) {// for aggregating with vertices
    this.Min.x = Math.min(this.Min.x, OtherX);
    this.Min.y = Math.min(this.Min.y, OtherY);
    this.Max.x = Math.max(this.Max.x, OtherX);
    this.Max.y = Math.max(this.Max.y, OtherY);
    this.ZeroCheck();
  }
  /* ********************************************************************************* */
  public void Map(OffsetBox MapBox, CajaDelimitadora results) {
    results.Min.x = MapBox.MapTime(this.Min.x);
    results.Max.x = MapBox.MapTime(this.Max.x);
    results.Min.y = MapBox.MapPitch(this.Min.y);
    results.Max.y = MapBox.MapPitch(this.Max.y);
    results.ZeroCheck();
  }
  /* ********************************************************************************* */
  public void UnMap(OffsetBox MapBox, CajaDelimitadora results) {
    if (results == null || MapBox == null) {
      boolean nop = true;
    }
    results.Min.x = MapBox.UnMapTime(this.Min.x);
    results.Max.x = MapBox.UnMapTime(this.Max.x);
    results.Min.y = MapBox.UnMapPitch(this.Min.y);
    results.Max.y = MapBox.UnMapPitch(this.Max.y);
    results.ZeroCheck();
  }
  /* ********************************************************************************* */
  @Override public boolean Create_Me() {// IDeletable
    return true;
  }
  @Override public void Delete_Me() {// IDeletable
    this.Min = null;// if you can't delete it, at least mess it up so it can't be reused without exploding
    this.Max = null;
    this.Limits = null;
  }
}
