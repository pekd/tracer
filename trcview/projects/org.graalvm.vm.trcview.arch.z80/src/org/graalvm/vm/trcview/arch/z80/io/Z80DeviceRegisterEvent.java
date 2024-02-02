package org.graalvm.vm.trcview.arch.z80.io;

import java.util.Collections;
import java.util.List;

import org.graalvm.vm.trcview.analysis.device.RegisterValue;
import org.graalvm.vm.trcview.arch.io.DeviceRegisterEvent;

public class Z80DeviceRegisterEvent extends DeviceRegisterEvent {
    private final int dev;
    private final int reg;
    private final byte value;
    private final boolean read;
    private final boolean write;

    public Z80DeviceRegisterEvent(int tid, int dev, int reg, byte value) {
        super(tid);
        this.dev = dev;
        this.reg = reg;
        this.value = value;
        this.write = false;
        this.read = false;
    }

    public Z80DeviceRegisterEvent(int tid, int dev, int reg, byte value, boolean write) {
        super(tid);
        this.dev = dev;
        this.reg = reg;
        this.value = value;
        this.write = write;
        this.read = !write;
    }

    @Override
    public int getDeviceId() {
        return dev;
    }

    @Override
    public List<RegisterValue> getValues() {
        return Collections.singletonList(new RegisterValue(reg, Byte.toUnsignedLong(value)));
    }

    @Override
    public List<RegisterValue> getWrites() {
        if (write) {
            return getValues();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<RegisterValue> getReads() {
        if (read) {
            return getValues();
        } else {
            return Collections.emptyList();
        }
    }
}
