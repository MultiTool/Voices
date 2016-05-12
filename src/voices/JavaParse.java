package voices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
/*
Ok the rules:
in a class there can be:
 variables
 methods
 classes
in a method there can be:
 variables

*/
/* ********************************************************************************************************* */
class JavaParse {
  public enum TokenType { None, CommentStar, CommentSlash, Word, Whitespace, SingleChar, TextString }
  public static String Environment_NewLine = "\r\n";
  public static String PhraseEnd=",";
  public static TokenType[] WordTarget = {TokenType.Word};
  public static TokenType[] PunctuationTarget = {TokenType.SingleChar};
  public static String[] Primitives = {"void", "byte", "short", "int", "long", "float", "double", "boolean", "char"};// String? 
  /* ********************************************************************************************************* */
  public static Phrase Parse(String JavaText) {
    ArrayList<Token> Chunks = Tokenizer.Tokenize(0, JavaText);
    System.out.println("");
    System.out.println("-----------------------------------------------------");
    Phrase parent;
    int Marker = 0;
    parent = Tokenizer.Chomp_File(Chunks, Marker, 0);
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
        tkn. Text = chunk; tkn.BlockType = TokenType.CommentStar;
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
      int len = txt.length();
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
      if (StartPlace < loc) {// then we found something
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
          //if (CompareStart(txt, loc0, "\\"+QuoteChar+"") >= 0) { loc0++; }// ignore slash-escaped quotes
          if (CompareStart(txt, loc0, "\\") >= 0) { loc0++; }// ignore slash-escaped anything
          else if (CompareStart(txt, loc0, ""+QuoteChar+""+QuoteChar+"") >= 0) { loc0++; }// ignore double-escaped quotes (only legal in some languages)
          else if (CompareStart(txt, loc0, QuoteChar) >= 0) { loc0++; break; }// we found a closing quote, break.
          //else if (ch == QuoteChar.charAt(0)) { loc0++; break; }
          loc0++;
        }
        if (StartPlace < loc0)
        {// then we found something
          String chunk = txt.substring(StartPlace, loc0);
          tkn = new Token();// { Text = chunk, BlockType = TokenType.TextString };
          tkn. Text = chunk; tkn.BlockType = TokenType.TextString;
          Tokens.add(tkn);
          if (tkn.Text.contains("escaping!")){
            boolean nop = true;  
          }
        }
      }
      return loc0;// escaping!!
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
      String singles = "}{][)(*&^%$#@!~+=;:>,<|\\?/.";
      // int loc = StartPlace;
      Token tkn = null;
      if (StartPlace >= txt.length()) { return StartPlace; }
      char ch = txt.charAt(StartPlace);
      if (singles.indexOf(ch, 0) >= 0) {// then we found something
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
      if ('-' == ch || ch == '.') { return true; }// for numbers snox must break words on . as well
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
    /* ********************************************************************************************************* */
    public static boolean InList(String text,String[] List){
      int len = List.length;
      for (int cnt=0;cnt<len;cnt++){
        if (text.equals(List[cnt])){ return true; }
      }
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
    public static int Skip_Until(ArrayList<Token> Chunks, int Marker, TokenType[] Targets){
      Token tkn=null;// how to indicate failure? 
      TokenType ttype;
      int tcnt;
      int NumTargets = Targets.length;
      while (Marker<Chunks.size()) {// skip everything that does not matter
        tkn = Chunks.get(Marker);
        for (tcnt=0;tcnt<NumTargets;tcnt++){
          ttype = Targets[tcnt];
          if (ttype.equals(tkn.BlockType)){return Marker;}
        }
        Marker++;
      }
      return Marker;// cues up the marker to the object we sought, does not go beyond
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
    public static Phrase Chomp_Number(ArrayList<Token> Chunks, int Marker, int RecurDepth) {// this is wrong. need to re-think it before using. 
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
    public static Phrase Chomp_NestedWhatever(ArrayList<Token> Chunks, int Marker, int RecurDepth, String Starter, String Ender){
      Phrase OnePhrase=null,SubPhrase=null;// recurse nested curly braces and ignore everything else
      RecurDepth++;
      Token tkn = Chunks.get(Marker);
      if (tkn.Text.contains("escaping!")){
        boolean nop = true;  
      }
      if (tkn.Text.equals(Starter)){
        OnePhrase = new Phrase(new ArrayList<Phrase>(), Marker);
        Marker++;
        while (Marker<Chunks.size()) {
          tkn = Chunks.get(Marker);
          if (tkn.Text.equals(Ender)){break;}
          if ((SubPhrase = Chomp_NestedWhatever(Chunks,  Marker, RecurDepth, Starter, Ender))!=null){
            OnePhrase.AddSubPhrase(SubPhrase); Marker = SubPhrase.ChunkEnd; 
          } else {/* skip over everything else */ }
          Marker++;
        }
        OnePhrase.ChunkEnd = Marker;// inclusive 
      }
      return OnePhrase;
    }
    /* ********************************************************************************************************* */
    public static Phrase Chomp_BracketedWhatever(ArrayList<Token> Chunks, int Marker, int RecurDepth, String Starter, String Ender){// for simple starts and stops
      Phrase OnePhrase=null;
      RecurDepth++;
      Token tkn = Chunks.get(Marker);
      if (tkn.Text.equals(Starter)){
        OnePhrase = new Phrase(new ArrayList<Phrase>(), Marker);
        while (Marker<Chunks.size()) {
         tkn = Chunks.get(Marker);
         if (tkn.Text.equals(Ender)){break;}
         Marker++;
        }
        OnePhrase.ChunkEnd = Marker;
      }
      return OnePhrase;
    }
    /* ********************************************************************************************************* */
    public static Phrase Chomp_CurlyBraces(ArrayList<Token> Chunks, int Marker, int RecurDepth){
      Phrase OnePhrase=null,SubPhrase=null;// recurse nested curly braces and ignore everything else
      String Starter="{", Ender="}";
      RecurDepth++;
      OnePhrase = Chomp_NestedWhatever(Chunks,  Marker, RecurDepth, Starter, Ender);
      return OnePhrase;
    }
    /* ********************************************************************************************************* */
    public static Phrase Chomp_Template(ArrayList<Token> Chunks, int Marker, int RecurDepth){
      Phrase OnePhrase=null,SubPhrase=null;// just get through the templates and find the exit
      String Starter="<", Ender=">";
      RecurDepth++;
      OnePhrase = Chomp_NestedWhatever(Chunks,  Marker, RecurDepth, Starter, Ender);
      return OnePhrase;
    }
    /* ********************************************************************************************************* */
    public static Phrase Chomp_EmptySquareBrackets(ArrayList<Token> Chunks, int Marker, int RecurDepth){
      String Starter="[", Ender="]";// just get through the brackets and find the exit
      RecurDepth++;
      System.out.println("Chomp_EmptySquareBrackets");
      return Chomp_BracketedWhatever(Chunks, Marker, RecurDepth, Starter, Ender);
//      Phrase OnePhrase=null;// just get through the brackets and find the exit
//      Token tkn = Chunks.get(Marker);
//      if (tkn.Text.equals(Starter)){
//        OnePhrase = new Phrase();
//        OnePhrase.ChunkStart = Marker;
//        OnePhrase.ChildrenArray = new ArrayList<Phrase>();
//        Marker++;
//        while (Marker<Chunks.size()) {
//          tkn = Chunks.get(Marker);
//          if (tkn.Text.equals(Ender)){break;}
//          else {/* skip over everything else */ Marker++;}// sloppy: no detection of bogus tokens like string literals or reserved words
//        }
//        OnePhrase.ChunkEnd = Marker;
//      }
//      return OnePhrase;
    }
    /* ********************************************************************************************************* */
    public static Phrase Chomp_VarType(ArrayList<Token> Chunks, int Marker, int RecurDepth){
      Phrase OnePhrase=null,SubPhrase=null; // Starts with any word, then consumes any array [] or template <> clause and finishes
      int PrevMark = Marker;
      Token tkn = Chunks.get(Marker);
      if (tkn.BlockType != TokenType.Word){ return null; }// must start with a word
      
      OnePhrase = new Phrase(new ArrayList<Phrase>(), Marker);
      int FinalWord = Marker;
      if ((SubPhrase = Chomp_DotWord(Chunks,  Marker, RecurDepth))!=null){// first get literal word part of typedef
        OnePhrase.ChildrenArray.add(SubPhrase); FinalWord = Marker = SubPhrase.ChunkEnd; 
      } else { return null; }
      
      // next we either encounter a word (variable name) or we encounter puctuation (template or array)
      // so loop while looking for either punctuation or word
      Marker++;
      
      // determine if this has templates or is an array
      boolean IsPunctuated = false;
      while (Marker<Chunks.size()) {
        tkn = Chunks.get(Marker);
        if (tkn.BlockType.equals(TokenType.Word)){
          IsPunctuated = false; break;// var name
        } else if (tkn.BlockType.equals(TokenType.SingleChar)) {
          IsPunctuated = true; break;// punctuation
        }
        Marker++;
      }
      
      if (IsPunctuated){
        while (Marker<Chunks.size()) {// Rule is: find 1 word, then any or no modifiers. 
          tkn = Chunks.get(Marker);
          if (tkn.BlockType == TokenType.Word){ break;}// terminate on any new word that is not in a modifier.
          if ((SubPhrase = Chomp_Template(Chunks,  Marker, RecurDepth))!=null){// jump over templates
            OnePhrase.ChildrenArray.add(SubPhrase); FinalWord = Marker = SubPhrase.ChunkEnd; 
          } else if ((SubPhrase = Chomp_EmptySquareBrackets(Chunks,  Marker, RecurDepth))!=null){// jump over array brackets
            OnePhrase.ChildrenArray.add(SubPhrase); FinalWord = Marker = SubPhrase.ChunkEnd; 
          } else { }// ignore whitespace, brake on what? brake after 1 word, followed by any or no template/array clauses
          Marker++;
        }
      }
      OnePhrase.ChunkEnd = FinalWord;
      return OnePhrase;
    }
    /* ********************************************************************************************************* */
    public static Phrase Chomp_FnParams(ArrayList<Token> Chunks, int Marker, int RecurDepth){
      Phrase OnePhrase=null;
      String Starter="(", Ender=")";
      int MarkNext=Marker;
      RecurDepth++;
      Token tkn = Chunks.get(Marker);
      if (tkn.Text.equals(Starter)){
        OnePhrase = new Phrase(new ArrayList<Phrase>(), Marker);
        MarkNext = ++Marker;
        boolean IsType = true;
        while (Marker<Chunks.size()) {
          tkn = Chunks.get(Marker);
          if (tkn.Text.equals(Ender)){break;}
          else if (tkn.BlockType == TokenType.Word){
            if (IsType){
              // parameter data type
            }else{
              // parameter variable name
            }
            IsType = false;
          }else if (tkn.BlockType == TokenType.SingleChar && tkn.Text.equals(",")){
            IsType = true;// reset for next
          }
          MarkNext++;
          Marker = MarkNext;
        }
        OnePhrase.ChunkEnd = Marker;
      }
      return OnePhrase;
    }
    /* ********************************************************************************************************* */
    public static Phrase Chomp_Preamble(ArrayList<Token> Chunks, int Marker, int RecurDepth){// ready for test?
      String Preludes[]={"@Override","public","private","protected","default"};// ,"static"  or none 
      String Staticness = "static";// or none
      Phrase OnePhrase = null;
      Token tkn=null;
      int BlockCnt=0;
      int AccessCnt=0, StaticnessCnt=0;
      int FinalWord=Marker;
      
      tkn = Chunks.get(Marker);
      if (InList(tkn.Text, Preludes)){
        AccessCnt++; FinalWord = Marker;
      }else if(tkn.Text.equals(Staticness)){
        StaticnessCnt++; FinalWord = Marker;
      } else {return null;}
      
      Marker = FinalWord;
      OnePhrase = new Phrase(new ArrayList<Phrase>(), Marker);
      Marker++;
      while (Marker<Chunks.size()) {// skip preludes
        tkn = Chunks.get(Marker);
        if (tkn.BlockType == TokenType.Word){// no whitespace, no comments 
          if (InList(tkn.Text, Preludes)){
            AccessCnt++; FinalWord = Marker;
          }else if(tkn.Text.equals(Staticness)){
            StaticnessCnt++; FinalWord = Marker;
          } else {break;}// sloppy: could lead with "public private private protected static private static" forever
          BlockCnt++;
        }
        Marker++;
      }
      OnePhrase.ChunkEnd = FinalWord;
      return OnePhrase;
    }
    /* ********************************************************************************************************* */
    public static Phrase Chomp_Import(ArrayList<Token> Chunks, int Marker, int RecurDepth){// easy, just jump over imports
      String Starter="import", Ender = ";";
      System.out.println("Chomp_Import");
      return Chomp_BracketedWhatever(Chunks, Marker, RecurDepth, Starter, Ender);
    }
    /* ********************************************************************************************************* */
    public static Phrase Chomp_Package(ArrayList<Token> Chunks, int Marker, int RecurDepth){// easy, just jump over package
      String Starter="package", Ender = ";";
      return Chomp_BracketedWhatever(Chunks, Marker, RecurDepth, Starter, Ender);
    }
    /* ********************************************************************************************************* */
    public static Phrase Chomp_DotWord(ArrayList<Token> Chunks, int Marker, int RecurDepth){// look for variable types such as blah.bleh, blah, blah.bleh.bloo etc. 
      Phrase OnePhrase=null;
      RecurDepth++;
      System.out.println("Chomp_DotWord");
      Token tkn = Chunks.get(Marker);
      if (tkn.BlockType != TokenType.Word){ return null; }// must start with a word
      boolean ReadyToQuit = false;
      int FinalWord = Marker;
      //if (tkn.BlockType == TokenType.Word){
      OnePhrase = new Phrase(new ArrayList<Phrase>(), Marker);
      while (Marker<Chunks.size()) {
        tkn = Chunks.get(Marker);
        if (tkn.BlockType == TokenType.Word){
          if (ReadyToQuit){ break; }// two words in a row means quit
          OnePhrase.AddSubPhrase(Phrase.MakeField(tkn.Text));
          FinalWord = Marker;
          ReadyToQuit = true;
        } else if (tkn.BlockType == TokenType.SingleChar){// look for . 
          if (tkn.Text.equals(".")){
            OnePhrase.AddSubPhrase(Phrase.MakeField(tkn.Text)); // do we really want to save the period? 
            ReadyToQuit = false; // we hit a dot, keep looking for next word
          } else { break; }// bail out for any SingleChar that is not a period
        } else if (tkn.BlockType == TokenType.TextString){// look for . 
          break;// Quoted text would be an error, but bail out anyway, we got our variable type definition.
        }
        Marker++;
      }
      OnePhrase.ChunkEnd = FinalWord;
      //}
      return OnePhrase;
    }
    /* ********************************************************************************************************* */
    public static MetaClass Chomp_Class(ArrayList<Token> Chunks, int Marker, int RecurDepth, MetaClass Parent){// this gets the whole class including preamble, right? 
      String Identifier = "class";
      String InheritanceFlags[]={"implements", "extends"};// or none
      String Starter = "{", Ender = "}";
      MetaClass ClassPhrase = null;
      Token tkn=null;
      String ClassName="";
      System.out.println("Chomp_Class");
      tkn = Chunks.get(Marker);
      if (tkn.BlockType != TokenType.Word){ return null; }// must start with a word
      
      ClassPhrase = new MetaClass(new ArrayList<Phrase>(), Marker);
      
      if ((ClassPhrase.Preamble = Chomp_Preamble(Chunks,  Marker,  RecurDepth))!=null){
        Marker = ClassPhrase.Preamble.ChunkEnd + 1;
      }
      
      // race ahead to find "class" identifier
      Marker=Skip_Until(Chunks, Marker, WordTarget);// jump to next word, no whitespace, no comments 
      if (Marker>=Chunks.size()) {return null;}
      tkn = Chunks.get(Marker);
      boolean IsClass = false;// this would be Chomp_ClassIdentifier
      if (tkn.Text.equals(Identifier)){IsClass = true; } else { IsClass = false; }
      if (!IsClass) { return null; }     

      Marker++;// start at next token
      
      Marker=Skip_Until(Chunks, Marker, WordTarget);// jump to next word, no whitespace, no comments 
      if (Marker>=Chunks.size()) {return null;}
      tkn = Chunks.get(Marker);// this would be Chomp_ClassName
      ClassPhrase.ClassName=(ClassName=tkn.Text); //OnePhrase.AddSubPhrase(Phrase.MakeField(ClassName));

      Marker++;// start at next token
      
      // this would be Chomp_Inheritances - seems ready
      Phrase SubPhrase = null;
      int FinalWord = Marker;
      while (Marker<Chunks.size()) {// skip through inheritance and get to brackets
        tkn = Chunks.get(Marker);
        if (tkn.BlockType == TokenType.Word){// no whitespace, no comments 
          if (InList(tkn.Text, InheritanceFlags)){// ignore "implements" and "extends"
          } else if ((SubPhrase = Chomp_DotWord(Chunks,  Marker, RecurDepth))!=null){ // add to dependency list
            ClassPhrase.AddInheritance(SubPhrase); Marker = FinalWord = SubPhrase.ChunkEnd;
          }
        } else if (tkn.BlockType == TokenType.SingleChar){
          if (tkn.Text.equals(Starter)){ break; }// break if starting { 
        }
        Marker++;
      }
      
      // should probably bail out here if we didn't find a {
      MetaEnum SubEnum = null;// will become MetaEnum
      MetaClass SubClass = null;
      MetaFunction SubFunction = null;
      MetaVar SubVar = null;
      if (tkn.Text.equals(Starter)){// this would be Chomp_ClassBody
        Marker++;
        while (Marker<Chunks.size()) {// get to brackets and dive in
          tkn = Chunks.get(Marker);
          if (tkn.Text.equals(Ender)){break;}
          if ((SubClass = Chomp_Class(Chunks, Marker, RecurDepth, ClassPhrase))!=null){
            System.out.println("AddMetaClass");
            ClassPhrase.AddMetaClass(SubClass); Marker = SubClass.ChunkEnd;
          } else if ((SubFunction = Chomp_Function(Chunks, Marker, RecurDepth, ClassPhrase))!=null){
            System.out.println("AddMetaFunction");
            ClassPhrase.AddMetaFunction(SubFunction); Marker = SubFunction.ChunkEnd;
          } else if ((SubEnum = Chomp_Enum(Chunks, Marker, RecurDepth))!=null){
            System.out.println("AddMetaEnum");
            ClassPhrase.AddMetaEnum(SubEnum); Marker = SubEnum.ChunkEnd;
          } else if ((SubVar = Chomp_VariableDeclaration(Chunks, Marker, RecurDepth))!=null){
            System.out.println("AddMetaVar");
            ClassPhrase.AddMetaVar(SubVar); Marker = SubVar.ChunkEnd;
          }
          Marker++;
        }
      } else {return null;}// no open bracket found

      ClassPhrase.ChunkEnd = Marker;
      return ClassPhrase;
    }
    /* ********************************************************************************************************* */
    public static MetaFunction Chomp_Function(ArrayList<Token> Chunks, int Marker, int RecurDepth, MetaClass Parent){// not working yet, just scribbles
      String Starter = "{", Ender = "}";
      System.out.println("Chomp_Function");
      Token tkn;
      tkn = Chunks.get(Marker);// must be on a word to even start
      if (tkn.BlockType != TokenType.Word){return null;}
      
      Phrase SubPhrase=null;
      MetaFunction FnPhrase = new MetaFunction(new ArrayList<Phrase>(), Marker);
      if ((FnPhrase.Preamble = Chomp_Preamble(Chunks,  Marker,  RecurDepth))!=null){
        Marker = FnPhrase.Preamble.ChunkEnd + 1;
      }
      
//      String ReturnType;// then we chomp function return type
//      Marker=Skip_Until(Chunks, Marker, WordTarget);// jump to next word, no whitespace, no comments 
//      if (Marker>=Chunks.size()) {return null;}
//      tkn = Chunks.get(Marker);// this would be Chomp_ReturnType
//      FnPhrase.ReturnType=tkn.Text; //OnePhrase.AddSubPhrase(Phrase.MakeField(ClassName));
      
      // then we chomp variable type
      Marker=Skip_Until(Chunks, Marker, WordTarget);// jump to next word, no whitespace, no comments 
      tkn = Chunks.get(Marker);// this would be Chomp_ReturnType
      FnPhrase.ReturnType=tkn.Text;// not really what we should do. snox
      FnPhrase.ReturnTypeLoc = Marker;
      if (Marker>=Chunks.size()) {return null;}
      if ((SubPhrase = Chomp_VarType(Chunks,  Marker, RecurDepth))!=null){// now look for templates<>, if any. 
        FnPhrase.AddVarType(SubPhrase); Marker = SubPhrase.ChunkEnd; 
      } else {return null;}
      
      Marker++;
      
      // determine if this is a constructor
      boolean IsConstructor = false;
      while (Marker<Chunks.size()) {
        tkn = Chunks.get(Marker);
        if (tkn.BlockType.equals(TokenType.Word)){
          IsConstructor = false; break;
        } else if (tkn.Text.equals("(")) {
          IsConstructor = true; break;
        }
        Marker++;
      }
      FnPhrase.IsConstructor = IsConstructor;
      if (Marker>=Chunks.size()) {return null;}
      if (IsConstructor){
        FnPhrase.FnName=FnPhrase.ReturnType;
      }else{
        FnPhrase.FnName = tkn.Text;// this would be Chomp_FnName
        if (InList(FnPhrase.ReturnType, Primitives)){// check if type is in Primitives. 
        }
      }
      FnPhrase.FnNameLoc = Marker; 
      
      // next get parameters
      Marker=Skip_Until(Chunks, Marker, PunctuationTarget);// jump to next punctuation, no whitespace, no comments 
      if (Marker>=Chunks.size()) {return null;}
      Phrase Params;
      if ((Params = Chomp_FnParams(Chunks, Marker, RecurDepth))!=null){
        FnPhrase.AddParams(Params); Marker = Params.ChunkEnd;// ends on ) 
      }else{ return null; }
      
      Marker++;// body begins on {
      
      tkn = Chunks.get(Marker);
      
      Marker=Skip_Until(Chunks, Marker, PunctuationTarget);// jump to next punctuation, no whitespace, no comments 
      Phrase Body;// finally dive into brackets
      if ((Body = Chomp_CurlyBraces(Chunks, Marker, RecurDepth))!=null){
        FnPhrase.AddBody(Body); Marker = Body.ChunkEnd;
      }
      FnPhrase.ChunkEnd=Marker;
      return FnPhrase;
    }
    /* ********************************************************************************************************* */
    public static VarAssignPair Chomp_VarAssign(ArrayList<Token> Chunks, int Marker, int RecurDepth){// not working yet, just scribbles
      // Chomp_VarAssign, gets a single variable name with or without an assignment. eg VarName, VarName = blah;
      System.out.println("Chomp_VarAssign");
      Token tkn=null;
      VarAssignPair VarPhrase=null;
      Phrase SubPhrase = null;
      String VarName;
      
      tkn = Chunks.get(Marker);// must be on a word to even start
      if (tkn.BlockType != TokenType.Word){return null;}
      
      VarPhrase = new VarAssignPair(new ArrayList<Phrase>(), Marker);
      
      // first get var name
      VarPhrase.VarName = VarName = tkn.Text;
      VarPhrase.VarNameLoc = Marker;

      Marker++;// do we need this?

      int FinalChunk = Marker;
      int cnt = 0;
      while (Marker<Chunks.size()) {
        tkn = Chunks.get(Marker);
        if (tkn.Text.equals("=")) { // ignore and jump over for now
          // skip to next ; or ,
        } else if ((SubPhrase = Chomp_Template(Chunks, Marker, RecurDepth))!=null){
          Marker = SubPhrase.ChunkEnd;
        } else if ((SubPhrase = Chomp_CurlyBraces(Chunks, Marker, RecurDepth))!=null){
          Marker = SubPhrase.ChunkEnd;
        } else if (tkn.Text.equals(",")){
           FinalChunk=Marker; break;
        } else if (tkn.Text.equals(";")){
           FinalChunk=Marker; break;
        }
        cnt++;
        Marker++;
      }
      //Chomp_CurlyBraces
      if (Marker>=Chunks.size()){ return null; }
      
      VarPhrase.ChunkEnd = FinalChunk;
      return VarPhrase;
    }
    /* ********************************************************************************************************* */
    public static MetaVar Chomp_VariableDeclaration(ArrayList<Token> Chunks, int Marker, int RecurDepth){// not working yet, just scribbles
      /*
      so variable declarations go:
      <public private protected> <int double string boolean objectname> <<type, type...> for templates> <[] for arrays> 
      <variablename> <=equals <literal, new object(), functioncall()>>
      */
      System.out.println("Chomp_VariableDeclaration");
      Token tkn=null;

      // or just read until semicolon; and NOT method and NOT class
      MetaVar VarPhrase=null; Phrase SubPhrase=null;
      String StartRay="[", EndRay="]";
      int MarkPrev=Marker;
      RecurDepth++;
      
      tkn = Chunks.get(Marker);// must be on a word to even start
      if (tkn.BlockType != TokenType.Word){return null;}
      
      VarPhrase = new MetaVar(new ArrayList<Phrase>(), Marker);
      if ((VarPhrase.Preamble = Chomp_Preamble(Chunks,  Marker,  RecurDepth))!=null){
        Marker = VarPhrase.Preamble.ChunkEnd; Marker++;
      }

      //Marker=Skip_Until(Chunks, Marker, WordTarget);// jump to next word
      
      // then we chomp variable type
      Marker=Skip_Until(Chunks, Marker, WordTarget);// jump to next word, no whitespace, no comments 
      if (Marker>=Chunks.size()) {return null;}
      if ((SubPhrase = Chomp_VarType(Chunks,  Marker, RecurDepth))!=null){// now look for templates<>, if any. 
        VarPhrase.AddVarType(SubPhrase); Marker = SubPhrase.ChunkEnd; 
      } else {return null;}

      Marker++;
      
      VarAssignPair vap;// After getting vartype, get list of variable names with or without assignments. eg VarName1, VarName2 = blah;
      while (Marker<Chunks.size()) {
        tkn = Chunks.get(Marker);
        if ((vap = Chomp_VarAssign(Chunks, Marker, RecurDepth))!=null){
          VarPhrase.AddVarAssign(vap); Marker = vap.ChunkEnd;
          tkn = Chunks.get(Marker);
          if (tkn.Text.equals(";")) { break; }
        }
        Marker++;
      }

      if (Marker >= Chunks.size()){return null;}
      
      // by now tkn.Text MUST be a semicolon ; 
      VarPhrase.ChunkEnd = Marker;
      return VarPhrase;
    }
    /* ********************************************************************************************************* */
    public static MetaEnum Chomp_Enum(ArrayList<Token> Chunks, int Marker, int RecurDepth){// to do: make this
      String Identifier = "enum";
      String Starter = "{", Ender = "}";
      int FinalChunk = 0;
      MetaEnum EnumPhrase = null;
      Token tkn=null;
      String EnumName="";
      System.out.println("Chomp_Enum");
      
      tkn = Chunks.get(Marker);
      if (tkn.BlockType != TokenType.Word){ return null; }// must start with a word
      
      EnumPhrase = new MetaEnum(new ArrayList<Phrase>(), Marker);
      
      if ((EnumPhrase.Preamble = Chomp_Preamble(Chunks,  Marker,  RecurDepth))!=null){
        Marker = EnumPhrase.Preamble.ChunkEnd + 1;
      }
      
      // race ahead to find "enum" identifier
      Marker=Skip_Until(Chunks, Marker, WordTarget);// jump to next word, no whitespace, no comments 
      if (Marker>=Chunks.size()) {return null;}
      tkn = Chunks.get(Marker);
      
      boolean IsEnum = false;// this would be Chomp_ClassIdentifier
      if (tkn.Text.equals(Identifier)){IsEnum = true; } else { IsEnum = false; }
      if (!IsEnum) { return null; }     

      Marker++;// start at next token
      
      Marker=Skip_Until(Chunks, Marker, WordTarget);// jump to next word, no whitespace, no comments 
      if (Marker>=Chunks.size()) {return null;}
      tkn = Chunks.get(Marker);// this would be Chomp_EnumName
      EnumPhrase.EnumName=(EnumName=tkn.Text); //OnePhrase.AddSubPhrase(Phrase.MakeField(ClassName));
      EnumPhrase.EnumNameLoc = Marker;
      
      Marker++;// start at next token. not needed really.
      
      Marker=Skip_Until(Chunks, Marker, PunctuationTarget);// jump to next punctuation, no whitespace, no comments 
      if (Marker>=Chunks.size()) {return null;}
      tkn = Chunks.get(Marker);// this would be Chomp_EnumName
      if (!tkn.Text.equals(Starter)){return null;}
      
      Marker++;// start at next token

//      Marker=Skip_Until(Chunks, Marker, PunctuationTarget);// jump to next punctuation, no whitespace, no comments 
//      if (Marker>=Chunks.size()) {return null;}
//      tkn = Chunks.get(Marker);// this would be Chomp_EnumName
//      if (!tkn.Text.equals(Ender)){return null;}
      
      while (Marker<Chunks.size()) {
        tkn = Chunks.get(Marker);
        if (tkn.Text.equals(Ender)){ FinalChunk=Marker; break; }
        else if (tkn.Text.equals(",")){ }
        Marker++;
      }
      
      if (Marker>=Chunks.size()) {return null;}
      
      EnumPhrase.ChunkEnd = FinalChunk;
      
      return EnumPhrase;
    }
    /* ********************************************************************************************************* */
    public static MetaClass Chomp_File(ArrayList<Token> Chunks, int Marker, int RecurDepth){// eat whole file
      Phrase Package, Import;
      MetaClass MClass = null;
      Token tkn;
      RecurDepth++;
      
      Marker=Skip_Until(Chunks, Marker, WordTarget);// jump to next word, no whitespace, no comments 
      if (Marker>=Chunks.size()) {return null;}
      
      if ((Package = Chomp_Package(Chunks,  Marker,  RecurDepth))!=null){// first skip over package
        Marker = Package.ChunkEnd;
      }
      
      Marker++;

      Marker=Skip_Until(Chunks, Marker, WordTarget);// jump to next word, no whitespace, no comments 
      if (Marker>=Chunks.size()) {return null;}

      while (Marker<Chunks.size()) {// skip through imports and get to class
        if ((Import = Chomp_Import(Chunks, Marker, RecurDepth))!=null){// first skip over imports
          System.out.println("Import Found");
          Marker = Import.ChunkEnd;
        } else if ((MClass = Chomp_Class(Chunks,  Marker,  RecurDepth, null))!=null){// then dive into class and quit
          System.out.println("Class Found");
          Marker = MClass.ChunkEnd; break;
        }
        Marker++;
      }
      return MClass;
    }
    // </editor-fold>
  }
  /* ********************************************************************************************************* */
  public static class Token {
    public String Text;
    public TokenType BlockType;
    public String SpecificType = "None";
    public void ToJava(StringBuilder sb) { sb.append(this.Text); }
    public void ToHpp(StringBuilder sb) { sb.append(this.Text); }
  }
  /* ********************************************************************************************************* */
  public static class Phrase {// a value that is a hashtable, an array, a literal, or a pointer to a multiply-used item
    public enum Types { None, Class, Interface, Method, Whatever }// Interface is not used yet.
    public Types MyType = Types.None;
    public String MyPhraseName = "***Nothing***";
    public Phrase Parent = null;
    public int ChunkStart,ChunkEnd;
    public String Literal=null;
    public Phrase Preamble;
    private ArrayList<Phrase> ChildrenArray = null;// new ArrayList<Phrase>();
    public Phrase(){ }
    //public Phrase(int Marker){ this.ChunkStart = Marker; }
    public Phrase(ArrayList<Phrase> ChildArray0, int Marker){
      this.ChunkStart = Marker;
      this.ChildrenArray = ChildArray0;
    }
    public void AddSubPhrase(Phrase ChildPhrase){
      this.ChildrenArray.add(ChildPhrase);
    }
    public void ConvertToCpp(ArrayList<Token> Chunks){// virtual
    }
    public static Phrase MakeField(String Value) {
      Phrase phrase = new Phrase();
      phrase.Literal = Value;
      return phrase;
    }
  }
  /* ********************************************************************************************************* */
  public static class MetaClass extends Phrase {
    private ArrayList<MetaClass> MetaClassList = new ArrayList<MetaClass>();
    private ArrayList<MetaFunction> MetaFunctionList = new ArrayList<MetaFunction>();
    private ArrayList<MetaVar> MetaVarList = new ArrayList<MetaVar>();
    private ArrayList<MetaEnum> MetaEnumList = new ArrayList<MetaEnum>();
    private ArrayList<Phrase> AncestorList = new ArrayList<Phrase>();
    public String ClassName = "";
    public int ClassNameLoc = Integer.MIN_VALUE;
    public MetaClass(){ super(); MyPhraseName = "MetaClass"; }
    public MetaClass(ArrayList<Phrase> ChildArray0, int Marker){
      super(ChildArray0, Marker);
    }
    public void AddMetaClass(MetaClass MClass){
      this.MetaClassList.add(MClass); this.AddSubPhrase(MClass); 
    }
    public void AddMetaFunction(MetaFunction MFun){
      this.MetaFunctionList.add(MFun); this.AddSubPhrase(MFun); 
    }
    public void AddMetaVar(MetaVar MVar){
      this.MetaVarList.add(MVar); this.AddSubPhrase(MVar); 
    }
    public void AddMetaEnum(MetaEnum MEnum){
      this.MetaEnumList.add(MEnum); this.AddSubPhrase(MEnum); 
    }
    public void AddInheritance(Phrase Ancestor){
      this.AncestorList.add(Ancestor); this.AddSubPhrase(Ancestor); 
    }
    @Override public void ConvertToCpp(ArrayList<Token> Chunks){
    }
  }
  /* ********************************************************************************************************* */
  public static class MetaEnum extends Phrase {
    public String EnumName;
    public int EnumNameLoc;
    public MetaEnum(){ super(); MyPhraseName = "MetaEnum"; }
    public MetaEnum(ArrayList<Phrase> ChildArray0, int Marker){
      super(ChildArray0, Marker);
    }
    @Override public void ConvertToCpp(ArrayList<Token> Chunks){
    }
  }
  /* ********************************************************************************************************* */
  public static class VarAssignPair extends Phrase {
    public String VarName;
    public int VarNameLoc = Integer.MIN_VALUE;
    public VarAssignPair(){ super(); MyPhraseName = "VarAssignPair"; }
    public VarAssignPair(ArrayList<Phrase> ChildArray0, int Marker){
      super(ChildArray0, Marker);
    }
    @Override public void ConvertToCpp(ArrayList<Token> Chunks){
    }
  }
  /* ********************************************************************************************************* */
  public static class MetaFunction extends Phrase {
    String ReturnType, FnName;
    public int ReturnTypeLoc = Integer.MIN_VALUE, FnNameLoc = Integer.MIN_VALUE;
    Phrase VarTypePhrase;
    boolean IsConstructor;
    private Phrase Params, Body;
    public MetaFunction(){ super(); MyPhraseName = "MetaVar"; }
    public MetaFunction(ArrayList<Phrase> ChildArray0, int Marker){ super(ChildArray0, Marker); }
    public void AddVarType(Phrase VarType0){
      this.VarTypePhrase = VarType0; 
      this.AddSubPhrase(VarType0);// and ReturnType? 
      //this.ReturnType;
    }
    public void AddParams(Phrase Params0){
      this.Params = Params0; this.AddSubPhrase(Params0);
    }
    public void AddBody(Phrase Body0){
      this.Body = Body0; this.AddSubPhrase(Body0);
    }
    @Override public void ConvertToCpp(ArrayList<Token> Chunks){
    }
  }
  /* ********************************************************************************************************* */
  public static class MetaVar extends Phrase {
    String VarType, VarName;
    public int VarTypeLoc = Integer.MIN_VALUE, VarNameLoc = Integer.MIN_VALUE;
    Phrase VarTypePhrase;
    private ArrayList<VarAssignPair> VarAssignList = new ArrayList<VarAssignPair>();
    public MetaVar(){ super(); MyPhraseName = "MetaVar"; }
    public MetaVar(ArrayList<Phrase> ChildArray0, int Marker){ super(ChildArray0, Marker); }
    public void AddVarType(Phrase VarType0){
      this.VarTypePhrase = VarType0; this.AddSubPhrase(VarType0);
      //this.VarType=tkn.Text; 
    }
    public void AddVarAssign(VarAssignPair vap){
      this.VarAssignList.add(vap);  this.AddSubPhrase(vap);
    }
    @Override public void ConvertToCpp(ArrayList<Token> Chunks){
    }
  }
}
