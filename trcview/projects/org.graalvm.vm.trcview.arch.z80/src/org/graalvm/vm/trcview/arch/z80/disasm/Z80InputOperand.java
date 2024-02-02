package org.graalvm.vm.trcview.arch.z80.disasm;

import org.graalvm.vm.trcview.disasm.Operand;
import org.graalvm.vm.trcview.disasm.Token;
import org.graalvm.vm.trcview.disasm.Type;

public class Z80InputOperand extends Z80Operand {
    private final Z80Operand target;

    public Z80InputOperand(Z80Operand target) {
        this.target = target;
    }

    @Override
    public String toString() {
        return "(" + target + ")";
    }

    @Override
    public Operand disassemble(Z80MachineCode code) {
        Operand op = target.disassemble(code);
        Token[] tokens = new Token[op.getTokens().length + 2];
        System.arraycopy(op.getTokens(), 0, tokens, 1, op.getTokens().length);
        tokens[0] = new Token(Type.OTHER, "(");
        tokens[tokens.length - 1] = new Token(Type.OTHER, ")");
        return new Operand(tokens);
    }
}
