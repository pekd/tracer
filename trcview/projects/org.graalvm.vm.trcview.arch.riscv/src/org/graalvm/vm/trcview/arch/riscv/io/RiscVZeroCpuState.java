package org.graalvm.vm.trcview.arch.riscv.io;

public class RiscVZeroCpuState extends RiscVCpuState {
    public RiscVZeroCpuState(int tid) {
        super(tid);
    }

    @Override
    public int getInstruction() {
        return 0;
    }

    @Override
    public long getGPR(int reg) {
        return 0;
    }

    @Override
    public long getCSR(int reg) {
        return 0;
    }

    @Override
    public long getStep() {
        return 0;
    }

    @Override
    public long getPC() {
        return 0;
    }
}
