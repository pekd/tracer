package org.graalvm.vm.trcview.arch.pdp11.io;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.graalvm.vm.trcview.analysis.device.RegisterValue;
import org.graalvm.vm.trcview.arch.io.DeviceEvent;
import org.graalvm.vm.trcview.arch.io.IoEvent;
import org.graalvm.vm.trcview.arch.pdp11.PDP11;
import org.graalvm.vm.trcview.arch.pdp11.device.PDP11Devices;
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

    @Override
    public int getDeviceId() {
        return PDP11Devices.DLV11J;
    }

    private static char printable(short value) {
        int val = value & 0xFF;
        if (val >= 0x20 && val < 0x7F) {
            return (char) val;
        } else {
            return '.';
        }
    }

    @Override
    public String getMessage() {
        switch (type) {
            case DLV11_RX:
                return "CH" + channel + ": RX '" + printable(value) + "' [" + Integer.toString(Short.toUnsignedInt(value), 8) + "]";
            case DLV11_TX:
                return "CH" + channel + ": TX '" + printable(value) + "' [" + Integer.toString(Short.toUnsignedInt(value), 8) + "]";
            case DLV11_RDY:
                return "READY";
            case DLV11_SEI:
                return "Interrupts enabled";
            case DLV11_CLI:
                return "Interrupts disable";
            default:
                return "???";
        }
    }

    @Override
    public List<RegisterValue> getValues() {
        if (channel == 3 && type == DLV11_RX) {
            return Collections.singletonList(new RegisterValue(PDP11Devices.DLV11J_RBUF, value));
        } else {
            return Collections.emptyList();
        }
    }
}
