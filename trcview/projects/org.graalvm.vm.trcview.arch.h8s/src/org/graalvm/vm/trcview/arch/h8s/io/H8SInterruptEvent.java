package org.graalvm.vm.trcview.arch.h8s.io;

import org.graalvm.vm.trcview.arch.io.InterruptEvent;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.util.HexFormatter;

public class H8SInterruptEvent extends InterruptEvent {
    private final H8SStepEvent step;
    private final short irq;

    protected H8SInterruptEvent(short irq, H8SStepEvent step) {
        super(0);
        this.step = step;
        this.irq = irq;
    }

    @Override
    public StepEvent getStep() {
        return step;
    }

    @Override
    public String toString() {
        return "<IRQ " + HexFormatter.tohex(Short.toUnsignedLong(irq), 6).toUpperCase() + ">";
    }
}
