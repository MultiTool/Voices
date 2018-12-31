package voices;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import voices.DrawingContext;
import voices.GoLive.LivePlayerCallbacks;
import voices.GroupBox.Group_OffsetBox;
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
    this.MyThread.AttachCallback(drawpanel);
    MakeButtons();
//    KeyBindings(this.drawpanel);
    Bleh();
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
  public static class DrawingPanel extends JPanel implements MouseMotionListener, MouseListener, MouseWheelListener, ComponentListener, KeyListener, LivePlayerCallbacks {
    AudProject MyProject = null;
    MainGui BigApp;
    Grabber Query;
    Grabber.DestinationGrabber DestQuery;
    int ScreenMouseX = 0, ScreenMouseY = 0;
    double MouseOffsetX = 0, MouseOffsetY = 0;
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
      DrawingContext dc = new DrawingContext();
      dc.gr = g2d;
      int wdt, hgt;

      this.ResetPlaybackCursor();

//      wdt = this.getWidth() * 7 / 8; hgt = this.getHeight() * 7 / 8;
      wdt = this.getWidth();
      hgt = this.getHeight();

      Rectangle2D rect = new Rectangle2D.Float();
      if (true) {
        rect.setRect(0, 0, wdt, hgt);
      } else {
        int shrink = 100;// to test clipping
        rect.setRect(shrink, shrink, wdt - (shrink * 2), hgt - (shrink * 2));
      }
      if (false) {// disable real clipping to see how much unnecessary drawing is being done.
        g2d.setClip(rect);
      }
      Stroke oldStroke = g2d.getStroke();
      BasicStroke bs = new BasicStroke(5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
      g2d.setStroke(bs);
      g2d.setColor(Color.green);
      g2d.draw(rect);// green rectangle confidence check for clipping
      g2d.setStroke(oldStroke);
      dc.ClipBounds.Assign(rect.getMinX(), rect.getMinY(), rect.getMaxX(), rect.getMaxY());
      dc.Offset = new OffsetBox();
      dc.GlobalOffset = new OffsetBox();
      this.MyProject.GraphicRoot.Draw_Me(dc);
    }
    /* ********************************************************************************* */
    public void RescaleGroupTimeX() {
      if (this.Query.Leaf != null) {//if anything is selected
        if (this.Query.Leaf instanceof Group_OffsetBox) {
          Group_OffsetBox gobx = (Group_OffsetBox) this.Query.Leaf;// another cast!
          //gobx.RescaleGroupTimeX(0.5);
          gobx.ScaleX = 0.5;// *= 0.75;
          this.MyProject.Update_Guts();
          this.repaint();
          /*
           to do: get distance between current mouse and selected's origin
           or rather map current mouse to selected's inner coords
           first position found is 1:1
           any further movements, map them, get ratio to first position.  always current/first. 
           */
          this.Query.MapThroughStack(this.ScreenMouseX, this.ScreenMouseY, results);
          double Dist = Math.hypot(results.x - gobx.TimeX, results.y - gobx.OctaveY);
        }
      }
    }
    /* ********************************************************************************* */
    public void HighlightTarget(boolean Highlight) {
      if (this.DestQuery.PossibleDestination != null) {
        this.DestQuery.PossibleDestination.SetSpineHighlight(Highlight);
      }
    }
    /* ********************************************************************************* */
    public OffsetBox DropOnTarget() {
      IDrawable.IMoveable floater = this.GetFloater();
      OffsetBox obox = null;
      if (floater != null && floater instanceof OffsetBox) {
        if (this.DestQuery.PossibleDestination != null) {
          obox = (OffsetBox) floater;
          this.DestQuery.MapThroughStack(obox.TimeX, obox.OctaveY, this.MyProject.AudioRoot, results);// Map new location to destination
          if (true) {// The destination is not the offsetbox, but instead the songlet that offsetbox points to. So we must map one level deeper. 
            OffsetBox UltimaCaja = (OffsetBox) this.DestQuery.Leaf;// another cast! 
            UltimaCaja.MapTo(results.x, results.y, results);
          }
          this.DestQuery.Floater = null;// reset
          obox.TimeX = results.x;
          obox.OctaveY = results.y;
          this.DestQuery.PossibleDestination.Add_SubSong(obox);
          this.MyProject.Update_Guts();
          this.MyProject.GraphicRoot.UpdateBoundingBox();
          this.repaint();
        }
      }
      return obox;
    }
    /* ********************************************************************************* */
    public IDrawable.IMoveable GetFloater() {
      return this.MyProject.GraphicRoot.Content.Floater;
    }
    public void SetFloater(IDrawable.IMoveable floater) {
      if (floater == null) {
        //this.MyProject.GraphicRoot.Content.Floater.Delete_Me();
      }
      this.MyProject.GraphicRoot.Content.Floater = floater;
    }
    public void MoveFloater(double ScreenX, double ScreenY) {
      this.MyProject.GraphicRoot.MapTo(ScreenX, ScreenY, results);
      this.GetFloater().MoveTo(results.x, results.y);// free independent floater
      this.GetFloater().UpdateBoundingBoxLocal();

      if (true) {
        this.HighlightTarget(false);
        this.DestQuery.AddFirstBox(this.MyProject.GraphicRoot, ScreenX, ScreenY);
        // this is really ugly.  #kludgey
        this.MyProject.GraphicRoot.Content.ContentOBox.GoFishing(this.DestQuery);// call this on graphic songlet's child obox. 
        this.DestQuery.DecrementStack();
        this.HighlightTarget(true);
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
          if (songlet instanceof ISonglet. IContainer) {
            ISonglet.IContainer con = (ISonglet.IContainer)songlet;
            con.Remove_SubNode(mbx);
          }
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
    public void BreakFromHerd() {
      if (this.Query.Leaf != null) {
        IDrawable.IMoveable Leaf = this.Query.Leaf;
        if (Leaf instanceof OffsetBox) {
          System.out.println("MainGui BreakFromHerd");
          this.Query.Leaf = null;
          OffsetBox obx = (OffsetBox) Leaf;// another cast! 
          BigApp.MyThread.PleaseStop();
          ITextable.CollisionLibrary HitTable = new ITextable.CollisionLibrary();
          obx.BreakFromHerd(HitTable);
          //BreakFromHerd_Shallow
          this.repaint();
        }
      }
    }
    /* ********************************************************************************* */
    public void BreakFromHerd_Shallow() {// Shallow cuts
      if (this.Query.Leaf != null) {
        IDrawable.IMoveable Leaf = this.Query.Leaf;
        if (Leaf instanceof Group_OffsetBox) {
          System.out.println("MainGui BreakFromHerd_Shallow");
          this.Query.Leaf = null;
          Group_OffsetBox obx = (Group_OffsetBox) Leaf;// another cast! 
          BigApp.MyThread.PleaseStop();
          obx.BreakFromHerd_Shallow();
          this.repaint();
        }
      }
    }
    /* ********************************************************************************* */
    public void Space_Evenly() {
      if (this.Query.Leaf != null) {
        IDrawable.IMoveable Leaf = this.Query.Leaf;
        if (Leaf instanceof Group_OffsetBox) {
          System.out.println("MainGui Space_Evenly");
          this.Query.Leaf = null;
          Group_OffsetBox obx = (Group_OffsetBox) Leaf;// another cast! 
          BigApp.MyThread.PleaseStop();
          obx.Content.Space_Evenly();
          this.repaint();
        }
      }
    }
    //()
    /* ********************************************************************************* */
    public void InstantPlayback(double XLoc) {
      if (this.Query.Leaf != null) {
        IDrawable.IMoveable Leaf = this.Query.Leaf;
        OffsetBox PlayHandle;

        this.ResetPlaybackCursor();

        if (Leaf instanceof OffsetBox) {// if we moved a whole songlet, play the whole thing
          OffsetBox obx = (OffsetBox) Leaf;// another cast! 
          PlayHandle = obx.GetContent().Spawn_OffsetBox();
          BigApp.MyThread.PleaseStop();
          this.Query.CompoundStack(this.MyProject.AudioRoot, PlayHandle);
          PlayHandle.Compound(obx);// Why do we have to do this? It should work without this extra compound. The final item in the stack should be obox iteslf. #kludgey
          BigApp.MyThread.Play_Branch(PlayHandle);
        } else {// if we just moved a voicepoint, play a small sample
          double TimeRadius = 0.15;
          double Time = this.MyProject.GraphicRoot.MapTime(XLoc);
          BigApp.MyThread.Skip_To(Time - TimeRadius);
          BigApp.MyThread.Assign_Stop_Time(Time + TimeRadius);
          BigApp.MyThread.start();// to do: find the voicepoint's voice offsetbox and play through that, shutting out simultaneous voices. 
        }
      }
    }
    /* ********************************************************************************* */
    public void CopyBranch(double XDisp, double YDisp) {
      if (this.Query.Leaf != null) {
        BigApp.MyThread.PleaseStop();
        IDrawable.IMoveable Leaf = this.Query.Leaf;
        OffsetBox FloatHandle, clone;
        if (Leaf instanceof OffsetBox) {
          OffsetBox obx = (OffsetBox) Leaf;// another cast! 
          //FloatHandle = obx.Deep_Clone_Me(HitTable);
          FloatHandle = obx.GetContent().Spawn_OffsetBox();
          FloatHandle.Copy_From(obx);
          FloatHandle.MyParentSong = null;
          if (false) {// one catch is that if this is a loopbox ghost handle we get a copy of the ghost instead of a native offsetbox
            ISonglet songlet = obx.GetContent();
            OffsetBox ObxCopy = songlet.Spawn_OffsetBox();
            ObxCopy.Copy_From(obx);// transfer original offsets
            ITextable.CollisionLibrary HitTable = new ITextable.CollisionLibrary();
            clone = ObxCopy.Deep_Clone_Me(HitTable);
          }
          this.Query.CompoundStack(this.MyProject.AudioRoot, FloatHandle);
          this.SetFloater(FloatHandle);// only deep clone handles to songlets. do not clone loudness handles. clone voicepoints? 
          this.DestQuery.Floater = obx;
          MoveFloater(XDisp, YDisp);
        } else if (Leaf instanceof VoicePoint) {// copy Voice control point
          VoicePoint vpnt = (VoicePoint) Leaf;// another cast! 
          this.Query.MapThroughStack(XDisp, YDisp, results);
          vpnt.MyParentVoice.Add_Note(results.x, results.y, 1.0);
          vpnt.MyParentVoice.Sort_Me();
          this.MyProject.GraphicRoot.UpdateBoundingBox();
          this.repaint();
          System.out.println("VoicePoint create");
        }
//        if (Leaf.getClass().isMemberClass() == OffsetBox.class) {
//        }
      }
    }
    /* ********************************************************************************* */
    @Override public void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2d = (Graphics2D) g;
      Draw_Me(g2d);// redrawing everything is overkill for every little change or move. to do: optimize this
    }
    /* ********************************************************************************* */
    @Override public void mouseDragged(MouseEvent me) {
      if (this.Query.Leaf != null) {
        {
          BigApp.MyThread.PleaseStop();
        }
        double XLoc = me.getX();
        double YLoc = me.getY();
        //MouseOffsetX = MouseOffsetY = 0;
        this.Query.MapThroughStack(XLoc + MouseOffsetX, YLoc + MouseOffsetY, results);
        this.Query.Leaf.MoveTo(results.x, results.y);// move while attached to tree
        this.repaint();
      }
    }
    @Override public void mouseMoved(MouseEvent me) {
      this.ScreenMouseX = me.getX();
      this.ScreenMouseY = me.getY();
      // to do: transform coordinates
      IDrawable.IMoveable floater = this.GetFloater();
      if (floater != null) {
        MoveFloater(this.ScreenMouseX, this.ScreenMouseY);
      }
    }
    /* ********************************************************************************* */
    @Override public void mouseClicked(MouseEvent me) {
    }
    @Override public void mousePressed(MouseEvent me) {
      if (this.Query.Leaf != null) {
        this.Query.Leaf.SetSelected(false);
        this.Query.Leaf = null;
      }
      if (this.GetFloater() != null) {// need to Delete_Me these
        this.DropOnTarget();
        this.SetFloater(null);
        this.HighlightTarget(false);
        //this.repaint();return;// to do: make the just-dropped copy the currently selected leaf, without a new gofishing search.
      }
      {
        {// go fishing
          this.Query.AddFirstBox(this.MyProject.GraphicRoot, me.getX(), me.getY());
          //this.MyProject.GraphicRoot.GoFishing(this.Query);
          // this is really ugly.  #kludgey
          this.MyProject.GraphicRoot.Content.ContentOBox.GoFishing(Query);// call this on graphic songlet's child obox. 
          this.Query.DecrementStack();
        }
        if (this.Query.Leaf != null) {
          this.Query.Leaf.SetSelected(true);
          if (this.Query.Leaf instanceof MonkeyBox) {// drag handles by wherever we clicked them, by the edge or whatever, not just by their centers.
            MonkeyBox leafbox = (MonkeyBox) this.Query.Leaf;// another cast!
            this.Query.UnMapThroughStack(leafbox.TimeX, leafbox.OctaveY, results);
            MouseOffsetX = results.x - me.getX();
            MouseOffsetY = results.y - me.getY();
          } else {// special case for loudness handles
            MouseOffsetX = MouseOffsetY = 0;
          }
        }
      }
      this.repaint();
    }
    @Override public void mouseReleased(MouseEvent me) {
      if (this.Query.Leaf != null) {
        this.MyProject.Update_Guts();
        this.MyProject.GraphicRoot.UpdateBoundingBox();
        this.InstantPlayback(me.getX());
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
      if (false) {
        System.out.println("keyPressed:" + ke.getKeyCode() + ":" + ke.getExtendedKeyCode() + ":" + ke.getModifiers() + ":" + ke.getKeyChar() + ":" + ke.getModifiersEx());
        char ch = Character.toLowerCase(ke.getKeyChar());
        int keycode = ke.getKeyCode();
        int mod = ke.getModifiers();
        boolean CtrlPress = ((mod & KeyEvent.CTRL_MASK) != 0);
        if ((keycode == KeyEvent.VK_C) && CtrlPress) {
          this.CopyBranch(this.ScreenMouseX, this.ScreenMouseY);
        } else if (keycode == KeyEvent.VK_DELETE) {
          this.DeleteBranch();
        } else if ((keycode == KeyEvent.VK_Q) && CtrlPress) {
          System.exit(0);
        } else if (keycode == KeyEvent.VK_ESCAPE) {
          if (this.GetFloater() != null) {// to do: delete Floater if not used
            this.SetFloater(null);
            this.HighlightTarget(false);
            this.repaint();
          }
        }
      }
    }
    @Override public void keyReleased(KeyEvent ke) {
      //System.out.println("keyReleased:" + ke.getKeyCode());
    }
    /* ********************************************************************************* */
    // Playback cursor
    int PrevTime0 = 0, PrevTime1 = 0;
    int TimeInt0 = 0, TimeInt1 = 0;
    /* ********************************************************************************* */
    public void ResetPlaybackCursor() {
      this.PrevTime0 = 0;
      this.PrevTime1 = 0;
      this.TimeInt0 = 0;
      this.TimeInt1 = 0;
    }
    /* ********************************************************************************* */
    public void DrawPlaybackCursor() {
      Graphics2D gr = (Graphics2D) this.getGraphics();
      gr.setXORMode(Color.white);
      gr.fillRect(TimeInt0, 0, TimeInt1 - TimeInt0, this.getHeight());// draw new
    }
    /* ********************************************************************************* */
    @Override public void LivePlayerUpdate(double Time0, double Time1) {// LivePlayerCallbacks
      System.out.println("LiveCall:" + Time0 + " to " + Time1);// to do: draw moving cursor here
      Graphics2D gr = (Graphics2D) this.getGraphics();

      PrevTime0 = TimeInt0;
      PrevTime1 = TimeInt1;
      TimeInt0 = (int) this.MyProject.GraphicRoot.UnMapTime(Time0);
      TimeInt1 = (int) this.MyProject.GraphicRoot.UnMapTime(Time1);

      gr.setXORMode(Color.white);//gr.setColor(Color.red);
      gr.fillRect(PrevTime0, 0, PrevTime1 - PrevTime0, this.getHeight());// clear old
      this.DrawPlaybackCursor();// draw new
    }
    /* ********************************************************************************* */
    @Override public void LivePlayerFinished() {// LivePlayerCallbacks
      this.DrawPlaybackCursor();
      this.ResetPlaybackCursor();
    }
  }
  /* ********************************************************************************* */
  public void Bleh() {// https://tips4java.wordpress.com/2009/08/30/global-event-listeners/
    long EventMask = AWTEvent.MOUSE_MOTION_EVENT_MASK + AWTEvent.MOUSE_EVENT_MASK + AWTEvent.KEY_EVENT_MASK;
    EventMask = AWTEvent.KEY_EVENT_MASK;
    //final DrawingPanel dp = this.drawpanel;
    Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
      @Override public void eventDispatched(AWTEvent Event) {
        KeyEvent ke = (KeyEvent) Event;
        HandleKeys(ke);
      }
    }, EventMask);
  }
  /* ********************************************************************************* */
  public void PromptSaveFile() {// gibberish, under construction
    String JsonTxt = "";
    JFileChooser FileChooser = new JFileChooser();
    FileChooser.setPreferredSize(new Dimension(800, 600));
    FileNameExtensionFilter filter = new FileNameExtensionFilter("JSong files", "jsong");
    FileChooser.setFileFilter(filter);
    int ReturnVal = FileChooser.showSaveDialog(this.drawpanel);
    if (ReturnVal == JFileChooser.APPROVE_OPTION) {
      File SelectedFile = FileChooser.getSelectedFile();
      System.out.println("Selected file: " + SelectedFile.getAbsolutePath());
      String fpath = SelectedFile.getAbsolutePath();
      byte[] encoded = null;
      JsonTxt = this.MyProject.Textify();
      try (PrintWriter out = new PrintWriter(fpath)) {
        out.println(JsonTxt);
      } catch (FileNotFoundException ex) {
        Logger.getLogger(MainGui.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }
  /* ********************************************************************************* */
  public void PromptOpenFile() {// ready for test
    String JsonTxt = "";
    JFileChooser FileChooser = new JFileChooser();
    FileChooser.setPreferredSize(new Dimension(800, 600));
    FileNameExtensionFilter filter = new FileNameExtensionFilter("JSong files", "jsong");
    FileChooser.setFileFilter(filter);
    int ReturnVal = FileChooser.showOpenDialog(this.drawpanel);
    if (ReturnVal == JFileChooser.APPROVE_OPTION) {
      File SelectedFile = FileChooser.getSelectedFile();
      System.out.println("Selected file: " + SelectedFile.getAbsolutePath());
      String fpath = SelectedFile.getAbsolutePath();
      byte[] encoded = null;
      try {
        encoded = Files.readAllBytes(Paths.get(fpath));
        JsonTxt = new String(encoded, StandardCharsets.UTF_8);
        this.MyProject.UnTextify(JsonTxt);
        // to do: force a repaint here
        this.drawpanel.repaint();
      } catch (Exception ex) {
        boolean nop = true;
      }
    }
  }
  boolean KeysEnabled = true;
  /* ********************************************************************************* */
  public void HandleKeys(KeyEvent ke) {
    DrawingPanel dpnl = this.drawpanel;
    System.out.println("keyPressed:" + ke.getKeyCode() + ":" + ke.getExtendedKeyCode() + ":" + ke.getModifiers() + ":" + ke.getKeyChar() + ":" + ke.getModifiersEx());
    char ch = Character.toLowerCase(ke.getKeyChar());
    int keycode = ke.getKeyCode();
    int mod = ke.getModifiers();
    String JsonTxt = "";
    if (KeysEnabled) {
      KeysEnabled = false;// to do: think of a better debouncer
      boolean CtrlPress = ((mod & KeyEvent.CTRL_MASK) != 0);
      if ((keycode == KeyEvent.VK_C) && CtrlPress) {
        dpnl.CopyBranch(dpnl.ScreenMouseX, dpnl.ScreenMouseY);
      } else if (keycode == KeyEvent.VK_DELETE) {
        dpnl.DeleteBranch();
      } else if ((keycode == KeyEvent.VK_Q) && CtrlPress) {
        System.exit(0);
      } else if ((keycode == KeyEvent.VK_S && !CtrlPress)) {
        dpnl.RescaleGroupTimeX();
      } else if ((keycode == KeyEvent.VK_X) && CtrlPress) {
        dpnl.BreakFromHerd();
      } else if ((keycode == KeyEvent.VK_T) && CtrlPress) {
        dpnl.BreakFromHerd_Shallow();
      } else if ((keycode == KeyEvent.VK_E) && CtrlPress) {
        dpnl.Space_Evenly();
      } else if ((keycode == KeyEvent.VK_S) && CtrlPress) {// ctrl S means save
        this.PromptSaveFile();
      } else if ((keycode == KeyEvent.VK_O) && CtrlPress) {// ctrl O means open
        this.PromptOpenFile();
      } else if (keycode == KeyEvent.VK_ESCAPE) {
        if (dpnl.GetFloater() != null) {// to do: delete Floater if not used
          dpnl.SetFloater(null);
          dpnl.HighlightTarget(false);
          dpnl.repaint();
        }
      }
      KeysEnabled = true;
    }
    System.out.println(ke.getID());
  }
  /* ********************************************************************************* */
  public void KeyBindings(JPanel panel) {
    InputMap InMap = panel.getInputMap();
    InMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false), "Next");
    ActionMap ActMap = panel.getActionMap();
    ActMap.put("Next", new NextAction());
  }
  /* ********************************************************************************* */
  public void savefile() {// http://stackoverflow.com/questions/19621105/save-image-from-jpanel-after-draw
    BufferedImage image2 = new BufferedImage(drawpanel.WIDTH, drawpanel.HEIGHT, BufferedImage.TYPE_INT_RGB);
    JFileChooser jFile = new JFileChooser();
    jFile.showSaveDialog(null);
    Path pth = jFile.getSelectedFile().toPath();
    JOptionPane.showMessageDialog(null, pth.toString());
    Graphics2D graphics2D = image2.createGraphics();// BufferedImage getSubimage(int i, int i1, int i2, int i3)
    try {
      ImageIO.write(image2, "", new File(pth.toString()));
    } catch (IOException ox) {
      ox.printStackTrace();
    }
  }
  class NextAction extends AbstractAction {
    public void actionPerformed(ActionEvent actionEvent) {
      System.out.println("Next");
    }
  }
}
