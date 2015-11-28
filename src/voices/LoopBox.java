/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

/**
 *
 * @author MultiTool
 */
public class LoopBox implements ISonglet {
  public double MyDuration = 1.0;// manually assigned duration, as loops are infinite otherwise
  private Project MyProject = null;
  private ISonglet Content = null;
  /* ********************************************************************************* */
  @Override public OffsetBox Spawn_OffsetBox() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  /* ********************************************************************************* */
  @Override public Singer Spawn_Singer() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  /* ********************************************************************************* */
  @Override public int Get_Sample_Count(int SampleRate) {
    return (int) (this.MyDuration * SampleRate);
  }
  /* ********************************************************************************* */
  @Override public double Get_Duration() {
    return this.MyDuration;
  }
  /* ********************************************************************************* */
  @Override public double Update_Durations() {// probably deprecated
    return this.Content.Update_Durations();
  }
  /* ********************************************************************************* */
  @Override public void Update_Guts(MetricsPacket metrics) {
    metrics.MaxDuration = 0;
    this.Content.Update_Guts(metrics);
    metrics.MaxDuration = this.MyDuration;
  }
  /* ********************************************************************************* */
  @Override public void Sort_Me() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  /* ********************************************************************************* */
  @Override public Project Get_Project() {
    return this.MyProject;
  }
  /* ********************************************************************************* */
  @Override public void Set_Project(Project project) {
    this.MyProject = project;
  }
}
