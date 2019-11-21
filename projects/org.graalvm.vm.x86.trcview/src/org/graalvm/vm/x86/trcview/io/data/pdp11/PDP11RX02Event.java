package org.graalvm.vm.x86.trcview.io.data.pdp11;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;
import org.graalvm.vm.x86.trcview.arch.PDP11;
import org.graalvm.vm.x86.trcview.io.data.DeviceEvent;

public class PDP11RX02Event extends DeviceEvent {
    private final short rx2cs;
    private final short rx2ta;
    private final short rx2sa;
    private final short rx2wc;
    private final short rx2ba;
    private final short rx2es;
    private final short command;
    private final short status;

    public PDP11RX02Event(WordInputStream in, int tid) throws IOException {
        super(PDP11.ID, tid);
        rx2cs = in.read16bit();
        rx2ta = in.read16bit();
        rx2sa = in.read16bit();
        rx2wc = in.read16bit();
        rx2ba = in.read16bit();
        rx2es = in.read16bit();
        command = in.read16bit();
        status = in.read16bit();
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        out.write16bit(rx2cs);
        out.write16bit(rx2ta);
        out.write16bit(rx2sa);
        out.write16bit(rx2wc);
        out.write16bit(rx2ba);
        out.write16bit(rx2es);
        out.write16bit(command);
        out.write16bit(status);
    }
}
