package org.graalvm.vm.trcview.arch.pdp11.device;

import org.graalvm.vm.trcview.analysis.device.Device;
import org.graalvm.vm.trcview.analysis.device.DeviceRegister;
import org.graalvm.vm.trcview.analysis.device.DeviceType;
import org.graalvm.vm.trcview.analysis.device.FieldFormat;
import org.graalvm.vm.trcview.analysis.device.FieldNumberType;
import org.graalvm.vm.trcview.analysis.device.IntegerFieldFormat;
import org.graalvm.vm.trcview.arch.io.DeviceDefinitionEvent;
import org.graalvm.vm.trcview.arch.pdp11.PDP11;

public class PDP11Devices {
    public static final int CPU = 0;
    public static final int QBUS = 1;
    public static final int DLV11J = 2;
    public static final int RXV21 = 3;

    public static final int RXV21_RX2CS = 0;
    public static final int RXV21_RX2TA = 1;
    public static final int RXV21_RX2SA = 2;
    public static final int RXV21_RX2WC = 3;
    public static final int RXV21_RX2BA = 4;
    public static final int RXV21_RX2ES = 5;
    public static final int RXV21_RX2DB = 6;

    public static final int DLV11J_RCSR = 0;
    public static final int DLV11J_RBUF = 1;
    public static final int DLV11J_XCSR = 2;
    public static final int DLV11J_XBUF = 3;

    public static DeviceDefinitionEvent createDevices() {
        DeviceDefinitionEvent evt = new DeviceDefinitionEvent(PDP11.ID);
        Device cpu = new Device(CPU, "KD11-NA", DeviceType.PROCESSOR);
        Device qbus = new Device(QBUS, "QBUS", DeviceType.BUS);
        Device dlv11j = new Device(DLV11J, "DLV11-J", DeviceType.INTERFACE);
        dlv11j.add(reg(DLV11J_RCSR, "RCSR", 0177560, new IntegerFieldFormat("READER_ENABLE", 0), new IntegerFieldFormat("RCVR_INT", 6), new IntegerFieldFormat("RCVR_DONE", 7)));
        dlv11j.add(reg(DLV11J_RBUF, "RBUF", 0177562, new IntegerFieldFormat("DATA", 7, 0, FieldNumberType.OCT), new IntegerFieldFormat("ERROR", 15), new IntegerFieldFormat("OVERRUN", 14),
                        new IntegerFieldFormat("FRAMING_ERROR", 13), new IntegerFieldFormat("PARITY_ERROR", 12)));
        dlv11j.add(reg(DLV11J_XCSR, "XCSR", 0177564, new IntegerFieldFormat("TRANSMIT_READY", 7), new IntegerFieldFormat("TRANSMIT_INT", 6), new IntegerFieldFormat("TRANSMIT_BREAK", 0)));
        dlv11j.add(reg(DLV11J_XBUF, "XBUF", 0177566));
        Device rxv21 = new Device(RXV21, "RXV21", DeviceType.STORAGE);
        rxv21.add(reg(RXV21_RX2CS, "RX2CS", 0177170, new IntegerFieldFormat("GO", 0), new IntegerFieldFormat("FUNCTION", 3, 1, FieldNumberType.OCT), new IntegerFieldFormat("UNIT_SEL", 4),
                        new IntegerFieldFormat("DONE", 5), new IntegerFieldFormat("INTR_ENB", 6), new IntegerFieldFormat("TR", 7), new IntegerFieldFormat("DEN", 8), new IntegerFieldFormat("RX02", 11),
                        new IntegerFieldFormat("EXT_ADDR", 13, 12, FieldNumberType.OCT), new IntegerFieldFormat("INIT", 14), new IntegerFieldFormat("ERROR", 15)));
        rxv21.add(reg(RXV21_RX2DB, "RX2DB", 0177172));
        rxv21.add(reg(RXV21_RX2TA, "RX2TA", 0177172));
        rxv21.add(reg(RXV21_RX2SA, "RX2SA", 0177172));
        rxv21.add(reg(RXV21_RX2WC, "RX2WC", 0177172));
        rxv21.add(reg(RXV21_RX2BA, "RX2BA", 0177172));
        rxv21.add(reg(RXV21_RX2ES, "RX2ES", 0177172));
        evt.addDevice(cpu);
        evt.addDevice(qbus);
        evt.addDevice(dlv11j);
        evt.addDevice(rxv21);
        return evt;
    }

    private static DeviceRegister reg(int id, String name, long addr, FieldFormat... fmt) {
        DeviceRegister reg = new DeviceRegister(id, name, addr, fmt);
        return reg;
    }
}
