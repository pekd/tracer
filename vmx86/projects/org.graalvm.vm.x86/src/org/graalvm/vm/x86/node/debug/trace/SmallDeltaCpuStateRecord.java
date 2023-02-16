package org.graalvm.vm.x86.node.debug.trace;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;
import org.graalvm.vm.x86.isa.CpuState;

public class SmallDeltaCpuStateRecord extends CpuStateRecord {
    public static final byte ID = 0x03;

    private CpuState state;

    private long rip;
    private long step;

    private byte reg;
    private long value;

    public SmallDeltaCpuStateRecord() {
        super(ID);
    }

    public SmallDeltaCpuStateRecord(byte[] machinecode, CpuState lastState, CpuState state) {
        super(ID, machinecode);
        this.state = state;

        rip = state.rip;
        step = state.instructionCount;

        if (lastState.rax != state.rax) {
            reg = ID_RAX;
            value = state.rax;
        } else if (lastState.rcx != state.rcx) {
            reg = ID_RCX;
            value = state.rcx;
        } else if (lastState.rdx != state.rdx) {
            reg = ID_RDX;
            value = state.rdx;
        } else if (lastState.rbx != state.rbx) {
            reg = ID_RBX;
            value = state.rbx;
        } else if (lastState.rsp != state.rsp) {
            reg = ID_RSP;
            value = state.rsp;
        } else if (lastState.rbp != state.rbp) {
            reg = ID_RBP;
            value = state.rbp;
        } else if (lastState.rsi != state.rsi) {
            reg = ID_RSI;
            value = state.rsi;
        } else if (lastState.rdi != state.rdi) {
            reg = ID_RDI;
            value = state.rdi;
        } else if (lastState.r8 != state.r8) {
            reg = ID_R8;
            value = state.r8;
        } else if (lastState.r9 != state.r9) {
            reg = ID_R9;
            value = state.r9;
        } else if (lastState.r10 != state.r10) {
            reg = ID_R10;
            value = state.r10;
        } else if (lastState.r11 != state.r11) {
            reg = ID_R11;
            value = state.r11;
        } else if (lastState.r12 != state.r12) {
            reg = ID_R12;
            value = state.r12;
        } else if (lastState.r13 != state.r13) {
            reg = ID_R13;
            value = state.r13;
        } else if (lastState.r14 != state.r14) {
            reg = ID_R14;
            value = state.r14;
        } else if (lastState.r15 != state.r15) {
            reg = ID_R15;
            value = state.r15;
        } else if (lastState.fs != state.fs) {
            reg = ID_FS;
            value = state.fs;
        } else if (lastState.gs != state.gs) {
            reg = ID_GS;
            value = state.gs;
        } else if (lastState.getRFL() != state.getRFL()) {
            reg = ID_RFL;
            value = state.getRFL();
        }
    }

    @Override
    public CpuState getState() {
        return state;
    }

    @Override
    public long getPC() {
        return rip;
    }

    @Override
    public long getInstructionCount() {
        return step;
    }

    @Override
    protected int getDataSize() {
        return super.getDataSize() + 3 * 8 + 1;
    }

    @Override
    protected void readRecord(WordInputStream in) throws IOException {
        super.readRecord(in);
        rip = in.read64bit();
        step = in.read64bit();
        value = in.read64bit();
        reg = (byte) in.read8bit();
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        super.writeRecord(out);
        out.write64bit(rip);
        out.write64bit(step);
        out.write64bit(value);
        out.write8bit(reg);
    }
}
