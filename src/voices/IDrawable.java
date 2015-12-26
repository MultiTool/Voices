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
  public interface IMoveable extends IDrawable {
    void GoFishing(HookAndLure Scoop);
    void MoveTo(double XLoc, double YLoc);
  }
  /*
   boolean IsHittable();  ?
   boolean IsHittable();
   types of selection:
   one where you select a drop destination - these are always containers and/or containers' line segments I think.
   one where you simply select a songlet to copy/delete/move (connectivity changes)
   . can also delete and insert voice cpoints
   one where you select a point to drag - can be anyone's offsetbox, or a voice control point (no connectivity changes)
  
   lowest-hanging fruit is NoReTopo, select points to drag them. Just burrow in to tree with a point and bounds box and dig up the winner hit. 
  
   */
  // Every IDrawable will have a bounding box, and every Drawing_Context will also have a bounding box. 
  // Drawing will always be called from the top, and the bounding box will define what to draw. 
  /* ********************************************************************************* */
  public final class Drawing_Context implements IDeletable {// Let's be final until we can't anymore
    public Graphics2D gr;
    public CajaDelimitadora ClipBounds;
    public OffsetBox Offset, GlobalOffset;// Global Offset is transformation to and from pixels
    public int RecurseDepth;
    public double Excitement;// to highlight animation, range 0 to 1. 
    /* ********************************************************************************* */
    public Drawing_Context() {
      this.GlobalOffset = new OffsetBox();
      this.GlobalOffset.Clear();
      this.ClipBounds = new CajaDelimitadora();
      this.RecurseDepth = 0;
      this.Excitement = 0.0;
      this.Create_Me();
    }
    /* ********************************************************************************* */
    public Drawing_Context(Drawing_Context Fresh_Parent, OffsetBox Fresh_Transform) {
      this.Offset = Fresh_Transform;
      this.GlobalOffset = Fresh_Parent.GlobalOffset.Clone_Me();
      this.GlobalOffset.Compound(this.Offset);// inherit and further transform parent space
      this.ClipBounds = new CajaDelimitadora();
      // inherit and transform bounding box.
      Fresh_Parent.ClipBounds.Map(this.Offset, this.ClipBounds);// map to child (my) internal coordinates
      this.ClipBounds.Sort_Me();
      this.gr = Fresh_Parent.gr;
      this.RecurseDepth = Fresh_Parent.RecurseDepth + 1;
      this.Create_Me();
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
    /* ********************************************************************************* */
    @Override public boolean Create_Me() {// IDeletable
      return true;
    }
    @Override public void Delete_Me() {// IDeletable
      this.ClipBounds.Delete_Me();
      this.ClipBounds = null;
      this.GlobalOffset.Delete_Me();
      this.GlobalOffset = null;
      this.Offset = null;
      this.gr = null;
    }
  }
}
