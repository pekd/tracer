package org.graalvm.vm.trcview.arch.h8s.device;

import org.graalvm.vm.trcview.analysis.device.DeviceType;
import org.graalvm.vm.trcview.analysis.device.EnumFieldFormat;
import org.graalvm.vm.trcview.analysis.device.FieldNumberType;
import org.graalvm.vm.trcview.analysis.device.IntegerFieldFormat;

public class H8SSerialCommunicationInterface extends H8SDevice {
    public static final short SMR0 = (short) 0xFF78;
    public static final short BRR0 = (short) 0xFF79;
    public static final short SCR0 = (short) 0xFF7A;
    public static final short TDR0 = (short) 0xFF7B;
    public static final short SSR0 = (short) 0xFF7C;
    public static final short RDR0 = (short) 0xFF7D;
    public static final short SCMR0 = (short) 0xFF7E;
    public static final short SMR1 = (short) 0xFF80;
    public static final short BRR1 = (short) 0xFF81;
    public static final short SCR1 = (short) 0xFF82;
    public static final short TDR1 = (short) 0xFF83;
    public static final short SSR1 = (short) 0xFF84;
    public static final short RDR1 = (short) 0xFF85;
    public static final short SCMR1 = (short) 0xFF86;

    private static final String[] CA_NAMES = {"MODE_ASYNC", "MODE_SYNC"};
    private static final String[] CHR_NAMES = {"DATA_8BIT", "DATA_7BIT"};
    private static final String[] OE_NAMES = {"PARITY_EVEN", "PARITY_ODD"};
    private static final String[] STOP_NAMES = {"STOP_1BIT", "STOP_2BIT"};
    private static final String[] CKS_NAMES = {"CLOCK_1", "CLOCK_4", "CLOCK_16", "CLOCK_64"};
    private static final String[] SDIR_NAMES = {"DIR_LSB_FIRST", "DIR_MSB_FIRST"};

    public H8SSerialCommunicationInterface(int id) {
        super(id, "Serial Communication Interface", DeviceType.INTERFACE);

        add(reg(RDR0, "RDR0", RDR0));
        add(reg(TDR0, "TDR0", TDR0));
        add(reg(SMR0, "SMR0", SMR0, new EnumFieldFormat("C/!A", 7, CA_NAMES), new EnumFieldFormat("CHR", 6, CHR_NAMES), new IntegerFieldFormat("PE", 5), new EnumFieldFormat("O/!E", 4, OE_NAMES),
                        new EnumFieldFormat("STOP", 3, STOP_NAMES), new IntegerFieldFormat("MP", 2), new EnumFieldFormat("CKS", 1, 0, CKS_NAMES)));
        add(reg(SCR0, "SCR0", SCR0, new IntegerFieldFormat("TIE", 7), new IntegerFieldFormat("RIE", 6), new IntegerFieldFormat("TE", 5), new IntegerFieldFormat("RE", 4),
                        new IntegerFieldFormat("MPIE", 3), new IntegerFieldFormat("TEIE", 2), new IntegerFieldFormat("CKE", 1, 0, FieldNumberType.HEX)));
        add(reg(SSR0, "SSR0", SSR0, new IntegerFieldFormat("TDRE", 7), new IntegerFieldFormat("RDRF", 6), new IntegerFieldFormat("ORER", 5), new IntegerFieldFormat("FER", 4),
                        new IntegerFieldFormat("PER", 3), new IntegerFieldFormat("TEND", 2), new IntegerFieldFormat("MPB", 1), new IntegerFieldFormat("MPBT", 0)));
        add(reg(BRR0, "BRR0", BRR0));
        add(reg(SCMR0, "SCMR0", SCMR0, new EnumFieldFormat("SDIR", 3, SDIR_NAMES), new IntegerFieldFormat("SINV", 2), new IntegerFieldFormat("SMIF", 0)));
        add(reg(RDR1, "RDR1", RDR1));
        add(reg(TDR1, "TDR1", TDR1));
        add(reg(SMR1, "SMR1", SMR1, new EnumFieldFormat("C/!A", 7, CA_NAMES), new EnumFieldFormat("CHR", 6, CHR_NAMES), new IntegerFieldFormat("PE", 5), new EnumFieldFormat("O/!E", 4, OE_NAMES),
                        new EnumFieldFormat("STOP", 3, STOP_NAMES), new IntegerFieldFormat("MP", 2), new EnumFieldFormat("CKS", 1, 0, CKS_NAMES)));
        add(reg(SCR1, "SCR1", SCR1, new IntegerFieldFormat("TIE", 7), new IntegerFieldFormat("RIE", 6), new IntegerFieldFormat("TE", 5), new IntegerFieldFormat("RE", 4),
                        new IntegerFieldFormat("MPIE", 3), new IntegerFieldFormat("TEIE", 2), new IntegerFieldFormat("CKE", 1, 0, FieldNumberType.HEX)));
        add(reg(SSR1, "SSR1", SSR1, new IntegerFieldFormat("TDRE", 7), new IntegerFieldFormat("RDRF", 6), new IntegerFieldFormat("ORER", 5), new IntegerFieldFormat("FER", 4),
                        new IntegerFieldFormat("PER", 3), new IntegerFieldFormat("TEND", 2), new IntegerFieldFormat("MPB", 1), new IntegerFieldFormat("MPBT", 0)));
        add(reg(BRR1, "BRR1", BRR1));
        add(reg(SCMR1, "SCMR1", SCMR1, new EnumFieldFormat("SDIR", 3, SDIR_NAMES), new IntegerFieldFormat("SINV", 2), new IntegerFieldFormat("SMIF", 0)));
    }
}
