package voices;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import javax.swing.*;
import voices.IDrawable.Drawing_Context;
// From http://www.tutorialspoint.com/javaexamples/gui_polygon.htm

public class MainGui {
  public JFrame frame;
  public DrawingPanel drawpanel;
  Project MyProject = null;
  GoLive MyThread;
  /* ********************************************************************************* */
  public MainGui() {
    this.MyThread = new GoLive();
  }
  /* ********************************************************************************* */
  public void Init() {
    this.frame = new JFrame();
    this.frame.setTitle("Polygon");
    this.frame.setSize(600, 400);
    this.frame.addWindowListener(new WindowAdapter() {
      @Override public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    Container contentPane = this.frame.getContentPane();
    this.drawpanel = new DrawingPanel();
    contentPane.add(this.drawpanel);
    MyProject = new Project();
    this.MyThread.MyProject = this.MyProject;
    //MyProject.Create_For_Graphics();
    MyProject.Compose_Test();
    this.drawpanel.BigApp = this;
    this.drawpanel.MyProject = this.MyProject;
    frame.setVisible(true);
  }
  /* ********************************************************************************* */
  public static class DrawingPanel extends JPanel implements MouseMotionListener, MouseListener, MouseWheelListener, ComponentListener, KeyListener {
    Project MyProject = null;
    MainGui BigApp;
    /* ********************************************************************************* */
    public DrawingPanel() {
      this.Init();
    }
    /* ********************************************************************************* */
    public final void Init() {
      this.addMouseListener(this);
      this.addMouseMotionListener(this);
      this.addMouseWheelListener(this);
      if (false) {// alternative: look into KeyBindings 
        this.setFocusable(true);
      }
      this.addKeyListener(this);
    }
    /* ********************************************************************************* */
    public void Draw_Me(Graphics2D g2d) {
      // to do: move this to MainGui, and base clipping, zoom etc. on canvas size. 
      Drawing_Context dc = new Drawing_Context();
      dc.gr = g2d;
      int wdt = this.getWidth() * 7 / 8;
      int hgt = this.getHeight() * 7 / 8;
      if (true) {// clip test - seems to work, yay
        Rectangle2D rect = new Rectangle2D.Float();
        rect.setRect(0, 0, wdt, hgt);
        if (false) {// disable real clipping to see how much unnecessary drawing is being done.
          g2d.setClip(rect);
        }
        Stroke oldStroke = g2d.getStroke();
        BasicStroke bs = new BasicStroke(5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
        g2d.setStroke(bs);
        g2d.setColor(Color.red);
        g2d.draw(rect);// red rectangle confidence check for clipping
        g2d.setStroke(oldStroke);
        dc.ClipBounds.Assign(0, 0, wdt, hgt);// problem: why does project disappear when clip bounds cover its root? 
      } else {
        dc.ClipBounds.Assign(0, 0, 10000, 10000);// arbitrarily large
      }
      dc.Offset = new OffsetBox();
      dc.GlobalOffset = new OffsetBox();
      this.MyProject.GraphicRoot.Draw_Me(dc);
    }
    /* ********************************************************************************* */
    @Override public void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2d = (Graphics2D) g;

      Polygon p = new Polygon();
      for (int i = 0; i < 5; i++) {
        p.addPoint((int) (100 + 50 * Math.cos(i * 2 * Math.PI / 5)), (int) (100 + 50 * Math.sin(i * 2 * Math.PI / 5)));
      }
      g2d.drawPolygon(p);

//      Drawing_Context dc = new Drawing_Context();
//      dc.gr = g2d;
      // dc.ClipBounds.Assign(0, 0, 10000, 10000);// arbitrarily large
//      dc.Offset = new OffsetBox(); dc.GlobalOffset = new OffsetBox();
      //MyProject.Draw_Me(g2d);
      Draw_Me(g2d);
    }
    /* ********************************************************************************* */
    @Override public void mouseDragged(MouseEvent me) {
    }
    @Override public void mouseMoved(MouseEvent me) {
    }
    boolean Toggle = true;// temporary until we have better ui
    /* ********************************************************************************* */
    @Override public void mouseClicked(MouseEvent me) {
      if (Toggle) {
        BigApp.MyThread.start();
        Toggle = false;
      } else {
        BigApp.MyThread.PleaseStop();
        Toggle = true;
      }
    }
    @Override public void mousePressed(MouseEvent me) {
    }
    @Override public void mouseReleased(MouseEvent me) {
      double XCtr, YCtr, Scale;
      if (false) {// for testing without mouse wheel
        Scale = 1.1;
        if (me.getButton() == MouseEvent.BUTTON1) {
          Scale = 1.1;;
        } else {
          Scale = 0.9;
        }
        XCtr = me.getX();
        YCtr = me.getY();
        GraphicBox.Graphic_OffsetBox gb = this.MyProject.GraphicRoot;
        gb.Zoom(XCtr, YCtr, Scale);
        this.repaint();
      }
    }
    @Override public void mouseEntered(MouseEvent me) {
    }
    @Override public void mouseExited(MouseEvent me) {
    }
    /* ********************************************************************************* */
    @Override public void mouseWheelMoved(MouseWheelEvent mwe) {
      double XCtr, YCtr, Rescale;
      XCtr = mwe.getX();
      YCtr = mwe.getY();
      double finerotation = mwe.getPreciseWheelRotation();
      Rescale = Math.pow(2, finerotation);// range 0 to 1 to positive infinity
      if (false) {// use these later
        int rotation = mwe.getWheelRotation();
        int amount = mwe.getScrollAmount();
        mwe.isControlDown();
        mwe.isAltDown();
      }
      GraphicBox.Graphic_OffsetBox gb = this.MyProject.GraphicRoot;
      gb.Zoom(XCtr, YCtr, Rescale);
      this.repaint();
    }
    /* ********************************************************************************* */
    @Override public void componentResized(ComponentEvent ce) {
    }
    @Override public void componentMoved(ComponentEvent ce) {
    }
    @Override public void componentShown(ComponentEvent ce) {
    }
    @Override public void componentHidden(ComponentEvent ce) {
    }
    /* ********************************************************************************* */
    @Override public void keyTyped(KeyEvent ke) {
      System.exit(0);
    }
    @Override public void keyPressed(KeyEvent ke) {
    }
    @Override public void keyReleased(KeyEvent ke) {
    }
  }
}
