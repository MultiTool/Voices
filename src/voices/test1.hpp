#ifndef Voice_hpp
#define Voice_hpp

#include <iostream>
#include <sstream>  // Required for stringstreams
#include <string>
#include <vector>
#include "Globals.hpp"
#include "Wave.hpp"
#include "CajaDelimitadora.hpp"


/**
 *
 * @author MultiTool
 */
 class Voice:  ISonglet, IDrawable {
 public:

  // collection of control points, each one having a pitch and a volume. rendering morphs from one cp to another. 
   ArrayList<VoicePoint>* CPoints = new ArrayList<VoicePoint>();
   String* CPointsName = "ControlPoints";// for serialization
   AudProject* MyProject;
   double MaxAmplitude;
   int FreshnessTimeStamp;
   double BaseFreq = Globals.BaseFreqC0;
  double ReverbDelay = 0.125 / 4.0;// delay in seconds
   int RefCount = 0;
  // graphics support
  CajaDelimitadora* MyBounds = new CajaDelimitadora();
  Color* FillColor;
  /* ********************************************************************************* */
   Voice() {}
  /* ********************************************************************************* */
   Voice_OffsetBox* Spawn_OffsetBox() { return null; }
  /* ********************************************************************************* */
   Voice_Singer* Spawn_Singer() { return null; }
  /* ********************************************************************************* */
   void Add_Note(VoicePoint& pnt) {}
  /* ********************************************************************************* */
   VoicePoint* Add_Note(double RealTime, double Octave, double Loudness) { return null; }
  /* ********************************************************************************* */
   void Remove_Note(VoicePoint& pnt) {}
  /* ************************************************************************************************************************ */
   int Tree_Search(double Time, int minloc, int maxloc) { return 0; }
  /* ********************************************************************************* */
   int Get_Sample_Count(int SampleRate) { return 0; }
  /* ********************************************************************************* */
   double Get_Duration() { return 0; }
  /* ********************************************************************************* */
   double Update_Durations() { return 0; }
  /* ********************************************************************************* */
   double Get_Max_Amplitude() { return 0; }
  /* ********************************************************************************* */
   void Update_Max_Amplitude() {}
  /* ********************************************************************************* */
   void Update_Guts(MetricsPacket& metrics) {}
  /* ********************************************************************************* */
   void Sort_Me() {}
  /* ********************************************************************************* */
   AudProject* Get_Project() { return null; }
  /* ********************************************************************************* */
   void Set_Project(AudProject& project) {}
  /* ********************************************************************************* */
   int FreshnessTimeStamp_g() { return 0; }
   void FreshnessTimeStamp_s(int TimeStampNew) {}
  /* ********************************************************************************* */
   void Recalc_Line_SubTime() {}
  /* ********************************************************************************* */
   double Integral(double OctaveRate, double TimeAlong) { return 0; }
  /* ********************************************************************************* */
//  public double GetWaveForm(double SubTimeAbsolute) {
//    return Math.sin(SubTimeAbsolute * this.BaseFreq * Globals.TwoPi);
//  }
  /* ********************************************************************************* */
   void Draw_Me(DrawingContext& ParentDC) {}
  /* ********************************************************************************* */
   CajaDelimitadora* GetBoundingBox() { return null; }
  /* ********************************************************************************* */
   void UpdateBoundingBox() {}
   void UpdateBoundingBoxLocal() {}
  /* ********************************************************************************* */
   void GoFishing(Grabber& Scoop) {}
  /* ********************************************************************************* */
   Voice* Clone_Me() { return null; }
  /* ********************************************************************************* */
   Voice* Deep_Clone_Me(ITextable::CollisionLibrary& HitTable) { return null; }
  /* ********************************************************************************* */
   void Copy_Children(Voice& donor, ITextable::CollisionLibrary& HitTable) {}
  /* ********************************************************************************* */
   void Copy_From(Voice& donor) {}
  /* ********************************************************************************* */
   boolean Create_Me() { return 0; }
   void Delete_Me() {}
   void Wipe_CPoints() {}
  /* ********************************************************************************* */
   int Ref_Songlet() { return 0; }
   int UnRef_Songlet() { return 0; }
   int GetRefCount() { return 0; }
  /* ********************************************************************************* */
   JsonParse::Phrase* Export(CollisionLibrary& HitTable) { return null; }
   void ShallowLoad(JsonParse::Phrase& phrase) {}
   void Consume(JsonParse::Phrase& phrase, CollisionLibrary& ExistingInstances) {}
  /* ********************************************************************************* */
   HashMap<String, JsonParse::Phrase>* SerializeMyContents(CollisionLibrary& HitTable) { return null; }
  /* ********************************************************************************* */
   class Voice_OffsetBox:  OffsetBox {
 public:
// location box to transpose in pitch, move in time, etc. 
     Voice* VoiceContent;
     String* ObjectTypeName = "Voice_OffsetBox";// for serialization
    /* ********************************************************************************* */
     Voice_OffsetBox() {}
    /* ********************************************************************************* */
     Voice* GetContent() { return null; }
    /* ********************************************************************************* */
     void Attach_Songlet(Voice& songlet) {}
    /* ********************************************************************************* */
     Voice_Singer* Spawn_Singer() { return null; }
    /* ********************************************************************************* */
     Voice_OffsetBox* Clone_Me() { return null; }
    /* ********************************************************************************* */
     Voice_OffsetBox* Deep_Clone_Me(ITextable::CollisionLibrary& HitTable) { return null; }
    /* ********************************************************************************* */
     void BreakFromHerd(ITextable::CollisionLibrary& HitTable) {}
    /* ********************************************************************************* */
     boolean Create_Me() { return 0; }
     void Delete_Me() {}
    /* ********************************************************************************* */
     JsonParse::Phrase* Export(CollisionLibrary& HitTable) { return null; }
     void ShallowLoad(JsonParse::Phrase& phrase) {}
     void Consume(JsonParse::Phrase& phrase, CollisionLibrary& ExistingInstances) {}
     ISonglet* Spawn_And_Attach_Songlet() { return null; }
    /* ********************************************************************************* */
     class Factory:  IFactory {
 public:
// for serialization
       Voice_OffsetBox* Create(JsonParse::Phrase& phrase, CollisionLibrary& ExistingInstances) { return null; }
    };
  };
  /* ********************************************************************************* */
   class Voice_Singer:  Singer {
 public:

     Voice* MyVoice;
    double Phase, Cycles;// Cycles is the number of cycles we've rotated since the start of this voice. The fractional part is the phase information. 
    double SubTime;// Subjective time.
    double Current_Octave, Current_Frequency;
    int Prev_Point_Dex, Next_Point_Dex;
    int Render_Sample_Count;
    VoicePoint* Cursor_Point = new VoicePoint();
     int Bone_Sample_Mark = 0;// number of samples since time 0
    double BaseFreq;
    /* ********************************************************************************* */
     Voice_Singer() {}
    /* ********************************************************************************* */
     void Start() {}
    /* ********************************************************************************* */
     void Skip_To(double EndTime) {}
    /* ********************************************************************************* */
     void Render_To(double EndTime, Wave& wave) {}
    /* ********************************************************************************* */
     double GetWaveForm(double SubTimeAbsolute) { return 0; }
    double flywheel = 0.0;
    double drag = 0.9, antidrag = 1.0 - drag;
    /* ********************************************************************************* */
     void Noise_Effect(Wave& wave) {}
    /* ********************************************************************************* */
     void Distortion_Effect(Wave& wave, double gain) {}
    /* ********************************************************************************* */
     void Reverb_Effect(Wave& wave) {}
    /* ********************************************************************************* */
     double ClipTime(double EndTime) { return 0; }
    /* ********************************************************************************* */
     OffsetBox* Get_OffsetBox() { return null; }
    /* ********************************************************************************* */
     void Render_Range(int dex0, int dex1, Wave& wave) {}
    /* ********************************************************************************* */
     void Interpolate_ControlPoint(VoicePoint& pnt0, VoicePoint& pnt1, double RealTime, VoicePoint& PntMid) {}
    /* ********************************************************************************* */
     void Render_Segment_Iterative(VoicePoint& pnt0, VoicePoint& pnt1, Wave& wave) {}
    /* ********************************************************************************* */
     void Render_Segment_Integral(VoicePoint& pnt0, VoicePoint& pnt1, Wave& wave) {}
    /* ********************************************************************************* */
     boolean Create_Me() { return 0; }
     void Delete_Me() {}
  };
};


#endif
