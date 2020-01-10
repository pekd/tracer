package org.graalvm.vm.trcview.script;

public class Token {
    public TokenType type;

    public final int line;
    public final int col;

    public long val;
    public String str;

    public Token(TokenType type, int line, int col) {
        this.type = type;
        this.line = line;
        this.col = col;
    }

    @Override
    public String toString() {
        String result = "line " + line + ", col " + col + ", type " + type;
        if (type == TokenType.ident) {
            result = result + ", str " + str;
        } else if (type == TokenType.number) {
            result = result + ", val " + val;
        } else if (type == TokenType.charConst) {
            result = result + ", val '" + (char) val + "'";
        }
        return result;
    }
}
