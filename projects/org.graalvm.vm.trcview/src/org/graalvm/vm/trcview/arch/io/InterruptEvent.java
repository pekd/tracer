package org.graalvm.vm.trcview.arch.io;

public abstract class InterruptEvent extends Event {
    protected InterruptEvent(short arch, int tid) {
        super(arch, INTERRUPT, tid);
    }

    public abstract StepEvent getStep();
}
