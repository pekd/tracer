package org.graalvm.vm.trcview.arch.x86.io;

import java.io.IOException;

import org.graalvm.vm.memory.vector.Vector128;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.x86.node.debug.trace.StepRecord;

public class AMD64FullCpuState extends AMD64CpuState {
    private final long rax;
    private final long rbx;
    private final long rcx;
    private final long rdx;
    private final long rsi;
    private final long rdi;
    private final long rbp;
    private final long rsp;
    private final long r8;
    private final long r9;
    private final long r10;
    private final long r11;
    private final long r12;
    private final long r13;
    private final long r14;
    private final long r15;
    private final long rip;

    private final long fs;
    private final long gs;

    private final long rfl;

    private final Vector128[] xmm = new Vector128[16];

    protected AMD64FullCpuState(WordInputStream in, int tid, byte[] machinecode) throws IOException {
        super(tid, machinecode);
        rax = in.read64bit();
        rbx = in.read64bit();
        rcx = in.read64bit();
        rdx = in.read64bit();
        rsi = in.read64bit();
        rdi = in.read64bit();
        rbp = in.read64bit();
        rsp = in.read64bit();
        r8 = in.read64bit();
        r9 = in.read64bit();
        r10 = in.read64bit();
        r11 = in.read64bit();
        r12 = in.read64bit();
        r13 = in.read64bit();
        r14 = in.read64bit();
        r15 = in.read64bit();
        rip = in.read64bit();
        fs = in.read64bit();
        gs = in.read64bit();
        rfl = in.read64bit();
        step = in.read64bit();
        for (int i = 0; i < xmm.length; i++) {
            long v0 = in.read64bit();
            long v1 = in.read64bit();
            xmm[i] = new Vector128(v0, v1);
        }
    }

    public AMD64FullCpuState(StepRecord record) {
        super(record.getTid(), record.getMachinecode());
        org.graalvm.vm.x86.isa.CpuState state = record.getState().getState();
        rax = state.rax;
        rbx = state.rbx;
        rcx = state.rcx;
        rdx = state.rdx;
        rsi = state.rsi;
        rdi = state.rdi;
        rbp = state.rbp;
        rsp = state.rsp;
        r8 = state.r8;
        r9 = state.r9;
        r10 = state.r10;
        r11 = state.r11;
        r12 = state.r12;
        r13 = state.r13;
        r14 = state.r14;
        r15 = state.r15;
        rip = state.rip;
        fs = state.fs;
        gs = state.gs;
        for (int i = 0; i < xmm.length; i++) {
            xmm[i] = state.xmm[i];
        }
        rfl = state.getRFL();
        step = record.getInstructionCount();
    }

    @Override
    public long getRAX() {
        return rax;
    }

    @Override
    public long getRBX() {
        return rbx;
    }

    @Override
    public long getRCX() {
        return rcx;
    }

    @Override
    public long getRDX() {
        return rdx;
    }

    @Override
    public long getRBP() {
        return rbp;
    }

    @Override
    public long getRSP() {
        return rsp;
    }

    @Override
    public long getRIP() {
        return rip;
    }

    @Override
    public long getRSI() {
        return rsi;
    }

    @Override
    public long getRDI() {
        return rdi;
    }

    @Override
    public long getR8() {
        return r8;
    }

    @Override
    public long getR9() {
        return r9;
    }

    @Override
    public long getR10() {
        return r10;
    }

    @Override
    public long getR11() {
        return r11;
    }

    @Override
    public long getR12() {
        return r12;
    }

    @Override
    public long getR13() {
        return r13;
    }

    @Override
    public long getR14() {
        return r14;
    }

    @Override
    public long getR15() {
        return r15;
    }

    @Override
    public long getRFL() {
        return rfl;
    }

    @Override
    public long getFS() {
        return fs;
    }

    @Override
    public long getGS() {
        return gs;
    }

    @Override
    public Vector128 getXMM(int i) {
        return xmm[i];
    }
}
