package org.graalvm.vm.x86.trcview.io.data.pdp11;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;
import org.graalvm.vm.x86.trcview.arch.PDP11;
import org.graalvm.vm.x86.trcview.io.data.DeviceEvent;

public class PDP11RXV21Disk extends DeviceEvent {
    private final short type;
    private final byte drive;
    private final byte density;
    private final short rx2sa;
    private final short rx2ta;

    public PDP11RXV21Disk(WordInputStream in, int tid) throws IOException {
        super(PDP11.ID, tid);
        type = in.read16bit();
        drive = (byte) in.read8bit();
        density = (byte) in.read8bit();
        rx2sa = in.read16bit();
        rx2ta = in.read16bit();
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        out.write16bit(type);
        out.write8bit(drive);
        out.write8bit(density);
        out.write16bit(rx2sa);
        out.write16bit(rx2ta);
    }
}
