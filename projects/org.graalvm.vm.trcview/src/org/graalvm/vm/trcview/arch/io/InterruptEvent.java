package org.graalvm.vm.trcview.arch.io;

public abstract class InterruptEvent extends Event {
    protected InterruptEvent(int tid) {
        super(tid);
    }

    public abstract StepEvent getStep();
}
