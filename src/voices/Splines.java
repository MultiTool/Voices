/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 *
 * @author MultiTool
 */
public class Splines {
  static final double Sin90 = Math.sin(Math.PI / 2.0);// 90 degrees
  static final double Cos90 = Math.cos(Math.PI / 2.0);// 90 degrees
  static final double Sin270 = Math.sin(Math.PI * 3.0 / 2.0);// 270 degrees
  static final double Cos270 = Math.cos(Math.PI * 3.0 / 2.0);// 270 degrees
  /* ********************************************************************************* */
  public static class Line {
    Globals.PointX[] pnt = new Globals.PointX[2];
    Globals.PointX delta = new Globals.PointX(); // delta X, delta Y
    public Line() {
    }
    public Line(Globals.PointX p0, Globals.PointX p1) {
      this.pnt[0] = new Globals.PointX(p0);
      this.pnt[1] = new Globals.PointX(p1);
      this.CompDelta();
    }
    public void Assign(double X0, double Y0, double X1, double Y1) {
      this.pnt[0] = new Globals.PointX(X0, Y0);
      this.pnt[1] = new Globals.PointX(X1, Y1);
      this.CompDelta();
    }
    public void Assign(Globals.PointX p0, Globals.PointX p1) {
      this.pnt[0] = new Globals.PointX(p0);
      this.pnt[1] = new Globals.PointX(p1);
      this.CompDelta();
    }
    public void CompDelta() {
      this.delta = new Globals.PointX(this.pnt[1].x - this.pnt[0].x, this.pnt[1].y - this.pnt[0].y);
    }
    public void FractAlong(double Fract, Globals.PointX results) {
      double dx = this.delta.x * Fract;
      double dy = this.delta.y * Fract;
      results.x = this.pnt[0].x + dx;
      results.y = this.pnt[0].y + dy;
      //System.out.println("Fract:" + Fract + " dx:" + this.delta.x + " dy:" + this.delta.y);
      //System.out.println("Fract:" + Fract + " dx:" + dx + " dy:" + dy);
    }
  }
  /* ********************************************************************************* */
  public static void Cheap_Spline(Graphics2D gr, Globals.PointX p0, Globals.PointX ctr, Globals.PointX p1, Globals.PointX[] results) {// cheap spline, cross spline, cross curve
    Globals.PointX p0prime = new Globals.PointX(ctr), p1prime = new Globals.PointX(ctr);
    //p0prime.CopyFrom(ctr);
    p0prime.Subtract(p0);
    p0prime.Add(ctr);
    //p1prime.CopyFrom(ctr);
    p1prime.Subtract(p1);
    p1prime.Add(ctr);
    Line lcross0 = new Line(p0, p1prime), lcross1 = new Line(p0prime, p1);

//    gr.setColor(Globals.ToColorWheel(Globals.RandomGenerator.nextDouble()));
//    gr.drawLine((int) lcross0.pnt[0].x, (int) lcross0.pnt[0].y, (int) lcross0.pnt[1].x, (int) lcross0.pnt[1].y);
//
//    gr.setColor(Globals.ToColorWheel(Globals.RandomGenerator.nextDouble()));
//    gr.drawLine((int) lcross1.pnt[0].x, (int) lcross1.pnt[0].y, (int) lcross1.pnt[1].x, (int) lcross1.pnt[1].y);
    //lcross0.Assign(p0, p1prime); lcross1.Assign(p0prime, p1);
    Globals.PointX results0 = new Globals.PointX(), results1 = new Globals.PointX(), resultsmeta = new Globals.PointX();
    Line crossmeta = new Line();
    int len = results.length;// 6;
    for (int cnt = 0; cnt < len; cnt++) {
      double Fract = ((double) cnt) / (double) len;
      System.out.println("lcross");
      lcross0.FractAlong(Fract, results0);
      lcross1.FractAlong(Fract, results1);

      //System.out.println("Fract:" + Fract + " results0 x0:" + results0.x + " y0:" + results0.y + " x1:" + results1.x + " y1:" + results1.y);
      crossmeta.Assign(results0, results1);
      System.out.println("crossmeta");
      crossmeta.FractAlong(Fract, resultsmeta);

//      gr.setColor(Globals.ToColorWheel(Globals.RandomGenerator.nextDouble()));
//      gr.drawLine((int) crossmeta.pnt[0].x, (int) crossmeta.pnt[0].y, (int) crossmeta.pnt[1].x, (int) crossmeta.pnt[1].y);
      results[cnt].CopyFrom(resultsmeta);// resultsmeta is the point we want to plot. 
      //System.out.println("Fract:" + Fract + " crossmeta x0:" + crossmeta.pnt[0].x + " y0:" + crossmeta.pnt[0].y + " x1:" + crossmeta.pnt[1].x + " y1:" + crossmeta.pnt[1].y);
    }
  }
  /* ********************************************************************************* */
  public static void Cubic_Spline_Chunk(Globals.PointX p0, Globals.PointX p1, Globals.PointX p2, Globals.PointX p3, double FractAlong, Globals.PointX result) {
    // B(t) = (1-t)^3*P0  +  3*(1-t)^2*t*P1  +  3*(1-t)*t^2*P2  +  t^3*P3 
    double MinusFract = 1.0 - FractAlong;
    double Wgt0 = Math.pow(MinusFract, 3);// * P0;
    double Wgt1 = 3.0 * FractAlong * Math.pow(MinusFract, 2);// * P1;
    double Wgt2 = 3.0 * MinusFract * Math.pow(FractAlong, 2);// * P2;
    double Wgt3 = Math.pow(FractAlong, 3);// * P3;

    double XLoc = (p0.x * Wgt0) + (p1.x * Wgt1) + (p2.x * Wgt2) + (p3.x * Wgt3);
    double YLoc = (p0.y * Wgt0) + (p1.y * Wgt1) + (p2.y * Wgt2) + (p3.y * Wgt3);
    result.Assign(XLoc, YLoc);
  }
  /* ********************************************************************************* */
  public static void Create_Control_Point(Globals.PointX Prev, Globals.PointX CornerPnt, Globals.PointX Next, Globals.PointX CtrlPnt) {
    Globals.PointX before = new Globals.PointX(Prev), after = new Globals.PointX(Next);
    before.Subtract(CornerPnt);
    before.Normalize();
    after.Subtract(CornerPnt);
    after.Normalize();
    before.Add(after);// this now divides the angle between the two lines
    before.Normalize();// this gives us normalized output
    // CtrlPnt.CopyFrom(before);
    double x0, y0, sin, cos;
    double d = (after.x * before.y) - (after.y * before.x); // winding order: d=(x-x1)*(y2-y1)-(y-y1)*(x2-x1)
    if (d > 0) {
      sin = Sin90;// 90 degrees
      cos = Cos90;
    } else {
      sin = Sin270;// 270 degrees
      cos = Cos270;
    }
    x0 = (before.x * cos) - (before.y * sin);// precalculated all to constants
    y0 = (before.y * cos) + (before.x * sin);
    CtrlPnt.Assign(x0, y0); // result is one point, 1 unit long, 'tangent' to the corner. 
    /*
     position = sign((Bx - Ax) * (Y - Ay) - (By - Ay) * (X - Ax))// alternative: cross product
     */
  }
  /* ********************************************************************************* */
  public static void Cubic_Spline_Boxes(ArrayList<OffsetBox> raw, int NumSubLines, Globals.PointX[] SplinePoints) {// Splines that only go forward in time
    double CtrlPntLength = 1.0 / 2.0;
    Globals.PointX Prev = new Globals.PointX(), Next = new Globals.PointX(0, 0);
    Globals.PointX CtrlPrev = new Globals.PointX(), CtrlNext = new Globals.PointX();
    Globals.PointX result = new Globals.PointX();
    int len = raw.size();
    double FractAlong;
    double XDist;
    OffsetBox NowBox;

    int pcnt = 0;
    int rescnt = 0;
    while (pcnt < len) {
      Prev.CopyFrom(Next);// rollover
      NowBox = raw.get(pcnt);
      NowBox.CopyTo_PointX(Next);
      XDist = Next.x - Prev.x;

      CtrlPrev.CopyFrom(Prev);
      CtrlPrev.x += (XDist * CtrlPntLength);

      CtrlNext.CopyFrom(Next);
      CtrlNext.x -= (XDist * CtrlPntLength);

      for (int subdex = 0; subdex < NumSubLines; subdex++) {
        FractAlong = ((double) subdex) / (double) NumSubLines;
        Cubic_Spline_Chunk(Prev, CtrlPrev, CtrlNext, Next, FractAlong, result);
        SplinePoints[rescnt].CopyFrom(result);
        rescnt++;
      }
      pcnt++;
    }
    SplinePoints[rescnt].CopyFrom(Next);// pin last segment to final point
  }
  /* ********************************************************************************* */
  public static void Cubic_Spline(Graphics2D gr, Globals.PointX[] raw, int NumSubLines, Globals.PointX[] results) {
    Globals.PointX p0, p1, p2, p3;// p0 = raw[cnt + 0]; p1 = raw[cnt + 1]; p2 = raw[cnt + 2]; p3 = raw[cnt + 3];
    Globals.PointX Prev, CornerPnt, Next, cpleft, cpright, CtrlPrev = new Globals.PointX(), CtrlNext = new Globals.PointX();
    Globals.PointX CtrlPrev2 = new Globals.PointX(), CtrlNext2 = new Globals.PointX();
    double PrevLen, NextLen;
    cpleft = new Globals.PointX();
    cpright = new Globals.PointX();
    double FractAlong;
    Globals.PointX result = new Globals.PointX();
    double CtrlPntLength = 1.0 / 3.0;
    //int NumSubLines = 50;
    int len = raw.length;

    int pcnt = 0;
    CornerPnt = Next = raw[pcnt++];

    Prev = CornerPnt;// rollover
    CornerPnt = Next;
    Next = raw[pcnt++];
    NextLen = Next.distance(CornerPnt) * CtrlPntLength;

    Stroke OldStroke = gr.getStroke();
    BasicStroke OutlineStroke;
    float InnerLineThickness = 3.0f;
    OutlineStroke = new BasicStroke(InnerLineThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    gr.setStroke(OutlineStroke);// draw outline or glow of spine
    int rescnt = 0;
    while (pcnt < len) {
      PrevLen = NextLen;
      CtrlPrev.CopyFrom(CtrlNext);// rollover control points
      CtrlPrev.Multiply(-1.0);// 180 degree turn
      CtrlPrev2.CopyFrom(CtrlPrev);// this is all really unnecessary, will just modify CtrlPrev
      CtrlPrev2.Multiply(PrevLen);// make this 1/3 the length of the current line
      CtrlPrev2.Add(CornerPnt);// offset

      Prev = CornerPnt;// rollover
      CornerPnt = Next;
      Next = raw[pcnt];

      NextLen = Next.distance(CornerPnt) * CtrlPntLength;
      Create_Control_Point(Prev, CornerPnt, Next, CtrlNext);
      CtrlNext2.CopyFrom(CtrlNext);
      CtrlNext2.Multiply(NextLen);// to do: make this 1/3 the length of the current line
      CtrlNext2.Add(CornerPnt);// offset

      gr.setColor(Color.green);
      gr.fillOval((int) CtrlPrev2.x - 5, (int) CtrlPrev2.y - 5, 10, 10);
      gr.drawLine((int) Prev.x, (int) Prev.y, (int) CtrlPrev2.x, (int) CtrlPrev2.y);

      gr.setColor(Color.red);
      gr.fillOval((int) CtrlNext2.x - 5, (int) CtrlNext2.y - 5, 10, 10);
      gr.drawLine((int) CornerPnt.x, (int) CornerPnt.y, (int) CtrlNext2.x, (int) CtrlNext2.y);
      gr.setColor(Color.blue);

      for (int subdex = 0; subdex < NumSubLines; subdex++) {
        FractAlong = ((double) subdex) / (double) NumSubLines;
        Cubic_Spline_Chunk(Prev, CtrlPrev2, CtrlNext2, CornerPnt, FractAlong, result);
//        gr.setColor(Globals.ToColorWheel(Globals.RandomGenerator.nextDouble()));
//        gr.fillOval((int) result.x, (int) result.y, 4, 4);
        results[rescnt].CopyFrom(result);
        rescnt++;
      }
      pcnt++;
    }
    if (true) {// under construction: interpolate final line segment
      PrevLen = NextLen;
      CtrlPrev.CopyFrom(CtrlNext);// rollover control points
      CtrlPrev.Multiply(-1.0);// 180 degree turn
      CtrlPrev2.CopyFrom(CtrlPrev);// this is all really unnecessary, will just modify CtrlPrev
      CtrlPrev2.Multiply(PrevLen);// make this 1/3 the length of the current line
      CtrlPrev2.Add(CornerPnt);// offset

      Prev = CornerPnt;// rollover
      CtrlNext2 = CornerPnt = Next;
      for (int subdex = 0; subdex < NumSubLines; subdex++) {
        FractAlong = ((double) subdex) / (double) NumSubLines;
        Cubic_Spline_Chunk(Prev, CtrlPrev2, CtrlNext2, CornerPnt, FractAlong, result);
//        gr.setColor(Globals.ToColorWheel(Globals.RandomGenerator.nextDouble()));
//        gr.fillOval((int) result.x, (int) result.y, 4, 4);
        results[rescnt].CopyFrom(result);
        rescnt++;
      }
      results[rescnt].CopyFrom(Next);
    }
    gr.setStroke(OldStroke);// draw outline or glow of spine
  }
  /* ********************************************************************************* */
  public static int Interpolate_Segment(Globals.PointX Prev, Globals.PointX CtrlPrev, Globals.PointX CtrlNext, Globals.PointX Next, int NumSubLines, Globals.PointX[] results, int resultcnt) {
    Globals.PointX result = new Globals.PointX();
    double FractAlong;
    for (int subdex = 0; subdex < NumSubLines; subdex++) {
      FractAlong = ((double) subdex) / (double) NumSubLines;
      Cubic_Spline_Chunk(Prev, CtrlPrev, CtrlNext, Next, FractAlong, result);
      results[resultcnt].CopyFrom(result);
      resultcnt++;
    }
    return resultcnt;
  }
  /* ********************************************************************************* */
  public static void Test(Graphics2D gr) {
    double Factor = 150;
    Globals.PointX p0 = new Globals.PointX(1 * Factor, 1 * Factor), ctr = new Globals.PointX(2 * Factor, 2 * Factor), p1 = new Globals.PointX(3 * Factor, 0.5 * Factor);

    int len = 256;
    Globals.PointX[] results = new Globals.PointX[len];
    for (int cnt = 0; cnt < len; cnt++) {
      results[cnt] = new Globals.PointX();
    }
    Cheap_Spline(gr, p0, ctr, p1, results);

    Globals.PointX[] raw = new Globals.PointX[10];

    p0.Assign(0, 0);
    Globals.PointX prevpnt = p0, pnt = p0;
    for (int cnt = 0; cnt < raw.length; cnt++) {
      prevpnt = pnt;
      pnt = new Globals.PointX(60 + cnt * 80, 200 + (cnt % 2) * 80 + (Globals.RandomGenerator.nextDouble() * 100));
      raw[cnt] = pnt;
      gr.setColor(Globals.ToColorWheel(Globals.RandomGenerator.nextDouble()));
      gr.drawLine((int) prevpnt.x, (int) prevpnt.y, (int) pnt.x, (int) pnt.y);
    }
    int NumSubLines = 8;
    results = new Globals.PointX[NumSubLines * (raw.length - 1) + 1];
    for (int pcnt = 0; pcnt < results.length; pcnt++) {
      results[pcnt] = new Globals.PointX();
    }
    Cubic_Spline(gr, raw, NumSubLines, results);
    if (true) {
      pnt = results[0];
      len = results.length;
      gr.setColor(Color.red);
      for (int cnt = 1; cnt < len; cnt++) {
        prevpnt = pnt;
        pnt = results[cnt];
//        gr.setColor(Globals.ToColorWheel(Globals.RandomGenerator.nextDouble()));
        gr.drawLine((int) prevpnt.x, (int) prevpnt.y, (int) pnt.x, (int) pnt.y);
      }
    }
  }
}
/*
 real cubic spline:
 B(t) = (1-t)^3*P0  +  3*(1-t)^2*P1  +  3*(1-t)*t^2*P2  +  t^3*P3 

 quadratic:
 B(t) = (1-t)^2*P0  +  2*(1-t)*t*P1  +  t^2*P2 

 */
