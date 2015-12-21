/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

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
    AntiAlias(ParentDC.gr);
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
  public static void AntiAlias(Graphics2D g2d) {//http://www.exampledepot.com/egs/java.awt/AntiAlias.html?l=rel
    {// Determine if antialiasing is enabled
      RenderingHints rhints = g2d.getRenderingHints();
      Boolean antialiasOn = rhints.containsValue(RenderingHints.VALUE_ANTIALIAS_ON);
    }
    // Enable antialiasing for shapes
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    //g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // Disable antialiasing for shapes
    //g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    // Draw shapes...; see Drawing Simple Shapes. Enable antialiasing for text
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    // Draw text...; see Drawing Simple Text. Disable antialiasing for text
    //g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    
    // Not related to antialiasing, but good to remember this way to mask one image over another, including alpha:
    // mydc.gr.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
  }
  /* ********************************************************************************* */
  public class Graphic_OffsetBox extends OffsetBox {
    GraphicBox Content;
    /* ********************************************************************************* */
    public Graphic_OffsetBox() {
      ScaleX = 60;// pixels per second
      if (false) {
        ScaleY = 20;// pixels per octave
        this.OctaveLoc = -50;
      } else {// inverting the Y axis does not work yet. 
        ScaleY = -80;// pixels per octave
        this.OctaveLoc = 10;
        this.OctaveLoc = 500;
      }
      this.TimeOrg = 20;
      MyBounds = new CajaDelimitadora();
    }
    /* ********************************************************************************* */
    public void Attach_Content(OffsetBox content) {
      this.Content.Attach_Content(content);
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
      this.MyBounds.Sort_Me();
    }
  }
}
