package voices;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
  public static TokenType[] WordOrPunctuationTarget = {TokenType.SingleChar, TokenType.Word};
  public static String[] Primitives = {"void", "byte", "short", "int", "long", "float", "double", "boolean", "char"};// String? 
  public static String InheritanceFlags[]={"implements", "extends"};// or none
  public static boolean UseNestedInheritances = false;
  /* ********************************************************************************************************* */
  public static MetaFile ParseFile(String FullPath) {
    byte[] encoded = null; String JavaTxt = "";
    try {
      encoded = Files.readAllBytes(Paths.get(FullPath));
      JavaTxt = new String(encoded, StandardCharsets.UTF_8);
    } catch (Exception ex) {
      boolean nop = true;
    }
    String SimpleName;
    //Path p = Paths.get(FullPath); SimpleName = p.getFileName().toString().replace(".java", "");// remove extension
    SimpleName = Paths.get(FullPath).getFileName().toString().replace(".java", "");// remove extension
    ArrayList<Token> Chunks = Tokenizer.Tokenize(0, JavaTxt);
    MetaFile JavaObj = TreeMaker.Chomp_File(Chunks, 0, 0);
    JavaObj.FileName = SimpleName;
//    CppLuggage Luggage = new CppLuggage(); Luggage.Chunks = Chunks;
//    JavaObj.ConvertToCpp(Luggage);
    return JavaObj;
  }
  /* ********************************************************************************************************* */
  public static String Parse(String JavaText, String FileName) {
    ArrayList<Token> Chunks = Tokenizer.Tokenize(0, JavaText);
    System.out.println("");
    System.out.println("-----------------------------------------------------");
    MetaFile parent;
    int Marker = 0;
    parent = TreeMaker.Chomp_File(Chunks, Marker, 0);
    parent.FileName = FileName;
    CppLuggage Luggage = new CppLuggage(); Luggage.Chunks = Chunks;
    parent.ConvertToCpp(Luggage);
    String txt = Tokenizer.Chunks2String(Chunks);
    System.out.println("Done");
    return txt;
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
      // String singles = "}{][)(*&^%$#@!~+=;:>,<|\\?/.-";
      String singles = "}{][)(*&^%$#@!~+=;:>,<|\\?/";
      singles+=".-";// snox
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
      //if ('-' == ch || ch == '.') { return true; }// for numbers snox must break words on . as well
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
    public static boolean IsDecimalPointChar(char ch)
    {// for number punctuation such as '.' - anything else? 
      if (ch == '.') { return true; }
      return false;
    }
    /* ********************************************************************************************************* */
    public static boolean IsNumericPunctuationChar(char ch)
    {// for number punctuation such as '.' and '-' anything else? 
      if ('-' == ch || ch == '.') { return true; }// currently we are sloppy and let gibberish like ".--99.00.-45..88" go through
      return false;
    }
    /* ********************************************************************************************************* */
    public static boolean IsNumericPrefixChar(char ch)
    {
      if (ch == '-' || ch == '.') { return true; }
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
    public static String Chunks2String(ArrayList<Token> Chunks)
    {// for alphanumerics, eg variable names, reserved words, etc.
      StringBuilder sb = new StringBuilder();
      for (int cnt=0; cnt<Chunks.size(); cnt++){
        sb.append(Chunks.get(cnt).Text);
      }
      return sb.toString();
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
  }
    /* ********************************************************************************************************* */
  public static class TreeMaker {
    // <editor-fold defaultstate="collapsed" desc="Tree Creation">
    /* ********************************************************************************************************* */
    public static boolean InList(String text, String[] List){
      if (text==null){return false;}
      int len = List.length;
      for (int cnt=0;cnt<len;cnt++){
        if (text.equals(List[cnt])){ return true; }
      }
      return false;
    }
    /* ********************************************************************************************************* */
    public static Node MakeLiteral(String Text, int ChunkStart, int ChunkEnd){
      Node OnePhrase = new Node();
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
    public static Node Chomp_Number2(ArrayList<Token> Chunks, int Marker, int RecurDepth) {// this is wrong. need to re-think it before using. 
      // chunks are a number if: all chunks are all numeric
      // but not if: numeric chunks end, but next chunk is non-whitespace, non-comma, non-semicolon, and what else? just non-delimiter? 
      // 12.345blah is not a number. maybe let that pass anyway? 123.4f is a number sometimes. 
      // hmm. valid numberenders: ; , []() etc. any single char thing that's not a .  
      // how about a number can end with whitespace or any non-numeric punctation. 
      // well 5-2 is TWO numbers, not one number. we really need to be smarter. 
      Node OnePhrase = null;
      int FirstChunk = Marker, FinalChunk = Marker;
      Token tkn = Chunks.get(Marker);
      String WholeString = "";
      Double number = 0.0;
      char ch = tkn.Text.charAt(0);
      if (Tokenizer.IsNumericPrefixChar(ch) || Tokenizer.IsNumericString(tkn.Text)) {
        WholeString = WholeString.concat(tkn.Text);
        Marker++;
        while (Marker < Chunks.size()) {//  AAAGH this is junk!  
          if ((number = TryParseNumber(WholeString)) != null){
            FinalChunk = Marker;// Just keep going until it does not parse.
          }else{
            break;
          }
          try {
            number = Double.parseDouble(WholeString);
            FinalChunk = Marker;// Just keep going until it does not parse.
          }catch(Exception ex){
            break;
          }
          tkn = Chunks.get(Marker);
          WholeString = WholeString.concat(tkn.Text);
          Marker++;
        }
        OnePhrase = new Node(); OnePhrase.ChunkStart = FirstChunk; OnePhrase.ChunkEnd = FinalChunk;
        OnePhrase.Literal = WholeString;
      }
      return OnePhrase;
    }
    /* ********************************************************************************************************* */
    public static Double TryParseNumber(String NumTxt){
      double number;
      try {
        number = Double.parseDouble(NumTxt);
        return number;// Just keep going until it does not parse.
      }catch(Exception ex){
        return null;
      }
    }
    /* ********************************************************************************************************* */
    public static Node Chomp_Number(ArrayList<Token> Chunks, int Marker, int RecurDepth) {// this is wrong. need to re-think it before using. 
      // chunks are a number if: all chunks are all numeric
      // but not if: numeric chunks end, but next chunk is non-whitespace, non-comma, non-semicolon, and what else? just non-delimiter? 
      // 12.345blah is not a number. maybe let that pass anyway? 123.4f is a number sometimes. 
      // hmm. valid numberenders: ; , []() etc. any single char thing that's not a .  
      // how about a number can end with whitespace or any non-numeric punctation. 
      // well 5-2 is TWO numbers, not one number. we really need to be smarter. 
      Node OnePhrase = null;
      Double number;
      int FirstChunk, FinalChunk = Marker;
      Token tkn = Chunks.get(Marker);
      char ch = tkn.Text.charAt(0);
      if (false){
        if (tkn.BlockType==TokenType.SingleChar){
        } else if (tkn.BlockType==TokenType.Word){
        } else { return null; }
        
        FirstChunk = Marker;
        String WholeString = "";
        if (Tokenizer.IsNumericPrefixChar(tkn.Text.charAt(0))) {
          WholeString = WholeString.concat(tkn.Text); 
          Marker++;
        }
        
        Marker=Skip_Until(Chunks, Marker, WordOrPunctuationTarget);
        if (!(Tokenizer.IsDecimalPointChar(tkn.Text.charAt(0)) || Tokenizer.IsNumericString(tkn.Text))) { return null; }
        FinalChunk = Marker;
        while (Marker < Chunks.size()) {
          tkn = Chunks.get(Marker);
          if (tkn.BlockType==TokenType.SingleChar || tkn.BlockType==TokenType.Word){
            //if (!(IsDecimalPointChar(tkn.Text.charAt(0)) || IsNumericString(tkn.Text) || IsNumericSuffixChar(tkn.Text.charAt(0)))) { break; }
            
            if (!(Tokenizer.IsDecimalPointChar(tkn.Text.charAt(0)))){
              if (!Tokenizer.IsNumericString(tkn.Text)){
                if (!Tokenizer.IsNumericSuffixChar(tkn.Text.charAt(0))){ break; }
              }
            }
            
            WholeString = WholeString.concat(tkn.Text); FinalChunk = Marker;
          }
          Marker++;
        }
        if ((number = TryParseNumber(WholeString))==null){
          return null;
        }
        //number = Double.parseDouble(WholeString);
      }
      
      if (Tokenizer.IsNumericPrefixChar(ch) || Tokenizer.IsNumericString(tkn.Text)) {
        FirstChunk = Marker;
        Marker++;
        String WholeString = "";
        while (Marker < Chunks.size()) {// to do: fix this. as-is will return true if text is empty.
          tkn = Chunks.get(Marker);
          ch = tkn.Text.charAt(0);
          if (!(Tokenizer.IsDecimalPointChar(ch) || Tokenizer.IsNumericString(tkn.Text) || Tokenizer.IsNumericSuffixChar(ch))) { break; }
          FinalChunk = Marker;
          //WholeString = WholeString + tkn.Text;
          WholeString = WholeString.concat(tkn.Text);
          Marker++;
        }
        OnePhrase = new Node(); OnePhrase.ChunkStart = FirstChunk; OnePhrase.ChunkEnd = FinalChunk;
        OnePhrase.Literal = WholeString;
      }
      return OnePhrase;
    }
    /* ********************************************************************************************************* */
    public static Node Chomp_NestedWhatever(ArrayList<Token> Chunks, int Marker, int RecurDepth, String Starter, String Ender){
      Node OnePhrase = null;
      Node SubPhrase = null; // recurse nested curly braces and ignore everything else
      
      RecurDepth++;
      Token tkn = Chunks.get(Marker);
      if (tkn.Text.contains("escaping!")){
        boolean nop = true;  
      }
      if (tkn.Text.equals(Starter)){
        OnePhrase = new Node(new ArrayList<Node>(), Marker);
        Marker++;
        while (Marker<Chunks.size()) {
          tkn = Chunks.get(Marker);
          if (tkn.Text.equals(Ender)){break;}
          if ((SubPhrase = Chomp_NestedWhatever(Chunks,  Marker, RecurDepth, Starter, Ender))!=null){
            OnePhrase.AddSubNode(SubPhrase); Marker = SubPhrase.ChunkEnd; 
          } else {/* skip over everything else */ }
          Marker++;
        }
        OnePhrase.ChunkEnd = Marker;// inclusive 
      }
      return OnePhrase;
    }
    /* ********************************************************************************************************* */
    public static Node Chomp_BracketedWhatever(ArrayList<Token> Chunks, int Marker, int RecurDepth, String Starter, String Ender){// for simple starts and stops
      Node OnePhrase=null;
      RecurDepth++;
      Token tkn = Chunks.get(Marker);
      if (tkn.Text.equals(Starter)){
        OnePhrase = new Node(new ArrayList<Node>(), Marker);
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
    public static Node Chomp_CurlyBraces(ArrayList<Token> Chunks, int Marker, int RecurDepth){
      Node OnePhrase = null;
      Node SubPhrase = null; // recurse nested curly braces and ignore everything else
      
      String Starter="{", Ender="}";
      RecurDepth++;
      OnePhrase = Chomp_NestedWhatever(Chunks,  Marker, RecurDepth, Starter, Ender);
      return OnePhrase;
    }
    /* ********************************************************************************************************* */
    public static Node Chomp_Template(ArrayList<Token> Chunks, int Marker, int RecurDepth){
      Node OnePhrase = null;
      Node SubPhrase = null; // just get through the templates and find the exit
      
      String Starter="<", Ender=">";
      RecurDepth++;
      OnePhrase = Chomp_NestedWhatever(Chunks,  Marker, RecurDepth, Starter, Ender);
      return OnePhrase;
    }
    /* ********************************************************************************************************* */
    public static Node Chomp_EmptySquareBrackets(ArrayList<Token> Chunks, int Marker, int RecurDepth){
      String Starter="[", Ender="]";// just get through the brackets and find the exit
      RecurDepth++;
      System.out.println("Chomp_EmptySquareBrackets");
      return Chomp_BracketedWhatever(Chunks, Marker, RecurDepth, Starter, Ender);
//      Node OnePhrase=null;// just get through the brackets and find the exit
//      Token tkn = Chunks.get(Marker);
//      if (tkn.Text.equals(Starter)){
//        OnePhrase = new Node();
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
    public static MetaVarType Chomp_VarType(ArrayList<Token> Chunks, int Marker, int RecurDepth){
      MetaVarType VarTypeNode=null; Node SubNode=null; // Starts with any word, then consumes any array [] or template <> clause and finishes
      int PrevMark = Marker;
      Token tkn = Chunks.get(Marker);
      if (tkn.BlockType != TokenType.Word){ return null; }// must start with a word
      
      VarTypeNode = new MetaVarType(new ArrayList<Node>(), Marker);
      int FinalWord = Marker;
      if ((SubNode = Chomp_DotWord(Chunks,  Marker, RecurDepth))!=null){// first get literal word part of typedef
        VarTypeNode.AddDotWord(SubNode); FinalWord = Marker = SubNode.ChunkEnd; 
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
          if ((SubNode = Chomp_Template(Chunks,  Marker, RecurDepth))!=null){// jump over templates
            VarTypeNode.AddSubNode(SubNode); FinalWord = Marker = SubNode.ChunkEnd; VarTypeNode.IsTemplated = true;
          } else if ((SubNode = Chomp_EmptySquareBrackets(Chunks,  Marker, RecurDepth))!=null){// jump over array brackets
            VarTypeNode.AddSubNode(SubNode); FinalWord = Marker = SubNode.ChunkEnd; VarTypeNode.IsArray = true;
          } else { }// ignore whitespace, brake on what? brake after 1 word, followed by any or no template/array clauses
          Marker++;
        }
      }
      
      VarTypeNode.Digest(Chunks);
      
      VarTypeNode.ChunkEnd = FinalWord;
      return VarTypeNode;
    }
    /* ********************************************************************************************************* */
    public static ParamListNode Chomp_FnParams(ArrayList<Token> Chunks, int Marker, int RecurDepth){
      ParamListNode ParamList=null; VarPairNode VarPair=null;
      MetaVarType VarTypeNode;
      String Starter="(", Ender=")";
      int MarkNext=Marker;
      RecurDepth++;
      Token tkn = Chunks.get(Marker);
      if (tkn.Text.equals(Starter)){
        ParamList = new ParamListNode(new ArrayList<Node>(), Marker);
        MarkNext = ++Marker;
        boolean IsType = true;
        while (Marker<Chunks.size()) {
          tkn = Chunks.get(Marker);
          if (tkn.Text.equals(Ender)){break;}
          else if (tkn.BlockType == TokenType.Word){
            if (IsType){// parameter data type
              VarPair = new VarPairNode(new ArrayList<Node>(), Marker);
              ParamList.AddVairPair(VarPair);
              if ((VarTypeNode = Chomp_VarType(Chunks,  Marker, RecurDepth))!=null){
                VarPair.AddVarType(VarTypeNode); Marker = VarTypeNode.ChunkEnd; 
              } else {return null;}// should throw 
              IsType = false;
            }else{// parameter variable name
              VarPair.AssignVarName(tkn.Text);// IsType = true;// reset for next
            }
          }else if (tkn.BlockType == TokenType.SingleChar && tkn.Text.equals(",")){
            IsType = true;// reset for next
          }
          Marker++;
        }
        ParamList.ChunkEnd = Marker;
      }
      return ParamList;
    }
    /* ********************************************************************************************************* */
    public static Node Chomp_Preamble(ArrayList<Token> Chunks, int Marker, int RecurDepth){// ready for test?
      String Preludes[]={"@Override","public","private","protected","default","const","abstract"};// ,"static"  or none 
      String Staticness = "static";// or none
      Node OnePhrase = null;
      Token tkn=null;
      int BlockCnt=0;
      int AccessCnt=0, StaticnessCnt=0;
      int FinalWord=Marker;
      
      tkn = Chunks.get(Marker);
      if (TreeMaker.InList(tkn.Text, Preludes)){
        AccessCnt++; FinalWord = Marker;
      }else if(tkn.Text.equals(Staticness)){
        StaticnessCnt++; FinalWord = Marker;
      } else {return null;}
      
      Marker = FinalWord;
      OnePhrase = new Node(new ArrayList<Node>(), Marker);
      Marker++;
      while (Marker<Chunks.size()) {// skip preludes
        tkn = Chunks.get(Marker);
        if (tkn.BlockType == TokenType.Word){// no whitespace, no comments 
          if (TreeMaker.InList(tkn.Text, Preludes)){
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
    public static Node Chomp_Import(ArrayList<Token> Chunks, int Marker, int RecurDepth){// easy, just jump over imports
      String Starter="import", Ender = ";";
      System.out.println("Chomp_Import");
      return Chomp_BracketedWhatever(Chunks, Marker, RecurDepth, Starter, Ender);
    }
    /* ********************************************************************************************************* */
    public static Node Chomp_Package(ArrayList<Token> Chunks, int Marker, int RecurDepth){// easy, just jump over package
      String Starter="package", Ender = ";";
      return Chomp_BracketedWhatever(Chunks, Marker, RecurDepth, Starter, Ender);
    }
    /* ********************************************************************************************************* */
    public static DotWord Chomp_DotWord(ArrayList<Token> Chunks, int Marker, int RecurDepth){// look for variable types such as blah.bleh, blah, blah.bleh.bloo etc. 
      DotWord OnePhrase=null;
      RecurDepth++;
      System.out.println("Chomp_DotWord");
      Token tkn = Chunks.get(Marker);
      if (tkn.BlockType != TokenType.Word){ return null; }// must start with a word
      boolean ReadyToQuit = false;
      int FinalWord = Marker;
      //if (tkn.BlockType == TokenType.Word){
      OnePhrase = new DotWord(new ArrayList<Node>(), Marker);
      while (Marker<Chunks.size()) {
        tkn = Chunks.get(Marker);
        if (tkn.BlockType == TokenType.Word){
          if (ReadyToQuit){ break; }// two words in a row means quit
          OnePhrase.AddSubNode(Node.MakeField(tkn.Text));
          FinalWord = Marker;
          ReadyToQuit = true;
        } else if (tkn.BlockType == TokenType.SingleChar){// look for . 
          if (tkn.Text.equals(".")){
            OnePhrase.AddSubNode(Node.MakeField(tkn.Text)); // do we really want to save the period? 
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
    public static MetaFunction Chomp_Function(ArrayList<Token> Chunks, int Marker, int RecurDepth, MetaClass Parent){// not working yet, just scribbles
      String Starter = "{", Ender = "}";
      System.out.println("Chomp_Function");
      Token tkn;
      tkn = Chunks.get(Marker);// must be on a word to even start
      if (tkn.BlockType != TokenType.Word){return null;}
      
      Node SubPhrase=null;
      MetaFunction FnPhrase = new MetaFunction(new ArrayList<Node>(), Marker);
      if ((FnPhrase.Preamble = Chomp_Preamble(Chunks,  Marker,  RecurDepth))!=null){
        Marker = FnPhrase.Preamble.ChunkEnd + 1;
      }
      
      MetaVarType VarTypeNode;
      // then we chomp variable type
      Marker=Skip_Until(Chunks, Marker, WordTarget);// jump to next word, no whitespace, no comments 
      tkn = Chunks.get(Marker);// this would be Chomp_ReturnType
      FnPhrase.ReturnType=tkn.Text;// not really what we should do. snox
      FnPhrase.ReturnTypeLoc = Marker;
      if (Marker>=Chunks.size()) {return null;}
      if ((VarTypeNode = Chomp_VarType(Chunks,  Marker, RecurDepth))!=null){// now look for templates<>, if any. 
        FnPhrase.AddVarType(VarTypeNode); Marker = VarTypeNode.ChunkEnd; 
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
        if (TreeMaker.InList(FnPhrase.ReturnType, Primitives)){// check if type is in Primitives. 
        }
      }
      FnPhrase.FnNameLoc = Marker; 
      
      // next get parameters
      Marker=Skip_Until(Chunks, Marker, PunctuationTarget);// jump to next punctuation, no whitespace, no comments 
      if (Marker>=Chunks.size()) {return null;}
      ParamListNode Params;
      if ((Params = Chomp_FnParams(Chunks, Marker, RecurDepth))!=null){
        FnPhrase.AddParams(Params); Marker = Params.ChunkEnd;// ends on ) 
      }else{ return null; }
      
      Marker++;// body begins on {
      
      tkn = Chunks.get(Marker);
      
      Marker=Skip_Until(Chunks, Marker, PunctuationTarget);// jump to next punctuation, no whitespace, no comments 
      Node Body;// finally dive into brackets
      if (tkn.Text.equals(";")) {
        FnPhrase.Body = null; FnPhrase.IsStub = true;// function stub
      }else if ((Body = Chomp_CurlyBraces(Chunks, Marker, RecurDepth))!=null){
        FnPhrase.AddBody(Body); Marker = Body.ChunkEnd;
      }
      FnPhrase.ChunkEnd=Marker;
      return FnPhrase;
    }
    /* ********************************************************************************************************* */
    public static MetaInheritance Chomp_Inheritances(ArrayList<Token> Chunks, int Marker, int RecurDepth){
      MetaInheritance WholeInheritance = null; DotWord SubPhrase = null;
      Token tkn;
      tkn = Chunks.get(Marker);
      int FinalWord = Marker;
      if (TreeMaker.InList(tkn.Text, InheritanceFlags)){// "implements" or "extends" triggers inheritance search
        WholeInheritance = new MetaInheritance(Marker);
        Marker++;
        while (Marker<Chunks.size()) {// skip through inheritance and get to brackets
          tkn = Chunks.get(Marker);
          if (tkn.BlockType == TokenType.Word){// no whitespace, no comments 
            if (TreeMaker.InList(tkn.Text, InheritanceFlags)){// ignore "implements" and "extends"
            } else if ((SubPhrase = Chomp_DotWord(Chunks,  Marker, RecurDepth))!=null){ // add to dependency list
              WholeInheritance.AddInheritance(SubPhrase); Marker = FinalWord = SubPhrase.ChunkEnd;
            }
          } else if (tkn.BlockType == TokenType.SingleChar){
            if (!tkn.Text.equals(",")){ break; }// break if not a var type or a comma
          }
          Marker++;
        }
        WholeInheritance.ChunkEnd = FinalWord;
      }
      return WholeInheritance;
    }
    /* ********************************************************************************************************* */
    public static MetaClass Chomp_Class(ArrayList<Token> Chunks, int Marker, int RecurDepth, MetaClass Parent){// this gets the whole class including preamble, right? 
      String Identifier = "class";
      String Starter = "{", Ender = "}";
      MetaClass ClassNode = null;
      Token tkn=null;
      String ClassName="";
      System.out.println("Chomp_Class");
      tkn = Chunks.get(Marker);
      if (tkn.BlockType != TokenType.Word){ return null; }// must start with a word
      
      ClassNode = new MetaClass(new ArrayList<Node>(), Marker);
      
      if ((ClassNode.Preamble = Chomp_Preamble(Chunks,  Marker,  RecurDepth))!=null){
        Marker = ClassNode.Preamble.ChunkEnd + 1;
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
      ClassNode.ClassName=(ClassName=tkn.Text); //OnePhrase.AddSubNode(Node.MakeField(ClassName));
      ClassNode.ClassNameLoc = Marker;

      Marker++;// start at next token
      
      if (UseNestedInheritances){
        Marker=Skip_Until(Chunks, Marker, WordOrPunctuationTarget);
        MetaInheritance Inherits;
        if ((Inherits = Chomp_Inheritances(Chunks,  Marker, RecurDepth))!=null){ // add to dependency list
          ClassNode.AddInheritanceNode(Inherits); Marker = Inherits.ChunkEnd; Marker++;
        }
        tkn = Chunks.get(Marker);
      } else {
        // this would be Chomp_Inheritances - seems ready
        Node SubPhrase = null;
        int FinalWord = Marker;
        while (Marker<Chunks.size()) {// skip through inheritance and get to brackets
          tkn = Chunks.get(Marker);
          if (tkn.BlockType == TokenType.Word){// no whitespace, no comments 
            if (TreeMaker.InList(tkn.Text, InheritanceFlags)){// ignore "implements" and "extends"
            } else if ((SubPhrase = Chomp_DotWord(Chunks,  Marker, RecurDepth))!=null){ // add to dependency list
              ClassNode.AddInheritance(SubPhrase); Marker = FinalWord = SubPhrase.ChunkEnd;
            }
          } else if (tkn.BlockType == TokenType.SingleChar){
            if (tkn.Text.equals(Starter)){ break; }// break if starting { 
          }
          Marker++;
        }
      }
      // should probably bail out here if we didn't find a {
      MetaEnum SubEnum = null;// will become MetaEnum
      MetaClass SubClass = null;
      MetaInterface SubInterface = null;
      MetaFunction SubFunction = null;
      MetaVar SubVar = null;
      int FirstSubNodeLoc = 0; // to do: remember FirstSubNodeLoc
      if (tkn.Text.equals(Starter)){// this would be Chomp_ClassBody
        ClassNode.BodyStartLoc = Marker;
        Marker++;
        while (Marker<Chunks.size()) {// get to brackets and dive in
          tkn = Chunks.get(Marker);
          if (tkn.Text.equals(Ender)){break;}
          if ((SubClass = Chomp_Class(Chunks, Marker, RecurDepth, ClassNode))!=null){
            System.out.println("AddMetaClass"); if (FirstSubNodeLoc==0){FirstSubNodeLoc=SubClass.ChunkStart;}
            ClassNode.AddMetaClass(SubClass); Marker = SubClass.ChunkEnd;
          } else if ((SubInterface = Chomp_Interface(Chunks, Marker, RecurDepth, ClassNode))!=null){
            System.out.println("AddMetaInterface");if (FirstSubNodeLoc==0){FirstSubNodeLoc=SubInterface.ChunkStart;}
            ClassNode.AddMetaInterface(SubInterface); Marker = SubInterface.ChunkEnd;
          } else if ((SubFunction = Chomp_Function(Chunks, Marker, RecurDepth, ClassNode))!=null){
            System.out.println("AddMetaFunction");if (FirstSubNodeLoc==0){FirstSubNodeLoc=SubFunction.ChunkStart;}
            ClassNode.AddMetaFunction(SubFunction); Marker = SubFunction.ChunkEnd;
          } else if ((SubEnum = Chomp_Enum(Chunks, Marker, RecurDepth))!=null){
            System.out.println("AddMetaEnum");if (FirstSubNodeLoc==0){FirstSubNodeLoc=SubEnum.ChunkStart;}
            ClassNode.AddMetaEnum(SubEnum); Marker = SubEnum.ChunkEnd;
          } else if ((SubVar = Chomp_VariableDeclaration(Chunks, Marker, RecurDepth))!=null){
            System.out.println("AddMetaVar");if (FirstSubNodeLoc==0){FirstSubNodeLoc=SubVar.ChunkStart;}
            ClassNode.AddMetaVar(SubVar); Marker = SubVar.ChunkEnd;
          }
          Marker++;
        }
      } else {return null;}// no open bracket found
      ClassNode.FirstSubNodeLoc = FirstSubNodeLoc;
      ClassNode.ChunkEnd = Marker;
      return ClassNode;
    }
    /* ********************************************************************************************************* */
    public static MetaInterface Chomp_Interface(ArrayList<Token> Chunks, int Marker, int RecurDepth, MetaClass Parent){
      String Identifier = "interface";
      String Starter = "{", Ender = "}";
      MetaInterface InterfaceNode = null;
      Token tkn=null;
      String InterfaceName="";
      System.out.println("Chomp_Interface");
      tkn = Chunks.get(Marker);
      if (tkn.BlockType != TokenType.Word){ return null; }// must start with a word
      
      InterfaceNode = new MetaInterface(new ArrayList<Node>(), Marker);
      
      if ((InterfaceNode.Preamble = Chomp_Preamble(Chunks,  Marker,  RecurDepth))!=null){
        Marker = InterfaceNode.Preamble.ChunkEnd + 1;
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
      InterfaceNode.ClassName=(InterfaceName=tkn.Text); //OnePhrase.AddSubNode(Node.MakeField(ClassName));
      InterfaceNode.ClassNameLoc = Marker;

      Marker++;// start at next token
      
      if (UseNestedInheritances){
        Marker=Skip_Until(Chunks, Marker, WordOrPunctuationTarget);
        MetaInheritance Inherits;
        if ((Inherits = Chomp_Inheritances(Chunks,  Marker, RecurDepth))!=null){ // add to dependency list
          InterfaceNode.AddInheritanceNode(Inherits); Marker = Inherits.ChunkEnd; Marker++;
        }
        tkn = Chunks.get(Marker);
      } else {
        // this would be Chomp_Inheritances - seems ready
        Node SubPhrase = null;
        int FinalWord = Marker;
        while (Marker<Chunks.size()) {// skip through inheritance and get to brackets
          tkn = Chunks.get(Marker);
          if (tkn.BlockType == TokenType.Word){// no whitespace, no comments 
            if (TreeMaker.InList(tkn.Text, InheritanceFlags)){// ignore "implements" and "extends"
            } else if ((SubPhrase = Chomp_DotWord(Chunks,  Marker, RecurDepth))!=null){ // add to dependency list
              InterfaceNode.AddInheritance(SubPhrase); Marker = FinalWord = SubPhrase.ChunkEnd;
            }
          } else if (tkn.BlockType == TokenType.SingleChar){
            if (tkn.Text.equals(Starter)){ break; }// break if starting { 
          }
          Marker++;
        }
     }
      // should probably bail out here if we didn't find a {
      MetaEnum SubEnum = null;// will become MetaEnum
      MetaClass SubClass = null;
      MetaInterface SubInterface = null;
      MetaFunction SubFunction = null;
      MetaVar SubVar = null;
      if (tkn.Text.equals(Starter)){// this would be Chomp_ClassBody
        InterfaceNode.BodyStartLoc = Marker;
        Marker++;
        while (Marker<Chunks.size()) {// get to brackets and dive in
          tkn = Chunks.get(Marker);
          if (tkn.Text.equals(Ender)){break;}
          if ((SubClass = Chomp_Class(Chunks, Marker, RecurDepth, InterfaceNode))!=null){
            System.out.println("AddMetaClass");
            InterfaceNode.AddMetaClass(SubClass); Marker = SubClass.ChunkEnd;
          } else if ((SubInterface = Chomp_Interface(Chunks, Marker, RecurDepth, InterfaceNode))!=null){
            System.out.println("AddMetaInterface");
            InterfaceNode.AddMetaInterface(SubInterface); Marker = SubInterface.ChunkEnd;
          } else if ((SubFunction = Chomp_Function(Chunks, Marker, RecurDepth, InterfaceNode))!=null){
            System.out.println("AddMetaFunction");
            InterfaceNode.AddMetaFunction(SubFunction); Marker = SubFunction.ChunkEnd;
          } else if ((SubEnum = Chomp_Enum(Chunks, Marker, RecurDepth))!=null){
            System.out.println("AddMetaEnum");
            InterfaceNode.AddMetaEnum(SubEnum); Marker = SubEnum.ChunkEnd;
          } else if ((SubVar = Chomp_VariableDeclaration(Chunks, Marker, RecurDepth))!=null){
            System.out.println("AddMetaVar");
            InterfaceNode.AddMetaVar(SubVar); Marker = SubVar.ChunkEnd;
          }
          Marker++;
        }
      } else {return null;}// no open bracket found

      InterfaceNode.ChunkEnd = Marker;
      return InterfaceNode;
    }
    /* ********************************************************************************************************* */
    public static VarAssignPair Chomp_VarAssign(ArrayList<Token> Chunks, int Marker, int RecurDepth){// not working yet, just scribbles
      // Chomp_VarAssign, gets a single variable name with or without an assignment. eg VarName, VarName = blah;
      System.out.println("Chomp_VarAssign");
      Token tkn=null;
      VarAssignPair VarPhrase=null;
      Node SubPhrase = null;
      String VarName;
      
      tkn = Chunks.get(Marker);// must be on a word to even start
      if (tkn.BlockType != TokenType.Word){return null;}
      
      VarPhrase = new VarAssignPair(new ArrayList<Node>(), Marker);
      
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
      MetaVar VarPhrase=null; Node SubPhrase=null;
      String StartRay="[", EndRay="]";
      int MarkPrev=Marker;
      RecurDepth++;
      
      tkn = Chunks.get(Marker);// must be on a word to even start
      if (tkn.BlockType != TokenType.Word){return null;}
      
      VarPhrase = new MetaVar(new ArrayList<Node>(), Marker);
      if ((VarPhrase.Preamble = Chomp_Preamble(Chunks,  Marker,  RecurDepth))!=null){
        Marker = VarPhrase.Preamble.ChunkEnd; Marker++;
      }

      //Marker=Skip_Until(Chunks, Marker, WordTarget);// jump to next word
      
      // then we chomp variable type
      MetaVarType VarTypePhrase;
      Marker=Skip_Until(Chunks, Marker, WordTarget);// jump to next word, no whitespace, no comments 
      if (Marker>=Chunks.size()) {return null;}
      if ((VarTypePhrase = Chomp_VarType(Chunks,  Marker, RecurDepth))!=null){// now look for templates<>, if any. 
        VarPhrase.AddVarType(VarTypePhrase); Marker = VarTypePhrase.ChunkEnd; 
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
      
      EnumPhrase = new MetaEnum(new ArrayList<Node>(), Marker);
      
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
      EnumPhrase.EnumName=(EnumName=tkn.Text); //OnePhrase.AddSubNode(Node.MakeField(ClassName));
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
    public static MetaFile Chomp_File(ArrayList<Token> Chunks, int Marker, int RecurDepth){// eat whole file
      Node Package, Import;
      MetaFile MFile = null;
      MetaClass MClass = null;
      MetaInterface MInterface = null;
      Token tkn;
      RecurDepth++;
      
      Marker=Skip_Until(Chunks, Marker, WordTarget);// jump to next word, no whitespace, no comments 
      if (Marker>=Chunks.size()) {return null;}
            
      MFile = new MetaFile();
      MFile.Chunks = Chunks;
      
      if ((Package = Chomp_Package(Chunks,  Marker,  RecurDepth))!=null){// first skip over package
        MFile.AddImport(Package); Marker = Package.ChunkEnd;
      }
      
      Marker++;

      Marker=Skip_Until(Chunks, Marker, WordTarget);// jump to next word, no whitespace, no comments 
      if (Marker>=Chunks.size()) {return null;}
      
      while (Marker<Chunks.size()) {// skip through imports and get to class
        if ((Import = Chomp_Import(Chunks, Marker, RecurDepth))!=null){// first skip over imports
          System.out.println("Import Found");
          MFile.AddImport(Import); Marker = Import.ChunkEnd;
        } else if ((MClass = Chomp_Class(Chunks,  Marker,  RecurDepth, null))!=null){// then dive into class and quit
          System.out.println("Class Found");
          Marker = MClass.ChunkEnd; MFile.AddMetaClass(MClass); break;
        } else if ((MInterface = Chomp_Interface(Chunks,  Marker,  RecurDepth, null))!=null){// then dive into interface and quit
          System.out.println("Interface Found");
          Marker = MInterface.ChunkEnd; MFile.AddMetaInterface(MInterface); break;
        }
        Marker++;
      }
      
      return MFile;
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
  public static class CppLuggage{
    public ArrayList<MetaFile> Files;
    public ArrayList<Token> Chunks;
  }
  public static class Node {// a value that is a hashtable, an array, a literal, or a pointer to a multiply-used item
    public enum Types { None, Class, Interface, Method, Whatever }// Interface is not used yet.
    public Types MyType = Types.None;
    public String MyPhraseName = "***Nothing***";
    public Node Parent = null;
    public int ChunkStart,ChunkEnd;
    public String Literal=null;
    public Node Preamble;
    private ArrayList<Node> ChildrenArray = new ArrayList<Node>();// new ArrayList<Phrase>();
    public Node(){ }
    //public Node(int Marker){ this.ChunkStart = Marker; }
    public Node(ArrayList<Node> ChildArray0, int Marker){
      this.ChunkStart = Marker;
      this.ChildrenArray = ChildArray0;
    }
    public void AddSubNode(Node ChildPhrase){
      if (ChildPhrase==null){
        boolean nop = true;
      }
      this.ChildrenArray.add(ChildPhrase); 
      ChildPhrase.Parent = this;
    }
    public void ConvertToCpp(CppLuggage Luggage){// virtual
    }
    public static Node MakeField(String Value) {
      Node phrase = new Node();
      phrase.Literal = Value;
      return phrase;
    }
    public void BlankMyText(ArrayList<Token> Chunks){
      Token tkn;
      for (int ccnt=this.ChunkStart; ccnt <= this.ChunkEnd; ccnt++){
        tkn = Chunks.get(ccnt); tkn.Text = "";
      }
    }
  }
  /* ********************************************************************************************************* */
  public static class MetaProject extends Node {
    private ArrayList<MetaFile> MetaFileList = new ArrayList<MetaFile>();
    /* ********************************************************************************* */
    public void PortAll() {
      String fdir = new File("").getAbsolutePath();
      String inpath = fdir + "\\src\\voices\\";
      String outpath = fdir + "\\..\\VM\\";

      File fl;
      ArrayList<String> FullPath = new ArrayList<String>();
      String OnePath = "";
      // read in all file info in the directory
      File dir = new File(inpath);
      File[] FileList = dir.listFiles(new FilenameFilter() {
        public boolean accept(File dir, String filename) {
          return filename.endsWith(".java");
        }
      });
      // reduce to a list of just the file names
      int len = FileList.length;
      for (int fcnt = 0; fcnt < len; fcnt++) {
        fl = FileList[fcnt];
        try {
          OnePath = fl.getCanonicalPath();
          FullPath.add(OnePath);
        } catch (Exception ex) {
          boolean nop = true;
        }
      }
      // parse and create the meta file objects
      for (int fcnt = 0; fcnt < len; fcnt++) {
        OnePath = FullPath.get(fcnt);
        JavaParse.MetaFile JavaFile = JavaParse.ParseFile(OnePath);
        MetaFileList.add(JavaFile);
      }
      // convert to cpp/hpp
      JavaParse.CppLuggage Luggage = new JavaParse.CppLuggage();
      Luggage.Files = MetaFileList;
      // Luggage.Chunks = Chunks; // to do: preserve chunks and pass it here.
      for (int fcnt = 0; fcnt < len; fcnt++) {
        JavaParse.MetaFile JavaFile = MetaFileList.get(fcnt);
        JavaFile.ConvertToCpp(Luggage);
        String CppTxt = Tokenizer.Chunks2String(Luggage.Chunks);
        File file = new File(outpath + JavaFile.FileName + ".hpp");
        try {
         FileWriter fileWriter = new FileWriter(file);
         fileWriter.write(CppTxt);
         fileWriter.flush();
         fileWriter.close();
       } catch (IOException e) {
         boolean nop = true;
       }
      }
      boolean nop = true;
    }
    public void AddMetaFile(MetaFile MFile){
      this.MetaFileList.add(MFile); this.AddSubNode(MFile);
    }
    @Override public void ConvertToCpp(CppLuggage Luggage){
      MetaFile MFile; Token tkn;
      ArrayList<Token> Chunks = Luggage.Chunks;
      for (int cnt=0; cnt < this.MetaFileList.size(); cnt++){
        MFile = this.MetaFileList.get(cnt); MFile.ConvertToCpp(Luggage);
      }
    }
  }
  /* ********************************************************************************************************* */
  public static class MetaFile extends Node {
    public String FileName = "";
    private ArrayList<Node> ImportList = new ArrayList<Node>();
    private ArrayList<MetaClass> MetaClassList = new ArrayList<MetaClass>();// really a Java file has only one base class, so a list is overkill
    private ArrayList<MetaInterface> MetaInterfaceList = new ArrayList<MetaInterface>();// really a Java file has only one base class, so a list is overkill
    private ArrayList<Token> Chunks;
    public MetaFile(){ super(); MyPhraseName = "MetaClass"; }
    public MetaFile(ArrayList<Node> ChildArray0, int Marker){
      super(ChildArray0, Marker);
    }
    public void AddImport(Node Import){
      this.ImportList.add(Import); this.AddSubNode(Import);
    }
    public void AddMetaClass(MetaClass MClass){
      this.MetaClassList.add(MClass); this.AddSubNode(MClass);
    }
    private void AddMetaInterface(MetaInterface MInterface) {
      this.MetaInterfaceList.add(MInterface); this.AddSubNode(MInterface);
    }
    @Override public void ConvertToCpp(CppLuggage Luggage){
      Node nd; Token tkn;
      
      Luggage.Chunks = this.Chunks;  //ArrayList<Token> Chunks = Luggage.Chunks;
      
      if (this.Preamble!=null){ this.Preamble.BlankMyText(this.Chunks); } 
//      for (int icnt=0; icnt < this.ImportList.size(); icnt++){
//        nd = this.ImportList.get(icnt); nd.BlankMyText(Chunks);
//      }
      if (this.ImportList.size()>0){
        int Start = this.ImportList.get(0).ChunkStart;
        int End = this.ImportList.get(this.ImportList.size()-1).ChunkEnd;
        for (int ccnt=Start; ccnt <= End; ccnt++){
          tkn = this.Chunks.get(ccnt); tkn.Text = "";
        }
      }
      StringBuilder sb = new StringBuilder();
      sb.append("#ifndef "+this.FileName+"_hpp\n");
      sb.append("#define "+this.FileName+"_hpp\n\n");

      sb.append("#include <iostream>\n");
      sb.append("#include <sstream>  // Required for stringstreams\n");
      sb.append("#include <string>\n");
      sb.append("#include <vector>\n");
      //#include <map>
      sb.append("#include \"Globals.hpp\"\n");
      sb.append("#include \"Wave.hpp\"\n");
      sb.append("#include \"CajaDelimitadora.hpp\"\n");
      //sb.append("#define String std::string\\n");
      //#define ArrayList std::vector
      // #define HashMap std::map
      /*
class Phrase;
class String;
class JsonParse;
class Token;
class StringBuilder;

template <class T>
class ArrayList;

template <class A, class B>
class HashMap;
      */
      tkn = Chunks.get(0); tkn.Text = sb.toString() + tkn.Text;

      for (int cnt=0; cnt < this.MetaClassList.size(); cnt++){
        nd = this.MetaClassList.get(cnt); nd.ConvertToCpp(Luggage);
      }
      for (int cnt=0; cnt < this.MetaInterfaceList.size(); cnt++){
        nd = this.MetaInterfaceList.get(cnt); nd.ConvertToCpp(Luggage);
      }
      tkn = Chunks.get(Chunks.size()-1); 
      tkn.Text = tkn.Text + "\n\n#endif\n";
    }
  }
  /* ********************************************************************************************************* */
  public static class MetaClass extends Node {
    public static String Identifier = "class";
    private ArrayList<MetaClass> MetaClassList = new ArrayList<MetaClass>();
    private ArrayList<MetaInterface> MetaInterfaceList = new ArrayList<MetaInterface>();
    private ArrayList<MetaFunction> MetaFunctionList = new ArrayList<MetaFunction>();
    private ArrayList<MetaVar> MetaVarList = new ArrayList<MetaVar>();
    private ArrayList<MetaEnum> MetaEnumList = new ArrayList<MetaEnum>();
    private ArrayList<Node> AncestorList = new ArrayList<Node>();
    private MetaInheritance Inheritance = null;
    public String ClassName = "";
    public int ClassNameLoc = Integer.MIN_VALUE;
    public int BodyStartLoc, FirstSubNodeLoc = Integer.MIN_VALUE;
    public MetaClass(){ super(); MyPhraseName = "MetaClass"; }
    public MetaClass(ArrayList<Node> ChildArray0, int Marker){
      super(ChildArray0, Marker);
    }
    public void AddMetaClass(MetaClass MClass){
      this.MetaClassList.add(MClass); this.AddSubNode(MClass); 
    }
    public void AddMetaInterface(MetaInterface MInterface){
      this.MetaInterfaceList.add(MInterface); this.AddSubNode(MInterface); 
    }
    public void AddMetaFunction(MetaFunction MFun){
      this.MetaFunctionList.add(MFun); this.AddSubNode(MFun); 
    }
    public void AddMetaVar(MetaVar MVar){
      this.MetaVarList.add(MVar); this.AddSubNode(MVar); 
    }
    public void AddMetaEnum(MetaEnum MEnum){
      this.MetaEnumList.add(MEnum); this.AddSubNode(MEnum); 
    }
    public void AddInheritanceNode(MetaInheritance MInheritance){
      this.Inheritance=MInheritance; this.AddSubNode(MInheritance);
      if (this.Inheritance==null){
        boolean nop = true;
      }
    }
    public void AddInheritance(Node Ancestor){
      this.AncestorList.add(Ancestor); this.AddSubNode(Ancestor); 
    }
    @Override public void ConvertToCpp(CppLuggage Luggage){
      Token tkn;
      int StartDex;
      ArrayList<Token> Chunks = Luggage.Chunks;
      if (this.Preamble!=null){ this.Preamble.BlankMyText(Chunks); }
      
      if (UseNestedInheritances){
        if (this.Inheritance != null){
          this.Inheritance.ConvertToCpp(Luggage);
        }
      }else{
        if (this.AncestorList.size()>0){
          StartDex = this.ClassNameLoc+1;// first token right after the class name

          // to do: we need to re-comma between different ancestors
          int ultimo = this.AncestorList.size()-1;
          Node FinalParent = this.AncestorList.get(ultimo);
          int EndDex = FinalParent.ChunkEnd;

          for (int cnt = StartDex; cnt<=EndDex; cnt++){
            tkn = Chunks.get(cnt);// remove all "implements" and "extends"
            if (TreeMaker.InList(tkn.Text, InheritanceFlags)){ tkn.Text = ""; }
          }
          tkn = Chunks.get(StartDex);
          tkn.Text = ": public" + tkn.Text; // precede with just a colon and 'public'
        }
      }
      
      // to do: insert public: inside top of brackets
      tkn = Chunks.get(this.ChunkStart);
      
      if (this.FirstSubNodeLoc>0){
        tkn = Chunks.get(this.FirstSubNodeLoc-1);
        //if (tkn.BlockType==TokenType.CommentSlash){tkn.Text =  tkn.Text+"\n";}
        tkn.Text =  tkn.Text+"public:\n";// make everything public for now
      }
      tkn = Chunks.get(this.ChunkEnd);
      tkn.Text = tkn.Text + ";";// all classes end with ; 
      
      // now convert all children
      for (int cnt=0; cnt<this.MetaClassList.size();cnt++){
        this.MetaClassList.get(cnt).ConvertToCpp(Luggage);
      }
      for (int cnt=0; cnt<this.MetaInterfaceList.size();cnt++){
        this.MetaInterfaceList.get(cnt).ConvertToCpp(Luggage);
      }
      for (int cnt=0; cnt<this.MetaFunctionList.size();cnt++){
        this.MetaFunctionList.get(cnt).ConvertToCpp(Luggage);
      }
      for (int cnt=0; cnt<this.MetaVarList.size();cnt++){
        this.MetaVarList.get(cnt).ConvertToCpp(Luggage);
      }
      for (int cnt=0; cnt<this.MetaEnumList.size();cnt++){
        this.MetaEnumList.get(cnt).ConvertToCpp(Luggage);
      }
      
    }
  }
  /* ********************************************************************************************************* */
  public static class MetaInterface extends MetaClass {
    // to do: fill this in
    public MetaInterface(){ super(); MyPhraseName = "MetaInterface"; }
    public MetaInterface(ArrayList<Node> ChildArray0, int Marker){
      super(ChildArray0, Marker);
    }
  }
  /* ********************************************************************************************************* */
  public static class MetaInheritance extends Node {
    private ArrayList<Node> AncestorList = new ArrayList<Node>();
    public MetaInheritance(){ super(); MyPhraseName = "MetaInheritance"; }
    public MetaInheritance(int Marker){
      this.ChunkStart = Marker;
    }
    public MetaInheritance(ArrayList<Node> ChildArray0, int Marker){
      super(ChildArray0, Marker);
    }
    public void AddInheritance(Node Ancestor){
      this.AncestorList.add(Ancestor); this.AddSubNode(Ancestor); 
    }
    @Override public void ConvertToCpp(CppLuggage Luggage){
      ArrayList<Token> Chunks = Luggage.Chunks;
      Token tkn;
      if (true){
        for (int cnt=this.ChunkStart;cnt<=this.ChunkEnd;cnt++){// for now, wipe all inheritances
          tkn = Chunks.get(cnt); tkn.Text = "";
        }
      }else{
        int StartDex;
        if (this.AncestorList.size()>0){
          StartDex = this.ChunkStart;// first token right after the class name
          tkn = Chunks.get(StartDex);
          tkn.Text = ": public"; // replace first 'extends' or 'implements' with just a colon and 'public'
          StartDex++;
          // to do: we need to re-comma between different ancestors
          //int ultimo = this.AncestorList.size()-1; Node FinalParent = this.AncestorList.get(ultimo);
          int EndDex = this.ChunkEnd;
          for (int cnt = StartDex; cnt<=EndDex; cnt++){
            tkn = Chunks.get(cnt);// replace all following "implements" and "extends" with commas
            if (TreeMaker.InList(tkn.Text, InheritanceFlags)){ tkn.Text = ","; }
          }
        }
      }
    }
  }
  /* ********************************************************************************************************* */
  public static class MetaEnum extends Node {
    public String EnumName;
    public int EnumNameLoc;
    public MetaEnum(){ super(); MyPhraseName = "MetaEnum"; }
    public MetaEnum(ArrayList<Node> ChildArray0, int Marker){
      super(ChildArray0, Marker);
    }
    @Override public void ConvertToCpp(CppLuggage Luggage){
      ArrayList<Token> Chunks = Luggage.Chunks;
      if (this.Preamble!=null){ this.Preamble.BlankMyText(Chunks); } 
      Token tkn = Chunks.get(this.ChunkEnd);
      tkn.Text = tkn.Text + ";";// all classes end with ; 
    }
  }
  /* ********************************************************************************************************* */
  public static class MetaVarType extends Node {
    public Node DotWordNode=null;
    String DefaultVal = "null";
    public boolean IsVoid = false, IsPrimitive = true, IsTemplated = false, IsArray = false;
    public MetaVarType(){ super(); MyPhraseName = "MetaVarType"; }
    public MetaVarType(ArrayList<Node> ChildArray0, int Marker){ super(ChildArray0, Marker); }
    public void Digest(ArrayList<Token> Chunks){
      int DotWordLoc = this.DotWordNode.ChunkEnd;
      Token tkn = Chunks.get(DotWordLoc);
      if (tkn.Text.equals("void")){ 
        IsVoid = true; 
      }else{
        IsVoid = false;
        if (TreeMaker.InList(tkn.Text, Primitives)){
          IsPrimitive = true; DefaultVal = "0";
        }else{
          IsPrimitive = false; DefaultVal = "nullptr";
        }
      }
    }
    public void ReplaceDot(ArrayList<Token> Chunks){
      Token tkn;
      for (int cnt = this.ChunkStart; cnt<=this.ChunkEnd; cnt++){
        tkn = Chunks.get(cnt);
        if (tkn.Text.equals(".")){ tkn.Text = "::"; }
      }
    }
    public void MakeRefParam(ArrayList<Token> Chunks){// generally just for function params
      Token tkn;
      if (!this.IsPrimitive){
        tkn = Chunks.get(this.ChunkEnd);
        tkn.Text = tkn.Text+"&";
      }
    }
    public void AddDotWord(Node DotWord){
      this.DotWordNode = DotWord; this.AddSubNode(DotWord);
    }
  }
  /* ********************************************************************************************************* */
  public static class MetaFunction extends Node {
    String ReturnType, FnName;
    public int ReturnTypeLoc = Integer.MIN_VALUE, FnNameLoc = Integer.MIN_VALUE;
    MetaVarType VarTypeNode;
    boolean IsConstructor;
    boolean ReturnsVal;
    boolean IsStub;
    private ParamListNode Params; 
    private Node Body;
    public MetaFunction(){ super(); MyPhraseName = "MetaVar"; }
    public MetaFunction(ArrayList<Node> ChildArray0, int Marker){ super(ChildArray0, Marker); }
    public void AddVarType(MetaVarType VarType0){
      this.VarTypeNode = VarType0;
      this.AddSubNode(VarType0);// and ReturnType? 
      //this.ReturnType;
    }
    public void AddParams(ParamListNode Params0){
      this.Params = Params0; this.AddSubNode(Params0);
    }
    public void AddBody(Node Body0){
      this.Body = Body0; this.AddSubNode(Body0); this.IsStub = false;
    }
    @Override public void ConvertToCpp(CppLuggage Luggage){
      Token tkn;
      int Start, End;
      ArrayList<Token> Chunks = Luggage.Chunks;
      
      if (this.Preamble!=null){ this.Preamble.BlankMyText(Chunks); } 
      
      if (this.IsConstructor || this.VarTypeNode.IsVoid){ ReturnsVal = false; }
      else{ ReturnsVal = true; }
      
      if (!(this.IsConstructor || this.VarTypeNode.IsPrimitive)){
        this.VarTypeNode.ReplaceDot(Chunks);
        End = this.VarTypeNode.ChunkEnd;
        tkn = Chunks.get(End); tkn.Text = tkn.Text + "*";
      }
      
      this.Params.ConvertToCpp(Luggage);
      if (this.Body == null){// IsStub
        boolean nop = true;
        tkn = Chunks.get(this.ChunkStart); tkn.Text = "virtual "+tkn.Text;
        tkn = Chunks.get(this.ChunkEnd); tkn.Text = " = 0"+tkn.Text;
      } else {
        Start = this.Body.ChunkStart+1; End = this.Body.ChunkEnd-1;
        for (int cnt=Start; cnt<=End; cnt++){// clear all body content to make this 'virtual'
          tkn = Chunks.get(cnt); tkn.Text = "";
        }
        tkn = Chunks.get(this.Body.ChunkStart);
        if (ReturnsVal){  
          tkn.Text = tkn.Text + " return "+this.VarTypeNode.DefaultVal+"; ";// nullptr
        }
      }
    }
  }
  /* ********************************************************************************************************* */
  public static class VarAssignPair extends Node {
    public String VarName;
    public int VarNameLoc = Integer.MIN_VALUE;
    public VarAssignPair(){ super(); MyPhraseName = "VarAssignPair"; }
    public VarAssignPair(ArrayList<Node> ChildArray0, int Marker){
      super(ChildArray0, Marker);
    }
    @Override public void ConvertToCpp(CppLuggage Luggage){
      ArrayList<Token> Chunks = Luggage.Chunks;
      Token tkn = Chunks.get(this.VarNameLoc);
    }
  }
  /* ********************************************************************************************************* */
  public static class DotWord extends Node {//  dotwords are like: BigClass.NestedClass.TinyClass etc. 
    public DotWord(){ super(); MyPhraseName = "DotWord"; }
    public DotWord(ArrayList<Node> ChildArray0, int Marker){
      super(ChildArray0, Marker);
    }
    @Override public void ConvertToCpp(CppLuggage Luggage){
      ArrayList<Token> Chunks = Luggage.Chunks;
      Token tkn = Chunks.get(this.ChunkStart);
    }
  }
  /* ********************************************************************************************************* */
  public static class MetaVar extends Node {
    MetaVarType VarTypeNode;
    private ArrayList<VarAssignPair> VarAssignList = new ArrayList<VarAssignPair>();
    public MetaVar(){ super(); MyPhraseName = "MetaVar"; }
    public MetaVar(ArrayList<Node> ChildArray0, int Marker){ super(ChildArray0, Marker); }
    public void AddVarType(MetaVarType VarType0){
      this.VarTypeNode = VarType0; this.AddSubNode(VarType0);
      //this.VarType=tkn.Text; 
    }
    public void AddVarAssign(VarAssignPair vap){
      this.VarAssignList.add(vap);  this.AddSubNode(vap);
    }
    @Override public void ConvertToCpp(CppLuggage Luggage){
      ArrayList<Token> Chunks = Luggage.Chunks;
      if (this.Preamble!=null){ this.Preamble.BlankMyText(Chunks); } 
      Token tkn = Chunks.get(this.VarTypeNode.ChunkStart);
      if (TreeMaker.InList(tkn.Text, Primitives)){// check if type is in Primitives. 
      } else {// therefore a class, make it a pointer
        this.VarTypeNode.ReplaceDot(Chunks);
        tkn = Chunks.get(this.VarTypeNode.ChunkEnd);
        tkn.Text = tkn.Text+"*";
      }
    }
  }
  /* ********************************************************************************************************* */
  public static class VarPairNode extends Node {// for parameters
    private MetaVarType VarTypeNode;
    private Node VarNameNode;
    public VarPairNode(){ super(); MyPhraseName = "VarPairNode"; }
    public VarPairNode(ArrayList<Node> ChildArray0, int Marker){ super(ChildArray0, Marker); }
    public void AddVarType(MetaVarType VarType0){
      this.VarTypeNode = VarType0; this.AddSubNode(VarType0);
    }
    public void AssignVarName(String VarNameTxt){
      Node vap = Node.MakeField(VarNameTxt);
      this.VarNameNode = vap;  this.AddSubNode(vap);
    }
    @Override public void ConvertToCpp(CppLuggage Luggage){
      ArrayList<Token> Chunks = Luggage.Chunks;
      this.VarTypeNode.ReplaceDot(Chunks);
      this.VarTypeNode.MakeRefParam(Chunks);
    }
  }
  /* ********************************************************************************************************* */
  public static class ParamListNode extends Node{
    private ArrayList<VarPairNode> VarPairList = new ArrayList<VarPairNode>();
    public ParamListNode(){ super(); MyPhraseName = "ParamListNode"; }
    public ParamListNode(ArrayList<Node> ChildArray0, int Marker){ super(ChildArray0, Marker); }
    public void AddVairPair(VarPairNode VairPair){
      this.VarPairList.add(VairPair); this.AddSubNode(VairPair);
    }
    @Override public void ConvertToCpp(CppLuggage Luggage){
      VarPairNode vpn;
      int len = this.VarPairList.size();
      for (int cnt=0;cnt<len;cnt++){
        vpn = this.VarPairList.get(cnt); vpn.ConvertToCpp(Luggage);
      }
    }
  }
}
/*
rules:
all function and variable class return types become MyClass* pointers - done
implements and extends  become  : - done
end classes with }; - done
strip out all public, private modifiers - done
strip out @Override for now - done
strip out all function guts. if fn returns class then return null. if fn returns primitive num then return 0. boolean returns false;  - done
all classes start with public: - done
put public before first declaration in classes - done
make interface functions go: virtual myfunction() = 0; - done

this. becomes this->
paramtype becomes paramtype& for classes
all type[] MyArray  become  type* MyArray
all type MyArray[] become  type* MyArray
change dotwords to ::
enclose whole file with #ifndef FileName_hpp #define FileName_hpp  #endif

enums should not be pointers
change interface to class
blah::bleh* x = new blah::bleh(); // make the 'new' part have ::  but for now remove all initializations!!!

namespace JsonParse {class Phrase;} // forwards like this

ArrayList<JsonParse::Phrase*>  // use :: and * in templates now

replace null with nullptr

forward declare ALL class parameters and other references
error: expected ';' at end of member declaration *** MEANS PARAMETERS ARE UNDEFINED!!!!!

for now, remove all inheritances
add chomp_inheritances to class and interface

deal with weird MakeArray syntax in ITextable
ArrayList<JsonParse::Phrase*> MakeArray(CollisionLibrary& HitTable, ArrayList<ITextable*>& Things) { return nullptr; };




*/



