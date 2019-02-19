package voices;

// WIP

import java.util.HashMap;
import voices.ITextable.IFactory;
import voices.ITextable.IFactory.Utils;

// FileMapper is part of deserialization.  It will convert a parsed json tree onto a playable Vectunes tree.
// Every time we upgrade our file format, we will create a new FileMapper to support it, and keep the old FileMapper around for backward compatibility.
// At the moment this FileMapper is too integrated with the songlet classes to be reused for new formats.  But, it will work if outright replaced. 
public class FileMapper {
  static final String TreePhraseName = "Tree", LibraryPhraseName = "Library";// for serialization
  /* ********************************************************************************* */
  public static class FactoryBase {
    FileMapper fmap;
    FactoryBase(FileMapper fmap0){ this.fmap = fmap0; }
    // /* virtual */ ~FactoryBase(){}
    /* virtual */ OffsetBox Deserialize_OffsetBox(JsonParse.HashNode OboxNode) { return null; };
    /* virtual */ ISonglet Deserialize_Songlet(JsonParse.HashNode node) { return null; };
    /* ********************************************************************************* */
    public OffsetBox Deserialize_Stack(JsonParse.HashNode OboxNode) {// Create and fill in a songlet and its obox, including deep pointers.
      JsonParse.Node SongletNode = OboxNode.Get(OffsetBox.ContentName);// value of songlet field
      OffsetBox obox = null;
      ISonglet songlet;
      if (SongletNode.MyType == JsonParse.Node.Types.IsLiteral){
        JsonParse.LiteralNode LinkNode = (JsonParse.LiteralNode)SongletNode; // another cast!
        String ContentTxt = LinkNode.Get();
        if (Globals.IsTxtPtr(ContentTxt)) {// if songlet content is just a pointer into the library
          ITextable.CollisionItem citem = fmap.ExistingInstances.GetItem(ContentTxt);// look up my songlet in the library
          if (citem != null){
            if ((songlet = (ISonglet)citem.Item) == null) {// another cast!
              citem.Item = songlet = this.Deserialize_Songlet(citem.JsonPhrase);
            }
          } else {
            return null; // Error!  content is a text link but has no item in library table.
          }
        } else {
          return null; // Error! content is text but not a pointer
        }
      } else if (SongletNode.MyType == JsonParse.Node.Types.IsHash){// songlet is inline, inside this one offsetbox
        JsonParse.HashNode ObjectNode = (JsonParse.HashNode)SongletNode; // another cast!
        songlet = this.Deserialize_Songlet(ObjectNode);
      } else {
        return null; // Error! content is neither link nor hash object
      }
      obox = songlet.Spawn_OffsetBox();
      //obox.ShallowLoad(OboxNode);
      // Custom-filling the obox is a problem if we ever want to do it from FileMapper. Will need a /* virtual */ fn with a cast.
      return obox;
    } //or Get_Songlet_Node?
  };
  /* ********************************************************************************* */
  public static class VoiceFactory extends FactoryBase{
    VoiceFactory(FileMapper fmap0)  {super(fmap0);}
    /* ********************************************************************************* */
    @Override Voice.Voice_OffsetBox Deserialize_OffsetBox(JsonParse.HashNode OboxNode) {
      Voice.Voice_OffsetBox vobox = (Voice.Voice_OffsetBox)Deserialize_Stack(OboxNode);// another cast!
      // For new file formats, we can replace vobox shallow load here.
      vobox.ShallowLoad(OboxNode);
      return vobox;
    }
    /* ********************************************************************************* */
    @Override Voice Deserialize_Songlet(JsonParse.HashNode node) {
      // Before we even enter this function, first determine if phrase just has a txt pointer instead of a ChildrenHash.
      Voice voz = new Voice();
      Hydrate_Voice(node, voz);
      return voz;
    }
    /* ********************************************************************************* */
    void Hydrate_Voice(JsonParse.HashNode VoiceNode, Voice voz) {// Fill in all the values of an already-created object, including deep pointers.
      if (VoiceNode == null) { return; }
      voz.ShallowLoad(VoiceNode);
      JsonParse.Node SubNode = VoiceNode.Get(Voice.CPointsName);
      if (SubNode != null && SubNode.MyType == JsonParse.Node.Types.IsArray) {
        JsonParse.ArrayNode PhrasePointList = (JsonParse.ArrayNode)SubNode;// another cast!
        voz.Wipe_CPoints();
        VoicePoint vpoint;
        JsonParse.HashNode PhrasePoint;
        int len = PhrasePointList.ChildrenArray.size();
        for (int pcnt = 0; pcnt < len; pcnt++) {
          PhrasePoint = (JsonParse.HashNode)PhrasePointList.Get(pcnt);// another cast!
          vpoint = new VoicePoint();// to do: replace this with a factory owned by VoicePoint.
//          vpoint.Consume(PhrasePoint, fmap.ExistingInstances);// to do: get rid of Consume and change this instance to shallowload(?)
          vpoint.ShallowLoad(PhrasePoint);
          voz.Add_Note(vpoint);
        }
        voz.Sort_Me();
      }
    }
  };
  /* ********************************************************************************* */
  public static class SampleVoiceFactory extends VoiceFactory{
//    static final String ObjTypeName = "SampleVoice_OffsetBox";
    SampleVoiceFactory(FileMapper fmap0) {super(fmap0);}
    /* ********************************************************************************* */
    @Override SampleVoice.SampleVoice_OffsetBox Deserialize_OffsetBox(JsonParse.HashNode OboxNode) {
      SampleVoice.SampleVoice_OffsetBox svobox = (SampleVoice.SampleVoice_OffsetBox)Deserialize_Stack(OboxNode);// another cast!
      // For new file formats, we can replace svobox shallow load here.
      svobox.ShallowLoad(OboxNode);
      return svobox;
    }
    /* ********************************************************************************* */
    @Override SampleVoice Deserialize_Songlet(JsonParse.HashNode node) {
      // Before we even enter this function, first determine if phrase just has a txt pointer instead of a ChildrenHash.
      SampleVoice svoz = new SampleVoice();
      Hydrate_SampleVoice(node, svoz);
      return svoz;
    }
    /* ********************************************************************************* */
    void Hydrate_SampleVoice(JsonParse.HashNode VoiceNode, SampleVoice voz) {// Fill in all the values of an already-created object, including deep pointers.
      //if (VoiceNode == null) { return; }
      Hydrate_Voice(VoiceNode, voz);// SampleVoices shallowload
      //voz.ShallowLoad(VoiceNode);
    }
  };
  /* ********************************************************************************* */
  public static class PluckVoiceFactory extends SampleVoiceFactory{
    PluckVoiceFactory(FileMapper fmap0) {super(fmap0);}
    /* ********************************************************************************* */
    @Override PluckVoice.PluckVoice_OffsetBox Deserialize_OffsetBox(JsonParse.HashNode OboxNode) {
      PluckVoice.PluckVoice_OffsetBox vobox = (PluckVoice.PluckVoice_OffsetBox)Deserialize_Stack(OboxNode);// another cast!
      // For new file formats, we can replace vobox shallow load here.
      vobox.ShallowLoad(OboxNode);
      return vobox;
    }
    /* ********************************************************************************* */
    @Override PluckVoice Deserialize_Songlet(JsonParse.HashNode node) {
      // Before we even enter this function, first determine if phrase just has a txt pointer instead of a ChildrenHash.
      PluckVoice voz = new PluckVoice();
      Hydrate_PluckVoice(node, voz);
      return voz;
    }
    /* ********************************************************************************* */
    void Hydrate_PluckVoice(JsonParse.HashNode VoiceNode, SampleVoice voz) {// Fill in all the values of an already-created object, including deep pointers.
      Hydrate_SampleVoice(VoiceNode, voz);// SampleVoices shallowload
      //voz.ShallowLoad(VoiceNode);
    }
  };
  /* ********************************************************************************* */
  public static class GroupSongFactory extends FactoryBase{
    GroupSongFactory(FileMapper fmap0) {super(fmap0);}
    /* ********************************************************************************* */
    @Override GroupSong.Group_OffsetBox Deserialize_OffsetBox(JsonParse.HashNode OboxNode) {
      GroupSong.Group_OffsetBox grobox = (GroupSong.Group_OffsetBox)Deserialize_Stack(OboxNode);// another cast!
      // For new file formats, we can replace grobox shallow load here.
      grobox.ShallowLoad(OboxNode);
      return grobox;
    }
    /* ********************************************************************************* */
    @Override GroupSong Deserialize_Songlet(JsonParse.HashNode node) {
      // Before we even enter this function, first determine if phrase just has a txt pointer instead of a ChildrenHash.
      GroupSong group = new GroupSong();
      Hydrate_Group(node, group);
      return group;
    }
    /* ********************************************************************************* */
    void Hydrate_Group(JsonParse.HashNode GroupNode, GroupSong group) {// Fill in all the values of an already-created object, including deep pointers.
      if (GroupNode == null) { return; }
      group.ShallowLoad(GroupNode);
      JsonParse.Node SubNode = GroupNode.Get(GroupSong.SubSongsName);
      if (SubNode != null && SubNode.MyType == JsonParse.Node.Types.IsArray) {
        JsonParse.ArrayNode ChildPhraseList = (JsonParse.ArrayNode)SubNode;// another cast!
        group.Wipe_SubSongs();
        OffsetBox ChildObox;
        JsonParse.Node ChildItemNode;
        int len = ChildPhraseList.ChildrenArray.size();
        for (int pcnt = 0; pcnt < len; pcnt++) {// iterate through the array
          ChildItemNode = ChildPhraseList.Get(pcnt);
          if (ChildItemNode!=null && ChildItemNode.MyType == JsonParse.Node.Types.IsHash){
            JsonParse.HashNode ChildObjNode = (JsonParse.HashNode)ChildItemNode;// another cast!
            String TypeName = Utils.GetStringField(ChildObjNode, ITextable.ObjectTypeName, "null");
            FactoryBase factory = fmap.GetFactory(TypeName);// use factories to deal with polymorphism
            if (factory!=null){
              ChildObox = factory.Deserialize_OffsetBox(ChildObjNode);
              group.Add_SubSong(ChildObox);
            }
          }
        }
        group.Sort_Me();
      }
    }
  };
  /* ********************************************************************************* */
  public static class LoopSongFactory extends GroupSongFactory{
    LoopSongFactory(FileMapper fmap0)  {super(fmap0);}
    /* ********************************************************************************* */
    @Override LoopSong.Loop_OffsetBox Deserialize_OffsetBox(JsonParse.HashNode OboxNode) {
      LoopSong.Loop_OffsetBox grobox = (LoopSong.Loop_OffsetBox)Deserialize_Stack(OboxNode);// another cast!
      // For new file formats, we can replace grobox shallow load here.
      grobox.ShallowLoad(OboxNode);
      return grobox;
    }
    /* ********************************************************************************* */
    @Override LoopSong Deserialize_Songlet(JsonParse.HashNode node) {
      // Before we even enter this function, first determine if phrase just has a txt pointer instead of a ChildrenHash.
      LoopSong loop = new LoopSong();
      Hydrate_Loop(node, loop); // this will not actually work
      return loop;
    }
    /* ********************************************************************************* */
    void Hydrate_Loop(JsonParse.HashNode LoopNode, LoopSong loop) {// Fill in all the values of an already-created object, including deep pointers.
      if (LoopNode == null) { return; }
      loop.ShallowLoad(LoopNode);// WARNING Hydrate_Loop is all untested junk shoveled from GroupFactory
      JsonParse.Node SubNode = LoopNode.Get(GroupSong.SubSongsName);
      if (SubNode != null && SubNode.MyType == JsonParse.Node.Types.IsArray) {
        JsonParse.ArrayNode ChildPhraseList = (JsonParse.ArrayNode)SubNode;// another cast!
        loop.Wipe_SubSongs();
        OffsetBox ChildObox;
        JsonParse.Node ChildItemNode;
        int len = ChildPhraseList.ChildrenArray.size();
        for (int pcnt = 0; pcnt < len; pcnt++) {// iterate through the array
          ChildItemNode = ChildPhraseList.Get(pcnt);
          if (ChildItemNode!=null && ChildItemNode.MyType == JsonParse.Node.Types.IsHash){
            JsonParse.HashNode ChildObjNode = (JsonParse.HashNode)ChildItemNode;// another cast!
            String TypeName = Utils.GetStringField(ChildObjNode, ITextable.ObjectTypeName, "null");
            FactoryBase factory = fmap.GetFactory(TypeName);// use factories to deal with polymorphism
            if (factory!=null){
              ChildObox = factory.Deserialize_OffsetBox(ChildObjNode);
              loop.Add_SubSong(ChildObox);
            }
          }
        }
        loop.Sort_Me();
      }
    }
  };
  /* ********************************************************************************* */
  public static class GraphicFactory extends FactoryBase {// for serialization
    GraphicFactory(FileMapper fmap0) {super(fmap0);}
    /* ********************************************************************************* */
    @Override GraphicBox.Graphic_OffsetBox Deserialize_OffsetBox(JsonParse.HashNode OboxNode) {
      GraphicBox.Graphic_OffsetBox grabox = (GraphicBox.Graphic_OffsetBox)Deserialize_Stack(OboxNode);// another cast!
      // For new file formats, we can replace grabox shallow load here.
      grabox.ShallowLoad(OboxNode);
      return grabox;
    }
    /* ********************************************************************************* */
    @Override GraphicBox Deserialize_Songlet(JsonParse.HashNode node) {
      // Before we even enter this function, first determine if phrase just has a txt pointer instead of a ChildrenHash.
      GraphicBox graphic = new GraphicBox();
      Hydrate_Graphic(node, graphic);
      return graphic;
    }
    void Hydrate_Graphic(JsonParse.HashNode GraphicNode, GraphicBox grabox){
      if (GraphicNode == null) { return; }
      grabox.ShallowLoad(GraphicNode);
      JsonParse.Node SubNode = GraphicNode.Get(GraphicBox.ContentOBoxName);
      if (SubNode != null && SubNode.MyType == JsonParse.Node.Types.IsHash) {
        JsonParse.HashNode ChildOboxNode = (JsonParse.HashNode)SubNode;// another cast!
        OffsetBox ChildObox;
        String TypeName = Utils.GetStringField(ChildOboxNode, ITextable.ObjectTypeName, "null");
        FactoryBase factory = fmap.GetFactory(TypeName);// use factories to deal with polymorphism
        if (factory!=null){
          ChildObox = factory.Deserialize_OffsetBox(ChildOboxNode);
          grabox.Attach_Content(ChildObox);
        }
      }
    }
  };
  /* ********************************************************************************* */
  ITextable.CollisionLibrary HitTable = new ITextable.CollisionLibrary();
  ITextable.CollisionLibrary ExistingInstances = new ITextable.CollisionLibrary();
  HashMap<String, FactoryBase> Factories = new HashMap<String, FactoryBase>();
  /* ********************************************************************************* */
  FileMapper(){
    this.BuildFactories();
    ExistingInstances.Clear();
    HitTable.Clear();
  }
  // ~FileMapper(){ this.DeleteFactories(); }
  /* ********************************************************************************* */
  static double GetNumberField(JsonParse.HashNode hnode, String FieldName, double DefaultValue){
    String FieldTxt;
    FieldTxt = hnode.GetField(FieldName, Double.toString(DefaultValue));
    return Double.parseDouble(FieldTxt);// yuck. convert double to string and parse it back? to do: fix this.
  }
  /* ********************************************************************************* */
  static void FillOffsetBox(OffsetBox obox, JsonParse.HashNode hnode){
    obox.TimeX = Utils.GetNumberField(hnode, MonkeyBox.TimeXName, 0);
    obox.OctaveY = GetNumberField(hnode, MonkeyBox.OctaveYName, 0);
    obox.LoudnessFactor = GetNumberField(hnode, MonkeyBox.LoudnessFactorName, 1.0);
    obox.ScaleX = GetNumberField(hnode, MonkeyBox.ScaleXName, 1.0);
    obox.ScaleY = GetNumberField(hnode, MonkeyBox.ScaleYName, 1.0);
    if (false){
      obox.OctavesPerRadius = GetNumberField(hnode, "OctavesPerRadius", 0.01);
    }
  }
  /* ********************************************************************************* */
  void BuildFactories(){
    Factories.put(GraphicBox.Graphic_OffsetBox.ObjectTypeName, new GraphicFactory(this));
    Factories.put(Voice.Voice_OffsetBox.ObjectTypeName, new VoiceFactory(this));
    Factories.put(SampleVoice.SampleVoice_OffsetBox.ObjectTypeName, new SampleVoiceFactory(this));
    Factories.put(PluckVoice.PluckVoice_OffsetBox.ObjectTypeName, new PluckVoiceFactory(this));
    Factories.put(GroupSong.Group_OffsetBox.ObjectTypeName, new GroupSongFactory(this));
    Factories.put(LoopSong.Loop_OffsetBox.ObjectTypeName, new LoopSongFactory(this));
  }
  /* ********************************************************************************* */
  void DeleteFactories(){
//    for (iter=this.Factories.begin(); iter!=this.Factories.end(); ++iter){
//      delete iter.second;
//    }
    this.Factories.clear();
  }
  /* ********************************************************************************* */
  FactoryBase GetFactory(String FactoryName){
    FactoryBase factory;
    if (this.Factories.containsKey(FactoryName)){
      factory = this.Factories.get(FactoryName);
      return factory;
    }
    return null;
  }
  /* ********************************************************************************* */
  GraphicBox.Graphic_OffsetBox Create(JsonParse.HashNode RootObjNode){
    GraphicBox.Graphic_OffsetBox RootSongObox = null;

    // Retrieve these hashnodes.
    JsonParse.HashNode TreePhrase = (JsonParse.HashNode)RootObjNode.Get(TreePhraseName);// another cast!
    JsonParse.HashNode LibraryPhrase = (JsonParse.HashNode)RootObjNode.Get(LibraryPhraseName);// another cast!

    this.ExistingInstances.ConsumeLibrary(LibraryPhrase);

    String TypeName = Utils.GetStringField(TreePhrase, ITextable.ObjectTypeName, "null");
    FactoryBase factory = this.GetFactory(TypeName);// use factories to deal with polymorphism
    if (factory!=null){
      // here we presume that the root obox type was a Graphic_OffsetBox.
      // to do: put some safeguards in here in case it isn't.
      RootSongObox = (GraphicBox.Graphic_OffsetBox)factory.Deserialize_OffsetBox(TreePhrase);// another cast!
    }
    return RootSongObox;
  }
};
