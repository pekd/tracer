package org.graalvm.vm.x86.trcview.test.mock;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.InterruptEvent;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.util.io.WordOutputStream;

public class MockTrapEvent extends InterruptEvent {
    private final StepEvent step;

    public MockTrapEvent(short arch, int tid, StepEvent step) {
        super(arch, tid);
        this.step = step;
    }

    @Override
    public StepEvent getStep() {
        return step;
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        // TODO Auto-generated method stub
    }
}
