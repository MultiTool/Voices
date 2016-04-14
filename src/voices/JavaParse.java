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
class JavaParse
{
  public enum TokenType { None, CommentStar, CommentSlash, Word, Whitespace, SingleChar, TextString }
  public static String Environment_NewLine = "\r\n";
  public static String PhraseEnd=",";
  /* ********************************************************************************************************* */
  public static class Tokenizer
  {
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
      if ((loc1 = CompareStart(txt, StartPlace, "//")) >= 0)
      {
        loc = txt.indexOf(ender0, loc1);
        if (loc >= 0) { loc += ender0.length(); }
        else
        {
          loc = txt.indexOf(ender1, loc1);
          if (loc >= 0) { loc += ender1.length(); } else { loc = txt.length(); }
          String chunk = txt.substring(StartPlace, loc - StartPlace);
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
          if (CompareStart(txt, loc0, "\\"+QuoteChar+"") >= 0) { loc0++; }// ignore slash-escaped quotes
          else if (CompareStart(txt, loc0, ""+QuoteChar+""+QuoteChar+"") >= 0) { loc0++; }// ignore double-escaped quotes (only legal in some languages)
          else { if (ch == '"') { loc0++; break; } }
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
    public static Phrase Chomp_Number(ArrayList<Token> Chunks, int Marker, int RecurDepth)
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
              SubPhrase = MakeLiteral(tkn.Text, Marker, Marker);// inclusive
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
            SubPhrase = MakeLiteral(tkn.Text, Marker, Marker);// inclusive
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
    /* ********************************************************************************************************* */
    public static Phrase Chomp_Template(ArrayList<Token> Chunks, int Marker, int RecurDepth){
      Phrase OnePhrase=null,SubPhrase=null;// just get through the templates and find the exit
      String Starter="<", Ender=">";
      RecurDepth++;
      Token tkn = Chunks.get(Marker);
      if (tkn.Text.equals(Starter)){
        OnePhrase = new Phrase();
        OnePhrase.ChunkStart = Marker;
        OnePhrase.ChildrenArray = new ArrayList<Phrase>();
        Marker++;
        while (Marker<Chunks.size()) {
          tkn = Chunks.get(Marker);
          if (tkn.Text.equals(Ender)){break;}
          if ((SubPhrase = Chomp_Template(Chunks,  Marker, RecurDepth))!=null){// nested templates
            OnePhrase.ChildrenArray.add(SubPhrase); Marker = SubPhrase.ChunkEnd+1; 
          } else {/* skip over everything else */ Marker++;}// sloppy: no detection of bogus tokens like string literals or reserved words
        }
        OnePhrase.ChunkEnd = Marker;// inclusive? 
      }
      return OnePhrase;
    }
    /* ********************************************************************************************************* */
    public static Phrase Chomp_EmptySquareBrackets(ArrayList<Token> Chunks, int Marker, int RecurDepth){
      Phrase OnePhrase=null;// just get through the brackets and find the exit
      String Starter="[", Ender="]";
      RecurDepth++;
      Token tkn = Chunks.get(Marker);
      if (tkn.Text.equals(Starter)){
        OnePhrase = new Phrase();
        OnePhrase.ChunkStart = Marker;
        OnePhrase.ChildrenArray = new ArrayList<Phrase>();
        Marker++;
        while (Marker<Chunks.size()) {
          tkn = Chunks.get(Marker);
          if (tkn.Text.equals(Ender)){break;}
          else {/* skip over everything else */ Marker++;}// sloppy: no detection of bogus tokens like string literals or reserved words
        }
        OnePhrase.ChunkEnd = Marker;
      }
      return OnePhrase;
    }
    /* ********************************************************************************************************* */
    public static Phrase Chomp_VarType(ArrayList<Token> Chunks, int Marker, int RecurDepth){
      Phrase OnePhrase=null,SubPhrase=null; // Starts with any word, then consumes any array [] or template <> clause and finishes
      Token tkn = Chunks.get(Marker);
      if (false){
        TokenType[] Targets = {TokenType.Word};
        Marker = Skip_Until(Chunks, Marker, Targets);
        /*
        it is:
        loop until word
        while not eof {
        .  if word break;
        .  else ignore <> and advance
        .  else ignore [] and advance
        .  else advance; // whitespace, comments etc. 
        }
        now we have our variable type, with or without <> or []
        ack what about that.thing? 
        wordmode = false;
        so Chomp_NameChain(){
        . if word, {
        .  if (wordmode){ break; } // we are already in wordmode and we hit a word, namechain is broken. 
        .  set wordmode=true, advance
        . }
        . else if punto . , set NOT wordmode,advance
        . else if (wordmode){ // if in wordmode and next thing is not a period, break?  not a ' ' either. 
        . }
        }
        */
        // then recognize <> or [] or both, in that order 
        Marker = Skip_Until(Chunks, Marker, Targets);// then skip to next word, which will be the variable name itself
      }
      if (tkn.BlockType == TokenType.Word){
        OnePhrase = new Phrase();
        OnePhrase.ChunkStart = Marker;
        OnePhrase.ChildrenArray = new ArrayList<Phrase>();
        while (Marker<Chunks.size()) {// do not do it this way!  rule is: find 1 word, then any or no modifiers. 
          tkn = Chunks.get(Marker);
          //if (tkn.Text.equals(Ender)){break;}
          if ((SubPhrase = Chomp_Template(Chunks,  Marker, RecurDepth))!=null){// templates
            OnePhrase.ChildrenArray.add(SubPhrase); Marker = SubPhrase.ChunkEnd+1; 
          } else if ((SubPhrase = Chomp_EmptySquareBrackets(Chunks,  Marker, RecurDepth))!=null){// array brackets
            OnePhrase.ChildrenArray.add(SubPhrase); Marker = SubPhrase.ChunkEnd+1; 
          } else {Marker++;}// ignore whitespace, brake on what? brake after 1 word, followed by any or no template/array clauses
          // terminmate not on whitespace, but after we hit the first word terminate on any new word that is not a modifier.
        }
        OnePhrase.ChunkEnd = Marker;
      }
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
        OnePhrase = new Phrase();
        OnePhrase.ChunkStart = Marker;
        OnePhrase.ChildrenHash = new HashMap<String,Phrase>();
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
    public static int Skip_Until(ArrayList<Token> Chunks, int Marker, TokenType[] Targets){
      Token tkn=null;// how to indicate failure? 
      int tcnt;
      int NumTargets = Targets.length;
      //int MarkerNext = Marker;
      while (Marker<Chunks.size()) {// skip everything that does not matter
        tkn = Chunks.get(Marker);
        for (tcnt=0;tcnt<NumTargets;tcnt++){
          if (Targets[tcnt].equals(tkn)){break;}
        }
        Marker++;
      }
      return Marker;// cues up the marker to the object we sought, does not go beyond
    }
    /* ********************************************************************************************************* */
    public static Phrase Chomp_VariableDeclaration(ArrayList<Token> Chunks, int Marker, int RecurDepth){// not working yet, just scribbles
      /*
      so variable declarations go:
      <public private protected> <int double string boolean objectname> <<type, type...> for templates> <[] for arrays> 
      <variablename> <=equals <literal, new object(), functioncall()>>
      */
      String Preludes[]={"public","private","protected","default","static"};// or none
      String Staticness = "static";// or none
      Token tkn=null;
      //CompareStartAny(String txt, Marker, Exposure);
      // or just read until semicolon; and NOT method and NOT class
      Phrase OnePhrase=null,SubPhrase=null;
      OnePhrase = new Phrase();
      String Starter="[", Ender="]";
      int MarkNext=Marker;
      RecurDepth++;
      int BlockCnt=0;
      int AccessCnt=0, StaticnessCnt=0;
      while (Marker<Chunks.size()) {// skip preludes
        tkn = Chunks.get(Marker);
        if (tkn.BlockType == TokenType.Word){// no whitespace, no comments 
          if (InList(tkn.Text, Preludes)){
            AccessCnt++;
          }else if(tkn.Text.equals(Staticness)){
            StaticnessCnt++;
          } else {break;}// sloppy: could lead with "public private private protected static private static" forever
          BlockCnt++;
        }
        Marker++;
      }
      Token TypeTkn=null;
      while (Marker<Chunks.size()) {// now look for variable type, can be anything
        tkn = Chunks.get(Marker);
        if (tkn.BlockType == TokenType.Word){// no whitespace, no comments 
          TypeTkn = tkn;
          BlockCnt++;
          break;
        }
        Marker++;
      }
      TokenType[] Targets = {TokenType.Word, TokenType.SingleChar};
      Marker=Skip_Until( Chunks, Marker, Targets);
      if ((SubPhrase = Chomp_Template(Chunks,  Marker, RecurDepth))!=null){// now look for templates<>, if any. 
        OnePhrase.ChildrenArray.add(SubPhrase); MarkNext = SubPhrase.ChunkEnd+1; 
        BlockCnt++;
        Marker++;
      }
      // next look for array[], if any. 
      
      //None, CommentStar, CommentSlash, Word, Whitespace, SingleChar, TextString }
      /*
      need a chain finder:
      [x, y, z, none] how do you find none? 
      if first is 
      */
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
            SubPhrase = MakeLiteral(tkn.Text, Marker, Marker);// inclusive
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
  public static class Token
  {
    public String Text;
    public TokenType BlockType;
    public String SpecificType = "None";
    public void ToJava(StringBuilder sb) { sb.append(this.Text); }
    public void ToHpp(StringBuilder sb) { sb.append(this.Text); }
  }
  /* ********************************************************************************************************* */
  public static class Phrase
  {// a value that is a hashtable, an array, a literal, or a pointer to a multiply-used item
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
        sb.append(this.Literal);// maybe put quotes around this
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
        Map.Entry<String, Phrase>[] EntRay = (Map.Entry<String, Phrase>[]) Entries.toArray();
        int cnt=0;
        while (cnt<ultimo){
          key = EntRay[cnt].getKey();
          child = EntRay[cnt].getValue();
          sb.append(key);
          sb.append(" : ");
          sb.append(child.ToJson());
          sb.append(", ");
          cnt++;
        }
        key = EntRay[cnt].getKey();
        child = this.ChildrenArray.get(ultimo);
        sb.append(key);
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
}
