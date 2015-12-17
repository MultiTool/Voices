package voices;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import voices.IDrawable.Drawing_Context;
// From http://www.tutorialspoint.com/javaexamples/gui_polygon.htm

public class MainGui {
  public JFrame frame;
  public DrawingPanel drawpanel;
  Project MyProject = null;
  /* ********************************************************************************* */
  public MainGui() {
  }
  /* ********************************************************************************* */
  public void Init() {
    // this.MyProject = prj;//Project prj
    this.frame = new JFrame();
    this.frame.setTitle("Polygon");
    this.frame.setSize(350, 250);
    this.frame.addWindowListener(new WindowAdapter() {
      @Override public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    Container contentPane = this.frame.getContentPane();
    this.drawpanel = new DrawingPanel();
    contentPane.add(this.drawpanel);
    MyProject = new Project();
    //MyProject.Create_For_Graphics();
    MyProject.Compose_Test();
    this.drawpanel.MyProject = this.MyProject;
    frame.setVisible(true);
  }
  /* ********************************************************************************* */
  public static class DrawingPanel extends JPanel implements MouseMotionListener, MouseListener, MouseWheelListener, ComponentListener, KeyListener {
    Project MyProject = null;
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
    @Override public void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2d = (Graphics2D) g;
      Polygon p = new Polygon();
      for (int i = 0; i < 5; i++) {
        p.addPoint((int) (100 + 50 * Math.cos(i * 2 * Math.PI / 5)), (int) (100 + 50 * Math.sin(i * 2 * Math.PI / 5)));
      }
      g2d.drawPolygon(p);

      Drawing_Context dc = new Drawing_Context();
      dc.gr = g2d;
      dc.ClipBounds = new CajaDelimitadora();
      dc.ClipBounds.Assign(0, 0, 10000, 10000);// arbitrarily large
      dc.Offset = new OffsetBox();
      dc.GlobalOffset = new OffsetBox();
      //this.MyProject;

      MyProject.Draw_Me(g2d);
    }
    /* ********************************************************************************* */
    @Override public void mouseDragged(MouseEvent me) {
      // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override public void mouseMoved(MouseEvent me) {
      // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    /* ********************************************************************************* */
    @Override public void mouseClicked(MouseEvent me) {
      this.MyProject.Audio_Test();
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
