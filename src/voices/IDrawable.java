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
public interface IDrawable extends ICloneable {
  void Draw_Me(DrawingContext ParentDC);
  CajaDelimitadora GetBoundingBox();
  void UpdateBoundingBox();
  void UpdateBoundingBoxLocal();
  void GoFishing(Grabber Scoop);
  @Override IDrawable Clone_Me();
  @Override IDrawable Deep_Clone_Me();
  public interface IMoveable extends IDrawable {// IMoveable is for things that can be selected, dragged, copied, pasted, deleted etc. through the UI.
    void MoveTo(double XLoc, double YLoc);
    boolean HitsMe(double XLoc, double YLoc);// click detection
    void SetSelected(boolean Selected);
    @Override IMoveable Clone_Me();
    @Override IMoveable Deep_Clone_Me();
  }
}
