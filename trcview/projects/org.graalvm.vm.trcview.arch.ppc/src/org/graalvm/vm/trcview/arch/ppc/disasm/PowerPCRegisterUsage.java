package org.graalvm.vm.trcview.arch.ppc.disasm;

public class PowerPCRegisterUsage {
    public static final InstructionFormat insnfmt = new InstructionFormat();

    private static final int[] EMPTY = new int[0];

    public static int[] getRegisterUsage(int word, boolean read) {
        InstructionFormat insn = new InstructionFormat(word);
        switch (insn.OPCD.get()) {
            case Opcode.TWI:
                return twi(insn, read);
            case Opcode.MULLI:
                return mulli(insn, read);
            case Opcode.SUBFIC:
                return subfic(insn, read);
            case Opcode.CMPLI:
                return cmpli(insn, read);
            case Opcode.CMPI:
                return cmpi(insn, read);
            case Opcode.ADDIC:
                return addic(insn, read);
            case Opcode.ADDIC_:
                return addic_(insn, read);
            case Opcode.ADDI:
                return addi(insn, read);
            case Opcode.ADDIS:
                return addis(insn, read);
            case Opcode.BC:
            case Opcode.SC:
            case Opcode.B:
                return EMPTY;
            case Opcode.CR_OPS:
                switch (insn.XO_1.get()) {
                    case Opcode.XO_MCRF:
                    case Opcode.XO_BCLR:
                    case Opcode.XO_RFI:
                    case Opcode.XO_ISYNC:
                    case Opcode.XO_CRXOR:
                    case Opcode.XO_CREQV:
                    case Opcode.XO_CRORC:
                    case Opcode.XO_CROR:
                    case Opcode.XO_BCCTR:
                    default:
                        return EMPTY;
                }
            case Opcode.RLWIMI:
                return rlwimi(insn, read);
            case Opcode.RLWINM:
                return rlwinm(insn, read);
            case Opcode.RLWNM:
                return rlwnm(insn, read);
            case Opcode.ORI:
                return ori(insn, read);
            case Opcode.ORIS:
                return oris(insn, read);
            case Opcode.XORI:
                return xori(insn, read);
            case Opcode.XORIS:
                return xoris(insn, read);
            case Opcode.ANDI:
                return andi(insn, read);
            case Opcode.ANDIS:
                return andis(insn, read);
            case Opcode.FX_EXTENDED_OPS:
                switch (insn.XO_1.get()) {
                    case Opcode.XO_CMP:
                        return cmp(insn, read);
                    case Opcode.XO_MFCR:
                        return mfcr(insn, read);
                    case Opcode.XO_LWARX:
                        return lwarx(insn, read);
                    case Opcode.XO_LWZX:
                        return lwzx(insn, read);
                    case Opcode.XO_SLW:
                        return slw(insn, read);
                    case Opcode.XO_CNTLZW:
                        return cntlzw(insn, read);
                    case Opcode.XO_AND:
                        return and(insn, read);
                    case Opcode.XO_CMPL:
                        return cmpl(insn, read);
                    case Opcode.XO_DCBST:
                        return dcbst(insn, read);
                    case Opcode.XO_LWZUX:
                        return lwzux(insn, read);
                    case Opcode.XO_ANDC:
                        return andc(insn, read);
                    case Opcode.XO_MFMSR:
                        return mfmsr(insn, read);
                    case Opcode.XO_DCBF:
                        return dcbf(insn, read);
                    case Opcode.XO_LBZX:
                        return lbzx(insn, read);
                    case Opcode.XO_LVX:
                        return lvx(insn, read);
                    case Opcode.XO_LBZUX:
                        return lbzux(insn, read);
                    case Opcode.XO_NOR:
                        return nor(insn, read);
                    case Opcode.XO_MTCRF:
                        return mtcrf(insn, read);
                    case Opcode.XO_MTMSR:
                        return mtmsr(insn, read);
                    case Opcode.XO_STWCX_:
                        return stwcx_(insn, read);
                    case Opcode.XO_STWX:
                        return stwx(insn, read);
                    case Opcode.XO_STWUX:
                        return stwux(insn, read);
                    case Opcode.XO_STBX:
                        return stbx(insn, read);
                    case Opcode.XO_STVX:
                        return stvx(insn, read);
                    case Opcode.XO_DCBTST:
                        return dcbtst(insn, read);
                    case Opcode.XO_STBUX:
                        return stbux(insn, read);
                    case Opcode.XO_DCBT:
                        return dcbt(insn, read);
                    case Opcode.XO_LHZX:
                        return lhzx(insn, read);
                    case Opcode.XO_EQV:
                        return eqv(insn, read);
                    case Opcode.XO_LHZUX:
                        return lhzux(insn, read);
                    case Opcode.XO_XOR:
                        return xor(insn, read);
                    case Opcode.XO_MFSPR:
                        return mfspr(insn, read);
                    case Opcode.XO_LHAX:
                        return lhax(insn, read);
                    case Opcode.XO_MFTB:
                        return mftb(insn, read);
                    case Opcode.XO_STHX:
                        return sthx(insn, read);
                    case Opcode.XO_ORC:
                        return orc(insn, read);
                    case Opcode.XO_OR:
                        return or(insn, read);
                    case Opcode.XO_MTSPR:
                        return mtspr(insn, read);
                    case Opcode.XO_DCBI:
                        return dcbi(insn, read);
                    case Opcode.XO_LWBRX:
                        return lwbrx(insn, read);
                    case Opcode.XO_LFSX:
                        return lfsx(insn, read);
                    case Opcode.XO_SRW:
                        return srw(insn, read);
                    case Opcode.XO_LSWI:
                        return lswi(insn, read);
                    case Opcode.XO_SYNC:
                        return EMPTY;
                    case Opcode.XO_LFDX:
                        return lfdx(insn, read);
                    case Opcode.XO_STWBRX:
                        return stwbrx(insn, read);
                    case Opcode.XO_STFSX:
                        return stfsx(insn, read);
                    case Opcode.XO_STFSUX:
                        return stfsux(insn, read);
                    case Opcode.XO_STSWI:
                        return stswi(insn, read);
                    case Opcode.XO_STFDX:
                        return stfdx(insn, read);
                    case Opcode.XO_LHBRX:
                        return lhbrx(insn, read);
                    case Opcode.XO_SRAW:
                        return sraw(insn, read);
                    case Opcode.XO_SRAWI:
                        return srawi(insn, read);
                    case Opcode.XO_LFIWAX:
                        return lfiwax(insn, read);
                    case Opcode.XO_STHBRX:
                        return sthbrx(insn, read);
                    case Opcode.XO_EXTSH:
                        return extsh(insn, read);
                    case Opcode.XO_EXTSB:
                        return extsb(insn, read);
                    case Opcode.XO_ICBI:
                        return icbi(insn, read);
                    case Opcode.XO_DCBZ:
                        return dcbz(insn, read);
                }
                switch (insn.XO_2.get()) {
                    case Opcode.XO_SUBFC:
                        return subfc(insn, read);
                    case Opcode.XO_ADDC:
                        return addc(insn, read);
                    case Opcode.XO_MULHWU:
                        return mulhwu(insn, read);
                    case Opcode.XO_SUBF:
                        return subf(insn, read);
                    case Opcode.XO_MULHW:
                        return mulhw(insn, read);
                    case Opcode.XO_NEG:
                        return neg(insn, read);
                    case Opcode.XO_SUBFE:
                        return subfe(insn, read);
                    case Opcode.XO_ADDE:
                        return adde(insn, read);
                    case Opcode.XO_SUBFZE:
                        return subfze(insn, read);
                    case Opcode.XO_ADDZE:
                        return addze(insn, read);
                    case Opcode.XO_ADDME:
                        return addme(insn, read);
                    case Opcode.XO_MULLW:
                        return mullw(insn, read);
                    case Opcode.XO_ADD:
                        return add(insn, read);
                    case Opcode.XO_DIVWU:
                        return divwu(insn, read);
                    case Opcode.XO_NAND:
                        return nand(insn, read);
                    case Opcode.XO_DIVW:
                        return divw(insn, read);
                    default:
                        return EMPTY;
                }
            case Opcode.LWZ:
                return lwz(insn, read);
            case Opcode.LWZU:
                return lwzu(insn, read);
            case Opcode.LBZ:
                return lbz(insn, read);
            case Opcode.LBZU:
                return lbzu(insn, read);
            case Opcode.STW:
                return stw(insn, read);
            case Opcode.STWU:
                return stwu(insn, read);
            case Opcode.STB:
                return stb(insn, read);
            case Opcode.STBU:
                return stbu(insn, read);
            case Opcode.LHZ:
                return lhz(insn, read);
            case Opcode.LHZU:
                return lhzu(insn, read);
            case Opcode.LHA:
                return lha(insn, read);
            case Opcode.LHAU:
                return lhau(insn, read);
            case Opcode.STH:
                return sth(insn, read);
            case Opcode.STHU:
                return sthu(insn, read);
            case Opcode.LMW:
                return lmw(insn, read);
            case Opcode.STMW:
                return stmw(insn, read);
            case Opcode.LFS:
                return lfs(insn, read);
            case Opcode.LFSU:
                return lfsu(insn, read);
            case Opcode.LFD:
                return lfd(insn, read);
            case Opcode.LFDU:
                return lfdu(insn, read);
            case Opcode.STFS:
                return stfs(insn, read);
            case Opcode.STFSU:
                return stfsu(insn, read);
            case Opcode.STFD:
                return stfd(insn, read);
            case Opcode.STFDU:
                return stfdu(insn, read);
            case Opcode.FP_SINGLE_OPS:
                switch (insn.XO_6.get()) {
                    case Opcode.XO_FDIVS:
                    case Opcode.XO_FSUBS:
                    case Opcode.XO_FMSUBS:
                    case Opcode.XO_FADDS:
                    case Opcode.XO_FMULS:
                    case Opcode.XO_FMADDS:
                    case Opcode.XO_FNMSUBS:
                    case Opcode.XO_FNMADDS:
                    default:
                        return EMPTY;
                }
            case Opcode.VSX_EXTENDED_OPS:
                switch (insn.XXO.get()) {
                    case Opcode.XO_XXLXOR:
                    default:
                        return EMPTY;
                }
            case Opcode.FP_DOUBLE_OPS:
                switch (insn.XO_6.get()) {
                    case Opcode.XO_FRSP:
                    case Opcode.XO_FDIV:
                    case Opcode.XO_FSUB:
                    case Opcode.XO_FADD:
                    case Opcode.XO_FMUL:
                    case Opcode.XO_FMSUB:
                    case Opcode.XO_FMADD:
                    case Opcode.XO_FNMSUB:
                    case Opcode.XO_FNMADD:
                        return EMPTY;
                }
                switch (insn.XO_1.get()) {
                    case Opcode.XO_FCMPU:
                    case Opcode.XO_FCTIWZ:
                    case Opcode.XO_FNEG:
                    case Opcode.XO_FMR:
                    case Opcode.XO_MTFSFI:
                    case Opcode.XO_FNABS:
                    case Opcode.XO_FABS:
                    case Opcode.XO_MFFS:
                    case Opcode.XO_MTFSF:
                    case Opcode.XO_FCFID:
                    default:
                        return EMPTY;
                }
        }
        return EMPTY;
    }

    protected static int[] r0(int r) {
        if (r == 0) {
            return EMPTY;
        } else {
            return r(r);
        }
    }

    protected static int[] r0(int r0, int r) {
        if (r0 == 0) {
            return r(r);
        } else {
            return r(r0, r);
        }
    }

    protected static int[] r0(int r0, int ra, int rb) {
        if (r0 == 0) {
            return r(ra, rb);
        } else {
            return r(r0, ra, rb);
        }
    }

    protected static int[] r(int r) {
        return new int[]{r};
    }

    protected static int[] r(int r1, int r2) {
        return new int[]{r1, r2};
    }

    protected static int[] r(int r1, int r2, int r3) {
        return new int[]{r1, r2, r3};
    }

    protected static int[] reg(int dst, int src, boolean read) {
        if (read) {
            return r(src);
        } else {
            return r(dst);
        }
    }

    protected static int[] reg(int dst, int src1, int src2, boolean read) {
        if (read) {
            return r(src1, src2);
        } else {
            return r(dst);
        }
    }

    protected static int[] reg0(int dst, int a0, boolean read) {
        if (read) {
            if (a0 == 0) {
                return EMPTY;
            } else {
                return r(a0);
            }
        } else {
            return r(dst);
        }
    }

    protected static int[] reg0(int dst, int a0, int b, boolean read) {
        if (read) {
            if (a0 == 0) {
                return r(b);
            } else {
                return r(a0, b);
            }
        } else {
            return r(dst);
        }
    }

    protected static int[] twi(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        if (read) {
            return r(ra);
        } else {
            return EMPTY;
        }
    }

    protected static int[] mulli(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        return reg(rt, ra, read);
    }

    protected static int[] subfic(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        return reg(rt, ra, read);
    }

    protected static int[] cmpli(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        if (read) {
            return r(ra);
        } else {
            return EMPTY;
        }
    }

    protected static int[] cmpi(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        if (read) {
            return r(ra);
        } else {
            return EMPTY;
        }
    }

    protected static int[] addic(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        return reg(rt, ra, read);
    }

    protected static int[] addic_(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        return reg(rt, ra, read);
    }

    protected static int[] addi(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        if (read) {
            return r0(ra);
        } else {
            return r(rt);
        }
    }

    protected static int[] addis(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        if (read) {
            return r0(ra);
        } else {
            return r(rt);
        }
    }

    protected static int[] rlwimi(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        return reg(ra, rs, read);
    }

    protected static int[] rlwinm(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        return reg(ra, rs, read);
    }

    protected static int[] rlwnm(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return reg(ra, rs, rb, read);
    }

    protected static int[] ori(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int ui = insn.UI.get();
        if (rs == 0 && ra == 0 && ui == 0) {
            return EMPTY; // nop
        } else {
            return reg(ra, rs, read);
        }
    }

    protected static int[] oris(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        return reg(ra, rs, read);
    }

    protected static int[] xori(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int ui = insn.UI.get();
        if (rs == 0 && ra == 0 && ui == 0) {
            return EMPTY; // xnop
        } else {
            return reg(ra, rs, read);
        }
    }

    protected static int[] xoris(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        return reg(ra, rs, read);
    }

    protected static int[] andi(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        return reg(ra, rs, read);
    }

    protected static int[] andis(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        return reg(ra, rs, read);
    }

    protected static int[] cmp(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        if (read) {
            return r(ra, rb);
        } else {
            return EMPTY;
        }
    }

    protected static int[] mfcr(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        if (read) {
            return EMPTY;
        } else {
            return r(rt);
        }
    }

    protected static int[] lwarx(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return reg0(rt, ra, rb, read);
    }

    protected static int[] lwzx(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return reg0(rt, ra, rb, read);
    }

    protected static int[] slw(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return reg(ra, rs, rb, read);
    }

    protected static int[] cntlzw(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        return reg(ra, rs, read);
    }

    protected static int[] and(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return reg(ra, rs, rb, read);
    }

    protected static int[] cmpl(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        if (read) {
            return r(ra, rb);
        } else {
            return EMPTY;
        }
    }

    protected static int[] dcbst(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        if (read) {
            return r0(ra, rb);
        } else {
            return EMPTY;
        }
    }

    protected static int[] lwzux(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return reg(rt, ra, rb, read);
    }

    protected static int[] andc(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return reg(ra, rs, rb, read);
    }

    protected static int[] mfmsr(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        if (read) {
            return EMPTY;
        } else {
            return r(rt);
        }
    }

    protected static int[] dcbf(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        if (read) {
            return r0(ra, rb);
        } else {
            return EMPTY;
        }
    }

    protected static int[] lbzx(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return reg0(rt, ra, rb, read);
    }

    protected static int[] lvx(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        if (read) {
            return r0(ra, rb);
        } else {
            return EMPTY;
        }
    }

    protected static int[] lbzux(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return reg(rt, ra, rb, read);
    }

    protected static int[] nor(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return reg(ra, rs, rb, read);
    }

    protected static int[] mtcrf(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        boolean one = insn.BIT_11.getBit();
        if (one) {
            return EMPTY;
        } else {
            if (read) {
                return r(rs);
            } else {
                return EMPTY;
            }
        }
    }

    protected static int[] mtmsr(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        if (read) {
            return r(rs);
        } else {
            return EMPTY;
        }
    }

    protected static int[] stwcx_(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        if (read) {
            return r0(ra, rs, rb);
        } else {
            return EMPTY;
        }
    }

    protected static int[] stwx(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        if (read) {
            return r0(ra, rs, rb);
        } else {
            return EMPTY;
        }
    }

    protected static int[] stwux(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        if (read) {
            return r0(ra, rs, rb);
        } else {
            return r(ra);
        }
    }

    protected static int[] stbx(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        if (read) {
            return r0(ra, rs, rb);
        } else {
            return EMPTY;
        }
    }

    protected static int[] stvx(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        if (read) {
            return r0(ra, rb);
        } else {
            return EMPTY;
        }
    }

    protected static int[] dcbtst(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        if (read) {
            return r0(ra, rb);
        } else {
            return EMPTY;
        }
    }

    protected static int[] stbux(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        if (read) {
            return r0(ra, rs, rb);
        } else {
            return r(ra);
        }
    }

    protected static int[] dcbt(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        if (read) {
            return r0(ra, rb);
        } else {
            return EMPTY;
        }
    }

    protected static int[] lhzx(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return reg0(rt, ra, rb, read);
    }

    protected static int[] eqv(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return reg(ra, rs, rb, read);
    }

    protected static int[] lhzux(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        if (read) {
            return r0(ra, rb);
        } else {
            return r(rt, ra);
        }
    }

    protected static int[] xor(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return reg(ra, rs, rb, read);
    }

    protected static int[] mfspr(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        if (read) {
            return EMPTY;
        } else {
            return r(rt);
        }
    }

    protected static int[] lhax(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return reg0(rt, ra, rb, read);
    }

    protected static int[] mftb(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        if (read) {
            return EMPTY;
        } else {
            return r(rt);
        }
    }

    protected static int[] sthx(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        if (read) {
            return r0(ra, rs, rb);
        } else {
            return EMPTY;
        }
    }

    protected static int[] orc(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return reg(ra, rs, rb, read);
    }

    protected static int[] or(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        if (rs == rb) {
            return reg(ra, rs, read); // mr
        } else {
            return reg(ra, rs, rb, read); // or
        }
    }

    protected static int[] mtspr(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        if (read) {
            return r(rs);
        } else {
            return EMPTY;
        }
    }

    protected static int[] dcbi(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        if (read) {
            return r0(ra, rb);
        } else {
            return EMPTY;
        }
    }

    protected static int[] lwbrx(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return reg0(rt, ra, rb, read);
    }

    protected static int[] lfsx(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        if (read) {
            return r0(ra, rb);
        } else {
            return EMPTY;
        }
    }

    protected static int[] srw(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return reg(ra, rs, rb, read);
    }

    protected static int[] lswi(InstructionFormat insn, boolean read) {
        // int rt = insn.RT.get();
        int ra = insn.RA.get();
        // int nb = insn.NB.get();
        // TODO: implement accurately
        if (read) {
            return r0(ra);
        } else {
            return EMPTY;
        }
    }

    protected static int[] lfdx(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        if (read) {
            return r0(ra, rb);
        } else {
            return EMPTY;
        }
    }

    protected static int[] stwbrx(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        if (read) {
            return r0(ra, rb, rs);
        } else {
            return EMPTY;
        }
    }

    protected static int[] stfsx(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        if (read) {
            return r0(ra, rb);
        } else {
            return EMPTY;
        }
    }

    protected static int[] stfsux(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        if (read) {
            return r0(ra, rb);
        } else {
            return r(ra);
        }
    }

    protected static int[] stswi(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        // int nb = insn.NB.get();
        // TODO: implement accurately
        if (read) {
            return r0(ra, rs);
        } else {
            return EMPTY;
        }
    }

    protected static int[] stfdx(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        if (read) {
            return r0(ra, rb);
        } else {
            return EMPTY;
        }
    }

    protected static int[] lhbrx(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return reg0(rt, ra, rb, read);
    }

    protected static int[] sraw(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return reg(ra, rs, rb, read);
    }

    protected static int[] srawi(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        return reg(ra, rs, read);
    }

    protected static int[] lfiwax(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        if (read) {
            return r0(ra, rb);
        } else {
            return EMPTY;
        }
    }

    protected static int[] sthbrx(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        if (read) {
            return r0(ra, rb, rs);
        } else {
            return EMPTY;
        }
    }

    protected static int[] extsh(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        return reg(ra, rs, read);
    }

    protected static int[] extsb(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        return reg(ra, rs, read);
    }

    protected static int[] icbi(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        if (read) {
            return r0(ra, rb);
        } else {
            return EMPTY;
        }
    }

    protected static int[] dcbz(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        if (read) {
            return r0(ra, rb);
        } else {
            return EMPTY;
        }
    }

    protected static int[] subfc(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        int rt = insn.RT.get();
        return reg(rt, ra, rb, read);
    }

    protected static int[] addc(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        int rt = insn.RT.get();
        return reg(rt, ra, rb, read);
    }

    protected static int[] mulhwu(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return reg(rt, ra, rb, read);
    }

    protected static int[] subf(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        int rt = insn.RT.get();
        return reg(rt, ra, rb, read);
    }

    protected static int[] mulhw(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return reg(rt, ra, rb, read);
    }

    protected static int[] neg(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        int rt = insn.RT.get();
        return reg(rt, ra, read);
    }

    protected static int[] subfe(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        int rt = insn.RT.get();
        return reg(rt, ra, rb, read);
    }

    protected static int[] adde(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        int rt = insn.RT.get();
        return reg(rt, ra, rb, read);
    }

    protected static int[] subfze(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        return reg(rt, ra, read);
    }

    protected static int[] addze(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        return reg(rt, ra, read);
    }

    protected static int[] addme(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        return reg(rt, ra, read);
    }

    protected static int[] mullw(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return reg(rt, ra, rb, read);
    }

    protected static int[] add(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        int rt = insn.RT.get();
        return reg(rt, ra, rb, read);
    }

    protected static int[] divwu(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return reg(rt, ra, rb, read);
    }

    protected static int[] nand(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return reg(ra, rs, rb, read);
    }

    protected static int[] divw(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        return reg(rt, ra, rb, read);
    }

    protected static int[] lwz(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        return reg0(rt, ra, read);
    }

    protected static int[] lwzu(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        if (read) {
            return r0(ra);
        } else {
            return r(rt, ra);
        }
    }

    protected static int[] lbz(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        return reg0(rt, ra, read);
    }

    protected static int[] lbzu(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        if (read) {
            return r0(ra);
        } else {
            return r(rt, ra);
        }
    }

    protected static int[] stw(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        if (read) {
            return r0(ra, rs);
        } else {
            return EMPTY;
        }
    }

    protected static int[] stwu(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        return reg0(ra, ra, rs, read);
    }

    protected static int[] stb(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        if (read) {
            return r0(ra, rs);
        } else {
            return EMPTY;
        }
    }

    protected static int[] stbu(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        return reg0(ra, ra, rs, read);
    }

    protected static int[] lhz(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        return reg0(rt, ra, read);
    }

    protected static int[] lhzu(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        if (read) {
            return r0(ra);
        } else {
            return r(rt, ra);
        }
    }

    protected static int[] lha(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        return reg0(rt, ra, read);
    }

    protected static int[] lhau(InstructionFormat insn, boolean read) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        if (read) {
            return r0(ra);
        } else {
            return r(rt, ra);
        }
    }

    protected static int[] sth(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        if (read) {
            return r0(ra, rs);
        } else {
            return EMPTY;
        }
    }

    protected static int[] sthu(InstructionFormat insn, boolean read) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        if (read) {
            return r0(ra, rs);
        } else {
            return r(ra);
        }
    }

    protected static int[] lmw(InstructionFormat insn, boolean read) {
        // int rt = insn.RT.get();
        int ra = insn.RA.get();
        // int d = insn.D.get();
        // TODO: implement accurately
        if (read) {
            return r0(ra);
        } else {
            return EMPTY;
        }
    }

    protected static int[] stmw(InstructionFormat insn, boolean read) {
        // int rs = insn.RS.get();
        int ra = insn.RA.get();
        // int d = insn.D.get();
        // TODO: implement accurately
        if (read) {
            return r0(ra);
        } else {
            return EMPTY;
        }
    }

    protected static int[] lfs(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        if (read) {
            return r0(ra);
        } else {
            return EMPTY;
        }
    }

    protected static int[] lfsu(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        return reg0(ra, ra, read);
    }

    protected static int[] lfd(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        if (read) {
            return r0(ra);
        } else {
            return EMPTY;
        }
    }

    protected static int[] lfdu(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        return reg0(ra, ra, read);
    }

    protected static int[] stfs(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        if (read) {
            return r0(ra);
        } else {
            return EMPTY;
        }
    }

    protected static int[] stfsu(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        return reg0(ra, ra, read);
    }

    protected static int[] stfd(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        if (read) {
            return r0(ra);
        } else {
            return EMPTY;
        }
    }

    protected static int[] stfdu(InstructionFormat insn, boolean read) {
        int ra = insn.RA.get();
        return reg0(ra, ra, read);
    }
}
