package org.graalvm.vm.trcview.arch.none.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.InterruptEvent;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.none.None;
import org.graalvm.vm.util.io.WordOutputStream;

public class GenericTrapEvent extends InterruptEvent {
    private final String msg;
    private final GenericStepEvent step;

    public GenericTrapEvent(int tid, String msg, GenericStepEvent step) {
        super(None.ID, tid);
        this.msg = msg;
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

    @Override
    public String toString() {
        return msg;
    }
}
