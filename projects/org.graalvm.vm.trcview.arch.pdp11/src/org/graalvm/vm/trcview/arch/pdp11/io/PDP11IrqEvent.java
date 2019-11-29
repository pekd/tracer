package org.graalvm.vm.trcview.arch.pdp11.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.DeviceEvent;
import org.graalvm.vm.trcview.arch.pdp11.PDP11;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class PDP11IrqEvent extends DeviceEvent {
    private final short trap;
    private final short type;

    public PDP11IrqEvent(WordInputStream in, int tid) throws IOException {
        super(PDP11.ID, tid);
        trap = in.read16bit();
        type = in.read16bit();
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        out.write16bit(trap);
        out.write16bit(type);
    }
}
