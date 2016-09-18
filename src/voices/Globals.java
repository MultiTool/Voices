package voices;

import java.awt.Color;
import java.util.HashMap;
import java.util.Random;

/**
 *
 * @author MultiTool
 */
public class Globals {
  public static int SampleRate = 44100;
  public static int SampleRateTest = 100;
  public static double BaseFreqC0 = 16.3516;// hz
  public static double BaseFreqA0 = 27.5000;// hz
  public static double MiddleC4Freq = 261.626;// hz
  public static double TwoPi = Math.PI * 2.0;// hz
  public static double Fudge = 0.00000000001;
  public static Random RandomGenerator = new Random();
  public static String PtrPrefix = "ptr:";// for serialization
  public static String ObjectTypeName = "ObjectTypeName";// for serialization
  public static HashMap<String, ITextable.IFactory> FactoryLUT = new HashMap<String, ITextable.IFactory>();// for serialization
  /* ********************************************************************************* */
  public static boolean IsTxtPtr(String ContentTxt) {// for serialization
    if (ContentTxt == null) {
      return false;
    }
    int strloc;
    return ((strloc = ContentTxt.indexOf(Globals.PtrPrefix)) >= 0);
  }
  /* ********************************************************************************* */
  public static Color ToAlpha(Color col, int Alpha) {
    return new Color(col.getRed(), col.getGreen(), col.getBlue(), Alpha);// rgba 
  }
  /* ********************************************************************************* */
  public static Color ToRainbow(double Fraction) {
    if (Fraction < 0.5) {
      Fraction *= 2;
      return new Color((float) (1.0 - Fraction), (float) Fraction, 0);
    } else {
      Fraction = Math.min((Fraction - 0.5) * 2, 1.0);
      return new Color(0, (float) (1.0 - Fraction), (float) Fraction);
    }
  }
  /* ********************************************************************************* */
  public static Color ToColorWheel(double Fraction) {
    Fraction = Fraction - Math.floor(Fraction); // remove whole number part if any
    if (Fraction < (1.0 / 3.0)) {// red to green
      Fraction *= 6.0;
      return new Color((float) Math.min(2.0 - Fraction, 1.0), (float) Math.min(Fraction, 1.0), 0);
    } else if (Fraction < (2.0 / 3.0)) {// green to blue
      Fraction = (Fraction - (1.0 / 3.0)) * 6.0;
      return new Color(0, (float) Math.min(2.0 - Fraction, 1.0), (float) Math.min(Fraction, 1.0));
    } else {// blue to red
      Fraction = (Fraction - (2.0 / 3.0)) * 6.0;
      return new Color((float) Math.min(2.0 - Fraction, 1.0), 0, (float) Math.min(Fraction, 1.0));
    }
  }
}
/*
// C++ stuff

#ifndef Globals_hpp
#define Globals_hpp

#define _USE_MATH_DEFINES 
#include <cmath>
#include <algorithm>    // std::min
#include <stdlib.h>     // srand, rand 
#include <cstdlib> // RAND_MAX
#include <vector>
#include <map>

#define boolean bool 
#define jpublic 
#define jprivate
#define implements :
#define interface class
#define extends :
#define Graphics2D (void*)
#define Object void*
#define null nullptr
#define String std::string
#define ArrayList std::vector
//#define HashMap std::unordered_map
#define HashMap std::map

#define Math_min(a, b) (std::min(a, b))
#define Math_max(a, b) (std::max(a, b))
#define Math_ceil(a) (std::ceil(a))
#define Math_abs(a) (std::abs(a))
#define Math_hypot(a, b) (std::hypot(a, b))

#define Double_POSITIVE_INFINITY DBL_MAX
#define Double_NEGATIVE_INFINITY DBL_MIN

class Math {
public:
  static double min(double a, double b) { return std::min(a, b); }
  static double max(double a, double b) {return std::max(a, b);}
  static double ceil(double a) {return std::ceil(a);}
  static double abs(double a) {return std::abs(a);}
  static double hypot(double a, double b) {return std::hypot(a, b);}
};

class Color {
public:
	Color() {};
	Color(double R, double G, double B) {};
	Color(double R, double G, double B, double A) {};
	double getRed() {};
	double getGreen() {};
	double getBlue() {};
};

struct Example {
	static double usPerSec() { return 1000000.0; }
};

class Globals {
public:
	const static int SampleRate = 44100;
	//const int SampleRate = 44100;
	const static int SampleRateTest = 100;
	//static const double BaseFreqC0 = 16.3516;// hz
	static const double BaseFreqA0;// = 27.5000;// hz
	static const double BaseFreqC0;// hz
	//const double BaseFreqC0 = 16.3516;// hz
	//static double BaseFreqA0;// = 27.5000;// hz
	//constexpr static double usPerSec = 1000000.0;
	// http://stackoverflow.com/questions/2777541/static-const-double-in-c
	static double BaseFreqA0_G() { return 27.5000; }// hz
	static double BaseFreqC0_G() { return 16.3516; }// hz

	const long double Math_PI = 3.141592653589793238L;
	const long double TwoPi = Math_PI * 2; //M_PI_2;// Math.PI * 2.0;// hz
	const double Fudge = 0.00000000001;
	class Random { double NextDouble() { return ((double)rand()) / (double)RAND_MAX; } };
	const static Random RandomGenerator();// = new Random();
	//* ********************************************************************************* *
	static Color* ToAlpha(Color& col, int Alpha) {
		//return new Color();
		return new Color(col.getRed(), col.getGreen(), col.getBlue(), Alpha);// rgba 
	}
	//* ********************************************************************************* *
	static Color* ToRainbow(double Fraction) {
		if (Fraction < 0.5) {
			Fraction *= 2;
			return new Color((1.0 - Fraction), Fraction, 0);
		} else {
			Fraction = Math_min((Fraction - 0.5) * 2, 1.0);
			return new Color(0, (1.0 - Fraction), Fraction);
		}
	}
};

#endif

*/