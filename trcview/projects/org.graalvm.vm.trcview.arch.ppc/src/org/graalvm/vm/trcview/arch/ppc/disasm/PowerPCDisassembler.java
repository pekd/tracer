package org.graalvm.vm.trcview.arch.ppc.disasm;

import org.graalvm.vm.trcview.arch.CodeReader;
import org.graalvm.vm.trcview.arch.Disassembler;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.ppc.io.PowerPCCpuState;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.util.HexFormatter;

public class PowerPCDisassembler extends Disassembler {
    public static final InstructionFormat insnfmt = new InstructionFormat();

    private static final String[] BC_TRUE = {"lt", "gt", "eq", "so"};
    private static final String[] BC_FALSE = {"ge", "le", "ne", "ns"};

    public PowerPCDisassembler(TraceAnalyzer trc) {
        super(trc);
    }

    private static boolean bo(int bo, int bit) {
        return (bo & (1 << (4 - bit))) != 0;
    }

    private static boolean cr(long cr, int bit) {
        return (cr & (1 << (63 - bit))) != 0;
    }

    public static InstructionType getType(PowerPCCpuState state, int insn) {
        int opcd = insnfmt.OPCD.get(insn);
        switch (opcd) {
            case Opcode.TWI:
                return InstructionType.SYSCALL;
            case Opcode.BC: {
                boolean ctr_ok;
                boolean cond_ok;
                int bi = insnfmt.BI.get(insn);
                int bo = insnfmt.BO.get(insn);
                if (!bo(bo, 2)) {
                    ctr_ok = (state.getCTR() != 0) ^ bo(bo, 3);
                } else {
                    ctr_ok = true;
                }
                cond_ok = bo(bo, 0) || (cr(state.getCR(), bi + 32) == bo(bo, 1));
                if (ctr_ok && cond_ok) {
                    if (insnfmt.LK.getBit(insn)) {
                        return InstructionType.CALL;
                    }
                }
                return InstructionType.JCC;
            }
            case Opcode.B:
                if (insnfmt.LK.getBit(insn)) {
                    return InstructionType.CALL;
                } else {
                    return InstructionType.JMP;
                }
            case Opcode.CR_OPS:
                switch (insnfmt.XO_1.get(insn)) {
                    case Opcode.XO_BCLR: {
                        boolean ctr_ok;
                        boolean cond_ok;
                        int bi = insnfmt.BI.get(insn);
                        int bo = insnfmt.BO.get(insn);
                        if (!bo(bo, 2)) {
                            ctr_ok = (state.getCTR() != 0) ^ bo(bo, 3);
                        } else {
                            ctr_ok = true;
                        }
                        cond_ok = bo(bo, 0) || (cr(state.getCR(), bi + 32) == bo(bo, 1));
                        if (ctr_ok && cond_ok) {
                            if (insnfmt.LK.getBit(insn)) {
                                // TODO: this is a RET+CALL in one instruction
                                return InstructionType.RET;
                            } else {
                                return InstructionType.RET;
                            }
                        }
                        return InstructionType.JCC;
                    }
                    case Opcode.XO_RFI:
                        return InstructionType.RTI;
                    case Opcode.XO_BCCTR: {
                        int bi = insnfmt.BI.get(insn);
                        int bo = insnfmt.BO.get(insn);
                        boolean cond_ok;
                        cond_ok = bo(bo, 0) || (cr(state.getCR(), bi + 32) == bo(bo, 1));
                        if (cond_ok) {
                            if (insnfmt.LK.getBit(insn)) {
                                return InstructionType.CALL;
                            }
                        }
                        return InstructionType.JCC;
                    }
                    default:
                        return InstructionType.OTHER;
                }
            case Opcode.SC:
                return InstructionType.SYSCALL;
            default:
                return InstructionType.OTHER;
        }
    }

    public static String[] disassemble(int pc, int word) {
        InstructionFormat insn = new InstructionFormat(word);
        switch (insn.OPCD.get()) {
            case Opcode.TWI:
                return twi(insn);
            case Opcode.MULLI:
                return mulli(insn);
            case Opcode.SUBFIC:
                return subfic(insn);
            case Opcode.CMPLI:
                return cmpli(insn);
            case Opcode.CMPI:
                return cmpi(insn);
            case Opcode.ADDIC:
                return addic(insn);
            case Opcode.ADDIC_:
                return addic_(insn);
            case Opcode.ADDI:
                return addi(insn);
            case Opcode.ADDIS:
                return addis(insn);
            case Opcode.BC:
                return bc(pc, insn);
            case Opcode.SC:
                return sc(insn);
            case Opcode.B:
                return b(pc, insn);
            case Opcode.CR_OPS:
                switch (insn.XO_1.get()) {
                    case Opcode.XO_MCRF:
                        return mcrf(insn);
                    case Opcode.XO_BCLR:
                        return bclr(insn);
                    case Opcode.XO_RFI:
                        return rfi();
                    case Opcode.XO_ISYNC:
                        return isync();
                    case Opcode.XO_CRXOR:
                        return crxor(insn);
                    case Opcode.XO_CREQV:
                        return creqv(insn);
                    case Opcode.XO_CRORC:
                        return crorc(insn);
                    case Opcode.XO_CROR:
                        return cror(insn);
                    case Opcode.XO_BCCTR:
                        return bcctr(insn);
                    default:
                        return new String[]{".int", "0x" + HexFormatter.tohex(word, 8),
                                        "# unknown opcode " + insn.OPCD.get() + ", xo " + insn.XO_1.get()};
                }
            case Opcode.RLWIMI:
                return rlwimi(insn);
            case Opcode.RLWINM:
                return rlwinm(insn);
            case Opcode.RLWNM:
                return rlwnm(insn);
            case Opcode.ORI:
                return ori(insn);
            case Opcode.ORIS:
                return oris(insn);
            case Opcode.XORI:
                return xori(insn);
            case Opcode.XORIS:
                return xoris(insn);
            case Opcode.ANDI:
                return andi(insn);
            case Opcode.ANDIS:
                return andis(insn);
            case Opcode.FX_EXTENDED_OPS:
                switch (insn.XO_1.get()) {
                    case Opcode.XO_CMP:
                        return cmp(insn);
                    case Opcode.XO_MFCR:
                        return mfcr(insn);
                    case Opcode.XO_LWARX:
                        return lwarx(insn);
                    case Opcode.XO_LWZX:
                        return lwzx(insn);
                    case Opcode.XO_SLW:
                        return slw(insn);
                    case Opcode.XO_CNTLZW:
                        return cntlzw(insn);
                    case Opcode.XO_AND:
                        return and(insn);
                    case Opcode.XO_CMPL:
                        return cmpl(insn);
                    case Opcode.XO_DCBST:
                        return dcbst(insn);
                    case Opcode.XO_LWZUX:
                        return lwzux(insn);
                    case Opcode.XO_ANDC:
                        return andc(insn);
                    case Opcode.XO_MFMSR:
                        return mfmsr(insn);
                    case Opcode.XO_DCBF:
                        return dcbf(insn);
                    case Opcode.XO_LBZX:
                        return lbzx(insn);
                    case Opcode.XO_LVX:
                        return lvx(insn);
                    case Opcode.XO_LBZUX:
                        return lbzux(insn);
                    case Opcode.XO_NOR:
                        return nor(insn);
                    case Opcode.XO_MTCRF:
                        return mtcrf(insn);
                    case Opcode.XO_MTMSR:
                        return mtmsr(insn);
                    case Opcode.XO_STWCX_:
                        return stwcx_(insn);
                    case Opcode.XO_STWX:
                        return stwx(insn);
                    case Opcode.XO_STWUX:
                        return stwux(insn);
                    case Opcode.XO_STBX:
                        return stbx(insn);
                    case Opcode.XO_STVX:
                        return stvx(insn);
                    case Opcode.XO_DCBTST:
                        return dcbtst(insn);
                    case Opcode.XO_STBUX:
                        return stbux(insn);
                    case Opcode.XO_DCBT:
                        return dcbt(insn);
                    case Opcode.XO_LHZX:
                        return lhzx(insn);
                    case Opcode.XO_EQV:
                        return eqv(insn);
                    case Opcode.XO_LHZUX:
                        return lhzux(insn);
                    case Opcode.XO_XOR:
                        return xor(insn);
                    case Opcode.XO_MFSPR:
                        return mfspr(insn);
                    case Opcode.XO_LHAX:
                        return lhax(insn);
                    case Opcode.XO_MFTB:
                        return mftb(insn);
                    case Opcode.XO_STHX:
                        return sthx(insn);
                    case Opcode.XO_ORC:
                        return orc(insn);
                    case Opcode.XO_OR:
                        return or(insn);
                    case Opcode.XO_MTSPR:
                        return mtspr(insn);
                    case Opcode.XO_DCBI:
                        return dcbi(insn);
                    case Opcode.XO_LWBRX:
                        return lwbrx(insn);
                    case Opcode.XO_LFSX:
                        return lfsx(insn);
                    case Opcode.XO_SRW:
                        return srw(insn);
                    case Opcode.XO_LSWI:
                        return lswi(insn);
                    case Opcode.XO_SYNC:
                        return sync(insn);
                    case Opcode.XO_LFDX:
                        return lfdx(insn);
                    case Opcode.XO_STWBRX:
                        return stwbrx(insn);
                    case Opcode.XO_STFSX:
                        return stfsx(insn);
                    case Opcode.XO_STFSUX:
                        return stfsux(insn);
                    case Opcode.XO_STSWI:
                        return stswi(insn);
                    case Opcode.XO_STFDX:
                        return stfdx(insn);
                    case Opcode.XO_LHBRX:
                        return lhbrx(insn);
                    case Opcode.XO_SRAW:
                        return sraw(insn);
                    case Opcode.XO_SRAWI:
                        return srawi(insn);
                    case Opcode.XO_LFIWAX:
                        return lfiwax(insn);
                    case Opcode.XO_STHBRX:
                        return sthbrx(insn);
                    case Opcode.XO_EXTSH:
                        return extsh(insn);
                    case Opcode.XO_EXTSB:
                        return extsb(insn);
                    case Opcode.XO_ICBI:
                        return icbi(insn);
                    case Opcode.XO_DCBZ:
                        return dcbz(insn);
                }
                switch (insn.XO_2.get()) {
                    case Opcode.XO_SUBFC:
                        return subfc(insn);
                    case Opcode.XO_ADDC:
                        return addc(insn);
                    case Opcode.XO_MULHWU:
                        return mulhwu(insn);
                    case Opcode.XO_SUBF:
                        return subf(insn);
                    case Opcode.XO_MULHW:
                        return mulhw(insn);
                    case Opcode.XO_NEG:
                        return neg(insn);
                    case Opcode.XO_SUBFE:
                        return subfe(insn);
                    case Opcode.XO_ADDE:
                        return adde(insn);
                    case Opcode.XO_SUBFZE:
                        return subfze(insn);
                    case Opcode.XO_ADDZE:
                        return addze(insn);
                    case Opcode.XO_ADDME:
                        return addme(insn);
                    case Opcode.XO_MULLW:
                        return mullw(insn);
                    case Opcode.XO_ADD:
                        return add(insn);
                    case Opcode.XO_DIVWU:
                        return divwu(insn);
                    case Opcode.XO_NAND:
                        return nand(insn);
                    case Opcode.XO_DIVW:
                        return divw(insn);
                    default:
                        return new String[]{".int", "0x" + HexFormatter.tohex(word, 8),
                                        "# unknown opcode " + insn.OPCD.get() + ", xo " + insn.XO_1.get()};
                }
            case Opcode.LWZ:
                return lwz(insn);
            case Opcode.LWZU:
                return lwzu(insn);
            case Opcode.LBZ:
                return lbz(insn);
            case Opcode.LBZU:
                return lbzu(insn);
            case Opcode.STW:
                return stw(insn);
            case Opcode.STWU:
                return stwu(insn);
            case Opcode.STB:
                return stb(insn);
            case Opcode.STBU:
                return stbu(insn);
            case Opcode.LHZ:
                return lhz(insn);
            case Opcode.LHZU:
                return lhzu(insn);
            case Opcode.LHA:
                return lha(insn);
            case Opcode.LHAU:
                return lhau(insn);
            case Opcode.STH:
                return sth(insn);
            case Opcode.STHU:
                return sthu(insn);
            case Opcode.LMW:
                return lmw(insn);
            case Opcode.STMW:
                return stmw(insn);
            case Opcode.LFS:
                return lfs(insn);
            case Opcode.LFSU:
                return lfsu(insn);
            case Opcode.LFD:
                return lfd(insn);
            case Opcode.LFDU:
                return lfdu(insn);
            case Opcode.STFS:
                return stfs(insn);
            case Opcode.STFSU:
                return stfsu(insn);
            case Opcode.STFD:
                return stfd(insn);
            case Opcode.STFDU:
                return stfdu(insn);
            case Opcode.FP_SINGLE_OPS:
                switch (insn.XO_6.get()) {
                    case Opcode.XO_FDIVS:
                        return fdivs(insn);
                    case Opcode.XO_FSUBS:
                        return fsubs(insn);
                    case Opcode.XO_FMSUBS:
                        return fmsubs(insn);
                    case Opcode.XO_FADDS:
                        return fadds(insn);
                    case Opcode.XO_FMULS:
                        return fmuls(insn);
                    case Opcode.XO_FMADDS:
                        return fmadds(insn);
                    case Opcode.XO_FNMSUBS:
                        return fnmsubs(insn);
                    case Opcode.XO_FNMADDS:
                        return fnmadds(insn);
                    default:
                        return new String[]{".int", "0x" + HexFormatter.tohex(word, 8),
                                        "# unknown opcode " + insn.OPCD.get() + ", xo " + insn.XO_6.get()};
                }
            case Opcode.VSX_EXTENDED_OPS:
                switch (insn.XXO.get()) {
                    case Opcode.XO_XXLXOR:
                        return xxlxor(insn);
                    default:
                        return new String[]{".int", "0x" + HexFormatter.tohex(word, 8),
                                        "# unknown opcode " + insn.OPCD.get() + ", xo " + insn.XXO.get()};
                }
            case Opcode.FP_DOUBLE_OPS:
                switch (insn.XO_6.get()) {
                    case Opcode.XO_FRSP:
                        return frsp(insn);
                    case Opcode.XO_FDIV:
                        return fdiv(insn);
                    case Opcode.XO_FSUB:
                        return fsub(insn);
                    case Opcode.XO_FADD:
                        return fadd(insn);
                    case Opcode.XO_FMUL:
                        return fmul(insn);
                    case Opcode.XO_FMSUB:
                        return fmsub(insn);
                    case Opcode.XO_FMADD:
                        return fmadd(insn);
                    case Opcode.XO_FNMSUB:
                        return fnmsub(insn);
                    case Opcode.XO_FNMADD:
                        return fnmadd(insn);
                }
                switch (insn.XO_1.get()) {
                    case Opcode.XO_FCMPU:
                        return fcmpu(insn);
                    case Opcode.XO_FCTIWZ:
                        return fctiwz(insn);
                    case Opcode.XO_FNEG:
                        return fneg(insn);
                    case Opcode.XO_FMR:
                        return fmr(insn);
                    case Opcode.XO_MTFSFI:
                        return mtfsfi(insn);
                    case Opcode.XO_FNABS:
                        return fnabs(insn);
                    case Opcode.XO_FABS:
                        return fabs(insn);
                    case Opcode.XO_MFFS:
                        return mffs(insn);
                    case Opcode.XO_MTFSF:
                        return mtfsf(insn);
                    case Opcode.XO_FCFID:
                        return fcfid(insn);
                    default:
                        return new String[]{".int", "0x" + HexFormatter.tohex(word, 8),
                                        "# unknown opcode " + insn.OPCD.get() + ", xo " + insn.XO_1.get()};
                }
        }
        return new String[]{".int", "0x" + HexFormatter.tohex(word, 8),
                        "# unknown opcode " + insn.OPCD.get()};
    }

    protected static String r0(int r) {
        if (r == 0) {
            return "0";
        } else {
            return r(r);
        }
    }

    protected static String r(int r) {
        if (r == 1) {
            return "sp";
        } else {
            return "r" + r;
        }
    }

    protected static String[] twi(InstructionFormat insn) {
        int to = insn.TO.get();
        int ra = insn.RA.get();
        int si = insn.SI.get();
        String dec = Trap.decodeTO(to);
        if (dec != null) {
            return new String[]{"tw" + dec + "i", r(ra), Integer.toString(si)};
        } else {
            return new String[]{"twi", Integer.toString(to), r(ra), Integer.toString(si)};
        }
    }

    protected static String[] mulli(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int si = insn.SI.get();
        return new String[]{"mulli", r(rt), r(ra), Integer.toString(si)};
    }

    protected static String[] subfic(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int si = insn.SI.get();
        return new String[]{"subfic", r(rt), r(ra), Integer.toString(si)};
    }

    protected static String[] cmpli(InstructionFormat insn) {
        int bf = insn.BF.get() >>> 2;
        boolean l = insn.L.getBit();
        int ra = insn.RA.get();
        int ui = insn.UI.get();
        String uis = Integer.toString(ui);
        if (!l) {
            if (bf == 0) {
                return new String[]{"cmplwi", r(ra), uis};
            } else {
                return new String[]{"cmplwi", "cr" + bf, r(ra), uis};
            }
        } else {
            if (bf == 0) {
                return new String[]{"cmpldi", "cr" + bf, r(ra), uis};
            } else {
                return new String[]{"cmpldi", r(ra), uis};
            }
        }
    }

    protected static String[] cmpi(InstructionFormat insn) {
        int bf = insn.BF.get() >>> 2;
        boolean l = insn.L.getBit();
        int ra = insn.RA.get();
        int si = insn.SI.get();
        if (!l) {
            if (bf != 0) {
                return new String[]{"cmpwi", "cr" + bf, r(ra), Integer.toString(si)};
            } else {
                return new String[]{"cmpwi", r(ra), Integer.toString(si)};
            }
        } else {
            if (bf != 0) {
                return new String[]{"cmpdi", "cr" + bf, r(ra), Integer.toString(si)};
            } else {
                return new String[]{"cmpdi", r(ra), Integer.toString(si)};
            }
        }
    }

    protected static String[] addic(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int si = insn.SI.get();
        if (si < 0) {
            return new String[]{"subic", r(rt), r(ra), Integer.toString(-si)};
        } else {
            return new String[]{"addic", r(rt), r(ra), Integer.toString(si)};
        }
    }

    protected static String[] addic_(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int si = insn.SI.get();
        if (si < 0) {
            return new String[]{"subic.", r(rt), r(ra), Integer.toString(-si)};
        } else {
            return new String[]{"addic.", r(rt), r(ra), Integer.toString(si)};
        }
    }

    protected static String[] addi(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int si = insn.SI.get();
        String arg = Integer.toString(si);
        if (ra == 0) {
            return new String[]{"li", r(rt), arg};
        } else if (si < 0) {
            return new String[]{"subi", r(rt), r0(ra), Integer.toString(-si)};
        } else {
            return new String[]{"addi", r(rt), r0(ra), arg};
        }
    }

    protected static String[] addis(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int si = insn.SI.get() << 16;
        String arg = Integer.toString(si >> 16);
        if (ra == 0) {
            return new String[]{"lis", r(rt), arg};
        } else if (si < 0) {
            return new String[]{"subis", r(rt), r0(ra), Integer.toString(-(si >> 16))};
        } else {
            return new String[]{"addis", r(rt), r0(ra), arg};
        }
    }

    protected static String[] bc(int pc, InstructionFormat insn) {
        int bo = insn.BO.get();
        int bi = insn.BI.get();
        int bd = insn.BD.get() << 2;
        boolean aa = insn.AA.getBit();
        boolean lk = insn.LK.getBit();
        String a = aa ? "a" : "";
        String l = lk ? "l" : "";
        String add = l + a;
        String bta = Integer.toHexString(aa ? bd : pc + bd);
        String prefix = "b";
        if ((bo & 0b11110) == 0 || (bo & 0b1110) == 0b00010 || (bo & 0b11100) == 0b00100) {
            if ((bo & 0b1110) == 0) {
                // Decrement the CTR, then branch if the decremented CTR != 0 and CR_BI = 0
                prefix = "bdnz";
            } else if ((bo & 0b11110) == 0b00010) {
                // Decrement the CTR, then branch if the decremented CTR = 0 and CR_BI = 0
                prefix = "bdz";
            }
            String suffix = BC_FALSE[bi & 0x3];
            int cr = bi >> 2;
            if (cr != 0) {
                return new String[]{prefix + suffix + add, "cr" + cr, bta};
            } else {
                return new String[]{prefix + suffix + add, bta};
            }
        } else if ((bo & 0b11110) == 0b01000 || (bo & 0b11110) == 0b01010 || (bo & 0b11100) == 0b01100) {
            if ((bo & 0b11110) == 0b01000) {
                // Decrement the CTR, then branch if the decremented CTR != 0 and CR_BI = 1
                prefix = "bdnz";
            } else if ((bo & 0b11110) == 0b01010) {
                // Decrement the CTR, then branch if the decremented CTR = 0 and CR_BI = 1
                prefix = "bdz";
            }
            String suffix = BC_TRUE[bi & 0x3];
            int cr = bi >> 2;
            if (cr != 0) {
                return new String[]{prefix + suffix + add, "cr" + cr, bta};
            } else {
                return new String[]{prefix + suffix + add, bta};
            }
        } else if ((bo & 0b10100) == 0b10100) {
            return new String[]{"b" + add, bta};
        } else if ((bo & 0b10110) == 0b10000) {
            return new String[]{"bdnz", bta};
        } else if ((bo & 0b10110) == 0b10010) {
            return new String[]{"bdz", bta};
        }
        return new String[]{"bc" + add, Integer.toString(bo), Integer.toString(bi), bta};
    }

    protected static String[] sc(InstructionFormat insn) {
        int lev = insn.LEV.get();
        if (lev == 0) {
            return new String[]{"sc"};
        } else {
            return new String[]{"sc", Integer.toString(lev)};
        }
    }

    protected static String[] b(int pc, InstructionFormat insn) {
        int li = insn.LI.get() << 2;
        boolean aa = insn.AA.getBit();
        boolean lk = insn.LK.getBit();
        StringBuilder name = new StringBuilder(3);
        int bta = li;
        name.append('b');
        if (lk) {
            name.append('l');
        }
        if (aa) {
            name.append('a');
        } else {
            bta += pc;
        }
        return new String[]{name.toString(), Integer.toHexString(bta)};
    }

    protected static String[] mcrf(InstructionFormat insn) {
        int bf = insn.XL_BF.get();
        int bfa = insn.XL_BFA.get();
        return new String[]{"mcrf", "cr" + bf, "cr" + bfa};
    }

    protected static String[] bclr(InstructionFormat insn) {
        int bo = insn.BO.get();
        int bi = insn.BI.get();
        int bh = insn.BH.get() & 3;
        boolean lk = insn.LK.getBit();
        String l = lk ? "l" : "";
        if (bo == 20 && bi == 0 && bh == 0) {
            return new String[]{"blr" + l};
        } else if (bo == 18 && bi == 0 && bh == 0) {
            return new String[]{"bdzlr" + l};
        } else if (bo == 16 && bi == 0 && bh == 0) {
            return new String[]{"bdnzlr" + l};
        } else if (bo == 12 && bh == 0) {
            if (bi == 0) {
                return new String[]{"btlr" + l};
            } else {
                return new String[]{"btlr" + l, Integer.toString(bi)};
            }
        } else if (bo == 4 && bh == 0) {
            if (bi == 0) {
                return new String[]{"bflr" + l};
            } else {
                return new String[]{"bflr" + l, Integer.toString(bi)};
            }
        } else if (bo == 10 && bh == 0) {
            if (bi == 0) {
                return new String[]{"bdztlr" + l};
            } else {
                return new String[]{"bdztlr" + l, Integer.toString(bi)};
            }
        } else if (bo == 2 && bh == 0) {
            if (bi == 0) {
                return new String[]{"bdzflr" + l};
            } else {
                return new String[]{"bdzflr" + l, Integer.toString(bi)};
            }
        } else if (bo == 8 && bh == 0) {
            if (bi == 0) {
                return new String[]{"bdnztlr" + l};
            } else {
                return new String[]{"bdnztlr" + l, Integer.toString(bi)};
            }
        } else if (bo == 0 && bh == 0) {
            if (bi == 0) {
                return new String[]{"bdnzflr" + l};
            } else {
                return new String[]{"bdnzflr" + l, Integer.toString(bi)};
            }
        }
        if (bh != 0) {
            return new String[]{"bclr" + l, Integer.toString(bo), Integer.toString(bi)};
        } else {
            return new String[]{"bclr" + l, Integer.toString(bo), Integer.toString(bi),
                            Integer.toString(bh)};
        }
    }

    protected static String[] rfi() {
        return new String[]{"rfi"};
    }

    protected static String[] isync() {
        return new String[]{"isync"};
    }

    protected static String[] crxor(InstructionFormat insn) {
        int bt = insn.BT.get();
        int ba = insn.BA.get();
        int bb = insn.BB.get();
        if (bt == ba && ba == bb) {
            return new String[]{"crclr", Cr.getName(bt)};
        } else {
            return new String[]{"crxor", Integer.toString(bt), Integer.toString(ba),
                            Integer.toString(bb)};
        }
    }

    protected static String[] creqv(InstructionFormat insn) {
        int bt = insn.BT.get();
        int ba = insn.BA.get();
        int bb = insn.BB.get();
        if (bt == ba && ba == bb) {
            return new String[]{"crset", Cr.getName(bt)};
        } else {
            return new String[]{"creqv", Integer.toString(bt), Integer.toString(ba),
                            Integer.toString(bb)};
        }
    }

    protected static String[] crorc(InstructionFormat insn) {
        int bt = insn.BT.get();
        int ba = insn.BA.get();
        int bb = insn.BB.get();
        return new String[]{"crorc", Integer.toString(bt), Integer.toString(ba), Integer.toString(bb)};
    }

    protected static String[] cror(InstructionFormat insn) {
        int bt = insn.BT.get();
        int ba = insn.BA.get();
        int bb = insn.BB.get();
        if (ba == bb) {
            return new String[]{"crmove", Cr.getName(bt), Cr.getName(ba)};
        } else {
            return new String[]{"cror", Integer.toString(bt), Integer.toString(ba),
                            Integer.toString(bb)};
        }
    }

    protected static String[] bcctr(InstructionFormat insn) {
        int bo = insn.BO.get();
        int bi = insn.BI.get();
        int bh = insn.BH.get() & 1;
        boolean lk = insn.LK.getBit();
        String l = lk ? "l" : "";
        if (bo == 20 && bi == 0 && bh == 0) {
            return new String[]{"bctr" + l};
        } else if (bo == 18 && bi == 0 && bh == 0) {
            return new String[]{"bdzctr" + l};
        } else if (bo == 16 && bi == 0 && bh == 0) {
            return new String[]{"bdnzctr" + l};
        } else if (bo == 12 && bh == 0) {
            if (bi == 0) {
                return new String[]{"btctr" + l};
            } else {
                return new String[]{"btctr" + l, Integer.toString(bi)};
            }
        } else if (bo == 4 && bh == 0) {
            if (bi == 0) {
                return new String[]{"bfctr" + l};
            } else {
                return new String[]{"bfctr" + l, Integer.toString(bi)};
            }
        } else if (bo == 10 && bh == 0) {
            if (bi == 0) {
                return new String[]{"bdztctr" + l};
            } else {
                return new String[]{"bdztctr" + l, Integer.toString(bi)};
            }
        } else if (bo == 2 && bh == 0) {
            if (bi == 0) {
                return new String[]{"bdzfctr" + l};
            } else {
                return new String[]{"bdzfctr" + l, Integer.toString(bi)};
            }
        } else if (bo == 8 && bh == 0) {
            if (bi == 0) {
                return new String[]{"bdnztctr" + l};
            } else {
                return new String[]{"bdnztctr" + l, Integer.toString(bi)};
            }
        } else if (bo == 0 && bh == 0) {
            if (bi == 0) {
                return new String[]{"bdnzfctr" + l};
            } else {
                return new String[]{"bdnzfctr" + l, Integer.toString(bi)};
            }
        }
        if (bh != 0) {
            return new String[]{"bcctr" + l, Integer.toString(bo), Integer.toString(bi)};
        } else {
            return new String[]{"bcctr" + l, Integer.toString(bo), Integer.toString(bi),
                            Integer.toString(bh)};
        }
    }

    protected static String[] rlwimi(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int sh = insn.SH.get();
        int mb = insn.MB.get();
        int me = insn.ME.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        if (sh == (32 - mb)) {
            int b = mb;
            int n = me - b + 1;
            return new String[]{"inslwi", r(ra), r(rs), Integer.toString(n), Integer.toString(b)};
        } else {
            return new String[]{"rlwimi" + dot, r(ra), r(rs), Integer.toString(sh),
                            Integer.toString(mb), Integer.toString(me)};
        }
    }

    protected static String[] rlwinm(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int sh = insn.SH.get();
        int mb = insn.MB.get();
        int me = insn.ME.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        if (sh == 0 && mb == 0) {
            int n = 31 - me;
            return new String[]{"clrrwi" + dot, r(ra), r(rs), Integer.toString(n)};
        } else if (sh == (32 - mb) && me == 31) {
            return new String[]{"srwi" + dot, r(ra), r(rs), Integer.toString(mb)};
        } else if (mb == 0) {
            int n = me + 1;
            return new String[]{"extlwi" + dot, r(ra), r(rs), Integer.toString(n),
                            Integer.toString(sh)};
        } else {
            return new String[]{"rlwinm" + dot, r(ra), r(rs), Integer.toString(sh),
                            Integer.toString(mb), Integer.toString(me)};
        }
    }

    protected static String[] rlwnm(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        int mb = insn.MB.get();
        int me = insn.ME.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        if (mb == 0 && me == 31) {
            return new String[]{"rotlw" + dot, r(ra), r(rs), r(rb)};
        } else {
            return new String[]{"rlwnm" + dot, r(ra), r(rs), r(rb), Integer.toString(mb),
                            Integer.toString(me)};
        }
    }

    protected static String[] ori(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int ui = insn.UI.get();
        if (rs == 0 && ra == 0 && ui == 0) {
            return new String[]{"nop"};
        } else {
            return new String[]{"ori", r(ra), r(rs), Integer.toString(ui)};
        }
    }

    protected static String[] oris(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int ui = insn.UI.get();
        return new String[]{"oris", r(ra), r(rs), Integer.toString(ui)};
    }

    protected static String[] xori(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int ui = insn.UI.get();
        if (rs == 0 && ra == 0 && ui == 0) {
            return new String[]{"xnop"};
        } else {
            return new String[]{"xori", r(ra), r(rs), Integer.toString(ui)};
        }
    }

    protected static String[] xoris(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int ui = insn.UI.get() << 16;
        return new String[]{"xoris", r(ra), r(rs), Integer.toString(ui >>> 16)};
    }

    protected static String[] andi(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int ui = insn.UI.get();
        return new String[]{"andi.", r(ra), r(rs), Integer.toString(ui)};
    }

    protected static String[] andis(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int ui = insn.UI.get() << 16;
        String arg = Integer.toString(ui >>> 16);
        return new String[]{"andis.", r(ra), r(rs), arg};
    }

    protected static String[] cmp(InstructionFormat insn) {
        int bf = insn.BF.get() >>> 2;
        boolean l = insn.L.getBit();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        if (!l) {
            if (bf == 0) {
                return new String[]{"cmpw", r(ra), r(rb)};
            } else {
                return new String[]{"cmpw", "cr" + Integer.toString(bf), r(ra), r(rb)};
            }
        } else {
            if (bf == 0) {
                return new String[]{"cmpd", r(ra), r(rb)};
            } else {
                return new String[]{"cmpd", "cr" + Integer.toString(bf), r(ra), r(rb)};
            }
        }
    }

    protected static String[] mfcr(InstructionFormat insn) {
        int rt = insn.RT.get();
        return new String[]{"mfcr", r(rt)};
    }

    protected static String[] lwarx(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        boolean eh = insn.EH.getBit();
        if (eh) {
            return new String[]{"lwarx", r(rt), r0(ra), r(rb), "1"};
        } else {
            return new String[]{"lwarx", r(rt), r0(ra), r(rb)};
        }
    }

    protected static String[] lwzx(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return new String[]{"lwzx", r(rt), r0(ra), r(rb)};
    }

    protected static String[] slw(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"slw" + dot, r(ra), r(rs), r(rb)};
    }

    protected static String[] cntlzw(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"cntlzw" + dot, r(ra), r(rs)};
    }

    protected static String[] and(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"and" + dot, r(ra), r(rs), r(rb)};
    }

    protected static String[] cmpl(InstructionFormat insn) {
        int bf = insn.BF.get() >>> 2;
        boolean l = insn.L.getBit();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        if (!l) {
            if (bf == 0) {
                return new String[]{"cmplw", r(ra), r(rb)};
            } else {
                return new String[]{"cmplw", "cr" + bf, r(ra), r(rb)};
            }
        } else {
            if (bf == 0) {
                return new String[]{"cmpld", "cr" + bf, r(ra), r(rb)};
            } else {
                return new String[]{"cmpld", r(ra), r(rb)};
            }
        }
    }

    protected static String[] dcbst(InstructionFormat insn) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return new String[]{"dcbst", r0(ra), r(rb)};
    }

    protected static String[] lwzux(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return new String[]{"lwzux", r(rt), r(ra), r(rb)};
    }

    protected static String[] andc(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"andc" + dot, r(ra), r(rs), r(rb)};
    }

    protected static String[] mfmsr(InstructionFormat insn) {
        int rt = insn.RT.get();
        return new String[]{"mfmsr", r(rt)};
    }

    protected static String[] dcbf(InstructionFormat insn) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return new String[]{"dcbf", r0(ra), r(rb)};
    }

    protected static String[] lbzx(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return new String[]{"lbzx", r(rt), r0(ra), r(rb)};
    }

    protected static String[] lvx(InstructionFormat insn) {
        int vrt = insn.VRT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return new String[]{"lvx", "v" + vrt, r0(ra), r(rb)};
    }

    protected static String[] lbzux(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return new String[]{"lbzux", r(rt), r(ra), r(rb)};
    }

    protected static String[] nor(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"nor" + dot, r(ra), r(rs), r(rb)};
    }

    protected static String[] mtcrf(InstructionFormat insn) {
        int rs = insn.RS.get();
        int fxm = insn.FXM.get();
        boolean one = insn.BIT_11.getBit();
        if (one) {
            return null;
        } else {
            if (fxm == 0xFF) {
                return new String[]{"mtcr", r(rs)};
            } else {
                return new String[]{"mtcrf", Integer.toString(fxm), r(rs)};
            }
        }
    }

    protected static String[] mtmsr(InstructionFormat insn) {
        int rs = insn.RS.get();
        return new String[]{"mtmsr", r(rs)};
    }

    protected static String[] stwcx_(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return new String[]{"stwcx.", r(rs), r0(ra), r(rb)};
    }

    protected static String[] stwx(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return new String[]{"stwx", r(rs), r0(ra), r(rb)};
    }

    protected static String[] stwux(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return new String[]{"stwux", r(rs), r(ra), r(rb)};
    }

    protected static String[] stbx(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return new String[]{"stbx", r(rs), r0(ra), r(rb)};
    }

    protected static String[] stvx(InstructionFormat insn) {
        int vrs = insn.VRS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return new String[]{"stvx", "v" + vrs, r0(ra), r(rb)};
    }

    protected static String[] dcbtst(InstructionFormat insn) {
        int th = insn.TH.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return new String[]{"dcbtst", r0(ra), r(rb), Integer.toString(th)};
    }

    protected static String[] stbux(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return new String[]{"stbux", r(rs), r0(ra), r(rb)};
    }

    protected static String[] dcbt(InstructionFormat insn) {
        int th = insn.TH.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        if (th == 0) {
            return new String[]{"dcbt", r0(ra), r(rb)};
        } else {
            return new String[]{"dcbt", r0(ra), r(rb), Integer.toString(th)};
        }
    }

    protected static String[] lhzx(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return new String[]{"lhzx", r(rt), r0(ra), r(rb)};
    }

    protected static String[] eqv(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"eqv" + dot, r(ra), r(rs), r(rb)};
    }

    protected static String[] lhzux(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return new String[]{"lhzux", r(rt), r0(ra), r(rb)};
    }

    protected static String[] xor(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"xor" + dot, r(ra), r(rs), r(rb)};
    }

    protected static String[] mfspr(InstructionFormat insn) {
        int rt = insn.RT.get();
        int spr = (insn.spr.get() & 0x1f) << 5 | (insn.spr.get() >> 5) & 0x1f;
        switch (spr) {
            case 1:
                return new String[]{"mfxer", r(rt)};
            case 8:
                return new String[]{"mflr", r(rt)};
            case 9:
                return new String[]{"mfctr", r(rt)};
            case 18:
                return new String[]{"mfdsisr", r(rt)};
            case 19:
                return new String[]{"mfdar", r(rt)};
            case 22:
                return new String[]{"mfdec", r(rt)};
            case 25:
                return new String[]{"mfsdr1", r(rt)};
            case 26:
                return new String[]{"mfsrr0", r(rt)};
            case 27:
                return new String[]{"mfsrr1", r(rt)};
            case 272:
                return new String[]{"mfsprg", r(rt), "0"};
            case 273:
                return new String[]{"mfsprg", r(rt), "1"};
            case 274:
                return new String[]{"mfsprg", r(rt), "2"};
            case 275:
                return new String[]{"mfsprg", r(rt), "3"};
            case 282:
                return new String[]{"mfear", r(rt)};
            case 287:
                return new String[]{"mfpvr", r(rt)};
            case 528:
                return new String[]{"mfibatu", r(rt), "0"};
            case 529:
                return new String[]{"mfibatl", r(rt), "0"};
            case 530:
                return new String[]{"mfibatu", r(rt), "1"};
            case 531:
                return new String[]{"mfibatl", r(rt), "1"};
            case 532:
                return new String[]{"mfibatu", r(rt), "2"};
            case 533:
                return new String[]{"mfibatl", r(rt), "2"};
            case 534:
                return new String[]{"mfibatu", r(rt), "3"};
            case 535:
                return new String[]{"mfibatl", r(rt), "3"};
            case 536:
                return new String[]{"mfdbatu", r(rt), "0"};
            case 537:
                return new String[]{"mfdbatl", r(rt), "0"};
            case 538:
                return new String[]{"mfdbatu", r(rt), "1"};
            case 539:
                return new String[]{"mfdbatl", r(rt), "1"};
            case 540:
                return new String[]{"mfdbatu", r(rt), "2"};
            case 541:
                return new String[]{"mfdbatl", r(rt), "2"};
            case 542:
                return new String[]{"mfdbatu", r(rt), "3"};
            case 543:
                return new String[]{"mfdbatl", r(rt), "3"};
            default:
                return new String[]{"mfspr", r(rt), Spr.toString(spr)};
        }
    }

    protected static String[] lhax(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return new String[]{"lhax", r(rt), r0(ra), r(rb)};
    }

    protected static String[] mftb(InstructionFormat insn) {
        int rt = insn.RT.get();
        int tbr = (insn.spr.get() & 0x1f) << 5 | (insn.spr.get() >> 5) & 0x1f;
        switch (tbr) {
            case 268:
                return new String[]{"mftb", r(rt)};
            case 269:
                return new String[]{"mftbu", r(rt)};
            default:
                return new String[]{"mftb", r(rt), Integer.toString(tbr)};
        }
    }

    protected static String[] sthx(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return new String[]{"sthx", r(rs), r0(ra), r(rb)};
    }

    protected static String[] orc(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"orc" + dot, r(ra), r(rs), r(rb)};
    }

    protected static String[] or(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        if (rs == rb) {
            return new String[]{"mr" + dot, r(ra), r(rs)};
        } else {
            return new String[]{"or" + dot, r(ra), r(rs), r(rb)};
        }
    }

    protected static String[] mtspr(InstructionFormat insn) {
        int rs = insn.RS.get();
        int spr = (insn.spr.get() & 0x1f) << 5 | (insn.spr.get() >> 5) & 0x1f;
        switch (spr) {
            case 1:
                return new String[]{"mtxer", r(rs)};
            case 8:
                return new String[]{"mtlr", r(rs)};
            case 9:
                return new String[]{"mtctr", r(rs)};
            case 18:
                return new String[]{"mtdsisr", r(rs)};
            case 19:
                return new String[]{"mtdar", r(rs)};
            case 22:
                return new String[]{"mtdec", r(rs)};
            case 25:
                return new String[]{"mtsdr1", r(rs)};
            case 26:
                return new String[]{"mtsrr0", r(rs)};
            case 27:
                return new String[]{"mtsrr1", r(rs)};
            case 272:
                return new String[]{"mtsprg", "0", r(rs)};
            case 273:
                return new String[]{"mtsprg", "1", r(rs)};
            case 274:
                return new String[]{"mtsprg", "2", r(rs)};
            case 275:
                return new String[]{"mtsprg", "3", r(rs)};
            case 282:
                return new String[]{"mtear", r(rs)};
            case 284:
                return new String[]{"mttbl", r(rs)};
            case 285:
                return new String[]{"mttbu", r(rs)};
            case 528:
                return new String[]{"mtibatu", "0", r(rs)};
            case 529:
                return new String[]{"mtibatl", "0", r(rs)};
            case 530:
                return new String[]{"mtibatu", "1", r(rs)};
            case 531:
                return new String[]{"mtibatl", "1", r(rs)};
            case 532:
                return new String[]{"mtibatu", "2", r(rs)};
            case 533:
                return new String[]{"mtibatl", "2", r(rs)};
            case 534:
                return new String[]{"mtibatu", "3", r(rs)};
            case 535:
                return new String[]{"mtibatl", "3", r(rs)};
            case 536:
                return new String[]{"mtdbatu", "0", r(rs)};
            case 537:
                return new String[]{"mtdbatl", "0", r(rs)};
            case 538:
                return new String[]{"mtdbatu", "1", r(rs)};
            case 539:
                return new String[]{"mtdbatl", "1", r(rs)};
            case 540:
                return new String[]{"mtdbatu", "2", r(rs)};
            case 541:
                return new String[]{"mtdbatl", "2", r(rs)};
            case 542:
                return new String[]{"mtdbatu", "3", r(rs)};
            case 543:
                return new String[]{"mtdbatl", "3", r(rs)};
            default:
                return new String[]{"mtspr", r(rs), Spr.toString(spr)};
        }
    }

    protected static String[] dcbi(InstructionFormat insn) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return new String[]{"dcbi", r0(ra), r(rb)};
    }

    protected static String[] lwbrx(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return new String[]{"lwbrx", r(rt), r0(ra), r(rb)};
    }

    protected static String[] lfsx(InstructionFormat insn) {
        int frt = insn.FRT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return new String[]{"lfsx", "f" + frt, r0(ra), r(rb)};
    }

    protected static String[] srw(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"srw" + dot, r(ra), r(rs), r(rb)};
    }

    protected static String[] lswi(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int nb = insn.NB.get();
        return new String[]{"lswi", r(rt), r(ra), Integer.toString(nb)};
    }

    protected static String[] sync(InstructionFormat insn) {
        boolean l = insn.X_L.getBit();
        int e = insn.E.get();
        return new String[]{"sync", l ? "1" : "0", Integer.toString(e)};
    }

    protected static String[] lfdx(InstructionFormat insn) {
        int frt = insn.FRT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return new String[]{"lfdx", "f" + frt, r0(ra), r(rb)};
    }

    protected static String[] stwbrx(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return new String[]{"stwbrx", r(rs), r0(ra), r(rb)};
    }

    protected static String[] stfsx(InstructionFormat insn) {
        int frs = insn.FRS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return new String[]{"stfsx", "f" + frs, r0(ra), r(rb)};
    }

    protected static String[] stfsux(InstructionFormat insn) {
        int frs = insn.FRS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return new String[]{"stfsux", "f" + frs, r(ra), r(rb)};
    }

    protected static String[] stswi(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int nb = insn.NB.get();
        return new String[]{"stswi", r(rs), r(ra), Integer.toString(nb)};
    }

    protected static String[] stfdx(InstructionFormat insn) {
        int frs = insn.FRS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return new String[]{"stfdx", "f" + frs, r0(ra), r(rb)};
    }

    protected static String[] lhbrx(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return new String[]{"lhbrx", r(rt), r0(ra), r(rb)};
    }

    protected static String[] sraw(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"sraw" + dot, r(ra), r(rs), r(rb)};
    }

    protected static String[] srawi(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int sh = insn.SH.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"srawi" + dot, r(ra), r(rs), Integer.toString(sh)};
    }

    protected static String[] lfiwax(InstructionFormat insn) {
        int frt = insn.FRT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return new String[]{"lfiwax", "f" + frt, r0(ra), r(rb)};
    }

    protected static String[] sthbrx(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return new String[]{"sthbrx", r(rs), r0(ra), r(rb)};
    }

    protected static String[] extsh(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"extsh" + dot, r(ra), r(rs)};
    }

    protected static String[] extsb(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"extsb" + dot, r(ra), r(rs)};
    }

    protected static String[] icbi(InstructionFormat insn) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return new String[]{"icbi", r0(ra), r(rb)};
    }

    protected static String[] dcbz(InstructionFormat insn) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return new String[]{"dcbz", r0(ra), r(rb)};
    }

    protected static String[] subfc(InstructionFormat insn) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        int rt = insn.RT.get();
        boolean oe = insn.OE.getBit();
        boolean rc = insn.Rc.getBit();
        StringBuilder add = new StringBuilder(2);
        if (oe) {
            add.append('o');
        }
        if (rc) {
            add.append('.');
        }
        return new String[]{"subfc" + add, r(rt), r(ra), r(rb)};
    }

    protected static String[] addc(InstructionFormat insn) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        int rt = insn.RT.get();
        boolean oe = insn.OE.getBit();
        boolean rc = insn.Rc.getBit();
        StringBuilder add = new StringBuilder(2);
        if (oe) {
            add.append('o');
        }
        if (rc) {
            add.append('.');
        }
        return new String[]{"addc" + add, r(rt), r(ra), r(rb)};
    }

    protected static String[] mulhwu(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"mulhwu" + dot, r(rt), r(ra), r(rb)};
    }

    protected static String[] subf(InstructionFormat insn) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        int rt = insn.RT.get();
        boolean oe = insn.OE.getBit();
        boolean rc = insn.Rc.getBit();
        StringBuilder add = new StringBuilder(2);
        if (oe) {
            add.append('o');
        }
        if (rc) {
            add.append('.');
        }
        return new String[]{"subf" + add, r(rt), r(ra), r(rb)};
    }

    protected static String[] mulhw(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"mulhw" + dot, r(rt), r(ra), r(rb)};
    }

    protected static String[] neg(InstructionFormat insn) {
        int ra = insn.RA.get();
        int rt = insn.RT.get();
        boolean oe = insn.OE.getBit();
        boolean rc = insn.Rc.getBit();
        StringBuilder add = new StringBuilder(2);
        if (oe) {
            add.append('o');
        }
        if (rc) {
            add.append('.');
        }
        return new String[]{"neg" + add, r(rt), r(ra)};
    }

    protected static String[] subfe(InstructionFormat insn) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        int rt = insn.RT.get();
        boolean oe = insn.OE.getBit();
        boolean rc = insn.Rc.getBit();
        StringBuilder add = new StringBuilder(2);
        if (oe) {
            add.append('o');
        }
        if (rc) {
            add.append('.');
        }
        return new String[]{"subfe" + add, r(rt), r(ra), r(rb)};
    }

    protected static String[] adde(InstructionFormat insn) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        int rt = insn.RT.get();
        boolean oe = insn.OE.getBit();
        boolean rc = insn.Rc.getBit();
        StringBuilder add = new StringBuilder(2);
        if (oe) {
            add.append('o');
        }
        if (rc) {
            add.append('.');
        }
        return new String[]{"adde" + add, r(rt), r(ra), r(rb)};
    }

    protected static String[] subfze(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        boolean oe = insn.OE.getBit();
        boolean rc = insn.Rc.getBit();
        StringBuilder add = new StringBuilder(2);
        if (oe) {
            add.append('o');
        }
        if (rc) {
            add.append('.');
        }
        return new String[]{"subfze" + add, r(rt), r(ra)};
    }

    protected static String[] addze(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        boolean oe = insn.OE.getBit();
        boolean rc = insn.Rc.getBit();
        StringBuilder add = new StringBuilder(2);
        if (oe) {
            add.append('o');
        }
        if (rc) {
            add.append('.');
        }
        return new String[]{"addze" + add, r(rt), r(ra)};
    }

    protected static String[] addme(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        boolean oe = insn.OE.getBit();
        boolean rc = insn.Rc.getBit();
        StringBuilder add = new StringBuilder(2);
        if (oe) {
            add.append('o');
        }
        if (rc) {
            add.append('.');
        }
        return new String[]{"addme" + add, r(rt), r(ra)};
    }

    protected static String[] mullw(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        boolean oe = insn.OE.getBit();
        boolean rc = insn.Rc.getBit();
        StringBuilder add = new StringBuilder(2);
        if (oe) {
            add.append('o');
        }
        if (rc) {
            add.append('.');
        }
        return new String[]{"mullw" + add, r(rt), r(ra), r(rb)};
    }

    protected static String[] add(InstructionFormat insn) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        int rt = insn.RT.get();
        boolean oe = insn.OE.getBit();
        boolean rc = insn.Rc.getBit();
        StringBuilder add = new StringBuilder(2);
        if (oe) {
            add.append('o');
        }
        if (rc) {
            add.append('.');
        }
        return new String[]{"add" + add, r(rt), r(ra), r(rb)};
    }

    protected static String[] divwu(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        boolean oe = insn.OE.getBit();
        boolean rc = insn.Rc.getBit();
        StringBuilder add = new StringBuilder(2);
        if (oe) {
            add.append('o');
        }
        if (rc) {
            add.append('.');
        }
        return new String[]{"divwu" + add, r(rt), r(ra), r(rb)};
    }

    protected static String[] nand(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"nand" + dot, r(ra), r(rs), r(rb)};
    }

    protected static String[] divw(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        boolean oe = insn.OE.getBit();
        boolean rc = insn.Rc.getBit();
        StringBuilder add = new StringBuilder(2);
        if (oe) {
            add.append('o');
        }
        if (rc) {
            add.append('.');
        }
        return new String[]{"div" + add, r(rt), r(ra), r(rb)};
    }

    protected static String[] lwz(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int d = insn.D.get();
        return new String[]{"lwz", r(rt), d + "(" + r0(ra) + ")"};
    }

    protected static String[] lwzu(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int d = insn.D.get();
        return new String[]{"lwzu", r(rt), d + "(r" + ra + ")"};
    }

    protected static String[] lbz(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int d = insn.D.get();
        return new String[]{"lbz", r(rt), d + "(" + r0(ra) + ")"};
    }

    protected static String[] lbzu(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int d = insn.D.get();
        return new String[]{"lbzu", r(rt), d + "(r" + ra + ")"};
    }

    protected static String[] stw(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int d = insn.D.get();
        return new String[]{"stw", r(rs), d + "(" + r0(ra) + ")"};
    }

    protected static String[] stwu(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int d = insn.D.get();
        return new String[]{"stwu", r(rs), d + "(r" + ra + ")"};
    }

    protected static String[] stb(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int d = insn.D.get();
        return new String[]{"stb", r(rs), d + "(" + r0(ra) + ")"};
    }

    protected static String[] stbu(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int d = insn.D.get();
        return new String[]{"stbu", r(rs), d + "(r" + ra + ")"};
    }

    protected static String[] lhz(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int d = insn.D.get();
        return new String[]{"lhz", r(rt), d + "(" + r0(ra) + ")"};
    }

    protected static String[] lhzu(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int d = insn.D.get();
        return new String[]{"lhzu", r(rt), d + "(r" + ra + ")"};
    }

    protected static String[] lha(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int d = insn.D.get();
        return new String[]{"lha", r(rt), d + "(" + r0(ra) + ")"};
    }

    protected static String[] lhau(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int d = insn.D.get();
        return new String[]{"lhau", r(rt), d + "(r" + ra + ")"};
    }

    protected static String[] sth(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int d = insn.D.get();
        return new String[]{"sth", r(rs), d + "(" + r0(ra) + ")"};
    }

    protected static String[] sthu(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int d = insn.D.get();
        return new String[]{"sthu", r(rs), d + "(" + r0(ra) + ")"};
    }

    protected static String[] lmw(InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int d = insn.D.get();
        return new String[]{"lmw", r(rt), d + "(" + r0(ra) + ")"};
    }

    protected static String[] stmw(InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int d = insn.D.get();
        return new String[]{"stmw", r(rs), d + "(" + r0(ra) + ")"};
    }

    protected static String[] lfs(InstructionFormat insn) {
        int frt = insn.FRT.get();
        int ra = insn.RA.get();
        int d = insn.D.get();
        return new String[]{"lfs", "f" + frt, d + "(" + r0(ra) + ")"};
    }

    protected static String[] lfsu(InstructionFormat insn) {
        int frt = insn.FRT.get();
        int ra = insn.RA.get();
        int d = insn.D.get();
        return new String[]{"lfsu", "f" + frt, d + "(" + r0(ra) + ")"};
    }

    protected static String[] lfd(InstructionFormat insn) {
        int frt = insn.FRT.get();
        int ra = insn.RA.get();
        int d = insn.D.get();
        return new String[]{"lfd", "f" + frt, d + "(" + r0(ra) + ")"};
    }

    protected static String[] lfdu(InstructionFormat insn) {
        int frt = insn.FRT.get();
        int ra = insn.RA.get();
        int d = insn.D.get();
        return new String[]{"lfdu", "f" + frt, d + "(r" + ra + ")"};
    }

    protected static String[] stfs(InstructionFormat insn) {
        int frs = insn.FRS.get();
        int ra = insn.RA.get();
        int d = insn.D.get();
        return new String[]{"stfs", "f" + frs, d + "(" + r0(ra) + ")"};
    }

    protected static String[] stfsu(InstructionFormat insn) {
        int frs = insn.FRS.get();
        int ra = insn.RA.get();
        int d = insn.D.get();
        return new String[]{"stfsu", "f" + frs, d + "(r" + ra + ")"};
    }

    protected static String[] stfd(InstructionFormat insn) {
        int frs = insn.FRS.get();
        int ra = insn.RA.get();
        int d = insn.D.get();
        return new String[]{"stfd", "f" + frs, d + "(" + r0(ra) + ")"};
    }

    protected static String[] stfdu(InstructionFormat insn) {
        int frs = insn.FRS.get();
        int ra = insn.RA.get();
        int d = insn.D.get();
        return new String[]{"stfdu", "f" + frs, d + "(" + r0(ra) + ")"};
    }

    protected static String[] fdivs(InstructionFormat insn) {
        int frt = insn.FRT.get();
        int fra = insn.FRA.get();
        int frb = insn.FRB.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"fdivs" + dot, "f" + frt, "f" + fra, "f" + frb};
    }

    protected static String[] fsubs(InstructionFormat insn) {
        int frt = insn.FRT.get();
        int fra = insn.FRA.get();
        int frb = insn.FRB.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"fsubs" + dot, "f" + frt, "f" + fra, "f" + frb};
    }

    protected static String[] fmsubs(InstructionFormat insn) {
        int frt = insn.FRT.get();
        int fra = insn.FRA.get();
        int frb = insn.FRB.get();
        int frc = insn.FRC.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"fmsubs" + dot, "f" + frt, "f" + fra, "f" + frc, "f" + frb};
    }

    protected static String[] fadds(InstructionFormat insn) {
        int frt = insn.FRT.get();
        int fra = insn.FRA.get();
        int frb = insn.FRB.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"fadds" + dot, "f" + frt, "f" + fra, "f" + frb};
    }

    protected static String[] fmuls(InstructionFormat insn) {
        int frt = insn.FRT.get();
        int fra = insn.FRA.get();
        int frc = insn.FRC.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"fmuls" + dot, "f" + frt, "f" + fra, "f" + frc};
    }

    protected static String[] fmadds(InstructionFormat insn) {
        int frt = insn.FRT.get();
        int fra = insn.FRA.get();
        int frb = insn.FRB.get();
        int frc = insn.FRC.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"fmadds" + dot, "f" + frt, "f" + fra, "f" + frc, "f" + frb};
    }

    protected static String[] fnmsubs(InstructionFormat insn) {
        int frt = insn.FRT.get();
        int fra = insn.FRA.get();
        int frb = insn.FRB.get();
        int frc = insn.FRC.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"fnmsubs" + dot, "f" + frt, "f" + fra, "f" + frc, "f" + frb};
    }

    protected static String[] fnmadds(InstructionFormat insn) {
        int frt = insn.FRT.get();
        int fra = insn.FRA.get();
        int frb = insn.FRB.get();
        int frc = insn.FRC.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"fnmadds" + dot, "f" + frt, "f" + fra, "f" + frc, "f" + frb};
    }

    protected static String[] xxlxor(InstructionFormat insn) {
        int t = insn.T.get();
        int a = insn.A.get();
        int b = insn.B.get();
        int ax = insn.AX.get() << 5;
        int bx = insn.BX.get() << 5;
        int tx = insn.TX.get() << 5;
        int xt = tx | t;
        int xa = ax | a;
        int xb = bx | b;
        return new String[]{"xxlxor", "vs" + xt, "vs" + xa, "vs" + xb};
    }

    protected static String[] frsp(InstructionFormat insn) {
        int frt = insn.FRT.get();
        int frb = insn.FRB.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"frsp" + dot, "f" + frt, "f" + frb};
    }

    protected static String[] fdiv(InstructionFormat insn) {
        int frt = insn.FRT.get();
        int fra = insn.FRA.get();
        int frb = insn.FRB.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"fdiv" + dot, "f" + frt, "f" + fra, "f" + frb};
    }

    protected static String[] fsub(InstructionFormat insn) {
        int frt = insn.FRT.get();
        int fra = insn.FRA.get();
        int frb = insn.FRB.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"fsub" + dot, "f" + frt, "f" + fra, "f" + frb};
    }

    protected static String[] fadd(InstructionFormat insn) {
        int frt = insn.FRT.get();
        int fra = insn.FRA.get();
        int frb = insn.FRB.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"fadd" + dot, "f" + frt, "f" + fra, "f" + frb};
    }

    protected static String[] fmul(InstructionFormat insn) {
        int frt = insn.FRT.get();
        int fra = insn.FRA.get();
        int frc = insn.FRC.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"fmul" + dot, "f" + frt, "f" + fra, "f" + frc};
    }

    protected static String[] fmsub(InstructionFormat insn) {
        int frt = insn.FRT.get();
        int fra = insn.FRA.get();
        int frb = insn.FRB.get();
        int frc = insn.FRC.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"fmsub" + dot, "f" + frt, "f" + fra, "f" + frc, "f" + frb};
    }

    protected static String[] fmadd(InstructionFormat insn) {
        int frt = insn.FRT.get();
        int fra = insn.FRA.get();
        int frb = insn.FRB.get();
        int frc = insn.FRC.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"fmadd" + dot, "f" + frt, "f" + fra, "f" + frc, "f" + frb};
    }

    protected static String[] fnmsub(InstructionFormat insn) {
        int frt = insn.FRT.get();
        int fra = insn.FRA.get();
        int frb = insn.FRB.get();
        int frc = insn.FRC.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"fnmsub" + dot, "f" + frt, "f" + fra, "f" + frc, "f" + frb};
    }

    protected static String[] fnmadd(InstructionFormat insn) {
        int frt = insn.FRT.get();
        int fra = insn.FRA.get();
        int frb = insn.FRB.get();
        int frc = insn.FRC.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"fnmadd" + dot, "f" + frt, "f" + fra, "f" + frc, "f" + frb};
    }

    protected static String[] fcmpu(InstructionFormat insn) {
        int bf = insn.X_BF.get();
        int fra = insn.FRA.get();
        int frb = insn.FRB.get();
        return new String[]{"fcmpu", "cr" + bf, "f" + fra, "f" + frb};
    }

    protected static String[] fctiwz(InstructionFormat insn) {
        int frt = insn.FRT.get();
        int frb = insn.FRB.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"fctiwz" + dot, "f" + frt, "f" + frb};
    }

    protected static String[] fneg(InstructionFormat insn) {
        int frt = insn.FRT.get();
        int frb = insn.FRB.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"fneg" + dot, "f" + frt, "f" + frb};
    }

    protected static String[] fmr(InstructionFormat insn) {
        int frt = insn.FRT.get();
        int frb = insn.FRB.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"fmr" + dot, "f" + frt, "f" + frb};
    }

    protected static String[] mtfsfi(InstructionFormat insn) {
        int bf = insn.XL_BF.get();
        boolean w = insn.W.getBit();
        int u = insn.U.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        if (!w) {
            return new String[]{"mtfsfi" + dot, Integer.toString(bf), Integer.toString(u)};
        } else {
            return new String[]{"mtfsfi" + dot, Integer.toString(bf), Integer.toString(u),
                            w ? "1" : "0"};
        }
    }

    protected static String[] fnabs(InstructionFormat insn) {
        int frt = insn.FRT.get();
        int frb = insn.FRB.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"fnabs" + dot, "f" + frt, "f" + frb};
    }

    protected static String[] fabs(InstructionFormat insn) {
        int frt = insn.FRT.get();
        int frb = insn.FRB.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"fabs" + dot, "f" + frt, "f" + frb};
    }

    protected static String[] mffs(InstructionFormat insn) {
        int frt = insn.FRT.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"mffs" + dot, "f" + frt};
    }

    protected static String[] mtfsf(InstructionFormat insn) {
        boolean l = insn.L.getBit();
        int flm = insn.XL_BF.get();
        boolean w = insn.W.getBit();
        int frb = insn.FRB.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        if (!w && !l) {
            return new String[]{"mtfsf" + dot, Integer.toString(flm), "f" + Integer.toString(frb)};
        } else {
            return new String[]{"mtfsf" + dot, Integer.toString(flm), "f" + Integer.toString(frb),
                            l ? "1" : "0", w ? "1" : "0"};
        }
    }

    protected static String[] fcfid(InstructionFormat insn) {
        int frt = insn.FRT.get();
        int frb = insn.FRB.get();
        boolean rc = insn.Rc.getBit();
        String dot = rc ? "." : "";
        return new String[]{"fcfid" + dot, "f" + frt, "f" + frb};
    }

    @Override
    public String[] getDisassembly(CodeReader code) {
        long pc = code.getPC();
        int insn = code.nextI32();
        return disassemble((int) pc, insn);
    }

    @Override
    public int getLength(CodeReader code) {
        return 4;
    }

    @Override
    public InstructionType getType(CodeReader code) {
        int insn = code.nextI32();
        int opcd = insnfmt.OPCD.get(insn);
        switch (opcd) {
            case Opcode.TWI:
                return InstructionType.SYSCALL;
            case Opcode.BC: {
                if (insnfmt.LK.getBit(insn)) {
                    return InstructionType.CALL;
                } else {
                    return InstructionType.JCC;
                }
            }
            case Opcode.B:
                if (insnfmt.LK.getBit(insn)) {
                    return InstructionType.CALL;
                } else {
                    return InstructionType.JMP;
                }
            case Opcode.CR_OPS:
                switch (insnfmt.XO_1.get(insn)) {
                    case Opcode.XO_BCLR: {
                        if (insnfmt.LK.getBit(insn)) {
                            // TODO: this is a RET+CALL in one instruction
                            return InstructionType.RET;
                        } else {
                            return InstructionType.RET;
                        }
                    }
                    case Opcode.XO_RFI:
                        return InstructionType.RTI;
                    case Opcode.XO_BCCTR: {
                        if (insnfmt.LK.getBit(insn)) {
                            return InstructionType.CALL;
                        } else {
                            return InstructionType.JCC;
                        }
                    }
                    default:
                        return InstructionType.OTHER;
                }
            case Opcode.SC:
                return InstructionType.SYSCALL;
            default:
                return InstructionType.OTHER;
        }
    }
}
