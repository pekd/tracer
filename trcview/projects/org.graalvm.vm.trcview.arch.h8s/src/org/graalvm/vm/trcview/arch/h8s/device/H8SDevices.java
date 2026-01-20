package org.graalvm.vm.trcview.arch.h8s.device;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.graalvm.vm.trcview.analysis.device.Device;
import org.graalvm.vm.trcview.analysis.device.DeviceRegister;
import org.graalvm.vm.trcview.analysis.device.DeviceType;
import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.analysis.type.Representation;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.arch.h8s.io.H8SDeviceRegisterEvent;
import org.graalvm.vm.trcview.arch.h8s.io.H8SDeviceRegisterEventI16;
import org.graalvm.vm.trcview.arch.h8s.io.H8SDeviceRegisterEventI32;
import org.graalvm.vm.trcview.arch.io.DeviceDefinitionEvent;
import org.graalvm.vm.trcview.arch.io.DeviceRegisterEvent;
import org.graalvm.vm.trcview.data.TypedMemory;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public class H8SDevices {
    public static final int CPU = 0;
    public static final int POWER_CONTROLLER = 1;
    public static final int INTERRUPT_CONTROLLER = 2;
    public static final int BUS_CONTROLLER = 3;
    public static final int DMA_CONTROLLER = 4;
    public static final int SCI_INTERFACE = 5;
    public static final int TPG_UNIT = 6;
    public static final int IO_PORTS = 7;

    private final DeviceDefinitionEvent evt;

    private Map<Short, H8SDevice> devices = new HashMap<>();

    public H8SDevices(int type) {
        evt = new DeviceDefinitionEvent();

        if (type == 2350 || type == 2351) {
            add(new H8SDevice(CPU, "H8S/" + type, DeviceType.PROCESSOR) {
            });
            add(new H8SPowerController(POWER_CONTROLLER));
            add(new H8SInterruptController(INTERRUPT_CONTROLLER));
            add(new H8SBusController(BUS_CONTROLLER));
            add(new H8SDMAController(DMA_CONTROLLER));
            add(new H8SSerialCommunicationInterface(SCI_INTERFACE));
            add(new H8STimePulseUnit(TPG_UNIT));
            add(new H8SIOPorts(IO_PORTS, type));
        }
    }

    private void add(H8SDevice device) {
        evt.addDevice(device);
        for (short addr : device.getAddresses()) {
            devices.put(addr, device);
        }
    }

    public DeviceDefinitionEvent getEvent() {
        return evt;
    }

    public DeviceRegisterEvent getReadEvent(int address, byte value) {
        short addr = (short) address;
        H8SDevice dev = devices.get(addr);
        if (dev != null) {
            return new H8SDeviceRegisterEvent(dev.getId(), addr, value, false);
        } else {
            return null;
        }
    }

    public DeviceRegisterEvent getReadEvent(int address, short value) {
        short addr = (short) address;
        H8SDevice dev = devices.get(addr);
        if (dev != null) {
            if (dev.is16Bit(addr)) {
                return new H8SDeviceRegisterEventI16(dev.getId(), addr, value, false);
            } else {
                return new H8SDeviceRegisterEvent(dev.getId(), addr, value, false);
            }
        } else {
            return null;
        }
    }

    public DeviceRegisterEvent getReadEvent(int address, int value) {
        short addr = (short) address;
        H8SDevice dev = devices.get(addr);
        if (dev != null) {
            if (dev.is32Bit(addr)) {
                return new H8SDeviceRegisterEventI32(dev.getId(), addr, value, false);
            } else if (dev.is16Bit(addr)) {
                return new H8SDeviceRegisterEventI16(dev.getId(), addr, (short) value, false);
            } else {
                return new H8SDeviceRegisterEvent(dev.getId(), addr, (short) value, false);
            }
        } else {
            return null;
        }
    }

    public DeviceRegisterEvent getWriteEvent(int address, byte value) {
        short addr = (short) address;
        H8SDevice dev = devices.get(addr);
        if (dev != null) {
            return new H8SDeviceRegisterEvent(dev.getId(), addr, value, true);
        } else {
            return null;
        }
    }

    public DeviceRegisterEvent getWriteEvent(int address, short value) {
        short addr = (short) address;
        H8SDevice dev = devices.get(addr);
        if (dev != null) {
            if (dev.is16Bit(addr)) {
                return new H8SDeviceRegisterEventI16(dev.getId(), addr, value, true);
            } else {
                return new H8SDeviceRegisterEvent(dev.getId(), addr, value, true);
            }
        } else {
            return null;
        }
    }

    public DeviceRegisterEvent getWriteEvent(int address, int value) {
        short addr = (short) address;
        H8SDevice dev = devices.get(addr);
        if (dev != null) {
            if (dev.is32Bit(addr)) {
                return new H8SDeviceRegisterEventI32(dev.getId(), addr, value, true);
            } else if (dev.is16Bit(addr)) {
                return new H8SDeviceRegisterEventI16(dev.getId(), addr, (short) value, true);
            } else {
                return new H8SDeviceRegisterEvent(dev.getId(), addr, (short) value, true);
            }
        } else {
            return null;
        }
    }

    public void setRegisterNames(TraceAnalyzer trc) {
        TypedMemory mem = trc.getTypedMemory();
        Type u8 = new Type(DataType.U8, Representation.HEX);
        Type u16 = new Type(DataType.U16, Representation.HEX);
        Type u32 = new Type(DataType.U32, Representation.HEX);

        for (Device device : evt.getDevices()) {
            H8SDevice dev = (H8SDevice) device;
            for (Entry<Integer, DeviceRegister> entry : dev.getRegisters().entrySet()) {
                DeviceRegister reg = entry.getValue();
                long addr = reg.getAddress();
                long ea = 0x00FF0000L | Short.toUnsignedInt((short) addr);
                if (dev.is32Bit((short) addr)) {
                    mem.set(ea, u32, reg.getName());
                } else if (dev.is16Bit((short) addr)) {
                    mem.set(ea, u16, reg.getName());
                } else {
                    mem.set(ea, u8, reg.getName());
                }
            }
        }
    }
}
