package org.graalvm.vm.trcview.arch.x86.io;

import java.io.IOException;

import org.graalvm.vm.util.Vector128;
import org.graalvm.vm.util.io.WordInputStream;

public class AMD64DeltaCpuState extends AMD64CpuState {
    public static final int ID_RAX = 0;
    public static final int ID_RCX = 1;
    public static final int ID_RDX = 2;
    public static final int ID_RBX = 3;
    public static final int ID_RSP = 4;
    public static final int ID_RBP = 5;
    public static final int ID_RSI = 6;
    public static final int ID_RDI = 7;
    public static final int ID_R8 = 8;
    public static final int ID_R9 = 9;
    public static final int ID_R10 = 10;
    public static final int ID_R11 = 11;
    public static final int ID_R12 = 12;
    public static final int ID_R13 = 13;
    public static final int ID_R14 = 14;
    public static final int ID_R15 = 15;
    public static final int ID_FS = 16;
    public static final int ID_GS = 17;
    public static final int ID_RFL = 18;

    private final AMD64CpuState previous;
    private final long regMask;
    private final long rip;
    private final long[] values;

    public AMD64DeltaCpuState(WordInputStream in, int tid, byte[] machinecode, AMD64StepEvent last) throws IOException {
        super(tid, machinecode);
        previous = last.getState();

        rip = in.read64bit();
        step = in.read64bit();
        int cnt = in.read8bit();
        values = new long[cnt];
        long mask = 0;
        for (int i = 0; i < cnt; i++) {
            mask |= 1L << in.read8bit();
            values[i] = in.read64bit();
        }
        regMask = mask;
    }

    private boolean has(int i) {
        return (regMask & (1L << i)) != 0;
    }

    private int getIdx(int i) {
        int off = 0;
        for (int n = 0; n < i; n++) {
            if ((regMask & (1L << n)) != 0) {
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
        return has(ID_RAX) ? get(ID_RAX) : previous.getRAX();
    }

    @Override
    public long getRBX() {
        return has(ID_RBX) ? get(ID_RBX) : previous.getRBX();
    }

    @Override
    public long getRCX() {
        return has(ID_RCX) ? get(ID_RCX) : previous.getRCX();
    }

    @Override
    public long getRDX() {
        return has(ID_RDX) ? get(ID_RDX) : previous.getRDX();
    }

    @Override
    public long getRBP() {
        return has(ID_RBP) ? get(ID_RBP) : previous.getRBP();
    }

    @Override
    public long getRSP() {
        return has(ID_RSP) ? get(ID_RSP) : previous.getRSP();
    }

    @Override
    public long getRIP() {
        return rip;
    }

    @Override
    public long getRSI() {
        return has(ID_RSI) ? get(ID_RSI) : previous.getRSI();
    }

    @Override
    public long getRDI() {
        return has(ID_RDI) ? get(ID_RDI) : previous.getRDI();
    }

    @Override
    public long getR8() {
        return has(ID_R8) ? get(ID_R8) : previous.getR8();
    }

    @Override
    public long getR9() {
        return has(ID_R9) ? get(ID_R9) : previous.getR9();
    }

    @Override
    public long getR10() {
        return has(ID_R10) ? get(ID_R10) : previous.getR10();
    }

    @Override
    public long getR11() {
        return has(ID_R11) ? get(ID_R11) : previous.getR11();
    }

    @Override
    public long getR12() {
        return has(ID_R12) ? get(ID_R12) : previous.getR12();
    }

    @Override
    public long getR13() {
        return has(ID_R13) ? get(ID_R13) : previous.getR13();
    }

    @Override
    public long getR14() {
        return has(ID_R14) ? get(ID_R14) : previous.getR14();
    }

    @Override
    public long getR15() {
        return has(ID_R15) ? get(ID_R15) : previous.getR15();
    }

    @Override
    public long getRFL() {
        return has(ID_RFL) ? get(ID_RFL) : previous.getRFL();
    }

    @Override
    public long getFS() {
        return has(ID_FS) ? get(ID_FS) : previous.getFS();
    }

    @Override
    public long getGS() {
        return has(ID_GS) ? get(ID_GS) : previous.getGS();
    }

    @Override
    public Vector128 getXMM(int i) {
        int id = 19 + i * 2;
        if (has(id)) {
            long hi = get(id + 0);
            long lo = get(id + 1);
            return new Vector128(hi, lo);
        } else {
            return previous.getXMM(i);
        }
    }
}
