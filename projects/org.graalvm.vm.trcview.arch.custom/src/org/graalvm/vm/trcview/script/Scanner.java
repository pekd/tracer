package org.graalvm.vm.trcview.script;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class Scanner {
    private static final char EOF = (char) -1;
    private static final char LF = '\n';

    /** Input data to read from. */
    private Reader in;

    /** Lookahead character. (= next (unhandled) character in the input stream) */
    private char ch;

    /** Current line in input stream. */
    private int line;

    /** Current column in input stream. */
    private int col;

    /** According errors object. */
    public final Errors errors;

    private static Map<String, TokenType> twoTokens;
    private static Map<Character, TokenType> oneTokens;
    private static Map<Character, Character> escapeSequences;
    private static Map<String, TokenType> keywords;

    // lookup tables
    static {
        twoTokens = new HashMap<>();
        twoTokens.put("++", TokenType.pplus);
        twoTokens.put("--", TokenType.mminus);
        twoTokens.put("+=", TokenType.plusas);
        twoTokens.put("-=", TokenType.minusas);
        twoTokens.put("*=", TokenType.timesas);
        twoTokens.put("/=", TokenType.slashas);
        twoTokens.put("%=", TokenType.remas);
        twoTokens.put("==", TokenType.eql);
        twoTokens.put("!=", TokenType.neq);
        twoTokens.put("<=", TokenType.leq);
        twoTokens.put(">=", TokenType.geq);
        twoTokens.put("&&", TokenType.and);
        twoTokens.put("||", TokenType.or);
        twoTokens.put("<<", TokenType.shl);
        twoTokens.put(">>", TokenType.shr);
        twoTokens.put("->", TokenType.arrow);

        oneTokens = new HashMap<>();
        oneTokens.put('+', TokenType.plus);
        oneTokens.put('-', TokenType.minus);
        oneTokens.put('*', TokenType.times);
        oneTokens.put('/', TokenType.slash);
        oneTokens.put('%', TokenType.rem);
        oneTokens.put('=', TokenType.assign);
        oneTokens.put('<', TokenType.lt);
        oneTokens.put('>', TokenType.gt);
        oneTokens.put('&', TokenType.bitand);
        oneTokens.put('|', TokenType.bitor);
        oneTokens.put('^', TokenType.xor);
        oneTokens.put('!', TokenType.not);
        oneTokens.put('~', TokenType.com);

        oneTokens.put(',', TokenType.comma);
        oneTokens.put(';', TokenType.semicolon);
        oneTokens.put('.', TokenType.period);
        oneTokens.put('(', TokenType.lpar);
        oneTokens.put(')', TokenType.rpar);
        oneTokens.put('[', TokenType.lbrack);
        oneTokens.put(']', TokenType.rbrack);
        oneTokens.put('{', TokenType.lbrace);
        oneTokens.put('}', TokenType.rbrace);

        escapeSequences = new HashMap<>();
        escapeSequences.put('\'', '\'');
        escapeSequences.put('\\', '\\');
        escapeSequences.put('n', '\n');
        escapeSequences.put('r', '\r');
        escapeSequences.put('"', '"');
        escapeSequences.put('?', '?');
        escapeSequences.put('a', '\u0007');
        escapeSequences.put('b', '\b');
        escapeSequences.put('e', '\u001B');
        escapeSequences.put('f', '\f');
        escapeSequences.put('t', '\t');
        escapeSequences.put('v', '\u000B');

        keywords = new HashMap<>();
        keywords.put("continue", TokenType.continue_);
        keywords.put("break", TokenType.break_);
        keywords.put("else", TokenType.else_);
        keywords.put("if", TokenType.if_);
        keywords.put("return", TokenType.return_);
        keywords.put("void", TokenType.void_);
        keywords.put("while", TokenType.while_);
        keywords.put("do", TokenType.do_);
        keywords.put("switch", TokenType.switch_);
        keywords.put("case", TokenType.case_);
        keywords.put("default", TokenType.default_);
        keywords.put("typedef", TokenType.typedef_);
        keywords.put("struct", TokenType.struct_);
        keywords.put("union", TokenType.union_);
        keywords.put("unsigned", TokenType.unsigned_);
        keywords.put("signed", TokenType.signed_);
        keywords.put("char", TokenType.char_);
        keywords.put("short", TokenType.short_);
        keywords.put("int", TokenType.int_);
        keywords.put("long", TokenType.long_);
        keywords.put("const", TokenType.const_);
        keywords.put("true", TokenType.true_);
        keywords.put("false", TokenType.false_);
    }

    public Scanner(String s) {
        this(new StringReader(s));
    }

    public Scanner(Reader r) {
        // initialize error handling support
        errors = new Errors();

        in = r;
        line = 1;
        col = 0;
        read();
    }

    /**
     * Adds error message to the list of errors.
     */
    private void error(Token t, Message msg, Object... msgParams) {
        errors.error(t.line, t.col, msg, msgParams);

        // reset token content (consistent JUnit tests)
        t.val = 0;
        t.str = null;
    }

    private boolean read() {
        if (ch == EOF)
            return false;
        try {
            int c = in.read();
            ch = (char) c;
            if (ch == LF) {
                line++;
                col = 0;
            } else if (c != -1)
                col++;
            return c != -1;
        } catch (IOException e) {
            ch = EOF;
            return false;
        }
    }

    private static boolean isStartIdentifier(char c) {
        char l = Character.toLowerCase(c);
        if (l >= 'a' && l <= 'z') // just ASCII
            return true;
        if (l == '_')
            return true;
        return false;
    }

    private static boolean isMidIdentifier(char c) {
        char l = Character.toLowerCase(c);
        if (l >= 'a' && l <= 'z') // just ASCII
            return true;
        if (l >= '0' && l <= '9')
            return true;
        if (l == '_')
            return true;
        return false;
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9'; // only ASCII
    }

    private static boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f');
    }

    private static boolean isOctalDigit(char c) {
        return c >= '0' && c <= '7';
    }

    /**
     * Returns next token. To be used by parser.
     */
    public Token next() {
        if (ch == EOF) {
            return new Token(TokenType.eof, line, col);
        }

        char c = ch;
        int tcol = col;
        int tline = line;
        read();

        Token t = new Token(TokenType.none, tline, tcol);

        String two = Character.toString(c) + Character.toString(ch);
        while (two.equals("/*") || (two.equals("//")) || Character.isWhitespace(c)) { // comment/whitespace
            if (two.equals("/*")) {
                int depth = 1;
                int state = 0;
                while (depth > 0) {
                    read();
                    if (ch == EOF) {
                        // hm?!
                        error(t, Message.EOF_IN_COMMENT);
                        return new Token(TokenType.eof, line, col);
                    }
                    switch (state) {
                        case 0: // nothing special
                            if (ch == '/')
                                state = 1;
                            else if (ch == '*')
                                state = 2;
                            break;
                        case 1: // '/'
                            if (ch == '*') {
                                depth++;
                                state = 0;
                            } else if (ch != '/')
                                state = 0;
                            break;
                        case 2: // '*'
                            if (ch == '/') {
                                depth--;
                                state = 0;
                            } else if (ch != '*')
                                state = 0;
                            break;
                    }
                }
                read();
                c = ch;
                tcol = col;
                tline = line;
                read();
                two = Character.toString(c) + Character.toString(ch);
            } else {
                if (two.equals("//")) {
                    while (ch != '\n' && ch != EOF)
                        read();
                }
                while (Character.isWhitespace(ch))
                    read();
                c = ch;
                tcol = col;
                tline = line;
                read();
                two = Character.toString(c) + Character.toString(ch);
            }
        }

        if (twoTokens.containsKey(two)) {
            t = new Token(twoTokens.get(two), tline, tcol);
            read();
            return t;
        } else if (oneTokens.containsKey(c)) {
            return new Token(oneTokens.get(c), tline, tcol);
        } else if (isStartIdentifier(c)) {
            StringBuffer buf = new StringBuffer();
            buf.append(c);
            while (isMidIdentifier(ch)) {
                buf.append(ch);
                read();
            }
            String s = buf.toString();
            if (keywords.containsKey(s)) {
                return new Token(keywords.get(s), tline, tcol);
            }
            t = new Token(TokenType.ident, tline, tcol);
            t.str = s;
            return t;
        } else if (isDigit(c)) {
            int radix = 10;
            StringBuffer buf = new StringBuffer();
            buf.append(c);
            if (ch == 'x') {
                // hex number
                radix = 16;
                read();
                while (isHexDigit(ch) || ch == '_') {
                    if (ch != '_') {
                        buf.append(ch);
                    }
                    read();
                }
            } else if (buf.charAt(0) == '0') {
                radix = 8;
                while (isOctalDigit(ch) || ch == '_') {
                    if (ch != '_') {
                        buf.append(ch);
                    }
                    read();
                }
            } else {
                while (isDigit(ch) || ch == '_') {
                    if (ch != '_') {
                        buf.append(ch);
                    }
                    read();
                }
            }
            String v = buf.toString();
            t = new Token(TokenType.number, tline, tcol);
            if (isStartIdentifier(ch) || isMidIdentifier(ch)) {
                t = new Token(TokenType.none, tline, tcol);
                error(t, Message.INVALID_CHAR, ch);
                // continue until end of word
                while (isStartIdentifier(ch) || isMidIdentifier(ch)) {
                    read();
                }
                return t;
            }
            try {
                t.val = Long.parseUnsignedLong(v, radix);
                return t;
            } catch (NumberFormatException e) { // too big for long
                error(t, Message.BIG_NUM, v);
                return t;
            }
        } else if (c == '\'') {
            boolean skip = false;
            t = new Token(TokenType.charConst, tline, tcol);
            if (ch == '\\') {
                boolean noread = false;
                read();
                if (ch == 'x') { // \x1b-like escape sequences
                    read();
                    char c1 = ch;
                    read();
                    char c2 = ch;
                    if (c1 == EOF || c2 == EOF)
                        error(t, Message.UNDEFINED_ESCAPE, ch);
                    else {
                        if (!isHexDigit(c1) || !isHexDigit(c2))
                            error(t, Message.UNDEFINED_ESCAPE, "x" + c1 + c2);
                        else
                            t.val = Integer.parseInt(new String(new char[]{c1, c2}), 16);
                    }
                } else if (isOctalDigit(ch)) {
                    int v = ch - '0';
                    read();
                    while (isOctalDigit(ch)) {
                        v <<= 3;
                        v |= ch - '0';
                        read();
                    }
                    t.val = (char) v;
                    noread = true;
                } else
                // no check for eof?
                if (!escapeSequences.containsKey(ch)) {
                    error(t, Message.UNDEFINED_ESCAPE, ch);
                } else {
                    t.val = escapeSequences.get(ch);
                }
                if (!noread)
                    read();
                if (ch != '\'') {
                    error(t, Message.MISSING_QUOTE);
                    skip = true;
                }
            } else if (ch == '\'') {
                error(t, Message.EMPTY_CHARCONST);
            } else if (ch == '\r' || ch == '\n') {
                error(t, Message.ILLEGAL_LINE_END);
            } else {
                t.val = ch;
                read();
                if (ch != '\'') {
                    error(t, Message.MISSING_QUOTE);
                    skip = true;
                }
            }
            if (!skip) {
                read();
            }
            return t;
        } else if (c == '"') {
            t = new Token(TokenType.stringConst, tline, tcol);
            int state = 0;
            StringBuilder buf = new StringBuilder();
            boolean ok = false;
            loop: while (ch != EOF) {
                boolean skip = false;
                switch (state) {
                    case 0: // normal text
                        switch (ch) {
                            case '\\':
                                state = 1;
                                break;
                            case '"':
                                ok = true;
                                break loop;
                            case EOF:
                                error(t, Message.ILLEGAL_LINE_END);
                            default:
                                buf.append(ch);
                                break;
                        }
                        break;
                    case 1: // escape
                        if (ch == 'x') { // \x1b-like escape sequences
                            read();
                            char c1 = ch;
                            read();
                            char c2 = ch;
                            if (c1 == EOF || c2 == EOF) {
                                error(t, Message.UNDEFINED_ESCAPE, ch);
                            } else {
                                if (!isHexDigit(c1) || !isHexDigit(c2))
                                    error(t, Message.UNDEFINED_ESCAPE, "x" + c1 + c2);
                                else
                                    buf.append((char) Integer.parseInt(new String(new char[]{c1, c2}), 16));
                            }
                            state = 0;
                        } else if (isOctalDigit(ch)) {
                            int v = ch - '0';
                            read();
                            while (isOctalDigit(ch)) {
                                v <<= 3;
                                v |= ch - '0';
                                read();
                            }
                            buf.append((char) v);
                            state = 0;
                            skip = true;
                        } else if (escapeSequences.containsKey(ch)) {
                            buf.append(escapeSequences.get(ch));
                            state = 0;
                        } else {
                            error(t, Message.UNDEFINED_ESCAPE, ch);
                            state = 0;
                        }
                        break;
                }
                if (!skip)
                    read();
            }
            if (!ok)
                error(t, Message.MISSING_QUOTE);
            else
                read();
            t.str = buf.toString();
            return t;
        } else if (c == EOF) {
            return new Token(TokenType.eof, tline, tcol);
        } else {
            error(t, Message.INVALID_CHAR, c);
        }

        return t;
    }
}
