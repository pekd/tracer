package org.graalvm.vm.trcview.arch.h8s.device;

import org.graalvm.vm.trcview.analysis.device.DeviceType;
import org.graalvm.vm.trcview.analysis.device.EnumFieldFormat;
import org.graalvm.vm.trcview.analysis.device.IntegerFieldFormat;

public class H8SBusController extends H8SDevice {
    public static final short ABWCR = (short) 0xFED0;
    public static final short ASTCR = (short) 0xFED1;
    public static final short WCRH = (short) 0xFED2;
    public static final short WCRL = (short) 0xFED3;
    public static final short BCRH = (short) 0xFED4;
    public static final short BCRL = (short) 0xFED5;
    public static final short MCR = (short) 0xFED6;
    public static final short DRAMCR = (short) 0xFED7;
    public static final short RTCNT = (short) 0xFED8;
    public static final short RTCOR = (short) 0xFED9;

    private static final String[] WCR_CYCLES = {"NO_WAIT", "WAIT_1", "WAIT_2", "WAIT_3"};
    private static final String[] ICIS_NAMES = {"NO_IDLE", "IDLE"};
    private static final String[] BRSTRM_NAMES = {"BASIC_BUS", "BURST_ROM"};
    private static final String[] BRSTS1_NAMES = {"BURST_CYCLE_1", "BURST_CYCLE_2"};
    private static final String[] BRSTS0_NAMES = {"MAX_4_WORDS", "MAX_8_WORDS"};
    private static final String[] RMTS_NAMES = {"A5/A4/A3/A2=NORMAL", "A5/A4/A3=NORMAL, A2=DRAM", "A5/A4=NORMAL, A3/A2=DRAM", "A5/A4/A3/A2=DRAM", "-", "-", "-", "-"};
    private static final String[] TPC_NAMES = {"PRECHARGE_1", "PRECHARGE_2"};
    private static final String[] BE_NAMES = {"BIRST_DISABLED", "DRAM_FPM"};
    private static final String[] RCDM_NAMES = {"RAS_UP", "RAS_DOWN"};
    private static final String[] CW2_NAMES = {"DRAM_16BIT", "DRAM_8BIT"};
    private static final String[] MXC_NAMES = {"SHIFT_8BIT", "SHIFT_9BIT", "SHIFT_10BIT", "-"};
    private static final String[] RLW_NAMES = {"WAIT_NONE", "WAIT_1", "WAIT_2", "WAIT_3"};
    private static final String[] RMODE_NAMES = {"CAS_BEFORE_RAS_REFRESH", "SELF_REFRESH"};
    private static final String[] CKS_NAMES = {"COUNT_DISABLED", "COUNT_PHI_2", "COUNT_PHI_8", "COUNT_PHI_32", "COUNT_PHI_128", "COUNT_PHI_512", "COUNT_PHI_2048", "COUNT_PHI_4096"};

    public H8SBusController(int id) {
        super(id, "Bus Controller", DeviceType.CONTROLLER);
        add(reg(ABWCR, "ABWCR", ABWCR, new IntegerFieldFormat("ABW7", 7), new IntegerFieldFormat("ABW6", 6), new IntegerFieldFormat("ABW5", 5), new IntegerFieldFormat("ABW4", 4),
                        new IntegerFieldFormat("ABW3", 3), new IntegerFieldFormat("ABW2", 2), new IntegerFieldFormat("ABW1", 1), new IntegerFieldFormat("ABW0", 0)));
        add(reg(ASTCR, "ASTCR", ASTCR, new IntegerFieldFormat("AST7", 7), new IntegerFieldFormat("AST6", 6), new IntegerFieldFormat("AST5", 5), new IntegerFieldFormat("AST4", 4),
                        new IntegerFieldFormat("AST3", 3), new IntegerFieldFormat("AST2", 2), new IntegerFieldFormat("AST1", 1), new IntegerFieldFormat("AST0", 0)));
        add(reg(WCRH, "WCRH", WCRH, new EnumFieldFormat("W7", 7, 6, WCR_CYCLES), new EnumFieldFormat("W6", 5, 4, WCR_CYCLES), new EnumFieldFormat("W5", 3, 2, WCR_CYCLES),
                        new EnumFieldFormat("W4", 1, 0, WCR_CYCLES)));
        add(reg(WCRL, "WCRL", WCRL, new EnumFieldFormat("W3", 7, 6, WCR_CYCLES), new EnumFieldFormat("W2", 5, 4, WCR_CYCLES), new EnumFieldFormat("W1", 3, 2, WCR_CYCLES),
                        new EnumFieldFormat("W0", 1, 0, WCR_CYCLES)));
        add(reg(BCRH, "BCRH", BCRH, new EnumFieldFormat("ICIS1", 7, ICIS_NAMES), new EnumFieldFormat("ICIS0", 6, ICIS_NAMES), new EnumFieldFormat("BRSTRM", 5, BRSTRM_NAMES),
                        new EnumFieldFormat("BRSTS1", 4, BRSTS1_NAMES), new EnumFieldFormat("BRSTS0", 3, BRSTS0_NAMES), new EnumFieldFormat("RMTS", 2, 0, RMTS_NAMES)));
        add(reg(BCRL, "BCRL", BCRL, new IntegerFieldFormat("BRLE", 7), new IntegerFieldFormat("BREQOE", 6), new IntegerFieldFormat("LCASS", 4), new IntegerFieldFormat("DDS", 3),
                        new IntegerFieldFormat("WDBE", 1), new IntegerFieldFormat("WAITE", 0)));
        add(reg(MCR, "MCR", MCR, new EnumFieldFormat("TPC", 7, TPC_NAMES), new EnumFieldFormat("BE", 6, BE_NAMES), new EnumFieldFormat("RCDM", 5, RCDM_NAMES),
                        new EnumFieldFormat("CW2", 4, CW2_NAMES), new EnumFieldFormat("MXC", 3, 2, MXC_NAMES), new EnumFieldFormat("RLW", 1, 0, RLW_NAMES)));
        add(reg(DRAMCR, "DRAMCR", DRAMCR, new IntegerFieldFormat("RFSHE", 7), new IntegerFieldFormat("RCW", 6), new EnumFieldFormat("RMODE", 5, RMODE_NAMES), new IntegerFieldFormat("CMF", 4),
                        new IntegerFieldFormat("CMIE", 3), new EnumFieldFormat("CKS", 2, 0, CKS_NAMES)));
        add(reg(RTCNT, "RTCNT", RTCNT));
        add(reg(RTCOR, "RTCOR", RTCOR));
    }
}
