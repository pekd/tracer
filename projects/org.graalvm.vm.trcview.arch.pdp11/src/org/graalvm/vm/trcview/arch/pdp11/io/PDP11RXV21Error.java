package org.graalvm.vm.trcview.arch.pdp11.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.DeviceEvent;
import org.graalvm.vm.trcview.arch.pdp11.PDP11;
import org.graalvm.vm.trcview.arch.pdp11.device.PDP11Devices;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class PDP11RXV21Error extends DeviceEvent {
    public static final int WC_OVFL = 0;
    public static final int DEN_ERR = 1;
    public static final int TRACK_NO = 2;
    public static final int SECT_NO = 3;

    private final short type;
    private final short info;

    public PDP11RXV21Error(WordInputStream in, int tid) throws IOException {
        super(PDP11.ID, tid);
        type = in.read16bit();
        info = in.read16bit();
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        out.write16bit(type);
        out.write16bit(info);
    }

    @Override
    public int getDeviceId() {
        return PDP11Devices.RXV21;
    }

    public String getErrorName() {
        switch (type) {
            case WC_OVFL:
                return "Word Count overflow";
            case DEN_ERR:
                return "Density mismatch";
            case TRACK_NO:
                return "Tried to access a track greater than 76";
            case SECT_NO:
                return "Desired sector could not be found after looking at 52 headers (2 revolutions)";
            default:
                return "???";
        }
    }

    @Override
    public String getMessage() {
        switch (type) {
            case WC_OVFL:
            case TRACK_NO:
            case SECT_NO:
                return String.format("Error: %s [%o/%d]", getErrorName(), Short.toUnsignedInt(info), Short.toUnsignedInt(info));
            default:
                return "Error: " + getErrorName();
        }
    }
}
