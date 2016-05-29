package voices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author MultiTool
 */
public interface ITextable {// DIY Json ISerializable - more control
  // void Read_In(JsonParse.Node phrase);
  // void IFactory(JsonParse.Node phrase);// will need a static factory function for each object type
  // hmm if we can't override statics, maybe every object can create a single MeFactory class that makes one of the parent object. 
  //void InhaleMySoul(JsonParse.Node phrase);

  //void Textify(StringBuilder sb);// to do: pass a collision table parameter
  JsonParse.Node Export(CollisionLibrary HitTable);// to do: pass a collision table parameter
  void ShallowLoad(JsonParse.Node phrase);// just fill in primitive fields that belong to this object, don't follow up pointers.
  void Consume(JsonParse.Node phrase, CollisionLibrary ExistingInstances);// Fill in all the values of an already-created object, including deep pointers.
//  IFactory GetMyFactory();// this always returns a singleton of IFactory, one for each class declaration. 
//  
  public interface IFactory {// probably overkill. will try delegate pointers to static methods instead
    public ITextable Create(JsonParse.Node phrase, CollisionLibrary ExistingInstances);
    public class Utils {
      public static String GetField(HashMap<String, JsonParse.Node> Fields, String FieldName, String DefaultValue) {
        String retval;
        if (Fields.containsKey(FieldName)) {
          JsonParse.Node phrase = Fields.get(FieldName);
          retval = phrase.Literal;
        } else {
          retval = DefaultValue;
        }
        return retval;
      }
      public static JsonParse.Node LookUpField(HashMap<String, JsonParse.Node> Fields, String FieldName) {
        if (Fields.containsKey(FieldName)) {
          return Fields.get(FieldName);
        } else {
          return null;
        }
      }
      public static <ThingType extends ITextable> ArrayList<JsonParse.Node> MakeArray(CollisionLibrary HitTable, ArrayList<ThingType> Things) {
        ArrayList<JsonParse.Node> stuff = new ArrayList<JsonParse.Node>();
        int len = Things.size();
        for (int cnt = 0; cnt < len; cnt++) {
          stuff.add(Things.get(cnt).Export(HitTable));
        }
        return stuff;
      }
      public static JsonParse.Node PackField(Object Value) {// probably not very C++ compatible
        JsonParse.Node phrase = new JsonParse.Node();
        phrase.Literal = String.valueOf(Value);
        return phrase;
      }
    }
  }
  /* ********************************************************************************* */
  public class CollisionItem {// do we really need this? 
    public String ItemTxtPtr;// Key, usually
    public ITextable Item = null, ClonedItem = null;
    public JsonParse.Node JsonPhrase = null;// serialization of the ITextable Item
  }
  /* ********************************************************************************* */
  public class CollisionLibrary {// contains twice-indexed list of instances of (usually) songlet/phrase pairs for serialization, DEserialization, and cloning
    int ItemIdNum = 0;
    private HashMap<ITextable, CollisionItem> Instances = new HashMap<ITextable, CollisionItem>();// serialization and cloning
    private HashMap<String, CollisionItem> Items = new HashMap<String, CollisionItem>();// DEserialization
    public CollisionItem InsertUniqueInstance(ITextable KeyObj) {// for serialization
      CollisionItem ci = new CollisionItem();
      ci.ItemTxtPtr = Globals.PtrPrefix + ItemIdNum;
      ci.Item = KeyObj;
      this.Instances.put(KeyObj, ci);// object is key
      this.Items.put(ci.ItemTxtPtr, ci);// string is key
      ItemIdNum++;
      return ci;
    }
    public void InsertTextifiedItem(String KeyTxt, JsonParse.Node Item) {// for deserialization, only on load
      CollisionItem ci = new CollisionItem();
      ci.Item = null;
      ci.ItemTxtPtr = KeyTxt;
      ci.JsonPhrase = Item;
      this.Items.put(KeyTxt, ci);// string is key
      //this.Instances.put(Item, ci);// object is key - wait, this won't work
    }
    public CollisionItem GetItem(ITextable KeyObj) {// for serialization
      return this.Instances.get(KeyObj);
    }
    public CollisionItem GetItem(String KeyTxt) {
      return this.Items.get(KeyTxt);
    }
    public JsonParse.Node ExportJson() {
      JsonParse.Node MainPhrase = new JsonParse.Node();
      MainPhrase.ChildrenHash = new HashMap<String, JsonParse.Node>();
      JsonParse.Node ChildPhrase;
      CollisionItem ci;
      for (Map.Entry<String, CollisionItem> entry : this.Items.entrySet()) {
        ci = entry.getValue();
        if (ci.JsonPhrase != null) {
          //ChildPhrase = new JsonParse.Node();// should we clone the child phrase? 
          MainPhrase.AddSubPhrase(ci.ItemTxtPtr, ci.JsonPhrase);
        }
      }
      return MainPhrase;
    }
    public void ConsumePhrase(JsonParse.Node LibraryPhrase) {
      this.Items.clear();
      this.Instances.clear();
      for (Map.Entry<String, JsonParse.Node> entry : LibraryPhrase.ChildrenHash.entrySet()) {
        this.InsertTextifiedItem(entry.getKey(), entry.getValue());
      }
    }
    public void Wipe_Songlets() {// for testing
      CollisionItem ci;
      IDeletable deletable;
      for (Map.Entry<String, CollisionItem> entry : this.Items.entrySet()) {
        ci = entry.getValue();
        if ((deletable = (IDeletable) ci.Item) != null) {
          //deletable.Delete_Me();
          ci.Item = null;
        }
      }
    }
  }
}
