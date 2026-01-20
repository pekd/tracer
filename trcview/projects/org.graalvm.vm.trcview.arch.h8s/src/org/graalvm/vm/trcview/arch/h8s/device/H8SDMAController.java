package org.graalvm.vm.trcview.arch.h8s.device;

import org.graalvm.vm.trcview.analysis.device.DeviceType;
import org.graalvm.vm.trcview.analysis.device.EnumFieldFormat;
import org.graalvm.vm.trcview.analysis.device.IntegerFieldFormat;

public class H8SDMAController extends H8SDevice {
    public static final short MAR0A = (short) 0xFEE0;
    public static final short IOAR0A = (short) 0xFEE4;
    public static final short ETCR0A = (short) 0xFEE6;
    public static final short MAR0B = (short) 0xFEE8;
    public static final short IOAR0B = (short) 0xFEEC;
    public static final short ETCR0B = (short) 0xFEEE;
    public static final short MAR1A = (short) 0xFEF0;
    public static final short IOAR1A = (short) 0xFEF4;
    public static final short ETCR1A = (short) 0xFEF6;
    public static final short MAR1B = (short) 0xFEF8;
    public static final short IOAR1B = (short) 0xFEFC;
    public static final short ETCR1B = (short) 0xFEFE;
    public static final short DMAWER = (short) 0xFF00;
    public static final short DMATCR = (short) 0xFF01;
    public static final short DMACR0A = (short) 0xFF02;
    public static final short DMACR0B = (short) 0xFF03;
    public static final short DMACR1A = (short) 0xFF04;
    public static final short DMACR1B = (short) 0xFF05;
    public static final short DMABCRH = (short) 0xFF06;
    public static final short DMABCRL = (short) 0xFF07;

    private static final String[] DTSZ_NAMES = {"SIZE_BYTE", "SIZE_WORD"};
    private static final String[] DTID_NAMES = {"INC_1", "INC_2"};
    private static final String[] DTF_CHA_NAMES = {"-", "AD_END", "-", "-", "SCI_CH0_TXI", "SCI_CH0_RXI", "SCI_CH1_TXI", "SCI_CH1_RXI", "TPU_CH0", "TPU_CH1", "TPU_CH2", "TPU_CH3", "TPU_CH4",
                    "TPU_CH5", "-", "-"};
    private static final String[] DTF_CHB_NAMES = {"-", "AD_END", "nDREQ_NEGEDGE", "nDREG_LOW_LEVEL", "SCI_CH0_TXI", "SCI_CH0_RXI", "SCI_CH1_TXI", "SCI_CH1_RXI", "TPU_CH0", "TPU_CH1", "TPU_CH2",
                    "TPU_CH3", "TPU_CH4", "TPU_CH5", "-", "-"};
    private static final String[] FAE_NAMES = {"MODE_SHORT", "MODE_FULL"};
    private static final String[] SAE_NAMES = {"MODE_DUAL", "MODE_SINGLE"};

    // TODO: full address mode
    public H8SDMAController(int id) {
        super(id, "DMA Controller", DeviceType.CONTROLLER);
        add(reg(MAR0A, "MAR0A", MAR0A));
        add(reg(IOAR0A, "IOAR0A", IOAR0A));
        add(reg(ETCR0A, "ETCR0A", ETCR0A));
        add(reg(MAR0B, "MAR0B", MAR0B));
        add(reg(IOAR0B, "IOAR0B", IOAR0B));
        add(reg(ETCR0B, "ETCR0B", ETCR0B));
        add(reg(MAR1A, "MAR1A", MAR1A));
        add(reg(IOAR1A, "IOAR1A", IOAR1A));
        add(reg(ETCR1A, "ETCR1A", ETCR1A));
        add(reg(MAR1B, "MAR1B", MAR1B));
        add(reg(IOAR1B, "IOAR1B", IOAR1B));
        add(reg(ETCR1B, "ETCR1B", ETCR1B));
        add(reg(DMACR0A, "DMACR0A", DMACR0A, new EnumFieldFormat("DTSZ", 7, DTSZ_NAMES), new EnumFieldFormat("DTID", 6, DTID_NAMES), new IntegerFieldFormat("RPE", 5),
                        new IntegerFieldFormat("DTDIR", 4), new EnumFieldFormat("DTF", 3, 0, DTF_CHA_NAMES)));
        add(reg(DMACR0B, "DMACR0B", DMACR0B, new EnumFieldFormat("DTSZ", 7, DTSZ_NAMES), new EnumFieldFormat("DTID", 6, DTID_NAMES), new IntegerFieldFormat("RPE", 5),
                        new IntegerFieldFormat("DTDIR", 4), new EnumFieldFormat("DTF", 3, 0, DTF_CHB_NAMES)));
        add(reg(DMACR1A, "DMACR1A", DMACR1A, new EnumFieldFormat("DTSZ", 7, DTSZ_NAMES), new EnumFieldFormat("DTID", 6, DTID_NAMES), new IntegerFieldFormat("RPE", 5),
                        new IntegerFieldFormat("DTDIR", 4), new EnumFieldFormat("DTF", 3, 0, DTF_CHA_NAMES)));
        add(reg(DMACR1B, "DMACR1B", DMACR1B, new EnumFieldFormat("DTSZ", 7, DTSZ_NAMES), new EnumFieldFormat("DTID", 6, DTID_NAMES), new IntegerFieldFormat("RPE", 5),
                        new IntegerFieldFormat("DTDIR", 4), new EnumFieldFormat("DTF", 3, 0, DTF_CHB_NAMES)));
        add(reg(DMABCRH, "DMABCRH", DMABCRH, new EnumFieldFormat("FAE1", 7, FAE_NAMES), new EnumFieldFormat("FAE0", 6, FAE_NAMES), new EnumFieldFormat("SAE1", 5, SAE_NAMES),
                        new EnumFieldFormat("SAE0", 4, SAE_NAMES), new IntegerFieldFormat("DTA1B", 3), new IntegerFieldFormat("DTA1A", 2), new IntegerFieldFormat("DTA0B", 1),
                        new IntegerFieldFormat("DTA0A", 0)));
        add(reg(DMABCRL, "DMABCRL", DMABCRL, new IntegerFieldFormat("DTE1B", 7), new IntegerFieldFormat("DTE1A", 6), new IntegerFieldFormat("DTE0B", 5), new IntegerFieldFormat("DTE0A", 4),
                        new IntegerFieldFormat("DTIE1B", 3), new IntegerFieldFormat("DTIE1A", 2), new IntegerFieldFormat("DTIE0B", 1), new IntegerFieldFormat("DTIE0A", 0)));
        add(reg(DMAWER, "DMAWER", DMAWER, new IntegerFieldFormat("WE1B", 3), new IntegerFieldFormat("WE1A", 2), new IntegerFieldFormat("WE0B", 1), new IntegerFieldFormat("WE0A", 0)));
        add(reg(DMATCR, "DMATCR", DMATCR, new IntegerFieldFormat("TEE1", 5), new IntegerFieldFormat("TEE0", 4)));
    }

    @Override
    public boolean is16Bit(short address) {
        int addr = Short.toUnsignedInt(address);
        if (addr >= 0xFF00 && addr <= 0xFF06) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean is32Bit(short address) {
        switch (address) {
            case MAR0A:
            case MAR0B:
            case MAR1A:
            case MAR1B:
                return true;
            default:
                return false;
        }
    }
}
