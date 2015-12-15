/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
// import javafx.geometry.BoundingBox;

/**
 *
 * @author MultiTool
 * 
 * This IDrawable approach is dubious. We will have to think it through a lot more. 
 */
public interface IDrawable {
  void Draw_Me(Drawing_Context ParentDC);
  CajaDelimitadora GetBoundingBox();
  void UpdateBoundingBox();
  // Every IDrawable will have a bounding box, and every Drawing_Context will also have a bounding box. 
  // Drawing will always be called from the top, and the bounding box will define what to draw. 
  /* ********************************************************************************* */
  public final class Drawing_Context {// Let's be final until we can't anymore
    // public double Absolute_X, Absolute_Y;
    public Graphics2D gr;
    public CajaDelimitadora ClipBounds;
    public OffsetBox Offset, GlobalOffset;// Global Offset is transformation to and from pixels
    /* ********************************************************************************* */
    public Drawing_Context() {
      this.GlobalOffset = new OffsetBox();
      this.GlobalOffset.Clear();
      this.ClipBounds = new CajaDelimitadora();
    }
    /* ********************************************************************************* */
    public Drawing_Context(Drawing_Context Fresh_Parent, OffsetBox Fresh_Transform) {
      this.Offset = Fresh_Transform;
      this.GlobalOffset = Fresh_Parent.GlobalOffset.Clone_Me();
      this.GlobalOffset.Compound(this.Offset);// inherit and further transform parent space
      this.ClipBounds = new CajaDelimitadora();
      // inherit and transform bounding box.
      Fresh_Parent.ClipBounds.Map(this.Offset, this.ClipBounds);// map to child (my) internal coordinates
      this.gr = Fresh_Parent.gr;
    }
    /* ********************************************************************************* */
    public Point2D.Double To_Screen(double Absolute_X, double Absolute_Y) {
      Point2D.Double pnt = new Point2D.Double(this.GlobalOffset.UnMapTime(Absolute_X), this.GlobalOffset.UnMapPitch(Absolute_Y));
      return pnt;
    }
    /* ********************************************************************************* */
    public void Compound(OffsetBox other) {
      this.GlobalOffset.Compound(other);
    }
  }
}
