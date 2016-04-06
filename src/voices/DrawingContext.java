package voices;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

/**
 *
 * @author MultiTool
 */
// Every IDrawable has a bounding box, and every DrawingContext also has a bounding box for clipping. 
// Drawing will always be called from the top, and the bounding box will define what to draw. 
/* ********************************************************************************* */
public final class DrawingContext implements IDeletable {// Let's be final until we can't anymore
  public Graphics2D gr;
  public CajaDelimitadora ClipBounds;
  public OffsetBox Offset, GlobalOffset = new OffsetBox();// Global Offset is transformation to and from pixels
  public int RecurseDepth;
  public double Excitement;// to highlight animation, range 0 to 1. 
  /* ********************************************************************************* */
  public DrawingContext() {
    this.GlobalOffset = new OffsetBox();
    this.GlobalOffset.Clear();
    this.ClipBounds = new CajaDelimitadora();
    this.RecurseDepth = 0;
    this.Excitement = 0.0;
    this.Create_Me();
  }
  /* ********************************************************************************* */
  public DrawingContext(DrawingContext Fresh_Parent, OffsetBox Fresh_Transform) {
    this.Offset = Fresh_Transform;
    this.GlobalOffset.Copy_From(Fresh_Parent.GlobalOffset);// = Fresh_Parent.GlobalOffset.Clone_Me();
    this.GlobalOffset.Compound(this.Offset);// inherit and further transform parent space
    this.ClipBounds = new CajaDelimitadora();
    // inherit and transform bounding box.
    this.Offset.MapTo(Fresh_Parent.ClipBounds, this.ClipBounds);// map to child (my) internal coordinates
    this.ClipBounds.Sort_Me();
    this.gr = Fresh_Parent.gr;
    this.RecurseDepth = Fresh_Parent.RecurseDepth + 1;
    this.Create_Me();
  }
  /* ********************************************************************************* */
  public Point2D.Double To_Screen(double XLoc, double YLoc) {
    Point2D.Double pnt = new Point2D.Double(this.GlobalOffset.UnMapTime(XLoc), this.GlobalOffset.UnMapPitch(YLoc));
    return pnt;
  }
  /* ********************************************************************************* */
  public void Compound(OffsetBox other) {
    this.GlobalOffset.Compound(other);
  }
  /* ********************************************************************************* */
  @Override public boolean Create_Me() {// IDeletable
    return true;
  }
  @Override public void Delete_Me() {// IDeletable
    this.ClipBounds.Delete_Me();
    this.ClipBounds = null;// wreck everything
    this.GlobalOffset.Delete_Me();
    this.GlobalOffset = null;
    this.Offset = null;
    this.gr = null;
  }
}
