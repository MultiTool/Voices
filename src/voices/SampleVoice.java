package voices;

/**
 *
 * @author MultiTool
 */
public class SampleVoice extends Voice {
  public Wave MySample = null;
  /* ********************************************************************************* */
  public void AttachWaveSample(Wave Sample, double BaseFrequency) {
    MySample = Sample;
    this.BaseFreq = BaseFrequency;
  }
  /* ********************************************************************************* */
//  @Override public double GetWaveForm(double SubTimeAbsolute) {
//    return this.MySample.GetResampleLooped(SubTimeAbsolute * BaseFreq);
//  }
  /* ********************************************************************************* */
  @Override public SampleVoice_Singer Spawn_My_Singer() {// for render time
    // Deliver one of my singers while exposing specific object class. 
    // Handy if my parent's singers know what class I am and want special access to my particular type of singer.
    SampleVoice_Singer ph = new SampleVoice_Singer();
    ph.MyVoice = ph.MySampleVoice = this;
    ph.MyProject = this.MyProject;// inherit project
    ph.BaseFreq = this.BaseFreq;
    ph.MySample = this.MySample;
    return ph;
  }
  /* ********************************************************************************* */
  public static class SampleVoice_Singer extends Voice_Singer {
    public SampleVoice MySampleVoice;
    public Wave MySample = null;
    /* ********************************************************************************* */
    @Override public double GetWaveForm(double SubTimeAbsolute) {
      return this.MySample.GetResampleLooped(SubTimeAbsolute * BaseFreq);
    }
  }
}
