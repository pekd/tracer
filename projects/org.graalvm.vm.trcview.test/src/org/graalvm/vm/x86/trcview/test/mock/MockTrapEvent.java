package org.graalvm.vm.x86.trcview.test.mock;

import org.graalvm.vm.trcview.arch.io.InterruptEvent;
import org.graalvm.vm.trcview.arch.io.StepEvent;

public class MockTrapEvent extends InterruptEvent {
    private final StepEvent step;

    public MockTrapEvent(int tid, StepEvent step) {
        super(tid);
        this.step = step;
    }

    @Override
    public StepEvent getStep() {
        return step;
    }
}
