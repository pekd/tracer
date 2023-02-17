package org.graalvm.vm.trcview.disasm;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Operand {
    private Token[] tokens;
    private long pc;

    public Operand(Type type, String text) {
        tokens = new Token[]{new Token(type, text)};
    }

    public Operand(Type type, String text, long value) {
        tokens = new Token[]{new Token(type, text, value)};
    }

    public Operand(Token... tokens) {
        this.tokens = tokens;
    }

    public Token[] getTokens() {
        for (Token t : tokens) {
            t.setPC(pc);
        }
        return tokens;
    }

    protected long getPC() {
        return pc;
    }

    public void setPC(long pc) {
        this.pc = pc;
    }

    @Override
    public String toString() {
        return Stream.of(getTokens()).map(Token::toString).collect(Collectors.joining());
    }
}
