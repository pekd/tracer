package org.graalvm.vm.trcview.arch.z80.disasm;

import org.graalvm.vm.trcview.disasm.Operand;
import org.graalvm.vm.trcview.disasm.Token;
import org.graalvm.vm.trcview.disasm.Type;
import org.graalvm.vm.util.HexFormatter;

public class Z80ByteOperand extends Z80Operand {
    @Override
    public String toString() {
        return "R";
    }

    @Override
    public Operand disassemble(Z80MachineCode code) {
        long value = Byte.toUnsignedInt(code.nextI8());
        return new Operand(new Token(Type.OTHER, "$"), new Token(Type.NUMBER, HexFormatter.tohex(value, 2), value));
    }
}
