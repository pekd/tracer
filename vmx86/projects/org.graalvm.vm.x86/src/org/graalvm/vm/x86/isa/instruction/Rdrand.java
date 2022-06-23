package org.graalvm.vm.x86.isa.instruction;

import java.security.SecureRandom;

import org.graalvm.vm.x86.ArchitecturalState;
import org.graalvm.vm.x86.isa.AMD64Instruction;
import org.graalvm.vm.x86.isa.Operand;
import org.graalvm.vm.x86.isa.OperandDecoder;
import org.graalvm.vm.x86.node.WriteFlagNode;
import org.graalvm.vm.x86.node.WriteNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class Rdrand extends AMD64Instruction {
    private static final SecureRandom rng = new SecureRandom();

    private final Operand dst;
    @Child protected WriteNode writeDst;
    @Child protected WriteFlagNode writeCF;

    protected Rdrand(long pc, byte[] instruction, Operand dst) {
        super(pc, instruction);
        this.dst = dst;

        setGPRWriteOperands(dst);
    }

    @Override
    protected void createChildNodes() {
        ArchitecturalState state = getState();
        writeDst = dst.createWrite(state, next());
        writeCF = state.getRegisters().getCF().createWrite();
    }

    public static class Rdrandw extends Rdrand {
        public Rdrandw(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands.getOperand1(OperandDecoder.R16));
        }

        @Override
        public long executeInstruction(VirtualFrame frame) {
            short value = (short) rng.nextInt();
            writeDst.executeI16(frame, value);
            writeCF.execute(frame, true);
            return next();
        }
    }

    public static class Rdrandl extends Rdrand {
        public Rdrandl(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands.getOperand1(OperandDecoder.R32));
        }

        @Override
        public long executeInstruction(VirtualFrame frame) {
            int value = rng.nextInt();
            writeDst.executeI32(frame, value);
            writeCF.execute(frame, true);
            return next();
        }
    }

    public static class Rdrandq extends Rdrand {
        public Rdrandq(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands.getOperand1(OperandDecoder.R64));
        }

        @Override
        public long executeInstruction(VirtualFrame frame) {
            long value = rng.nextLong();
            writeDst.executeI64(frame, value);
            writeCF.execute(frame, true);
            return next();
        }
    }

    @Override
    protected String[] disassemble() {
        return new String[]{"rdrand", dst.toString()};
    }
}
