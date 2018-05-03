package org.graalvm.vm.x86.isa.instruction;

import org.graalvm.vm.memory.vector.Vector128;
import org.graalvm.vm.x86.ArchitecturalState;
import org.graalvm.vm.x86.isa.AMD64Instruction;
import org.graalvm.vm.x86.isa.Operand;
import org.graalvm.vm.x86.isa.OperandDecoder;
import org.graalvm.vm.x86.node.ReadNode;
import org.graalvm.vm.x86.node.WriteNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class Psubus extends AMD64Instruction {
    private final String name;
    private final Operand operand1;
    private final Operand operand2;

    @Child protected ReadNode readA;
    @Child protected ReadNode readB;
    @Child protected WriteNode writeDst;

    protected Psubus(long pc, byte[] instruction, Operand operand1, Operand operand2, String name) {
        super(pc, instruction);
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.name = name;

        setGPRReadOperands(operand1, operand2);
        setGPRWriteOperands(operand1);
    }

    protected void createChildrenIfNecessary() {
        if (readA == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            ArchitecturalState state = getContextReference().get().getState();
            readA = operand1.createRead(state, next());
            readB = operand2.createRead(state, next());
            writeDst = operand1.createWrite(state, next());
        }
    }

    public static class Psubusb extends Psubus {
        public Psubusb(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands.getAVXOperand2(128), operands.getAVXOperand1(128), "psubusb");
        }

        @Override
        public long executeInstruction(VirtualFrame frame) {
            createChildrenIfNecessary();
            Vector128 a = readA.executeI128(frame);
            Vector128 b = readB.executeI128(frame);
            Vector128 result = a.subPackedI8SaturateUnsigned(b);
            writeDst.executeI128(frame, result);
            return next();
        }
    }

    public static class Psubusw extends Psubus {
        public Psubusw(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands.getAVXOperand2(128), operands.getAVXOperand1(128), "psubusw");
        }

        @Override
        public long executeInstruction(VirtualFrame frame) {
            createChildrenIfNecessary();
            Vector128 a = readA.executeI128(frame);
            Vector128 b = readB.executeI128(frame);
            Vector128 result = a.subPackedI16SaturateUnsigned(b);
            writeDst.executeI128(frame, result);
            return next();
        }
    }

    @Override
    protected String[] disassemble() {
        return new String[]{name, operand1.toString(), operand2.toString()};
    }
}