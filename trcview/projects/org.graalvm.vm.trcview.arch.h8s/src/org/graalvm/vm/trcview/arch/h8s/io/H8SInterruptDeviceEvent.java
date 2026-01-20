package org.graalvm.vm.trcview.arch.h8s.io;

import org.graalvm.vm.trcview.arch.h8s.device.H8SDevices;
import org.graalvm.vm.trcview.arch.io.DeviceEvent;
import org.graalvm.vm.util.HexFormatter;

public class H8SInterruptDeviceEvent extends DeviceEvent {
    private final short irq;

    protected H8SInterruptDeviceEvent(short irq) {
        super(0);
        this.irq = irq;
    }

    @Override
    public int getDeviceId() {
        return H8SDevices.CPU;
    }

    @Override
    public String getMessage() {
        return "IRQ " + HexFormatter.tohex(Short.toUnsignedLong(irq), 4).toUpperCase();
    }
}
