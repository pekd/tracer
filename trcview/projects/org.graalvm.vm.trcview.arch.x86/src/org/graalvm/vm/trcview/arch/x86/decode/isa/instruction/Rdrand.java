package org.graalvm.vm.trcview.arch.x86.decode.isa.instruction;

import org.graalvm.vm.trcview.arch.x86.decode.isa.AMD64Instruction;
import org.graalvm.vm.trcview.arch.x86.decode.isa.Operand;
import org.graalvm.vm.trcview.arch.x86.decode.isa.OperandDecoder;
import org.graalvm.vm.trcview.disasm.AssemblerInstruction;

public abstract class Rdrand extends AMD64Instruction {
    private final Operand dst;

    protected Rdrand(long pc, byte[] instruction, Operand dst) {
        super(pc, instruction);
        this.dst = dst;

        setGPRWriteOperands(dst);
    }

    public static class Rdrandw extends Rdrand {
        public Rdrandw(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands.getOperand1(OperandDecoder.R16));
        }
    }

    public static class Rdrandl extends Rdrand {
        public Rdrandl(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands.getOperand1(OperandDecoder.R32));
        }
    }

    public static class Rdrandq extends Rdrand {
        public Rdrandq(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands.getOperand1(OperandDecoder.R64));
        }
    }

    @Override
    protected AssemblerInstruction disassemble() {
        return new AssemblerInstruction("rdrand", dst);
    }
}
