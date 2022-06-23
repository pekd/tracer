package org.graalvm.vm.trcview.arch.x86.io;

import org.graalvm.vm.memory.vector.Vector128;
import org.graalvm.vm.x86.node.debug.trace.StepRecord;

public class AMD64SmallDeltaCpuState extends AMD64CpuState {
    private final byte type;
    private final long value;
    private final long rip;
    private final int efl;

    private AMD64CpuState previous;

    public AMD64SmallDeltaCpuState(AMD64CpuState previous, StepRecord current, byte type) {
        super(current.getTid(), current.getMachinecode());
        this.previous = previous;
        this.type = type;
        org.graalvm.vm.x86.isa.CpuState state = current.getState().getState();
        step = current.getInstructionCount();
        rip = current.getPC();
        efl = (int) state.getRFL();
        switch (type) {
            case RAX:
                value = state.rax;
                break;
            case RBX:
                value = state.rbx;
                break;
            case RCX:
                value = state.rcx;
                break;
            case RDX:
                value = state.rdx;
                break;
            case RSP:
                value = state.rsp;
                break;
            case RBP:
                value = state.rbp;
                break;
            case RSI:
                value = state.rsi;
                break;
            case RDI:
                value = state.rdi;
                break;
            case R8:
                value = state.r8;
                break;
            case R9:
                value = state.r9;
                break;
            case R10:
                value = state.r10;
                break;
            case R11:
                value = state.r11;
                break;
            case R12:
                value = state.r12;
                break;
            case R13:
                value = state.r13;
                break;
            case R14:
                value = state.r14;
                break;
            case R15:
                value = state.r15;
                break;
            default:
                throw new IllegalArgumentException("invalid type: " + type);
        }
    }

    @Override
    public long getRAX() {
        return type == RAX ? value : previous.getRAX();
    }

    @Override
    public long getRBX() {
        return type == RBX ? value : previous.getRBX();
    }

    @Override
    public long getRCX() {
        return type == RCX ? value : previous.getRCX();
    }

    @Override
    public long getRDX() {
        return type == RDX ? value : previous.getRDX();
    }

    @Override
    public long getRBP() {
        return type == RBP ? value : previous.getRBP();
    }

    @Override
    public long getRSP() {
        return type == RSP ? value : previous.getRSP();
    }

    @Override
    public long getRIP() {
        return rip;
    }

    @Override
    public long getRSI() {
        return type == RSI ? value : previous.getRSI();
    }

    @Override
    public long getRDI() {
        return type == RDI ? value : previous.getRDI();
    }

    @Override
    public long getR8() {
        return type == R8 ? value : previous.getR8();
    }

    @Override
    public long getR9() {
        return type == R9 ? value : previous.getR9();
    }

    @Override
    public long getR10() {
        return type == R10 ? value : previous.getR10();
    }

    @Override
    public long getR11() {
        return type == R11 ? value : previous.getR11();
    }

    @Override
    public long getR12() {
        return type == R12 ? value : previous.getR12();
    }

    @Override
    public long getR13() {
        return type == R13 ? value : previous.getR13();
    }

    @Override
    public long getR14() {
        return type == R14 ? value : previous.getR14();
    }

    @Override
    public long getR15() {
        return type == R15 ? value : previous.getR15();
    }

    @Override
    public long getRFL() {
        return efl;
    }

    @Override
    public long getFS() {
        return previous.getFS();
    }

    @Override
    public long getGS() {
        return previous.getGS();
    }

    @Override
    public Vector128 getXMM(int i) {
        return previous.getXMM(i);
    }
}
