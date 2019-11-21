package org.graalvm.vm.x86.trcview.io.data.pdp11;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;
import org.graalvm.vm.x86.trcview.arch.PDP11;
import org.graalvm.vm.x86.trcview.io.data.DeviceEvent;
import org.graalvm.vm.x86.trcview.io.data.MemoryEvent;

public class PDP11BusEvent extends DeviceEvent {
    public static final int BUS_RD = 0;
    public static final int BUS_WR = 1;
    public static final int BUS_RDFAIL = 2;
    public static final int BUS_WRFAIL = 3;
    public static final int BUS_RESET = 4;

    private final short addr;
    private final short value;
    private final short type;

    protected PDP11BusEvent(WordInputStream in, int tid) throws IOException {
        super(PDP11.ID, tid);
        addr = in.read16bit();
        value = in.read16bit();
        type = in.read16bit();
        in.read16bit();
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        out.write16bit(addr);
        out.write16bit(value);
        out.write16bit(type);
    }

    public MemoryEvent getMemoryEvent() {
        switch (type) {
            case BUS_RD:
                return new MemoryEvent(false, getTid(), Short.toUnsignedLong(addr), (byte) 2, false, Short.toUnsignedLong(value));
            case BUS_WR:
                return new MemoryEvent(false, getTid(), Short.toUnsignedLong(addr), (byte) 2, true, Short.toUnsignedLong(value));
            case BUS_RDFAIL:
                return new MemoryEvent(false, getTid(), Short.toUnsignedLong(addr), (byte) 2, false);
            case BUS_WRFAIL:
                return new MemoryEvent(false, getTid(), Short.toUnsignedLong(addr), (byte) 2, true);
            default:
                return null;
        }
    }

    public short getType() {
        return type;
    }
}
