package org.graalvm.vm.trcview.arch.ppc.io;

public class PowerPCZeroCpuState extends PowerPCCpuState {
    public PowerPCZeroCpuState(int tid) {
        super(tid);
    }

    @Override
    public int getInstruction() {
        return 0;
    }

    @Override
    public int getLR() {
        return 0;
    }

    @Override
    public int getCR() {
        return 0;
    }

    @Override
    public int getCTR() {
        return 0;
    }

    @Override
    public int getXER() {
        return 0;
    }

    @Override
    public int getFPSCR() {
        return 0;
    }

    @Override
    public int getGPR(int reg) {
        return 0;
    }

    @Override
    public int getSRR0() {
        return 0;
    }

    @Override
    public int getSRR1() {
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
