package voices;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.HashMap;

/**
 *
 * @author MultiTool
 */
/* ********************************************************************************* */
public class VoicePoint extends MonkeyBox {
  public double SubTime = 0.0;// SubTime is cumulative subjective time.

  // graphics support, will move to separate object
  double OctavesPerLoudness = 0.125;// to do: loudness will have to be mapped to screen. not a pixel value right?
  public LoudnessHandle UpHandle, DownHandle;
  public static String UpHandleName = "UpHandle", DownHandleName = "DownHandle";
  public Voice MyParentVoice = null;
  /* ********************************************************************************* */
  public VoicePoint() {
    this.Create_Me();
    this.UpHandle = new LoudnessHandle();
    this.UpHandle.ParentPoint = this;
    this.DownHandle = new LoudnessHandle();
    this.DownHandle.ParentPoint = this;
    this.MyBounds = new CajaDelimitadora();
  }
  public void RefParent(Voice Parent) {
    this.MyParentSong = this.MyParentVoice = Parent;
  }
  /* ********************************************************************************* */
  public void CopyFrom(VoicePoint source) {
    super.Copy_From(source);
    this.SubTime = source.SubTime;
  }
  /* ********************************************************************************* */
  public double GetFrequencyFactor() {
    return Math.pow(2.0, this.OctaveY);
  }
  /* ********************************************************************************* */
  @Override public void Draw_Me(DrawingContext ParentDC) {// IDrawable
    // Control points have the same space as their parent, so no need to create a local map.
    Point2D.Double pnt = ParentDC.To_Screen(this.TimeX, this.OctaveY);
    double RadiusPixels = Math.abs(ParentDC.GlobalOffset.ScaleY) * OctavesPerRadius;
    RadiusPixels = Math.ceil(RadiusPixels);
    double DiameterPixels = RadiusPixels * 2.0;
    this.UpHandle.Draw_Me(ParentDC);
    MonkeyBox.Draw_Dot2(ParentDC, pnt.x, pnt.y, OctavesPerRadius, this.IsSelected, Globals.ToAlpha(Color.yellow, 200));
    if (false) {
      ParentDC.gr.setColor(Globals.ToAlpha(Color.yellow, 200));// control point just looks like a dot
      ParentDC.gr.fillOval((int) (pnt.x - RadiusPixels), (int) (pnt.y - RadiusPixels), (int) DiameterPixels, (int) DiameterPixels);
      ParentDC.gr.setColor(Globals.ToAlpha(Color.darkGray, 200));
      ParentDC.gr.drawOval((int) (pnt.x - RadiusPixels), (int) (pnt.y - RadiusPixels), (int) DiameterPixels, (int) DiameterPixels);
    }
  }
  /* ********************************************************************************* */
  @Override public CajaDelimitadora GetBoundingBox() {
    return this.MyBounds;
  }
  /* ********************************************************************************* */
  @Override public void UpdateBoundingBox() {// IDrawable
    this.UpHandle.UpdateBoundingBox();
    this.UpdateBoundingBoxLocal();// Points have no children, nothing else to do.
  }
  @Override public void UpdateBoundingBoxLocal() {// IDrawable
    double LoudnessHeight = LoudnessFactor * OctavesPerLoudness;// Map loudness to screen pixels.
    double MinX = TimeX - OctavesPerRadius;
    double MaxX = TimeX + OctavesPerRadius;
    double HeightRad = Math.max(OctavesPerRadius, LoudnessHeight);
    double MinY = OctaveY - HeightRad;
    double MaxY = OctaveY + HeightRad;
    this.MyBounds.Assign(MinX, MinY, MaxX, MaxY);

    this.MyBounds.Include(this.UpHandle.GetBoundingBox());// Don't have to UnMap in this case because my points are already in my internal coordinates.
  }
  /* ********************************************************************************* */
  @Override public void GoFishing(Grabber Scoop) {// IDrawable
    System.out.print(" Point GoFishing: ");
    if (Scoop.CurrentContext.SearchBounds.Intersects(MyBounds)) {
      System.out.print(" InBounds, ");
      Scoop.ConsiderLeaf(this);
      this.UpHandle.GoFishing(Scoop);
    }
    System.out.println();
  }
  @Override public boolean HitsMe(double XLoc, double YLoc) {// IDrawable.IMoveable
    System.out.print("** Point HitsMe:");
    boolean Hit = false;
    if (this.MyBounds.Contains(XLoc, YLoc)) {
      System.out.print(" InBounds ");
      double dist = Math.hypot(XLoc - this.TimeX, YLoc - this.OctaveY);
      if (dist <= this.OctavesPerRadius) {
        System.out.print(" Hit!");
        Hit = true;
      } else {
        System.out.print(" Missed!");
      }
    } else {
      System.out.print(" OutBounds ");
    }
    return Hit;
  }
  /* ********************************************************************************* */
  @Override public VoicePoint Clone_Me() {// ICloneable
    VoicePoint child = new VoicePoint();
    child.Copy_From(this);
    return child;
  }
  /* ********************************************************************************* */
  @Override public VoicePoint Deep_Clone_Me(ITextable.CollisionLibrary HitTable) {// ICloneable
    VoicePoint child = new VoicePoint();
    child.Copy_From(this);
    (child.UpHandle = this.UpHandle.Deep_Clone_Me(HitTable)).ParentPoint = child; //child.UpHandle.ParentPoint = child;
    (child.DownHandle = this.DownHandle.Deep_Clone_Me(HitTable)).ParentPoint = child; //child.DownHandle.ParentPoint = child;
    return child;
  }
  /* ********************************************************************************* */
  public void Copy_From(VoicePoint donor) {
    super.Copy_From(donor);
    this.SubTime = donor.SubTime;
    this.OctavesPerLoudness = donor.OctavesPerLoudness;
  }
  /* ********************************************************************************* */
  @Override public boolean Create_Me() {// IDeletable
    return true;
  }
  @Override public void Delete_Me() {// IDeletable
    this.SubTime = Double.NEGATIVE_INFINITY;// wreck everything
    this.OctavesPerLoudness = Double.NEGATIVE_INFINITY;
    this.MyBounds.Delete_Me();
    this.MyBounds = null;
    this.UpHandle.Delete_Me();
    this.UpHandle = null;
    this.DownHandle.Delete_Me();
    this.DownHandle = null;
  }
  /* ********************************************************************************* */
  @Override public JsonParse.Node Export(CollisionLibrary HitTable) {// ITextable
    JsonParse.Node phrase = super.Export(HitTable);
    phrase.AddSubPhrase("OctavesPerLoudness", IFactory.Utils.PackField(this.OctavesPerLoudness));
    phrase.AddSubPhrase("SubTime", IFactory.Utils.PackField(this.SubTime));// can be calculated
    phrase.AddSubPhrase(VoicePoint.UpHandleName, this.UpHandle.Export(HitTable));
    phrase.AddSubPhrase(VoicePoint.DownHandleName, this.DownHandle.Export(HitTable));
    this.UpHandle.Export(HitTable);
    this.DownHandle.Export(HitTable);
    return phrase;
  }
  @Override public void ShallowLoad(JsonParse.Node phrase) {// ITextable
    super.ShallowLoad(phrase);
    HashMap<String, JsonParse.Node> Fields = phrase.ChildrenHash;
    this.OctavesPerLoudness = Double.parseDouble(IFactory.Utils.GetField(Fields, "OctavesPerLoudness", "0.125"));
    if (false) {
      this.SubTime = Double.parseDouble(IFactory.Utils.GetField(Fields, "SubTime", "0"));// can be calculated
    }
  }
  @Override public void Consume(JsonParse.Node phrase, CollisionLibrary ExistingInstances) {// ITextable - Fill in all the values of an already-created object, including deep pointers.
    if (phrase == null) {
      return;
    }
    this.ShallowLoad(phrase);
    HashMap<String, JsonParse.Node> Fields = phrase.ChildrenHash;
    this.UpHandle.Consume(IFactory.Utils.LookUpField(Fields, VoicePoint.UpHandleName), ExistingInstances);
    this.DownHandle.Consume(IFactory.Utils.LookUpField(Fields, VoicePoint.DownHandleName), ExistingInstances);
  }
  /* ********************************************************************************* */
  public static class LoudnessHandle implements IDrawable.IMoveable, IDeletable, ITextable {// probably should not be ITextable
    public CajaDelimitadora MyBounds = new CajaDelimitadora();
    public VoicePoint ParentPoint;
    public double OctavesPerRadius = 0.007;
    private boolean IsSelected = false;
    /* ********************************************************************************* */
    public double GetX() {
      return this.ParentPoint.TimeX;
    }
    public double GetY() {
      double LoudnessHeight = this.ParentPoint.LoudnessFactor * this.ParentPoint.OctavesPerLoudness;// Map loudness to screen pixels.
      return this.ParentPoint.OctaveY + LoudnessHeight;
    }
    @Override public void MoveTo(double XLoc, double YLoc) {// IDrawable.IMoveable
      if (XLoc >= 0) {// don't go backward in time
        this.ParentPoint.TimeX = XLoc;
      }
      double RelativeY = YLoc - this.ParentPoint.OctaveY;
      if (RelativeY >= 0) {// non negative loudness
        RelativeY /= this.ParentPoint.OctavesPerLoudness;
        if (RelativeY <= 1.0) {// shouldn't amplify loudness above 1.0, so that we can keep wave clipping under control
          this.ParentPoint.LoudnessFactor = RelativeY;
        }
      }
    }
    @Override public boolean HitsMe(double XLoc, double YLoc) {// IDrawable.IMoveable
      System.out.print("** LoudnessHandle HitsMe:");
      boolean Hit = false;
      if (this.MyBounds.Contains(XLoc, YLoc)) {
        System.out.print(" InBounds ");
        double dist = Math.hypot(XLoc - this.GetX(), YLoc - (this.GetY() + this.OctavesPerRadius));
        if (dist <= this.OctavesPerRadius) {
          System.out.print(" Hit!");
          Hit = true;
        } else {
          System.out.print(" Missed!");
        }
      } else {
        System.out.print(" OutBounds ");
      }
      return Hit;
    }
    @Override public void SetSelected(boolean Selected) {// IDrawable.IMoveable
      this.IsSelected = Selected;
    }
    @Override public void Draw_Me(DrawingContext ParentDC) {
      // Control points have the same space as their parent, so no need to create a local map.
      Point2D.Double pnt = ParentDC.To_Screen(this.ParentPoint.TimeX, this.ParentPoint.OctaveY);
      double RadiusPixels = Math.abs(ParentDC.GlobalOffset.ScaleY) * OctavesPerRadius;
      double LoudnessHgt = this.ParentPoint.LoudnessFactor * this.ParentPoint.OctavesPerLoudness;
      double YlocHigh = ParentDC.GlobalOffset.UnMapPitch(this.ParentPoint.OctaveY + LoudnessHgt) - RadiusPixels;// My handle rests *upon* the line I control, so I don't occlude my VoicePoint. 

      RadiusPixels = Math.ceil(RadiusPixels);
      double DiameterPixels = RadiusPixels * 2.0;

      MonkeyBox.Draw_Dot2(ParentDC, pnt.x, YlocHigh, OctavesPerRadius, this.IsSelected, Globals.ToAlpha(Color.lightGray, 100));

      if (false) {
        ParentDC.gr.setColor(Globals.ToAlpha(Color.lightGray, 100));// control point just looks like a dot
        ParentDC.gr.fillOval((int) (pnt.x - RadiusPixels), (int) (YlocHigh - RadiusPixels), (int) DiameterPixels, (int) DiameterPixels);
        ParentDC.gr.setColor(Globals.ToAlpha(Color.darkGray, 200));
        ParentDC.gr.drawOval((int) (pnt.x - RadiusPixels), (int) (YlocHigh - RadiusPixels), (int) DiameterPixels, (int) DiameterPixels);
      }
    }
    @Override public CajaDelimitadora GetBoundingBox() {
      return this.MyBounds;
    }
    @Override public void UpdateBoundingBox() {
      this.UpdateBoundingBoxLocal();
    }
    @Override public void UpdateBoundingBoxLocal() {
      double XLoc = this.GetX();
      double YLoc = this.GetY() + this.OctavesPerRadius;// *upon* the line
      double MinX = XLoc - this.OctavesPerRadius;
      double MaxX = XLoc + this.OctavesPerRadius;
      double MinY = YLoc - this.OctavesPerRadius;
      double MaxY = YLoc + this.OctavesPerRadius;
      this.MyBounds.Assign(MinX, MinY, MaxX, MaxY);
    }
    @Override public void GoFishing(Grabber Scoop) {
      System.out.println();
      System.out.print(" LoudnessHandle GoFishing: ");
      if (Scoop.CurrentContext.SearchBounds.Intersects(MyBounds)) {
        System.out.print(" InBounds, ");
        Scoop.ConsiderLeaf(this);
      }
      System.out.println();
    }
    /* ********************************************************************************* */
    @Override public LoudnessHandle Clone_Me() {// ICloneable
      LoudnessHandle child = new LoudnessHandle();
      return child;
    }
    /* ********************************************************************************* */
    @Override public LoudnessHandle Deep_Clone_Me(ITextable.CollisionLibrary HitTable) {// ICloneable
      LoudnessHandle child = new LoudnessHandle();
      child.OctavesPerRadius = this.OctavesPerRadius;
      child.ParentPoint = this.ParentPoint;
      child.MyBounds.Copy_From(this.MyBounds);
      return child;
    }
    /* ********************************************************************************* */
    @Override public boolean Create_Me() {
      return true;
    }
    @Override public void Delete_Me() {
      this.MyBounds.Delete_Me();
      this.MyBounds = null;// wreck everything
      this.OctavesPerRadius = Double.NEGATIVE_INFINITY;
      this.ParentPoint = null;
      this.IsSelected = false;
    }
    /* ********************************************************************************* */
    @Override public JsonParse.Node Export(CollisionLibrary HitTable) {// ITextable
      JsonParse.Node phrase = new JsonParse.Node();
      phrase.ChildrenHash = new HashMap<String, JsonParse.Node>();
      phrase.AddSubPhrase("OctavesPerRadius", IFactory.Utils.PackField(this.OctavesPerRadius));
      if (false) {// can be calculated
        phrase.AddSubPhrase("IsSelected", IFactory.Utils.PackField(this.IsSelected));
        phrase.AddSubPhrase("MyBounds", this.MyBounds.Export(HitTable));
      }
      return phrase;
    }
    @Override public void ShallowLoad(JsonParse.Node phrase) {// ITextable
      HashMap<String, JsonParse.Node> Fields = phrase.ChildrenHash;
      this.OctavesPerRadius = Double.parseDouble(IFactory.Utils.GetField(Fields, "OctavesPerRadius", "0.007"));
    }
    @Override public void Consume(JsonParse.Node phrase, CollisionLibrary ExistingInstances) {// ITextable - Fill in all the values of an already-created object, including deep pointers.
      if (phrase == null) {
        return;
      }
      this.ShallowLoad(phrase);
    }
  }
}
