package voices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/* ********************************************************************************************************* */
public class JsonParse {
  public enum TokenType { None, CommentStar, CommentSlash, Word, Whitespace, SingleChar, TextString }
  public static String Environment_NewLine = "\r\n";
  public static String PhraseEnd=",";
  HashNode RootNode = null;
  /* ********************************************************************************************************* */
  public static class Ops { // basic static string operations
    // <editor-fold defaultstate="collapsed" desc="Literal finders, Is Word, Is Number, etc">
    /* ********************************************************************************************************* */
    static boolean IsWordChar(char ch) {// for alphanumerics, eg variable names, reserved words, etc.
      if ('a' <= ch && ch <= 'z') { return true; }
      if ('A' <= ch && ch <= 'Z') { return true; }
      if ('0' <= ch && ch <= '9') { return true; }
      if ('_' == ch || ch == '@') { return true; }
      if ('-' == ch || ch == '.') { return true; }// for numbers
      return false;
    }
    /* ********************************************************************************************************* */
    static boolean IsNumericChar(char ch) {// for numbers
      if ('0' <= ch && ch <= '9') { return true; }
      if ('-' == ch || ch == '.') { return true; }// currently we are sloppy and let gibberish like ".--99.00.-45..88" go through
      return false;
    }
    /* ********************************************************************************************************* */
    static boolean IsNumericPunctuationChar(char ch) {// for number punctuation such as '.' and '-' anything else?
      if ('-' == ch || ch == '.') { return true; }// currently we are sloppy and let gibberish like ".--99.00.-45..88" go through
      return false;
    }
    /* ********************************************************************************************************* */
    public static boolean IsNumericSuffixChar(char ch) {
      ch = Character.toLowerCase(ch);
      if ('e' == ch || ch == 'f') { return true; }// currently we are sloppy and let gibberish like ".--99.00.-45..88" go through
      return false;
    }
    /* ********************************************************************************************************* */
    static boolean IsNumericString(String txt) {// for numbers
      if (txt.length()==0){return false;}// currently we are sloppy and let gibberish like ".--99.00.-45..88" go through
      int chcnt = 0;
      boolean isnum = true;
      while (chcnt < txt.length()) {
        if (!IsNumericChar(txt.charAt(chcnt))) { isnum = false; break; }
        chcnt++;
      }
      return isnum;
    }
    /* ********************************************************************************************************* */
    public static boolean IsBlankChar(char ch) {// any whitespace
      if (' ' == ch || ch == '\t' || ch == '\n' || ch == '\r') { return true; }
      return false;
    }
    // </editor-fold>
    /* ********************************************************************************************************* */
    static public String Trim(String RawText) {// trim from both ends (in place)
      return RawText.trim();
    }
    /* ********************************************************************************************************* */
    public static String DeQuote(String Text) {
      Text= Text.trim();
      if (Text.length()<2){return Text;}
      if (Text.charAt(0)=='\'' || Text.charAt(0)=='\"'){
        Text = Text.substring(1);
      }
      int last = Text.length()-1;
      if (Text.charAt(last)=='\'' || Text.charAt(last)=='\"'){
        Text = Text.substring(0, last);
      }
      return Text;
    }
    /* ********************************************************************************************************* */
    public static String EnQuote(String Text){
      if (Text==null){
        System.out.println("txt");
      }
      Text = Text.replace("\"", "\"\"");
      return "\"" + Text + "\"";
    }
    /* ********************************************************************************************************* */
    static int CompareStartStay(String txt, int StartPlace, String target) {// look for any substring right at the StartPlace of the text
      int foundloc = StartPlace;// if not found, return the place where we started
      int cnt = StartPlace;
      int tcnt = 0;
      while (cnt < txt.length()){// && tcnt < target.length())
        char ch = txt.charAt(cnt);
        if (ch != target.charAt(tcnt)) { break; }
        cnt++; tcnt++;
        if (tcnt >= target.length()) { foundloc = cnt; break; }// return the loc of just past the end of what we found
      }
      return foundloc;
    }
    /* ********************************************************************************************************* */
    static int CompareStart(String txt, int StartPlace, String target) {// look for any substring right at the StartPlace of the text
      int foundloc = -1;
      int cnt = StartPlace;
      int tcnt = 0;
      while (cnt < txt.length()){// && tcnt < target.length())
        char ch = txt.charAt(cnt);
        if (ch != target.charAt(tcnt)) { break; }
        cnt++; tcnt++;
        if (tcnt >= target.length()) { foundloc = cnt; break; }// return the loc of just past the end of what we found
      }
      return foundloc;
    }
    /* ********************************************************************************************************* */
    static int CompareStartAny(String txt, int StartPlace, ArrayList<String> targets) {// look for one of any substrings right at the StartPlace of the text
      int foundloc = -1;
      int sz = targets.size();
      for (int cnt=0; cnt<sz; cnt++) {  // snox, replace foreach
        if ((foundloc = CompareStart(txt, StartPlace, targets.get(cnt))) >= 0) { break; }
      }
      return foundloc;
    }
  };
  /* ********************************************************************************************************* */
  public static class Token {
    public String Text;
    public TokenType BlockType;
    public String SpecificType = "None";
    public String DeQuoted() { return Ops.DeQuote(this.Text); }
  };
  /* ********************************************************************************************************* */
  public static class Tokenizer {
  ArrayList<Token> Tokens = new ArrayList<Token>();//TokenList;
  /* ********************************************************************************************************* */
  Tokenizer(){}
  /* ********************************************************************************************************* */
  ArrayList<Token> Tokenize(int StartPlace, String txt) {
    Clear_TokenList();
    int len = txt.length();
    int MarkPrev = 0, MarkNext = 0;
    while (MarkPrev < len){
      if ((MarkNext = Chomp_CommentStar(txt, MarkPrev)) > MarkPrev) { }
      else if ((MarkNext = Chomp_CommentSlash(txt, MarkPrev)) > MarkPrev) { }
      else if ((MarkNext = Chomp_Word(txt, MarkPrev)) > MarkPrev) { }
      else if ((MarkNext = Chomp_Whitespace(txt, MarkPrev)) > MarkPrev) { }
      else if ((MarkNext = Chomp_SingleChar(txt, MarkPrev)) > MarkPrev) { }
      else if ((MarkNext = Chomp_TextStringDoubleQuoted(txt, MarkPrev)) > MarkPrev) { }
      else if ((MarkNext = Chomp_TextStringSingleQuoted(txt, MarkPrev)) > MarkPrev) { }
      else { System.out.printf("Inconceivable!\n"); break; }//throw new Exception("Inconceivable!");
      MarkPrev = MarkNext;
    }
    return Tokens;
  }
  /* ********************************************************************************************************* */
  void Clear_TokenList(){
    int Len = this.Tokens.size();
    for (int cnt=0;cnt<Len;cnt++){
      this.Tokens.set(cnt, null);
    }
    this.Tokens.clear();
  }
  /* ********************************************************************************************************* */
  void Print_Tokens(){
    int Len = Tokens.size();
    System.out.printf("Print_Tokens: %i tokens\n", Len);
    for (int cnt=0;cnt<Len;cnt++){
      Token tkn = Tokens.get(cnt);
      System.out.printf("[%s]\n", tkn.Text);
    }
  }
  // Chunk Finders
  /* ********************************************************************************************************* */
  int Chomp_CommentStar(String txt, int StartPlace) {
    String ender = "*/";
    int loc = StartPlace, loc1;
    Token tkn = null;
    if ((loc1 = Ops.CompareStart(txt, StartPlace, "/*")) >= 0) {
      loc = txt.indexOf(ender, loc1);
      if (loc >= 0) { loc += ender.length(); } else { loc = txt.length(); }
      String chunk = txt.substring(StartPlace, loc);
      tkn = new Token();//{ Text = chunk, BlockType = TokenType.CommentStar };
      tkn.Text = chunk; tkn.BlockType = TokenType.CommentStar;
      Tokens.add(tkn);
    }
    return loc;
  }
  /* ********************************************************************************************************* */
  int Chomp_CommentSlash(String txt, int StartPlace) {
    String ender0 = Environment_NewLine;
    String ender1 = "\n";
    int loc = StartPlace, loc1;
    Token tkn = null;
    if ((loc1 = Ops.CompareStart(txt, StartPlace, "//")) >= 0) {
      loc = txt.indexOf(ender0, loc1);
      if (loc >= 0) { loc += ender0.length(); }
      else {
        loc = txt.indexOf(ender1, loc1);
        if (loc >= 0) { loc += ender1.length(); } else { loc = txt.length(); }
        String chunk = txt.substring(StartPlace, loc);
        tkn = new Token();// { Text = chunk, BlockType = TokenType.CommentSlash };
        tkn. Text = chunk; tkn.BlockType = TokenType.CommentSlash;
        Tokens.add(tkn);
      }
    }
    return loc;
  }
  /* ********************************************************************************************************* */
  int Chomp_Word(String txt, int StartPlace) {// for alphanumerics, eg variable names, reserved words, etc.
    int loc = StartPlace;
    Token tkn = null;
    char ch = txt.charAt(StartPlace);
    while (loc < txt.length() && Ops.IsWordChar(txt.charAt(loc))) { loc++; }
    if (StartPlace < loc){// then we found something
      String chunk = txt.substring(StartPlace, loc);
      tkn = new Token();// { Text = chunk, BlockType = TokenType.Word };
      tkn. Text = chunk; tkn.BlockType = TokenType.Word;
      Tokens.add(tkn);
    }
    return loc;
  }
  /* ********************************************************************************************************* */
  int Chomp_Whitespace(String txt, int StartPlace) {
    int loc = StartPlace;// for whitespace
    Token tkn = null; // by default whitespace ends where text ends
    while (loc < txt.length() && Ops.IsBlankChar(txt.charAt(loc))) { loc++; }
    if (StartPlace < loc) {// then we found something
      String chunk = txt.substring(StartPlace, loc);
      tkn = new Token();// { Text = chunk, BlockType = TokenType.Whitespace };
      tkn.Text = chunk; tkn.BlockType = TokenType.Whitespace;
      Tokens.add(tkn);
    }
    return loc;
  }
  /* ********************************************************************************************************* */
  public static int Chomp_TextString(String txt,String QuoteChar, int StartPlace, ArrayList<Token> Tokens) {// for text strings "blah blah"  
    int loc0 = StartPlace, loc1;
    Token tkn = null;
    if ((loc1 = Ops.CompareStart(txt, StartPlace, QuoteChar)) >= 0) {
      loc0 = loc1;
      while (loc0 < txt.length()) {// by default String ends where text ends
        char ch = txt.charAt(loc0);
        if (Ops.CompareStart(txt, loc0, "\\") >= 0) { loc0++; }// ignore slash-escaped characters
        else if (Ops.CompareStart(txt, loc0, ""+QuoteChar+""+QuoteChar+"") >= 0) { loc0++; }// ignore double-escaped quotes (only legal in some languages)
        else if (Ops.CompareStart(txt, loc0, QuoteChar) >= 0) { loc0++; break; }// we found a closing quote, break.
        //else { if (ch == QuoteChar.charAt(0)) { loc0++; break; } }
        loc0++;
      }
      if (StartPlace < loc0) {// then we found something
        String chunk = txt.substring(StartPlace, loc0);
        tkn = new Token();// { Text = chunk, BlockType = TokenType.TextString };
        tkn. Text = chunk; tkn.BlockType = TokenType.TextString;
        Tokens.add(tkn);
      }
    }
    return loc0;
  }
  /* ********************************************************************************************************* */
  int Chomp_TextStringDoubleQuoted(String txt, int StartPlace) {
    return Chomp_TextString(txt,"\"", StartPlace, this.Tokens);
  }
  /* ********************************************************************************************************* */
  int Chomp_TextStringSingleQuoted(String txt, int StartPlace) {
    return Chomp_TextString(txt,"'", StartPlace, this.Tokens);// for text strings 'blah blah'
  }
  /* ********************************************************************************************************* */
  int Chomp_SingleChar(String txt, int StartPlace) {
    // for single char such as { or ] or ; etc. 
    // String singles = "}{][)(*&^%$#@!~+=;:.>,<|\\?/-";
    String singles = "}{][)(*&^%$#@!~+=;:>,<|\\?/";
    // int loc = StartPlace;
    Token tkn = null;
    if (StartPlace >= txt.length()) { return StartPlace; }
    char ch = txt.charAt(StartPlace);
    if (singles.indexOf(ch, 0) >= 0){// then we found something
      tkn = new Token();// { Text = ch.toString(), BlockType = TokenType.SingleChar };
      tkn. Text = Character.toString(ch);
      tkn.BlockType = TokenType.SingleChar;
      Tokens.add(tkn);
      StartPlace++;
    }
    return StartPlace;
  }
  };
  /* ********************************************************************************************************* */
  public static class Node {// a value that is a hashtable, an array, a literal, or a pointer to a multiply-used item
    enum Types { None, IsHash, IsArray, IsLiteral, IsPointer };
    Types MyType = Types.None;
    String MyPhraseName = "***Nothing***";
    Node Parent = null;
    int ChunkStart,ChunkEnd;
    Node() {}
    public String ToJson() {return "";};//{ return "Node ToJson should be overridden"; }
  };
  /* ********************************************************************************************************* */
  public static class LiteralNode extends Node {// a value that is a literal
    String Literal = "";
    LiteralNode(){ this.MyType = Types.IsLiteral; }
    //~LiteralNode() { this.Literal = ""; }
    String Get() { return this.Literal; }
    @Override public String ToJson() { return Ops.EnQuote(this.Get()); }
  };
  /* ********************************************************************************************************* */
  public static class HashNode extends Node {// a value that is a hashtable
    public HashMap<String, Node> ChildrenHash = new HashMap<String, Node>();
    public HashNode(){ this.MyType = Types.IsHash; }
    /* ********************************************************************************* */
    public String GetField(String FieldName, String DefaultValue) {
      String retval = "";
      if (ChildrenHash.containsKey(FieldName)) {
        Node phrase = ChildrenHash.get(FieldName);
        if (phrase != null && phrase.MyType == Node.Types.IsLiteral){
          retval = ((LiteralNode)phrase).Literal;// another cast!
        } else {
          retval = DefaultValue;
        }
      } else {
        retval = DefaultValue;
      }
      return retval;
    }
    /* ********************************************************************************* */
    public void AddSubPhrase(String Name, Node ChildPhrase) {
      this.ChildrenHash.put(Name, ChildPhrase); ChildPhrase.Parent = this;
    }
    public Node Get(String Name) {
      if (this.ChildrenHash.containsKey(Name)) { return this.ChildrenHash.get(Name); }
      else { return null; }
    }
    public String ToHash() {
      Node child;
      StringBuilder Text = new StringBuilder();
      Text.append("{");
      int len = this.ChildrenHash.size();
      int ultimo = len-1;
      String key;
      this.ChildrenHash.keySet().toArray();
      if (0<len){
        Set<Map.Entry<String, Node>> Entries = this.ChildrenHash.entrySet();
        Object[] objray = Entries.toArray();
        //Map.Entry<String, Node>[] EntRay = (Map.Entry<String, Node>[]) Entries.toArray();
        int cnt=0;
        while (cnt<ultimo){
          Map.Entry<String, Node> entry = (Map.Entry<String, Node>) objray[cnt];
          key = entry.getKey();
          child = entry.getValue();
          Text.append(Ops.EnQuote(key));
          Text.append(" : ");
          Text.append(child.ToJson());
          Text.append(", ");
          cnt++;
        }
        key = ((Map.Entry<String, Node>)objray[cnt]).getKey();
        child = this.ChildrenHash.get(key);
        Text.append(Ops.EnQuote(key));
        Text.append(" : ");
        Text.append(child.ToJson());
      }
      Text.append("}");
      return Text.toString();
    }
    @Override public String ToJson() { return this.ToHash(); }
  };
  /* ********************************************************************************************************* */
  public static class ArrayNode extends Node {// a value that is an array
    ArrayList<Node> ChildrenArray = new ArrayList<Node>();
    ArrayNode(){ this.MyType = Types.IsArray; }
    void AddSubPhrase(Node ChildPhrase) {
      this.ChildrenArray.add(ChildPhrase); ChildPhrase.Parent = this;
    }
    public Node Get(int Dex) { return this.ChildrenArray.get(Dex);}
    public String ToArray() {
      Node child;
      StringBuilder Text = new StringBuilder();
      Text.append("[");
      int len = this.ChildrenArray.size();
      if (0 < len) {
        int cnt = 0;
        child = this.ChildrenArray.get(cnt++); Text.append(child.ToJson());
        while (cnt < len) {
          Text.append(", ");
          child = this.ChildrenArray.get(cnt++); Text.append(child.ToJson());
        }
      }
      Text.append("]");
      return Text.toString();
    }
    @Override public String ToJson() { return this.ToArray(); }
  };
  /* ********************************************************************************************************* */
  public JsonParse(){}
  /* ********************************************************************************************************* */
  LiteralNode MakeLiteral(String Text, int ChunkStart, int ChunkEnd) {
    LiteralNode OnePhrase = new LiteralNode();
    OnePhrase.Literal = Text; OnePhrase.ChunkStart=ChunkStart; OnePhrase.ChunkEnd=ChunkEnd;
    OnePhrase.MyType = Node.Types.IsLiteral;
    return OnePhrase;
  }
  /* ********************************************************************************************************* */
  LiteralNode Chomp_Number(ArrayList<Token> Chunks, int Marker, int RecurDepth) {// this is wrong. need to re-think it before using.
    // chunks are a number if: all chunks are all numeric
    // but not if: numeric chunks end, but next chunk is non-whitespace, non-comma, non-semicolon, and what else? just non-delimiter?
    // 12.345blah is not a number. maybe let that pass anyway? 123.4f is a number sometimes.
    // hmm. valid numberenders: ; , []() etc. any single char thing that's not a .
    // how about a number can end with whitespace or any non-numeric punctation.
    LiteralNode OnePhrase = null;
    int FirstChunk, FinalChunk = Marker;
    Token tkn = Chunks.get(Marker);
    char ch = tkn.Text.charAt(0);
    if (Ops.IsNumericPunctuationChar(ch) || Ops.IsNumericString(tkn.Text)) {
      FirstChunk = Marker;
      String WholeString = "";
      while (Marker < Chunks.size()) {// to do: fix this. as-is will return true if text is empty.
        tkn = Chunks.get(Marker);
        ch = tkn.Text.charAt(0);
        if (!(Ops.IsNumericPunctuationChar(ch) || Ops.IsNumericString(tkn.Text) || Ops.IsNumericSuffixChar(ch))) { break; }
        FinalChunk = Marker;
        WholeString = WholeString + tkn.Text;
        Marker++;
      }
      OnePhrase = new LiteralNode(); OnePhrase.ChunkStart = FirstChunk; OnePhrase.ChunkEnd = FinalChunk;
      OnePhrase.Literal = WholeString;
      OnePhrase.MyType = Node.Types.IsLiteral;
    }
    return OnePhrase;
  }
  /* ********************************************************************************************************* */
  HashNode Chomp_HashMap(ArrayList<Token> Chunks, int Marker, int RecurDepth) {
    HashNode OnePhrase = null;
    Node SubPhrase = null;
    String Starter="{", Ender="}";
    int MarkNext=Marker;
    int KeyOrValue=0;
    String Empty = "";
    String Key=Empty;
    RecurDepth++;
    Token tkn = Chunks.get(Marker);
    if (tkn.Text.equals(Starter)){
      OnePhrase = new HashNode();
      OnePhrase.ChunkStart = Marker;
      OnePhrase.ChildrenHash.clear();
      MarkNext = ++Marker;
      while (Marker<Chunks.size()) {
        tkn = Chunks.get(Marker);
        if (tkn.Text.equals(Ender)){break;}
        else if ((SubPhrase = Chomp_HashMap(Chunks,  Marker, RecurDepth))!=null){
          OnePhrase.AddSubPhrase(Key, SubPhrase); Key=Empty; MarkNext = SubPhrase.ChunkEnd+1;
        } else if ((SubPhrase = Chomp_Array(Chunks,  Marker, RecurDepth))!=null){
          OnePhrase.AddSubPhrase(Key, SubPhrase); Key=Empty; MarkNext = SubPhrase.ChunkEnd+1;
        } else if ((SubPhrase = Chomp_Number(Chunks, Marker, RecurDepth)) != null) {
          OnePhrase.AddSubPhrase(Key, SubPhrase); Key=Empty; MarkNext = SubPhrase.ChunkEnd+1;
        } else if ((tkn.BlockType == TokenType.TextString) || (tkn.BlockType == TokenType.Word)) {
          if (KeyOrValue == 0) {
            Key = tkn.Text;
            if (tkn.BlockType == TokenType.TextString) { Key=Ops.DeQuote(Key); }//Key = Ops.DeQuote(Key);
          } else {
            String UnQuoted;
            UnQuoted=Ops.DeQuote(tkn.Text);
            SubPhrase = MakeLiteral(UnQuoted, Marker, Marker);// inclusive
            OnePhrase.AddSubPhrase(Key, SubPhrase);
            Key = Empty;
          }
          MarkNext++;
        } else if (tkn.BlockType == TokenType.SingleChar){
          if (tkn.Text.equals(":")){ /* Key=CurrentLiteral; */ KeyOrValue=1; }
          else if (tkn.Text.equals(",")){ Key=null; KeyOrValue=0; }
          MarkNext++;
        } else if (tkn.BlockType == TokenType.Whitespace){/* skip whitespace */ MarkNext++; }
        else {/* skip over everything else */ MarkNext++;}

        Marker = MarkNext;
      } // while (Marker<Chunks.size() && !(tkn = Chunks.get(Marker)).Text.equals(Ender));
      OnePhrase.ChunkEnd = Marker;// inclusive?
      OnePhrase.MyType = Node.Types.IsHash;
    }
    return OnePhrase;
  }
  /* ********************************************************************************************************* */
  ArrayNode Chomp_Array(ArrayList<Token> Chunks, int Marker, int RecurDepth) {
    ArrayNode OnePhrase = null;
    Node SubPhrase = null;
    String Starter="[", Ender="]";
    String results;
    int MarkNext=Marker;
    RecurDepth++;
    Token tkn = Chunks.get(Marker);
    if (tkn.Text.equals(Starter)){
      OnePhrase = new ArrayNode();
      OnePhrase.ChunkStart = Marker;
      OnePhrase.ChildrenArray.clear();// = new ArrayList<Node>();
      Marker++;
      MarkNext=Marker;
      while (Marker<Chunks.size()) {
        tkn = Chunks.get(Marker);
        if (tkn.Text.equals(Ender)){break;}
        if ((SubPhrase = Chomp_HashMap(Chunks,  Marker, RecurDepth))!=null){
          OnePhrase.AddSubPhrase(SubPhrase); MarkNext = SubPhrase.ChunkEnd+1;
        } else if ((SubPhrase = Chomp_Array(Chunks,  Marker, RecurDepth))!=null){
          OnePhrase.AddSubPhrase(SubPhrase); MarkNext = SubPhrase.ChunkEnd+1;
        } else if ((SubPhrase = Chomp_Number(Chunks, Marker, RecurDepth)) != null) {
          OnePhrase.AddSubPhrase(SubPhrase); MarkNext = SubPhrase.ChunkEnd+1;
        } else if ((tkn.BlockType == TokenType.TextString) || (tkn.BlockType == TokenType.Word)){
          results=Ops.DeQuote(tkn.Text);
          SubPhrase = MakeLiteral(results, Marker, Marker);// inclusive
          OnePhrase.AddSubPhrase(SubPhrase);
          MarkNext++;
        } else if (tkn.BlockType == TokenType.SingleChar){
          if (tkn.Text.equals(",")){ }
          MarkNext++;
        } else if (tkn.BlockType == TokenType.Whitespace){/* skip whitespace */ MarkNext++; }
        else {/* skip over everything else */ MarkNext++;}

        Marker = MarkNext;
      }// while (Marker<Chunks.size() && !(tkn = Chunks.get(Marker)).Text.equals(Ender));
      OnePhrase.ChunkEnd = Marker;// inclusive?
      OnePhrase.MyType = Node.Types.IsArray;
    }
    return OnePhrase;
  }
  /* ********************************************************************************************************* */
  public HashNode Parse(String JsonText) {
    this.RootNode = null;//delete 
    Tokenizer tknizer = new Tokenizer();
    ArrayList<Token> Chunks = tknizer.Tokenize(0, JsonText);
    this.RootNode = Fold(Chunks);
    return this.RootNode;
  }
  /* ********************************************************************************************************* */
  public HashNode Fold(ArrayList<Token> Chunks) {
    System.out.printf("\n");
    System.out.printf("-----------------------------------------------------\n");
    int Marker = 0;
    HashNode parent;
    parent = Chomp_HashMap(Chunks, Marker, 0);
    System.out.printf("Done\n");
    String JsonTxt = parent.ToJson();
    // std.cout << "JsonTxt:" << JsonTxt << ":\n";
    return parent;
  }
};

