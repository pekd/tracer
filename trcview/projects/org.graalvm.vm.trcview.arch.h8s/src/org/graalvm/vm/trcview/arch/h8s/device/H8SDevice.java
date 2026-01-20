package org.graalvm.vm.trcview.arch.h8s.device;

import java.util.Set;

import org.graalvm.vm.trcview.analysis.device.Device;
import org.graalvm.vm.trcview.analysis.device.DeviceRegister;
import org.graalvm.vm.trcview.analysis.device.DeviceType;
import org.graalvm.vm.trcview.analysis.device.FieldFormat;

public abstract class H8SDevice extends Device {
    public H8SDevice(int id, String name, DeviceType type) {
        super(id, name, type);
    }

    public short[] getAddresses() {
        Set<Integer> addresses = getRegisters().keySet();
        short[] result = new short[addresses.size()];
        int i = 0;
        for (int addr : addresses) {
            result[i++] = (short) addr;
        }
        return result;
    }

    public boolean is16Bit(@SuppressWarnings("unused") short address) {
        return false;
    }

    public boolean is32Bit(@SuppressWarnings("unused") short address) {
        return false;
    }

    protected static DeviceRegister reg(int id, String name, long addr, FieldFormat... fmt) {
        DeviceRegister reg = new DeviceRegister(id, name, addr, fmt);
        return reg;
    }
}
