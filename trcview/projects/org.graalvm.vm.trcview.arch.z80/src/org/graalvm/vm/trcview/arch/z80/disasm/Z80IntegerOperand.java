package org.graalvm.vm.trcview.arch.z80.disasm;

import org.graalvm.vm.trcview.disasm.Operand;
import org.graalvm.vm.trcview.disasm.Type;
import org.graalvm.vm.util.HexFormatter;

public class Z80IntegerOperand extends Z80Operand {
    private final int value;

    public Z80IntegerOperand(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        if (value < 10) {
            return Integer.toString(value);
        } else {
            return HexFormatter.tohex(value, 2) + "h";
        }
    }

    @Override
    public Operand disassemble(Z80MachineCode code) {
        return new Operand(Type.NUMBER, toString(), value);
    }
}
