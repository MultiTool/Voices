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
  public class Drawing_Context {
    public double Absolute_X, Absolute_Y;
    public Graphics2D gr;
    public CajaDelimitadora Bounds;
    public OffsetBox Offset, GlobalOffset;
    /* ********************************************************************************* */
    public Drawing_Context() {
      this.GlobalOffset = new OffsetBox();
      this.GlobalOffset.Clear();
    }
    /* ********************************************************************************* */
    public Drawing_Context(Drawing_Context Fresh_Parent, IDrawable Fresh_Note) {
    }
    /* ********************************************************************************* */
    public Point2D.Double To_Screen(double Absolute_X, double Absolute_Y) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    /* ********************************************************************************* */
    public void Compound(OffsetBox other) {
      this.GlobalOffset.Compound(other);
    }
  }
}
