package org.graalvm.vm.trcview.arch.z80.io;

import org.graalvm.vm.trcview.arch.io.InterruptEvent;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.util.HexFormatter;

public class Z80InterruptEvent extends InterruptEvent {
    private final Z80StepEvent step;
    private final byte irq;

    protected Z80InterruptEvent(int tid, byte irq, Z80StepEvent step) {
        super(tid);
        this.step = step;
        this.irq = irq;
    }

    @Override
    public StepEvent getStep() {
        return step;
    }

    @Override
    public String toString() {
        return "<IRQ " + HexFormatter.tohex(Byte.toUnsignedLong(irq), 2).toUpperCase() + ">";
    }
}
