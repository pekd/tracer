package org.graalvm.vm.trcview.arch.pdp11.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.DeviceEvent;
import org.graalvm.vm.trcview.arch.pdp11.device.PDP11Devices;
import org.graalvm.vm.trcview.arch.pdp11.device.RXV21;
import org.graalvm.vm.util.io.WordInputStream;

public class PDP11RXV21Disk extends DeviceEvent {
    private final short type;
    private final byte drive;
    private final byte density;
    private final short rx2sa;
    private final short rx2ta;

    public PDP11RXV21Disk(WordInputStream in, int tid) throws IOException {
        super(tid);
        type = in.read16bit();
        drive = (byte) in.read8bit();
        density = (byte) in.read8bit();
        rx2sa = in.read16bit();
        rx2ta = in.read16bit();
    }

    @Override
    public int getDeviceId() {
        return PDP11Devices.RXV21;
    }

    private String getName() {
        switch (type) {
            case RXV21.READ:
                return "read";
            case RXV21.WRITE:
                return "write";
            case RXV21.WRITE_DD:
                return "write (delete data)";
            default:
                return "???";
        }
    }

    @Override
    public String getMessage() {
        String name = getName();
        return name + " sector SEC=" + rx2sa + ", TR=" + rx2ta + " [drive=" + drive + ", " + (density == 0 ? "single" : "double") + " density]";
    }
}
