package org.graalvm.vm.trcview.arch.h8s.io;

import java.util.Collections;
import java.util.List;

import org.graalvm.vm.trcview.analysis.device.RegisterValue;
import org.graalvm.vm.trcview.arch.io.DeviceRegisterEvent;

public class H8SDeviceRegisterEventI16 extends DeviceRegisterEvent {
    private final int dev;
    private final int reg;
    private final short value;
    private final boolean read;
    private final boolean write;

    public H8SDeviceRegisterEventI16(int dev, int reg, short value) {
        super(0);
        this.dev = dev;
        this.reg = reg;
        this.value = value;
        this.write = false;
        this.read = false;
    }

    public H8SDeviceRegisterEventI16(int dev, int reg, short value, boolean write) {
        super(0);
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
        return Collections.singletonList(new RegisterValue(reg, Short.toUnsignedLong(value)));
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
