package org.graalvm.vm.trcview.arch.pdp11.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.DeviceEvent;
import org.graalvm.vm.trcview.arch.pdp11.PDP11;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class PDP11RXV21Command extends DeviceEvent {
    private final byte type;
    private final byte commit;
    private final short rx2cs;

    public PDP11RXV21Command(WordInputStream in, int tid) throws IOException {
        super(PDP11.ID, tid);
        type = (byte) in.read8bit();
        commit = (byte) in.read8bit();
        rx2cs = in.read16bit();
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        out.write8bit(type);
        out.write8bit(commit);
        out.write16bit(rx2cs);
    }
}
