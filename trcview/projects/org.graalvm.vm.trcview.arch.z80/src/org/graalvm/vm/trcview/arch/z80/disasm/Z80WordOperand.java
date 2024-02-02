package org.graalvm.vm.trcview.arch.z80.disasm;

import org.graalvm.vm.trcview.arch.Disassembler;
import org.graalvm.vm.trcview.disasm.Operand;
import org.graalvm.vm.trcview.disasm.Token;
import org.graalvm.vm.trcview.disasm.Type;
import org.graalvm.vm.util.HexFormatter;

public class Z80WordOperand extends Z80Operand {
    @Override
    public String toString() {
        return "W";
    }

    @Override
    public Operand disassemble(Z80MachineCode code) {
        long value = Short.toUnsignedInt(code.nextI16());
        String name = null;
        if (code.isBranch()) {
            Disassembler disasm = code.getDisassembler();
            if (disasm != null) {
                name = disasm.getName(value);
            }
        }
        if (name != null) {
            return new Operand(Type.LABEL, name, value);
        } else {
            return new Operand(new Token(Type.OTHER, "$"), new Token(Type.NUMBER, HexFormatter.tohex(value, 4), value));
        }
    }
}
