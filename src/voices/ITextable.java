package voices;

/**
 *
 * @author MultiTool
 */
public interface ITextable {// DIY Json ISerializable
  void Read_In(JsonParse.Phrase phrase);
  void Textify(StringBuilder sb);// to do: pass a collision table parameter
}
