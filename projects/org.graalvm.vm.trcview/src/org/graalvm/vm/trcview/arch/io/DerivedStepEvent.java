package org.graalvm.vm.trcview.arch.io;

public abstract class DerivedStepEvent extends StepEvent {
    protected DerivedStepEvent(int tid) {
        super(tid);
    }

    public abstract long getParentStep();
}
