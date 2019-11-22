package org.graalvm.vm.x86.trcview.io.data.pdp11;

import java.io.IOException;

import org.graalvm.vm.util.OctFormatter;
import org.graalvm.vm.util.io.WordOutputStream;
import org.graalvm.vm.x86.trcview.arch.PDP11;
import org.graalvm.vm.x86.trcview.io.data.InterruptEvent;
import org.graalvm.vm.x86.trcview.io.data.StepEvent;

public class PDP11CpuTrapEvent extends InterruptEvent {
    private final short trap;
    private final PDP11StepEvent step;

    protected PDP11CpuTrapEvent(int tid, short trap, PDP11StepEvent step) {
        super(PDP11.ID, tid);
        this.trap = trap;
        this.step = step;
    }

    @Override
    public StepEvent getStep() {
        return step;
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        out.write16bit(trap);
        step.write(out);
    }

    @Override
    public String toString() {
        return "<trap " + OctFormatter.tooct(Short.toUnsignedLong(trap)) + ">";
    }
}
