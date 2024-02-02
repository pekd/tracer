package org.graalvm.vm.trcview.arch.z80.io;

import org.graalvm.vm.trcview.arch.io.DeviceEvent;
import org.graalvm.vm.trcview.arch.z80.device.Z80Devices;
import org.graalvm.vm.util.HexFormatter;

public class Z80InterruptDeviceEvent extends DeviceEvent {
    private final byte irq;

    protected Z80InterruptDeviceEvent(int tid, byte irq) {
        super(tid);
        this.irq = irq;
    }

    @Override
    public int getDeviceId() {
        return Z80Devices.CPU;
    }

    @Override
    public String getMessage() {
        return "IRQ " + HexFormatter.tohex(Byte.toUnsignedLong(irq), 2).toUpperCase();
    }
}
