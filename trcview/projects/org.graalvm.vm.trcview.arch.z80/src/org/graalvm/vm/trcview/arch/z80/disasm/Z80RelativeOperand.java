package org.graalvm.vm.trcview.arch.z80.disasm;

import org.graalvm.vm.trcview.arch.Disassembler;
import org.graalvm.vm.trcview.disasm.Operand;
import org.graalvm.vm.trcview.disasm.Token;
import org.graalvm.vm.trcview.disasm.Type;
import org.graalvm.vm.util.HexFormatter;

public class Z80RelativeOperand extends Z80Operand {
    @Override
    public String toString() {
        return "B";
    }

    @Override
    public Operand disassemble(Z80MachineCode code) {
        long addr = (code.getPC() + 2 + code.nextI8()) & 0xFFFF;
        Disassembler disasm = code.getDisassembler();
        String name = null;
        if (disasm != null) {
            name = disasm.getName(addr);
        }
        if (name != null) {
            return new Operand(Type.LABEL, name, addr);
        } else {
            return new Operand(new Token(Type.OTHER, "$"), new Token(Type.ADDRESS, HexFormatter.tohex(addr, 4), addr));
        }
    }
}
