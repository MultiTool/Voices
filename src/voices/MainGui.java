package voices;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
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
    this.frame.setSize(700, 400);
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
    MakeButtons();
    frame.setVisible(true);
  }
  /* ********************************************************************************* */
  public void MakeButtons() {
    javax.swing.JButton PlayButton = new JButton("Play");
    PlayButton.setVerticalTextPosition(AbstractButton.CENTER);
    PlayButton.setHorizontalTextPosition(AbstractButton.CENTER);
    this.drawpanel.add(PlayButton);
    PlayButton.addActionListener(new Act(this) {
      @Override public void actionPerformed(ActionEvent e) {
        this.BigApp.MyThread.Play_All();
      }
    });

    JButton StopButton = new JButton("Stop");
    StopButton.setVerticalTextPosition(AbstractButton.CENTER);
    StopButton.setHorizontalTextPosition(AbstractButton.CENTER);
    this.drawpanel.add(StopButton);
    StopButton.addActionListener(new Act(this) {
      @Override public void actionPerformed(ActionEvent e) {
        this.BigApp.MyThread.PleaseStop();
      }
    });

    JButton SaveButton = new JButton("Export Audio");
    SaveButton.setVerticalTextPosition(AbstractButton.CENTER);
    SaveButton.setHorizontalTextPosition(AbstractButton.CENTER);
    this.drawpanel.add(SaveButton);
    SaveButton.addActionListener(new Act(this) {
      @Override public void actionPerformed(ActionEvent e) {
        this.BigApp.SaveAudio();
      }
    });
  }
  /* ********************************************************************************* */
  class Act implements ActionListener {
    MainGui BigApp;
    public Act(MainGui App) {
      this.BigApp = App;
    }
    @Override public void actionPerformed(ActionEvent ae) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
  }
  /* ********************************************************************************* */
  public void SaveAudio() {
    Audio aud = new Audio();
    aud.SaveAudio("sample.wav", this.drawpanel.MyProject.AudioRoot);
  }
  /* ********************************************************************************* */
  public static class DrawingPanel extends JPanel implements MouseMotionListener, MouseListener, MouseWheelListener, ComponentListener, KeyListener {
    Project MyProject = null;
    MainGui BigApp;
    HookAndLure Query;
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
      this.Query = new HookAndLure();
    }
    /* ********************************************************************************* */
    public void Draw_Me(Graphics2D g2d) {
      // to do: move this to MainGui, and base clipping, zoom etc. on canvas size. 
      Drawing_Context dc = new Drawing_Context();
      dc.gr = g2d;
      int wdt, hgt;
//      wdt = this.getWidth() * 7 / 8; hgt = this.getHeight() * 7 / 8;
      wdt = this.getWidth();
      hgt = this.getHeight();

      Rectangle2D rect = new Rectangle2D.Float();
      rect.setRect(0, 0, wdt, hgt);
      if (false) {// disable real clipping to see how much unnecessary drawing is being done.
        g2d.setClip(rect);
      }
      Stroke oldStroke = g2d.getStroke();
      BasicStroke bs = new BasicStroke(5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
      g2d.setStroke(bs);
      g2d.setColor(Color.green);
      g2d.draw(rect);// red rectangle confidence check for clipping
      g2d.setStroke(oldStroke);
      dc.ClipBounds.Assign(0, 0, wdt, hgt);

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
      if (this.Query.Leaf != null) {
        {
          BigApp.MyThread.PleaseStop();
          Toggle = true;
        }
        Point2D.Double results = new Point2D.Double();
        this.Query.MapThroughStack(me.getX(), me.getY(), results);
        this.Query.Leaf.MoveTo(results.x, results.y);
        this.repaint();
      }
    }
    @Override public void mouseMoved(MouseEvent me) {
    }
    boolean Toggle = true;// temporary until we have better ui
    /* ********************************************************************************* */
    @Override public void mouseClicked(MouseEvent me) {
      if (false) {
        if (Toggle) {
          BigApp.MyThread.Play_All();
          Toggle = false;
        } else {
          BigApp.MyThread.PleaseStop();
          Toggle = true;
        }
      }
    }
    @Override public void mousePressed(MouseEvent me) {
      this.Query.AddFirstBox(this.MyProject.GraphicRoot, me.getX(), me.getY());
      //this.MyProject.GraphicRoot.GoFishing(this.Query);
      // this is really ugly.  #kludgey
      this.MyProject.GraphicRoot.Content.ContentOBox.GoFishing(Query);// call this on graphic songlet's child obox. 
      this.Query.DecrementStack();
      System.out.println();
    }
    @Override public void mouseReleased(MouseEvent me) {
      double XCtr, YCtr, Scale;
      if (this.Query.Leaf != null) {
        this.MyProject.Update_Guts();
        this.MyProject.GraphicRoot.UpdateBoundingBox();
        //this.Query.UpdateBoundingBoxes();
        {
          double TimeRadius = 0.25;
          double Time = this.MyProject.GraphicRoot.MapTime(me.getX());
          BigApp.MyThread.Skip_To(Time - TimeRadius);
          BigApp.MyThread.Assign_Stop_Time(Time + TimeRadius);
          BigApp.MyThread.start();
          Toggle = false;
        }
        this.repaint();
      }
      if (false) {// for testing without mouse wheel
        Scale = 1.1;
        if (me.getButton() == MouseEvent.BUTTON1) {
          Scale = 1.1;
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
      finerotation = -finerotation * 0.2;
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
