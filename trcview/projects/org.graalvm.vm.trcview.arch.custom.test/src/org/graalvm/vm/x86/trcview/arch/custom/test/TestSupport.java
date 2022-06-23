package org.graalvm.vm.x86.trcview.arch.custom.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.graalvm.vm.trcview.arch.custom.Intrinsics;
import org.graalvm.vm.trcview.script.Errors;
import org.graalvm.vm.trcview.script.Message;
import org.graalvm.vm.trcview.script.Parser;
import org.graalvm.vm.trcview.script.Scanner;
import org.graalvm.vm.trcview.script.Token;
import org.graalvm.vm.trcview.script.TokenType;
import org.junit.After;
import org.junit.Before;

public class TestSupport {
    public static final int DEFAULT_TIMEOUT = 500;

    private List<Token> tokens;
    private List<String> errors;

    @Before
    public void setup() {
        tokens = new ArrayList<>();
        errors = new ArrayList<>();
    }

    @After
    public void cleanup() {
        tokens.clear();
        errors.clear();
    }

    // scanner specific
    protected void token(TokenType type, int line, int col) {
        Token t = new Token(type, line, col);
        tokens.add(t);
    }

    protected void token(TokenType type, int line, int col, long value) {
        Token t = new Token(type, line, col);
        t.val = value;
        tokens.add(t);
    }

    protected void token(TokenType type, int line, int col, String value) {
        Token t = new Token(type, line, col);
        t.str = value;
        tokens.add(t);
    }

    protected void check(String str) {
        Scanner s = new Scanner(str);
        for (Token t : tokens) {
            Token tok = s.next();
            assertSame("type", t.type, tok.type);
            assertEquals("string value", t.str, tok.str);
            assertEquals("value", t.val, tok.val);
            assertEquals("line", t.line, tok.line);
            assertEquals("column", t.col, tok.col);
        }
        Token eof = s.next();
        assertSame(TokenType.eof, eof.type);
        checkErrors(s.errors);
    }

    // parser specific
    protected Parser parse(String s) {
        Parser p = new Parser(s);
        Intrinsics.register(p.symtab);
        p.parse();
        if (p.errors.numErrors() > 0) {
            System.out.println(p.errors.dump());
        }
        checkErrors(p.errors);
        return p;
    }

    // common
    protected void error(int line, int col, Message msg, Object... params) {
        errors.add("-- line " + line + " col " + col + ": " + msg.format(params));
    }

    private void checkErrors(Errors err) {
        List<String> actual = err.getErrors();
        assertEquals(errors, actual);
        assertEquals(errors.size(), actual.size());
    }
}
