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
  @Override public double GetWaveForm(double SubTimeAbsolute) {
    return this.MySample.GetResampleLooped(SubTimeAbsolute * BaseFreq);
  }
}
