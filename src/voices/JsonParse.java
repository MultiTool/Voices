package voices;

import java.util.ArrayList;
    class JsonParse // Porting from an old 'JavaParse' experiment
    {// This does not work yet. We will use json parsing later on to save and load composition files.
        public enum TokenType { None, CommentStar, CommentSlash, Word, Whitespace, SingleChar, TextString }
        public static String Environment_NewLine = "\r\n";
        public static String PhraseEnd=",";
        /* ********************************************************************************************************* */
        public static class Tokenizer
        {
            //#region end finders
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
                String singles = "}{][)(*&^%$#@!~+=;:.>,<|\\?/-";
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
            // #endregion end finders
            /* ********************************************************************************************************* */
            public static boolean IsWordChar(char ch)
            {// for alphanumerics, eg variable names, reserved words, etc.
                if ('a' <= ch && ch <= 'z') { return true; }
                if ('A' <= ch && ch <= 'Z') { return true; }
                if ('0' <= ch && ch <= '9') { return true; }
                if ('_' == ch || ch == '@') { return true; }
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
            /* ********************************************************************************************************* */
            public static int Delve_Non_Squiggle(ArrayList<Token> Chunks, String BracketStart, String BracketFinish, int Marker, ArrayList<Phrase> Phrases, int RecurDepth)
            {
                System.out.println("RecurDepth:" + RecurDepth);
                RecurDepth++;
                Phrase OnePhrase;
                Token tkn;
                boolean terminator = false;

                tkn = Chunks.get(Marker);
                //if ((tkn = Chunks[Marker]).Text.equals(BracketStart)) { }
                while (!terminator && Marker < Chunks.size())
                {
                    OnePhrase = new Phrase(); Phrases.add(OnePhrase);
                    while (Marker < Chunks.size())
                    {
                        tkn = Chunks.get(Marker);
                        if (tkn.Text.equals(BracketStart))// recurse
                        {
                            OnePhrase.Tokens.add(tkn); OnePhrase.Children = new ArrayList<Phrase>();
                            Marker++;
                            Marker = Delve_Non_Squiggle(Chunks, BracketStart, BracketFinish, Marker, OnePhrase.Children, RecurDepth);
                            for (Phrase child : OnePhrase.Children) { child.Parent = OnePhrase; }
                            tkn = Chunks.get(Marker);// should be BracketFinish at this point
                            OnePhrase.Tokens.add(tkn);
                            Marker++;
                            break;
                        }
                        else if (tkn.Text.equals(BracketFinish)) { OnePhrase.Tokens.add(tkn); terminator = true; break; }// do not advance the marker so parent can pick up exit char
                        else { OnePhrase.Tokens.add(tkn); Marker++; }
                    }
                }
                return Marker;
            }
            /* ********************************************************************************************************* */
            public static int Delve(ArrayList<Token> Chunks, int Marker, ArrayList<Phrase> Phrases, int RecurDepth)
            {
                System.out.println("RecurDepth:" + RecurDepth);
                RecurDepth++;
                Phrase OnePhrase;
                Token tkn;
                boolean terminator = false;
                String state = "Start";
                while (!terminator && Marker < Chunks.size())
                {
                    OnePhrase = new Phrase(); Phrases.add(OnePhrase);
                    while (Marker < Chunks.size())
                    {
                        tkn = Chunks.get(Marker);
                        if (tkn.Text.equals(PhraseEnd)) { OnePhrase.Tokens.add(tkn); Marker++; state = "PhraseEnd"; break; }// good
                        else if (tkn.Text.equals("{"))// recurse
                        {
                            OnePhrase.Tokens.add(tkn); OnePhrase.Children = new ArrayList<Phrase>();
                            Marker++;
                            Marker = Delve(Chunks, Marker, OnePhrase.Children, RecurDepth);
                            for (Phrase child : OnePhrase.Children) { child.Parent = OnePhrase; }
                            tkn = Chunks.get(Marker);// should be } at this point
                            OnePhrase.Tokens.add(tkn);
                            Marker++;
                            state = "Decurse";
                            break;
                        }
                        else if (tkn.Text.equals("}")) { OnePhrase.Tokens.add(tkn); terminator = true; state = "UnBracket"; break; }// do not advance the marker so parent can pick up exit char
                        else { OnePhrase.Tokens.add(tkn); state = "Whatever[" + tkn.Text + "]"; Marker++; }
                    }
                }
                System.out.println(state);
                return Marker;
            }
            /* ********************************************************************************************************* */
            public static void Fold(ArrayList<Token> Chunks, ArrayList<Phrase> Phrases)
            {
                System.out.println("");
                System.out.println("-----------------------------------------------------");
                int Marker = 0;
                Marker = Delve(Chunks, Marker, Phrases, 0);
                LabelClasses(Phrases);
                System.out.println("Done");
            }
            /* ********************************************************************************************************* */
            public static Token FindMethodName(ArrayList<Token> Chunks, String BracketStart, String BracketFinish, int Marker)
            {
                Token chunk = null;
                int cnt = Marker;
                int depth = 0;
                //chunk = Chunks[cnt];
                do
                {
                    chunk = Chunks.get(cnt);
                    if (chunk.Text.equals( BracketFinish)) { depth++; }
                    else if (chunk.Text.equals( BracketStart)) { depth--; }
                    cnt--;
                    //chunk = Chunks[cnt];
                } while (depth > 0 || (chunk.BlockType == TokenType.Whitespace));
                if (chunk.Text.equals( BracketStart)) { chunk = Chunks.get(cnt); }// yuck hack
                String name = chunk.Text;
                return chunk;
            }
            /* ********************************************************************************************************* */
            public static void LabelClasses(ArrayList<Phrase> Phrases)
            {
                int findloc0, findloc1;
                Token tkn;
                for (Phrase ph : Phrases)
                {
                    if (ph.Children != null)
                    {
                        if ((findloc0 = ph.Find("class")) >= 0)
                        {
                            ph.MyType = Phrase.Types.Class;
                            tkn = ph.Tokens.get(findloc0 + 2);// skip over blank space too
                            ph.MyName = tkn.Text;
                        }
                        else if ((findloc0 = ph.Find("interface")) >= 0)
                        {
                            ph.MyType = Phrase.Types.Class;
                            tkn = ph.Tokens.get(findloc0 + 2);// skip over blank space too
                            ph.MyName = tkn.Text;
                        }
                        else
                        {
                            if (ph.Parent != null && ph.Parent.MyType == Phrase.Types.Class)
                            {// If I own squiggle brackets and my immediate parent is a class, I am a method. 
                                if ((findloc0 = ph.Find("(")) >= 0)// but I must also contain parenthesis! 
                                {
                                    ph.MyType = Phrase.Types.Method;
                                    // @Override
                                    // tkn = ph.Tokens[findloc0 - 1];// skip over blank space too
                                    tkn = FindMethodName(ph.Tokens, "<", ">", findloc0 - 1);// skip over blank spaces and templates
                                    ph.MyName = tkn.Text;
                                    System.out.println("MethodName[" + ph.MyName + "]");
                                    // get parameter list here.
                                    findloc0++;
                                    findloc1 = ph.FindNext(findloc0, ")");
                                    findloc1--;
                                    boolean VarName = false;
                                    for (int cnt = findloc0; cnt < findloc1; cnt++)
                                    {// parameters
                                        Token tkn1 = ph.Tokens.get(cnt);
                                        if (tkn1.BlockType != TokenType.CommentSlash && tkn1.BlockType != TokenType.CommentStar)
                                        {
                                            if (tkn1.BlockType != TokenType.Whitespace && tkn1.Text != "," && tkn1.Text != "[" && tkn1.Text != "]")
                                            {
                                                if (!VarName)
                                                {
                                                    if (tkn1.Text.toLowerCase() != "double" && tkn1.Text.toLowerCase() != "int" && tkn1.Text != "String")
                                                    {
                                                        tkn1.SpecificType = "ParameterObject";
                                                        // tkn1.Text += "&";
                                                        System.out.println();
                                                    }
                                                }
                                                VarName = !VarName;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        LabelClasses(ph.Children);
                    }
                }
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
        {// a list of tokens that end with either ; or {}
            public enum Types { None, Class, Interface, Method, Whatever }// Interface is not used yet.
            public Types MyType = Types.None;
            public String MyName = "***Nothing***";
            public ArrayList<Token> Tokens = new ArrayList<Token>();
            public Phrase Parent = null;
            public ArrayList<Phrase> Children = null;
            public void ToJava(StringBuilder sb)
            {
                for (Token tkn : this.Tokens)
                {
                    if (tkn.Text.equals("{"))
                    {
                        tkn.ToJava(sb);
                        for (Phrase ph : this.Children) { ph.ToJava(sb); }
                    }
                    else { tkn.ToJava(sb); }
                }
            }
            public void ToHpp(StringBuilder sb)
            {
                for  (Token tkn : this.Tokens)
                {
                    if (this.MyType == Types.Class)
                    {
                    }
                    else if (this.MyType == Types.Method)
                    {// replace override here
                        if (tkn.Text.equals("@Override"))
                        {
                        }
                        else if (tkn.Text.equals(")"))
                        {// add override 
                        }
                    }

                    if (tkn.Text.equals("{"))
                    {
                        tkn.ToHpp(sb);
                        for  (Phrase ph : this.Children) { ph.ToHpp(sb); }
                    }
                    else { tkn.ToHpp(sb); }
                }
            }
            public int Find(String target)
            {
                int cnt = 0;
                while (cnt < this.Tokens.size())
                {
                    if (this.Tokens.get(cnt).equals(target)) { return cnt; }
                    cnt++;
                }
                return -1;
            }
            public int FindNext(int StartPlace, String target)
            {
                int cnt = StartPlace;
                while (cnt < this.Tokens.size())
                {
                    if (this.Tokens.get(cnt).equals(target)) { return cnt; }
                    cnt++;
                }
                return -1;
            }
            public int FindNonBlankBackward(int StartDex)
            {
                int cnt = StartDex;
                while (cnt >= 0)
                {
                    if (this.Tokens.get(cnt).BlockType != TokenType.Whitespace) { return cnt; }
                    cnt--;
                }
                return this.Tokens.size();
            }
            @Override public  String toString()
            {
                StringBuilder sb = new StringBuilder();
                for (Token ck : this.Tokens) { sb.append("[" + ck.Text + "]"); }
                int cnt;
                if (Children == null) { cnt = 0; } else { cnt = Children.size(); }
                return "C:" + cnt + ": " + sb.toString();
            }
            public String ToText()
            {
                StringBuilder sb = new StringBuilder();
                for (Token ck : this.Tokens) { sb.append(ck.Text); }
                return sb.toString();
            }
        }
    }

