/*
 *
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
public class GraphicBox implements IDrawable, ISonglet, IDeletable {// 
  public OffsetBox ContentOBox = null;
  private CajaDelimitadora MyBounds = new CajaDelimitadora();
  private int FreshnessTimeStamp;
  /* ********************************************************************************* */
  public void Attach_Content(OffsetBox content) {
    this.ContentOBox = content;
  }
  /* ********************************************************************************* */
  @Override public OffsetBox Spawn_OffsetBox() {// for compose time
    return this.Spawn_My_OffsetBox();
  }
  /* ********************************************************************************* */
  public Graphic_OffsetBox Spawn_My_OffsetBox() {// for compose time
    Graphic_OffsetBox lbox = new Graphic_OffsetBox();// Deliver a OffsetBox specific to this type of phrase.
    lbox.Content = this;
    return lbox;
  }
  /* ********************************************************************************* */
  @Override public void Draw_Me(Drawing_Context ParentDC) {// IDrawable
    AntiAlias(ParentDC.gr);
    Draw_Grid(ParentDC);
    this.ContentOBox.Draw_Me(ParentDC);
  }
  @Override public CajaDelimitadora GetBoundingBox() {// IDrawable
    return this.ContentOBox.GetBoundingBox();
  }
  @Override public void UpdateBoundingBox() {// IDrawable
    this.ContentOBox.UpdateBoundingBox();
    this.UpdateBoundingBoxLocal();
  }
  @Override public void UpdateBoundingBoxLocal() {// IDrawable
    this.MyBounds.Reset();
    CajaDelimitadora ChildBBoxUnMapped = this.ContentOBox.GetBoundingBox();// project child limits into parent (my) space
    this.MyBounds.Include(ChildBBoxUnMapped);// Inefficient. Could be just assigned or copied.
  }
  /* ********************************************************************************* */
  @Override public void GoFishing(Grabber Scoop) {// IDrawable
    if (Scoop.CurrentContext.SearchBounds.Intersects(MyBounds)) {//wrong
      this.ContentOBox.GoFishing(Scoop);
    }
  }
  /* ********************************************************************************* */
  public void Draw_Grid(Drawing_Context ParentDC) {
    double xloc, yloc;
    int MinX, MinY, MaxX, MaxY;
    int X0, Y0;
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

    // draw horizontal lines
    ParentDC.gr.setColor(Globals.ToAlpha(Color.lightGray, 100));
    for (double ysemi = MinY; ysemi < MaxY; ysemi += 1.0 / 12.0) {
      yloc = ParentDC.GlobalOffset.UnMapPitch(ysemi);
      ParentDC.gr.drawLine(MinX, (int) yloc, width, (int) yloc);
    }
    ParentDC.gr.setColor(Globals.ToAlpha(Color.darkGray, 100));
    for (int xcnt = MinX; xcnt < MaxX; xcnt++) {
      xloc = ParentDC.GlobalOffset.UnMapTime(xcnt);
      ParentDC.gr.drawLine((int) xloc, MinY, (int) xloc, height);
    }

    // draw vertical lines
    ParentDC.gr.setColor(Globals.ToAlpha(Color.darkGray, 100));
    for (int ycnt = MinY; ycnt < MaxY; ycnt++) {
      yloc = ParentDC.GlobalOffset.UnMapPitch(ycnt);
      ParentDC.gr.drawLine(MinX, (int) yloc, width, (int) yloc);
    }

    // draw origin lines
    ParentDC.gr.setColor(Globals.ToAlpha(Color.red, 255));
    X0 = (int) ParentDC.GlobalOffset.UnMapTime(0);
    ParentDC.gr.drawLine(X0, MinY, X0, height);

    Y0 = (int) ParentDC.GlobalOffset.UnMapPitch(0);
    ParentDC.gr.drawLine(MinX, Y0, width, Y0);
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
  // this is all junk that is never used
  @Override public Singer Spawn_Singer() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  @Override public int Get_Sample_Count(int SampleRate) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  @Override public double Get_Duration() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  @Override public double Get_Max_Amplitude() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  @Override public double Update_Durations() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  @Override public void Update_Guts(MetricsPacket metrics) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  @Override public void Sort_Me() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  @Override public AudProject Get_Project() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  @Override public void Set_Project(AudProject project) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  /* ********************************************************************************* */
  @Override public int FreshnessTimeStamp_g() {// ISonglet
    return this.FreshnessTimeStamp;
  }
  @Override public void FreshnessTimeStamp_s(int TimeStampNew) {// ISonglet
    this.FreshnessTimeStamp = TimeStampNew;
  }
  /* ********************************************************************************* */
  public class Graphic_OffsetBox extends OffsetBox {
    GraphicBox Content;
    /* ********************************************************************************* */
    public Graphic_OffsetBox() {
      ScaleX = 40;// pixels per second
      ScaleY = -40;// pixels per octave
      this.OctaveY = 400;
      this.TimeX = 20;
      MyBounds = new CajaDelimitadora();
    }
    /* ********************************************************************************* */
    public void Attach_Content(OffsetBox content) {
      this.Content.Attach_Content(content);
    }
    /* ********************************************************************************* */
    @Override public ISonglet GetContent() {// Problem: Need to override this, but GraphicBox is not an ISonglet
      return this.Content;
    }
    /* ********************************************************************************* */
    @Override public void Draw_Me(Drawing_Context ParentDC) {// IDrawable
      if (ParentDC.ClipBounds.Intersects(MyBounds)) {
        Drawing_Context ChildDC = new Drawing_Context(ParentDC, this);// map to child (my) internal coordinates
        this.Content.Draw_Me(ChildDC);
      }
    }
    @Override public void UpdateBoundingBox() {// IDrawable
      this.Content.UpdateBoundingBox();// Problem: Overriding this because GraphicBox is not an ISonglet
      this.UpdateBoundingBoxLocal();
    }
    @Override public void UpdateBoundingBoxLocal() {// IDrawable
      this.Content.GetBoundingBox().UnMap(this, MyBounds);// project child limits into parent (my) space
      this.MyBounds.Sort_Me();
    }
    @Override public void GoFishing(Grabber Scoop) {// IDrawable
      Scoop.AddFirstBox(this, Scoop.CurrentContext.Loc.x, Scoop.CurrentContext.Loc.y);
      if (Scoop.CurrentContext.SearchBounds.Intersects(MyBounds)) {
        if (this.HitsMe(Scoop.CurrentContext.Loc.x, Scoop.CurrentContext.Loc.y)) {
          Scoop.ConsiderLeaf(this);
        }
        // Scoop.AddFirstBox(this, Scoop.CurrentContext.Loc.x, Scoop.CurrentContext.Loc.y);
        this.Content.GoFishing(Scoop);
      }
      Scoop.DecrementStack();
    }
    /* ********************************************************************************* */
    @Override public void MoveTo(double XLoc, double YLoc) {// IDrawable.IMoveable
      // if (XLoc >= 0) {// don't go backward in time
      this.TimeX = XLoc;
      this.OctaveY = YLoc;
    }
    /* ********************************************************************************* */
    @Override public OffsetBox Clone_Me() {// always override this thusly
      Graphic_OffsetBox child = new Graphic_OffsetBox();
      child.Copy_From(this);
      child.Content = this.Content;
      return child;
    }
  }
}
