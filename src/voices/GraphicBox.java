/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

/**
 *
 * @author MultiTool
 */
public class GraphicBox implements IDrawable {// ISonglet, 
  private OffsetBox ContentOBox = null;
  private CajaDelimitadora MyBounds = new CajaDelimitadora();
  /* ********************************************************************************* */
  public void Attach_Content(OffsetBox content) {
    this.ContentOBox = content;
  }
  /* ********************************************************************************* */
  //@Override 
  public OffsetBox Spawn_OffsetBox() {// for compose time
    return this.Spawn_My_OffsetBox();
  }
  /* ********************************************************************************* */
  public Graphic_OffsetBox Spawn_My_OffsetBox() {// for compose time
    Graphic_OffsetBox lbox = new Graphic_OffsetBox();// Deliver a OffsetBox specific to this type of phrase.
    lbox.Content = this;
    return lbox;
  }
  /* ********************************************************************************* */
  @Override public void Draw_Me(Drawing_Context ParentDC) {
    this.ContentOBox.Draw_Me(ParentDC);
  }
  @Override public CajaDelimitadora GetBoundingBox() {
    return this.ContentOBox.GetBoundingBox();
  }
  @Override public void UpdateBoundingBox() {
    this.MyBounds.Reset();
    this.ContentOBox.UpdateBoundingBox();
    CajaDelimitadora ChildBBoxUnMapped = this.ContentOBox.GetBoundingBox();// project child limits into parent (my) space
    this.MyBounds.Include(ChildBBoxUnMapped);// Inefficient. Could be just assigned or copied.
  }
  /* ********************************************************************************* */
  public class Graphic_OffsetBox extends OffsetBox {
    //double XRatio = 50, YRatio = 50;// pixels per second, pixels per octave
    GraphicBox Content;
    /* ********************************************************************************* */
    public void Attach_Content(OffsetBox content) {
      this.Content.Attach_Content(content);
    }
    /* ********************************************************************************* */
    @Override public double MapTime(double ParentTime) {// convert time coordinate from my parent's frame to my child's frame
      return (ParentTime - this.TimeOrg) / XRatio;
    }
    /* ********************************************************************************* */
    @Override public double UnMapTime(double ChildTime) {// convert time coordinate from my child's frame to my parent's frame
      return this.TimeOrg + (ChildTime * XRatio);
    }
    /* ********************************************************************************* */
    @Override public double MapPitch(double ParentPitch) {// convert octave coordinate from my parent's frame to my child's frame
      return (ParentPitch - this.OctaveLoc) / YRatio;
    }
    /* ********************************************************************************* */
    @Override public double UnMapPitch(double ChildPitch) {// convert octave coordinate from my child's frame to my parent's frame
      return this.OctaveLoc + (ChildPitch * YRatio);
    }
    /* ********************************************************************************* */
    @Override public void Draw_Me(Drawing_Context ParentDC) {// IDrawable
      if (ParentDC.ClipBounds.Intersects(MyBounds)) {
        Drawing_Context ChildDC = new Drawing_Context(ParentDC, this);// map to child (my) internal coordinates
        this.Content.Draw_Me(ChildDC);
      }
    }
    @Override public void UpdateBoundingBox() {// IDrawable
      this.Content.UpdateBoundingBox();
      this.Content.GetBoundingBox().UnMap(this, MyBounds);// project child limits into parent (my) space
    }
  }
}
