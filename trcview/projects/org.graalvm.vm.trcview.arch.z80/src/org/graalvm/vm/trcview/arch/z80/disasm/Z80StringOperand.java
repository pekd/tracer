package org.graalvm.vm.trcview.arch.z80.disasm;

import org.graalvm.vm.trcview.disasm.Operand;
import org.graalvm.vm.trcview.disasm.Type;

public class Z80StringOperand extends Z80Operand {
    private final String value;

    public Z80StringOperand(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public Operand disassemble(Z80MachineCode code) {
        return new Operand(Type.OTHER, value);
    }
}
