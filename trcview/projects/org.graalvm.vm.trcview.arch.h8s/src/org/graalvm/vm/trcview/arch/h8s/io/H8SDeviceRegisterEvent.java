package org.graalvm.vm.trcview.arch.h8s.io;

import java.util.Collections;
import java.util.List;

import org.graalvm.vm.trcview.analysis.device.RegisterValue;
import org.graalvm.vm.trcview.arch.io.DeviceRegisterEvent;

public class H8SDeviceRegisterEvent extends DeviceRegisterEvent {
    private final int dev;
    private final int reg;
    private final short value;
    private final boolean read;
    private final boolean write;
    private boolean isByte;

    public H8SDeviceRegisterEvent(int dev, int reg, byte value) {
        super(0);
        this.dev = dev;
        this.reg = reg;
        this.value = value;
        this.write = false;
        this.read = false;
        this.isByte = true;
    }

    public H8SDeviceRegisterEvent(int dev, int reg, short value) {
        super(0);
        this.dev = dev;
        this.reg = reg;
        this.value = value;
        this.write = false;
        this.read = false;
        this.isByte = false;
    }

    public H8SDeviceRegisterEvent(int dev, int reg, byte value, boolean write) {
        super(0);
        this.dev = dev;
        this.reg = reg;
        this.value = value;
        this.write = write;
        this.read = !write;
        this.isByte = true;
    }

    public H8SDeviceRegisterEvent(int dev, int reg, short value, boolean write) {
        super(0);
        this.dev = dev;
        this.reg = reg;
        this.value = value;
        this.write = write;
        this.read = !write;
        this.isByte = false;
    }

    @Override
    public int getDeviceId() {
        return dev;
    }

    @Override
    public List<RegisterValue> getValues() {
        if (isByte) {
            return Collections.singletonList(new RegisterValue(reg, Byte.toUnsignedLong((byte) value)));
        } else {
            return List.of(new RegisterValue(reg, Byte.toUnsignedLong((byte) (value >> 8))), new RegisterValue(reg + 1, Byte.toUnsignedLong((byte) value)));
        }
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
