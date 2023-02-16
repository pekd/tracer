package org.graalvm.vm.x86.el.ast;

import org.graalvm.vm.x86.ArchitecturalState;
import org.graalvm.vm.x86.RegisterAccessFactory;
import org.graalvm.vm.x86.isa.Register;
import org.graalvm.vm.x86.node.ReadFlagsNode;
import org.graalvm.vm.x86.node.ReadNode;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;

public class VariableNode extends Expression {
    private final String name;

    @Child private ReadVariableNode read;

    public VariableNode(String name, ArchitecturalState state) {
        this.name = name;
        read = create(name, state);
        if (read instanceof ReadRegister) {
            setGPRRead(((ReadRegister) read).getGPR());
        }
    }

    private static interface ReadRegister {
        Register getGPR();
    }

    private abstract static class ReadVariableNode extends Node {
        public abstract long execute(VirtualFrame frame, long pc);
    }

    private static class ReadI8VariableNode extends ReadVariableNode {
        @Child private ReadNode read;

        private ReadI8VariableNode(ReadNode node) {
            read = node;
        }

        @Override
        public long execute(VirtualFrame frame, long pc) {
            return read.executeI8(frame);
        }
    }

    private static class ReadI16VariableNode extends ReadVariableNode {
        @Child private ReadNode read;

        private ReadI16VariableNode(ReadNode node) {
            read = node;
        }

        @Override
        public long execute(VirtualFrame frame, long pc) {
            return read.executeI16(frame);
        }
    }

    private static class ReadI32VariableNode extends ReadVariableNode {
        @Child private ReadNode read;

        private ReadI32VariableNode(ReadNode node) {
            read = node;
        }

        @Override
        public long execute(VirtualFrame frame, long pc) {
            return read.executeI32(frame);
        }
    }

    private static class ReadI64VariableNode extends ReadVariableNode {
        @Child private ReadNode read;

        private ReadI64VariableNode(ReadNode node) {
            read = node;
        }

        @Override
        public long execute(VirtualFrame frame, long pc) {
            return read.executeI64(frame);
        }
    }

    private static class ReadIPNode extends ReadVariableNode {
        @Override
        public long execute(VirtualFrame frame, long pc) {
            return (short) pc;
        }
    }

    private static class ReadEIPNode extends ReadVariableNode {
        @Override
        public long execute(VirtualFrame frame, long pc) {
            return (int) pc;
        }
    }

    private static class ReadRIPNode extends ReadVariableNode {
        @Override
        public long execute(VirtualFrame frame, long pc) {
            return pc;
        }
    }

    private static class ReadI8RegisterNode extends ReadI8VariableNode implements ReadRegister {
        private final Register reg;

        private ReadI8RegisterNode(RegisterAccessFactory regs, Register r) {
            super(regs.getRegister(r).createRead());
            this.reg = r;
        }

        @Override
        public Register getGPR() {
            return reg;
        }
    }

    private static class ReadI16RegisterNode extends ReadI16VariableNode implements ReadRegister {
        private final Register reg;

        private ReadI16RegisterNode(RegisterAccessFactory regs, Register r) {
            super(regs.getRegister(r).createRead());
            this.reg = r;
        }

        @Override
        public Register getGPR() {
            return reg;
        }
    }

    private static class ReadI32RegisterNode extends ReadI16VariableNode implements ReadRegister {
        private final Register reg;

        private ReadI32RegisterNode(RegisterAccessFactory regs, Register r) {
            super(regs.getRegister(r).createRead());
            this.reg = r;
        }

        @Override
        public Register getGPR() {
            return reg;
        }
    }

    private static class ReadI64RegisterNode extends ReadI16VariableNode implements ReadRegister {
        private final Register reg;

        private ReadI64RegisterNode(RegisterAccessFactory regs, Register r) {
            super(regs.getRegister(r).createRead());
            this.reg = r;
        }

        @Override
        public Register getGPR() {
            return reg;
        }
    }

    private static ReadVariableNode create(String name, ArchitecturalState state) {
        CompilerAsserts.neverPartOfCompilation();
        RegisterAccessFactory reg = state.getRegisters();
        switch (name) {
            case "al":
                return new ReadI8RegisterNode(reg, Register.AL);
            case "ah":
                return new ReadI8RegisterNode(reg, Register.AH);
            case "ax":
                return new ReadI16RegisterNode(reg, Register.AX);
            case "eax":
                return new ReadI32RegisterNode(reg, Register.EAX);
            case "rax":
                return new ReadI64RegisterNode(reg, Register.RAX);
            case "bl":
                return new ReadI8RegisterNode(reg, Register.BL);
            case "bh":
                return new ReadI8RegisterNode(reg, Register.BH);
            case "bx":
                return new ReadI16RegisterNode(reg, Register.BX);
            case "ebx":
                return new ReadI32RegisterNode(reg, Register.EBX);
            case "rbx":
                return new ReadI64RegisterNode(reg, Register.RBX);
            case "cl":
                return new ReadI8RegisterNode(reg, Register.CL);
            case "ch":
                return new ReadI8RegisterNode(reg, Register.CH);
            case "cx":
                return new ReadI16RegisterNode(reg, Register.CX);
            case "ecx":
                return new ReadI32RegisterNode(reg, Register.ECX);
            case "rcx":
                return new ReadI64RegisterNode(reg, Register.RCX);
            case "dl":
                return new ReadI8RegisterNode(reg, Register.DL);
            case "dh":
                return new ReadI8RegisterNode(reg, Register.DH);
            case "dx":
                return new ReadI16RegisterNode(reg, Register.DX);
            case "edx":
                return new ReadI32RegisterNode(reg, Register.EDX);
            case "rdx":
                return new ReadI64RegisterNode(reg, Register.RDX);
            case "bpl":
                return new ReadI8RegisterNode(reg, Register.BPL);
            case "bp":
                return new ReadI16RegisterNode(reg, Register.BP);
            case "ebp":
                return new ReadI32RegisterNode(reg, Register.EBP);
            case "rbp":
                return new ReadI64RegisterNode(reg, Register.RBP);
            case "spl":
                return new ReadI8RegisterNode(reg, Register.SPL);
            case "sp":
                return new ReadI16RegisterNode(reg, Register.SP);
            case "esp":
                return new ReadI32RegisterNode(reg, Register.ESP);
            case "rsp":
                return new ReadI64RegisterNode(reg, Register.RSP);
            case "sil":
                return new ReadI8RegisterNode(reg, Register.SIL);
            case "si":
                return new ReadI16RegisterNode(reg, Register.SI);
            case "esi":
                return new ReadI32RegisterNode(reg, Register.ESI);
            case "rsi":
                return new ReadI64RegisterNode(reg, Register.RSI);
            case "dil":
                return new ReadI8RegisterNode(reg, Register.DIL);
            case "di":
                return new ReadI16RegisterNode(reg, Register.DI);
            case "edi":
                return new ReadI32RegisterNode(reg, Register.EDI);
            case "rdi":
                return new ReadI64RegisterNode(reg, Register.RDI);
            case "r8b":
                return new ReadI8RegisterNode(reg, Register.R8B);
            case "r8w":
                return new ReadI16RegisterNode(reg, Register.R8W);
            case "r8d":
                return new ReadI32RegisterNode(reg, Register.R8D);
            case "r8":
                return new ReadI64RegisterNode(reg, Register.R8);
            case "r9b":
                return new ReadI8RegisterNode(reg, Register.R9B);
            case "r9w":
                return new ReadI16RegisterNode(reg, Register.R9W);
            case "r9d":
                return new ReadI32RegisterNode(reg, Register.R9D);
            case "r9":
                return new ReadI64RegisterNode(reg, Register.R9);
            case "r10b":
                return new ReadI8RegisterNode(reg, Register.R10B);
            case "r10w":
                return new ReadI16RegisterNode(reg, Register.R10W);
            case "r10d":
                return new ReadI32RegisterNode(reg, Register.R10D);
            case "r10":
                return new ReadI64RegisterNode(reg, Register.R10);
            case "r11b":
                return new ReadI8RegisterNode(reg, Register.R11B);
            case "r11w":
                return new ReadI16RegisterNode(reg, Register.R11W);
            case "r11d":
                return new ReadI32RegisterNode(reg, Register.R11D);
            case "r11":
                return new ReadI64RegisterNode(reg, Register.R11);
            case "r12b":
                return new ReadI8RegisterNode(reg, Register.R12B);
            case "r12w":
                return new ReadI16RegisterNode(reg, Register.R12W);
            case "r12d":
                return new ReadI32RegisterNode(reg, Register.R12D);
            case "r12":
                return new ReadI64RegisterNode(reg, Register.R12);
            case "r13b":
                return new ReadI8RegisterNode(reg, Register.R13B);
            case "r13w":
                return new ReadI16RegisterNode(reg, Register.R13W);
            case "r13d":
                return new ReadI32RegisterNode(reg, Register.R13D);
            case "r13":
                return new ReadI64RegisterNode(reg, Register.R13);
            case "r14b":
                return new ReadI8RegisterNode(reg, Register.R14B);
            case "r14w":
                return new ReadI16RegisterNode(reg, Register.R14W);
            case "r14d":
                return new ReadI32RegisterNode(reg, Register.R14D);
            case "r14":
                return new ReadI64RegisterNode(reg, Register.R14);
            case "r15b":
                return new ReadI8RegisterNode(reg, Register.R15B);
            case "r15w":
                return new ReadI16RegisterNode(reg, Register.R15W);
            case "r15d":
                return new ReadI32RegisterNode(reg, Register.R15D);
            case "r15":
                return new ReadI64RegisterNode(reg, Register.R15);
            case "flags":
                return new ReadI16VariableNode(new ReadFlagsNode());
            case "eflags":
                return new ReadI32VariableNode(new ReadFlagsNode());
            case "rflags":
                return new ReadI64VariableNode(new ReadFlagsNode());
            case "ip":
                return new ReadIPNode();
            case "eip":
                return new ReadEIPNode();
            case "rip":
                return new ReadRIPNode();
        }
        throw new IllegalArgumentException("invalid register");
    }

    @Override
    public long execute(VirtualFrame frame, long pc) {
        return read.execute(frame, pc);
    }

    @Override
    public String toString() {
        CompilerAsserts.neverPartOfCompilation();
        return name;
    }

    @Override
    public VariableNode clone() {
        CompilerAsserts.neverPartOfCompilation();
        ArchitecturalState state = getContextReference().get(this).getState();
        return new VariableNode(name, state);
    }
}
