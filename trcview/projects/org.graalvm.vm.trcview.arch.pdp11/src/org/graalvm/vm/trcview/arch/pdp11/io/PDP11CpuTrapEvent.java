package org.graalvm.vm.trcview.arch.pdp11.io;

import org.graalvm.vm.trcview.arch.io.InterruptEvent;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.util.OctFormatter;

public class PDP11CpuTrapEvent extends InterruptEvent {
    private final short trap;
    private final PDP11StepEvent step;

    protected PDP11CpuTrapEvent(int tid, short trap, PDP11StepEvent step) {
        super(tid);
        this.trap = trap;
        this.step = step;
    }

    @Override
    public StepEvent getStep() {
        return step;
    }

    @Override
    public String toString() {
        return "<trap " + OctFormatter.tooct(Short.toUnsignedLong(trap)) + ">";
    }
}
