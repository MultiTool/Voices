/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 *
 * @author MultiTool
 */
public class GraphicBox implements IDrawable, IDeletable {// ISonglet, 
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
    Draw_Grid(ParentDC);
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
  public void Draw_Grid(Drawing_Context ParentDC) {
    double xloc, yloc;
    int MinX, MinY, MaxX, MaxY;
    int width, height;

    MinX = (int) ParentDC.GlobalOffset.UnMapTime(Math.floor(ParentDC.ClipBounds.Min.x));
    MinY = (int) ParentDC.GlobalOffset.UnMapPitch(Math.floor(ParentDC.ClipBounds.Min.y));

    MaxX = (int) ParentDC.GlobalOffset.UnMapTime(Math.ceil(ParentDC.ClipBounds.Max.x));
    MaxY = (int) ParentDC.GlobalOffset.UnMapPitch(Math.ceil(ParentDC.ClipBounds.Max.y));

    if (MaxY < MinY) {// swap
      int temp = MaxY;
      MaxY = MinY;
      MinY = temp;
    }

    width = MaxX - MinX;// (int) ParentDC.GlobalOffset.UnMapTime(100);
    height = MaxY - MinY;//(int) ParentDC.GlobalOffset.UnMapPitch(100);
    ParentDC.gr.setColor(Globals.ToAlpha(Color.lightGray, 100));
    for (int xcnt = MinX; xcnt < MaxX; xcnt++) {
      xloc = ParentDC.GlobalOffset.UnMapTime(xcnt);
      ParentDC.gr.drawLine((int) xloc, MinY, (int) xloc, height);
    }
    for (int ycnt = MinY; ycnt < MaxY; ycnt++) {
      yloc = ParentDC.GlobalOffset.UnMapPitch(ycnt);
      ParentDC.gr.drawLine(MinX, (int) yloc, width, (int) yloc);
    }
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
  @Override public boolean Create_Me() {// IDeletable
    return true;
  }
  @Override public void Delete_Me() {// IDeletable
    this.MyBounds.Delete_Me();
    this.ContentOBox.Delete_Me();
  }
  /* ********************************************************************************* */
  public class Graphic_OffsetBox extends OffsetBox {
    GraphicBox Content;
    /* ********************************************************************************* */
    public Graphic_OffsetBox() {
      ScaleX = 40;// pixels per second
      if (false) {
        ScaleY = 20;// pixels per octave
        this.OctaveLoc = -50;
      } else {// inverting the Y axis does not work yet. 
        ScaleY = -40;// pixels per octave
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
//    @Override public ISonglet GetContent() {
//      return this.Content;
//    }
    /* ********************************************************************************* */
    @Override public void Draw_Me(Drawing_Context ParentDC) {// IDrawable
      if (true) {
        this.MyBounds.Sort_Me();
        int rx0 = (int) ParentDC.GlobalOffset.UnMapTime(this.MyBounds.Min.x);
        int rx1 = (int) ParentDC.GlobalOffset.UnMapTime(this.MyBounds.Max.x);
        int ry0 = (int) ParentDC.GlobalOffset.UnMapPitch(this.MyBounds.Min.y);
        int ry1 = (int) ParentDC.GlobalOffset.UnMapPitch(this.MyBounds.Max.y);
        if (ry1 < ry0) {// swap
          int temp = ry1;
          ry1 = ry0;
          ry0 = temp;
        }
        int wdt = rx1 - rx0;
        int hgt = ry1 - ry0;
        ParentDC.gr.setColor(Color.red);
        ParentDC.gr.drawRect(rx0, ry0, wdt, hgt);
      }
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
