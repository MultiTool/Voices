/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

import voices.VoiceBase.Point;

/**
 *
 * @author MultiTool
 */
public class Voices {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    // TODO code application logic here
  }
  public static void Circus(double Time) {
    double Cycles = 0.0;
    //double StartPhase;
    Point pnt0 = new Point();
    pnt0.RealTime = 0;
    pnt0.Octave = 2;
    pnt0.Loudness = 1;
    Point pnt1 = new Point();
    pnt1.RealTime = 1;// 1 second later? 
    pnt1.Octave = 3;
    pnt1.Loudness = 1;

    // C0 16.35 
    // A0 27.50 
    double TimeRange = pnt1.RealTime - pnt0.RealTime;
    double TimeAlong = Time = pnt0.RealTime;
    double FractAlong = TimeAlong / TimeRange;

    double PitchRange = pnt1.Octave - pnt0.Octave;
    double CurrentPitch = pnt0.Octave + (PitchRange * FractAlong);
    double LoudnessRange = pnt1.Loudness - pnt0.Loudness;
    double CurrentLoudness = pnt0.Loudness + (LoudnessRange * FractAlong);

    double freq = VoiceBase.BaseFreqC0 * Math.pow(2.0, pnt0.Octave);
    double Amplitude;

    double integrate = Math.pow(2.0, pnt0.Octave) / Math.log(2);// integral without range
    double Integral_Ranged = (Math.pow(2.0, pnt0.Octave) - 1.0) / Math.log(2);// integral with range from 0 to pnt0

    // do all from end to end? or start in the middle and hope the phase was done correctly?
    // end to end for starters
    // we need samples per second, what are our time units, 
    double SamplesPerSecond = 44100.0;
    double SecondsPerSample = 1.0 / SamplesPerSecond;
    // Freq is in hertz, must convert cycles to angles for sine 

    double Radians = Cycles * Math.PI * 2.0;
    // the most important thing in the world is to bend notes, and then bend them some more to other notes.
    double RealTime, SubTime;
    double OctavePrev = 1, OctaveNext = 2, OctaveRange, OctaveNow;
    OctaveRange = OctaveNext - OctavePrev;
    RealTime = 0.0;
    int NumSamples = 100;
    double Freq = 60.0; // hz
    for (int tcnt = 0; tcnt < NumSamples; tcnt++) {
      FractAlong = ((double) tcnt) / (double) NumSamples;
      RealTime = FractAlong;// so 0 to 1 seconds
      OctaveNow = OctavePrev + (OctaveRange * FractAlong);
      SubTime = (Math.pow(2.0, OctaveNow) - 1.0) / Math.log(2);// integral with range from 0 to pnt0
      Cycles = SubTime * Freq;
      Radians = Cycles * Math.PI * 2.0;
      Amplitude = Math.sin(Radians);
    }

    /*
     so we don't really need the integral of frequency to get current cycle and phase. 
     we can just count them over the number of samples (slower but works for general case?)
     are we really counting number of cycles, or more like subjective time, as time compresses?
    
     so integral with range will be (2^endval)-1)/ln(2);
     ln(2) = 0.69314718 = 0.69314718055994530941723212145818
    
    http://rtcmix.org/
    
    http://quickmath.com/webMathematica3/quickmath/calculus/integrate/advanced.jsp#c=integrate_advancedintegrate&v1=t*%282^%28t*z%29%29&v2=t&v3=0&v4=7

t*(2^(t*z))  steady octave change. t is real time, z is octave change rate assuming t starts at 0.
(log(2)*z-1)*%e^(log(2)*z)/(log(2)^2*z^2)+1/(log(2)^2*z^2)  range 0 to 1
(2*log(2)*z-1)*%e^(2*log(2)*z)/(log(2)^2*z^2)+1/(log(2)^2*z^2)  0 to 2
(3*log(2)*z-1)*%e^(3*log(2)*z)/(log(2)^2*z^2)+1/(log(2)^2*z^2)  to 3
(4*log(2)*z-1)*%e^(4*log(2)*z)/(log(2)^2*z^2)+1/(log(2)^2*z^2)  to 4
(5*log(2)*z-1)*%e^(5*log(2)*z)/(log(2)^2*z^2)+1/(log(2)^2*z^2)  to 5

(7*log(2)*z-1)*%e^(7*log(2)*z)/(log(2)^2*z^2)+1/(log(2)^2*z^2)  to 7

%e seems to be e=2.718281828459045

t*((t*z))  steady frequency change.
     */
  }
}
