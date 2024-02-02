package org.graalvm.vm.trcview.arch.z80.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.DeviceEvent;
import org.graalvm.vm.trcview.arch.z80.device.Z80Devices;
import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.util.io.WordInputStream;

public class Z80InputEvent extends DeviceEvent {
    private final short addr;
    private final byte value;

    public Z80InputEvent(int tid, WordInputStream in) throws IOException {
        super(tid);
        value = (byte) in.read8bit();
        addr = in.read16bit();
    }

    public short getAddress() {
        return addr;
    }

    public byte getValue() {
        return value;
    }

    @Override
    public int getDeviceId() {
        return Z80Devices.CPU;
    }

    @Override
    public String getMessage() {
        return "IN  " + HexFormatter.tohex(Short.toUnsignedInt(addr), 4) + " = " + HexFormatter.tohex(Byte.toUnsignedInt(value), 2);
    }
}
