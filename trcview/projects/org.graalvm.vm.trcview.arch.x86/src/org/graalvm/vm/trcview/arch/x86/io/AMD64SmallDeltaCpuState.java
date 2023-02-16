package org.graalvm.vm.trcview.arch.x86.io;

import java.io.IOException;

import org.graalvm.vm.util.Vector128;
import org.graalvm.vm.util.io.WordInputStream;

public class AMD64SmallDeltaCpuState extends AMD64CpuState {
    private final byte reg;
    private final long value;
    private final long rip;
    private final int efl;

    private AMD64CpuState previous;

    public AMD64SmallDeltaCpuState(WordInputStream in, int tid, byte[] machinecode, AMD64StepEvent last) throws IOException {
        super(tid, machinecode);
        previous = last.getState();

        rip = in.read64bit();
        step = in.read64bit();
        value = in.read64bit();
        reg = (byte) in.read8bit();

        if (reg == ID_RFL) {
            efl = (int) value;
        } else {
            efl = (int) previous.getRFL();
        }
    }

    @Override
    public long getRAX() {
        return reg == ID_RAX ? value : previous.getRAX();
    }

    @Override
    public long getRBX() {
        return reg == ID_RBX ? value : previous.getRBX();
    }

    @Override
    public long getRCX() {
        return reg == ID_RCX ? value : previous.getRCX();
    }

    @Override
    public long getRDX() {
        return reg == ID_RDX ? value : previous.getRDX();
    }

    @Override
    public long getRBP() {
        return reg == ID_RBP ? value : previous.getRBP();
    }

    @Override
    public long getRSP() {
        return reg == ID_RSP ? value : previous.getRSP();
    }

    @Override
    public long getRIP() {
        return rip;
    }

    @Override
    public long getRSI() {
        return reg == ID_RSI ? value : previous.getRSI();
    }

    @Override
    public long getRDI() {
        return reg == ID_RDI ? value : previous.getRDI();
    }

    @Override
    public long getR8() {
        return reg == ID_R8 ? value : previous.getR8();
    }

    @Override
    public long getR9() {
        return reg == ID_R9 ? value : previous.getR9();
    }

    @Override
    public long getR10() {
        return reg == ID_R10 ? value : previous.getR10();
    }

    @Override
    public long getR11() {
        return reg == ID_R11 ? value : previous.getR11();
    }

    @Override
    public long getR12() {
        return reg == ID_R12 ? value : previous.getR12();
    }

    @Override
    public long getR13() {
        return reg == ID_R13 ? value : previous.getR13();
    }

    @Override
    public long getR14() {
        return reg == ID_R14 ? value : previous.getR14();
    }

    @Override
    public long getR15() {
        return reg == ID_R15 ? value : previous.getR15();
    }

    @Override
    public long getRFL() {
        return efl;
    }

    @Override
    public long getFS() {
        return reg == ID_FS ? value : previous.getFS();
    }

    @Override
    public long getGS() {
        return reg == ID_GS ? value : previous.getGS();
    }

    @Override
    public Vector128 getXMM(int i) {
        return previous.getXMM(i);
    }
}
