package voices;

import java.util.HashMap;
import static voices.Voice.Voice_OffsetBox.ObjectTypeName;

/**
 *
 * @author MultiTool
 */
public class SampleVoice extends Voice {
  public Wave MySample = null;
  public boolean Looped = true;

  // for serialization
  public static String LoopedName = "LoopedName";
  /* ********************************************************************************* */
  public SampleVoice() {
  }
  /* ********************************************************************************* */
  public void AttachWaveSample(Wave Sample, double BaseFrequency) {
    MySample = Sample;
    this.BaseFreq = BaseFrequency;
  }
  /* ********************************************************************************* */
  @Override public SampleVoice_OffsetBox Spawn_OffsetBox() {// for compose time
    SampleVoice_OffsetBox lbox = new SampleVoice_OffsetBox();// Deliver an OffsetBox specific to this type of phrase.
    lbox.Attach_Songlet(this);
    return lbox;
  }
  /* ********************************************************************************* */
  @Override public SampleVoice_Singer Spawn_Singer() {// for render time
    // Deliver one of my singers while exposing specific object class. 
    // Handy if my parent's singers know what class I am and want special access to my particular type of singer.
    SampleVoice_Singer singer;
    if (this.Looped) {
      singer = new SampleVoice_Looped_Singer();
    } else {
      singer = new SampleVoice_Singer();
    }
    singer.MyVoice = singer.MySampleVoice = this;
    singer.MyProject = this.MyProject;// inherit project
    singer.BaseFreq = this.BaseFreq;
    singer.MySample = this.MySample;
    return singer;
  }
  /* ********************************************************************************* */
  @Override public SampleVoice Clone_Me() {// ICloneable
    SampleVoice child = new SampleVoice();
    child.Copy_From(this);
    return child;
  }
  /* ********************************************************************************* */
  @Override public SampleVoice Deep_Clone_Me(ITextable.CollisionLibrary HitTable) {// ICloneable
    SampleVoice child = new SampleVoice();
    child.Copy_From(this);
    child.Copy_Children(this, HitTable);
    return child;
  }
  /* ********************************************************************************* */
  public void Copy_From(SampleVoice donor) {
    super.Copy_From(donor);
    this.MySample = donor.MySample;// maybe we should clone the sample too? 
  }
  /* ********************************************************************************* */
  @Override public JsonParse.Phrase Export(CollisionLibrary HitTable) {// ITextable
    JsonParse.Phrase phrase = super.Export(HitTable);// to do: export my wave too
    phrase.ChildrenHash = this.SerializeMyContents(HitTable);
    phrase.AddSubPhrase(LoopedName, IFactory.Utils.PackField(this.Looped));
    this.MySample.Export();
    return phrase;
  }
  @Override public void ShallowLoad(JsonParse.Phrase phrase) {// ITextable
    super.ShallowLoad(phrase);
    HashMap<String, JsonParse.Phrase> Fields = phrase.ChildrenHash;
    this.Looped = Boolean.parseBoolean(IFactory.Utils.GetField(Fields, LoopedName, "true"));//Boolean.toString(this.Looped)));
    // Boolean.getBoolean(""); maybe use this instead. simpler, returns false if parse fails. 
  }
  @Override public void Consume(JsonParse.Phrase phrase, CollisionLibrary ExistingInstances) {// ITextable - Fill in all the values of an already-created object, including deep pointers.
    if (phrase == null) {// to do: consume my wave too
      return;
    }
    super.Consume(phrase, ExistingInstances);
    HashMap<String, JsonParse.Phrase> Fields = phrase.ChildrenHash;
  }
  /* ********************************************************************************* */
  public static class SampleVoice_OffsetBox extends Voice_OffsetBox {// location box to transpose in pitch, move in time, etc. 
    public SampleVoice SampleVoiceContent;
    public static String ObjectTypeName = "SampleVoice_OffsetBox";
    /* ********************************************************************************* */
    public SampleVoice_OffsetBox() {
      super();
    }
    /* ********************************************************************************* */
    @Override public Voice GetContent() {
      return VoiceContent;
    }
    /* ********************************************************************************* */
    public void Attach_Songlet(SampleVoice songlet) {// for serialization
      this.SampleVoiceContent = songlet;
      this.VoiceContent = this.SampleVoiceContent = songlet;
      songlet.Ref_Songlet();
    }
    /* ********************************************************************************* */
    @Override public SampleVoice_Singer Spawn_Singer() {// always always always override this
      SampleVoice_Singer ph = this.SampleVoiceContent.Spawn_Singer();
      ph.MyOffsetBox = this;
      return ph;
    }
    /* ********************************************************************************* */
    @Override public SampleVoice_OffsetBox Clone_Me() {// always override this thusly
      SampleVoice_OffsetBox child = new SampleVoice_OffsetBox();
      child.Copy_From(this);
      child.VoiceContent = child.SampleVoiceContent = this.SampleVoiceContent;// iffy, remove?
      return child;
    }
    /* ********************************************************************************* */
    @Override public SampleVoice_OffsetBox Deep_Clone_Me(ITextable.CollisionLibrary HitTable) {// ICloneable
      SampleVoice_OffsetBox child = this.Clone_Me();
      child.VoiceContent = child.SampleVoiceContent = this.SampleVoiceContent.Deep_Clone_Me(HitTable);
      return child;
    }
    /* ********************************************************************************* */
    @Override public void BreakFromHerd(ITextable.CollisionLibrary HitTable) {// for compose time. detach from my songlet and attach to an identical but unlinked songlet
      SampleVoice clone = this.SampleVoiceContent.Deep_Clone_Me(HitTable);
      if (this.SampleVoiceContent.UnRef_Songlet() <= 0) {
        this.SampleVoiceContent.Delete_Me();
      }
      this.SampleVoiceContent = clone;
      this.SampleVoiceContent.Ref_Songlet();
    }
    /* ********************************************************************************* */
    @Override public JsonParse.Phrase Export(CollisionLibrary HitTable) {// ITextable
      JsonParse.Phrase SelfPackage = super.Export(HitTable);// tested
      SelfPackage.AddSubPhrase(Globals.ObjectTypeName, IFactory.Utils.PackField(ObjectTypeName));
      return SelfPackage;
    }
    @Override public void ShallowLoad(JsonParse.Phrase phrase) {// ITextable
      super.ShallowLoad(phrase);
    }
    @Override public void Consume(JsonParse.Phrase phrase, CollisionLibrary ExistingInstances) {// ITextable - Fill in all the values of an already-created object, including deep pointers.
      if (phrase == null) {// ready for test?
        return;
      }
      this.ShallowLoad(phrase);
      JsonParse.Phrase SongletPhrase = phrase.ChildrenHash.get(OffsetBox.ContentName);// value of songlet field
      String ContentTxt = SongletPhrase.Literal;
      SampleVoice songlet;
      if (Globals.IsTxtPtr(ContentTxt)) {// if songlet content is just a pointer into the library
        CollisionItem ci = ExistingInstances.GetItem(ContentTxt);// look up my songlet in the library
        if (ci == null) {// then null reference even in file - the json is corrupt
          throw new RuntimeException("CollisionItem is null in " + ObjectTypeName);
        }
        if ((songlet = (SampleVoice) ci.Item) == null) {// another cast!
          ci.Item = songlet = new SampleVoice();// if not instantiated, create one and save it
          songlet.Consume(ci.JsonPhrase, ExistingInstances);
        }
      } else {
        songlet = new SampleVoice();// songlet is inline, inside this one offsetbox
        songlet.Consume(SongletPhrase, ExistingInstances);
      }
      this.Attach_Songlet(songlet);
    }
    @Override public ISonglet Spawn_And_Attach_Songlet() {// reverse birth, use ONLY for deserialization
      SampleVoice songlet = new SampleVoice();
      this.Attach_Songlet(songlet);
      return songlet;
    }
    /* ********************************************************************************* */
    public static class Factory implements IFactory {// for serialization
      @Override public SampleVoice_OffsetBox Create(JsonParse.Phrase phrase, CollisionLibrary ExistingInstances) {
        SampleVoice_OffsetBox obox = new SampleVoice_OffsetBox();
        obox.Consume(phrase, ExistingInstances);
        return obox;
      }
//      @Override public Voice_OffsetBox Create(JsonParse.Phrase phrase, CollisionLibrary ExistingInstances) {
//        Voice_OffsetBox obox = new Voice_OffsetBox();// hack to load sample voices as voices
//        obox.Consume(phrase, ExistingInstances);
//        return obox;
//      }
    }
  }
  /* ********************************************************************************* */
  public static class SampleVoice_Singer extends Voice_Singer {
    public SampleVoice MySampleVoice;
    public Wave MySample = null;
    /* ********************************************************************************* */
    @Override public double GetWaveForm(double SubTimeAbsolute) {
      return this.MySample.GetResample(SubTimeAbsolute * BaseFreq);
    }
  }
  /* ********************************************************************************* */
  public static class SampleVoice_Looped_Singer extends SampleVoice_Singer {
    /* ********************************************************************************* */
    @Override public double GetWaveForm(double SubTimeAbsolute) {
      return this.MySample.GetResampleLooped(SubTimeAbsolute * BaseFreq);
    }
  }
}
