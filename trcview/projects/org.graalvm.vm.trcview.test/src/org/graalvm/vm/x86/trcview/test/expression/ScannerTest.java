package org.graalvm.vm.x86.trcview.test.expression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.text.ParseException;

import org.graalvm.vm.trcview.expression.Scanner;
import org.graalvm.vm.trcview.expression.Token;
import org.graalvm.vm.trcview.expression.Token.TokenType;
import org.junit.Test;

public class ScannerTest {
    @Test
    public void testHex001() throws ParseException {
        Scanner s = new Scanner("0x100");
        Token t = s.next();
        assertEquals(TokenType.NUMBER, t.type);
        assertEquals(0x100, t.value);

        t = s.next();
        assertEquals(TokenType.EOF, t.type);
    }

    @Test
    public void testHex002() throws ParseException {
        Scanner s = new Scanner("0xBEEF");
        Token t = s.next();
        assertEquals(TokenType.NUMBER, t.type);
        assertEquals(0xBEEF, t.value);

        t = s.next();
        assertEquals(TokenType.EOF, t.type);
    }

    @Test
    public void testHex003() throws ParseException {
        Scanner s = new Scanner("0x1234deadBeEF");
        Token t = s.next();
        assertEquals(TokenType.NUMBER, t.type);
        assertEquals(0x1234DEADBEEFL, t.value);

        t = s.next();
        assertEquals(TokenType.EOF, t.type);
    }

    @Test
    public void testHexErr001() {
        Scanner s = new Scanner("0x");
        try {
            s.next();
            fail();
        } catch (ParseException e) {
            assertEquals(2, e.getErrorOffset());
            assertEquals("unexpected eof", e.getMessage());
        }
    }

    @Test
    public void testHexErr002() {
        Scanner s = new Scanner("0xg");
        try {
            s.next();
            fail();
        } catch (ParseException e) {
            assertEquals(3, e.getErrorOffset());
            assertEquals("unexpected char: 'g'", e.getMessage());
        }
    }

    @Test
    public void testDec001() throws ParseException {
        Scanner s = new Scanner("1234");
        Token t = s.next();
        assertEquals(TokenType.NUMBER, t.type);
        assertEquals(1234, t.value);

        t = s.next();
        assertEquals(TokenType.EOF, t.type);
    }

    @Test
    public void testDec002() throws ParseException {
        Scanner s = new Scanner("0");
        Token t = s.next();
        assertEquals(TokenType.NUMBER, t.type);
        assertEquals(0, t.value);

        t = s.next();
        assertEquals(TokenType.EOF, t.type);
    }

    @Test
    public void testDec003() throws ParseException {
        Scanner s = new Scanner("42 21");
        Token t = s.next();
        assertEquals(TokenType.NUMBER, t.type);
        assertEquals(42, t.value);

        t = s.next();
        assertEquals(TokenType.NUMBER, t.type);
        assertEquals(21, t.value);

        t = s.next();
        assertEquals(TokenType.EOF, t.type);
    }

    @Test
    public void testDecErr001() {
        Scanner s = new Scanner("0y1");
        try {
            s.next();
            fail();
        } catch (ParseException e) {
            assertEquals(2, e.getErrorOffset());
            assertEquals("unexpected char: 'y'", e.getMessage());
        }
    }

    @Test
    public void testDecErr002() {
        Scanner s = new Scanner("0y");
        try {
            s.next();
            fail();
        } catch (ParseException e) {
            assertEquals(2, e.getErrorOffset());
            assertEquals("unexpected char: 'y'", e.getMessage());
        }
    }

    @Test
    public void testDecErr003() {
        Scanner s = new Scanner("1y2");
        try {
            s.next();
            fail();
        } catch (ParseException e) {
            assertEquals(2, e.getErrorOffset());
            assertEquals("unexpected char: 'y'", e.getMessage());
        }
    }

    @Test
    public void testIdent001() throws ParseException {
        Scanner s = new Scanner("hello");
        Token t = s.next();
        assertEquals(TokenType.IDENT, t.type);
        assertEquals("hello", t.str);

        t = s.next();
        assertEquals(TokenType.EOF, t.type);
    }

    @Test
    public void testIdent002() throws ParseException {
        Scanner s = new Scanner("hello world");
        Token t = s.next();
        assertEquals(TokenType.IDENT, t.type);
        assertEquals("hello", t.str);

        t = s.next();
        assertEquals(TokenType.IDENT, t.type);
        assertEquals("world", t.str);

        t = s.next();
        assertEquals(TokenType.EOF, t.type);
    }

    @Test
    public void testExpr001() throws ParseException {
        Scanner s = new Scanner("(eax + 4) * 2 + 0xBEEF");
        Token t = s.next();
        assertEquals(TokenType.LPAR, t.type);

        t = s.next();
        assertEquals(TokenType.IDENT, t.type);
        assertEquals("eax", t.str);

        t = s.next();
        assertEquals(TokenType.ADD, t.type);

        t = s.next();
        assertEquals(TokenType.NUMBER, t.type);
        assertEquals(4, t.value);

        t = s.next();
        assertEquals(TokenType.RPAR, t.type);

        t = s.next();
        assertEquals(TokenType.MUL, t.type);

        t = s.next();
        assertEquals(TokenType.NUMBER, t.type);
        assertEquals(2, t.value);

        t = s.next();
        assertEquals(TokenType.ADD, t.type);

        t = s.next();
        assertEquals(TokenType.NUMBER, t.type);
        assertEquals(0xbeef, t.value);

        t = s.next();
        assertEquals(TokenType.EOF, t.type);
    }
}