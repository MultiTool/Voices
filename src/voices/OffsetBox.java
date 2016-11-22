package voices;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.HashMap;
import voices.ISonglet.Singer;

/**
 *
 * @author MultiTool
 * 
 * OffsetBox handles all affine transformations of your songlet. 
 * For audio, the only transformations handled are X and Y offsets of timing and pitch. No scaling or shearing etc. 
 * We do scale for graphics though. 
 * 
 */
public class OffsetBox extends MonkeyBox { //implements IDrawable.IMoveable, IDeletable {// location box to transpose in pitch, move in time, etc.  //IOffsetBox, 
//  public double TimeX = 0, OctaveY = 0, LoudnessFactor = 1.0;// all of these are in parent coordinates
//  double ScaleX = 1.0, ScaleY = 1.0;// to be used for pixels per second, pixels per octave
//  double ChildXorg = 0, ChildYorg = 0;// These are only non-zero for graphics. Audio origins are always 0,0. 
//  public CajaDelimitadora MyBounds;
//  public ISonglet MyParentSong;// can do this but not used yet

  // graphics support, will move to separate object
//  double OctavesPerRadius = 0.03;
  public static Factory MyFactory = InitFactory();
  public static String ContentName = "Content";
  /* ********************************************************************************* */
  public OffsetBox() {
    //this.Clear();
    this.Create_Me();
    MyBounds = new CajaDelimitadora();
    this.MyBounds.Reset();
  }
  /* ********************************************************************************* */
  @Override public OffsetBox Clone_Me() {// ICloneable
    OffsetBox child = new OffsetBox();
    child.Copy_From(this);
    return child;
  }
  /* ********************************************************************************* */
  @Override public OffsetBox Deep_Clone_Me(ITextable.CollisionLibrary HitTable) {// ICloneable
    OffsetBox child = this.Clone_Me();
    return child;
  }
  /* ********************************************************************************* */
  public void BreakFromHerd(ITextable.CollisionLibrary HitTable) {// virtual
  }
  /* ********************************************************************************* */
  public void Copy_From(OffsetBox donor) {
    super.Copy_From(donor);
  }
  /* ********************************************************************************* */
  @Override public double Get_Max_Amplitude() {
    return this.GetContent().Get_Max_Amplitude() * this.LoudnessFactor;
  }
  /* ********************************************************************************* */
  @Override public void Rebase_Time(double Time) {
    this.TimeX = Time;
    double RelativeMinBound = this.MyBounds.Min.x;// preserve the relative relationship of my bounds and my origin.
    this.MyBounds.Rebase_Time(Time + RelativeMinBound);
  }
  /* ********************************************************************************* */
  public Singer Spawn_Singer() {// always always always override this
    throw new UnsupportedOperationException("Not supported yet.");
  }
  /* ********************************************************************************* */
  public void Rescale_TimeX(double Factor) {// for compose time
    this.ScaleX = Factor;
  }
  /* ********************************************************************************* */
  public ISonglet GetContent() {// always always override this
    throw new UnsupportedOperationException("Not supported yet.");//  public abstract ISonglet GetContent(); ? 
  }
  // <editor-fold defaultstate="collapsed" desc="IDrawable and IMoveable">
  /* ********************************************************************************* */
  @Override public void Draw_Me(DrawingContext ParentDC) {// IDrawable
    if (ParentDC.ClipBounds.Intersects(MyBounds)) {
      Point2D.Double pnt = ParentDC.To_Screen(this.TimeX, this.OctaveY);
      double extra = (1.0 / (double) ParentDC.RecurseDepth);
      //extra *= 0.02;
      double RadiusPixels = Math.abs(ParentDC.GlobalOffset.ScaleY) * (OctavesPerRadius + extra * 0.02);
      RadiusPixels = Math.ceil(RadiusPixels);
      Color col = Globals.ToRainbow(extra);
      if (false) {
        double DiameterPixels = RadiusPixels * 2.0;
        ParentDC.gr.setColor(Globals.ToAlpha(col, 200));// control point just looks like a dot
        ParentDC.gr.fillOval((int) (pnt.x - RadiusPixels), (int) (pnt.y - RadiusPixels), (int) DiameterPixels, (int) DiameterPixels);
        ParentDC.gr.setColor(Globals.ToAlpha(Color.darkGray, 200));
        ParentDC.gr.drawOval((int) (pnt.x - RadiusPixels), (int) (pnt.y - RadiusPixels), (int) DiameterPixels, (int) DiameterPixels);
      }
      MonkeyBox.Draw_Dot2(ParentDC, pnt.x, pnt.y, OctavesPerRadius, this.IsSelected, Globals.ToAlpha(col, 200));
      if (false) {
        this.Draw_Dot(ParentDC, col);
      }
      // maybe reduce ChildDC's clipbounds to intersect with my own bounds? after all none of my children should go outside my bounds
      // eh, that would conflict with dragging my child outside of my bounds - it would stop being drawn during the drag. 
      DrawingContext ChildDC = new DrawingContext(ParentDC, this);// In C++ ChildDC will be a local variable from the stack, not heap. 
      ISonglet Content = this.GetContent();
      Content.Draw_Me(ChildDC);
      ChildDC.Delete_Me();
    }
  }
  /* ********************************************************************************* */
  @Override public void Draw_Dot(DrawingContext DC, Color col) {
    Paint oldpaint = DC.gr.getPaint();
    Point2D.Double pnt = DC.To_Screen(this.TimeX, this.OctaveY);
    double extra = (1.0 / (double) DC.RecurseDepth);
//    double RadiusPixels = Math.abs(ParentDC.GlobalOffset.ScaleY) * (OctavesPerRadius + extra * 0.02);
    double RadiusPixels = Math.abs(DC.GlobalOffset.ScaleY) * (OctavesPerRadius);
    RadiusPixels = Math.ceil(RadiusPixels);
    double DiameterPixels = RadiusPixels * 2.0;
//    Color col = Globals.ToRainbow(extra);
//    col = Color.MAGENTA;
    if (true && this.IsSelected) {// to do: add glow for selected objects
      Point2D center = new Point2D.Float(50, 50);
      float radius = 25;
      float[] dist = {0.0f, 0.5f, 1.0f};
      Color[] colors = {Color.RED, Color.red, Globals.ToAlpha(Color.red, 1)};
      double GradRadius = RadiusPixels * 2;
      double GradDiameter = GradRadius * 2;
      RadialGradientPaint paint = new RadialGradientPaint(pnt, (int) GradRadius, dist, colors);
      DC.gr.setPaint(paint);
      DC.gr.fillOval((int) (pnt.x - GradRadius), (int) (pnt.y - GradRadius), (int) GradDiameter, (int) GradDiameter);
      DC.gr.setPaint(oldpaint);
    }
    DC.gr.setColor(Globals.ToAlpha(col, 200));// control point just looks like a dot
    DC.gr.fillOval((int) (pnt.x - RadiusPixels), (int) (pnt.y - RadiusPixels), (int) DiameterPixels, (int) DiameterPixels);
    DC.gr.setColor(Globals.ToAlpha(Color.darkGray, 200));
    DC.gr.drawOval((int) (pnt.x - RadiusPixels), (int) (pnt.y - RadiusPixels), (int) DiameterPixels, (int) DiameterPixels);
  }
  /* ********************************************************************************* */
  public void Draw_My_Bounds(DrawingContext ParentDC) {// for debugging. break glass in case of emergency
    OffsetBox GlobalOffset = ParentDC.GlobalOffset;
    Graphics2D gr = ParentDC.gr;
    this.MyBounds.Sort_Me();
    int rx0 = (int) GlobalOffset.UnMapTime(this.MyBounds.Min.x);
    int rx1 = (int) GlobalOffset.UnMapTime(this.MyBounds.Max.x);
    int ry0 = (int) GlobalOffset.UnMapPitch(this.MyBounds.Min.y);
    int ry1 = (int) GlobalOffset.UnMapPitch(this.MyBounds.Max.y);
    if (ry1 < ry0) {// swap
      int temp = ry1;
      ry1 = ry0;
      ry0 = temp;
    }

    // thinner lines for more distal sub-branches
    double extra = (2.0 / (double) ParentDC.RecurseDepth);

    int buf = (int) Math.ceil(extra * 2);
    rx0 -= buf;
    rx1 += buf;
    ry0 -= buf;
    ry1 += buf;
    int wdt = rx1 - rx0;
    int hgt = ry1 - ry0;
    int cint = Globals.RandomGenerator.nextInt() % 256;
    Color col = new Color(cint);

    Stroke oldStroke = gr.getStroke();
    gr.setColor(Globals.ToAlpha(col, 100));

    BasicStroke bs = new BasicStroke((float) (1.0 + extra), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    gr.setStroke(bs);

    //ParentDC.gr.setColor(col);//Color.magenta
    gr.drawRect(rx0, ry0, wdt, hgt);

    gr.setStroke(oldStroke);
  }
  /* ********************************************************************************* */
  @Override public CajaDelimitadora GetBoundingBox() {// IDrawable
    return this.MyBounds;
  }
  @Override public void UpdateBoundingBox() {// IDrawable
    ISonglet Content = this.GetContent();
    Content.UpdateBoundingBox();
    this.UpdateBoundingBoxLocal();
  }
  @Override public void UpdateBoundingBoxLocal() {// IDrawable
    ISonglet Content = this.GetContent();
    Content.UpdateBoundingBoxLocal();// either this
    this.UnMap(Content.GetBoundingBox(), MyBounds);// project child limits into parent (my) space
    // include my bubble in bounds
    this.MyBounds.IncludePoint(this.TimeX - OctavesPerRadius, this.OctaveY - OctavesPerRadius);
    this.MyBounds.IncludePoint(this.TimeX + OctavesPerRadius, this.OctaveY + OctavesPerRadius);
  }
  @Override public void GoFishing(Grabber Scoop) {// IDrawable
    if (Scoop.KeepDigging(this)) {
      Scoop.ConsiderLeaf(this);
      Scoop.AddBoxToStack(this);
      this.GetContent().GoFishing(Scoop);
      Scoop.DecrementStack();
    }
  }
  @Override public void MoveTo(double XLoc, double YLoc) {// IDrawable.IMoveable
    if (XLoc >= 0) {// don't go backward in time
      this.TimeX = XLoc;
    }
    this.OctaveY = YLoc;
    this.MyParentSong.Refresh_From_Beneath();
  }
  @Override public boolean HitsMe(double XLoc, double YLoc) {// IDrawable.IMoveable
    System.out.print("HitsMe:");
    if (this.MyBounds.Contains(XLoc, YLoc)) {// redundant test
      double dist = Math.hypot(XLoc - this.TimeX, YLoc - this.OctaveY);
      if (dist <= this.OctavesPerRadius) {
        System.out.println("true");
        return true;
      }
    }
    System.out.println("false");
    return false;
  }
  // </editor-fold>
  /* ********************************************************************************* */
  @Override public boolean Create_Me() {// IDeletable
    return true;
  }
  @Override public void Delete_Me() {// IDeletable
    super.Delete_Me();// wreck everything
    if (false) {// we can probably enable this and remove it from everywhere else
      ISonglet Content = this.GetContent();
      if (Content != null) {
        if (Content.UnRef_Songlet() <= 0) {
          Content.Delete_Me();
          Content = null;// well maybe this makes a problem with the above plan. need a NullContent() ? 
        }
      }
    }
  }
  /* ********************************************************************************* */
  @Override public JsonParse.Node Export(CollisionLibrary HitTable) {// ITextable
    JsonParse.Node SelfPackage = super.Export(HitTable);// ready for test?
    JsonParse.Node ChildPackage;
    if (this.GetContent().GetRefCount() != 1) {// songlet exists in more than one place, use a pointer to library
      ChildPackage = new JsonParse.Node();// multiple references, use a pointer to library instead
      CollisionItem ci;// songlet is already in library, just create a child phrase and assign its textptr to that entry key
      if ((ci = HitTable.GetItem(this.GetContent())) == null) {
        ci = HitTable.InsertUniqueInstance(this.GetContent());// songlet is NOT in library, serialize it and add to library
        ci.JsonPhrase = this.GetContent().Export(HitTable);
      }
      ChildPackage.Literal = ci.ItemTxtPtr;
    } else {// songlet only exists in one place, make it inline.
      ChildPackage = this.GetContent().Export(HitTable);
    }
    SelfPackage.AddSubPhrase(OffsetBox.ContentName, ChildPackage);
    return SelfPackage;
  }
  @Override public void ShallowLoad(JsonParse.Node phrase) {// ITextable
    super.ShallowLoad(phrase);
  }
  @Override public void Consume(JsonParse.Node phrase, CollisionLibrary ExistingInstances) {// ITextable - Fill in all the values of an already-created object, including deep pointers.
    // should never hit this function - work in progress
    if (phrase == null) {
      return;
    }
    this.ShallowLoad(phrase);
    JsonParse.Node SongletPhrase = phrase.ChildrenHash.get(OffsetBox.ContentName);// value of songlet field
    String ContentTxt = SongletPhrase.Literal;
    ISonglet songlet;
    if (Globals.IsTxtPtr(ContentTxt)) {// if songlet content is just a pointer into the library
      CollisionItem ci = ExistingInstances.GetItem(ContentTxt);// look up my songlet in the library
      if (ci == null) {// then null reference even in file - the json is corrupt
        throw new RuntimeException("CollisionItem is null in OffsetBox");// + ObjectTypeName);
      }
      if ((songlet = (ISonglet) ci.Item) == null) {// another cast!
        ci.Item = songlet = this.Spawn_And_Attach_Songlet();// if not instantiated, create one and save it
        songlet.Consume(ci.JsonPhrase, ExistingInstances);
      } else {// songlet is found in library
        //this.Attach_Songlet(songlet);// Attach_Songlet would have to be called by inheriting classes after calling super.Consume()
      }
    } else {
      songlet = this.Spawn_And_Attach_Songlet();// songlet is inline, inside this one offsetbox
      songlet.Consume(SongletPhrase, ExistingInstances);
    }
  }
  public ISonglet Spawn_And_Attach_Songlet() {// virtual
    throw new UnsupportedOperationException("Spawn_And_Attach_Songlet not supported in OffseBox base class.");
  }
  /* ********************************************************************************* */
  public static Factory InitFactory() {// for serialization
    if (OffsetBox.MyFactory == null) {
      OffsetBox.MyFactory = new Factory();
    }
    return OffsetBox.MyFactory;
  }
  /* ********************************************************************************* */
  public static class Factory implements IFactory {// for serialization
    @Override public OffsetBox Create(JsonParse.Node phrase, CollisionLibrary ExistingInstances) {// under construction, this does not do anything yet
      return null;
    }
  }
}
