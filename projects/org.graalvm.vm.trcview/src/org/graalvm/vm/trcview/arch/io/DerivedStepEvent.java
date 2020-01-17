package org.graalvm.vm.trcview.arch.io;

public abstract class DerivedStepEvent extends StepEvent {
    protected DerivedStepEvent(short arch, int tid) {
        super(arch, tid);
    }

    public abstract long getParentStep();
}
