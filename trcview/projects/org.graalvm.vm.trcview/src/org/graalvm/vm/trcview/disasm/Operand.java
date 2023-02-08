package org.graalvm.vm.trcview.disasm;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Operand {
    private Token[] tokens;

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
        return tokens;
    }

    @Override
    public String toString() {
        return Stream.of(tokens).map(Token::toString).collect(Collectors.joining());
    }
}
