package org.graalvm.vm.trcview.arch.z80.disasm;

import org.graalvm.vm.trcview.disasm.Operand;
import org.graalvm.vm.trcview.disasm.Token;
import org.graalvm.vm.trcview.disasm.Type;
import org.graalvm.vm.util.HexFormatter;

public class Z80IndexedOperand extends Z80Operand {
    private final boolean read;

    public Z80IndexedOperand(boolean read) {
        this.read = read;
    }

    private static String sign(byte x) {
        return x < 0 ? "-" : "+";
    }

    private static int abs(byte x) {
        if (x < 0) {
            return -x;
        } else {
            return x;
        }
    }

    @Override
    public String toString() {
        return read ? "X" : "Y";
    }

    @Override
    public Operand disassemble(Z80MachineCode code) {
        String r = code.isIX() ? "ix" : "iy";
        byte offset = read ? code.nextI8() : code.getOffset();
        return new Operand(new Token(Type.OTHER, "("), new Token(Type.REGISTER, r), new Token(Type.OTHER, sign(offset)), new Token(Type.OTHER, "$"),
                        new Token(Type.NUMBER, HexFormatter.tohex(abs(offset), 2), abs(offset)), new Token(Type.OTHER, ")"));
    }
}
