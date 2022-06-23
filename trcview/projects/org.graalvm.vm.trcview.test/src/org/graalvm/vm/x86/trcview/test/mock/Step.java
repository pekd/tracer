package org.graalvm.vm.x86.trcview.test.mock;

public class Step {
    public final long step;
    public final long parent;
    public final long pc;
    public final int type;
    public final byte[] machinecode;
    public final byte[] cpustate;

    public Step(long step, long parent, long pc, int type, byte[] machinecode, byte[] cpustate) {
        this.step = step;
        this.parent = parent;
        this.pc = pc;
        this.type = type;
        this.machinecode = machinecode;
        this.cpustate = cpustate;
    }
}
