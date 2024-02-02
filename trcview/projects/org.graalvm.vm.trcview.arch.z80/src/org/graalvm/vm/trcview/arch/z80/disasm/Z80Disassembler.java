package org.graalvm.vm.trcview.arch.z80.disasm;

import java.util.logging.Logger;

import org.graalvm.vm.trcview.arch.ByteCodeReader;
import org.graalvm.vm.trcview.arch.CodeReader;
import org.graalvm.vm.trcview.arch.Disassembler;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.disasm.AssemblerInstruction;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.util.log.Trace;

public class Z80Disassembler extends Disassembler {
    private static final Logger log = Trace.create(Z80Disassembler.class);

    public Z80Disassembler() {
        super();
    }

    public Z80Disassembler(TraceAnalyzer trc) {
        super(trc);
    }

    @Override
    public AssemblerInstruction disassemble(CodeReader code) {
        return Z80Instruction.disassemble(code, this);
    }

    @Override
    public String[] getDisassembly(CodeReader code) {
        return getDisassembly(disassemble(code));
    }

    @Override
    public int getLength(CodeReader code) {
        try {
            return Z80Info.getCodeLength(code);
        } catch (ArrayIndexOutOfBoundsException e) {
            log.warning("Invalid code detected at PC=" + HexFormatter.tohex(code.getPC(), 4));
            return 0;
        }
    }

    public static int getLength(byte[] machinecode) {
        try {
            return Z80Info.getCodeLength(new ByteCodeReader(machinecode, 0, false));
        } catch (ArrayIndexOutOfBoundsException e) {
            log.warning("Invalid code detected");
            return 0;
        }
    }

    public String[] getDisassembly(byte[] code, short pc) {
        String[] disasm = getDisassembly(new ByteCodeReader(code, Short.toUnsignedInt(pc), false));
        if (disasm != null) {
            return disasm;
        } else {
            StringBuilder buf = new StringBuilder();
            buf.append("; unknown [");
            buf.append(HexFormatter.tohex(Byte.toUnsignedInt(code[0]), 2));
            buf.append(']');
            return new String[]{buf.toString()};
        }
    }

    @Override
    public InstructionType getType(CodeReader code) {
        return type(code);
    }

    public static InstructionType getType(byte[] machinecode) {
        return type(new ByteCodeReader(machinecode, 0, false));
    }

    public static InstructionType type(CodeReader code) {
        try {
            Z80Instruction insn = Z80Instruction.getInstruction(code);
            // this may or may not throw an NPE, for performance reasons we just catch it
            return insn.getType();
        } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
            log.warning("Invalid code detected at PC=" + HexFormatter.tohex(code.getPC(), 4));
            return InstructionType.OTHER;
        }
    }

    public static int getCondition(byte[] machinecode) {
        return getCondition(new ByteCodeReader(machinecode, 0, false));
    }

    public static int getCondition(CodeReader code) {
        try {
            Z80Instruction insn = Z80Instruction.getInstruction(code);
            // this may or may not throw an NPE, for performance reasons we just catch it
            return insn.getCondition();
        } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
            log.warning("Invalid code detected at PC=" + HexFormatter.tohex(code.getPC(), 4));
            return Z80Instruction.ALWAYS;
        }
    }
}
