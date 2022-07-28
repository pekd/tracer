package org.graalvm.vm.trcview.arch.riscv.disasm;

import org.graalvm.vm.util.HexFormatter;

public class CSR {
    // unprivileged floating-point CSRs
    public static final int CSR_fflags = 0x001;
    public static final int CSR_frm = 0x002;
    public static final int CSR_fcsr = 0x003;

    // unprivileged counter/timers
    public static final int CSR_cycle = 0xC00;
    public static final int CSR_time = 0xC01;
    public static final int CSR_instret = 0xC02;
    public static final int CSR_hpmcounter3 = 0xC03;
    public static final int CSR_hpmcounter4 = 0xC04;
    public static final int CSR_hpmcounter5 = 0xC05;
    public static final int CSR_hpmcounter6 = 0xC06;
    public static final int CSR_hpmcounter7 = 0xC07;
    public static final int CSR_hpmcounter8 = 0xC08;
    public static final int CSR_hpmcounter9 = 0xC09;
    public static final int CSR_hpmcounter10 = 0xC0A;
    public static final int CSR_hpmcounter11 = 0xC0B;
    public static final int CSR_hpmcounter12 = 0xC0C;
    public static final int CSR_hpmcounter13 = 0xC0D;
    public static final int CSR_hpmcounter14 = 0xC0E;
    public static final int CSR_hpmcounter15 = 0xC0F;
    public static final int CSR_hpmcounter16 = 0xC10;
    public static final int CSR_hpmcounter17 = 0xC11;
    public static final int CSR_hpmcounter18 = 0xC12;
    public static final int CSR_hpmcounter19 = 0xC13;
    public static final int CSR_hpmcounter20 = 0xC14;
    public static final int CSR_hpmcounter21 = 0xC15;
    public static final int CSR_hpmcounter22 = 0xC16;
    public static final int CSR_hpmcounter23 = 0xC17;
    public static final int CSR_hpmcounter24 = 0xC18;
    public static final int CSR_hpmcounter25 = 0xC19;
    public static final int CSR_hpmcounter26 = 0xC1A;
    public static final int CSR_hpmcounter27 = 0xC1B;
    public static final int CSR_hpmcounter28 = 0xC1C;
    public static final int CSR_hpmcounter29 = 0xC1D;
    public static final int CSR_hpmcounter30 = 0xC1E;
    public static final int CSR_hpmcounter31 = 0xC1F;

    public static final int CSR_cycleh = 0xC80;
    public static final int CSR_timeh = 0xC81;
    public static final int CSR_instreth = 0xC82;
    public static final int CSR_hpmcounter3h = 0xC83;
    public static final int CSR_hpmcounter4h = 0xC84;
    public static final int CSR_hpmcounter5h = 0xC85;
    public static final int CSR_hpmcounter6h = 0xC86;
    public static final int CSR_hpmcounter7h = 0xC87;
    public static final int CSR_hpmcounter8h = 0xC88;
    public static final int CSR_hpmcounter9h = 0xC89;
    public static final int CSR_hpmcounter10h = 0xC8A;
    public static final int CSR_hpmcounter11h = 0xC8B;
    public static final int CSR_hpmcounter12h = 0xC8C;
    public static final int CSR_hpmcounter13h = 0xC8D;
    public static final int CSR_hpmcounter14h = 0xC8E;
    public static final int CSR_hpmcounter15h = 0xC8F;
    public static final int CSR_hpmcounter16h = 0xC90;
    public static final int CSR_hpmcounter17h = 0xC91;
    public static final int CSR_hpmcounter18h = 0xC92;
    public static final int CSR_hpmcounter19h = 0xC93;
    public static final int CSR_hpmcounter20h = 0xC94;
    public static final int CSR_hpmcounter21h = 0xC95;
    public static final int CSR_hpmcounter22h = 0xC96;
    public static final int CSR_hpmcounter23h = 0xC97;
    public static final int CSR_hpmcounter24h = 0xC98;
    public static final int CSR_hpmcounter25h = 0xC99;
    public static final int CSR_hpmcounter26h = 0xC9A;
    public static final int CSR_hpmcounter27h = 0xC9B;
    public static final int CSR_hpmcounter28h = 0xC9C;
    public static final int CSR_hpmcounter29h = 0xC9D;
    public static final int CSR_hpmcounter30h = 0xC9E;
    public static final int CSR_hpmcounter31h = 0xC9F;

    // supervisor trap setup
    public static final int CSR_sstatus = 0x100;
    public static final int CSR_sie = 0x104;
    public static final int CSR_stvec = 0x105;
    public static final int CSR_scounteren = 0x106;

    // supervisor configuration
    public static final int CSR_senvcfg = 0x10A;

    // supervisor trap handling
    public static final int CSR_sscratch = 0x140;
    public static final int CSR_sepc = 0x141;
    public static final int CSR_scause = 0x142;
    public static final int CSR_stval = 0x143;
    public static final int CSR_sip = 0x144;

    // supervisor protection and translation
    public static final int CSR_satp = 0x180;

    // debug/trace registers
    public static final int CSR_scontext = 0x5A8;

    // hypervisor trap setup
    public static final int CSR_hstatus = 0x600;
    public static final int CSR_hedeleg = 0x602;
    public static final int CSR_hideleg = 0x603;
    public static final int CSR_hie = 0x604;
    public static final int CSR_hcounteren = 0x606;
    public static final int CSR_hgeie = 0x607;

    // hypervisor trap handling
    public static final int CSR_htval = 0x643;
    public static final int CSR_hip = 0x644;
    public static final int CSR_hvip = 0x645;
    public static final int CSR_htinst = 0x64A;
    public static final int CSR_hgeip = 0xE12;

    // hypervior configuration
    public static final int CSR_henvcfg = 0x60A;
    public static final int CSR_henvcfgh = 0x61A;

    // hypervisor protection and translation
    public static final int CSR_hgatp = 0x680;

    // hypervisor debug/trace registers
    public static final int CSR_hcontext = 0x6A8;

    // hypervisor counter/timer virtualization registers
    public static final int CSR_htimedelta = 0x605;
    public static final int CSR_htimedeltah = 0x615;

    // virtual supervisor registers
    public static final int CSR_vsstatus = 0x200;
    public static final int CSR_vsie = 0x204;
    public static final int CSR_vstvec = 0x205;
    public static final int CSR_vsscratch = 0x240;
    public static final int CSR_vsepc = 0x241;
    public static final int CSR_vscause = 0x242;
    public static final int CSR_vstval = 0x243;
    public static final int CSR_vsip = 0x244;
    public static final int CSR_vsatp = 0x280;

    // machine information registers
    public static final int CSR_mvendorid = 0xF11;
    public static final int CSR_marchid = 0xF12;
    public static final int CSR_mimpid = 0xF13;
    public static final int CSR_mhartid = 0xF14;
    public static final int CSR_mconfigptr = 0xF15;

    // machine trap setup
    public static final int CSR_mstatus = 0x300;
    public static final int CSR_misa = 0x301;
    public static final int CSR_medeleg = 0x302;
    public static final int CSR_mideleg = 0x303;
    public static final int CSR_mie = 0x304;
    public static final int CSR_mtvec = 0x305;
    public static final int CSR_mcounteren = 0x306;
    public static final int CSR_mstatush = 0x310;

    // machine trap handling
    public static final int CSR_mscratch = 0x340;
    public static final int CSR_mepc = 0x341;
    public static final int CSR_mcause = 0x342;
    public static final int CSR_mtval = 0x343;
    public static final int CSR_mip = 0x344;
    public static final int CSR_mtinst = 0x34A;
    public static final int CSR_mtval2 = 0x34B;

    // machine configuration
    public static final int CSR_menvcfg = 0x30A;
    public static final int CSR_menvcfgh = 0x31A;
    public static final int CSR_mseccfg = 0x747;
    public static final int CSR_mseccfgh = 0x757;

    // machine memory protection
    public static final int CSR_pmpcfg0 = 0x3A0;
    public static final int CSR_pmpcfg1 = 0x3A1;
    public static final int CSR_pmpcfg2 = 0x3A2;
    public static final int CSR_pmpcfg3 = 0x3A3;
    public static final int CSR_pmpcfg4 = 0x3A4;
    public static final int CSR_pmpcfg5 = 0x3A5;
    public static final int CSR_pmpcfg6 = 0x3A6;
    public static final int CSR_pmpcfg7 = 0x3A7;
    public static final int CSR_pmpcfg8 = 0x3A8;
    public static final int CSR_pmpcfg9 = 0x3A9;
    public static final int CSR_pmpcfg10 = 0x3AA;
    public static final int CSR_pmpcfg11 = 0x3AB;
    public static final int CSR_pmpcfg12 = 0x3AC;
    public static final int CSR_pmpcfg13 = 0x3AD;
    public static final int CSR_pmpcfg14 = 0x3AE;
    public static final int CSR_pmpcfg15 = 0x3AF;
    public static final int CSR_pmpaddr0 = 0x3B0;
    public static final int CSR_pmpaddr1 = 0x3B1;
    public static final int CSR_pmpaddr2 = 0x3B2;
    public static final int CSR_pmpaddr3 = 0x3B3;
    public static final int CSR_pmpaddr4 = 0x3B4;
    public static final int CSR_pmpaddr5 = 0x3B5;
    public static final int CSR_pmpaddr6 = 0x3B6;
    public static final int CSR_pmpaddr7 = 0x3B7;
    public static final int CSR_pmpaddr8 = 0x3B8;
    public static final int CSR_pmpaddr9 = 0x3B9;
    public static final int CSR_pmpaddr10 = 0x3BA;
    public static final int CSR_pmpaddr11 = 0x3BB;
    public static final int CSR_pmpaddr12 = 0x3BC;
    public static final int CSR_pmpaddr13 = 0x3BD;
    public static final int CSR_pmpaddr14 = 0x3BE;
    public static final int CSR_pmpaddr15 = 0x3BF;
    public static final int CSR_pmpaddr16 = 0x3C0;
    public static final int CSR_pmpaddr17 = 0x3C1;
    public static final int CSR_pmpaddr18 = 0x3C2;
    public static final int CSR_pmpaddr19 = 0x3C3;
    public static final int CSR_pmpaddr20 = 0x3C4;
    public static final int CSR_pmpaddr21 = 0x3C5;
    public static final int CSR_pmpaddr22 = 0x3C6;
    public static final int CSR_pmpaddr23 = 0x3C7;
    public static final int CSR_pmpaddr24 = 0x3C8;
    public static final int CSR_pmpaddr25 = 0x3C9;
    public static final int CSR_pmpaddr26 = 0x3CA;
    public static final int CSR_pmpaddr27 = 0x3CB;
    public static final int CSR_pmpaddr28 = 0x3CC;
    public static final int CSR_pmpaddr29 = 0x3CD;
    public static final int CSR_pmpaddr30 = 0x3CE;
    public static final int CSR_pmpaddr31 = 0x3CF;
    public static final int CSR_pmpaddr32 = 0x3D0;
    public static final int CSR_pmpaddr33 = 0x3D1;
    public static final int CSR_pmpaddr34 = 0x3D2;
    public static final int CSR_pmpaddr35 = 0x3D3;
    public static final int CSR_pmpaddr36 = 0x3D4;
    public static final int CSR_pmpaddr37 = 0x3D5;
    public static final int CSR_pmpaddr38 = 0x3D6;
    public static final int CSR_pmpaddr39 = 0x3D7;
    public static final int CSR_pmpaddr40 = 0x3D8;
    public static final int CSR_pmpaddr41 = 0x3D9;
    public static final int CSR_pmpaddr42 = 0x3DA;
    public static final int CSR_pmpaddr43 = 0x3DB;
    public static final int CSR_pmpaddr44 = 0x3DC;
    public static final int CSR_pmpaddr45 = 0x3DD;
    public static final int CSR_pmpaddr46 = 0x3DE;
    public static final int CSR_pmpaddr47 = 0x3DF;
    public static final int CSR_pmpaddr48 = 0x3E0;
    public static final int CSR_pmpaddr49 = 0x3E1;
    public static final int CSR_pmpaddr50 = 0x3E2;
    public static final int CSR_pmpaddr51 = 0x3E3;
    public static final int CSR_pmpaddr52 = 0x3E4;
    public static final int CSR_pmpaddr53 = 0x3E5;
    public static final int CSR_pmpaddr54 = 0x3E6;
    public static final int CSR_pmpaddr55 = 0x3E7;
    public static final int CSR_pmpaddr56 = 0x3E8;
    public static final int CSR_pmpaddr57 = 0x3E9;
    public static final int CSR_pmpaddr58 = 0x3EA;
    public static final int CSR_pmpaddr59 = 0x3EB;
    public static final int CSR_pmpaddr60 = 0x3EC;
    public static final int CSR_pmpaddr61 = 0x3ED;
    public static final int CSR_pmpaddr62 = 0x3EE;
    public static final int CSR_pmpaddr63 = 0x3EF;

    // machine counters/timers
    public static final int CSR_mcycle = 0xB00;
    public static final int CSR_minstret = 0xB02;
    public static final int CSR_mhpmcounter3 = 0xB03;
    public static final int CSR_mhpmcounter4 = 0xB04;
    public static final int CSR_mhpmcounter5 = 0xB05;
    public static final int CSR_mhpmcounter6 = 0xB06;
    public static final int CSR_mhpmcounter7 = 0xB07;
    public static final int CSR_mhpmcounter8 = 0xB08;
    public static final int CSR_mhpmcounter9 = 0xB09;
    public static final int CSR_mhpmcounter10 = 0xB0A;
    public static final int CSR_mhpmcounter11 = 0xB0B;
    public static final int CSR_mhpmcounter12 = 0xB0C;
    public static final int CSR_mhpmcounter13 = 0xB0D;
    public static final int CSR_mhpmcounter14 = 0xB0E;
    public static final int CSR_mhpmcounter15 = 0xB0F;
    public static final int CSR_mhpmcounter16 = 0xB10;
    public static final int CSR_mhpmcounter17 = 0xB11;
    public static final int CSR_mhpmcounter18 = 0xB12;
    public static final int CSR_mhpmcounter19 = 0xB13;
    public static final int CSR_mhpmcounter20 = 0xB14;
    public static final int CSR_mhpmcounter21 = 0xB15;
    public static final int CSR_mhpmcounter22 = 0xB16;
    public static final int CSR_mhpmcounter23 = 0xB17;
    public static final int CSR_mhpmcounter24 = 0xB18;
    public static final int CSR_mhpmcounter25 = 0xB19;
    public static final int CSR_mhpmcounter26 = 0xB1A;
    public static final int CSR_mhpmcounter27 = 0xB1B;
    public static final int CSR_mhpmcounter28 = 0xB1C;
    public static final int CSR_mhpmcounter29 = 0xB1D;
    public static final int CSR_mhpmcounter30 = 0xB1E;
    public static final int CSR_mhpmcounter31 = 0xB1F;

    public static final int CSR_mcycleh = 0xB80;
    public static final int CSR_minstreth = 0xB82;
    public static final int CSR_mhpmcounter3h = 0xB83;
    public static final int CSR_mhpmcounter4h = 0xB84;
    public static final int CSR_mhpmcounter5h = 0xB85;
    public static final int CSR_mhpmcounter6h = 0xB86;
    public static final int CSR_mhpmcounter7h = 0xB87;
    public static final int CSR_mhpmcounter8h = 0xB88;
    public static final int CSR_mhpmcounter9h = 0xB89;
    public static final int CSR_mhpmcounter10h = 0xB8A;
    public static final int CSR_mhpmcounter11h = 0xB8B;
    public static final int CSR_mhpmcounter12h = 0xB8C;
    public static final int CSR_mhpmcounter13h = 0xB8D;
    public static final int CSR_mhpmcounter14h = 0xB8E;
    public static final int CSR_mhpmcounter15h = 0xB8F;
    public static final int CSR_mhpmcounter16h = 0xB90;
    public static final int CSR_mhpmcounter17h = 0xB91;
    public static final int CSR_mhpmcounter18h = 0xB92;
    public static final int CSR_mhpmcounter19h = 0xB93;
    public static final int CSR_mhpmcounter20h = 0xB94;
    public static final int CSR_mhpmcounter21h = 0xB95;
    public static final int CSR_mhpmcounter22h = 0xB96;
    public static final int CSR_mhpmcounter23h = 0xB97;
    public static final int CSR_mhpmcounter24h = 0xB98;
    public static final int CSR_mhpmcounter25h = 0xB99;
    public static final int CSR_mhpmcounter26h = 0xB9A;
    public static final int CSR_mhpmcounter27h = 0xB9B;
    public static final int CSR_mhpmcounter28h = 0xB9C;
    public static final int CSR_mhpmcounter29h = 0xB9D;
    public static final int CSR_mhpmcounter30h = 0xB9E;
    public static final int CSR_mhpmcounter31h = 0xB9F;

    // machine counter setup
    public static final int CSR_mcountinhibit = 0x320;
    public static final int CSR_mhpmevent3 = 0x323;
    public static final int CSR_mhpmevent4 = 0x324;
    public static final int CSR_mhpmevent5 = 0x325;
    public static final int CSR_mhpmevent6 = 0x326;
    public static final int CSR_mhpmevent7 = 0x327;
    public static final int CSR_mhpmevent8 = 0x328;
    public static final int CSR_mhpmevent9 = 0x329;
    public static final int CSR_mhpmevent10 = 0x32A;
    public static final int CSR_mhpmevent11 = 0x32B;
    public static final int CSR_mhpmevent12 = 0x32C;
    public static final int CSR_mhpmevent13 = 0x32D;
    public static final int CSR_mhpmevent14 = 0x32E;
    public static final int CSR_mhpmevent15 = 0x32F;
    public static final int CSR_mhpmevent16 = 0x330;
    public static final int CSR_mhpmevent17 = 0x331;
    public static final int CSR_mhpmevent18 = 0x332;
    public static final int CSR_mhpmevent19 = 0x333;
    public static final int CSR_mhpmevent20 = 0x334;
    public static final int CSR_mhpmevent21 = 0x335;
    public static final int CSR_mhpmevent22 = 0x336;
    public static final int CSR_mhpmevent23 = 0x337;
    public static final int CSR_mhpmevent24 = 0x338;
    public static final int CSR_mhpmevent25 = 0x339;
    public static final int CSR_mhpmevent26 = 0x33A;
    public static final int CSR_mhpmevent27 = 0x33B;
    public static final int CSR_mhpmevent28 = 0x33C;
    public static final int CSR_mhpmevent29 = 0x33D;
    public static final int CSR_mhpmevent30 = 0x33E;
    public static final int CSR_mhpmevent31 = 0x33F;

    // debug/trace registers (shared with debug mode)
    public static final int CSR_tselect = 0x7A0;
    public static final int CSR_tdata1 = 0x7A1;
    public static final int CSR_tdata2 = 0x7A2;
    public static final int CSR_tdata3 = 0x7A3;
    public static final int CSR_tcontext = 0x7A8;

    // debug mode registers
    public static final int CSR_dcsr = 0x7B0;
    public static final int CSR_dpc = 0x7B1;
    public static final int CSR_dscratch0 = 0x7B2;
    public static final int CSR_dscratch1 = 0x7B3;

    public static String getName(int csr) {
        switch (csr) {
            case CSR_fflags:
                return "fflags";
            case CSR_frm:
                return "frm";
            case CSR_fcsr:
                return "fcsr";

            case CSR_cycle:
                return "cycle";
            case CSR_time:
                return "time";
            case CSR_instret:
                return "instret";
            case CSR_hpmcounter3:
                return "hpmcounter3";
            case CSR_hpmcounter4:
                return "hpmcounter4";
            case CSR_hpmcounter5:
                return "hpmcounter5";
            case CSR_hpmcounter6:
                return "hpmcounter6";
            case CSR_hpmcounter7:
                return "hpmcounter7";
            case CSR_hpmcounter8:
                return "hpmcounter8";
            case CSR_hpmcounter9:
                return "hpmcounter9";
            case CSR_hpmcounter10:
                return "hpmcounter10";
            case CSR_hpmcounter11:
                return "hpmcounter11";
            case CSR_hpmcounter12:
                return "hpmcounter12";
            case CSR_hpmcounter13:
                return "hpmcounter13";
            case CSR_hpmcounter14:
                return "hpmcounter14";
            case CSR_hpmcounter15:
                return "hpmcounter15";
            case CSR_hpmcounter16:
                return "hpmcounter16";
            case CSR_hpmcounter17:
                return "hpmcounter17";
            case CSR_hpmcounter18:
                return "hpmcounter18";
            case CSR_hpmcounter19:
                return "hpmcounter19";
            case CSR_hpmcounter20:
                return "hpmcounter20";
            case CSR_hpmcounter21:
                return "hpmcounter21";
            case CSR_hpmcounter22:
                return "hpmcounter22";
            case CSR_hpmcounter23:
                return "hpmcounter23";
            case CSR_hpmcounter24:
                return "hpmcounter24";
            case CSR_hpmcounter25:
                return "hpmcounter25";
            case CSR_hpmcounter26:
                return "hpmcounter26";
            case CSR_hpmcounter27:
                return "hpmcounter27";
            case CSR_hpmcounter28:
                return "hpmcounter28";
            case CSR_hpmcounter29:
                return "hpmcounter29";
            case CSR_hpmcounter30:
                return "hpmcounter30";
            case CSR_hpmcounter31:
                return "hpmcounter31";

            case CSR_cycleh:
                return "cycleh";
            case CSR_timeh:
                return "timeh";
            case CSR_instreth:
                return "instreth";
            case CSR_hpmcounter3h:
                return "hpmcounter3h";
            case CSR_hpmcounter4h:
                return "hpmcounter4h";
            case CSR_hpmcounter5h:
                return "hpmcounter5h";
            case CSR_hpmcounter6h:
                return "hpmcounter6h";
            case CSR_hpmcounter7h:
                return "hpmcounter7h";
            case CSR_hpmcounter8h:
                return "hpmcounter8h";
            case CSR_hpmcounter9h:
                return "hpmcounter9h";
            case CSR_hpmcounter10h:
                return "hpmcounter10h";
            case CSR_hpmcounter11h:
                return "hpmcounter11h";
            case CSR_hpmcounter12h:
                return "hpmcounter12h";
            case CSR_hpmcounter13h:
                return "hpmcounter13h";
            case CSR_hpmcounter14h:
                return "hpmcounter14h";
            case CSR_hpmcounter15h:
                return "hpmcounter15h";
            case CSR_hpmcounter16h:
                return "hpmcounter16h";
            case CSR_hpmcounter17h:
                return "hpmcounter17h";
            case CSR_hpmcounter18h:
                return "hpmcounter18h";
            case CSR_hpmcounter19h:
                return "hpmcounter19h";
            case CSR_hpmcounter20h:
                return "hpmcounter20h";
            case CSR_hpmcounter21h:
                return "hpmcounter21h";
            case CSR_hpmcounter22h:
                return "hpmcounter22h";
            case CSR_hpmcounter23h:
                return "hpmcounter23h";
            case CSR_hpmcounter24h:
                return "hpmcounter24h";
            case CSR_hpmcounter25h:
                return "hpmcounter25h";
            case CSR_hpmcounter26h:
                return "hpmcounter26h";
            case CSR_hpmcounter27h:
                return "hpmcounter27h";
            case CSR_hpmcounter28h:
                return "hpmcounter28h";
            case CSR_hpmcounter29h:
                return "hpmcounter29h";
            case CSR_hpmcounter30h:
                return "hpmcounter30h";
            case CSR_hpmcounter31h:
                return "hpmcounter31h";

            case CSR_sstatus:
                return "sstatus";
            case CSR_sie:
                return "sie";
            case CSR_stvec:
                return "stvec";
            case CSR_scounteren:
                return "scounteren";

            case CSR_senvcfg:
                return "senvcfg";

            case CSR_sscratch:
                return "sscratch";
            case CSR_sepc:
                return "sepc";
            case CSR_scause:
                return "scause";
            case CSR_stval:
                return "stval";
            case CSR_sip:
                return "sip";

            case CSR_satp:
                return "satp";

            case CSR_scontext:
                return "scontext";

            case CSR_hstatus:
                return "hstatus";
            case CSR_hedeleg:
                return "hedeleg";
            case CSR_hideleg:
                return "hideleg";
            case CSR_hie:
                return "hie";
            case CSR_hcounteren:
                return "hcounteren";
            case CSR_hgeie:
                return "hgeie";

            case CSR_htval:
                return "htval";
            case CSR_hip:
                return "hip";
            case CSR_hvip:
                return "hvip";
            case CSR_htinst:
                return "htinst";
            case CSR_hgeip:
                return "hgeip";

            case CSR_henvcfg:
                return "henvcfg";
            case CSR_henvcfgh:
                return "henvcfgh";

            case CSR_hgatp:
                return "hgatp";

            case CSR_hcontext:
                return "hcontext";

            case CSR_htimedelta:
                return "htimedelta";
            case CSR_htimedeltah:
                return "htimedeltah";

            case CSR_vsstatus:
                return "vsstatus";
            case CSR_vsie:
                return "vsie";
            case CSR_vstvec:
                return "vstvec";
            case CSR_vsscratch:
                return "vsscratch";
            case CSR_vsepc:
                return "vsepc";
            case CSR_vscause:
                return "vscause";
            case CSR_vstval:
                return "vstval";
            case CSR_vsip:
                return "vsip";
            case CSR_vsatp:
                return "vsatp";

            case CSR_mvendorid:
                return "mvendorid";
            case CSR_marchid:
                return "marchid";
            case CSR_mimpid:
                return "mimpid";
            case CSR_mhartid:
                return "mhartid";
            case CSR_mconfigptr:
                return "mconfigptr";

            case CSR_mstatus:
                return "mstatus";
            case CSR_misa:
                return "misa";
            case CSR_medeleg:
                return "medeleg";
            case CSR_mideleg:
                return "mideleg";
            case CSR_mie:
                return "mie";
            case CSR_mtvec:
                return "mtvec";
            case CSR_mcounteren:
                return "mcounteren";
            case CSR_mstatush:
                return "mstatush";

            case CSR_mscratch:
                return "mscratch";
            case CSR_mepc:
                return "mepc";
            case CSR_mcause:
                return "mcause";
            case CSR_mtval:
                return "mtval";
            case CSR_mip:
                return "mip";
            case CSR_mtinst:
                return "mtinst";
            case CSR_mtval2:
                return "mtval2";

            case CSR_menvcfg:
                return "menvcfg";
            case CSR_menvcfgh:
                return "menvcfgh";
            case CSR_mseccfg:
                return "mseccfg";
            case CSR_mseccfgh:
                return "mseccfgh";

            case CSR_pmpcfg0:
            case CSR_pmpcfg1:
            case CSR_pmpcfg2:
            case CSR_pmpcfg3:
            case CSR_pmpcfg4:
            case CSR_pmpcfg5:
            case CSR_pmpcfg6:
            case CSR_pmpcfg7:
            case CSR_pmpcfg8:
            case CSR_pmpcfg9:
            case CSR_pmpcfg10:
            case CSR_pmpcfg11:
            case CSR_pmpcfg12:
            case CSR_pmpcfg13:
            case CSR_pmpcfg14:
            case CSR_pmpcfg15:
                return "pmpcfg" + (csr - CSR_pmpcfg0);
            case CSR_pmpaddr0:
            case CSR_pmpaddr1:
            case CSR_pmpaddr2:
            case CSR_pmpaddr3:
            case CSR_pmpaddr4:
            case CSR_pmpaddr5:
            case CSR_pmpaddr6:
            case CSR_pmpaddr7:
            case CSR_pmpaddr8:
            case CSR_pmpaddr9:
            case CSR_pmpaddr10:
            case CSR_pmpaddr11:
            case CSR_pmpaddr12:
            case CSR_pmpaddr13:
            case CSR_pmpaddr14:
            case CSR_pmpaddr15:
            case CSR_pmpaddr16:
            case CSR_pmpaddr17:
            case CSR_pmpaddr18:
            case CSR_pmpaddr19:
            case CSR_pmpaddr20:
            case CSR_pmpaddr21:
            case CSR_pmpaddr22:
            case CSR_pmpaddr23:
            case CSR_pmpaddr24:
            case CSR_pmpaddr25:
            case CSR_pmpaddr26:
            case CSR_pmpaddr27:
            case CSR_pmpaddr28:
            case CSR_pmpaddr29:
            case CSR_pmpaddr30:
            case CSR_pmpaddr31:
            case CSR_pmpaddr32:
            case CSR_pmpaddr33:
            case CSR_pmpaddr34:
            case CSR_pmpaddr35:
            case CSR_pmpaddr36:
            case CSR_pmpaddr37:
            case CSR_pmpaddr38:
            case CSR_pmpaddr39:
            case CSR_pmpaddr40:
            case CSR_pmpaddr41:
            case CSR_pmpaddr42:
            case CSR_pmpaddr43:
            case CSR_pmpaddr44:
            case CSR_pmpaddr45:
            case CSR_pmpaddr46:
            case CSR_pmpaddr47:
            case CSR_pmpaddr48:
            case CSR_pmpaddr49:
            case CSR_pmpaddr50:
            case CSR_pmpaddr51:
            case CSR_pmpaddr52:
            case CSR_pmpaddr53:
            case CSR_pmpaddr54:
            case CSR_pmpaddr55:
            case CSR_pmpaddr56:
            case CSR_pmpaddr57:
            case CSR_pmpaddr58:
            case CSR_pmpaddr59:
            case CSR_pmpaddr60:
            case CSR_pmpaddr61:
            case CSR_pmpaddr62:
            case CSR_pmpaddr63:
                return "pmpaddr" + (csr - CSR_pmpaddr0);

            case CSR_mcycle:
                return "mcycle";
            case CSR_minstret:
                return "minstret";
            case CSR_mhpmcounter3:
                return "mhpmcounter3";
            case CSR_mhpmcounter4:
                return "mhpmcounter4";
            case CSR_mhpmcounter5:
                return "mhpmcounter5";
            case CSR_mhpmcounter6:
                return "mhpmcounter6";
            case CSR_mhpmcounter7:
                return "mhpmcounter7";
            case CSR_mhpmcounter8:
                return "mhpmcounter8";
            case CSR_mhpmcounter9:
                return "mhpmcounter9";
            case CSR_mhpmcounter10:
                return "mhpmcounter10";
            case CSR_mhpmcounter11:
                return "mhpmcounter11";
            case CSR_mhpmcounter12:
                return "mhpmcounter12";
            case CSR_mhpmcounter13:
                return "mhpmcounter13";
            case CSR_mhpmcounter14:
                return "mhpmcounter14";
            case CSR_mhpmcounter15:
                return "mhpmcounter15";
            case CSR_mhpmcounter16:
                return "mhpmcounter16";
            case CSR_mhpmcounter17:
                return "mhpmcounter17";
            case CSR_mhpmcounter18:
                return "mhpmcounter18";
            case CSR_mhpmcounter19:
                return "mhpmcounter19";
            case CSR_mhpmcounter20:
                return "mhpmcounter20";
            case CSR_mhpmcounter21:
                return "mhpmcounter21";
            case CSR_mhpmcounter22:
                return "mhpmcounter22";
            case CSR_mhpmcounter23:
                return "mhpmcounter23";
            case CSR_mhpmcounter24:
                return "mhpmcounter24";
            case CSR_mhpmcounter25:
                return "mhpmcounter25";
            case CSR_mhpmcounter26:
                return "mhpmcounter26";
            case CSR_mhpmcounter27:
                return "mhpmcounter27";
            case CSR_mhpmcounter28:
                return "mhpmcounter28";
            case CSR_mhpmcounter29:
                return "mhpmcounter29";
            case CSR_mhpmcounter30:
                return "mhpmcounter30";
            case CSR_mhpmcounter31:
                return "mhpmcounter31";

            case CSR_mcycleh:
                return "mcycleh";
            case CSR_minstreth:
                return "minstreth";
            case CSR_mhpmcounter3h:
                return "mhpmcounter3h";
            case CSR_mhpmcounter4h:
                return "mhpmcounter4h";
            case CSR_mhpmcounter5h:
                return "mhpmcounter5h";
            case CSR_mhpmcounter6h:
                return "mhpmcounter6h";
            case CSR_mhpmcounter7h:
                return "mhpmcounter7h";
            case CSR_mhpmcounter8h:
                return "mhpmcounter8h";
            case CSR_mhpmcounter9h:
                return "mhpmcounter9h";
            case CSR_mhpmcounter10h:
                return "mhpmcounter10h";
            case CSR_mhpmcounter11h:
                return "mhpmcounter11h";
            case CSR_mhpmcounter12h:
                return "mhpmcounter12h";
            case CSR_mhpmcounter13h:
                return "mhpmcounter13h";
            case CSR_mhpmcounter14h:
                return "mhpmcounter14h";
            case CSR_mhpmcounter15h:
                return "mhpmcounter15h";
            case CSR_mhpmcounter16h:
                return "mhpmcounter16h";
            case CSR_mhpmcounter17h:
                return "mhpmcounter17h";
            case CSR_mhpmcounter18h:
                return "mhpmcounter18h";
            case CSR_mhpmcounter19h:
                return "mhpmcounter19h";
            case CSR_mhpmcounter20h:
                return "mhpmcounter20h";
            case CSR_mhpmcounter21h:
                return "mhpmcounter21h";
            case CSR_mhpmcounter22h:
                return "mhpmcounter22h";
            case CSR_mhpmcounter23h:
                return "mhpmcounter23h";
            case CSR_mhpmcounter24h:
                return "mhpmcounter24h";
            case CSR_mhpmcounter25h:
                return "mhpmcounter25h";
            case CSR_mhpmcounter26h:
                return "mhpmcounter26h";
            case CSR_mhpmcounter27h:
                return "mhpmcounter27h";
            case CSR_mhpmcounter28h:
                return "mhpmcounter28h";
            case CSR_mhpmcounter29h:
                return "mhpmcounter29h";
            case CSR_mhpmcounter30h:
                return "mhpmcounter30h";
            case CSR_mhpmcounter31h:
                return "mhpmcounter31h";

            case CSR_mcountinhibit:
                return "mcountinhibit";
            case CSR_mhpmevent3:
                return "mhpmevent3";
            case CSR_mhpmevent4:
                return "mhpmevent4";
            case CSR_mhpmevent5:
                return "mhpmevent5";
            case CSR_mhpmevent6:
                return "mhpmevent6";
            case CSR_mhpmevent7:
                return "mhpmevent7";
            case CSR_mhpmevent8:
                return "mhpmevent8";
            case CSR_mhpmevent9:
                return "mhpmevent9";
            case CSR_mhpmevent10:
                return "mhpmevent10";
            case CSR_mhpmevent11:
                return "mhpmevent11";
            case CSR_mhpmevent12:
                return "mhpmevent12";
            case CSR_mhpmevent13:
                return "mhpmevent13";
            case CSR_mhpmevent14:
                return "mhpmevent14";
            case CSR_mhpmevent15:
                return "mhpmevent15";
            case CSR_mhpmevent16:
                return "mhpmevent16";
            case CSR_mhpmevent17:
                return "mhpmevent17";
            case CSR_mhpmevent18:
                return "mhpmevent18";
            case CSR_mhpmevent19:
                return "mhpmevent19";
            case CSR_mhpmevent20:
                return "mhpmevent20";
            case CSR_mhpmevent21:
                return "mhpmevent21";
            case CSR_mhpmevent22:
                return "mhpmevent22";
            case CSR_mhpmevent23:
                return "mhpmevent23";
            case CSR_mhpmevent24:
                return "mhpmevent24";
            case CSR_mhpmevent25:
                return "mhpmevent25";
            case CSR_mhpmevent26:
                return "mhpmevent26";
            case CSR_mhpmevent27:
                return "mhpmevent27";
            case CSR_mhpmevent28:
                return "mhpmevent28";
            case CSR_mhpmevent29:
                return "mhpmevent29";
            case CSR_mhpmevent30:
                return "mhpmevent30";
            case CSR_mhpmevent31:
                return "mhpmevent31";

            case CSR_tselect:
                return "tselect";
            case CSR_tdata1:
                return "tdata1";
            case CSR_tdata2:
                return "tdata2";
            case CSR_tdata3:
                return "tdata3";
            case CSR_tcontext:
                return "tcontext";

            case CSR_dcsr:
                return "dcsr";
            case CSR_dpc:
                return "dpc";
            case CSR_dscratch0:
                return "dscratch0";
            case CSR_dscratch1:
                return "dscratch1";

            default:
                return "0x" + HexFormatter.tohex(csr, 3);
        }
    }
}
