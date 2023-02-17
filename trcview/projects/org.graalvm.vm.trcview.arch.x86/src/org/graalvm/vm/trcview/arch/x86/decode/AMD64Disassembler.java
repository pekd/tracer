package org.graalvm.vm.trcview.arch.x86.decode;

import org.graalvm.vm.trcview.arch.CodeReader;
import org.graalvm.vm.trcview.arch.Disassembler;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.x86.decode.isa.AMD64Instruction;
import org.graalvm.vm.trcview.arch.x86.decode.isa.AMD64InstructionDecoder;
import org.graalvm.vm.trcview.arch.x86.decode.isa.AMD64InstructionQuickInfo;
import org.graalvm.vm.trcview.disasm.AssemblerInstruction;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public class AMD64Disassembler extends Disassembler {
    public AMD64Disassembler(TraceAnalyzer trc) {
        super(trc);
    }

    @Override
    public String[] getDisassembly(CodeReader code) {
        AMD64Instruction insn = AMD64InstructionDecoder.decode(code.getPC(), code);
        return insn.getDisassemblyComponents();
    }

    @Override
    public AssemblerInstruction disassemble(CodeReader code) {
        long pc = code.getPC();
        AMD64Instruction insn = AMD64InstructionDecoder.decode(pc, code);
        AssemblerInstruction asm = insn.getAssemblerInstruction();
        asm.setDisassembler(this);
        return asm;
    }

    public AssemblerInstruction disassemble(AMD64Instruction insn) {
        AssemblerInstruction asm = insn.getAssemblerInstruction();
        asm.setDisassembler(this);
        return asm;
    }

    @Override
    public int getLength(CodeReader code) {
        AMD64Instruction insn = AMD64InstructionDecoder.decode(code.getPC(), code);
        return insn.getSize();
    }

    @Override
    public InstructionType getType(CodeReader code) {
        byte[] data = new byte[2];
        data[0] = code.nextI8();
        try {
            data[1] = code.nextI8();
        } catch (Throwable t) {
            data = new byte[]{data[0]};
        }

        switch (AMD64InstructionQuickInfo.getType(data)) {
            case CALL:
                return InstructionType.CALL;
            case RET:
                return InstructionType.RET;
            case JMP:
                return InstructionType.JMP;
            case JMP_INDIRECT:
                return InstructionType.JMP_INDIRECT;
            case JCC:
                return InstructionType.JCC;
            case SYSCALL:
                return InstructionType.SYSCALL;
            default:
                return InstructionType.OTHER;
        }
    }
}
