package org.graalvm.vm.trcview.arch.pdp11.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.DeviceEvent;
import org.graalvm.vm.trcview.arch.pdp11.PDP11;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class PDP11DLV11Event extends DeviceEvent {
    private final byte channel;
    private final byte type;
    private final short value;

    public PDP11DLV11Event(WordInputStream in, int tid) throws IOException {
        super(PDP11.ID, tid);
        channel = (byte) in.read();
        type = (byte) in.read();
        value = in.read16bit();
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        out.write8bit(channel);
        out.write8bit(type);
        out.write16bit(value);
    }
}
