package org.graalvm.vm.trcview.arch.ppc.io;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;

public class PowerPCFullCpuState extends PowerPCCpuState {
    private final int insn;
    private final int[] gpr;
    private final int lr;
    private final int ctr;
    private final int pc;
    private final int cr;
    private final int xer;
    private final int fpscr;
    private final int srr0;
    private final int srr1;
    private final long step;

    public PowerPCFullCpuState(WordInputStream in, int tid) throws IOException {
        super(tid);
        insn = in.read32bit();
        gpr = new int[32];
        for (int i = 0; i < 32; i++) {
            gpr[i] = in.read32bit();
        }
        for (int i = 0; i < 32; i++) {
            in.read64bit();
        }
        lr = in.read32bit();
        ctr = in.read32bit();
        pc = in.read32bit();
        cr = in.read32bit();
        xer = in.read32bit();
        fpscr = in.read32bit();
        srr0 = in.read32bit();
        srr1 = in.read32bit();
        step = in.read64bit();
    }

    public PowerPCFullCpuState(PowerPCCpuState state) {
        super(state.getTid());
        insn = state.getInstruction();
        gpr = new int[32];
        for (int i = 0; i < gpr.length; i++) {
            gpr[i] = state.getGPR(i);
        }
        lr = state.getLR();
        ctr = state.getCTR();
        pc = (int) state.getPC();
        cr = state.getCR();
        xer = state.getXER();
        fpscr = state.getFPSCR();
        srr0 = state.getSRR0();
        srr1 = state.getSRR1();
        step = state.getStep();
    }

    public PowerPCFullCpuState(PowerPCDeltaCpuState state, PowerPCCpuState last) {
        super(state.getTid());
        insn = state.getInstruction();
        gpr = new int[32];
        for (int i = 0; i < gpr.length; i++) {
            gpr[i] = state.getGPR(i, last);
        }
        lr = state.getLR(last);
        ctr = state.getCTR(last);
        pc = (int) state.getPC();
        cr = state.getCR(last);
        xer = state.getXER(last);
        fpscr = state.getFPSCR(last);
        srr0 = state.getSRR0(last);
        srr1 = state.getSRR1(last);
        step = state.getStep();
    }

    @Override
    public long getStep() {
        return step;
    }

    @Override
    public long getPC() {
        return Integer.toUnsignedLong(pc);
    }

    @Override
    public int getInstruction() {
        return insn;
    }

    @Override
    public int getLR() {
        return lr;
    }

    @Override
    public int getCR() {
        return cr;
    }

    @Override
    public int getXER() {
        return xer;
    }

    @Override
    public int getFPSCR() {
        return fpscr;
    }

    @Override
    public int getCTR() {
        return ctr;
    }

    @Override
    public int getGPR(int reg) {
        return gpr[reg];
    }

    @Override
    public int getSRR0() {
        return srr0;
    }

    @Override
    public int getSRR1() {
        return srr1;
    }
}
