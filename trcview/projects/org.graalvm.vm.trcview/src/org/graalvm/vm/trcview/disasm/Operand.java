package org.graalvm.vm.trcview.disasm;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.graalvm.vm.trcview.arch.Disassembler;

public class Operand {
    private Token[] tokens;
    private long pc;
    private Disassembler disasm;

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

    public void setDisassembler(Disassembler disasm) {
        this.disasm = disasm;
    }

    protected String getName(long addr) {
        if (disasm != null) {
            return disasm.getName(addr);
        } else {
            return null;
        }
    }

    protected String getLocation(long addr) {
        if (disasm != null) {
            return disasm.getLocation(addr);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return Stream.of(getTokens()).map(Token::toString).collect(Collectors.joining());
    }
}
