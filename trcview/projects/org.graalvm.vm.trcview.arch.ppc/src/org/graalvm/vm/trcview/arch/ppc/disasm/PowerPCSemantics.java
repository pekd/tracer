package org.graalvm.vm.trcview.arch.ppc.disasm;

import org.graalvm.vm.trcview.data.Semantics;
import org.graalvm.vm.trcview.data.ir.ConstOperand;
import org.graalvm.vm.trcview.data.ir.IndexedMemoryOperand;
import org.graalvm.vm.trcview.data.ir.MemoryOperand;
import org.graalvm.vm.trcview.data.ir.Operand;
import org.graalvm.vm.trcview.data.ir.RegisterOperand;
import org.graalvm.vm.trcview.data.type.VariableType;

public class PowerPCSemantics {
    public static final InstructionFormat insnfmt = new InstructionFormat();

    public static void getSemantics(Semantics semantics, int word) {
        InstructionFormat insn = new InstructionFormat(word);
        switch (insn.OPCD.get()) {
            case Opcode.TWI:
                twi(semantics, insn);
                return;
            case Opcode.MULLI:
                mulli(semantics, insn);
                return;
            case Opcode.SUBFIC:
                subfic(semantics, insn);
                return;
            case Opcode.CMPLI:
                cmpli(semantics, insn);
                return;
            case Opcode.CMPI:
                cmpi(semantics, insn);
                return;
            case Opcode.ADDIC:
                addic(semantics, insn);
                return;
            case Opcode.ADDIC_:
                addic_(semantics, insn);
                return;
            case Opcode.ADDI:
                addi(semantics, insn);
                return;
            case Opcode.ADDIS:
                addis(semantics, insn);
                return;
            case Opcode.BC:
                // bc(pc, insn);
                return;
            case Opcode.SC:
                // sc(insn);
                return;
            case Opcode.B:
                // b(pc, insn);
                return;
            case Opcode.CR_OPS:
                switch (insn.XO_1.get()) {
                    case Opcode.XO_MCRF:
                        // mcrf(insn);
                        return;
                    case Opcode.XO_BCLR:
                        // return bclr(insn);
                        return;
                    case Opcode.XO_RFI:
                        // return rfi();
                        return;
                    case Opcode.XO_ISYNC:
                        // return isync();
                        return;
                    case Opcode.XO_CRXOR:
                        // return crxor(insn);
                        return;
                    case Opcode.XO_CREQV:
                        // return creqv(insn);
                        return;
                    case Opcode.XO_CRORC:
                        // return crorc(insn);
                        return;
                    case Opcode.XO_CROR:
                        // return cror(insn);
                        return;
                    case Opcode.XO_BCCTR:
                        // return bcctr(insn);
                        return;
                    default:
                        return;
                }
            case Opcode.RLWIMI:
                rlwimi(semantics, insn);
                return;
            case Opcode.RLWINM:
                rlwinm(semantics, insn);
                return;
            case Opcode.RLWNM:
                rlwnm(semantics, insn);
                return;
            case Opcode.ORI:
                ori(semantics, insn);
                return;
            case Opcode.ORIS:
                oris(semantics, insn);
                return;
            case Opcode.XORI:
                xori(semantics, insn);
                return;
            case Opcode.XORIS:
                xoris(semantics, insn);
                return;
            case Opcode.ANDI:
                andi(semantics, insn);
                return;
            case Opcode.ANDIS:
                andis(semantics, insn);
                return;
            case Opcode.FX_EXTENDED_OPS:
                switch (insn.XO_1.get()) {
                    case Opcode.XO_CMP:
                        cmp(semantics, insn);
                        return;
                    case Opcode.XO_MFCR:
                        mfcr(semantics, insn);
                        return;
                    case Opcode.XO_LWARX:
                        lwarx(semantics, insn);
                        return;
                    case Opcode.XO_LWZX:
                        lwzx(semantics, insn);
                        return;
                    case Opcode.XO_SLW:
                        slw(semantics, insn);
                        return;
                    case Opcode.XO_CNTLZW:
                        cntlzw(semantics, insn);
                        return;
                    case Opcode.XO_AND:
                        and(semantics, insn);
                        return;
                    case Opcode.XO_CMPL:
                        cmpl(semantics, insn);
                        return;
                    case Opcode.XO_DCBST:
                        dcbst(semantics, insn);
                        return;
                    case Opcode.XO_LWZUX:
                        lwzux(semantics, insn);
                        return;
                    case Opcode.XO_ANDC:
                        andc(semantics, insn);
                        return;
                    case Opcode.XO_MFMSR:
                        mfmsr(semantics, insn);
                        return;
                    case Opcode.XO_DCBF:
                        dcbf(semantics, insn);
                        return;
                    case Opcode.XO_LBZX:
                        lbzx(semantics, insn);
                        return;
                    case Opcode.XO_LVX:
                        lvx(semantics, insn);
                        return;
                    case Opcode.XO_LBZUX:
                        lbzux(semantics, insn);
                        return;
                    case Opcode.XO_NOR:
                        nor(semantics, insn);
                        return;
                    case Opcode.XO_MTCRF:
                        mtcrf(semantics, insn);
                        return;
                    case Opcode.XO_MTMSR:
                        mtmsr(semantics, insn);
                        return;
                    case Opcode.XO_STWCX_:
                        stwcx_(semantics, insn);
                        return;
                    case Opcode.XO_STWX:
                        stwx(semantics, insn);
                        return;
                    case Opcode.XO_STWUX:
                        stwux(semantics, insn);
                        return;
                    case Opcode.XO_STBX:
                        stbx(semantics, insn);
                        return;
                    case Opcode.XO_STVX:
                        stvx(semantics, insn);
                        return;
                    case Opcode.XO_DCBTST:
                        dcbtst(semantics, insn);
                        return;
                    case Opcode.XO_STBUX:
                        stbux(semantics, insn);
                        return;
                    case Opcode.XO_DCBT:
                        dcbt(semantics, insn);
                        return;
                    case Opcode.XO_LHZX:
                        lhzx(semantics, insn);
                        return;
                    case Opcode.XO_EQV:
                        eqv(semantics, insn);
                        return;
                    case Opcode.XO_LHZUX:
                        lhzux(semantics, insn);
                        return;
                    case Opcode.XO_XOR:
                        xor(semantics, insn);
                        return;
                    case Opcode.XO_MFSPR:
                        mfspr(semantics, insn);
                        return;
                    case Opcode.XO_LHAX:
                        lhax(semantics, insn);
                        return;
                    case Opcode.XO_MFTB:
                        mftb(semantics, insn);
                        return;
                    case Opcode.XO_STHX:
                        sthx(semantics, insn);
                        return;
                    case Opcode.XO_ORC:
                        orc(semantics, insn);
                        return;
                    case Opcode.XO_OR:
                        or(semantics, insn);
                        return;
                    case Opcode.XO_MTSPR:
                        mtspr(semantics, insn);
                        return;
                    case Opcode.XO_DCBI:
                        dcbi(semantics, insn);
                        return;
                    case Opcode.XO_LWBRX:
                        lwbrx(semantics, insn);
                        return;
                    case Opcode.XO_LFSX:
                        lfsx(semantics, insn);
                        return;
                    case Opcode.XO_SRW:
                        srw(semantics, insn);
                        return;
                    case Opcode.XO_LSWI:
                        lswi(semantics, insn);
                        return;
                    case Opcode.XO_SYNC:
                        // sync(semantics, insn);
                        return;
                    case Opcode.XO_LFDX:
                        lfdx(semantics, insn);
                        return;
                    case Opcode.XO_STWBRX:
                        stwbrx(semantics, insn);
                        return;
                    case Opcode.XO_STFSX:
                        stfsx(semantics, insn);
                        return;
                    case Opcode.XO_STFSUX:
                        stfsux(semantics, insn);
                        return;
                    case Opcode.XO_STSWI:
                        stswi(semantics, insn);
                        return;
                    case Opcode.XO_STFDX:
                        stfdx(semantics, insn);
                        return;
                    case Opcode.XO_LHBRX:
                        lhbrx(semantics, insn);
                        return;
                    case Opcode.XO_SRAW:
                        sraw(semantics, insn);
                        return;
                    case Opcode.XO_SRAWI:
                        srawi(semantics, insn);
                        return;
                    case Opcode.XO_LFIWAX:
                        lfiwax(semantics, insn);
                        return;
                    case Opcode.XO_STHBRX:
                        sthbrx(semantics, insn);
                        return;
                    case Opcode.XO_EXTSH:
                        extsh(semantics, insn);
                        return;
                    case Opcode.XO_EXTSB:
                        extsb(semantics, insn);
                        return;
                    case Opcode.XO_ICBI:
                        icbi(semantics, insn);
                        return;
                    case Opcode.XO_DCBZ:
                        dcbz(semantics, insn);
                        return;
                }
                switch (insn.XO_2.get()) {
                    case Opcode.XO_SUBFC:
                        subfc(semantics, insn);
                        return;
                    case Opcode.XO_ADDC:
                        addc(semantics, insn);
                        return;
                    case Opcode.XO_MULHWU:
                        mulhwu(semantics, insn);
                        return;
                    case Opcode.XO_SUBF:
                        subf(semantics, insn);
                        return;
                    case Opcode.XO_MULHW:
                        mulhw(semantics, insn);
                        return;
                    case Opcode.XO_NEG:
                        neg(semantics, insn);
                        return;
                    case Opcode.XO_SUBFE:
                        subfe(semantics, insn);
                        return;
                    case Opcode.XO_ADDE:
                        adde(semantics, insn);
                        return;
                    case Opcode.XO_SUBFZE:
                        subfze(semantics, insn);
                        return;
                    case Opcode.XO_ADDZE:
                        addze(semantics, insn);
                        return;
                    case Opcode.XO_ADDME:
                        addme(semantics, insn);
                        return;
                    case Opcode.XO_MULLW:
                        mullw(semantics, insn);
                        return;
                    case Opcode.XO_ADD:
                        add(semantics, insn);
                        return;
                    case Opcode.XO_DIVWU:
                        divwu(semantics, insn);
                        return;
                    case Opcode.XO_NAND:
                        nand(semantics, insn);
                        return;
                    case Opcode.XO_DIVW:
                        divw(semantics, insn);
                        return;
                    default:
                        return;
                }
            case Opcode.LWZ:
                lwz(semantics, insn);
                return;
            case Opcode.LWZU:
                lwzu(semantics, insn);
                return;
            case Opcode.LBZ:
                lbz(semantics, insn);
                return;
            case Opcode.LBZU:
                lbzu(semantics, insn);
                return;
            case Opcode.STW:
                stw(semantics, insn);
                return;
            case Opcode.STWU:
                stwu(semantics, insn);
                return;
            case Opcode.STB:
                stb(semantics, insn);
                return;
            case Opcode.STBU:
                stbu(semantics, insn);
                return;
            case Opcode.LHZ:
                lhz(semantics, insn);
                return;
            case Opcode.LHZU:
                lhzu(semantics, insn);
                return;
            case Opcode.LHA:
                lha(semantics, insn);
                return;
            case Opcode.LHAU:
                lhau(semantics, insn);
                return;
            case Opcode.STH:
                sth(semantics, insn);
                return;
            case Opcode.STHU:
                sthu(semantics, insn);
                return;
            case Opcode.LMW:
                lmw(semantics, insn);
                return;
            case Opcode.STMW:
                stmw(semantics, insn);
                return;
            case Opcode.LFS:
                lfs(semantics, insn);
                return;
            case Opcode.LFSU:
                lfsu(semantics, insn);
                return;
            case Opcode.LFD:
                lfd(semantics, insn);
                return;
            case Opcode.LFDU:
                lfdu(semantics, insn);
                return;
            case Opcode.STFS:
                stfs(semantics, insn);
                return;
            case Opcode.STFSU:
                stfsu(semantics, insn);
                return;
            case Opcode.STFD:
                stfd(semantics, insn);
                return;
            case Opcode.STFDU:
                stfdu(semantics, insn);
                return;
            case Opcode.FP_SINGLE_OPS:
                switch (insn.XO_6.get()) {
                    case Opcode.XO_FDIVS:
                        // return fdivs(insn);
                    case Opcode.XO_FSUBS:
                        // return fsubs(insn);
                    case Opcode.XO_FMSUBS:
                        // return fmsubs(insn);
                    case Opcode.XO_FADDS:
                        // return fadds(insn);
                    case Opcode.XO_FMULS:
                        // return fmuls(insn);
                    case Opcode.XO_FMADDS:
                        // return fmadds(insn);
                    case Opcode.XO_FNMSUBS:
                        // return fnmsubs(insn);
                    case Opcode.XO_FNMADDS:
                        // return fnmadds(insn);
                    default:
                        return;
                }
            case Opcode.VSX_EXTENDED_OPS:
                switch (insn.XXO.get()) {
                    case Opcode.XO_XXLXOR:
                        // return xxlxor(insn);
                    default:
                        return;
                }
            case Opcode.FP_DOUBLE_OPS:
                switch (insn.XO_6.get()) {
                    case Opcode.XO_FRSP:
                        // return frsp(insn);
                    case Opcode.XO_FDIV:
                        // return fdiv(insn);
                    case Opcode.XO_FSUB:
                        // return fsub(insn);
                    case Opcode.XO_FADD:
                        // return fadd(insn);
                    case Opcode.XO_FMUL:
                        // return fmul(insn);
                    case Opcode.XO_FMSUB:
                        // return fmsub(insn);
                    case Opcode.XO_FMADD:
                        // return fmadd(insn);
                    case Opcode.XO_FNMSUB:
                        // return fnmsub(insn);
                    case Opcode.XO_FNMADD:
                        // return fnmadd(insn);
                }
                switch (insn.XO_1.get()) {
                    case Opcode.XO_FCMPU:
                        // return fcmpu(insn);
                    case Opcode.XO_FCTIWZ:
                        // return fctiwz(insn);
                    case Opcode.XO_FNEG:
                        // return fneg(insn);
                    case Opcode.XO_FMR:
                        // return fmr(insn);
                    case Opcode.XO_MTFSFI:
                        // return mtfsfi(insn);
                    case Opcode.XO_FNABS:
                        // return fnabs(insn);
                    case Opcode.XO_FABS:
                        // return fabs(insn);
                    case Opcode.XO_MFFS:
                        // return mffs(insn);
                    case Opcode.XO_MTFSF:
                        // return mtfsf(insn);
                    case Opcode.XO_FCFID:
                        // return fcfid(insn);
                    default:
                        return;
                }
        }
    }

    protected static Operand r0(int r) {
        if (r == 0) {
            return new ConstOperand();
        } else {
            return r(r);
        }
    }

    protected static Operand r(int r) {
        return new RegisterOperand(r);
    }

    protected static Operand mem(Semantics semantics, int ra0, int rb, VariableType type) {
        Operand mem;
        if (ra0 == 0) {
            mem = new IndexedMemoryOperand(rb);
            semantics.constraint(r(rb), type);
        } else {
            mem = new IndexedMemoryOperand(ra0, rb);
            semantics.constraint(r(ra0), VariableType.I32);
            semantics.constraint(r(rb), VariableType.I32);
        }
        return mem;
    }

    // keep type argument for documentation purposes for now
    protected static Operand mem2(Semantics semantics, int ra, int rb, @SuppressWarnings("unused") VariableType type) {
        Operand mem = new IndexedMemoryOperand(ra, rb);
        semantics.constraint(r(ra), VariableType.I32);
        semantics.constraint(r(rb), VariableType.I32);
        return mem;
    }

    protected static Operand memOff(Semantics semantics, int ra0, long off, VariableType type) {
        Operand mem;
        if (ra0 == 0) {
            mem = new MemoryOperand(off);
        } else {
            mem = new IndexedMemoryOperand(ra0, off);
            semantics.constraint(r(ra0), VariableType.I32);
            if (off == 0) {
                semantics.constraint(r(ra0), type);
            }
        }
        return mem;
    }

    protected static void twi(Semantics semantics, InstructionFormat insn) {
        int ra = insn.RA.get();
        semantics.constraint(r(ra), VariableType.S32);
    }

    protected static void mulli(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        semantics.constraint(r(ra), VariableType.S32);
        semantics.constraint(r(rt), VariableType.S32);
        semantics.arithmetic(r(rt), true);
    }

    protected static void subfic(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        semantics.constraint(r(ra), VariableType.I32);
        semantics.constraint(r(rt), VariableType.I32);
        semantics.arithmetic(r(rt), false);
    }

    protected static void cmpli(Semantics semantics, InstructionFormat insn) {
        boolean l = insn.L.getBit();
        int ra = insn.RA.get();
        if (!l) {
            semantics.constraint(r(ra), VariableType.U32);
        } else {
            semantics.constraint(r(ra), VariableType.U64);
        }
    }

    protected static void cmpi(Semantics semantics, InstructionFormat insn) {
        boolean l = insn.L.getBit();
        int ra = insn.RA.get();
        if (!l) {
            semantics.constraint(r(ra), VariableType.S32);
        } else {
            semantics.constraint(r(ra), VariableType.S64);
        }
    }

    protected static void addic(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        semantics.constraint(r(ra), VariableType.S32);
        semantics.move(r(rt), r(ra));
        semantics.arithmetic(r(rt), false);
    }

    protected static void addic_(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        semantics.constraint(r(ra), VariableType.S32);
        semantics.move(r(rt), r(ra));
        semantics.arithmetic(r(rt), false);
    }

    protected static void addi(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        if (ra == 0) {
            semantics.move(r(rt), new ConstOperand());
            semantics.constraint(r(rt), VariableType.S32);
        } else {
            semantics.constraint(r0(ra), VariableType.S32);
            semantics.move(r(rt), r0(ra));
            semantics.arithmetic(r(rt), false);
        }
    }

    protected static void addis(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        if (ra == 0) {
            semantics.move(r(rt), new ConstOperand());
            semantics.constraint(r(rt), VariableType.S32);
        } else {
            semantics.move(r(rt), r0(ra));
            semantics.constraint(r(rt), VariableType.S32);
            semantics.arithmetic(r(rt), false);
        }
    }

    protected static void rlwimi(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        semantics.constraint(r(ra), VariableType.I32);
        semantics.constraint(r(rs), VariableType.I32);
    }

    protected static void rlwinm(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        semantics.constraint(r(ra), VariableType.I32);
        semantics.constraint(r(rs), VariableType.I32);
    }

    protected static void rlwnm(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        semantics.constraint(r(ra), VariableType.I32);
        semantics.constraint(r(rs), VariableType.I32);
    }

    protected static void ori(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int ui = insn.UI.get();
        if (rs == 0 && ra == 0 && ui == 0) {
            // nop
        } else {
            semantics.constraint(r(rs), VariableType.I32);
            semantics.move(r(ra), r(rs));
        }
    }

    protected static void oris(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        semantics.constraint(r(rs), VariableType.I32);
        semantics.move(r(ra), r(rs));
    }

    protected static void xori(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int ui = insn.UI.get();
        if (rs == 0 && ra == 0 && ui == 0) {
            // xnop
        } else {
            semantics.constraint(r(rs), VariableType.I32);
            semantics.move(r(ra), r(rs));
        }
    }

    protected static void xoris(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int ui = insn.UI.get() << 16;
        if (rs == 0 && ra == 0 && ui == 0) {
            // xnop
        } else {
            semantics.constraint(r(rs), VariableType.I32);
            semantics.move(r(ra), r(rs));
        }
    }

    protected static void andi(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        semantics.constraint(r(rs), VariableType.I32);
        semantics.move(r(ra), r(rs));
    }

    protected static void andis(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        semantics.constraint(r(rs), VariableType.I32);
        semantics.move(r(ra), r(rs));
    }

    protected static void cmp(Semantics semantics, InstructionFormat insn) {
        boolean l = insn.L.getBit();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        if (!l) {
            semantics.constraint(r(ra), VariableType.S32);
            semantics.unify(r(ra), r(rb));
        } else {
            semantics.constraint(r(ra), VariableType.S64);
            semantics.unify(r(ra), r(rb));
        }
    }

    protected static void mfcr(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        semantics.set(r(rt), VariableType.I32);
    }

    protected static void lwarx(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        // essentially the same as lwx
        Operand mem = mem(semantics, ra, rb, VariableType.POINTER_I32);
        semantics.constraint(mem, VariableType.I32);
        semantics.set(r(rt), VariableType.I32);
    }

    protected static void lwzx(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        // essentially the same as lwx
        Operand mem = mem(semantics, ra, rb, VariableType.POINTER_U32);
        semantics.constraint(mem, VariableType.I32);
        semantics.set(r(rt), VariableType.I32);
    }

    protected static void slw(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        semantics.move(r(ra), r(rs));
        semantics.constraint(r(ra), VariableType.U32);
    }

    protected static void cntlzw(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        semantics.constraint(r(rs), VariableType.I32);
        semantics.constraint(r(ra), VariableType.U32);
        semantics.set(r(ra), VariableType.U32);
    }

    protected static void and(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        semantics.constraint(r(rs), VariableType.U32);
        semantics.unify(r(rs), r(rb));
        semantics.move(r(ra), r(rs));
    }

    protected static void cmpl(Semantics semantics, InstructionFormat insn) {
        boolean l = insn.L.getBit();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        if (!l) {
            semantics.constraint(r(ra), VariableType.U32);
            semantics.constraint(r(rb), VariableType.U32);
            semantics.unify(r(ra), r(rb));
        } else {
            semantics.constraint(r(ra), VariableType.U64);
            semantics.constraint(r(rb), VariableType.U64);
            semantics.unify(r(ra), r(rb));
        }
    }

    protected static void dcbst(Semantics semantics, InstructionFormat insn) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        semantics.constraint(r0(ra), VariableType.I32);
        semantics.constraint(r(rb), VariableType.I32);
    }

    protected static void lwzux(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        Operand mem = mem(semantics, ra, rb, VariableType.POINTER_U32);
        semantics.constraint(mem, VariableType.I32);
        semantics.set(r(rt), VariableType.I32);
        semantics.constraint(r(ra), VariableType.POINTER_U32);
    }

    protected static void andc(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        semantics.constraint(r(ra), VariableType.U32);
        semantics.constraint(r(rs), VariableType.U32);
        semantics.constraint(r(rb), VariableType.U32);
    }

    protected static void mfmsr(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        semantics.set(r(rt), VariableType.U32);
    }

    protected static void dcbf(Semantics semantics, InstructionFormat insn) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        semantics.constraint(r0(ra), VariableType.I32);
        semantics.constraint(r(rb), VariableType.I32);
    }

    protected static void lbzx(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        Operand mem = mem(semantics, ra, rb, VariableType.POINTER_U8);
        semantics.constraint(mem, VariableType.U8);
        semantics.set(r(rt), VariableType.I32);
        semantics.constraint(r(ra), VariableType.POINTER_U8);
    }

    protected static void lvx(Semantics semantics, InstructionFormat insn) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        semantics.constraint(r0(ra), VariableType.I32);
        semantics.constraint(r(rb), VariableType.I32);
    }

    protected static void lbzux(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        Operand mem = mem(semantics, ra, rb, VariableType.POINTER_U8);
        semantics.constraint(mem, VariableType.U8);
        semantics.constraint(r(ra), VariableType.POINTER_U8);
        semantics.set(r(rt), VariableType.I32);
    }

    protected static void nor(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        semantics.constraint(r(ra), VariableType.I32);
        semantics.constraint(r(rs), VariableType.I32);
        semantics.constraint(r(rb), VariableType.I32);
    }

    protected static void mtcrf(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        boolean one = insn.BIT_11.getBit();
        if (one) {
            return;
        } else {
            semantics.set(r(rs), VariableType.I32);
        }
    }

    protected static void mtmsr(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        semantics.constraint(r(rs), VariableType.I32);
    }

    protected static void stwcx_(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        Operand mem = mem(semantics, ra, rb, VariableType.POINTER_I32);
        semantics.constraint(r(rs), VariableType.I32);
        semantics.move(mem, r(rs));
    }

    protected static void stwx(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        Operand mem = mem(semantics, ra, rb, VariableType.POINTER_I32);
        semantics.constraint(r(rs), VariableType.I32);
        semantics.move(mem, r(rs));
    }

    protected static void stwux(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        Operand mem = mem(semantics, ra, rb, VariableType.POINTER_I32);
        semantics.constraint(r(rs), VariableType.I32);
        semantics.move(mem, r(rs));
        semantics.set(r(ra), VariableType.POINTER_I32);
    }

    protected static void stbx(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        Operand mem = mem(semantics, ra, rb, VariableType.POINTER_I8);
        semantics.constraint(r(rs), VariableType.I8);
        semantics.set(mem, VariableType.I8);
    }

    protected static void stvx(Semantics semantics, InstructionFormat insn) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        semantics.constraint(r0(ra), VariableType.I32);
        semantics.constraint(r(rb), VariableType.I32);
    }

    protected static void dcbtst(Semantics semantics, InstructionFormat insn) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        semantics.constraint(r0(ra), VariableType.I32);
        semantics.constraint(r(rb), VariableType.I32);
    }

    protected static void stbux(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        Operand mem = mem(semantics, ra, rb, VariableType.POINTER_I8);
        semantics.constraint(r(rs), VariableType.I8);
        semantics.set(mem, VariableType.I8);
        semantics.set(r(ra), VariableType.POINTER_I8);
    }

    protected static void dcbt(Semantics semantics, InstructionFormat insn) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        semantics.constraint(r0(ra), VariableType.I32);
        semantics.constraint(r(rb), VariableType.I32);
    }

    protected static void lhzx(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        Operand mem = mem(semantics, ra, rb, VariableType.POINTER_U16);
        semantics.constraint(mem, VariableType.U16);
        semantics.set(r(rt), VariableType.U32);
        semantics.constraint(r(rt), VariableType.U16);
    }

    protected static void eqv(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        semantics.constraint(r(rs), VariableType.I32);
        semantics.constraint(r(rb), VariableType.I32);
        semantics.set(r(ra), VariableType.I32);
    }

    protected static void lhzux(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        Operand mem = mem(semantics, ra, rb, VariableType.POINTER_U16);
        semantics.move(r(rt), mem);
        semantics.constraint(r(rt), VariableType.U16);
        semantics.set(r(ra), VariableType.POINTER_U16);
    }

    protected static void xor(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        semantics.constraint(r(rs), VariableType.I32);
        semantics.constraint(r(rb), VariableType.I32);
        semantics.set(r(ra), VariableType.I32);
    }

    protected static void mfspr(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        semantics.set(r(rt), VariableType.I32);
    }

    protected static void lhax(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        Operand mem = mem(semantics, ra, rb, VariableType.POINTER_S16);
        semantics.move(r(rt), mem);
        semantics.constraint(r(rt), VariableType.S16);
    }

    protected static void mftb(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();

        semantics.set(r(rt), VariableType.I32);
    }

    protected static void sthx(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        Operand mem = mem(semantics, ra, rb, VariableType.POINTER_I16);
        semantics.constraint(r(rs), VariableType.I16);
        semantics.move(mem, r(rs));
    }

    protected static void orc(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        semantics.constraint(r(rs), VariableType.I32);
        semantics.constraint(r(rb), VariableType.I32);
        semantics.set(r(ra), VariableType.I32);
    }

    protected static void or(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        semantics.constraint(r(rs), VariableType.I32);
        semantics.constraint(r(rb), VariableType.I32);
        semantics.set(r(ra), VariableType.I32);
    }

    protected static void mtspr(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();

        semantics.constraint(r(rs), VariableType.I32);
    }

    protected static void dcbi(Semantics semantics, InstructionFormat insn) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        semantics.constraint(r0(ra), VariableType.I32);
        semantics.constraint(r(rb), VariableType.I32);
    }

    protected static void lwbrx(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        Operand mem = mem(semantics, ra, rb, VariableType.POINTER_U32);
        semantics.move(r(rt), mem);
        semantics.constraint(r(rt), VariableType.U32);
    }

    protected static void lfsx(Semantics semantics, InstructionFormat insn) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        Operand mem = mem(semantics, ra, rb, VariableType.POINTER_F32);
        // semantics.move(fr(frt), mem);
        semantics.constraint(mem, VariableType.F32);
    }

    protected static void srw(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        // rb = shift amount
        semantics.constraint(r(rb), VariableType.I32);
        semantics.constraint(r(rs), VariableType.U32);
        semantics.set(r(ra), VariableType.U32);
        semantics.arithmetic(r(ra), false);
    }

    protected static void lswi(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        // int ra = insn.RA.get();
        int nb = insn.NB.get();
        int n = nb == 0 ? 32 : nb;
        int r = rt - 1;
        int i = 32;
        // TODO: implement semantics more accurately
        while (n > 0) {
            if (i == 32) {
                r = (r + 1) % 32;
                semantics.set(r(r), VariableType.I32);
            }
            semantics.set(r(r), VariableType.I32);
            i += 8;
            if (i == 64) {
                i = 32;
            }
            n--;
        }
    }

    protected static void lfdx(Semantics semantics, InstructionFormat insn) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        Operand mem = mem(semantics, ra, rb, VariableType.POINTER_F64);
        // semantics.move(fr(frt), mem);
        semantics.constraint(mem, VariableType.F64);
    }

    protected static void stwbrx(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        Operand mem = mem(semantics, ra, rb, VariableType.POINTER_I32);
        semantics.move(mem, r(rs));
        semantics.constraint(r(rs), VariableType.I32);
    }

    protected static void stfsx(Semantics semantics, InstructionFormat insn) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        Operand mem = mem(semantics, ra, rb, VariableType.POINTER_F32);
        // semantics.move(fr(frt), mem);
        semantics.constraint(mem, VariableType.F32);
    }

    protected static void stfsux(Semantics semantics, InstructionFormat insn) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        Operand mem = mem2(semantics, ra, rb, VariableType.POINTER_F32);
        // semantics.move(fr(frt), mem);
        semantics.constraint(mem, VariableType.F32);
        semantics.set(r(ra), VariableType.POINTER_F32);
    }

    protected static void stswi(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        // int ra = insn.RA.get();
        int nb = insn.NB.get();
        int n = nb == 0 ? 32 : nb;
        int r = rs - 1;
        int i = 32;
        // TODO: implement semantics more accurately
        while (n > 0) {
            if (i == 32) {
                r = (r + 1) % 32;
                semantics.constraint(r(r), VariableType.I32);
            }
            semantics.constraint(r(r), VariableType.I32);
            i += 8;
            if (i == 64) {
                i = 32;
            }
            n--;
        }
    }

    protected static void stfdx(Semantics semantics, InstructionFormat insn) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        Operand mem = mem(semantics, ra, rb, VariableType.POINTER_F64);
        // semantics.move(fr(frt), mem);
        semantics.constraint(mem, VariableType.F64);
    }

    protected static void lhbrx(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        Operand mem = mem(semantics, ra, rb, VariableType.POINTER_I16);
        semantics.constraint(mem, VariableType.I16);
        semantics.move(r(rt), mem);
    }

    protected static void sraw(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        semantics.constraint(r(rs), VariableType.I32);
        semantics.constraint(r(rb), VariableType.I32);
        semantics.set(r(ra), VariableType.I32);
    }

    protected static void srawi(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();

        semantics.constraint(r(rs), VariableType.I32);
        semantics.set(r(ra), VariableType.I32);
    }

    protected static void lfiwax(Semantics semantics, InstructionFormat insn) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        Operand mem = mem(semantics, ra, rb, VariableType.POINTER_I32);
        semantics.constraint(mem, VariableType.I32);
        // semantics.move(frt(frt), mem);
    }

    protected static void sthbrx(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        Operand mem = mem(semantics, ra, rb, VariableType.POINTER_I16);
        semantics.constraint(r(rs), VariableType.I16);
        semantics.move(mem, r(rs));
    }

    protected static void extsh(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();

        semantics.constraint(r(rs), VariableType.S16);
        semantics.set(r(ra), VariableType.S32);
    }

    protected static void extsb(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();

        semantics.constraint(r(rs), VariableType.S8);
        semantics.set(r(ra), VariableType.S32);
    }

    protected static void icbi(Semantics semantics, InstructionFormat insn) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        semantics.constraint(r0(ra), VariableType.I32);
        semantics.constraint(r(rb), VariableType.I32);
    }

    protected static void dcbz(Semantics semantics, InstructionFormat insn) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        // TODO: implement the clear operation, although this requires the
        // dcache line size
        semantics.constraint(r0(ra), VariableType.I32);
        semantics.constraint(r(rb), VariableType.I32);
    }

    protected static void subfc(Semantics semantics, InstructionFormat insn) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        int rt = insn.RT.get();

        semantics.constraint(r(ra), VariableType.I32);
        semantics.constraint(r(rb), VariableType.I32);
        semantics.set(r(rt), VariableType.I32);
    }

    protected static void addc(Semantics semantics, InstructionFormat insn) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        int rt = insn.RT.get();

        semantics.constraint(r(ra), VariableType.I32);
        semantics.constraint(r(rb), VariableType.I32);
        semantics.set(r(rt), VariableType.I32);
    }

    protected static void mulhwu(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        semantics.constraint(r(ra), VariableType.U32);
        semantics.constraint(r(rb), VariableType.U32);
        semantics.set(r(rt), VariableType.U32);
    }

    protected static void subf(Semantics semantics, InstructionFormat insn) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        int rt = insn.RT.get();

        semantics.constraint(r(ra), VariableType.I32);
        semantics.constraint(r(rb), VariableType.I32);
        semantics.set(r(rt), VariableType.I32);
    }

    protected static void mulhw(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        semantics.constraint(r(ra), VariableType.S32);
        semantics.constraint(r(rb), VariableType.S32);
        semantics.set(r(rt), VariableType.S32);
    }

    protected static void neg(Semantics semantics, InstructionFormat insn) {
        int ra = insn.RA.get();
        int rt = insn.RT.get();

        semantics.constraint(r(ra), VariableType.S32);
        semantics.move(r(rt), r(ra));
    }

    protected static void subfe(Semantics semantics, InstructionFormat insn) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        int rt = insn.RT.get();

        semantics.constraint(r(ra), VariableType.I32);
        semantics.constraint(r(rb), VariableType.I32);
        semantics.set(r(rt), VariableType.I32);
    }

    protected static void adde(Semantics semantics, InstructionFormat insn) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        int rt = insn.RT.get();

        semantics.constraint(r(ra), VariableType.I32);
        semantics.constraint(r(rb), VariableType.I32);
        semantics.set(r(rt), VariableType.I32);
    }

    protected static void subfze(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();

        semantics.constraint(r(ra), VariableType.I32);
        semantics.move(r(rt), r(ra));
    }

    protected static void addze(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();

        semantics.constraint(r(ra), VariableType.I32);
        semantics.move(r(rt), r(ra));
    }

    protected static void addme(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();

        semantics.constraint(r(ra), VariableType.S32);
        semantics.move(r(rt), r(ra));
    }

    protected static void mullw(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        semantics.constraint(r(ra), VariableType.S32);
        semantics.constraint(r(rb), VariableType.S32);
        semantics.set(r(rt), VariableType.S32);
    }

    protected static void add(Semantics semantics, InstructionFormat insn) {
        int ra = insn.RA.get();
        int rb = insn.RB.get();
        int rt = insn.RT.get();

        semantics.constraint(r(ra), VariableType.I32);
        semantics.constraint(r(rb), VariableType.I32);
        semantics.set(r(rt), VariableType.I32);
    }

    protected static void divwu(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        semantics.constraint(r(ra), VariableType.U32);
        semantics.constraint(r(rb), VariableType.U32);
        semantics.set(r(rt), VariableType.U32);
    }

    protected static void nand(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        semantics.constraint(r(rs), VariableType.I32);
        semantics.constraint(r(rb), VariableType.I32);
        semantics.set(r(ra), VariableType.I32);
    }

    protected static void divw(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int rb = insn.RB.get();

        semantics.constraint(r(ra), VariableType.S32);
        semantics.constraint(r(rb), VariableType.S32);
        semantics.set(r(rt), VariableType.S32);
    }

    protected static void lwz(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int d = insn.D.get();

        Operand mem = memOff(semantics, ra, d, VariableType.POINTER_U32);
        semantics.constraint(mem, VariableType.I32);
        semantics.set(r(rt), VariableType.I32);
    }

    protected static void lwzu(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int d = insn.D.get();

        Operand mem = memOff(semantics, ra, d, VariableType.POINTER_U32);
        semantics.constraint(mem, VariableType.I32);
        semantics.set(r(rt), VariableType.I32);
        semantics.set(r(ra), VariableType.POINTER_U32);
    }

    protected static void lbz(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int d = insn.D.get();

        Operand mem = memOff(semantics, ra, d, VariableType.POINTER_U8);
        semantics.constraint(mem, VariableType.U8);
        semantics.set(r(rt), VariableType.I32);
    }

    protected static void lbzu(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int d = insn.D.get();

        Operand mem = memOff(semantics, ra, d, VariableType.POINTER_U8);
        semantics.constraint(mem, VariableType.U8);
        semantics.set(r(rt), VariableType.I32);
        semantics.set(r(ra), VariableType.POINTER_U8);
    }

    protected static void stw(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int d = insn.D.get();

        Operand mem = memOff(semantics, ra, d, VariableType.POINTER_I32);
        semantics.constraint(r(rs), VariableType.I32);
        semantics.move(mem, r(rs));
    }

    protected static void stwu(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int d = insn.D.get();

        Operand mem = memOff(semantics, ra, d, VariableType.POINTER_I32);
        semantics.constraint(r(rs), VariableType.I32);
        semantics.move(mem, r(rs));
        semantics.set(r(ra), VariableType.POINTER_I32);
    }

    protected static void stb(Semantics semantics, InstructionFormat insn) {
        // int rs = insn.RS.get();
        int ra = insn.RA.get();
        int d = insn.D.get();

        Operand mem = memOff(semantics, ra, d, VariableType.POINTER_I8);
        semantics.set(mem, VariableType.I8);
    }

    protected static void stbu(Semantics semantics, InstructionFormat insn) {
        // int rs = insn.RS.get();
        int ra = insn.RA.get();
        int d = insn.D.get();

        Operand mem = memOff(semantics, ra, d, VariableType.POINTER_I8);
        semantics.constraint(mem, VariableType.I8);
        semantics.set(r(ra), VariableType.POINTER_I8);
    }

    protected static void lhz(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int d = insn.D.get();

        Operand mem = memOff(semantics, ra, d, VariableType.POINTER_U16);
        semantics.move(r(rt), mem);
        semantics.constraint(r(rt), VariableType.U16);
    }

    protected static void lhzu(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int d = insn.D.get();

        Operand mem = memOff(semantics, ra, d, VariableType.POINTER_U16);
        semantics.constraint(mem, VariableType.U16);
        semantics.set(r(rt), VariableType.I32);
        semantics.set(r(ra), VariableType.POINTER_U16);
    }

    protected static void lha(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int d = insn.D.get();

        Operand mem = memOff(semantics, ra, d, VariableType.POINTER_S16);
        semantics.constraint(mem, VariableType.S16);
        semantics.set(r(rt), VariableType.I32);
    }

    protected static void lhau(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int d = insn.D.get();

        Operand mem = memOff(semantics, ra, d, VariableType.POINTER_S16);
        semantics.constraint(mem, VariableType.S16);
        semantics.set(r(rt), VariableType.I32);
        semantics.set(r(ra), VariableType.POINTER_S16);
    }

    protected static void sth(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int d = insn.D.get();

        Operand mem = memOff(semantics, ra, d, VariableType.POINTER_I16);
        semantics.constraint(r(rs), VariableType.I16);
        semantics.move(mem, r(rs));
    }

    protected static void sthu(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int d = insn.D.get();

        Operand mem = memOff(semantics, ra, d, VariableType.POINTER_I16);
        semantics.constraint(r(rs), VariableType.I16);
        semantics.move(mem, r(rs));
        semantics.set(r(ra), VariableType.POINTER_I16);
    }

    protected static void lmw(Semantics semantics, InstructionFormat insn) {
        int rt = insn.RT.get();
        int ra = insn.RA.get();
        int d = insn.D.get();

        int r = rt;
        int doff = d;
        while (r <= 31) {
            Operand mem = memOff(semantics, ra, doff, VariableType.POINTER_U32);
            semantics.move(r(r), mem);
            semantics.constraint(r(r), VariableType.U32);
            r++;
            doff += 4;
        }
    }

    protected static void stmw(Semantics semantics, InstructionFormat insn) {
        int rs = insn.RS.get();
        int ra = insn.RA.get();
        int d = insn.D.get();

        int r = rs;
        int doff = d;
        while (r <= 31) {
            Operand mem = memOff(semantics, ra, doff, VariableType.POINTER_I32);
            semantics.constraint(r(r), VariableType.I32);
            semantics.move(mem, r(r));
            r++;
            doff += 4;
        }
    }

    protected static void lfs(Semantics semantics, InstructionFormat insn) {
        int ra = insn.RA.get();
        int d = insn.D.get();

        Operand mem = memOff(semantics, ra, d, VariableType.POINTER_F32);
        semantics.constraint(mem, VariableType.F32);
        // semantics.move(frt(frt), mem);
    }

    protected static void lfsu(Semantics semantics, InstructionFormat insn) {
        int ra = insn.RA.get();
        int d = insn.D.get();

        Operand mem = memOff(semantics, ra, d, VariableType.POINTER_F32);
        semantics.constraint(mem, VariableType.F32);
        // semantics.move(frt(frt), mem);
        semantics.set(r(ra), VariableType.POINTER_F32);
    }

    protected static void lfd(Semantics semantics, InstructionFormat insn) {
        int ra = insn.RA.get();
        int d = insn.D.get();

        Operand mem = memOff(semantics, ra, d, VariableType.POINTER_F64);
        semantics.constraint(mem, VariableType.F64);
        // semantics.move(frt(frt), mem);
    }

    protected static void lfdu(Semantics semantics, InstructionFormat insn) {
        int ra = insn.RA.get();
        int d = insn.D.get();

        Operand mem = memOff(semantics, ra, d, VariableType.POINTER_F64);
        semantics.constraint(mem, VariableType.F64);
        // semantics.move(frt(frt), mem);
        semantics.set(r(ra), VariableType.POINTER_F64);
    }

    protected static void stfs(Semantics semantics, InstructionFormat insn) {
        int ra = insn.RA.get();
        int d = insn.D.get();

        Operand mem = memOff(semantics, ra, d, VariableType.POINTER_F32);
        semantics.constraint(mem, VariableType.F32);
        // semantics.move(mem, frt(frt));
    }

    protected static void stfsu(Semantics semantics, InstructionFormat insn) {
        int ra = insn.RA.get();
        int d = insn.D.get();

        Operand mem = memOff(semantics, ra, d, VariableType.POINTER_F32);
        semantics.constraint(mem, VariableType.F32);
        // semantics.move(mem, frt(frt));
        semantics.set(r(ra), VariableType.POINTER_F32);
    }

    protected static void stfd(Semantics semantics, InstructionFormat insn) {
        int ra = insn.RA.get();
        int d = insn.D.get();

        Operand mem = memOff(semantics, ra, d, VariableType.POINTER_F64);
        semantics.constraint(mem, VariableType.F64);
        // semantics.move(mem, frt(frt));
    }

    protected static void stfdu(Semantics semantics, InstructionFormat insn) {
        int ra = insn.RA.get();
        int d = insn.D.get();

        Operand mem = memOff(semantics, ra, d, VariableType.POINTER_F64);
        semantics.constraint(mem, VariableType.F64);
        // semantics.move(mem, frt(frt));
        semantics.set(r(ra), VariableType.POINTER_F64);
    }
}
