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
public interface ISonglet {// Cancionita
  /* ********************************************************************************* */
  public static class Singer {//extends CoordBox {
    public Project MyProject;
    double Inherited_Octave = 0.0, Inherited_OctaveRate = 0.0, Inherited_Loudness;// octave, bend and loudness context
    public boolean IsFinished = false;
    public Singer ParentSinger;
    /* ********************************************************************************* */
    public void Start() {
      IsFinished = false;
    }
    /* ********************************************************************************* */
    public void Skip_To(double EndTime) {
    }
    /* ********************************************************************************* */
    public void Render_To(double EndTime, Wave wave) {
    }
  }
  /* ********************************************************************************* */
  public CoordBox Spawn_CoordBox();// for compose time
  /* ********************************************************************************* */
  public ISonglet.Singer Spawn_Player();// for render time
  /* ********************************************************************************* */
  public int Get_Sample_Count(int SampleRate);
  /* ********************************************************************************* */
  public double Get_Duration();
  /* ********************************************************************************* */
  public double Update_Durations();
  /* ********************************************************************************* */
  public void Sort_Me();
  /* ********************************************************************************* */
  public Project Get_Project();
  /* ********************************************************************************* */
  public void Set_Project(Project project);
}
