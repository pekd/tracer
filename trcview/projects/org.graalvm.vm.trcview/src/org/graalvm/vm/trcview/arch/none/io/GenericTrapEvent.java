package org.graalvm.vm.trcview.arch.none.io;

import org.graalvm.vm.trcview.arch.io.InterruptEvent;
import org.graalvm.vm.trcview.arch.io.StepEvent;

public class GenericTrapEvent extends InterruptEvent {
    private final String msg;
    private final GenericStepEvent step;

    public GenericTrapEvent(int tid, String msg, GenericStepEvent step) {
        super(tid);
        this.msg = msg;
        this.step = step;
    }

    @Override
    public StepEvent getStep() {
        return step;
    }

    @Override
    public String toString() {
        return msg;
    }
}
