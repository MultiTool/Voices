/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;

/**
 *
 * @author MultiTool
 */
public class Splines {
  /* ********************************************************************************* */
  public static class PointX extends Point.Double {
    //public double x, y;
    public PointX() {

    }
    public PointX(PointX donor) {
      this.CopyFrom(donor);
    }
    public PointX(double XLoc, double YLoc) {
      this.x = XLoc;
      this.y = YLoc;
    }
    public void Assign(double XLoc, double YLoc) {
      this.x = XLoc;
      this.y = YLoc;
    }
    public final void CopyFrom(PointX donor) {
      this.x = donor.x;
      this.y = donor.y;
    }
    public void Add(PointX other) {
      this.x += other.x;
      this.y += other.y;
    }
    public void Subtract(PointX other) {
      this.x -= other.x;
      this.y -= other.y;
    }
  }
  /* ********************************************************************************* */
  public static class Line {
    PointX[] pnt = new PointX[2];
    PointX delta = new PointX(); // delta X, delta Y
    public Line() {
    }
    public Line(PointX p0, PointX p1) {
      this.pnt[0] = new PointX(p0);
      this.pnt[1] = new PointX(p1);
      this.CompDelta();
    }
    public void Assign(double X0, double Y0, double X1, double Y1) {
      this.pnt[0] = new PointX(X0, Y0);
      this.pnt[1] = new PointX(X1, Y1);
      this.CompDelta();
    }
    public void Assign(PointX p0, PointX p1) {
      this.pnt[0] = new PointX(p0);
      this.pnt[1] = new PointX(p1);
      this.CompDelta();
    }
    public void CompDelta() {
      this.delta = new PointX(this.pnt[1].x - this.pnt[0].x, this.pnt[1].y - this.pnt[0].y);
    }
    public void FractAlong(double Fract, PointX results) {
      double dx = this.delta.x * Fract;
      double dy = this.delta.y * Fract;
      results.x = this.pnt[0].x + dx;
      results.y = this.pnt[0].y + dy;
      //System.out.println("Fract:" + Fract + " dx:" + this.delta.x + " dy:" + this.delta.y);
      //System.out.println("Fract:" + Fract + " dx:" + dx + " dy:" + dy);
    }
  }
  /* ********************************************************************************* */
  public static void Cheap_Spline(Graphics2D gr, PointX p0, PointX ctr, PointX p1, PointX[] results) {// cheap spline, cross spline, cross curve
    PointX p0prime = new PointX(ctr), p1prime = new PointX(ctr);
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
    PointX results0 = new PointX(), results1 = new PointX(), resultsmeta = new PointX();
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
  public static void Test(Graphics2D gr) {
    double Factor = 150;
    PointX p0 = new PointX(1 * Factor, 1 * Factor), ctr = new PointX(2 * Factor, 2 * Factor), p1 = new PointX(3 * Factor, 0.5 * Factor);
    gr.setColor(Globals.ToColorWheel(Globals.RandomGenerator.nextDouble()));
    gr.drawLine((int) p0.x, (int) p0.y, (int) ctr.x, (int) ctr.y);
    gr.drawLine((int) ctr.x, (int) ctr.y, (int) p1.x, (int) p1.y);

    int len = 256;
    PointX[] results = new PointX[len];
    for (int cnt = 0; cnt < len; cnt++) {
      results[cnt] = new PointX();
    }
    Cheap_Spline(gr, p0, ctr, p1, results);
    PointX prevpnt = p0, pnt = p0;
    gr.setColor(Color.red);
    for (int cnt = 0; cnt < len; cnt++) {
      prevpnt = pnt;
      pnt = results[cnt];
      gr.setColor(Globals.ToColorWheel(Globals.RandomGenerator.nextDouble()));
      gr.drawLine((int) prevpnt.x, (int) prevpnt.y, (int) pnt.x, (int) pnt.y);
    }
    pnt = p1;
    gr.setColor(Globals.ToColorWheel(Globals.RandomGenerator.nextDouble()));
    gr.drawLine((int) prevpnt.x, (int) prevpnt.y, (int) pnt.x, (int) pnt.y);
  }
}
/*
real cubic spline:
B(t) = (1-t)^3*P0  +  3*(1-t)^2*P1  +  3*(1-t)*t^2*P2  +  t^3*P3 

quadratic:
B(t) = (1-t)^2*P0  +  2*(1-t)*t*P1  +  t^2*P2 

*/


