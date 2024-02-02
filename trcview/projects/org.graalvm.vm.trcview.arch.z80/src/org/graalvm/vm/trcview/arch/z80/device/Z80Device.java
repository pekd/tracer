package org.graalvm.vm.trcview.arch.z80.device;

import org.graalvm.vm.trcview.analysis.device.Device;
import org.graalvm.vm.trcview.analysis.device.DeviceRegister;
import org.graalvm.vm.trcview.analysis.device.DeviceType;
import org.graalvm.vm.trcview.analysis.device.FieldFormat;
import org.graalvm.vm.trcview.arch.z80.io.Z80DeviceRegisterEvent;

public abstract class Z80Device extends Device {
    public Z80Device(int id, String name, DeviceType type) {
        super(id, name, type);
    }

    public abstract Z80DeviceRegisterEvent getInputEvent(byte addr, byte value);

    public abstract Z80DeviceRegisterEvent getOutputEvent(byte addr, byte value);

    protected static DeviceRegister reg(int id, String name, long addr, FieldFormat... fmt) {
        DeviceRegister reg = new DeviceRegister(id, name, addr, fmt);
        return reg;
    }
}
