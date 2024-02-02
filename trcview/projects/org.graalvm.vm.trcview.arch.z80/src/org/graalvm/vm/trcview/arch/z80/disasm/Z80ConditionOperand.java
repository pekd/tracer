package org.graalvm.vm.trcview.arch.z80.disasm;

import org.graalvm.vm.trcview.disasm.Operand;
import org.graalvm.vm.trcview.disasm.Type;

public class Z80ConditionOperand extends Z80Operand {
    private final int cond;

    public Z80ConditionOperand(int cond) {
        this.cond = cond;
    }

    @Override
    public String toString() {
        switch (cond) {
            case Z80Instruction.Z:
                return "z";
            case Z80Instruction.NZ:
                return "nz";
            case Z80Instruction.C:
                return "c";
            case Z80Instruction.NC:
                return "nc";
            case Z80Instruction.M:
                return "m";
            case Z80Instruction.P:
                return "p";
            case Z80Instruction.PE:
                return "pe";
            case Z80Instruction.PO:
                return "po";
            default:
                return "??";
        }
    }

    @Override
    public Operand disassemble(Z80MachineCode code) {
        return new Operand(Type.OTHER, toString());
    }
}
