package org.graalvm.vm.trcview.arch.x86.io;

import org.graalvm.vm.memory.vector.Vector128;
import org.graalvm.vm.x86.node.debug.trace.DeltaCpuStateRecord;
import org.graalvm.vm.x86.node.debug.trace.StepRecord;

public class AMD64DeltaCpuState extends AMD64CpuState {
    private final AMD64CpuState previous;
    private final int regMask;
    private final long rip;
    private final int efl;
    private final long[] values;

    protected AMD64DeltaCpuState(int tid, byte[] machinecode, AMD64CpuState previous, StepRecord record, int mask) {
        super(tid, machinecode);
        org.graalvm.vm.x86.isa.CpuState current = record.getState().getState();
        this.previous = previous;
        this.regMask = mask;
        this.rip = current.rip;
        this.efl = (int) current.getRFL();
        int n = Integer.bitCount(mask);
        values = new long[n];
        for (int i = 0, w = 0; i < 16; i++) {
            if ((regMask & (1 << i)) != 0) {
                values[w++] = get(current, i);
            }
        }
    }

    private static long get(org.graalvm.vm.x86.isa.CpuState state, int i) {
        switch (i) {
            case RAX:
                return state.rax;
            case RBX:
                return state.rbx;
            case RCX:
                return state.rcx;
            case RDX:
                return state.rdx;
            case RSP:
                return state.rsp;
            case RBP:
                return state.rbp;
            case RSI:
                return state.rsi;
            case RDI:
                return state.rdi;
            case R8:
                return state.r8;
            case R9:
                return state.r9;
            case R10:
                return state.r10;
            case R11:
                return state.r11;
            case R12:
                return state.r12;
            case R13:
                return state.r13;
            case R14:
                return state.r14;
            case R15:
                return state.r15;
            default:
                throw new IllegalArgumentException("invalid register " + i);
        }
    }

    public static AMD64CpuState deltaState(AMD64CpuState previous, StepRecord last, StepRecord current) {
        int bits = 0;
        if (current.getState() instanceof DeltaCpuStateRecord) {
            DeltaCpuStateRecord record = (DeltaCpuStateRecord) current.getState();
            long delta = record.getDelta();
            if (Long.bitCount(delta) > 0) {
                if (record.getDeltaId(DeltaCpuStateRecord.ID_FS) | record.getDeltaId(DeltaCpuStateRecord.ID_GS)) {
                    return new AMD64FullCpuState(current);
                }
                bits = (int) (delta & 0xFFFF);
                for (int i = 0; i < 16; i++) {
                    if (record.getDeltaXMM(i)) {
                        bits |= 1 << (16 + i);
                    }
                }
            }
        } else {
            org.graalvm.vm.x86.isa.CpuState state = current.getState().getState();
            org.graalvm.vm.x86.isa.CpuState prev = last.getState().getState();
            if (state.fs != prev.fs || state.gs != prev.gs) {
                return new AMD64FullCpuState(current);
            }
            for (int i = 0; i < 16; i++) {
                if (get(prev, i) != get(state, i)) {
                    bits |= 1 << i;
                }
            }
            for (int i = 0; i < 16; i++) {
                if (!prev.xmm[i].equals(state.xmm[i])) {
                    bits |= 1 << (16 + i);
                }
            }
        }

        // no register changed (other than rip/rfl)
        if (bits == 0) {
            return new AMD64TinyDeltaCpuState(previous, current);
        }

        // only one register changed
        if (Integer.bitCount(bits & 0xFFFF) == 1 && (bits & 0xFFFF0000) == 0) {
            return new AMD64SmallDeltaCpuState(previous, current, (byte) Integer.numberOfTrailingZeros(bits));
        }

        // only integer register changed
        if ((bits & 0xFFFF0000) == 0) {
            return new AMD64DeltaCpuState(current.getTid(), current.getMachinecode(), previous, current, bits);
        }

        return new AMD64FullCpuState(current);
    }

    private boolean has(int i) {
        return (regMask & (1 << i)) != 0;
    }

    private int getIdx(int i) {
        int off = 0;
        for (int n = 0; n < i; n++) {
            if ((regMask & (1 << n)) != 0) {
                off++;
            }
        }
        return off;
    }

    private long get(int i) {
        return values[getIdx(i)];
    }

    @Override
    public long getRAX() {
        return has(RAX) ? get(RAX) : previous.getRAX();
    }

    @Override
    public long getRBX() {
        return has(RBX) ? get(RBX) : previous.getRBX();
    }

    @Override
    public long getRCX() {
        return has(RCX) ? get(RCX) : previous.getRCX();
    }

    @Override
    public long getRDX() {
        return has(RDX) ? get(RDX) : previous.getRDX();
    }

    @Override
    public long getRBP() {
        return has(RBP) ? get(RBP) : previous.getRBP();
    }

    @Override
    public long getRSP() {
        return has(RSP) ? get(RSP) : previous.getRSP();
    }

    @Override
    public long getRIP() {
        return rip;
    }

    @Override
    public long getRSI() {
        return has(RSI) ? get(RSI) : previous.getRSI();
    }

    @Override
    public long getRDI() {
        return has(RDI) ? get(RDI) : previous.getRDI();
    }

    @Override
    public long getR8() {
        return has(R8) ? get(R8) : previous.getR8();
    }

    @Override
    public long getR9() {
        return has(R9) ? get(R9) : previous.getR9();
    }

    @Override
    public long getR10() {
        return has(R10) ? get(R10) : previous.getR10();
    }

    @Override
    public long getR11() {
        return has(R11) ? get(R11) : previous.getR11();
    }

    @Override
    public long getR12() {
        return has(R12) ? get(R12) : previous.getR12();
    }

    @Override
    public long getR13() {
        return has(R13) ? get(R13) : previous.getR13();
    }

    @Override
    public long getR14() {
        return has(R14) ? get(R14) : previous.getR14();
    }

    @Override
    public long getR15() {
        return has(R15) ? get(R15) : previous.getR15();
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
