package org.graalvm.vm.trcview.arch.io;

public abstract class CpuState extends Event {
    protected CpuState(short arch, int tid) {
        super(arch, CPU_STATE, tid);
    }

    public abstract long getStep();

    public abstract long getPC();

    public abstract long get(String name);
}
