package org.graalvm.vm.trcview.arch.h8s.device;

import org.graalvm.vm.trcview.analysis.device.DeviceType;
import org.graalvm.vm.trcview.analysis.device.EnumFieldFormat;

public class H8SPowerController extends H8SDevice {
    public static final short MSTPCRH = (short) 0xFF3C;
    public static final short MSTPCRL = (short) 0xFF3D;

    private static final String[] MSTPCR_NAMES = {"RUN", "STOP"};

    public H8SPowerController(int id) {
        super(id, "Power Configuration", DeviceType.PROCESSOR);

        add(reg(MSTPCRH, "MSTPCRH", MSTPCRH, new EnumFieldFormat("DMAC", 7, MSTPCR_NAMES), new EnumFieldFormat("DTC", 6, MSTPCR_NAMES), new EnumFieldFormat("TPU", 5, MSTPCR_NAMES),
                        new EnumFieldFormat("PPG", 3, MSTPCR_NAMES), new EnumFieldFormat("DAC", 2, MSTPCR_NAMES), new EnumFieldFormat("ADC", 1, MSTPCR_NAMES)));
        add(reg(MSTPCRL, "MSTPCRL", MSTPCRL, new EnumFieldFormat("SCI_CH1", 6, MSTPCR_NAMES), new EnumFieldFormat("SCI_CH0", 5, MSTPCR_NAMES)));
    }
}
