package org.graalvm.vm.trcview.arch.h8s.device;

import org.graalvm.vm.trcview.analysis.device.DeviceType;
import org.graalvm.vm.trcview.analysis.device.EnumFieldFormat;
import org.graalvm.vm.trcview.analysis.device.FieldNumberType;
import org.graalvm.vm.trcview.analysis.device.IntegerFieldFormat;

public class H8STimePulseUnit extends H8SDevice {
    public static final short TCR0 = (short) 0xFFD0;
    public static final short TMDR0 = (short) 0xFFD1;
    public static final short TIOR0H = (short) 0xFFD2;
    public static final short TIOR0L = (short) 0xFFD3;
    public static final short TIER0 = (short) 0xFFD4;
    public static final short TSR0 = (short) 0xFFD5;
    public static final short TCNT0 = (short) 0xFFD6;
    public static final short TGR0A = (short) 0xFFD8;
    public static final short TGR0B = (short) 0xFFDA;
    public static final short TGR0C = (short) 0xFFDC;
    public static final short TGR0D = (short) 0xFFDE;
    public static final short TCR1 = (short) 0xFFE0;
    public static final short TMDR1 = (short) 0xFFE1;
    public static final short TIOR1 = (short) 0xFFE2;
    public static final short TIER1 = (short) 0xFFE4;
    public static final short TSR1 = (short) 0xFFE5;
    public static final short TCNT1 = (short) 0xFFE6;
    public static final short TGR1A = (short) 0xFFE8;
    public static final short TGR1B = (short) 0xFFEA;
    public static final short TCR2 = (short) 0xFFF0;
    public static final short TMDR2 = (short) 0xFFF1;
    public static final short TIOR2 = (short) 0xFFF2;
    public static final short TIER2 = (short) 0xFFF4;
    public static final short TSR2 = (short) 0xFFF5;
    public static final short TCNT2 = (short) 0xFFF6;
    public static final short TGR2A = (short) 0xFFF8;
    public static final short TGR2B = (short) 0xFFFA;
    public static final short TCR3 = (short) 0xFE80;
    public static final short TMDR3 = (short) 0xFE81;
    public static final short TIOR3H = (short) 0xFE82;
    public static final short TIOR3L = (short) 0xFE83;
    public static final short TIER3 = (short) 0xFE84;
    public static final short TSR3 = (short) 0xFE85;
    public static final short TCNT3 = (short) 0xFE86;
    public static final short TGR3A = (short) 0xFE88;
    public static final short TGR3B = (short) 0xFE8A;
    public static final short TGR3C = (short) 0xFE8C;
    public static final short TGR3D = (short) 0xFE8E;
    public static final short TCR4 = (short) 0xFE90;
    public static final short TMDR4 = (short) 0xFE91;
    public static final short TIOR4 = (short) 0xFE92;
    public static final short TIER4 = (short) 0xFE94;
    public static final short TSR4 = (short) 0xFE95;
    public static final short TCNT4 = (short) 0xFE96;
    public static final short TGR4A = (short) 0xFE98;
    public static final short TGR4B = (short) 0xFE9A;
    public static final short TCR5 = (short) 0xFEA0;
    public static final short TMDR5 = (short) 0xFEA1;
    public static final short TIOR5 = (short) 0xFEA2;
    public static final short TIER5 = (short) 0xFEA4;
    public static final short TSR5 = (short) 0xFEA5;
    public static final short TCNT5 = (short) 0xFEA6;
    public static final short TGR5A = (short) 0xFEA8;
    public static final short TGR5B = (short) 0xFEAA;
    public static final short TSTR = (short) 0xFFC0;
    public static final short TSYR = (short) 0xFFC1;

    private static final String[] CCLR_NAMES = {"TCNT clearing disabled", "TCNT cleared by TGRA compare match/input capture", "TCNT cleared by TGRB compare match/input capture",
                    "TCNT cleared by counter clearing for another channel performing synchronous clearing/synchronous operation", "TCNT clearing disabled",
                    "TCNT cleared by TGRC compare match/input capture", "TCNT cleared by TGRD compare match/input capture",
                    "TCNT cleared by counter clearing for another channel performing synchronous clearing/synchronous operation"};
    private static final String[] CKEG_NAMES = {"Count at rising edge", "Count at falling edge", "-", "-"};
    private static final String[] TPSC_CH0_NAMES = {"Internal: φ/1", "Internal: φ/4", "Internal: φ/16", "Internal: φ/64", "External: TCLKA pin input", "External: TCLKB pin input",
                    "External: TCLKC pin input", "External: TCLKD pin input"};
    private static final String[] TPSC_CH1_NAMES = {"Internal: φ/1", "Internal: φ/4", "Internal: φ/16", "Internal: φ/64", "External: TCLKA pin input", "External: TCLKB pin input", "Internal: φ/256",
                    "TCNT2 overflow/underflow"};
    private static final String[] TPSC_CH2_NAMES = {"Internal: φ/1", "Internal: φ/4", "Internal: φ/16", "Internal: φ/64", "External: TCLKA pin input", "External: TCLKB pin input",
                    "External: TCLKC pin input", "Internal: φ/1024"};
    private static final String[] TPSC_CH3_NAMES = {"Internal: φ/1", "Internal: φ/4", "Internal: φ/16", "Internal: φ/64", "External: TCLKA pin input", "Internal: φ/1024", "Internal: φ/256",
                    "Internal: φ/4096"};
    private static final String[] TPSC_CH4_NAMES = {"Internal: φ/1", "Internal: φ/4", "Internal: φ/16", "Internal: φ/64", "External: TCLKA pin input", "External: TCLKC pin input", "Internal: φ/1024",
                    "TCNT5 overflow/underflow"};
    private static final String[] TPSC_CH5_NAMES = {"Internal: φ/1", "Internal: φ/4", "Internal: φ/16", "Internal: φ/16", "External: TCLKA pin input", "External: TCLKC pin input", "Internal: φ/256",
                    "External: TCLKD pin input"};
    private static final String[] MD_NAMES = {"Normal Operation", "Reserved", "PWM mode 1", "PWM mode 2", "Phase counting mode 1", "Phase counting mode 2", "Phase counting mode 3",
                    "Phase counting mode 4", "-", "-", "-", "-", "-", "-", "-", "-"};
    private static final String[] TCFD_NAMES = {"TCNT counts down", "TCNT counts up"};

    public H8STimePulseUnit(int id) {
        super(id, "Time Pulse Unit", DeviceType.CONTROLLER);

        add(reg(TCR0, "TCR0", TCR0, new EnumFieldFormat("CCLR", 7, 5, CCLR_NAMES), new EnumFieldFormat("CKEG", 4, 3, CKEG_NAMES), new EnumFieldFormat("TPSC", 2, 0, TPSC_CH0_NAMES)));
        add(reg(TMDR0, "TMDR0", TMDR0, new IntegerFieldFormat("BFB", 5), new IntegerFieldFormat("BFA", 4), new EnumFieldFormat("MD", 3, 0, MD_NAMES)));
        add(reg(TIOR0H, "TIOR0H", TIOR0H, new IntegerFieldFormat("IOB", 7, 4, FieldNumberType.BIN), new IntegerFieldFormat("IOA", 3, 0, FieldNumberType.BIN)));
        add(reg(TIOR0L, "TIOR0L", TIOR0L, new IntegerFieldFormat("IOD", 7, 4, FieldNumberType.BIN), new IntegerFieldFormat("IOC", 3, 0, FieldNumberType.BIN)));
        add(reg(TIER0, "TIER0", TIER0, new IntegerFieldFormat("TTGE", 7), new IntegerFieldFormat("TCIEV", 4), new IntegerFieldFormat("TGIED", 3), new IntegerFieldFormat("TGIEC", 2),
                        new IntegerFieldFormat("TGIEB", 1), new IntegerFieldFormat("TGIEA", 0)));
        add(reg(TSR0, "TSR0", TSR0, new IntegerFieldFormat("TCFV", 4), new IntegerFieldFormat("TGFD", 3), new IntegerFieldFormat("TGFC", 2), new IntegerFieldFormat("TGFB", 1),
                        new IntegerFieldFormat("TGFA", 0)));
        add(reg(TCNT0, "TCNT0", TCNT0));
        add(reg(TGR0A, "TGR0A", TGR0A));
        add(reg(TGR0B, "TGR0B", TGR0B));
        add(reg(TGR0C, "TGR0C", TGR0C));
        add(reg(TGR0D, "TGR0D", TGR0D));

        add(reg(TCR1, "TCR1", TCR1, new EnumFieldFormat("CCLR", 7, 5, CCLR_NAMES), new EnumFieldFormat("CKEG", 4, 3, CKEG_NAMES), new EnumFieldFormat("TPSC", 2, 0, TPSC_CH1_NAMES)));
        add(reg(TMDR1, "TMDR1", TMDR1, new EnumFieldFormat("MD", 3, 0, MD_NAMES)));
        add(reg(TIOR1, "TIOR1", TIOR1, new IntegerFieldFormat("IOB", 7, 4, FieldNumberType.BIN), new IntegerFieldFormat("IOA", 3, 0, FieldNumberType.BIN)));
        add(reg(TIER1, "TIER1", TIER1, new IntegerFieldFormat("TTGE", 7), new IntegerFieldFormat("TCIEU", 5), new IntegerFieldFormat("TCIEV", 4), new IntegerFieldFormat("TGIEB", 1),
                        new IntegerFieldFormat("TGIEA", 0)));
        add(reg(TSR1, "TSR1", TSR1, new EnumFieldFormat("TCFD", 7, TCFD_NAMES), new IntegerFieldFormat("TCFU", 5), new IntegerFieldFormat("TCFV", 4), new IntegerFieldFormat("TGFB", 1),
                        new IntegerFieldFormat("TGFA", 0)));
        add(reg(TCNT1, "TCNT1", TCNT1));
        add(reg(TGR1A, "TGR1A", TGR1A));
        add(reg(TGR1B, "TGR1B", TGR1B));

        add(reg(TCR2, "TCR2", TCR2, new EnumFieldFormat("CCLR", 7, 5, CCLR_NAMES), new EnumFieldFormat("CKEG", 4, 3, CKEG_NAMES), new EnumFieldFormat("TPSC", 2, 0, TPSC_CH2_NAMES)));
        add(reg(TMDR2, "TMDR2", TMDR2, new EnumFieldFormat("MD", 3, 0, MD_NAMES)));
        add(reg(TIOR2, "TIOR2", TIOR2, new IntegerFieldFormat("IOB", 7, 4, FieldNumberType.BIN), new IntegerFieldFormat("IOA", 3, 0, FieldNumberType.BIN)));
        add(reg(TIER2, "TIER2", TIER2, new IntegerFieldFormat("TTGE", 7), new IntegerFieldFormat("TCIEU", 5), new IntegerFieldFormat("TCIEV", 4), new IntegerFieldFormat("TGIEB", 1),
                        new IntegerFieldFormat("TGIEA", 0)));
        add(reg(TSR2, "TSR2", TSR2, new EnumFieldFormat("TCFD", 7, TCFD_NAMES), new IntegerFieldFormat("TCFU", 5), new IntegerFieldFormat("TCFV", 4), new IntegerFieldFormat("TGFB", 1),
                        new IntegerFieldFormat("TGFA", 0)));
        add(reg(TCNT2, "TCNT2", TCNT2));
        add(reg(TGR2A, "TGR2A", TGR2A));
        add(reg(TGR2B, "TGR2B", TGR2B));

        add(reg(TCR3, "TCR3", TCR3, new EnumFieldFormat("CCLR", 7, 5, CCLR_NAMES), new EnumFieldFormat("CKEG", 4, 3, CKEG_NAMES), new EnumFieldFormat("TPSC", 2, 0, TPSC_CH3_NAMES)));
        add(reg(TMDR3, "TMDR3", TMDR3, new IntegerFieldFormat("BFB", 5), new IntegerFieldFormat("BFA", 4), new EnumFieldFormat("MD", 3, 0, MD_NAMES)));
        add(reg(TIOR3H, "TIOR3H", TIOR3H, new IntegerFieldFormat("IOB", 7, 4, FieldNumberType.BIN), new IntegerFieldFormat("IOA", 3, 0, FieldNumberType.BIN)));
        add(reg(TIOR3L, "TIOR3L", TIOR3L, new IntegerFieldFormat("IOD", 7, 4, FieldNumberType.BIN), new IntegerFieldFormat("IOC", 3, 0, FieldNumberType.BIN)));
        add(reg(TIER3, "TIER3", TIER3, new IntegerFieldFormat("TTGE", 7), new IntegerFieldFormat("TCIEV", 4), new IntegerFieldFormat("TGIED", 3), new IntegerFieldFormat("TGIEC", 2),
                        new IntegerFieldFormat("TGIEB", 1), new IntegerFieldFormat("TGIEA", 0)));
        add(reg(TSR3, "TSR3", TSR3, new IntegerFieldFormat("TCFV", 4), new IntegerFieldFormat("TGFD", 3), new IntegerFieldFormat("TGFC", 2), new IntegerFieldFormat("TGFB", 1),
                        new IntegerFieldFormat("TGFA", 0)));
        add(reg(TCNT3, "TCNT3", TCNT3));
        add(reg(TGR3A, "TGR3A", TGR3A));
        add(reg(TGR3B, "TGR3B", TGR3B));
        add(reg(TGR3C, "TGR3C", TGR3C));
        add(reg(TGR3D, "TGR3D", TGR3D));

        add(reg(TCR4, "TCR4", TCR4, new EnumFieldFormat("CCLR", 7, 5, CCLR_NAMES), new EnumFieldFormat("CKEG", 4, 3, CKEG_NAMES), new EnumFieldFormat("TPSC", 2, 0, TPSC_CH4_NAMES)));
        add(reg(TMDR4, "TMDR4", TMDR4, new EnumFieldFormat("MD", 3, 0, MD_NAMES)));
        add(reg(TIOR4, "TIOR4", TIOR4, new IntegerFieldFormat("IOB", 7, 4, FieldNumberType.BIN), new IntegerFieldFormat("IOA", 3, 0, FieldNumberType.BIN)));
        add(reg(TIER4, "TIER4", TIER4, new IntegerFieldFormat("TTGE", 7), new IntegerFieldFormat("TCIEU", 5), new IntegerFieldFormat("TCIEV", 4), new IntegerFieldFormat("TGIEB", 1),
                        new IntegerFieldFormat("TGIEA", 0)));
        add(reg(TSR4, "TSR4", TSR4, new EnumFieldFormat("TCFD", 7, TCFD_NAMES), new IntegerFieldFormat("TCFU", 5), new IntegerFieldFormat("TCFV", 4), new IntegerFieldFormat("TGFB", 1),
                        new IntegerFieldFormat("TGFA", 0)));
        add(reg(TCNT4, "TCNT4", TCNT4));
        add(reg(TGR4A, "TGR4A", TGR4A));
        add(reg(TGR4B, "TGR4B", TGR4B));

        add(reg(TCR5, "TCR5", TCR5, new EnumFieldFormat("CCLR", 7, 5, CCLR_NAMES), new EnumFieldFormat("CKEG", 4, 3, CKEG_NAMES), new EnumFieldFormat("TPSC", 2, 0, TPSC_CH5_NAMES)));
        add(reg(TMDR5, "TMDR5", TMDR5, new EnumFieldFormat("MD", 3, 0, MD_NAMES)));
        add(reg(TIOR5, "TIOR5", TIOR5, new IntegerFieldFormat("IOB", 7, 4, FieldNumberType.BIN), new IntegerFieldFormat("IOA", 3, 0, FieldNumberType.BIN)));
        add(reg(TIER5, "TIER5", TIER5, new IntegerFieldFormat("TTGE", 7), new IntegerFieldFormat("TCIEU", 5), new IntegerFieldFormat("TCIEV", 4), new IntegerFieldFormat("TGIEB", 1),
                        new IntegerFieldFormat("TGIEA", 0)));
        add(reg(TSR5, "TSR5", TSR5, new EnumFieldFormat("TCFD", 7, TCFD_NAMES), new IntegerFieldFormat("TCFU", 5), new IntegerFieldFormat("TCFV", 4), new IntegerFieldFormat("TGFB", 1),
                        new IntegerFieldFormat("TGFA", 0)));
        add(reg(TCNT5, "TCNT5", TCNT5));
        add(reg(TGR5A, "TGR5A", TGR5A));
        add(reg(TGR5B, "TGR5B", TGR5B));

        add(reg(TSTR, "TSTR", TSTR, new IntegerFieldFormat("CST5", 5), new IntegerFieldFormat("CST4", 4), new IntegerFieldFormat("CST3", 3), new IntegerFieldFormat("CST2", 2),
                        new IntegerFieldFormat("CST1", 1), new IntegerFieldFormat("CST0", 0)));
        add(reg(TSYR, "TSYR", TSYR, new IntegerFieldFormat("SYNC5", 5), new IntegerFieldFormat("SYNC4", 4), new IntegerFieldFormat("SYNC3", 3), new IntegerFieldFormat("SYNC2", 2),
                        new IntegerFieldFormat("SYNC1", 1), new IntegerFieldFormat("SYNC0", 0)));
    }

    @Override
    public boolean is16Bit(short addr) {
        switch (addr) {
            case TCNT0:
            case TGR0A:
            case TGR0B:
            case TGR0C:
            case TGR0D:
            case TCNT1:
            case TGR1A:
            case TGR1B:
            case TCNT2:
            case TGR2A:
            case TGR2B:
            case TCNT3:
            case TGR3A:
            case TGR3B:
            case TGR3C:
            case TGR3D:
            case TCNT4:
            case TGR4A:
            case TGR4B:
            case TCNT5:
            case TGR5A:
            case TGR5B:
                return true;
            default:
                return false;
        }
    }
}
