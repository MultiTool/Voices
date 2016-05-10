package voices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
/* ********************************************************************************************************* */
class JsonParse {
  public enum TokenType { None, CommentStar, CommentSlash, Word, Whitespace, SingleChar, TextString }
  public static String Environment_NewLine = "\r\n";
  public static String PhraseEnd=",";
  /* ********************************************************************************************************* */
  public static Phrase Parse(String JsonText) {
    ArrayList<Token> Chunks = Tokenizer.Tokenize(0, JsonText);
    System.out.println("");
    System.out.println("-----------------------------------------------------");
    int Marker = 0;
    Phrase parent;
    parent = Tokenizer.Chomp_HashMap(Chunks, Marker, 0);
    System.out.println("Done");
    return parent;
  }
  /* ********************************************************************************************************* */
  public static class Tokenizer {
    // <editor-fold defaultstate="collapsed" desc="Chunk Finders">
    /* ********************************************************************************************************* */
    public static int Chomp_CommentStar(String txt, int StartPlace, ArrayList<Token> Tokens)// more compact approach
    {// for /* comments
      String ender = "*/";
      int loc = StartPlace, loc1;
      Token tkn = null;
      if ((loc1 = CompareStart(txt, StartPlace, "/*")) >= 0)
      {
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
    public static int Chomp_CommentSlash(String txt, int StartPlace, ArrayList<Token> Tokens)// more compact approach
    {// for // comments
      String ender0 = Environment_NewLine;
      String ender1 = "\n";
      int loc = StartPlace, loc1;
      Token tkn = null;
      if ((loc1 = CompareStart(txt, StartPlace, "//")) >= 0)
      {
        loc = txt.indexOf(ender0, loc1);
        if (loc >= 0) { loc += ender0.length(); }
        else
        {
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
    public static int Chomp_Word(String txt, int StartPlace, ArrayList<Token> Tokens)// more compact approach
    {// for alphanumerics, eg variable names, reserved words, etc.
      int loc = StartPlace;
      Token tkn = null;
      char ch = txt.charAt(StartPlace);
      while (loc < txt.length() && IsWordChar(txt.charAt(loc))) { loc++; }
      if (StartPlace < loc)
      {// then we found something
        String chunk = txt.substring(StartPlace, loc);
        tkn = new Token();// { Text = chunk, BlockType = TokenType.Word };
        tkn. Text = chunk; tkn.BlockType = TokenType.Word;
        Tokens.add(tkn);
      }
      return loc;
    }
    /* ********************************************************************************************************* */
    public static int Chomp_Whitespace(String txt, int StartPlace, ArrayList<Token> Tokens)// more compact approach
    {// for whitespace
      int loc = StartPlace;
      Token tkn = null; // by default whitespace ends where text ends
      while (loc < txt.length() && IsBlankChar(txt.charAt(loc))) { loc++; }
      if (StartPlace < loc)
      {// then we found something
        String chunk = txt.substring(StartPlace, loc);
        tkn = new Token();// { Text = chunk, BlockType = TokenType.Whitespace };
        tkn. Text = chunk; tkn.BlockType = TokenType.Whitespace;
        Tokens.add(tkn);
      }
      return loc;
    }
    /* ********************************************************************************************************* */
    public static int Chomp_TextString(String txt,String QuoteChar, int StartPlace, ArrayList<Token> Tokens)// more compact approach
    {// for text strings "blah blah"  
      int loc0 = StartPlace, loc1;
      Token tkn = null;
      if ((loc1 = CompareStart(txt, StartPlace, QuoteChar)) >= 0)
      {
        loc0 = loc1;
        while (loc0 < txt.length())// by default String ends where text ends
        {
          char ch = txt.charAt(loc0);
          if (CompareStart(txt, loc0, "\\") >= 0) { loc0++; }// ignore slash-escaped characters
          else if (CompareStart(txt, loc0, ""+QuoteChar+""+QuoteChar+"") >= 0) { loc0++; }// ignore double-escaped quotes (only legal in some languages)
          else if (CompareStart(txt, loc0, QuoteChar) >= 0) { loc0++; break; }// we found a closing quote, break.
          //else { if (ch == QuoteChar.charAt(0)) { loc0++; break; } }
          loc0++;
        }
        if (StartPlace < loc0)
        {// then we found something
          String chunk = txt.substring(StartPlace, loc0);
          tkn = new Token();// { Text = chunk, BlockType = TokenType.TextString };
          tkn. Text = chunk; tkn.BlockType = TokenType.TextString;
          Tokens.add(tkn);
        }
      }
      return loc0;
    }
    /* ********************************************************************************************************* */
    public static int Chomp_TextStringDoubleQuoted(String txt, int StartPlace, ArrayList<Token> Tokens)// more compact approach
    {// for text strings "blah blah" 
      return Chomp_TextString(txt,"\"", StartPlace, Tokens);
    }
    /* ********************************************************************************************************* */
    public static int Chomp_TextStringSingleQuoted(String txt, int StartPlace, ArrayList<Token> Tokens)// more compact approach
    {// for text strings 'blah blah'
      return Chomp_TextString(txt,"'", StartPlace, Tokens);
    }
    /* ********************************************************************************************************* */
    public static int Chomp_SingleChar(String txt, int StartPlace, ArrayList<Token> Tokens)// more compact approach
    {// for single char such as { or ] or ; etc. 
      // String singles = "}{][)(*&^%$#@!~+=;:.>,<|\\?/-";
      String singles = "}{][)(*&^%$#@!~+=;:>,<|\\?/";
      // int loc = StartPlace;
      Token tkn = null;
      if (StartPlace >= txt.length()) { return StartPlace; }
      char ch = txt.charAt(StartPlace);
      if (singles.indexOf(ch, 0) >= 0)
      {// then we found something
        tkn = new Token();// { Text = ch.toString(), BlockType = TokenType.SingleChar };
        tkn. Text = Character.toString(ch);
        tkn.BlockType = TokenType.SingleChar;
        Tokens.add(tkn);
        StartPlace++;
      }
      return StartPlace;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Literal finders, Is Word, Is Number, etc">
    /* ********************************************************************************************************* */
    public static boolean IsWordChar(char ch)
    {// for alphanumerics, eg variable names, reserved words, etc.
      if ('a' <= ch && ch <= 'z') { return true; }
      if ('A' <= ch && ch <= 'Z') { return true; }
      if ('0' <= ch && ch <= '9') { return true; }
      if ('_' == ch || ch == '@') { return true; }
      if ('-' == ch || ch == '.') { return true; }// for numbers
      return false;
    }
    /* ********************************************************************************************************* */
    public static boolean IsNumericChar(char ch)
    {// for numbers
      if ('0' <= ch && ch <= '9') { return true; }
      if ('-' == ch || ch == '.') { return true; }// currently we are sloppy and let gibberish like ".--99.00.-45..88" go through
      return false;
    }
    /* ********************************************************************************************************* */
    public static boolean IsNumericPunctuationChar(char ch)
    {// for number punctuation such as '.' and '-' anything else? 
      if ('-' == ch || ch == '.') { return true; }// currently we are sloppy and let gibberish like ".--99.00.-45..88" go through
      return false;
    }
    /* ********************************************************************************************************* */
    public static boolean IsNumericSuffixChar(char ch)
    {
      ch = Character.toLowerCase(ch);
      if ('e' == ch || ch == 'f') { return true; }// currently we are sloppy and let gibberish like ".--99.00.-45..88" go through
      return false;
    }
    /* ********************************************************************************************************* */
    public static boolean IsNumericString(String txt) {// for numbers
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
    public static boolean IsBlankChar(char ch)
    {// any whitespace
      if (' ' == ch || ch == '\t' || ch == '\n' || ch == '\r') { return true; }
      return false;
    }
    // </editor-fold>
    /* ********************************************************************************************************* */
    public static int CompareStartStay(String txt, int StartPlace, String target)
    {// look for any substring right at the StartPlace of the text
      int foundloc = StartPlace;// if not found, return the place where we started
      int cnt = StartPlace;
      int tcnt = 0;
      while (cnt < txt.length())// && tcnt < target.length())
      {
        char ch = txt.charAt(cnt);
        if (ch != target.charAt(tcnt)) { break; }
        cnt++; tcnt++;
        if (tcnt >= target.length()) { foundloc = cnt; break; }// return the loc of just past the end of what we found 
      }
      return foundloc;
    }
    /* ********************************************************************************************************* */
    public static int CompareStart(String txt, int StartPlace, String target)
    {// look for any substring right at the StartPlace of the text
      int foundloc = -1;
      int cnt = StartPlace;
      int tcnt = 0;
      while (cnt < txt.length())// && tcnt < target.length())
      {
        char ch = txt.charAt(cnt);
        if (ch != target.charAt(tcnt)) { break; }
        cnt++; tcnt++;
        if (tcnt >= target.length()) { foundloc = cnt; break; }// return the loc of just past the end of what we found 
      }
      return foundloc;
    }
    /* ********************************************************************************************************* */
    public static int CompareStartAny(String txt, int StartPlace, String[] targets)
    {// look for one of any substrings right at the StartPlace of the text
      int foundloc = -1;
      for (String target : targets)
      {
        if ((foundloc = CompareStart(txt, StartPlace, target)) >= 0) { break; }
      }
      return foundloc;
    }
    /* ********************************************************************************************************* */
    public static ArrayList<Token> Tokenize(int StartPlace, String txt)
    {
      ArrayList<Token> Chunks = new ArrayList<Token>();
      int len = txt.length();
      int MarkPrev = 0;
      int MarkNext = 0;
      while (MarkPrev < len)
      {
        if ((MarkNext = Chomp_CommentStar(txt, MarkPrev, Chunks)) > MarkPrev) { }
        else if ((MarkNext = Chomp_CommentSlash(txt, MarkPrev, Chunks)) > MarkPrev) { }
        else if ((MarkNext = Chomp_Word(txt, MarkPrev, Chunks)) > MarkPrev) { }
        else if ((MarkNext = Chomp_Whitespace(txt, MarkPrev, Chunks)) > MarkPrev) { }
        else if ((MarkNext = Chomp_SingleChar(txt, MarkPrev, Chunks)) > MarkPrev) { }
        else if ((MarkNext = Chomp_TextStringDoubleQuoted(txt, MarkPrev, Chunks)) > MarkPrev) { }
        else if ((MarkNext = Chomp_TextStringSingleQuoted(txt, MarkPrev, Chunks)) > MarkPrev) { }
        else { System.out.println("Inconceivable!"); break; }//throw new Exception("Inconceivable!");
        MarkPrev = MarkNext;
      }
      return Chunks;
    }
    // <editor-fold defaultstate="collapsed" desc="Tree Creation">
    /* ********************************************************************************************************* */
    public static Phrase MakeLiteral(String Text, int ChunkStart, int ChunkEnd){
      Phrase OnePhrase = new Phrase();
      OnePhrase.Literal = Text; OnePhrase.ChunkStart=ChunkStart; OnePhrase.ChunkEnd=ChunkEnd;
      return OnePhrase;
    }
    /* ********************************************************************************************************* */
    public static String DeQuote(String Text){
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
    public static String EnQuote(String txt){
      if (txt==null){
        System.out.println("txt");
      }
      txt = txt.replace("\"", "\"\"");
      return "\"" + txt + "\"";
    }
    /* ********************************************************************************************************* */
    public static Phrase Chomp_NumberX(ArrayList<Token> Chunks, int Marker, int RecurDepth)
    {// this is wrong. need to re-think it before using. 
      // chunks are a number if: all chunks are all numeric
      // but not if: numeric chunks end, but next chunk is non-whitespace, non-comma, non-semicolon, and what else? just non-delimiter? 
      // 12.345blah is not a number. maybe let that pass anyway? 123.4f is a number sometimes. 
      // hmm. valid numberenders: ; , []() etc. any single char thing that's not a .  
      // how about a number can end with whitespace or any non-numeric punctation. 
      Phrase OnePhrase=null;
      Token tkn = Chunks.get(Marker);
      char ch = tkn.Text.charAt(0);
      if (IsNumericPunctuationChar(ch) || IsNumericString(tkn.Text)){
        int MarkNext = ++Marker;
        String WholeString = "";
        while (MarkNext<Chunks.size()){// to do: fix this. as-is will return true if text is empty.
          tkn = Chunks.get(MarkNext);
          ch = tkn.Text.charAt(0);
          if (!(IsNumericPunctuationChar(ch) || IsNumericString(tkn.Text) || IsNumericSuffixChar(ch))){ break; }
          WholeString = WholeString.concat(tkn.Text);
          MarkNext++;
        }
        OnePhrase = new Phrase(); OnePhrase.ChunkStart = Marker; OnePhrase.ChunkEnd = MarkNext-1;
        OnePhrase.Literal = WholeString;
      }
      return OnePhrase;
    }
    /* ********************************************************************************************************* */
    public static Phrase Chomp_Number(ArrayList<Token> Chunks, int Marker, int RecurDepth)
    {// this is wrong. need to re-think it before using. 
      // chunks are a number if: all chunks are all numeric
      // but not if: numeric chunks end, but next chunk is non-whitespace, non-comma, non-semicolon, and what else? just non-delimiter? 
      // 12.345blah is not a number. maybe let that pass anyway? 123.4f is a number sometimes. 
      // hmm. valid numberenders: ; , []() etc. any single char thing that's not a .  
      // how about a number can end with whitespace or any non-numeric punctation. 
      Phrase OnePhrase = null;
      int FirstChunk, FinalChunk = Marker;
      Token tkn = Chunks.get(Marker);
      char ch = tkn.Text.charAt(0);
      if (IsNumericPunctuationChar(ch) || IsNumericString(tkn.Text)) {
        FirstChunk = Marker;
        String WholeString = "";
        while (Marker < Chunks.size()) {// to do: fix this. as-is will return true if text is empty.
          tkn = Chunks.get(Marker);
          ch = tkn.Text.charAt(0);
          if (!(IsNumericPunctuationChar(ch) || IsNumericString(tkn.Text) || IsNumericSuffixChar(ch))) { break; }
          FinalChunk = Marker;
          WholeString = WholeString + tkn.Text;
          Marker++;
        }
        OnePhrase = new Phrase(); OnePhrase.ChunkStart = FirstChunk; OnePhrase.ChunkEnd = FinalChunk;
        OnePhrase.Literal = WholeString;
      }
      return OnePhrase;
    }
    /* ********************************************************************************************************* */
    public static Phrase Chomp_HashMap(ArrayList<Token> Chunks, int Marker, int RecurDepth){
      Phrase OnePhrase=null,SubPhrase=null;
      String Starter="{", Ender="}";
      int MarkNext=Marker;
      int KeyOrValue=0;
      String Key=null;
      RecurDepth++;
      Token tkn = Chunks.get(Marker);
      if (tkn.Text.equals(Starter)){
        OnePhrase = new Phrase();
        OnePhrase.ChunkStart = Marker;
        OnePhrase.ChildrenHash = new HashMap<String,Phrase>();
        MarkNext = ++Marker;
        while (Marker<Chunks.size()) {
          tkn = Chunks.get(Marker);
          if (tkn.Text.equals(Ender)){break;}
          else if ((SubPhrase = Chomp_HashMap(Chunks,  Marker, RecurDepth))!=null){
            OnePhrase.ChildrenHash.put(Key, SubPhrase); Key=null; MarkNext = SubPhrase.ChunkEnd+1; 
          } else if ((SubPhrase = Chomp_Array(Chunks,  Marker, RecurDepth))!=null){
            OnePhrase.ChildrenHash.put(Key, SubPhrase); Key=null; MarkNext = SubPhrase.ChunkEnd+1; 
          } else if ((SubPhrase = Chomp_Number(Chunks, Marker, RecurDepth)) != null) {
            OnePhrase.ChildrenHash.put(Key, SubPhrase); Key=null; MarkNext = SubPhrase.ChunkEnd+1;
          } else if ((tkn.BlockType == TokenType.TextString) || (tkn.BlockType == TokenType.Word)) {
            if (KeyOrValue == 0) {
              Key = tkn.Text;
              if (tkn.BlockType == TokenType.TextString) { Key = DeQuote(Key); }
            } else {
              SubPhrase = MakeLiteral(DeQuote(tkn.Text), Marker, Marker);// inclusive
              OnePhrase.ChildrenHash.put(Key, SubPhrase);
              Key = null;
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
      }
      return OnePhrase;
    }
    /* ********************************************************************************************************* */
    public static Phrase Chomp_Array(ArrayList<Token> Chunks, int Marker, int RecurDepth){
      Phrase OnePhrase=null,SubPhrase=null;
      String Starter="[", Ender="]";
      int MarkNext=Marker;
      RecurDepth++;
      Token tkn = Chunks.get(Marker);
      if (tkn.Text.equals(Starter)){
        OnePhrase = new Phrase();
        OnePhrase.ChunkStart = Marker;
        OnePhrase.ChildrenArray = new ArrayList<Phrase>();
        Marker++;
        MarkNext=Marker;
        while (Marker<Chunks.size()) {
          tkn = Chunks.get(Marker);
          if (tkn.Text.equals(Ender)){break;}
          if ((SubPhrase = Chomp_HashMap(Chunks,  Marker, RecurDepth))!=null){
            OnePhrase.ChildrenArray.add(SubPhrase); MarkNext = SubPhrase.ChunkEnd+1; 
          } else if ((SubPhrase = Chomp_Array(Chunks,  Marker, RecurDepth))!=null){
            OnePhrase.ChildrenArray.add(SubPhrase); MarkNext = SubPhrase.ChunkEnd+1; 
          } else if ((SubPhrase = Chomp_Number(Chunks, Marker, RecurDepth)) != null) {
            OnePhrase.ChildrenArray.add(SubPhrase); MarkNext = SubPhrase.ChunkEnd+1;
          } else if ((tkn.BlockType == TokenType.TextString) || (tkn.BlockType == TokenType.Word)){
            SubPhrase = MakeLiteral(DeQuote(tkn.Text), Marker, Marker);// inclusive
            OnePhrase.ChildrenArray.add(SubPhrase);
            MarkNext++;
          } else if (tkn.BlockType == TokenType.SingleChar){
            if (tkn.Text.equals(",")){ }
            MarkNext++;
          } else if (tkn.BlockType == TokenType.Whitespace){/* skip whitespace */ MarkNext++; }  
          else {/* skip over everything else */ MarkNext++;}

          Marker = MarkNext;
        }// while (Marker<Chunks.size() && !(tkn = Chunks.get(Marker)).Text.equals(Ender));
        OnePhrase.ChunkEnd = Marker;// inclusive? 
      }
      return OnePhrase;
    }
    // </editor-fold>
    /* ********************************************************************************************************* */
    public static Phrase Fold(ArrayList<Token> Chunks) {
      System.out.println("");
      System.out.println("-----------------------------------------------------");
      int Marker = 0;
      Phrase parent;
      parent = Chomp_HashMap(Chunks, Marker, 0);
      System.out.println("Done");
      return parent;
    }
  }
  /* ********************************************************************************************************* */
  public static class Token {
    public String Text;
    public TokenType BlockType;
    public String SpecificType = "None";
    public void ToJava(StringBuilder sb) { sb.append(this.Text); }
    public void ToHpp(StringBuilder sb) { sb.append(this.Text); }
    public String DeQuoted() { return Tokenizer.DeQuote(this.Text); }
  }
  /* ********************************************************************************************************* */
  public static class Phrase {// a value that is a hashtable, an array, a literal, or a pointer to a multiply-used item
    public enum Types { None, Class, Interface, Method, Whatever }// Interface is not used yet.
    public Types MyType = Types.None;
    public String MyPhraseName = "***Nothing***";
    public Phrase Parent = null;
    public int ChunkStart,ChunkEnd;
    public String ItemPtr=null;
    public String Literal=null;
    public ArrayList<Phrase> ChildrenArray = null;
    public HashMap<String,Phrase> ChildrenHash = null;
    public String ToJson(){
      StringBuilder sb = new StringBuilder();
      if (this.ChildrenHash!=null){
        sb.append(ToHash());
      }else if (this.ChildrenArray!=null){
        sb.append(ToArray());
      }else if (this.Literal!=null){
        sb.append(Tokenizer.EnQuote(this.Literal));// maybe put quotes around this
        //sb.append(this.Literal);// maybe put quotes around this
      }else if (this.ItemPtr!=null){
        sb.append(this.ItemPtr);// maybe put quotes around this
      }
      return sb.toString();
    }
    public String ToHash(){
      Phrase child;
      StringBuilder sb = new StringBuilder();
      sb.append("{");
      int len = this.ChildrenHash.size();
      int ultimo = len-1;
      String key;
      this.ChildrenHash.keySet().toArray();
      if (0<len){
        Set<Map.Entry<String, Phrase>> Entries = this.ChildrenHash.entrySet();
        Object[] objray = Entries.toArray();
        //Map.Entry<String, Phrase>[] EntRay = (Map.Entry<String, Phrase>[]) Entries.toArray();
        int cnt=0;
        while (cnt<ultimo){
          Map.Entry<String, Phrase> entry = (Map.Entry<String, Phrase>) objray[cnt];
          key = entry.getKey();
          child = entry.getValue();
          sb.append(Tokenizer.EnQuote(key));
          sb.append(" : ");
          sb.append(child.ToJson());
          sb.append(", ");
          cnt++;
        }
        key = ((Map.Entry<String, Phrase>)objray[cnt]).getKey();
        child = this.ChildrenHash.get(key);
        sb.append(Tokenizer.EnQuote(key));
        sb.append(" : ");
        sb.append(child.ToJson());
      }
      sb.append("}");
      return sb.toString();
    }
    public String ToArray(){
      Phrase child;
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      int len = this.ChildrenArray.size();
      int ultimo = len-1;
      if (0<len){
        int cnt=0;
        while (cnt<ultimo){
          child = this.ChildrenArray.get(cnt);
          sb.append(child.ToJson());
          sb.append(", ");
          cnt++;
        }
        child = this.ChildrenArray.get(ultimo);
        sb.append(child.ToJson());
      }
      sb.append("]");
      return sb.toString();
    }
  }
  /* ********************************************************************************************************* */
  public static class PhraseBase {// a value that is a hashtable, an array, a literal, or a pointer to a multiply-used item
    public enum Types { None, Class, Interface, Method, Whatever }// Interface is not used yet.
    public Types MyType = Types.None;
    public String MyPhraseName = "***Nothing***";
    public PhraseBase Parent = null;
    public int ChunkStart, ChunkEnd;
    public String ToJson() { return ""; }// virtual
  }
  /* ********************************************************************************************************* */
  public static class LiteralPhrase extends PhraseBase {// a value that is a literal
    private String Literal = null;
    public String Get() { return this.Literal; }
    @Override public String ToJson() { return Tokenizer.EnQuote(this.Get()); }
  }
  /* ********************************************************************************************************* */
  public static class HashPhrase extends PhraseBase
  {// a value that is a hashtable, an array, a literal, or a pointer to a multiply-used item
    private HashMap<String, PhraseBase> ChildrenHash = new HashMap<String, PhraseBase>();
    public void AddSubPhrase(String Name, PhraseBase ChildPhrase)
    {
      this.ChildrenHash.put(Name, ChildPhrase); ChildPhrase.Parent = this;
    }
    public PhraseBase Get(String Name)
    {
      if (this.ChildrenHash.containsKey(Name)) { return this.ChildrenHash.get(Name); }
      else { return null; }
    }
    public String ToHash()
    {
      PhraseBase child;
      StringBuilder sb = new StringBuilder();
      sb.append("{");
      int len = this.ChildrenHash.size();
      int ultimo = len-1;
      String key;
      this.ChildrenHash.keySet().toArray();
      if (0<len){
        Set<Map.Entry<String, PhraseBase>> Entries = this.ChildrenHash.entrySet();
        Object[] objray = Entries.toArray();
        int cnt=0;
        while (cnt<ultimo){
          Map.Entry<String, PhraseBase> entry = (Map.Entry<String, PhraseBase>) objray[cnt];
          key = entry.getKey(); child = entry.getValue();
          sb.append(Tokenizer.EnQuote(key)); sb.append(" : "); sb.append(child.ToJson());
          sb.append(", ");
          cnt++;
        }
        key = ((Map.Entry<String, Phrase>)objray[cnt]).getKey();
        child = this.ChildrenHash.get(key);
        sb.append(Tokenizer.EnQuote(key)); sb.append(" : "); sb.append(child.ToJson());
      }
      sb.append("}");
      return sb.toString();
    }
    @Override public String ToJson() { return this.ToHash(); }
  }
  /* ********************************************************************************************************* */
  public static class ArrayPhrase extends PhraseBase
  {// a value that is an array
    private ArrayList<PhraseBase> ChildrenArray = new ArrayList<PhraseBase>();
    public void AddSubPhrase(PhraseBase ChildPhrase) {
        this.ChildrenArray.add(ChildPhrase); ChildPhrase.Parent = this;
    }
    public PhraseBase Get(int Dex){ return this.ChildrenArray.get(Dex);}
    public String ToArray() {
      PhraseBase child;
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      int len = this.ChildrenArray.size();
      int ultimo = len - 1;
      if (0 < len) {
        int cnt = 0;
        while (cnt < ultimo) {
          child = this.ChildrenArray.get(cnt); sb.append(child.ToJson());
          sb.append(", ");
          cnt++;
        }
        child = this.ChildrenArray.get(ultimo); sb.append(child.ToJson());
      }
      sb.append("]");
      return sb.toString();
    }
    @Override public String ToJson() { return this.ToArray(); }
  }
}
