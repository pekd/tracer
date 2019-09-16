package org.graalvm.vm.x86.trcview.expression;

public class Token {
    public final TokenType type;
    public final long value;
    public final String str;

    public static enum TokenType {
        NUMBER,
        ADD,
        SUB,
        MUL,
        DIV,
        AND,
        OR,
        XOR,
        INV,
        LAND,
        LOR,
        GT,
        LT,
        GE,
        LE,
        SHL,
        SHR,
        SAR,
        EQ,
        NE,
        NOT,
        LPAR,
        RPAR,
        COMMA,
        IDENT,
        SIGNED,
        UNSIGNED,
        CHAR,
        SHORT,
        INT,
        LONG,
        U8,
        U16,
        U32,
        U64,
        S8,
        S16,
        S32,
        S64,
        VOID,
        CONST,
        EOF;
    }

    public Token(TokenType type) {
        this.type = type;
        this.value = 0;
        this.str = null;
    }

    public Token(TokenType type, long value) {
        this.type = type;
        this.value = value;
        this.str = null;
    }

    public Token(TokenType type, String str) {
        this.type = type;
        this.value = 0;
        this.str = str;
    }
}
