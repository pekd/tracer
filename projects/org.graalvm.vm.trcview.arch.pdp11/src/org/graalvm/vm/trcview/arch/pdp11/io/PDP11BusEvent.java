package org.graalvm.vm.trcview.arch.pdp11.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.DeviceEvent;
import org.graalvm.vm.trcview.arch.io.MemoryEvent;
import org.graalvm.vm.trcview.arch.io.MemoryEventI16;
import org.graalvm.vm.trcview.arch.pdp11.device.PDP11Devices;
import org.graalvm.vm.util.io.WordInputStream;

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
        super(tid);
        addr = in.read16bit();
        value = in.read16bit();
        type = in.read16bit();
        in.read16bit();
    }

    public MemoryEvent getMemoryEvent() {
        switch (type) {
            case BUS_RD:
                return new MemoryEventI16(false, getTid(), Short.toUnsignedLong(addr), false, value);
            case BUS_WR:
                return new MemoryEventI16(false, getTid(), Short.toUnsignedLong(addr), true, value);
            case BUS_RDFAIL:
                return new MemoryEventI16(false, getTid(), Short.toUnsignedLong(addr), false);
            case BUS_WRFAIL:
                return new MemoryEventI16(false, getTid(), Short.toUnsignedLong(addr), true);
            default:
                return null;
        }
    }

    public short getType() {
        return type;
    }

    @Override
    public int getDeviceId() {
        return PDP11Devices.QBUS;
    }

    @Override
    public String getMessage() {
        int uaddr = Short.toUnsignedInt(addr);
        int uval = Short.toUnsignedInt(value);

        switch (type) {
            case BUS_RD:
                return String.format("read %06o = %06o", uaddr, uval);
            case BUS_RDFAIL:
                return String.format("read %06o timed out", uaddr);
            case BUS_WR:
                return String.format("write %06o = %06o", uaddr, uval);
            case BUS_WRFAIL:
                return String.format("write %06o = %06o timed out", uaddr, uval);
            case BUS_RESET:
                return "reset";
            default:
                return "???";
        }
    }
}
