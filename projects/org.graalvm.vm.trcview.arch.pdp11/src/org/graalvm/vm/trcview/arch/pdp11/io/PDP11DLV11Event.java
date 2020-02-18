package org.graalvm.vm.trcview.arch.pdp11.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.DeviceEvent;
import org.graalvm.vm.trcview.arch.io.IoEvent;
import org.graalvm.vm.trcview.arch.pdp11.PDP11;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class PDP11DLV11Event extends DeviceEvent {
    public static final int DLV11_RX = 0;
    public static final int DLV11_TX = 1;
    public static final int DLV11_RDY = 2;
    public static final int DLV11_SEI = 3;
    public static final int DLV11_CLI = 4;

    private final byte channel;
    private final byte type;
    private final short value;
    private final long step;

    public PDP11DLV11Event(WordInputStream in, int tid, long step) throws IOException {
        super(PDP11.ID, tid);
        this.step = step;
        channel = (byte) in.read();
        type = (byte) in.read();
        value = in.read16bit();
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        out.write64bit(step);
        out.write8bit(channel);
        out.write8bit(type);
        out.write16bit(value);
    }

    public IoEvent getIoEvent() {
        if (type == DLV11_RX) {
            return new IoEvent(getArchitectureId(), getTid(), step, channel, true, Character.toString((char) (value & 0xFF)));
        } else if (type == DLV11_TX) {
            return new IoEvent(getArchitectureId(), getTid(), step, channel, false, Character.toString((char) (value & 0xFF)));
        } else {
            return null;
        }
    }
}
