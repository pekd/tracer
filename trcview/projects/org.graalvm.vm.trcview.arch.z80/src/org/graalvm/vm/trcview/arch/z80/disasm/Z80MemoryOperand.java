package org.graalvm.vm.trcview.arch.z80.disasm;

import org.graalvm.vm.trcview.arch.Disassembler;
import org.graalvm.vm.trcview.disasm.Operand;
import org.graalvm.vm.trcview.disasm.Token;
import org.graalvm.vm.trcview.disasm.Type;

public class Z80MemoryOperand extends Z80Operand {
    private final Z80Operand target;

    public Z80MemoryOperand(Z80Operand target) {
        this.target = target;
    }

    @Override
    public String toString() {
        return "(" + target + ")";
    }

    @Override
    public Operand disassemble(Z80MachineCode code) {
        Operand op = target.disassemble(code);
        Token[] t = op.getTokens();

        String name = null;
        if (t.length == 2 && t[0].getText().equals("$") && t[1].getType() == Type.NUMBER) {
            long addr = t[1].getValue();
            Disassembler disasm = code.getDisassembler();
            if (disasm != null) {
                name = disasm.getName(addr);
            }
            if (name != null) {
                return new Operand(new Token(Type.OTHER, "("), new Token(Type.LABEL, name, addr), new Token(Type.OTHER, ")"));
            } else {
                return new Operand(new Token(Type.OTHER, "("), new Token(Type.OTHER, "$"), new Token(Type.ADDRESS, t[1].getText(), t[1].getValue()), new Token(Type.OTHER, ")"));
            }
        } else {
            Token[] tokens = new Token[t.length + 2];
            System.arraycopy(t, 0, tokens, 1, t.length);
            tokens[0] = new Token(Type.OTHER, "(");
            tokens[tokens.length - 1] = new Token(Type.OTHER, ")");
            return new Operand(tokens);
        }
    }
}
