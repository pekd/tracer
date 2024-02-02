package org.graalvm.vm.trcview.arch.z80.device;

import org.graalvm.vm.trcview.analysis.device.DeviceType;
import org.graalvm.vm.trcview.analysis.device.EnumFieldFormat;
import org.graalvm.vm.trcview.analysis.device.FieldNumberType;
import org.graalvm.vm.trcview.analysis.device.IntegerFieldFormat;
import org.graalvm.vm.trcview.arch.z80.io.Z80DeviceRegisterEvent;

public class Z80CTC extends Z80Device {
    public static final int VECTOR = 0;
    public static final int CCW_CH0 = 1;
    public static final int CCW_CH1 = 2;
    public static final int CCW_CH2 = 3;
    public static final int CCW_CH3 = 4;
    public static final int TC_CH0 = 5;
    public static final int TC_CH1 = 6;
    public static final int TC_CH2 = 7;
    public static final int TC_CH3 = 8;

    private final byte port0;
    private final byte port1;
    private final byte port2;
    private final byte port3;

    private boolean tclatch;

    private static final String[] MODE = {"Timer", "Counter"};
    private static final String[] PRESCALER = {"16", "256"};
    private static final String[] EDGE = {"Falling", "Rising"};
    private static final String[] TRIGGER = {"Automatic when time constant is loaded", "CLK/TRG pulse starts timer"};

    public Z80CTC(int id, String name, int port0, int port1, int port2, int port3) {
        super(id, name, DeviceType.INTERFACE);
        this.port0 = (byte) port0;
        this.port1 = (byte) port1;
        this.port2 = (byte) port2;
        this.port3 = (byte) port3;

        add(reg(VECTOR, "VECTOR", port0));
        add(reg(CCW_CH0, "CCW_CH0", port0, new IntegerFieldFormat("INTERRUPT", 7), new EnumFieldFormat("MODE", 6, MODE), new EnumFieldFormat("PRESCALER", 5, PRESCALER),
                        new EnumFieldFormat("EDGE", 4, EDGE), new EnumFieldFormat("TRIGGER", 3, TRIGGER), new IntegerFieldFormat("TIME_CONSTANT_FOLLOWS", 2), new IntegerFieldFormat("RESET", 1),
                        new IntegerFieldFormat("CONTROL_WORD", 0)));
        add(reg(TC_CH0, "TC_CH0", port0, new IntegerFieldFormat("TIME_CONSTANT", 7, 0, FieldNumberType.HEX)));

        add(reg(CCW_CH1, "CCW_CH1", port1, new IntegerFieldFormat("INTERRUPT", 7), new EnumFieldFormat("MODE", 6, MODE), new EnumFieldFormat("PRESCALER", 5, PRESCALER),
                        new EnumFieldFormat("EDGE", 4, EDGE), new EnumFieldFormat("TRIGGER", 3, TRIGGER), new IntegerFieldFormat("TIME_CONSTANT_FOLLOWS", 2), new IntegerFieldFormat("RESET", 1),
                        new IntegerFieldFormat("CONTROL_WORD", 0)));
        add(reg(TC_CH1, "TC_CH1", port1, new IntegerFieldFormat("TIME_CONSTANT", 7, 0, FieldNumberType.HEX)));

        add(reg(CCW_CH2, "CCW_CH2", port2, new IntegerFieldFormat("INTERRUPT", 7), new EnumFieldFormat("MODE", 6, MODE), new EnumFieldFormat("PRESCALER", 5, PRESCALER),
                        new EnumFieldFormat("EDGE", 4, EDGE), new EnumFieldFormat("TRIGGER", 3, TRIGGER), new IntegerFieldFormat("TIME_CONSTANT_FOLLOWS", 2), new IntegerFieldFormat("RESET", 1),
                        new IntegerFieldFormat("CONTROL_WORD", 0)));
        add(reg(TC_CH2, "TC_CH2", port2, new IntegerFieldFormat("TIME_CONSTANT", 7, 0, FieldNumberType.HEX)));

        add(reg(CCW_CH3, "CCW_CH3", port3, new IntegerFieldFormat("INTERRUPT", 7), new EnumFieldFormat("MODE", 6, MODE), new EnumFieldFormat("PRESCALER", 5, PRESCALER),
                        new EnumFieldFormat("EDGE", 4, EDGE), new EnumFieldFormat("TRIGGER", 3, TRIGGER), new IntegerFieldFormat("TIME_CONSTANT_FOLLOWS", 2), new IntegerFieldFormat("RESET", 1),
                        new IntegerFieldFormat("CONTROL_WORD", 0)));
        add(reg(TC_CH3, "TC_CH3", port3, new IntegerFieldFormat("TIME_CONSTANT", 7, 0, FieldNumberType.HEX)));
    }

    private int getChannel(byte addr) {
        if (addr == port0) {
            return 0;
        } else if (addr == port1) {
            return 1;
        } else if (addr == port2) {
            return 2;
        } else if (addr == port3) {
            return 3;
        } else {
            return -1;
        }
    }

    @Override
    public Z80DeviceRegisterEvent getInputEvent(byte addr, byte value) {
        int ch = getChannel(addr);
        if (ch == -1) {
            return null;
        }

        return null;
    }

    @Override
    public Z80DeviceRegisterEvent getOutputEvent(byte addr, byte value) {
        int ch = getChannel(addr);
        if (ch == -1) {
            return null;
        }

        if (tclatch) {
            tclatch = false;
            return new Z80DeviceRegisterEvent(0, getId(), TC_CH0 + ch, value, true);
        } else if ((value & 1) == 0) {
            tclatch = false;
            return new Z80DeviceRegisterEvent(0, getId(), VECTOR, value, true);
        } else {
            tclatch = (value & 4) != 0;
            return new Z80DeviceRegisterEvent(0, getId(), CCW_CH0 + ch, value, true);
        }
    }
}
