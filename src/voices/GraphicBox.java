package voices;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.HashMap;

/**
 *
 * @author MultiTool
 */
public class GraphicBox implements IDrawable, ISonglet, IDeletable {// 
  public OffsetBox ContentOBox = null;
  public IDrawable.IMoveable Floater = null;
  private CajaDelimitadora MyBounds = new CajaDelimitadora();
  int RefCount = 0;
  private int FreshnessTimeStamp;

  public static String ContentOBoxName = "ContentOBox";
  /* ********************************************************************************* */
  public void Attach_Content(OffsetBox content) {
    this.ContentOBox = content;
  }
  /* ********************************************************************************* */
  @Override public Graphic_OffsetBox Spawn_OffsetBox() {// for compose time
    Graphic_OffsetBox lbox = new Graphic_OffsetBox();// Deliver a OffsetBox specific to this type of phrase.
    lbox.Attach_Songlet(this);
    return lbox;
  }
  /* ********************************************************************************* */
  @Override public void Draw_Me(DrawingContext ParentDC) {// IDrawable
    AntiAlias(ParentDC.gr);
    Draw_Grid(ParentDC);
    this.ContentOBox.Draw_Me(ParentDC);
    if (this.Floater != null) {
      this.Floater.Draw_Me(ParentDC);
    }
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
  @Override public GraphicBox Clone_Me() {// ICloneable
    GraphicBox child = new GraphicBox();
    return child;
  }
  /* ********************************************************************************* */
  @Override public GraphicBox Deep_Clone_Me(ITextable.CollisionLibrary HitTable) {// ICloneable
    GraphicBox child = new GraphicBox();
    child.Copy_From(this);
    child.ContentOBox = this.ContentOBox.Deep_Clone_Me(HitTable);
    return child;
  }
  /* ********************************************************************************* */
  public void Copy_From(GraphicBox donor) {
    // this.ContentOBox = null;
    this.FreshnessTimeStamp = 0;
    this.MyBounds.Copy_From(donor.MyBounds);
    //this.ContentOBox = donor.ContentOBox.GetContent().Spawn_OffsetBox();
  }
  /* ********************************************************************************* */
  public void Draw_Grid(DrawingContext ParentDC) {
    double xloc, yloc;
    double MinX, MinY, MaxX, MaxY;// in audio coordinates
    int ScreenMinX, ScreenMinY, ScreenMaxX, ScreenMaxY;// in screen coordinates
    int X0, Y0;
    int width, height;

    MinX = Math.floor(ParentDC.ClipBounds.Min.x);
    MinY = Math.floor(ParentDC.ClipBounds.Min.y);
    MaxX = Math.ceil(ParentDC.ClipBounds.Max.x);
    MaxY = Math.ceil(ParentDC.ClipBounds.Max.y);

    ScreenMinX = (int) ParentDC.GlobalOffset.UnMapTime(MinX);
    ScreenMinY = (int) ParentDC.GlobalOffset.UnMapPitch(MinY);
    ScreenMaxX = (int) ParentDC.GlobalOffset.UnMapTime(MaxX);
    ScreenMaxY = (int) ParentDC.GlobalOffset.UnMapPitch(MaxY);

    if (ScreenMaxY < ScreenMinY) {// swap
      int temp = ScreenMaxY;
      ScreenMaxY = ScreenMinY;
      ScreenMinY = temp;
    }

    width = ScreenMaxX - ScreenMinX;// (int) ParentDC.GlobalOffset.UnMapTime(100);
    height = ScreenMaxY - ScreenMinY;//(int) ParentDC.GlobalOffset.UnMapPitch(100);

    ParentDC.gr.setColor(Globals.ToAlpha(Color.lightGray, 100));// draw minor horizontal pitch lines
    for (double ysemi = MinY; ysemi < MaxY; ysemi += 1.0 / 12.0) {// semitone lines
      yloc = ParentDC.GlobalOffset.UnMapPitch(ysemi);
      ParentDC.gr.drawLine(ScreenMinX, (int) yloc, ScreenMaxX, (int) yloc);
    }

    ParentDC.gr.setColor(Globals.ToAlpha(Color.lightGray, 100));// draw minor vertical time lines
    for (double xsemi = MinX; xsemi < MaxX; xsemi += 1.0 / 4.0) {// 1/4 second time lines
      xloc = ParentDC.GlobalOffset.UnMapTime(xsemi);
      ParentDC.gr.drawLine((int) xloc, ScreenMinY, (int) xloc, ScreenMaxY);
    }

    Stroke PrevStroke = ParentDC.gr.getStroke();
    BasicStroke bs = new BasicStroke(2.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
    ParentDC.gr.setStroke(bs);
    ParentDC.gr.setColor(Globals.ToAlpha(Color.darkGray, 100));// draw major horizontal pitch lines
    for (double ycnt = MinY; ycnt < MaxY; ycnt++) {// octave lines
      yloc = ParentDC.GlobalOffset.UnMapPitch(ycnt);
      ParentDC.gr.drawLine(ScreenMinX, (int) yloc, ScreenMaxX, (int) yloc);
    }

    ParentDC.gr.setColor(Globals.ToAlpha(Color.darkGray, 100));// draw major vertical time lines
    for (double xcnt = MinX; xcnt < MaxX; xcnt++) {// 1 second time lines
      xloc = ParentDC.GlobalOffset.UnMapTime(xcnt);
      ParentDC.gr.drawLine((int) xloc, ScreenMinY, (int) xloc, ScreenMaxY);
    }

    // draw origin lines
    ParentDC.gr.setColor(Globals.ToAlpha(Color.red, 255));
    X0 = (int) ParentDC.GlobalOffset.UnMapTime(0);
    ParentDC.gr.drawLine(X0, ScreenMinY, X0, ScreenMaxY);

    Y0 = (int) ParentDC.GlobalOffset.UnMapPitch(0);
    ParentDC.gr.drawLine(ScreenMinX, Y0, ScreenMaxX, Y0);

    ParentDC.gr.setStroke(PrevStroke);
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
    this.Floater = null;// wreck everything
    this.RefCount = Integer.MIN_VALUE;
    this.FreshnessTimeStamp = Integer.MIN_VALUE;
  }
  /* ********************************************************************************* */
  @Override public int Ref_Songlet() {// ISonglet Reference Counting: increment ref counter and return new value just for kicks
    return ++this.RefCount;
  }
  @Override public int UnRef_Songlet() {// ISonglet Reference Counting: decrement ref counter and return new value just for kicks
    return --this.RefCount;
  }
  @Override public int GetRefCount() {// ISonglet Reference Counting: get number of references for serialization
    return this.RefCount;
  }
  /* ********************************************************************************* */
  @Override public JsonParse.Node Export(CollisionLibrary HitTable) {// ITextable
    JsonParse.Node phrase = new JsonParse.Node();
    phrase.ChildrenHash = new HashMap<String, JsonParse.Node>();
    JsonParse.Node ContentOBoxPhrase = this.ContentOBox.Export(HitTable);
    phrase.AddSubPhrase(ContentOBoxName, ContentOBoxPhrase);
    return phrase;
  }
  @Override public void ShallowLoad(JsonParse.Node phrase) {// ITextable
    HashMap<String, JsonParse.Node> Fields = phrase.ChildrenHash;
    //this.BaseFreq = Double.parseDouble(IFactory.Utils.GetField(Fields, "BaseFreq", Double.toString(Globals.BaseFreqC0)));
    // this.MaxAmplitude = Double.parseDouble(IFactory.Utils.GetField(Fields, "MaxAmplitude", "0.125")); can be calculated
  }
  @Override public void Consume(JsonParse.Node phrase, CollisionLibrary ExistingInstances) {// ITextable - Fill in all the values of an already-created object, including deep pointers.
    if (phrase == null) {// ready for test
      return;
    }
    this.ShallowLoad(phrase);
    JsonParse.Node ChildPhrase = IFactory.Utils.LookUpField(phrase.ChildrenHash, this.ContentOBoxName);// array of subsongs object
    if (ChildPhrase != null) {
      String TypeName = IFactory.Utils.GetField(ChildPhrase.ChildrenHash, Globals.ObjectTypeName, "null");
      IFactory factory = Globals.FactoryLUT.get(TypeName);// use factories to deal with polymorphism
      ITextable child = factory.Create(ChildPhrase, ExistingInstances);// child.Consume(ChildPhrase, ExistingInstances);
      this.ContentOBox = (OffsetBox) child;// another cast!
    }
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
  @Override public void Refresh_From_Beneath() {
  }
  /* ********************************************************************************* */
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
  @Override public void SetMute(boolean Mute) {
  }
  /* ********************************************************************************* */
  @Override public int FreshnessTimeStamp_g() {// ISonglet
    return this.FreshnessTimeStamp;
  }
  @Override public void FreshnessTimeStamp_s(int TimeStampNew) {// ISonglet
    this.FreshnessTimeStamp = TimeStampNew;
  }
  /* ********************************************************************************* */
  public static class Graphic_OffsetBox extends OffsetBox {
    GraphicBox Content;
    public static String ObjectTypeName = "Graphic_OffsetBox";
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
    @Override public GraphicBox GetContent() {// Problem: Need to override this, but GraphicBox is not an ISonglet
      return this.Content;
    }
    /* ********************************************************************************* */
    public void Attach_Songlet(GraphicBox songlet) {// for serialization
      this.Content = songlet;
      songlet.Ref_Songlet();
    }
    /* ********************************************************************************* */
    @Override public void Draw_Me(DrawingContext ParentDC) {// IDrawable
      if (ParentDC.ClipBounds.Intersects(MyBounds)) {
        DrawingContext ChildDC = new DrawingContext(ParentDC, this);// map to child (my) internal coordinates
        this.Content.Draw_Me(ChildDC);
      }
    }
    @Override public void UpdateBoundingBox() {// IDrawable
      this.Content.UpdateBoundingBox();// Problem: Overriding this because GraphicBox is not an ISonglet
      this.UpdateBoundingBoxLocal();
    }
    @Override public void UpdateBoundingBoxLocal() {// IDrawable
      this.UnMap(this.Content.GetBoundingBox(), MyBounds);// project child limits into parent (my) space
      this.MyBounds.Sort_Me();
    }
    @Override public void GoFishing(Grabber Scoop) {// IDrawable
      Scoop.AddFirstBox(this, Scoop.CurrentContext.Loc.x, Scoop.CurrentContext.Loc.y);
      if (Scoop.KeepDigging(this)) {
        Scoop.ConsiderLeaf(this);
        this.Content.GoFishing(Scoop);
      }
      Scoop.DecrementStack();
    }
    /* ********************************************************************************* */
    @Override public void MoveTo(double XLoc, double YLoc) {// IDrawable.IMoveable
      // if (XLoc >= 0) {// don't go backward in time
      this.TimeX = XLoc;
      this.OctaveY = YLoc;
      this.MyParentSong.Refresh_From_Beneath();
    }
    /* ********************************************************************************* */
    @Override public Graphic_OffsetBox Clone_Me() {// ICloneable always override this thusly
      Graphic_OffsetBox child = new Graphic_OffsetBox();
      child.Copy_From(this);
      child.Content = this.Content;// we can actually use this to spawn multiple graphic views of one thing
      return child;
    }
    /* ********************************************************************************* */
    @Override public Graphic_OffsetBox Deep_Clone_Me(ITextable.CollisionLibrary HitTable) {// ICloneable
      Graphic_OffsetBox child = this.Clone_Me();
      child.Attach_Songlet(this.Content.Deep_Clone_Me(HitTable));
      return child;
    }
    /* ********************************************************************************* */
    @Override public void BreakFromHerd(ITextable.CollisionLibrary HitTable) {// for compose time. detach from my songlet and attach to an identical but unlinked songlet
      GraphicBox clone = this.Content.Deep_Clone_Me(HitTable);
      if (this.Content.UnRef_Songlet() <= 0) {
        this.Content.Delete_Me();
      }
      this.Attach_Songlet(clone);
    }
    /* ********************************************************************************* */
    @Override public boolean Create_Me() {// IDeletable
      return true;
    }
    @Override public void Delete_Me() {// IDeletable
      super.Delete_Me();
      if (this.Content != null) {
        if (this.Content.UnRef_Songlet() <= 0) {
          this.Content.Delete_Me();
          this.Content = null;
        }
      }
    }
    /* ********************************************************************************* */
    @Override public JsonParse.Node Export(CollisionLibrary HitTable) {// ITextable
      JsonParse.Node SelfPackage = super.Export(HitTable);// ready for test?
      SelfPackage.AddSubPhrase(Globals.ObjectTypeName, IFactory.Utils.PackField(ObjectTypeName));
      if (false) {
        SelfPackage.AddSubPhrase(Globals.ObjectTypeName, IFactory.Utils.PackField(ObjectTypeName));
        JsonParse.Node ChildPackage;
        if (this.Content.GetRefCount() != 1) {// songlet exists in more than one place, use a pointer to library
          ChildPackage = new JsonParse.Node();// multiple references, use a pointer to library instead
          CollisionItem ci;// songlet is already in library, just create a child phrase and assign its textptr to that entry key
          if ((ci = HitTable.GetItem(this.Content)) == null) {
            ci = HitTable.InsertUniqueInstance(this.Content);// songlet is NOT in library, serialize it and add to library
            ci.JsonPhrase = this.Content.Export(HitTable);
          }
          ChildPackage.Literal = ci.ItemTxtPtr;
        } else {// songlet only exists in one place, make it inline.
          ChildPackage = this.Content.Export(HitTable);
        }
        SelfPackage.AddSubPhrase(OffsetBox.ContentName, ChildPackage);
      }
      return SelfPackage;
    }
    @Override public void ShallowLoad(JsonParse.Node phrase) {// ITextable
      super.ShallowLoad(phrase);
    }
    @Override public void Consume(JsonParse.Node phrase, CollisionLibrary ExistingInstances) {// ITextable - Fill in all the values of an already-created object, including deep pointers.
      if (phrase == null) {// ready for test?
        return;
      }
      this.ShallowLoad(phrase);
      JsonParse.Node SongletPhrase = phrase.ChildrenHash.get(OffsetBox.ContentName);// value of songlet field
      String ContentTxt = SongletPhrase.Literal;
      GraphicBox songlet;
      if (Globals.IsTxtPtr(ContentTxt)) {// if songlet content is just a pointer into the library
        CollisionItem ci = ExistingInstances.GetItem(ContentTxt);// look up my songlet in the library
        if (ci == null) {// then null reference even in file - the json is corrupt
          throw new RuntimeException("CollisionItem is null in " + ObjectTypeName);
        }
        if ((songlet = (GraphicBox) ci.Item) == null) {// another cast!
          ci.Item = songlet = new GraphicBox();// if not instantiated, create one and save it
          songlet.Consume(ci.JsonPhrase, ExistingInstances);
        }
      } else {
        songlet = new GraphicBox();// songlet is inline, inside this one offsetbox
        songlet.Consume(SongletPhrase, ExistingInstances);
      }
      this.Attach_Songlet(songlet);
    }
    @Override public ISonglet Spawn_And_Attach_Songlet() {// reverse birth, use ONLY for deserialization
      GraphicBox songlet = new GraphicBox();
      this.Attach_Songlet(songlet);
      return songlet;
    }
    /* ********************************************************************************* */
    public void Zoom(double XCtr, double YCtr, double Scale) {
      double XMov = XCtr - (Scale * XCtr);
      double YMov = YCtr - (Scale * YCtr);

      this.TimeX = XMov + (Scale * this.TimeX);
      this.OctaveY = YMov + (Scale * this.OctaveY);

      this.ScaleX *= Scale;
      this.ScaleY *= Scale;
    }
    /* ********************************************************************************* */
    public static class Factory implements IFactory {// for serialization
      @Override public Graphic_OffsetBox Create(JsonParse.Node phrase, CollisionLibrary ExistingInstances) {// under construction, this does not do anything yet
        Graphic_OffsetBox obox = new Graphic_OffsetBox();
        obox.Consume(phrase, ExistingInstances);
        return obox;
      }
    }
  }
}
