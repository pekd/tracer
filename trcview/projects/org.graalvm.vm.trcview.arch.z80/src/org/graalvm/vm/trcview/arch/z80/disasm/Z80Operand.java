package org.graalvm.vm.trcview.arch.z80.disasm;

import org.graalvm.vm.trcview.disasm.Operand;

public abstract class Z80Operand {
    @Override
    public abstract String toString();

    public abstract Operand disassemble(Z80MachineCode code);
}
