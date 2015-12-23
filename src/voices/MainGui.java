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
      int wdt = this.getWidth() / 1;
      int hgt = this.getHeight() / 1;
      if (true) {// clip test - seems to work, yay
        Rectangle2D rect = new Rectangle2D.Float();
        rect.setRect(0, 0, wdt, hgt);
        g2d.setClip(rect);
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
    }
    @Override public void mouseEntered(MouseEvent me) {
    }
    @Override public void mouseExited(MouseEvent me) {
    }
    /* ********************************************************************************* */
    @Override public void mouseWheelMoved(MouseWheelEvent mwe) {
      int rotation = mwe.getWheelRotation();
      double finerotation = mwe.getPreciseWheelRotation();
      int amount = mwe.getScrollAmount();
      mwe.getX();
      mwe.getY();
      mwe.isControlDown();
      mwe.isAltDown();
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
