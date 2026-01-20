package org.graalvm.vm.trcview.arch.h8s.device;

import org.graalvm.vm.trcview.analysis.device.DeviceType;
import org.graalvm.vm.trcview.analysis.device.IntegerFieldFormat;

public class H8SInterruptController extends H8SDevice {
    public static final short SYSCR = (short) 0xFF39;
    public static final short ISCRH = (short) 0xFF2C;
    public static final short ISCRL = (short) 0xFF2D;
    public static final short IER = (short) 0xFF2E;
    public static final short ISR = (short) 0xFF2F;
    public static final short IPRA = (short) 0xFEC4;
    public static final short IPRB = (short) 0xFEC5;
    public static final short IPRC = (short) 0xFEC6;
    public static final short IPRD = (short) 0xFEC7;
    public static final short IPRE = (short) 0xFEC8;
    public static final short IPRF = (short) 0xFEC9;
    public static final short IPRG = (short) 0xFECA;
    public static final short IPRH = (short) 0xFECB;
    public static final short IPRI = (short) 0xFECC;
    public static final short IPRJ = (short) 0xFECD;
    public static final short IPRK = (short) 0xFECE;

    public H8SInterruptController(int id) {
        super(id, "Interrupt Controller", DeviceType.CONTROLLER);
        add(reg(SYSCR, "SYSCR", SYSCR, new IntegerFieldFormat("INTM1", 5), new IntegerFieldFormat("INTM0", 4), new IntegerFieldFormat("NMIEG", 3), new IntegerFieldFormat("RAME", 0)));
        add(reg(IER, "IER", IER, new IntegerFieldFormat("IRQ7E", 7), new IntegerFieldFormat("IRQ6E", 6), new IntegerFieldFormat("IRQ5E", 5), new IntegerFieldFormat("IRQ4", 4),
                        new IntegerFieldFormat("IRQ3E", 3), new IntegerFieldFormat("IRQ2E", 2), new IntegerFieldFormat("IRQ1E", 1), new IntegerFieldFormat("IRQ0E", 0)));
        add(reg(ISCRH, "ISCRH", ISCRH, new IntegerFieldFormat("IRQ7SCB", 7), new IntegerFieldFormat("IRQ7SCA", 6), new IntegerFieldFormat("IRQ6SCB", 5), new IntegerFieldFormat("IRQ6SCA", 4),
                        new IntegerFieldFormat("IRQ5SCB", 3), new IntegerFieldFormat("IRQ5SCA", 2), new IntegerFieldFormat("IRQ4SCB", 1), new IntegerFieldFormat("IRQ4SCA", 0)));
        add(reg(ISCRL, "ISCRL", ISCRL, new IntegerFieldFormat("IRQ3SCB", 7), new IntegerFieldFormat("IRQ3SCA", 6), new IntegerFieldFormat("IRQ2SCB", 5), new IntegerFieldFormat("IRQ2SCA", 4),
                        new IntegerFieldFormat("IRQ1SCB", 3), new IntegerFieldFormat("IRQ1SCA", 2), new IntegerFieldFormat("IRQ0SCB", 1), new IntegerFieldFormat("IRQ0SCA", 0)));
        add(reg(ISR, "ISR", ISR, new IntegerFieldFormat("IRQ7F", 7), new IntegerFieldFormat("IRQ6F", 6), new IntegerFieldFormat("IRQ5F", 5), new IntegerFieldFormat("IRQ4F", 4),
                        new IntegerFieldFormat("IRQ3F", 3), new IntegerFieldFormat("IRQ2F", 2), new IntegerFieldFormat("IRQ1F", 1), new IntegerFieldFormat("IRQ0F", 0)));
        for (int i = 0; i < 11; i++) {
            short addr = (short) (IPRA + i);
            String name = "IPR" + (char) ('A' + i);
            add(reg(addr, name, addr, new IntegerFieldFormat("IPR6", 6), new IntegerFieldFormat("IPR5", 5), new IntegerFieldFormat("IPR4", 4), new IntegerFieldFormat("IPR2", 2),
                            new IntegerFieldFormat("IPR1", 1), new IntegerFieldFormat("IPR0", 0)));
        }
    }
}
