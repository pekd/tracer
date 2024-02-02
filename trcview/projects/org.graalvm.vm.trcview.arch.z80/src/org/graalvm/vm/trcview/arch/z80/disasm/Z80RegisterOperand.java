package org.graalvm.vm.trcview.arch.z80.disasm;

import org.graalvm.vm.trcview.disasm.Operand;
import org.graalvm.vm.trcview.disasm.Type;

public class Z80RegisterOperand extends Z80Operand {
    private final String name;

    public Z80RegisterOperand(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Operand disassemble(Z80MachineCode code) {
        return new Operand(Type.REGISTER, name);
    }
}
