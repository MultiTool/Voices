package voices;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author MultiTool
 */
public interface ITextable {// DIY Json ISerializable - more control
  // void Read_In(JsonParse.Phrase phrase);
  // void IFactory(JsonParse.Phrase phrase);// will need a static factory function for each object type
  // hmm if we can't override statics, maybe every object can create a single MeFactory class that makes one of the parent object. 
  //void InhaleMySoul(JsonParse.Phrase phrase);

  void Textify(StringBuilder sb);// to do: pass a collision table parameter
  JsonParse.Phrase Export(InstanceCollisionTable HitTable);// to do: pass a collision table parameter
  void ShallowLoad(JsonParse.Phrase phrase);// just fill in primitive fields that belong to this object, don't follow up pointers.
  void Consume(JsonParse.Phrase phrase, TextCollisionTable ExistingInstances);// Fill in all the values of an already-created object, including deep pointers.
//  IFactory GetMyFactory();// this always returns a singleton of IFactory, one for each class declaration. 
//  
  public interface IFactory {// probably overkill. will try delegate pointers to static methods instead
    public ITextable Create(JsonParse.Phrase phrase, TextCollisionTable ExistingInstances);
//    public static <ThingType> ThingType GetFieldGen(HashMap<String, JsonParse.Phrase> Fields, String FieldName, ThingType DefaultValue) {
//      ThingType retval;
//      if (Fields.containsKey(FieldName)) {
//        JsonParse.Phrase phrase = Fields.get(FieldName);
//        // Double.parseDouble(phrase.Literal);
//        retval = ThingType. phrase.Literal;// oy need generic parser like std::cout
//      } else {
//        retval = DefaultValue;
//      }
//      return retval;
//    }
//    public static <ThingType> ArrayList<JsonParse.Phrase> MakeArrayGen(ArrayList<ThingType> Things) {
//      ArrayList<JsonParse.Phrase> stuff = new ArrayList<JsonParse.Phrase>();
//      int len = Things.size();
//      for (int cnt = 0; cnt < len; cnt++) {
//        stuff.add(Things.get(cnt);
//      }
//    }
    public class Utils {
      public static String GetField(HashMap<String, JsonParse.Phrase> Fields, String FieldName, String DefaultValue) {
        String retval;
        if (Fields.containsKey(FieldName)) {
          JsonParse.Phrase phrase = Fields.get(FieldName);
          retval = phrase.Literal;
        } else {
          retval = DefaultValue;
        }
        return retval;
      }
      public static JsonParse.Phrase LookUpField(HashMap<String, JsonParse.Phrase> Fields, String FieldName) {
        if (Fields.containsKey(FieldName)) {
          return Fields.get(FieldName);
        } else {
          return null;
        }
      }
      public static <ThingType extends ITextable> ArrayList<JsonParse.Phrase> MakeArray(InstanceCollisionTable HitTable, ArrayList<ThingType> Things) {
        ArrayList<JsonParse.Phrase> stuff = new ArrayList<JsonParse.Phrase>();
        int len = Things.size();
        for (int cnt = 0; cnt < len; cnt++) {
          stuff.add(Things.get(cnt).Export(HitTable));
        }
        return stuff;
      }
      public static JsonParse.Phrase PackField(Object Value) {// probably not very C++ compatible
        JsonParse.Phrase phrase = new JsonParse.Phrase();
        phrase.Literal = String.valueOf(Value);
        return phrase;
      }
    }
  }
  public class CollisionItem {// do we really need this? 
    public String ItemPtr;
    public ITextable Item;
    JsonParse.Phrase JsonPhrase = new JsonParse.Phrase();// serialization of the ITextable Item
    // ci.JsonPhrase = new JsonParse.Phrase();
  }
  public class InstanceCollisionTable {// contains list of instances of (usually) songlets for serialization
    int ItemIdNum = 0;
    private HashMap<ITextable, CollisionItem> Instances = new HashMap<ITextable, CollisionItem>();
    public CollisionItem InsertUniqueInstance(ITextable Key) {
      CollisionItem ci = new CollisionItem();
      //ci.ItemPtr = "some unique counter" + ItemIdNum;
      ci.ItemPtr = Globals.PtrPrefix + ItemIdNum;
      ci.Item = Key;
      this.Instances.put(Key, ci);// ITextable
      ItemIdNum++;
      return ci;
    }
    public boolean ContainsInstance(ITextable Key) {
      return this.Instances.containsKey(Key);
    }
    public String GetItemPtr(ITextable Key) {
      return this.Instances.get(Key).ItemPtr;
    }
  }
  /*
   for export, we must map from songlet pointer to stringptr
   for import, we must map from stringptr to json phrase, and from stringptr to songlet pointer
   */
  public class TextCollisionTable {// contains list of instances of (usually) songlets for DEserialization
    private HashMap<String, JsonParse.Phrase> RawDescriptions = new HashMap<String, JsonParse.Phrase>();
    private HashMap<String, ITextable> Instances = new HashMap<String, ITextable>();
    public void InsertJsonDescription(String KeyPtr, JsonParse.Phrase Item) {
      this.RawDescriptions.put(KeyPtr, Item);
    }
    public JsonParse.Phrase GetJsonDescription(String KeyPtr) {
      return this.RawDescriptions.get(KeyPtr);
    }
    public void InsertUniqueInstance(String KeyPtr, ITextable Item) {
      this.Instances.put(KeyPtr, Item);// ITextable
    }
    public ITextable GetInstance(String KeyPtr) {
      return this.Instances.get(KeyPtr);
      //if (this.table.containsKey(KeyPtr)) { return this.table.get(KeyPtr); } else { return null; }
    }
    public boolean ContainsKey(ITextable Key) {
      return this.Instances.containsKey(Key);
    }
  }
}
