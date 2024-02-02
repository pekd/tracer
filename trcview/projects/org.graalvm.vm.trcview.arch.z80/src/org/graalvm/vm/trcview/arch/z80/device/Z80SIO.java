package org.graalvm.vm.trcview.arch.z80.device;

import org.graalvm.vm.trcview.analysis.device.DeviceType;
import org.graalvm.vm.trcview.analysis.device.EnumFieldFormat;
import org.graalvm.vm.trcview.analysis.device.FieldNumberType;
import org.graalvm.vm.trcview.analysis.device.IntegerFieldFormat;
import org.graalvm.vm.trcview.arch.z80.io.Z80DeviceRegisterEvent;

public class Z80SIO extends Z80Device {
    public static final int SIO_A_RX = 0;
    public static final int SIO_A_TX = 1;
    public static final int SIO_A_RR0 = 2;
    public static final int SIO_A_RR1 = 3;
    // public static final int SIO_A_RR2 = 4;
    public static final int SIO_A_WR0 = 5;
    public static final int SIO_A_WR1 = 6;
    // public static final int SIO_A_WR2 = 7;
    public static final int SIO_A_WR3 = 8;
    public static final int SIO_A_WR4 = 9;
    public static final int SIO_A_WR5 = 10;
    public static final int SIO_A_WR6 = 11;
    public static final int SIO_A_WR7 = 12;
    public static final int SIO_B_RX = 13;
    public static final int SIO_B_TX = 14;
    public static final int SIO_B_RR0 = 15;
    public static final int SIO_B_RR1 = 16;
    public static final int SIO_B_RR2 = 17;
    public static final int SIO_B_WR0 = 18;
    public static final int SIO_B_WR1 = 19;
    public static final int SIO_B_WR2 = 20;
    public static final int SIO_B_WR3 = 21;
    public static final int SIO_B_WR4 = 22;
    public static final int SIO_B_WR5 = 23;
    public static final int SIO_B_WR6 = 24;
    public static final int SIO_B_WR7 = 25;

    private static final String[] CRC_RESET_CODE = {"Null Code", "Reset RX CRC Checker", "Reset TX CRC Generator", "Reset TX Underrun/EOM latch"};
    private static final String[] WR0_CMD = {"NOP", "Send Abort", "Reset Ext/Status Interrupts", "Channel Reset", "Enable INT on next RX Char", "Reset TX INT Pending", "Error Reset",
                    "Return from INT"};
    private static final String[] RX_INT_MODE = {"RX INT disable", "RX INT on first char", "INT on all RX chars (parity affects vector)", "INT on all RX chars (parity does not affect vector)"};
    private static final String[] RX_SIZE = {"5 Bits/Character", "7 Bits/Character", "6 Bits/Character", "8 Bits/Character"};
    private static final String[] CLOCK_MODE = {"X1", "X16", "X32", "X64"};
    private static final String[] SYNC_MODE = {"8 Bit Sync Char", "16 Bit Sync Char", "SDLC Mode", "External Sync Mode"};
    private static final String[] STOP_MODE = {"Sync modes enable", "1 stop bit/char", "1.5 stop bits/char", "2 stop bits/char"};
    private static final String[] PARITY = {"Odd", "Even"};
    private static final String[] CRC_POLYNOMIAL = {"SDLC", "CRC-16"};

    private final byte paData;
    private final byte paCtrl;
    private final byte pbData;
    private final byte pbCtrl;

    private int ptrlatchA = 0;
    private int ptrlatchB = 0;

    public Z80SIO(int id, String name, int paData, int paCtrl, int pbData, int pbCtrl) {
        super(id, name, DeviceType.INTERFACE);
        this.paData = (byte) paData;
        this.paCtrl = (byte) paCtrl;
        this.pbData = (byte) pbData;
        this.pbCtrl = (byte) pbCtrl;

        // channel A
        add(reg(SIO_A_RX, "A_RX", paData));
        add(reg(SIO_A_TX, "A_TX", paData));
        add(reg(SIO_A_RR0, "A_RR0", paCtrl, new IntegerFieldFormat("BREAK/ABORT", 7), new IntegerFieldFormat("TXUNDERRUN/EOM", 6), new IntegerFieldFormat("CTS", 5),
                        new IntegerFieldFormat("SYNC/HUNT", 4), new IntegerFieldFormat("DCD", 3), new IntegerFieldFormat("TXE", 2), new IntegerFieldFormat("INT_PENDING", 1),
                        new IntegerFieldFormat("RXNE", 0)));
        add(reg(SIO_A_RR1, "A_RR1", paCtrl, new IntegerFieldFormat("EOF", 7), new IntegerFieldFormat("CRC/FRAMING ERROR", 6), new IntegerFieldFormat("RX_UNDERRUN", 5),
                        new IntegerFieldFormat("PARITY_ERROR", 4), new IntegerFieldFormat("RESIDUE_CODE", 3, 1, FieldNumberType.BIN), new IntegerFieldFormat("ALL_SENT", 0)));
        // add(reg(SIO_A_RR2, "A_RR2", paCtrl, new IntegerFieldFormat("VECTOR", 7, 0,
        // FieldNumberType.HEX)));
        add(reg(SIO_A_WR0, "A_WR0", paCtrl, new EnumFieldFormat("CRC_RESET_CODE", 7, 6, CRC_RESET_CODE), new EnumFieldFormat("CMD", 5, 3, WR0_CMD),
                        new IntegerFieldFormat("PTR", 2, 0, FieldNumberType.HEX)));
        add(reg(SIO_A_WR1, "A_WR1", paCtrl, new IntegerFieldFormat("WAIT/READY ENABLE", 7), new IntegerFieldFormat("!WAIT/READY_FUNCTION", 6), new IntegerFieldFormat("WAIT/READY ON R/T", 5),
                        new EnumFieldFormat("RX_INT_MODE", 4, 3, RX_INT_MODE), new IntegerFieldFormat("TX_INT_ENABLE", 1), new IntegerFieldFormat("EXT_INT_ENABLE", 0)));
        // add(reg(SIO_A_WR2, "A_WR2", paCtrl, new IntegerFieldFormat("VECTOR", 7, 0,
        // FieldNumberType.HEX)));
        add(reg(SIO_A_WR3, "A_WR3", paCtrl, new EnumFieldFormat("RX_BITS", 7, 6, RX_SIZE), new IntegerFieldFormat("AUTO_ENABLES", 5), new IntegerFieldFormat("ENTER_HUNT_PHASE", 4),
                        new IntegerFieldFormat("RX_CRC_ENABLE", 3), new IntegerFieldFormat("ADDR_SEARCH_MODE", 2), new IntegerFieldFormat("SYNC_CHAR_LOAD_INHIBIT", 1),
                        new IntegerFieldFormat("RX_ENABLE", 0)));
        add(reg(SIO_A_WR4, "A_WR4", paCtrl, new EnumFieldFormat("CLOCK_MODE", 7, 6, CLOCK_MODE), new EnumFieldFormat("SYNC_MODE", 5, 4, SYNC_MODE), new EnumFieldFormat("STOP_MODE", 3, 2, STOP_MODE),
                        new EnumFieldFormat("PARITY", 1, PARITY), new IntegerFieldFormat("PARITY_ENABLE", 0)));
        add(reg(SIO_A_WR5, "A_WR5", paCtrl, new IntegerFieldFormat("DTR", 7), new EnumFieldFormat("TX_BITS", 6, 5, RX_SIZE), new IntegerFieldFormat("SEND_BREAK", 4),
                        new IntegerFieldFormat("TX_ENABLE", 3), new EnumFieldFormat("CRC_POLYNOMIAL", 2, CRC_POLYNOMIAL), new IntegerFieldFormat("RTS", 1),
                        new IntegerFieldFormat("TX_CRC_ENABLE", 0)));
        add(reg(SIO_A_WR6, "A_WR6", paCtrl, new IntegerFieldFormat("SYNC[7..0]", 7, 0, FieldNumberType.BIN)));
        add(reg(SIO_A_WR7, "A_WR7", paCtrl, new IntegerFieldFormat("SYNC[15..8]", 7, 0, FieldNumberType.BIN)));

        // channel B
        add(reg(SIO_B_RX, "B_RX", pbData));
        add(reg(SIO_B_TX, "B_TX", pbData));
        add(reg(SIO_B_RR0, "B_RR0", pbCtrl, new IntegerFieldFormat("BREAK/ABORT", 7), new IntegerFieldFormat("TXUNDERRUN/EOM", 6), new IntegerFieldFormat("CTS", 5),
                        new IntegerFieldFormat("SYNC/HUNT", 4), new IntegerFieldFormat("DCD", 3), new IntegerFieldFormat("TXE", 2), new IntegerFieldFormat("INT_PENDING", 1),
                        new IntegerFieldFormat("RXNE", 0)));
        add(reg(SIO_B_RR1, "B_RR1", pbCtrl, new IntegerFieldFormat("EOF", 7), new IntegerFieldFormat("CRC/FRAMING ERROR", 6), new IntegerFieldFormat("RX_UNDERRUN", 5),
                        new IntegerFieldFormat("PARITY_ERROR", 4), new IntegerFieldFormat("RESIDUE_CODE", 3, 1, FieldNumberType.BIN), new IntegerFieldFormat("ALL_SENT", 0)));
        add(reg(SIO_B_RR2, "B_RR2", pbCtrl, new IntegerFieldFormat("VECTOR", 7, 0, FieldNumberType.HEX)));
        add(reg(SIO_B_WR0, "B_WR0", pbCtrl, new EnumFieldFormat("CRC_RESET_CODE", 7, 6, CRC_RESET_CODE), new EnumFieldFormat("CMD", 5, 3, WR0_CMD),
                        new IntegerFieldFormat("PTR", 2, 0, FieldNumberType.HEX)));
        add(reg(SIO_B_WR1, "B_WR1", pbCtrl, new IntegerFieldFormat("WAIT/READY ENABLE", 7), new IntegerFieldFormat("!WAIT/READY_FUNCTION", 6), new IntegerFieldFormat("WAIT/READY ON R/T", 5),
                        new EnumFieldFormat("RX_INT_MODE", 4, 3, RX_INT_MODE), new IntegerFieldFormat("STATUS_AFFECTS_VECTOR", 2), new IntegerFieldFormat("TX_INT_ENABLE", 1),
                        new IntegerFieldFormat("EXT_INT_ENABLE", 0)));
        add(reg(SIO_B_WR2, "B_WR2", pbCtrl, new IntegerFieldFormat("VECTOR", 7, 0, FieldNumberType.HEX)));
        add(reg(SIO_B_WR3, "B_WR3", pbCtrl, new EnumFieldFormat("RX_BITS", 7, 6, RX_SIZE), new IntegerFieldFormat("AUTO_ENABLES", 5), new IntegerFieldFormat("ENTER_HUNT_PHASE", 4),
                        new IntegerFieldFormat("RX_CRC_ENABLE", 3), new IntegerFieldFormat("ADDR_SEARCH_MODE", 2), new IntegerFieldFormat("SYNC_CHAR_LOAD_INHIBIT", 1),
                        new IntegerFieldFormat("RX_ENABLE", 0)));
        add(reg(SIO_B_WR4, "B_WR4", pbCtrl, new EnumFieldFormat("CLOCK_MODE", 7, 6, CLOCK_MODE), new EnumFieldFormat("SYNC_MODE", 5, 4, SYNC_MODE), new EnumFieldFormat("STOP_MODE", 3, 2, STOP_MODE),
                        new EnumFieldFormat("PARITY", 1, PARITY), new IntegerFieldFormat("PARITY_ENABLE", 0)));
        add(reg(SIO_B_WR5, "B_WR5", pbCtrl, new IntegerFieldFormat("DTR", 7), new EnumFieldFormat("TX_BITS", 6, 5, RX_SIZE), new IntegerFieldFormat("SEND_BREAK", 4),
                        new IntegerFieldFormat("TX_ENABLE", 3), new EnumFieldFormat("CRC_POLYNOMIAL", 2, CRC_POLYNOMIAL), new IntegerFieldFormat("RTS", 1),
                        new IntegerFieldFormat("TX_CRC_ENABLE", 0)));
        add(reg(SIO_B_WR6, "B_WR6", pbCtrl, new IntegerFieldFormat("SYNC[7..0]", 7, 0, FieldNumberType.BIN)));
        add(reg(SIO_B_WR7, "B_WR7", pbCtrl, new IntegerFieldFormat("SYNC[15..8]", 7, 0, FieldNumberType.BIN)));
    }

    @Override
    public Z80DeviceRegisterEvent getInputEvent(byte addr, byte value) {
        if (addr == paData) {
            return new Z80DeviceRegisterEvent(0, getId(), SIO_A_RX, value, false);
        } else if (addr == pbData) {
            return new Z80DeviceRegisterEvent(0, getId(), SIO_B_RX, value, false);
        } else if (addr == paCtrl) {
            switch (ptrlatchA) {
                case 0:
                    ptrlatchA = 0;
                    return new Z80DeviceRegisterEvent(0, getId(), SIO_A_RR0, value, false);
                case 1:
                    ptrlatchA = 0;
                    return new Z80DeviceRegisterEvent(0, getId(), SIO_A_RR1, value, false);
                default:
                    ptrlatchA = 0;
                    return null;
            }
        } else if (addr == pbCtrl) {
            switch (ptrlatchB) {
                case 0:
                    ptrlatchB = 0;
                    return new Z80DeviceRegisterEvent(0, getId(), SIO_B_RR0, value, false);
                case 1:
                    ptrlatchB = 0;
                    return new Z80DeviceRegisterEvent(0, getId(), SIO_B_RR1, value, false);
                case 2:
                    ptrlatchB = 0;
                    return new Z80DeviceRegisterEvent(0, getId(), SIO_B_RR2, value, false);
                default:
                    ptrlatchA = 0;
                    return null;
            }
        }
        return null;
    }

    @Override
    public Z80DeviceRegisterEvent getOutputEvent(byte addr, byte value) {
        if (addr == paData) {
            return new Z80DeviceRegisterEvent(0, getId(), SIO_A_TX, value, true);
        } else if (addr == pbData) {
            return new Z80DeviceRegisterEvent(0, getId(), SIO_B_TX, value, true);
        } else if (addr == paCtrl) {
            if (ptrlatchA != 0) {
                switch (ptrlatchA) {
                    case 1:
                        ptrlatchA = 0;
                        return new Z80DeviceRegisterEvent(0, getId(), SIO_A_WR1, value, true);
                    case 2:
                        ptrlatchA = 0;
                        return null;
                    case 3:
                        ptrlatchA = 0;
                        return new Z80DeviceRegisterEvent(0, getId(), SIO_A_WR3, value, true);
                    case 4:
                        ptrlatchA = 0;
                        return new Z80DeviceRegisterEvent(0, getId(), SIO_A_WR4, value, true);
                    case 5:
                        ptrlatchA = 0;
                        return new Z80DeviceRegisterEvent(0, getId(), SIO_A_WR5, value, true);
                    case 6:
                        ptrlatchA = 0;
                        return new Z80DeviceRegisterEvent(0, getId(), SIO_A_WR6, value, true);
                    case 7:
                        ptrlatchA = 0;
                        return new Z80DeviceRegisterEvent(0, getId(), SIO_A_WR7, value, true);
                    default:
                        ptrlatchA = 0;
                        return null;
                }
            } else {
                ptrlatchA = value & 0x07;
                return new Z80DeviceRegisterEvent(0, getId(), SIO_A_WR0, value, true);
            }
        } else if (addr == pbCtrl) {
            if (ptrlatchB != 0) {
                switch (ptrlatchB) {
                    case 1:
                        ptrlatchB = 0;
                        return new Z80DeviceRegisterEvent(0, getId(), SIO_B_WR1, value, true);
                    case 2:
                        ptrlatchB = 0;
                        return new Z80DeviceRegisterEvent(0, getId(), SIO_B_WR2, value, true);
                    case 3:
                        ptrlatchB = 0;
                        return new Z80DeviceRegisterEvent(0, getId(), SIO_B_WR3, value, true);
                    case 4:
                        ptrlatchB = 0;
                        return new Z80DeviceRegisterEvent(0, getId(), SIO_B_WR4, value, true);
                    case 5:
                        ptrlatchB = 0;
                        return new Z80DeviceRegisterEvent(0, getId(), SIO_B_WR5, value, true);
                    case 6:
                        ptrlatchB = 0;
                        return new Z80DeviceRegisterEvent(0, getId(), SIO_B_WR6, value, true);
                    case 7:
                        ptrlatchB = 0;
                        return new Z80DeviceRegisterEvent(0, getId(), SIO_B_WR7, value, true);
                    default:
                        ptrlatchB = 0;
                        return null;
                }
            } else {
                ptrlatchB = value & 0x07;
                return new Z80DeviceRegisterEvent(0, getId(), SIO_B_WR0, value, true);
            }
        }
        return null;
    }
}
