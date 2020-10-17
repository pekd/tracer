package org.graalvm.vm.trcview.arch.pdp11.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.DeviceEvent;
import org.graalvm.vm.trcview.arch.pdp11.device.PDP11Devices;
import org.graalvm.vm.trcview.arch.pdp11.device.RXV21;
import org.graalvm.vm.util.io.WordInputStream;

public class PDP11RXV21Dma extends DeviceEvent {
    private final short type;
    private final short rx2wc;
    private final short rx2ba;

    public PDP11RXV21Dma(WordInputStream in, int tid) throws IOException {
        super(tid);
        type = in.read16bit();
        rx2wc = in.read16bit();
        rx2ba = in.read16bit();
        in.read16bit();
    }

    @Override
    public int getDeviceId() {
        return PDP11Devices.RXV21;
    }

    @Override
    public String getMessage() {
        return String.format("DMA transfer [%s]: %06o words to %06o", RXV21.getName(type), Short.toUnsignedInt(rx2wc), Short.toUnsignedInt(rx2ba));
    }
}
