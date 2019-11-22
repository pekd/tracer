package org.graalvm.vm.x86.trcview.io.data;

public abstract class InterruptEvent extends Event {
    protected InterruptEvent(short arch, int tid) {
        super(arch, INTERRUPT, tid);
    }

    public abstract StepEvent getStep();
}
