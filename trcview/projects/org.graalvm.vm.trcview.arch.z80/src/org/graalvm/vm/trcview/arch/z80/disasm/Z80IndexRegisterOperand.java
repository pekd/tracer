package org.graalvm.vm.trcview.arch.z80.disasm;

import org.graalvm.vm.trcview.disasm.Operand;
import org.graalvm.vm.trcview.disasm.Type;

public class Z80IndexRegisterOperand extends Z80Operand {
    private final String suffix;

    public Z80IndexRegisterOperand() {
        suffix = null;
    }

    public Z80IndexRegisterOperand(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public String toString() {
        if (suffix != null) {
            return "I" + suffix;
        } else {
            return "I";
        }
    }

    @Override
    public Operand disassemble(Z80MachineCode code) {
        String r = code.isIX() ? "ix" : "iy";
        if (suffix != null) {
            r += suffix;
        }
        return new Operand(Type.REGISTER, r);
    }
}
