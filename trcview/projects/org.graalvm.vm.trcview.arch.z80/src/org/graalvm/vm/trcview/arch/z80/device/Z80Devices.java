package org.graalvm.vm.trcview.arch.z80.device;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.graalvm.vm.trcview.analysis.device.Device;
import org.graalvm.vm.trcview.analysis.device.DeviceRegister;
import org.graalvm.vm.trcview.analysis.device.DeviceType;
import org.graalvm.vm.trcview.arch.io.DeviceDefinitionEvent;
import org.graalvm.vm.trcview.arch.z80.io.Z80DeviceRegisterEvent;
import org.graalvm.vm.util.io.WordInputStream;

public class Z80Devices {
    public static final int CPU = 0;

    public static final int CPU_I = 0;
    public static final int CPU_IM = 1;
    public static final int CPU_EI = 2;

    public static final int DEV_PIO = 0;
    public static final int DEV_SIO = 1;
    public static final int DEV_CTC = 2;
    public static final int DEV_REG = 3;

    private final DeviceDefinitionEvent evt;

    private Map<Byte, Z80Device> devices = new HashMap<>();

    public Z80Devices(WordInputStream in) throws IOException {
        evt = new DeviceDefinitionEvent();
        Device cpu = new Device(CPU, "Z80", DeviceType.PROCESSOR);
        cpu.add(new DeviceRegister(CPU_I, "I", 0));
        cpu.add(new DeviceRegister(CPU_IM, "IM", 0));
        cpu.add(new DeviceRegister(CPU_EI, "EI", 0));
        evt.addDevice(cpu);

        int count = in.read8bit();
        int pioN = 0;
        int sioN = 0;
        int ctcN = 0;
        for (int i = 0; i < count; i++) {
            int type = in.read8bit();
            switch (type) {
                case DEV_PIO: {
                    int id = in.read8bit();
                    int paData = in.read8bit();
                    int paCtrl = in.read8bit();
                    int pbData = in.read8bit();
                    int pbCtrl = in.read8bit();
                    Z80PIO pio = new Z80PIO(id, pioN > 0 ? ("PIO " + pioN) : "PIO", paData, paCtrl, pbData, pbCtrl);
                    evt.addDevice(pio);
                    devices.put((byte) paData, pio);
                    devices.put((byte) paCtrl, pio);
                    devices.put((byte) pbData, pio);
                    devices.put((byte) pbCtrl, pio);
                    pioN++;
                    break;
                }
                case DEV_SIO: {
                    int id = in.read8bit();
                    int paData = in.read8bit();
                    int paCtrl = in.read8bit();
                    int pbData = in.read8bit();
                    int pbCtrl = in.read8bit();
                    Z80SIO sio = new Z80SIO(id, sioN > 0 ? ("SIO " + sioN) : "SIO", paData, paCtrl, pbData, pbCtrl);
                    evt.addDevice(sio);
                    devices.put((byte) paData, sio);
                    devices.put((byte) paCtrl, sio);
                    devices.put((byte) pbData, sio);
                    devices.put((byte) pbCtrl, sio);
                    sioN++;
                    break;
                }
                case DEV_CTC: {
                    int id = in.read8bit();
                    int port0 = in.read8bit();
                    int port1 = in.read8bit();
                    int port2 = in.read8bit();
                    int port3 = in.read8bit();
                    Z80CTC ctc = new Z80CTC(id, ctcN > 0 ? ("CTC " + ctcN) : "CTC", port0, port1, port2, port3);
                    evt.addDevice(ctc);
                    devices.put((byte) port0, ctc);
                    devices.put((byte) port1, ctc);
                    devices.put((byte) port2, ctc);
                    devices.put((byte) port3, ctc);
                    ctcN++;
                    break;
                }
            }
        }
    }

    public DeviceDefinitionEvent getEvent() {
        return evt;
    }

    public Z80DeviceRegisterEvent getInputEvent(short address, byte value) {
        byte addr = (byte) address;
        Z80Device dev = devices.get(addr);
        if (dev != null) {
            return dev.getInputEvent(addr, value);
        } else {
            return null;
        }
    }

    public Z80DeviceRegisterEvent getOutputEvent(short address, byte value) {
        byte addr = (byte) address;
        Z80Device dev = devices.get(addr);
        if (dev != null) {
            return dev.getOutputEvent(addr, value);
        } else {
            return null;
        }
    }
}
