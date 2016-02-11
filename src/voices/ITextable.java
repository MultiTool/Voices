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

  void ShallowLoad(JsonParse.Phrase phrase);// just fill in primitive fields that belong to this object, don't follow up pointers.
  void Textify(StringBuilder sb);// to do: pass a collision table parameter
  JsonParse.Phrase Export(CollisionTable CTable);// to do: pass a collision table parameter

//  IFactory GetMyFactory();// this always returns a singleton of IFactory, one for each class declaration. 
//  
  public interface IFactory {// probably overkill. will try delegate pointers to static methods instead
    public ITextable Create(JsonParse.Phrase phrase);
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
//    public static <ThingType> ArrayList<JsonParse.Phrase> MakeArrayGen(ArrayList<ThingType> Things) {
//      ArrayList<JsonParse.Phrase> stuff = new ArrayList<JsonParse.Phrase>();
//      int len = Things.size();
//      for (int cnt = 0; cnt < len; cnt++) {
//        stuff.add(Things.get(cnt);
//      }
//    }
    public static <ThingType extends ITextable> ArrayList<JsonParse.Phrase> MakeArray(CollisionTable CTable, ArrayList<ThingType> Things) {
      ArrayList<JsonParse.Phrase> stuff = new ArrayList<JsonParse.Phrase>();
      int len = Things.size();
      for (int cnt = 0; cnt < len; cnt++) {
        stuff.add(Things.get(cnt).Export(CTable));
      }
      return stuff;
    }
    public static JsonParse.Phrase PackField(Object Value) {// probably not very C++ compatible
      JsonParse.Phrase phrase = new JsonParse.Phrase();
      phrase.Literal = String.valueOf(Value);
      return phrase;
    }
  }
  public class CollisionItem {// do we really need this? 
    public String ItemPtr;
    public ITextable Item;
  }
  public class CollisionTable {
    int ItemIdNum = 0;
    public HashMap<ITextable, CollisionItem> table;
    public void InsertNewItem(ITextable Key) {
      CollisionItem ci = new CollisionItem();
      ci.ItemPtr = "some unique counter" + ItemIdNum;
      ci.Item = Key;
      this.table.put(Key, ci);// ITextable
    }
  }
}
