package org.graalvm.vm.trcview.arch.z80.device;

import org.graalvm.vm.trcview.analysis.device.DeviceType;
import org.graalvm.vm.trcview.analysis.device.EnumFieldFormat;
import org.graalvm.vm.trcview.analysis.device.FieldFormat;
import org.graalvm.vm.trcview.analysis.device.FieldNumberType;
import org.graalvm.vm.trcview.analysis.device.IntegerFieldFormat;
import org.graalvm.vm.trcview.arch.z80.io.Z80DeviceRegisterEvent;

public class Z80PIO extends Z80Device {
    public static final int PIO_A_INPUT = 0;
    public static final int PIO_A_OUTPUT = 1;
    public static final int PIO_A_VECTOR = 2;
    public static final int PIO_A_MODE = 3;
    public static final int PIO_A_DIRECTION = 4;
    public static final int PIO_A_IRQCTL = 5;
    public static final int PIO_A_MASK = 6;
    public static final int PIO_B_INPUT = 7;
    public static final int PIO_B_OUTPUT = 8;
    public static final int PIO_B_VECTOR = 9;
    public static final int PIO_B_MODE = 10;
    public static final int PIO_B_DIRECTION = 11;
    public static final int PIO_B_IRQCTL = 12;
    public static final int PIO_B_MASK = 13;

    private static final String[] IO = {"Output", "Input"};

    private final byte paData;
    private final byte paCtrl;
    private final byte pbData;
    private final byte pbCtrl;

    private boolean mode3latch = false;

    public Z80PIO(int id, String name, int paData, int paCtrl, int pbData, int pbCtrl) {
        super(id, name, DeviceType.INTERFACE);
        this.paData = (byte) paData;
        this.paCtrl = (byte) paCtrl;
        this.pbData = (byte) pbData;
        this.pbCtrl = (byte) pbCtrl;

        add(reg(PIO_A_INPUT, "A_INPUT", paData));
        add(reg(PIO_A_OUTPUT, "A_OUTPUT", paData));
        add(reg(PIO_A_VECTOR, "A_VECTOR", paCtrl, new IntegerFieldFormat("VECTOR", 7, 1, FieldNumberType.HEX)));
        add(reg(PIO_A_MODE, "A_MODE", paCtrl, new EnumFieldFormat("MODE", 7, 6, new String[]{"Output", "Input", "Bidirectional", "Bit Control"})));
        add(reg(PIO_A_DIRECTION, "A_DIRECTION", paCtrl, io(7), io(6), io(5), io(4), io(3), io(2), io(1), io(0)));
        add(reg(PIO_A_IRQCTL, "A_IRQCTL", paCtrl, new IntegerFieldFormat("INTERRUPT_ENABLE", 7), new IntegerFieldFormat("AND_OR", 6), new IntegerFieldFormat("HIGH_LOW", 5)));
        add(reg(PIO_A_MASK, "A_MASK", paCtrl));

        add(reg(PIO_B_INPUT, "B_INPUT", pbData));
        add(reg(PIO_B_OUTPUT, "B_OUTPUT", pbData));
        add(reg(PIO_B_VECTOR, "B_VECTOR", pbCtrl, new IntegerFieldFormat("VECTOR", 7, 1, FieldNumberType.HEX)));
        add(reg(PIO_B_MODE, "B_MODE", paCtrl, new EnumFieldFormat("MODE", 7, 6, new String[]{"Output", "Input", "Bidirectional", "Bit Control"})));
        add(reg(PIO_B_DIRECTION, "B_DIRECTION", paCtrl, io(7), io(6), io(5), io(4), io(3), io(2), io(1), io(0)));
        add(reg(PIO_B_IRQCTL, "B_IRQCTL", pbCtrl, new IntegerFieldFormat("INTERRUPT_ENABLE", 7), new IntegerFieldFormat("AND_OR", 6), new IntegerFieldFormat("HIGH_LOW", 5)));
        add(reg(PIO_B_MASK, "B_MASK", pbCtrl));
    }

    private static FieldFormat io(int n) {
        return new EnumFieldFormat("I/O " + n, n, IO);
    }

    @Override
    public Z80DeviceRegisterEvent getInputEvent(byte addr, byte value) {
        if (addr == paData) {
            return new Z80DeviceRegisterEvent(0, getId(), PIO_A_INPUT, value, false);
        } else if (addr == pbData) {
            return new Z80DeviceRegisterEvent(0, getId(), PIO_B_INPUT, value, false);
        }
        return null;
    }

    @Override
    public Z80DeviceRegisterEvent getOutputEvent(byte addr, byte value) {
        if (addr == paData) {
            return new Z80DeviceRegisterEvent(0, getId(), PIO_A_OUTPUT, value, true);
        } else if (addr == pbData) {
            return new Z80DeviceRegisterEvent(0, getId(), PIO_B_OUTPUT, value, true);
        } else if (addr == paCtrl) {
            if (mode3latch) {
                mode3latch = false;
                return new Z80DeviceRegisterEvent(0, getId(), PIO_A_DIRECTION, value, true);
            }
            if ((value & 1) == 0) {
                return new Z80DeviceRegisterEvent(0, getId(), PIO_A_VECTOR, value, true);
            } else if ((value & 0x0F) == 0x0F) {
                mode3latch = ((value >> 6) & 0x03) == 3;
                return new Z80DeviceRegisterEvent(0, getId(), PIO_A_MODE, value, true);
            } else if ((value & 0x0F) == 0x07) {
                return new Z80DeviceRegisterEvent(0, getId(), PIO_A_IRQCTL, value, true);
            } else if ((value & 0x0F) == 0x03) {
                return new Z80DeviceRegisterEvent(0, getId(), PIO_A_IRQCTL, value, true);
            }
        } else if (addr == pbCtrl) {
            if (mode3latch) {
                mode3latch = false;
                return new Z80DeviceRegisterEvent(0, getId(), PIO_B_DIRECTION, value, true);
            }
            if ((value & 1) == 0) {
                return new Z80DeviceRegisterEvent(0, getId(), PIO_B_VECTOR, value, true);
            } else if ((value & 0x0F) == 0x0F) {
                mode3latch = ((value >> 6) & 0x03) == 3;
                return new Z80DeviceRegisterEvent(0, getId(), PIO_B_MODE, value, true);
            } else if ((value & 0x0F) == 0x07) {
                return new Z80DeviceRegisterEvent(0, getId(), PIO_B_IRQCTL, value, true);
            } else if ((value & 0x0F) == 0x03) {
                return new Z80DeviceRegisterEvent(0, getId(), PIO_B_IRQCTL, value, true);
            }
        }
        return null;
    }
}
