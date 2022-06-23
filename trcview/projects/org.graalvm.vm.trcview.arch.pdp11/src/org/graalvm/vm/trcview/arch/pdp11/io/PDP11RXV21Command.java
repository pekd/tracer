package org.graalvm.vm.trcview.arch.pdp11.io;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.graalvm.vm.trcview.analysis.device.RegisterValue;
import org.graalvm.vm.trcview.arch.io.DeviceEvent;
import org.graalvm.vm.trcview.arch.pdp11.device.PDP11Devices;
import org.graalvm.vm.trcview.arch.pdp11.device.RXV21;
import org.graalvm.vm.util.io.WordInputStream;

public class PDP11RXV21Command extends DeviceEvent {
    private final byte type;
    private final byte commit;
    private final short rx2cs;

    public PDP11RXV21Command(WordInputStream in, int tid) throws IOException {
        super(tid);
        type = (byte) in.read8bit();
        commit = (byte) in.read8bit();
        rx2cs = in.read16bit();
    }

    @Override
    public int getDeviceId() {
        return PDP11Devices.RXV21;
    }

    @Override
    public String getMessage() {
        return (commit == 0 ? "initiate" : "finish") + " command " + RXV21.getName(type);
    }

    @Override
    public List<RegisterValue> getValues() {
        return Collections.singletonList(new RegisterValue(PDP11Devices.RXV21_RX2CS, Short.toUnsignedLong(rx2cs)));
    }

    @Override
    public List<RegisterValue> getWrites() {
        if (commit == 0) {
            return getValues();
        } else {
            return Collections.emptyList();
        }
    }
}
