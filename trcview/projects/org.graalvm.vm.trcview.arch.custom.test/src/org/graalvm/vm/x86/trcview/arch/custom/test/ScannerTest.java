package org.graalvm.vm.x86.trcview.arch.custom.test;

import static org.graalvm.vm.trcview.script.Message.BIG_NUM;
import static org.graalvm.vm.trcview.script.Message.EMPTY_CHARCONST;
import static org.graalvm.vm.trcview.script.Message.EOF_IN_COMMENT;
import static org.graalvm.vm.trcview.script.Message.ILLEGAL_LINE_END;
import static org.graalvm.vm.trcview.script.Message.INVALID_CHAR;
import static org.graalvm.vm.trcview.script.Message.MISSING_QUOTE;
import static org.graalvm.vm.trcview.script.TokenType.bitand;
import static org.graalvm.vm.trcview.script.TokenType.bitor;
import static org.graalvm.vm.trcview.script.TokenType.charConst;
import static org.graalvm.vm.trcview.script.TokenType.com;
import static org.graalvm.vm.trcview.script.TokenType.eof;
import static org.graalvm.vm.trcview.script.TokenType.false_;
import static org.graalvm.vm.trcview.script.TokenType.gt;
import static org.graalvm.vm.trcview.script.TokenType.ident;
import static org.graalvm.vm.trcview.script.TokenType.if_;
import static org.graalvm.vm.trcview.script.TokenType.int_;
import static org.graalvm.vm.trcview.script.TokenType.lbrace;
import static org.graalvm.vm.trcview.script.TokenType.lbrack;
import static org.graalvm.vm.trcview.script.TokenType.lt;
import static org.graalvm.vm.trcview.script.TokenType.minus;
import static org.graalvm.vm.trcview.script.TokenType.none;
import static org.graalvm.vm.trcview.script.TokenType.not;
import static org.graalvm.vm.trcview.script.TokenType.number;
import static org.graalvm.vm.trcview.script.TokenType.rbrace;
import static org.graalvm.vm.trcview.script.TokenType.rbrack;
import static org.graalvm.vm.trcview.script.TokenType.semicolon;
import static org.graalvm.vm.trcview.script.TokenType.stringConst;
import static org.graalvm.vm.trcview.script.TokenType.struct_;
import static org.graalvm.vm.trcview.script.TokenType.true_;
import static org.graalvm.vm.trcview.script.TokenType.while_;
import static org.graalvm.vm.trcview.script.TokenType.xor;

import org.graalvm.vm.trcview.script.TokenType;
import org.junit.Test;

public class ScannerTest extends TestSupport {
    @Test(timeout = DEFAULT_TIMEOUT)
    public void testKeywords001() {
        token(if_, 1, 1);
        check("if");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testKeywords002() {
        token(if_, 1, 1);
        token(while_, 1, 4);
        token(true_, 1, 10);
        token(false_, 1, 15);
        token(ident, 1, 21, "noodle");
        check("if while true false noodle");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testNumbersDec001() {
        token(TokenType.number, 1, 1, 42);
        check("42");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testNumbersOct001() {
        token(TokenType.number, 1, 1, 27);
        check("033");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testNumbersHex001() {
        token(TokenType.number, 1, 1, 27);
        check("0x1b");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testNumbersHex002() {
        token(TokenType.number, 1, 1, 27);
        check("0x1B");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testStruct001() {
        String code = "struct State {\n" +
                        "    u16 registers[8];\n" +
                        "    u16 psw;\n" +
                        "    int state;\n" +
                        "};";
        token(struct_, 1, 1);
        token(ident, 1, 8, "State");
        token(lbrace, 1, 14);
        token(ident, 2, 5, "u16");
        token(ident, 2, 9, "registers");
        token(lbrack, 2, 18);
        token(number, 2, 19, 8);
        token(rbrack, 2, 20);
        token(semicolon, 2, 21);
        token(ident, 3, 5, "u16");
        token(ident, 3, 9, "psw");
        token(semicolon, 3, 12);
        token(int_, 4, 5);
        token(ident, 4, 9, "state");
        token(semicolon, 4, 14);
        token(rbrace, 5, 1);
        token(semicolon, 5, 2);
        check(code);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void oneToken() {
        token(semicolon, 1, 1);
        token(eof, 1, 1);

        check(";");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void twoTokens() {
        token(semicolon, 1, 1);
        token(semicolon, 1, 2);
        token(eof, 1, 2);

        check(";;");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void space() {

        token(semicolon, 1, 1);
        token(semicolon, 1, 4);
        token(eof, 1, 4);

        check(";  ;");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void tabulator() {
        token(semicolon, 1, 1);
        token(semicolon, 1, 4);
        token(eof, 1, 4);

        check(";\t\t;");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void noToken() {
        token(eof, 1, 0);

        check("");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void crLfLineSeparators() {
        token(semicolon, 1, 1);
        token(semicolon, 2, 2);
        token(semicolon, 3, 3);
        token(eof, 3, 4);

        check(";\r\n ;\r\n  ; ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void lFLineSeparators() {
        token(semicolon, 1, 1);
        token(semicolon, 2, 2);
        token(semicolon, 3, 3);
        token(eof, 3, 4);

        check(";\n ;\n  ; ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void invalidChar1() {
        token(lbrace, 1, 2);
        token(none, 1, 3);
        error(1, 3, INVALID_CHAR, '�');
        token(rbrace, 1, 4);
        token(eof, 1, 5);

        check(" {�} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void invalidChar2() {
        token(lbrace, 1, 2);
        token(none, 1, 3);
        error(1, 3, INVALID_CHAR, '\0');
        token(rbrace, 1, 4);
        token(eof, 1, 5);

        check(" {\0} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void invalidChar3() {
        token(lbrace, 1, 2);
        token(ident, 1, 3, "ident");
        token(none, 1, 8);
        error(1, 8, INVALID_CHAR, '�');
        token(rbrace, 1, 9);
        token(eof, 1, 10);

        check(" {ident�} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void bitand() {
        token(lbrace, 1, 2);
        token(bitand, 1, 3);
        token(rbrace, 1, 4);
        token(eof, 1, 5);

        check(" {&} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void bitor() {
        token(lbrace, 1, 2);
        token(bitor, 1, 3);
        token(rbrace, 1, 4);
        token(eof, 1, 5);

        check(" {|} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void not() {
        token(lbrace, 1, 2);
        token(not, 1, 3);
        token(rbrace, 1, 4);
        token(eof, 1, 5);

        check(" {!} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void com() {
        token(lbrace, 1, 2);
        token(com, 1, 3);
        token(rbrace, 1, 4);
        token(eof, 1, 5);

        check(" {~} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void xor() {
        token(lbrace, 1, 2);
        token(xor, 1, 3);
        token(rbrace, 1, 4);
        token(eof, 1, 5);

        check(" {^} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void ident() {
        token(lbrace, 1, 2);
        token(ident, 1, 3, "i");
        token(ident, 1, 5, "I");
        token(ident, 1, 7, "i1");
        token(ident, 1, 10, "i_");
        token(ident, 1, 13, "i1I_i");
        token(rbrace, 1, 18);
        token(eof, 1, 19);

        check(" {i I i1 i_ i1I_i} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void identSepararator() {
        token(lbrace, 1, 2);
        token(ident, 1, 3, "i");
        token(lbrack, 1, 4);
        token(ident, 1, 5, "i");
        token(lt, 1, 6);
        token(ident, 1, 7, "i0i_i");
        token(gt, 1, 12);
        token(ident, 1, 13, "i");
        token(rbrack, 1, 14);
        token(ident, 1, 15, "i");
        token(rbrace, 1, 16);
        token(eof, 1, 17);

        check(" {i[i<i0i_i>i]i} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void singleIdent() {
        token(ident, 1, 1, "i");
        token(eof, 1, 1);

        check("i");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void number() {
        token(lbrace, 1, 2);
        token(number, 1, 3, 123);
        token(number, 1, 7, 2147483647);
        token(rbrace, 1, 17);
        token(eof, 1, 18);

        check(" {123 2147483647} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void singleNumber() {
        token(number, 1, 1, 123);
        token(eof, 1, 3);

        check("123");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void negativeNumber() {
        token(lbrace, 1, 2);
        token(minus, 1, 3);
        token(number, 1, 4, 123);
        token(rbrace, 1, 7);
        token(eof, 1, 8);

        check(" {-123} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void bigNumber() {
        token(lbrace, 1, 2);
        token(number, 1, 3, 0);
        error(1, 3, BIG_NUM, "18446744073709551616");
        token(rbrace, 1, 23);
        token(eof, 1, 24);

        check(" {18446744073709551616} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void negativeBigNumber() {
        token(lbrace, 1, 2);
        token(minus, 1, 3);
        token(number, 1, 4, 0);
        error(1, 4, BIG_NUM, "18446744073709551616");
        token(rbrace, 1, 24);
        token(eof, 1, 25);

        check(" {-18446744073709551616} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void reallyBigNumber() {
        token(lbrace, 1, 2);
        token(number, 1, 3, 0);
        error(1, 3, BIG_NUM, "1234567890123456789012345678901234567890");
        token(rbrace, 1, 43);
        token(eof, 1, 44);

        check(" {1234567890123456789012345678901234567890} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void numberIdent() {
        token(lbrace, 1, 2);
        token(none, 1, 3);
        error(1, 3, INVALID_CHAR, "a");
        token(none, 1, 13);
        error(1, 13, INVALID_CHAR, "b");
        token(rbrace, 1, 21);
        token(eof, 1, 22);

        check(" {123abc123 123break} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void charConst() {
        token(lbrace, 1, 2);
        token(charConst, 1, 3, ' ');
        token(charConst, 1, 7, 'A');
        token(charConst, 1, 11, 'z');
        token(charConst, 1, 15, '0');
        token(charConst, 1, 19, '!');
        token(charConst, 1, 23, '"');
        token(charConst, 1, 27, '�');
        token(charConst, 1, 31, '\0');
        token(rbrace, 1, 34);
        token(eof, 1, 35);

        check(" {' ' 'A' 'z' '0' '!' '\"' '�' '\0'} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void singleCharConst() {
        token(charConst, 1, 1, 'x');
        token(eof, 1, 3);

        check("'x'");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void escapeCharConst() {
        token(lbrace, 1, 2);
        token(charConst, 1, 3, '\n');
        token(charConst, 1, 8, '\r');
        token(charConst, 1, 13, '\\');
        token(charConst, 1, 18, '\'');
        token(rbrace, 1, 22);
        token(eof, 1, 23);

        check(" {'\\n' '\\r' '\\\\' '\\''} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void singleEscapeCharConst() {
        token(charConst, 1, 1, '\n');
        token(eof, 1, 4);

        check("'\\n'");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void emptyCharConst() {
        token(lbrace, 1, 2);
        token(charConst, 1, 3, '\0');
        error(1, 3, EMPTY_CHARCONST);
        token(rbrace, 1, 5);
        token(eof, 1, 6);

        check(" {''} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void unclosedCharConst() {
        token(lbrace, 1, 2);
        token(charConst, 1, 3, '\0');
        error(1, 3, MISSING_QUOTE);
        token(rbrace, 1, 5);
        token(eof, 1, 6);

        check(" {'a} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void emptyAndUnclosedCharConst() {
        token(charConst, 1, 2, '\0');
        error(1, 2, EMPTY_CHARCONST);
        token(charConst, 1, 4, '\0');
        error(1, 4, MISSING_QUOTE);
        token(eof, 1, 5);

        check(" \'\'\' ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void unclosedEscapeCharConst() {
        token(lbrace, 1, 2);
        token(charConst, 1, 3, '\0');
        error(1, 3, MISSING_QUOTE);
        token(rbrace, 1, 6);
        token(eof, 1, 7);

        check(" {'\\r} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void unclosedBackslashCharConst() {
        token(lbrace, 1, 2);
        token(charConst, 1, 3, '\0');
        error(1, 3, MISSING_QUOTE);
        token(rbrace, 1, 6);
        token(eof, 1, 7);

        check(" {'\\'} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void invalidEscapeCharConst() {
        token(lbrace, 1, 2);
        token(charConst, 1, 3, '\u0007');
        token(rbrace, 1, 7);
        token(eof, 1, 8);

        check(" {'\\a'} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void invalidEscapeCharMissingQuote() {
        token(charConst, 1, 2, 0);
        error(1, 2, MISSING_QUOTE);
        token(eof, 1, 5);

        check(" '\\a ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void fileEndCharConst() {
        token(lbrace, 1, 2);
        token(charConst, 1, 3, '\0');
        error(1, 3, MISSING_QUOTE);
        token(eof, 1, 3);

        check(" {'");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void lineEndCharConst() {
        token(lbrace, 1, 2);
        token(charConst, 1, 3, '\0');
        error(1, 3, ILLEGAL_LINE_END);
        token(charConst, 2, 1, 'a');
        token(rbrace, 2, 4);
        token(eof, 2, 5);

        check(" {'\n'a'} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void lineEndWithCRCharConst() {
        token(lbrace, 1, 2);
        token(charConst, 1, 3, '\0');
        error(1, 3, ILLEGAL_LINE_END);
        token(charConst, 2, 1, 'a');
        token(rbrace, 2, 4);
        token(eof, 2, 5);

        check(" {'\r\n'a'} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void keyword1() {
        token(lbrace, 1, 2);
        token(if_, 1, 4);
        token(rbrace, 1, 7);
        token(eof, 1, 8);

        check(" { if } ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void keyword2() {
        token(lbrace, 1, 2);
        token(if_, 1, 3);
        token(rbrace, 1, 5);
        token(eof, 1, 6);

        check(" {if} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void singleKeyword() {
        token(if_, 1, 1);
        token(eof, 1, 2);

        check("if");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void keyword3() {
        token(lbrace, 1, 2);
        token(ident, 1, 3, "for_");
        token(rbrace, 1, 7);
        token(eof, 1, 8);

        check(" {for_} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void keyword4() {
        token(lbrace, 1, 2);
        token(bitand, 1, 3);
        token(if_, 1, 4);
        token(rbrace, 1, 6);
        token(eof, 1, 7);

        check(" {&if} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void caseSensitiv1() {
        token(lbrace, 1, 2);
        token(ident, 1, 3, "For");
        token(rbrace, 1, 6);
        token(eof, 1, 7);

        check(" {For} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void caseSensitiv2() {
        token(lbrace, 1, 2);
        token(ident, 1, 3, "FOR");
        token(rbrace, 1, 6);
        token(eof, 1, 7);

        check(" {FOR} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void simpleSingleLineComment() {
        token(lbrace, 1, 2);
        token(rbrace, 1, 40);
        token(eof, 1, 41);

        check(" {/* Simple / single * line comment. */} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void simpleMultiLineComment() {
        token(lbrace, 1, 2);
        token(rbrace, 5, 2);
        token(eof, 5, 3);

        check(" {\n  /* Simple \n     / multi * line \n     comment. */ \n } ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void nestedSingleLineComment2() {
        token(lbrace, 1, 2);
        token(rbrace, 1, 21);
        token(eof, 1, 22);

        check(" {/*//*///****/**/*/} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void nestedSingleLineComment() {
        token(lbrace, 1, 2);
        token(rbrace, 1, 62);
        token(eof, 1, 63);

        check(" {/* This / is * a /* nested  /* single line */ comment. */*/} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void nestedMultiLineComment() {
        token(lbrace, 1, 2);
        token(rbrace, 8, 2);
        token(eof, 8, 3);

        check(" {\n  /* This / is * a \n   /* nested  \n" +
                        "    /* multi line */\n    comment. \n   */" +
                        "\n  */ \n } ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void nestedMultiLineComment2() {
        token(lbrace, 1, 2);
        token(rbrace, 8, 2);
        token(eof, 8, 3);

        check(" {\n  /* This / is * a \n   /* nested  \n    /* multi /*/* double nestet */*/ line */\n    comment. \n   */\n  */ \n } ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void commentAtEnd1() {
        token(lbrace, 1, 2);
        token(eof, 1, 62);

        check(" {/* This / is * a /* nested  /* single line */ comment. */*/ ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void commentAtEnd2() {
        token(lbrace, 1, 2);
        token(eof, 1, 61);

        check(" {/* This / is * a /* nested  /* single line */ comment. */*/");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void unclosedComment() {
        token(lbrace, 1, 2);
        error(1, 3, EOF_IN_COMMENT);
        token(eof, 1, 46);

        check(" {/* This / is * a nested unclosed comment. } ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void unclosedComment2() {
        token(lbrace, 1, 2);
        error(1, 3, EOF_IN_COMMENT);
        token(eof, 1, 5);

        check(" {/*/");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void nestedUnclosedComment() {
        token(lbrace, 1, 2);
        error(1, 3, EOF_IN_COMMENT);
        token(eof, 1, 54);

        check(" {/* This / is * a /* nested /* unclosed comment. */} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void nestedUnclosedComment2() {
        token(lbrace, 1, 2);
        error(1, 3, EOF_IN_COMMENT);
        token(eof, 1, 51);

        check(" {/* This / is * a nested unclosed /* comment. } */");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void lineComment() {
        token(lbrace, 1, 2);
        token(ident, 1, 3, "This");
        token(ident, 1, 8, "is");
        token(eof, 1, 24);

        check(" {This is // a comment} ");
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void string1() {
        token(stringConst, 1, 2, "Hello world!");
        token(eof, 1, 16);

        check(" \"Hello world!\" ");
    }
}
