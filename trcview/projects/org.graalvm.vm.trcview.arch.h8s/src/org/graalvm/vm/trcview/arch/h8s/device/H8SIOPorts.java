package org.graalvm.vm.trcview.arch.h8s.device;

import org.graalvm.vm.trcview.analysis.device.DeviceType;
import org.graalvm.vm.trcview.analysis.device.IntegerFieldFormat;

public class H8SIOPorts extends H8SDevice {
    public static final short P1DDR = (short) 0xFEB0;
    public static final short P1DR = (short) 0xFF60;
    public static final short PORT1 = (short) 0xFF50;
    public static final short P2DDR = (short) 0xFEB1;
    public static final short P2DR = (short) 0xFF61;
    public static final short PORT2 = (short) 0xFF51;
    public static final short P3DDR = (short) 0xFEB2;
    public static final short P3DR = (short) 0xFF62;
    public static final short PORT3 = (short) 0xFF52;
    public static final short P3ODR = (short) 0xFF76;
    public static final short PORT4 = (short) 0xFF53;
    public static final short P5DDR = (short) 0xFEB4;
    public static final short P5DR = (short) 0xFF64;
    public static final short PORT5 = (short) 0xFF54;
    public static final short P6DDR = (short) 0xFEB5;
    public static final short P6DR = (short) 0xFF65;
    public static final short PORT6 = (short) 0xFF55;
    public static final short PADDR = (short) 0xFEB9;
    public static final short PADR = (short) 0xFF69;
    public static final short PORTA = (short) 0xFF59;
    public static final short PAPCR = (short) 0xFF70;
    public static final short PAODR = (short) 0xFF77;
    public static final short PBDDR = (short) 0xFEBA;
    public static final short PBDR = (short) 0xFF6A;
    public static final short PORTB = (short) 0xFF5A;
    public static final short PBPCR = (short) 0xFF71;
    public static final short PCDDR = (short) 0xFEBB;
    public static final short PCDR = (short) 0xFF6B;
    public static final short PORTC = (short) 0xFF5B;
    public static final short PCPCR = (short) 0xFF72;
    public static final short PDDDR = (short) 0xFEBC;
    public static final short PDDR = (short) 0xFF6C;
    public static final short PORTD = (short) 0xFF5C;
    public static final short PDPCR = (short) 0xFF73;
    public static final short PEDDR = (short) 0xFEBD;
    public static final short PEDR = (short) 0xFF6D;
    public static final short PORTE = (short) 0xFF5D;
    public static final short PEPCR = (short) 0xFF74;
    public static final short PFDDR = (short) 0xFEBE;
    public static final short PFDR = (short) 0xFF6E;
    public static final short PORTF = (short) 0xFF5E;
    public static final short PGDDR = (short) 0xFEBF;
    public static final short PGDR = (short) 0xFF6F;
    public static final short PORTG = (short) 0xFF5F;

    public H8SIOPorts(int id, int mcu) {
        super(id, "I/O Ports", DeviceType.INTERFACE);

        add(reg(P1DDR, "P1DDR", P1DDR, new IntegerFieldFormat("P17DDR", 7), new IntegerFieldFormat("P16DDR", 6), new IntegerFieldFormat("P15DDR", 5), new IntegerFieldFormat("P14DDR", 4),
                        new IntegerFieldFormat("P13DDR", 3), new IntegerFieldFormat("P12DDR", 2), new IntegerFieldFormat("P11DDR", 1), new IntegerFieldFormat("P10DDR", 0)));
        add(reg(P1DR, "P1DR", P1DR, new IntegerFieldFormat("P17DR", 7), new IntegerFieldFormat("P16DR", 6), new IntegerFieldFormat("P15DR", 5), new IntegerFieldFormat("P14DR", 4),
                        new IntegerFieldFormat("P13DR", 3), new IntegerFieldFormat("P12DR", 2), new IntegerFieldFormat("P11DR", 1), new IntegerFieldFormat("P10DR", 0)));
        add(reg(PORT1, "PORT1", PORT1, new IntegerFieldFormat("P17", 7), new IntegerFieldFormat("P16", 6), new IntegerFieldFormat("P15", 5), new IntegerFieldFormat("P14", 4),
                        new IntegerFieldFormat("P13", 3), new IntegerFieldFormat("P12", 2), new IntegerFieldFormat("P11", 1), new IntegerFieldFormat("P10", 0)));

        add(reg(P2DDR, "P2DDR", P2DDR, new IntegerFieldFormat("P27DDR", 7), new IntegerFieldFormat("P26DDR", 6), new IntegerFieldFormat("P25DDR", 5), new IntegerFieldFormat("P24DDR", 4),
                        new IntegerFieldFormat("P23DDR", 3), new IntegerFieldFormat("P22DDR", 2), new IntegerFieldFormat("P21DDR", 1), new IntegerFieldFormat("P20DDR", 0)));
        add(reg(P2DR, "P2DR", P2DR, new IntegerFieldFormat("P27DR", 7), new IntegerFieldFormat("P26DR", 6), new IntegerFieldFormat("P25DR", 5), new IntegerFieldFormat("P24DR", 4),
                        new IntegerFieldFormat("P23DR", 3), new IntegerFieldFormat("P22DR", 2), new IntegerFieldFormat("P21DR", 1), new IntegerFieldFormat("P20DR", 0)));
        add(reg(PORT2, "PORT2", PORT2, new IntegerFieldFormat("P27", 7), new IntegerFieldFormat("P26", 6), new IntegerFieldFormat("P25", 5), new IntegerFieldFormat("P24", 4),
                        new IntegerFieldFormat("P23", 3), new IntegerFieldFormat("P22", 2), new IntegerFieldFormat("P21", 1), new IntegerFieldFormat("P20", 0)));

        add(reg(P3DDR, "P3DDR", P3DDR, new IntegerFieldFormat("P35DDR", 5), new IntegerFieldFormat("P34DDR", 4), new IntegerFieldFormat("P33DDR", 3), new IntegerFieldFormat("P32DDR", 2),
                        new IntegerFieldFormat("P31DDR", 1), new IntegerFieldFormat("P30DDR", 0)));
        add(reg(P3DR, "P3DR", P3DR, new IntegerFieldFormat("P35DR", 5), new IntegerFieldFormat("P34DR", 4), new IntegerFieldFormat("P33DR", 3), new IntegerFieldFormat("P32DR", 2),
                        new IntegerFieldFormat("P31DR", 1), new IntegerFieldFormat("P30DR", 0)));
        add(reg(PORT3, "PORT3", PORT3, new IntegerFieldFormat("P35", 5), new IntegerFieldFormat("P34", 4), new IntegerFieldFormat("P33", 3), new IntegerFieldFormat("P32", 2),
                        new IntegerFieldFormat("P31", 1), new IntegerFieldFormat("P30", 0)));
        add(reg(P3ODR, "P3ODR", P3ODR, new IntegerFieldFormat("P35ODR", 5), new IntegerFieldFormat("P34ODR", 4), new IntegerFieldFormat("P33ODR", 3), new IntegerFieldFormat("P32ODR", 2),
                        new IntegerFieldFormat("P31ODR", 1), new IntegerFieldFormat("P30ODR", 0)));

        add(reg(PORT4, "PORT4", PORT4, new IntegerFieldFormat("P17", 7), new IntegerFieldFormat("P16", 6), new IntegerFieldFormat("P15", 5), new IntegerFieldFormat("P14", 4),
                        new IntegerFieldFormat("P13", 3), new IntegerFieldFormat("P12", 2), new IntegerFieldFormat("P11", 1), new IntegerFieldFormat("P10", 0)));

        add(reg(P5DDR, "P5DDR", P5DDR, new IntegerFieldFormat("P53DDR", 3), new IntegerFieldFormat("P52DDR", 2), new IntegerFieldFormat("P51DDR", 1), new IntegerFieldFormat("P50DDR", 0)));
        add(reg(P5DR, "P5DR", P5DR, new IntegerFieldFormat("P53DR", 3), new IntegerFieldFormat("P52DR", 2), new IntegerFieldFormat("P51DR", 1), new IntegerFieldFormat("P50DR", 0)));
        add(reg(PORT5, "PORT5", PORT5, new IntegerFieldFormat("P53", 3), new IntegerFieldFormat("P52", 2), new IntegerFieldFormat("P51", 1), new IntegerFieldFormat("P50", 0)));

        add(reg(P6DDR, "P6DDR", P6DDR, new IntegerFieldFormat("P67DDR", 7), new IntegerFieldFormat("P66DDR", 6), new IntegerFieldFormat("P65DDR", 5), new IntegerFieldFormat("P64DDR", 4),
                        new IntegerFieldFormat("P63DDR", 3), new IntegerFieldFormat("P62DDR", 2), new IntegerFieldFormat("P61DDR", 1), new IntegerFieldFormat("P60DDR", 0)));
        add(reg(P6DR, "P6DR", P6DR, new IntegerFieldFormat("P67DR", 7), new IntegerFieldFormat("P66DR", 6), new IntegerFieldFormat("P65DR", 5), new IntegerFieldFormat("P64DR", 4),
                        new IntegerFieldFormat("P63DR", 3), new IntegerFieldFormat("P62DR", 2), new IntegerFieldFormat("P61DR", 1), new IntegerFieldFormat("P60DR", 0)));
        add(reg(PORT6, "PORT6", PORT6, new IntegerFieldFormat("P67", 7), new IntegerFieldFormat("P66", 6), new IntegerFieldFormat("P65", 5), new IntegerFieldFormat("P64", 4),
                        new IntegerFieldFormat("P63", 3), new IntegerFieldFormat("P62", 2), new IntegerFieldFormat("P61", 1), new IntegerFieldFormat("P60", 0)));

        add(reg(PADDR, "PADDR", PADDR, new IntegerFieldFormat("PA7DDR", 7), new IntegerFieldFormat("PA6DDR", 6), new IntegerFieldFormat("PA5DDR", 5), new IntegerFieldFormat("PA4DDR", 4),
                        new IntegerFieldFormat("PA3DDR", 3), new IntegerFieldFormat("PA2DDR", 2), new IntegerFieldFormat("PA1DDR", 1), new IntegerFieldFormat("PA0DDR", 0)));
        add(reg(PADR, "PADR", PADR, new IntegerFieldFormat("PA7DR", 7), new IntegerFieldFormat("PA6DR", 6), new IntegerFieldFormat("PA5DR", 5), new IntegerFieldFormat("PA4DR", 4),
                        new IntegerFieldFormat("PA3DR", 3), new IntegerFieldFormat("PA2DR", 2), new IntegerFieldFormat("PA1DR", 1), new IntegerFieldFormat("PA0DR", 0)));
        add(reg(PORTA, "PORTA", PORTA, new IntegerFieldFormat("PA7", 7), new IntegerFieldFormat("PA6", 6), new IntegerFieldFormat("PA5", 5), new IntegerFieldFormat("PA4", 4),
                        new IntegerFieldFormat("PA3", 3), new IntegerFieldFormat("PA2", 2), new IntegerFieldFormat("PA1", 1), new IntegerFieldFormat("PA0", 0)));
        if (mcu == 2351) {
            add(reg(PAPCR, "PAPCR", PAPCR, new IntegerFieldFormat("PA7PCR", 7), new IntegerFieldFormat("PA6PCR", 6), new IntegerFieldFormat("PA5PCR", 5), new IntegerFieldFormat("PA4PCR", 4),
                            new IntegerFieldFormat("PA3PCR", 3), new IntegerFieldFormat("PA2PCR", 2), new IntegerFieldFormat("PA1PCR", 1), new IntegerFieldFormat("PA0PCR", 0)));
            add(reg(PAODR, "PAODR", PAODR, new IntegerFieldFormat("PPA7ODR", 7), new IntegerFieldFormat("PA6ODR", 6), new IntegerFieldFormat("PA5ODR", 5), new IntegerFieldFormat("PA4ODR", 4),
                            new IntegerFieldFormat("PA3ODR", 3), new IntegerFieldFormat("PA2ODR", 2), new IntegerFieldFormat("PA1ODR", 1), new IntegerFieldFormat("PA0ODR", 0)));
        }

        if (mcu == 2351) {
            add(reg(PBDDR, "PBDDR", PBDDR, new IntegerFieldFormat("PB7DDR", 7), new IntegerFieldFormat("PB6DDR", 6), new IntegerFieldFormat("PB5DDR", 5), new IntegerFieldFormat("PB4DDR", 4),
                            new IntegerFieldFormat("PB3DDR", 3), new IntegerFieldFormat("PB2DDR", 2), new IntegerFieldFormat("PB1DDR", 1), new IntegerFieldFormat("PB0DDR", 0)));
            add(reg(PBDR, "PBDR", PBDR, new IntegerFieldFormat("PB7DR", 7), new IntegerFieldFormat("PB6DR", 6), new IntegerFieldFormat("PB5DR", 5), new IntegerFieldFormat("PB4DR", 4),
                            new IntegerFieldFormat("PB3DR", 3), new IntegerFieldFormat("PB2DR", 2), new IntegerFieldFormat("PB1DR", 1), new IntegerFieldFormat("PB0DR", 0)));
            add(reg(PORTB, "PORTB", PORTB, new IntegerFieldFormat("PB7", 7), new IntegerFieldFormat("PB6", 6), new IntegerFieldFormat("PB5", 5), new IntegerFieldFormat("PB4", 4),
                            new IntegerFieldFormat("PB3", 3), new IntegerFieldFormat("PB2", 2), new IntegerFieldFormat("PB1", 1), new IntegerFieldFormat("PB0", 0)));
            add(reg(PBPCR, "PBPCR", PBPCR, new IntegerFieldFormat("PB7PCR", 7), new IntegerFieldFormat("PB6PCR", 6), new IntegerFieldFormat("PB5PCR", 5), new IntegerFieldFormat("PB4PCR", 4),
                            new IntegerFieldFormat("PB3PCR", 3), new IntegerFieldFormat("PB2PCR", 2), new IntegerFieldFormat("PB1PCR", 1), new IntegerFieldFormat("PB0PCR", 0)));
        }

        if (mcu == 2351) {
            add(reg(PCDDR, "PCDDR", PCDDR, new IntegerFieldFormat("PC7DDR", 7), new IntegerFieldFormat("PC6DDR", 6), new IntegerFieldFormat("PC5DDR", 5), new IntegerFieldFormat("PC4DDR", 4),
                            new IntegerFieldFormat("PC3DDR", 3), new IntegerFieldFormat("PC2DDR", 2), new IntegerFieldFormat("PC1DDR", 1), new IntegerFieldFormat("PC0DDR", 0)));
            add(reg(PCDR, "PCDR", PCDR, new IntegerFieldFormat("PC7DR", 7), new IntegerFieldFormat("PC6DR", 6), new IntegerFieldFormat("PC5DR", 5), new IntegerFieldFormat("PC4DR", 4),
                            new IntegerFieldFormat("PC3DR", 3), new IntegerFieldFormat("PC2DR", 2), new IntegerFieldFormat("PC1DR", 1), new IntegerFieldFormat("PC0DR", 0)));
            add(reg(PORTC, "PORTC", PORTC, new IntegerFieldFormat("PC7", 7), new IntegerFieldFormat("PC6", 6), new IntegerFieldFormat("PC5", 5), new IntegerFieldFormat("PC4", 4),
                            new IntegerFieldFormat("PC3", 3), new IntegerFieldFormat("PC2", 2), new IntegerFieldFormat("PC1", 1), new IntegerFieldFormat("PC0", 0)));
            add(reg(PCPCR, "PCPCR", PCPCR, new IntegerFieldFormat("PC7PCR", 7), new IntegerFieldFormat("PC6PCR", 6), new IntegerFieldFormat("PC5PCR", 5), new IntegerFieldFormat("PC4PCR", 4),
                            new IntegerFieldFormat("PC3PCR", 3), new IntegerFieldFormat("PC2PCR", 2), new IntegerFieldFormat("PC1PCR", 1), new IntegerFieldFormat("PC0PCR", 0)));
        }

        if (mcu == 2351) {
            add(reg(PDDDR, "PDDDR", PDDDR, new IntegerFieldFormat("PD7DDR", 7), new IntegerFieldFormat("PD6DDR", 6), new IntegerFieldFormat("PD5DDR", 5), new IntegerFieldFormat("PD4DDR", 4),
                            new IntegerFieldFormat("PD3DDR", 3), new IntegerFieldFormat("PD2DDR", 2), new IntegerFieldFormat("PD1DDR", 1), new IntegerFieldFormat("PD0DDR", 0)));
            add(reg(PDDR, "PDDR", PDDR, new IntegerFieldFormat("PD7DR", 7), new IntegerFieldFormat("PD6DR", 6), new IntegerFieldFormat("PD5DR", 5), new IntegerFieldFormat("PD4DR", 4),
                            new IntegerFieldFormat("PD3DR", 3), new IntegerFieldFormat("PD2DR", 2), new IntegerFieldFormat("PD1DR", 1), new IntegerFieldFormat("PD0DR", 0)));
            add(reg(PORTD, "PORTD", PORTD, new IntegerFieldFormat("PD7", 7), new IntegerFieldFormat("PD6", 6), new IntegerFieldFormat("PD5", 5), new IntegerFieldFormat("PD4", 4),
                            new IntegerFieldFormat("PD3", 3), new IntegerFieldFormat("PD2", 2), new IntegerFieldFormat("PD1", 1), new IntegerFieldFormat("PD0", 0)));
            add(reg(PDPCR, "PDPCR", PDPCR, new IntegerFieldFormat("PD7PCR", 7), new IntegerFieldFormat("PD6PCR", 6), new IntegerFieldFormat("PD5PCR", 5), new IntegerFieldFormat("PD4PCR", 4),
                            new IntegerFieldFormat("PD3PCR", 3), new IntegerFieldFormat("PD2PCR", 2), new IntegerFieldFormat("PD1PCR", 1), new IntegerFieldFormat("PD0PCR", 0)));
        }

        add(reg(PEDDR, "PEDDR", PEDDR, new IntegerFieldFormat("PE7DDR", 7), new IntegerFieldFormat("PE6DDR", 6), new IntegerFieldFormat("PE5DDR", 5), new IntegerFieldFormat("PE4DDR", 4),
                        new IntegerFieldFormat("PE3DDR", 3), new IntegerFieldFormat("PE2DDR", 2), new IntegerFieldFormat("PE1DDR", 1), new IntegerFieldFormat("PE0DDR", 0)));
        add(reg(PEDR, "PEDR", PEDR, new IntegerFieldFormat("PE7DR", 7), new IntegerFieldFormat("PE6DR", 6), new IntegerFieldFormat("PE5DR", 5), new IntegerFieldFormat("PE4DR", 4),
                        new IntegerFieldFormat("PE3DR", 3), new IntegerFieldFormat("PE2DR", 2), new IntegerFieldFormat("PE1DR", 1), new IntegerFieldFormat("PE0DR", 0)));
        add(reg(PORTE, "PORTE", PORTE, new IntegerFieldFormat("PE7", 7), new IntegerFieldFormat("PE6", 6), new IntegerFieldFormat("PE5", 5), new IntegerFieldFormat("PE4", 4),
                        new IntegerFieldFormat("PE3", 3), new IntegerFieldFormat("PE2", 2), new IntegerFieldFormat("PE1", 1), new IntegerFieldFormat("PE0", 0)));
        if (mcu == 2351) {
            add(reg(PEPCR, "PEPCR", PEPCR, new IntegerFieldFormat("PE7PCR", 7), new IntegerFieldFormat("PE6PCR", 6), new IntegerFieldFormat("PE5PCR", 5), new IntegerFieldFormat("PE4PCR", 4),
                            new IntegerFieldFormat("PE3PCR", 3), new IntegerFieldFormat("PE2PCR", 2), new IntegerFieldFormat("PE1PCR", 1), new IntegerFieldFormat("PE0PCR", 0)));
        }

        add(reg(PFDDR, "PFDDR", PFDDR, new IntegerFieldFormat("PF7DDR", 7), new IntegerFieldFormat("PF6DDR", 6), new IntegerFieldFormat("PF5DDR", 5), new IntegerFieldFormat("PF4DDR", 4),
                        new IntegerFieldFormat("PF3DDR", 3), new IntegerFieldFormat("PF2DDR", 2), new IntegerFieldFormat("PF1DDR", 1), new IntegerFieldFormat("PF0DDR", 0)));
        add(reg(PFDR, "PFDR", PFDR, new IntegerFieldFormat("PF7DR", 7), new IntegerFieldFormat("PF6DR", 6), new IntegerFieldFormat("PF5DR", 5), new IntegerFieldFormat("PF4DR", 4),
                        new IntegerFieldFormat("PF3DR", 3), new IntegerFieldFormat("PF2DR", 2), new IntegerFieldFormat("PF1DR", 1), new IntegerFieldFormat("PF0DR", 0)));
        add(reg(PORTF, "PORTF", PORTF, new IntegerFieldFormat("PF7", 7), new IntegerFieldFormat("PF6", 6), new IntegerFieldFormat("PF5", 5), new IntegerFieldFormat("PF4", 4),
                        new IntegerFieldFormat("PF3", 3), new IntegerFieldFormat("PF2", 2), new IntegerFieldFormat("PF1", 1), new IntegerFieldFormat("PF0", 0)));

        add(reg(PGDDR, "PGDDR", PGDDR, new IntegerFieldFormat("PG4DDR", 4), new IntegerFieldFormat("PG3DDR", 3), new IntegerFieldFormat("PG2DDR", 2), new IntegerFieldFormat("PG1DDR", 1),
                        new IntegerFieldFormat("PG0DDR", 0)));
        add(reg(PGDR, "PGDR", PGDR, new IntegerFieldFormat("PG4DR", 4), new IntegerFieldFormat("PG3DR", 3), new IntegerFieldFormat("PG2DR", 2), new IntegerFieldFormat("PG1DR", 1),
                        new IntegerFieldFormat("PG0DR", 0)));
        add(reg(PORTG, "PORTG", PORTG, new IntegerFieldFormat("PG4", 4), new IntegerFieldFormat("PG3", 3), new IntegerFieldFormat("PG2", 2), new IntegerFieldFormat("PG1", 1),
                        new IntegerFieldFormat("PG0", 0)));
    }
}
