package voices;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.swing.*;
import voices.DrawingContext;
// From http://www.tutorialspoint.com/javaexamples/gui_polygon.htm

public class MainGui {
  public JFrame frame;
  public DrawingPanel drawpanel;
  AudProject MyProject = null;
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
    MyProject = new AudProject();
    this.MyThread.MyProject = this.MyProject;
    //MyProject.Create_For_Graphics();
    MyProject.Compose_Test();
    this.drawpanel.BigApp = this;
    this.drawpanel.MyProject = this.MyProject;
    MakeButtons();
    KeyBindings(this.drawpanel);
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
    AudProject MyProject = null;
    MainGui BigApp;
    Grabber Query;
    Grabber.DestinationGrabber DestQuery;
    int ScreenMouseX = 0, ScreenMouseY = 0;
    //public IDrawable.IMoveable Floater = null;// copy we are dragging around (in hover mode, no mouse buttons) 
    Point2D.Double results = new Point2D.Double();// used in multiple places as a return parameter for mapping
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
      this.Query = new Grabber();
      this.DestQuery = new Grabber.DestinationGrabber();
    }
    /* ********************************************************************************* */
    public void Draw_Me(Graphics2D g2d) {
      // to do: move this to MainGui, and base clipping, zoom etc. on canvas size. 
      DrawingContext dc = new DrawingContext();
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
    public IDrawable.IMoveable GetFloater() {
      return this.MyProject.GraphicRoot.Content.Floater;
    }
    public void SetFloater(IDrawable.IMoveable floater) {
      this.MyProject.GraphicRoot.Content.Floater = floater;
    }
    public void MoveFloater(double ScreenX, double ScreenY) {
      this.MyProject.GraphicRoot.MapTo(ScreenX, ScreenY, results);
      this.GetFloater().MoveTo(results.x, results.y);
      this.GetFloater().UpdateBoundingBoxLocal();

      if (true) {
        if (this.DestQuery.PossibleDestination != null) {
          this.DestQuery.PossibleDestination.SetSpineHighlight(false);
        }
        this.DestQuery.AddFirstBox(this.MyProject.GraphicRoot, ScreenX, ScreenY);
        // this is really ugly.  #kludgey
        this.MyProject.GraphicRoot.Content.ContentOBox.GoFishing(this.DestQuery);// call this on graphic songlet's child obox. 
        this.DestQuery.DecrementStack();
        //this.DestQuery.Leaf.SetSelected(true);
        if (this.DestQuery.PossibleDestination != null) {
          this.DestQuery.PossibleDestination.SetSpineHighlight(true);
        }
      }
      this.repaint();
    }
    /* ********************************************************************************* */
    public void DeleteBranch() {
      ISonglet songlet;
      if (this.Query.Leaf != null) {
        BigApp.MyThread.PleaseStop();
        IDrawable.IMoveable Leaf = this.Query.Leaf;
        if (Leaf instanceof MonkeyBox) {// hmm, we might be able to delete from voice and groupbox without having to know which is which
          MonkeyBox mbx = (MonkeyBox) Leaf;// another cast! 
          songlet = mbx.MyParentSong;// we would just need an IContainer interface with a virtual Remove_SubNode(MonkeyBox)
//          if (songlet instanceof IContainer) {
//            IContainer con = (IContainer)songlet;
//            con.Remove_SubNode(mbx);
//          }
        }
        if (Leaf instanceof OffsetBox) {
          OffsetBox obx = (OffsetBox) Leaf;// another cast! 
          songlet = obx.MyParentSong;
          if (songlet instanceof GroupBox) {
            GroupBox gbx = (GroupBox) songlet;
            gbx.Remove_SubSong(obx);
            this.repaint();
          }
        } else if (Leaf instanceof VoicePoint) {
          VoicePoint vpnt = (VoicePoint) Leaf;// another cast! 
          songlet = vpnt.MyParentSong;
          if (songlet instanceof Voice) {
            Voice voz = (Voice) songlet;
            voz.Remove_Note(vpnt);
            this.repaint();
          }
        }
      }
    }
    /* ********************************************************************************* */
    public void CopyBranch(double XDisp, double YDisp) {
      if (this.Query.Leaf != null) {
        {
          BigApp.MyThread.PleaseStop();
        }
        IDrawable.IMoveable Leaf = this.Query.Leaf;
        if (Leaf instanceof OffsetBox) {
          OffsetBox obx = (OffsetBox) Leaf;// another cast! 
          OffsetBox clone = obx.Deep_Clone_Me();
          if (false) {// one catch is that if this is a loopbox ghost handle we get a copy of the ghost instead of a native offsetbox
            ISonglet songlet = obx.GetContent();
            OffsetBox ObxCopy = songlet.Spawn_OffsetBox();
            ObxCopy.Copy_From(obx);// transfer original offsets
            clone = ObxCopy.Deep_Clone_Me();
          }
          this.Query.CompoundStack(this.MyProject.AudioRoot, clone);
          this.SetFloater(clone);// only deep clone handles to songlets. do not clone loudness handles. clone voicepoints? 
          MoveFloater(XDisp, YDisp);
        }
//        if (Leaf.getClass().isMemberClass() == OffsetBox.class) {
//        }
      }
    }
    /* ********************************************************************************* */
    @Override public void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2d = (Graphics2D) g;
      Draw_Me(g2d);
    }
    /* ********************************************************************************* */
    @Override public void mouseDragged(MouseEvent me) {
      if (this.Query.Leaf != null) {
        {
          BigApp.MyThread.PleaseStop();
        }
        this.Query.MapThroughStack(me.getX(), me.getY(), results);
        this.Query.Leaf.MoveTo(results.x, results.y);
        this.repaint();
      }
    }
    @Override public void mouseMoved(MouseEvent me) {
      this.ScreenMouseX = me.getX();
      this.ScreenMouseY = me.getY();
      // to do: transform coordinates
      IDrawable.IMoveable floater = this.GetFloater();
      if (true && floater != null) {
        MoveFloater(this.ScreenMouseX, this.ScreenMouseY);
      }
    }
    /* ********************************************************************************* */
    @Override public void mouseClicked(MouseEvent me) {
    }
    @Override public void mousePressed(MouseEvent me) {
      if (this.Query.Leaf != null) {
        this.Query.Leaf.SetSelected(false);
      }
      if (this.GetFloater() != null) {// need to delete these
        this.SetFloater(null);
      }
      this.Query.AddFirstBox(this.MyProject.GraphicRoot, me.getX(), me.getY());
      //this.MyProject.GraphicRoot.GoFishing(this.Query);
      // this is really ugly.  #kludgey
      this.MyProject.GraphicRoot.Content.ContentOBox.GoFishing(Query);// call this on graphic songlet's child obox. 
      this.Query.DecrementStack();
      if (this.Query.Leaf != null) {
        this.Query.Leaf.SetSelected(true);
        //this.repaint();
      }
      this.repaint();
    }
    @Override public void mouseReleased(MouseEvent me) {
      if (this.Query.Leaf != null) {
        if (false) {// disable to make selected persistent, for copy paste, del etc. 
          this.Query.Leaf.SetSelected(false);
        }
        this.MyProject.Update_Guts();
        this.MyProject.GraphicRoot.UpdateBoundingBox();
        //this.Query.UpdateBoundingBoxes();
        {
          double TimeRadius = 0.15;
          double Time = this.MyProject.GraphicRoot.MapTime(me.getX());
          BigApp.MyThread.Skip_To(Time - TimeRadius);
          BigApp.MyThread.Assign_Stop_Time(Time + TimeRadius);
          BigApp.MyThread.start();
        }
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
    }
    @Override public void keyPressed(KeyEvent ke) {
      System.out.println("keyPressed:" + ke.getKeyCode() + ":" + ke.getExtendedKeyCode() + ":" + ke.getModifiers() + ":" + ke.getKeyChar() + ":" + ke.getModifiersEx());
      char ch = Character.toLowerCase(ke.getKeyChar());
      int keycode = ke.getKeyCode();
      int mod = ke.getModifiers();
      boolean CtrlPress = ((mod & KeyEvent.CTRL_MASK) != 0);
      if ((keycode == KeyEvent.VK_C) && CtrlPress) {
//        this.Query.MapThroughStack(this.ScreenMouseX, this.ScreenMouseY, results);
//        this.CopyBranch(results.x, results.y);
        this.CopyBranch(this.ScreenMouseX, this.ScreenMouseY);
      } else if (keycode == KeyEvent.VK_DELETE) {
        this.DeleteBranch();
      } else if ((keycode == KeyEvent.VK_Q) && CtrlPress) {
        System.exit(0);
      } else if (keycode == KeyEvent.VK_ESCAPE) {
        if (this.GetFloater() != null) {// to do: delete Floater if not used
          this.SetFloater(null);
          this.repaint();
        }
      }
    }
    @Override public void keyReleased(KeyEvent ke) {
      //System.out.println("keyReleased:" + ke.getKeyCode());
    }
  }
  /* ********************************************************************************* */
  public void KeyBindings(JPanel panel) {
    InputMap inputMap = panel.getInputMap();
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false), "Next");
    ActionMap actionMap = panel.getActionMap();
    actionMap.put("Next", new NextAction());
  }
  class NextAction extends AbstractAction {
    public void actionPerformed(ActionEvent actionEvent) {
      System.out.println("Next");
    }
  }
}
