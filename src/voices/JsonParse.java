package geocodegetter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
    class JsonParse
    {
        public enum TokenType { None, CommentStar, CommentSlash, Word, Whitespace, SingleChar, TextString }
        public static String Environment_NewLine = "\r\n";
        public static String PhraseEnd=",";
        /* ********************************************************************************************************* */
        public static class Tokenizer
        {
            // <editor-fold defaultstate="collapsed" desc="Chunk Finders">
            /* ********************************************************************************************************* */
            public static int FindEnd_CommentStar(String txt, int StartPlace, ArrayList<Token> Tokens)// more compact approach
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
            public static int FindEnd_CommentSlash(String txt, int StartPlace, ArrayList<Token> Tokens)// more compact approach
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
            public static int FindEnd_Word(String txt, int StartPlace, ArrayList<Token> Tokens)// more compact approach
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
            public static int FindEnd_Whitespace(String txt, int StartPlace, ArrayList<Token> Tokens)// more compact approach
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
            public static int FindEnd_TextString(String txt,String QuoteChar, int StartPlace, ArrayList<Token> Tokens)// more compact approach
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
            public static int FindEnd_TextStringDoubleQuoted(String txt, int StartPlace, ArrayList<Token> Tokens)// more compact approach
            {// for text strings "blah blah" 
              return FindEnd_TextString(txt,"\"", StartPlace, Tokens);
            }
            /* ********************************************************************************************************* */
            public static int FindEnd_TextStringSingleQuoted(String txt, int StartPlace, ArrayList<Token> Tokens)// more compact approach
            {// for text strings 'blah blah'
              return FindEnd_TextString(txt,"'", StartPlace, Tokens);
            }
            /* ********************************************************************************************************* */
            public static int FindEnd_SingleChar(String txt, int StartPlace, ArrayList<Token> Tokens)// more compact approach
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
                if ('-' == ch || ch == '.') { return true; }
                return false;
            }
            /* ********************************************************************************************************* */
            public static boolean IsBlankChar(char ch)
            {// any whitespace
                if (' ' == ch || ch == '\t' || ch == '\n' || ch == '\r') { return true; }
                return false;
            }
            /* ********************************************************************************************************* */
            public static int CompareStart3(String txt, int StartPlace, String target)
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
                    if ((MarkNext = FindEnd_CommentStar(txt, MarkPrev, Chunks)) > MarkPrev) { }
                    else if ((MarkNext = FindEnd_CommentSlash(txt, MarkPrev, Chunks)) > MarkPrev) { }
                    else if ((MarkNext = FindEnd_Word(txt, MarkPrev, Chunks)) > MarkPrev) { }
                    else if ((MarkNext = FindEnd_Whitespace(txt, MarkPrev, Chunks)) > MarkPrev) { }
                    else if ((MarkNext = FindEnd_SingleChar(txt, MarkPrev, Chunks)) > MarkPrev) { }
                    else if ((MarkNext = FindEnd_TextStringDoubleQuoted(txt, MarkPrev, Chunks)) > MarkPrev) { }
                    else if ((MarkNext = FindEnd_TextStringSingleQuoted(txt, MarkPrev, Chunks)) > MarkPrev) { }
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
            public static Phrase FindClauseHashMap(ArrayList<Token> Chunks, int Marker, int RecurDepth)
            {
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
                OnePhrase.ChildrenHash = new HashMap<>();
                Marker++;
                MarkNext=Marker;
                while (Marker<Chunks.size()) {
                  tkn = Chunks.get(Marker);
                  if (tkn.Text.equals(Ender)){break;}
                  else if ((SubPhrase = FindClauseHashMap(Chunks,  Marker, RecurDepth))!=null){
                    OnePhrase.ChildrenHash.put(Key, SubPhrase); Key=null; MarkNext = SubPhrase.ChunkEnd+1; 
                  }
                  else if ((SubPhrase = FindClauseArray(Chunks,  Marker, RecurDepth))!=null){
                    OnePhrase.ChildrenHash.put(Key, SubPhrase); Key=null; MarkNext = SubPhrase.ChunkEnd+1; 
                  }
                  else if ((tkn.BlockType == TokenType.TextString) || (tkn.BlockType == TokenType.Word)){
                    if (KeyOrValue==0){
                      Key=tkn.Text;
                      if (tkn.BlockType == TokenType.TextString){
                        Key = DeQuote(Key);
                      }
                    }else{
                      SubPhrase = MakeLiteral(tkn.Text, Marker, Marker);// inclusive
                      OnePhrase.ChildrenHash.put(Key, SubPhrase); Key=null;
                    }
                    MarkNext++;
                  }
                  else if (tkn.BlockType == TokenType.SingleChar){
                    if (tkn.Text.equals(":")){ /* Key=CurrentLiteral; */ KeyOrValue=1; }
                    else if (tkn.Text.equals(",")){ Key=null; KeyOrValue=0; }
                    MarkNext++;
                  }
                  else if (tkn.BlockType == TokenType.Whitespace){/* skip whitespace */ MarkNext++; }  
                  else {/* skip over everything else */ MarkNext++;}
                  
                  Marker = MarkNext;
                } // while (Marker<Chunks.size() && !(tkn = Chunks.get(Marker)).Text.equals(Ender));
                OnePhrase.ChunkEnd = Marker;// inclusive? 
              }
              return OnePhrase;
            }
            /* ********************************************************************************************************* */
            public static Phrase FindClauseArray(ArrayList<Token> Chunks, int Marker, int RecurDepth){
              Phrase OnePhrase=null,SubPhrase=null;
              String Starter="[", Ender="]";
              int MarkNext=Marker;
              RecurDepth++;
              Token tkn = Chunks.get(Marker);
              if (tkn.Text.equals(Starter)){
                OnePhrase = new Phrase();
                OnePhrase.ChunkStart = Marker;
                OnePhrase.ChildrenArray = new ArrayList<>();
                Marker++;
                MarkNext=Marker;
                while (Marker<Chunks.size()) {
                  tkn = Chunks.get(Marker);
                  if (tkn.Text.equals(Ender)){break;}
                  if ((SubPhrase = FindClauseHashMap(Chunks,  Marker, RecurDepth))!=null){
                    OnePhrase.ChildrenArray.add(SubPhrase); MarkNext = SubPhrase.ChunkEnd+1; 
                  }
                  else if ((SubPhrase = FindClauseArray(Chunks,  Marker, RecurDepth))!=null){
                    OnePhrase.ChildrenArray.add(SubPhrase); MarkNext = SubPhrase.ChunkEnd+1; 
                  }
                  else if ((tkn.BlockType == TokenType.TextString) || (tkn.BlockType == TokenType.Word)){
                    SubPhrase = MakeLiteral(tkn.Text, Marker, Marker);// inclusive
                    OnePhrase.ChildrenArray.add(SubPhrase);
                    MarkNext++;
                  }
                  else if (tkn.BlockType == TokenType.SingleChar){
                    if (tkn.Text.equals(",")){ }
                    MarkNext++;
                  }
                  else if (tkn.BlockType == TokenType.Whitespace){/* skip whitespace */ MarkNext++; }  
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
          parent = FindClauseHashMap(Chunks, Marker, 0);
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
        {// a value that is a hashtable, an array, or a literal
            public enum Types { None, Class, Interface, Method, Whatever }// Interface is not used yet.
            public Types MyType = Types.None;
            public String MyName = "***Nothing***";
            public Phrase Parent = null;
            public int ChunkStart,ChunkEnd;
            public String Literal=null;
            public ArrayList<Phrase> ChildrenArray = null;
            public HashMap<String,Phrase> ChildrenHash = null;
        }
    }

