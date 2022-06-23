package org.graalvm.vm.trcview.arch.riscv.io;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;

public class RiscVFullCpuState extends RiscVCpuState {
    private final int insn;
    private final long[] gpr;
    private final long pc;
    private final long step;

    public RiscVFullCpuState(WordInputStream in, int tid) throws IOException {
        super(tid);
        insn = (int) in.read64bit();
        step = in.read64bit();
        pc = in.read64bit();
        gpr = new long[32];
        for (int i = 0; i < 32; i++) {
            gpr[i] = in.read64bit();
        }
    }

    public RiscVFullCpuState(RiscVCpuState state) {
        super(state.getTid());
        insn = state.getInstruction();
        gpr = new long[32];
        for (int i = 0; i < gpr.length; i++) {
            gpr[i] = state.getGPR(i);
        }
        pc = (int) state.getPC();
        step = state.getStep();
    }

    @Override
    public long getGPR(int reg) {
        return gpr[reg];
    }

    @Override
    public long getCSR(int reg) {
        return 0;
    }

    @Override
    public long getStep() {
        return step;
    }

    @Override
    public long getPC() {
        return pc;
    }

    @Override
    public int getInstruction() {
        return insn;
    }
}
