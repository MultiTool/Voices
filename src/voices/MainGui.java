package voices;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
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
    this.drawpanel.MyProject = this.MyProject;
    frame.setVisible(true);
  }
  /* ********************************************************************************* */
  public static class DrawingPanel extends JPanel implements MouseMotionListener, MouseListener, KeyListener {
    Project MyProject = null;
    /* ********************************************************************************* */
    public DrawingPanel() {
    }
    /* ********************************************************************************* */
    @Override public void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g;
      Polygon p = new Polygon();
      for (int i = 0; i < 5; i++) {
        p.addPoint((int) (100 + 50 * Math.cos(i * 2 * Math.PI / 5)),
          (int) (100 + 50 * Math.sin(i * 2 * Math.PI / 5)));
      }
      g2.drawPolygon(p);
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
      // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override public void mousePressed(MouseEvent me) {
      // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override public void mouseReleased(MouseEvent me) {
      // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override public void mouseEntered(MouseEvent me) {
      // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override public void mouseExited(MouseEvent me) {
      // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    /* ********************************************************************************* */
    @Override public void keyTyped(KeyEvent ke) {
      System.exit(0);// not working yet
    }
    @Override public void keyPressed(KeyEvent ke) {
      // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override public void keyReleased(KeyEvent ke) {
      // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
  }
}
