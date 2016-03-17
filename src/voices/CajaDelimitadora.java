package voices;

import java.awt.geom.Point2D;
import java.util.HashMap;

/**
 *
 * @author MultiTool
 */

/* ********************************************************************************* */
public class CajaDelimitadora implements IDeletable, ITextable {// DIY BoundingBox
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
    if (!this.LineFramed(this.Min.x, this.Max.x, other.Min.x, other.Max.x)) {
      return false;
    } else if (!this.LineFramed(this.Min.y, this.Max.y, other.Min.y, other.Max.y)) {
      return false;
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
    if (this.Min == null || donor == null) {
      boolean nop = true;
    }
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
  public void Sort_Me() {// CajaDelimitadora bounds are ALWAYS to be sorted min->max, even if we are in an inverted space such as screen graphics. 
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
//  /* ********************************************************************************* */
//  public void Map(OffsetBox MapBox, CajaDelimitadora results) {
//    results.Min.x = MapBox.MapTime(this.Min.x);
//    results.Max.x = MapBox.MapTime(this.Max.x);
//    results.Min.y = MapBox.MapPitch(this.Min.y);
//    results.Max.y = MapBox.MapPitch(this.Max.y);
//    results.Sort_Me();
//  }
//  /* ********************************************************************************* */
//  public void UnMap(OffsetBox MapBox, CajaDelimitadora results) {
//    results.Min.x = MapBox.UnMapTime(this.Min.x);
//    results.Max.x = MapBox.UnMapTime(this.Max.x);
//    results.Min.y = MapBox.UnMapPitch(this.Min.y);
//    results.Max.y = MapBox.UnMapPitch(this.Max.y);
//    results.Sort_Me();
//  }
  /* ********************************************************************************* */
  @Override public boolean Create_Me() {// IDeletable
    return true;
  }
  @Override public void Delete_Me() {// IDeletable
    this.Min = null;// if you can't delete it, at least mess it up so it can't be reused without exploding
    this.Max = null;
    this.Limits = null;
  }
  public boolean Contains(double XLoc, double YLoc) {
    if (this.Min.x <= XLoc) {
      if (XLoc <= this.Max.x) {
        if (this.Min.y <= YLoc) {
          if (YLoc <= this.Max.y) {
            return true;
          }
        }
      }
    }
    return false;
  }
  /* ********************************************************************************* */
  @Override public void ShallowLoad(JsonParse.Phrase phrase) {// ITextable
    HashMap<String, JsonParse.Phrase> Fields = phrase.ChildrenHash;
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  @Override public void Textify(StringBuilder sb) {// ITextable
    // or maybe we'd rather export to a Phrase tree first? might be easier, less redundant { and } code. 
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  @Override public JsonParse.Phrase Export(CollisionTable CTable) {// ITextable
    JsonParse.Phrase phrase = new JsonParse.Phrase();
    if (false) {// Caja always has a single owner when we save it. (do we ever really need to save it?  it is constantly regenerated.)
      if (CTable.table.containsKey(this)) {
        phrase.ItemPtr = CTable.table.get(this).ItemPtr;
        return phrase;
      }
      CTable.InsertNewItem(this);
      phrase.ItemPtr = CTable.table.get(this).ItemPtr;
    }
    phrase.ChildrenHash = new HashMap<String, JsonParse.Phrase>();
    HashMap<String, JsonParse.Phrase> Fields = phrase.ChildrenHash;
    Fields.put("Min", IFactory.Utils.PackField(this.Min.toString()));
    Fields.put("Max", IFactory.Utils.PackField(this.Max.toString()));
    return phrase;
  }
}
