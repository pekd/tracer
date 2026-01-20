package org.graalvm.vm.trcview.arch.h8s.disasm;

import org.graalvm.vm.trcview.arch.BranchTarget;
import org.graalvm.vm.trcview.arch.CodeReader;
import org.graalvm.vm.trcview.arch.Disassembler;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.disasm.AssemblerInstruction;
import org.graalvm.vm.trcview.disasm.Operand;
import org.graalvm.vm.trcview.disasm.Token;
import org.graalvm.vm.trcview.disasm.Type;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.util.HexFormatter;

public class H8SDisassembler extends Disassembler {
    public H8SDisassembler() {
        super();
    }

    public H8SDisassembler(TraceAnalyzer trc) {
        super(trc);
    }

    @Override
    public String[] getDisassembly(CodeReader code) {
        AssemblerInstruction asm = disassemble(code);
        if (asm == null) {
            return null;
        } else {
            return getDisassembly(asm);
        }
    }

    @Override
    public AssemblerInstruction disassemble(CodeReader code) {
        try {
            return disasm(code);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public int getLength(CodeReader code) {
        try {
            return H8SInstructionLengthDecoder.getLength(code);
        } catch (IndexOutOfBoundsException e) {
            return 2;
        }
    }

    @Override
    public InstructionType getType(CodeReader code) {
        return H8SInstructionTypeDecoder.getType(code);
    }

    @Override
    public BranchTarget getBranchTarget(CodeReader code) {
        return H8SInstructionBranchDecoder.getBranchTarget(code);
    }

    private static final boolean PRINT_IMM_SIZE = false;
    private static final int REG_SP = 7;

    private static String getER(int er) {
        if (er == REG_SP) {
            return "SP";
        } else {
            return "ER" + er;
        }
    }

    private static Operand reglist(int rn, int cnt) {
        return new Operand(new Token(Type.REGISTER, "ER" + rn), new Token(Type.OTHER, "-"), new Token(Type.REGISTER, "ER" + (rn + cnt - 1)));
    }

    public static Operand reg8(int rn) {
        int n = rn % 8;
        String hl = rn >= 8 ? "L" : "H";
        return new Operand(Type.REGISTER, "R" + n + hl);
    }

    public static Operand reg16(int rn) {
        int n = rn % 8;
        String er = rn >= 8 ? "E" : "R";
        return new Operand(Type.REGISTER, er + n);
    }

    private static Operand atER(int er) {
        return new Operand(new Token(Type.OTHER, "@"), new Token(Type.REGISTER, getER(er)));
    }

    private static Operand atERinc(int er) {
        return new Operand(new Token(Type.OTHER, "@"), new Token(Type.REGISTER, getER(er)), new Token(Type.OTHER, "+"));
    }

    private static Operand atDecER(int er) {
        return new Operand(new Token(Type.OTHER, "@"), new Token(Type.OTHER, "-"), new Token(Type.REGISTER, getER(er)));
    }

    private static Operand atDispER(int disp, int sz, int er) {
        int d = disp;
        if (sz == 8) {
            d = (byte) disp;
        } else if (sz == 16) {
            d = (short) disp;
        }
        String dis;
        if (d <= 256) {
            dis = Integer.toString(d);
        } else if (d >= 0) {
            dis = "0x" + Integer.toHexString(d).toUpperCase();
        } else {
            dis = Integer.toString(d);
        }
        return new Operand(new Token(Type.OTHER, "@"), new Token(Type.OTHER, "("), new Token(Type.OFFSET, dis, d), new Token(Type.OTHER, ":"), new Token(Type.NUMBER, sz), new Token(Type.OTHER, ","),
                        new Token(Type.REGISTER, getER(er)), new Token(Type.OTHER, ")"));
    }

    private static Operand ER(int er) {
        return new Operand(Type.REGISTER, getER(er));
    }

    private Operand atAA(long aa, int sz) {
        /* TODO: #xx:8 => FFFFFF00 | aa; #xx:16 => (s16) aa; #xx:24 => (u24) aa */
        long addr = aa;
        if (sz == 8) {
            addr = ((byte) aa) & 0x00FFFFFF;
        } else if (sz == 16) {
            addr = ((short) aa) & 0x00FFFFFF;
        } else if (sz == 24) {
            addr = aa & 0x00FFFFFF;
        }

        String name = getName(aa);
        if (name != null) {
            return new Operand(new Token(Type.LABEL, name, addr), new Token(Type.OTHER, ":"), new Token(Type.NUMBER, sz));
        } else {
            return new Operand(new Token(Type.OTHER, "@"), new Token(Type.ADDRESS, "0x" + HexFormatter.tohex(aa).toUpperCase(), addr), new Token(Type.OTHER, ":"), new Token(Type.NUMBER, sz));
        }
    }

    private static Operand imm(long value) {
        if (value > -10 && value < 10) {
            return new Operand(new Token(Type.OTHER, "#"), new Token(Type.NUMBER, Long.toString(value), value));
        } else {
            return new Operand(new Token(Type.OTHER, "#"), new Token(Type.NUMBER, "0x" + HexFormatter.tohex(value).toUpperCase(), value));
        }
    }

    private static Operand imm(long value, int sz) {
        if (PRINT_IMM_SIZE) {
            return new Operand(new Token(Type.OTHER, "#"), new Token(Type.NUMBER, "0x" + HexFormatter.tohex(value).toUpperCase(), value), new Token(Type.OTHER, ":"), new Token(Type.NUMBER, sz));
        } else {
            return imm(value);
        }
    }

    private static Operand cons(int value) {
        return new Operand(new Token(Type.OTHER, "#"), new Token(Type.NUMBER, value));
    }

    private Operand bta(long addr, int sz) {
        String name = getName(addr);
        if (name != null) {
            return new Operand(new Token(Type.LABEL, name, addr), new Token(Type.OTHER, ":"), new Token(Type.NUMBER, sz));
        } else {
            return new Operand(new Token(Type.ADDRESS, "0x" + HexFormatter.tohex(addr).toUpperCase(), addr), new Token(Type.OTHER, ":"), new Token(Type.NUMBER, sz));
        }
    }

    public static Operand CCR() {
        return new Operand(Type.REGISTER, "CCR");
    }

    public static Operand EXR() {
        return new Operand(Type.REGISTER, "EXR");
    }

    public AssemblerInstruction disasm(CodeReader reader) {
        int op = Short.toUnsignedInt(reader.peekI16(0));

        switch (op >> 8) {
            case 0x00:
                if (op == 0) {
                    /* NOP */
                    return new AssemblerInstruction("NOP");
                } else {
                    return null;
                }
            case 0x01:
                switch (op & 0xFF) {
                    case 0x00: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(2));
                        switch (op2 >> 8) {
                            case 0x69:
                                switch (op2 & 0x88) {
                                    case 0x00: {
                                        /* MOV.L @ERs, ERd */
                                        int erd = op2 & 0x07;
                                        int ers = (op2 >> 4) & 0x07;
                                        return new AssemblerInstruction("MOV.L", atER(ers), ER(erd));
                                    }
                                    case 0x80: {
                                        /* MOV.L ERs, @ERd */
                                        int ers = op2 & 0x07;
                                        int erd = (op2 >> 4) & 0x07;
                                        return new AssemblerInstruction("MOV.L", ER(ers), atER(erd));
                                    }
                                    default:
                                        return null;
                                }
                            case 0x6B:
                                switch (op2 & 0xF8) {
                                    case 0x00: {
                                        /* MOVL.L @aa:16, ERd */
                                        int erd = op2 & 0x07;
                                        int abs = Short.toUnsignedInt(reader.peekI16(4));
                                        return new AssemblerInstruction("MOV.L", atAA(abs, 16), ER(erd));
                                    }
                                    case 0x20: {
                                        /* MOVL.L @aa:32, ERd */
                                        int erd = op2 & 0x07;
                                        long abs = Integer.toUnsignedLong(reader.peekI32(4));
                                        return new AssemblerInstruction("MOV.L", atAA(abs, 32), ER(erd));
                                    }
                                    case 0x80: {
                                        /* MOVL.L ERs, @aa:16 */
                                        int ers = op2 & 0x07;
                                        int abs = Short.toUnsignedInt(reader.peekI16(4));
                                        return new AssemblerInstruction("MOV.L", ER(ers), atAA(abs, 16));
                                    }
                                    case 0xA0: {
                                        /* MOVL.L ERs, @aa:32 */
                                        int ers = op2 & 0x07;
                                        long abs = Integer.toUnsignedLong(reader.peekI32(4));
                                        return new AssemblerInstruction("MOV.L", ER(ers), atAA(abs, 32));
                                    }
                                    default:
                                        return null;
                                }
                            case 0x6D:
                                switch (op2 & 0x88) {
                                    case 0x00: {
                                        /* MOV.L @ERs+, ERd */
                                        int erd = op2 & 0x07;
                                        int ers = (op2 >> 4) & 0x07;
                                        if (ers == REG_SP) {
                                            return new AssemblerInstruction("POP.L", ER(erd));
                                        } else {
                                            return new AssemblerInstruction("MOV.L", atERinc(ers), ER(erd));
                                        }
                                    }
                                    case 0x80: {
                                        /* MOV.L ERs, @-ERd */
                                        int ers = op2 & 0x07;
                                        int erd = (op2 >> 4) & 0x07;
                                        if (erd == REG_SP) {
                                            return new AssemblerInstruction("PUSH.L", ER(ers));
                                        } else {
                                            return new AssemblerInstruction("MOV.L", ER(ers), atDecER(erd));
                                        }
                                    }
                                    default:
                                        return null;
                                }
                            case 0x6F:
                                switch (op2 & 0x88) {
                                    case 0x00: {
                                        /* MOV.L @(d:16, ERs), ERd */
                                        int erd = op2 & 0x07;
                                        int ers = (op2 >> 4) & 0x07;
                                        short disp = reader.peekI16(4);
                                        return new AssemblerInstruction("MOV.L", atDispER(disp, 16, ers), ER(erd));
                                    }
                                    case 0x80: {
                                        /* MOV.L ERs, @(d:16, ERd) */
                                        int ers = op2 & 0x07;
                                        int erd = (op2 >> 4) & 0x07;
                                        short disp = reader.peekI16(4);
                                        return new AssemblerInstruction("MOV.L", ER(ers), atDispER(disp, 16, erd));
                                    }
                                    default:
                                        return null;
                                }
                            case 0x78:
                                switch (op2 & 0x8F) {
                                    case 0x00: {
                                        int op3 = Short.toUnsignedInt(reader.peekI16(4));
                                        switch (op3 >> 4) {
                                            case 0x6B2: {
                                                if ((op3 & 0x08) != 0) {
                                                    return null;
                                                } else {
                                                    /* MOV.L @(d:32, ERs), ERd */
                                                    int erd = op3 & 0x07;
                                                    int ers = (op2 >> 4) & 0x07;
                                                    int disp = reader.peekI32(6);
                                                    return new AssemblerInstruction("MOV.L", atDispER(disp, 32, ers), ER(erd));
                                                }
                                            }
                                            case 0x6BA: {
                                                if ((op3 & 0x08) != 0) {
                                                    return null;
                                                } else {
                                                    /* MOV.L ERs, @(d:32, ERd) */
                                                    int ers = op3 & 0x07;
                                                    int erd = (op2 >> 4) & 0x07;
                                                    int disp = reader.peekI32(6);
                                                    return new AssemblerInstruction("MOV.L", ER(ers), atDispER(disp, 32, erd));
                                                }
                                            }
                                            default:
                                                return null;
                                        }
                                    }
                                    default:
                                        return null;
                                }
                            default:
                                return null;
                        }
                    }
                    case 0x10: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(2));
                        switch (op2 & 0xFFF8) {
                            case 0x6D70: {
                                /* LDM.L @SP+, (ERn-ERn+1) */
                                int rn = op2 & 0x07;
                                return new AssemblerInstruction("LDM.L", atERinc(REG_SP), reglist(rn - 1, 2));
                            }
                            case 0x6DF0: {
                                /* STM.L (ERn-ERn+1), @-SP */
                                int rn = op2 & 0x07;
                                return new AssemblerInstruction("STM.L", reglist(rn, 2), atDecER(REG_SP));
                            }
                            default:
                                return null;
                        }
                    }
                    case 0x20: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(2));
                        switch (op2 & 0xFFF8) {
                            case 0x6D70: {
                                /* LDM.L @SP+, (ERn-ERn+2) */
                                int rn = op2 & 0x07;
                                return new AssemblerInstruction("LDM.L", atERinc(REG_SP), reglist(rn - 2, 3));
                            }
                            case 0x6DF0: {
                                /* STM.L (ERn-ERn+2), @-SP */
                                int rn = op2 & 0x07;
                                return new AssemblerInstruction("STM.L", reglist(rn, 3), atDecER(REG_SP));
                            }
                            default:
                                return null;
                        }
                    }
                    case 0x30: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(2));
                        switch (op2 & 0xFFF8) {
                            case 0x6D70: {
                                /* LDM.L @SP+, (ERn-ERn+2) */
                                int rn = op2 & 0x07;
                                return new AssemblerInstruction("LDM.L", atERinc(REG_SP), reglist(rn - 3, 4));
                            }
                            case 0x6DF0: {
                                /* STM.L (ERn-ERn+3), @-SP */
                                int rn = op2 & 0x07;
                                return new AssemblerInstruction("STM.L", reglist(rn, 4), atDecER(REG_SP));
                            }
                            default:
                                return null;
                        }
                    }
                    case 0x40: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(2));
                        switch (op2 >> 8) {
                            case 0x69:
                                switch (op2 & 0x8F) {
                                    case 0x00: {
                                        /* LDC.W @ERs, CCR */
                                        int rs = (op2 >> 4) & 0x07;
                                        return new AssemblerInstruction("LDC.W", atER(rs), CCR());
                                    }
                                    case 0x80: {
                                        /* STC.W CCR, @ERd */
                                        int rd = (op2 >> 4) & 0x07;
                                        return new AssemblerInstruction("STC.W", CCR(), atER(rd));
                                    }
                                    default:
                                        return null;
                                }
                            case 0x6B:
                                switch (op2 & 0xFF) {
                                    case 0x00: {
                                        /* LDC.W @aa:16, CCR */
                                        int abs = Short.toUnsignedInt(reader.peekI16(4));
                                        return new AssemblerInstruction("LDC.W", atAA(abs, 16), CCR());
                                    }
                                    case 0x20: {
                                        /* LDC.W @aa:32, CCR */
                                        long abs = Integer.toUnsignedLong(reader.peekI32(4));
                                        return new AssemblerInstruction("LDC.W", atAA(abs, 32), CCR());
                                    }
                                    case 0x80: {
                                        /* STC.W CCR, @aa:16 */
                                        int abs = Short.toUnsignedInt(reader.peekI16(4));
                                        return new AssemblerInstruction("STC.W", CCR(), atAA(abs, 16));
                                    }
                                    case 0xA0: {
                                        /* STC.W CCR, @aa:32 */
                                        long abs = Integer.toUnsignedLong(reader.peekI32(4));
                                        return new AssemblerInstruction("STC.W", CCR(), atAA(abs, 32));
                                    }
                                    default:
                                        return null;
                                }
                            case 0x6D:
                                switch (op2 & 0x8F) {
                                    case 0x00: {
                                        /* LDC.W @ERs+, CCR */
                                        int rs = (op2 >> 4) & 0x07;
                                        return new AssemblerInstruction("LDC.W", atERinc(rs), CCR());
                                    }
                                    case 0x80: {
                                        /* STC.W CCR, @-ERs */
                                        int rd = (op2 >> 4) & 0x07;
                                        return new AssemblerInstruction("STC.W", CCR(), atDecER(rd));
                                    }
                                    default:
                                        return null;
                                }
                            case 0x6F:
                                switch (op2 & 0x8F) {
                                    case 0x00: {
                                        /* LDC.W @(d:16, ERs), CCR */
                                        int rs = (op2 >> 4) & 0x07;
                                        short disp = reader.peekI16(4);
                                        return new AssemblerInstruction("LDC.W", atDispER(disp, 16, rs), CCR());
                                    }
                                    case 0x80: {
                                        /* STC.W CCR, @(d:16, ERd) */
                                        int rd = (op2 >> 4) & 0x07;
                                        short disp = reader.peekI16(4);
                                        return new AssemblerInstruction("STC.W", CCR(), atDispER(disp, 16, rd));
                                    }
                                    default:
                                        return null;
                                }
                            case 0x78: {
                                int op3 = Short.toUnsignedInt(reader.peekI16(4));
                                switch (op3) {
                                    case 0x6B20:
                                        if ((op2 & 0x88) != 0) {
                                            return null;
                                        } else {
                                            /* LDC.W @(d:32, ERs), CCR */
                                            int rs = (op2 >> 4) & 0x07;
                                            int disp = reader.peekI32(6);
                                            return new AssemblerInstruction("LDC.W", atDispER(disp, rs, 32), CCR());
                                        }
                                    case 0x6BA0:
                                        if ((op2 & 0x88) != 0) {
                                            return null;
                                        } else {
                                            /* STC.W CCR, @(d:32, ERs) */
                                            int rd = (op2 >> 4) & 0x07;
                                            int disp = reader.peekI32(6);
                                            return new AssemblerInstruction("STC.W", CCR(), atDispER(disp, rd, 32));
                                        }
                                    default:
                                        return null;
                                }
                            }
                            default:
                                return null;
                        }
                    }
                    case 0x41: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(2));
                        switch (op2 >> 8) {
                            case 0x04:
                                /* ORC #xx:8, EXR */
                                return new AssemblerInstruction("ORC", imm(op2 & 0xFF, 8), EXR());
                            case 0x05:
                                /* XORC #xx:8, EXR */
                                return new AssemblerInstruction("XORC", imm(op2 & 0xFF, 8), EXR());
                            case 0x06:
                                /* ANDC #xx:8, EXR */
                                return new AssemblerInstruction("ANDC", imm(op2 & 0xFF, 8), EXR());
                            case 0x07:
                                /* LDC.B #xx:8, EXR */
                                return new AssemblerInstruction("LDC.B", imm(op2 & 0xFF, 8), EXR());
                            case 0x69:
                                switch (op2 & 0x8F) {
                                    case 0x00: {
                                        /* LDC.W @ERs, EXR */
                                        int rs = (op2 >> 4) & 0x07;
                                        return new AssemblerInstruction("LDC.W", atER(rs), EXR());
                                    }
                                    case 0x80: {
                                        /* STC.W EXR, @ERs */
                                        int rd = (op2 >> 4) & 0x07;
                                        return new AssemblerInstruction("STC.W", EXR(), atER(rd));
                                    }
                                    default:
                                        return null;
                                }
                            case 0x6B:
                                switch (op2 & 0xFF) {
                                    case 0x00: {
                                        /* LDC.W @aa:16, EXR */
                                        int abs = Short.toUnsignedInt(reader.peekI16(4));
                                        return new AssemblerInstruction("LDC.W", atAA(abs, 16), EXR());
                                    }
                                    case 0x20: {
                                        /* LDC.W @aa:32, EXR */
                                        long abs = Integer.toUnsignedLong(reader.peekI32(4));
                                        return new AssemblerInstruction("LDC.W", atAA(abs, 32), EXR());
                                    }
                                    case 0x80: {
                                        /* STC.W EXR, @aa:16 */
                                        int abs = Short.toUnsignedInt(reader.peekI16(4));
                                        return new AssemblerInstruction("STC.W", EXR(), atAA(abs, 16));
                                    }
                                    case 0xA0: {
                                        /* STC.W EXR, @aa:32 */
                                        long abs = Integer.toUnsignedLong(reader.peekI32(4));
                                        return new AssemblerInstruction("STC.W", EXR(), atAA(abs, 32));
                                    }
                                    default:
                                        return null;
                                }
                            case 0x6D:
                                switch (op2 & 0x8F) {
                                    case 0x00: {
                                        /* LDC.W @ERs+, EXR */
                                        int rs = (op2 >> 4) & 0x07;
                                        return new AssemblerInstruction("LDC.W", atERinc(rs), EXR());
                                    }
                                    case 0x80: {
                                        /* STC.W EXR, @-ERd */
                                        int rd = (op2 >> 4) & 0x07;
                                        return new AssemblerInstruction("STC.W", EXR(), atDecER(rd));
                                    }
                                    default:
                                        return null;
                                }
                            case 0x6F:
                                switch (op2 & 0x8F) {
                                    case 0x00: {
                                        /* LDC.W @(d:16, ERs), EXR */
                                        int rs = (op2 >> 4) & 0x07;
                                        short disp = reader.peekI16(4);
                                        return new AssemblerInstruction("LDC.W", atDispER(disp, 16, rs), EXR());
                                    }
                                    case 0x80: {
                                        /* STC.W EXR, @(d:16, ERs) */
                                        int rd = (op2 >> 4) & 0x07;
                                        short disp = reader.peekI16(4);
                                        return new AssemblerInstruction("STC.W", EXR(), atDispER(disp, 16, rd));
                                    }
                                    default:
                                        return null;
                                }
                            case 0x78: {
                                int op3 = Short.toUnsignedInt(reader.peekI16(4));
                                switch (op3) {
                                    case 0x6B20:
                                        if ((op2 & 0x8F) != 0) {
                                            return null;
                                        } else {
                                            /* LDC.W @(d:32, ERs), EXR */
                                            int rs = (op2 >> 4) & 0x07;
                                            int disp = reader.peekI32(6);
                                            return new AssemblerInstruction("LDC.W", atDispER(disp, 32, rs), EXR());
                                        }
                                    case 0x6BA0:
                                        if ((op2 & 0x8F) != 0) {
                                            return null;
                                        } else {
                                            /* STC.W EXR, @(d:32, ERs) */
                                            int rd = (op2 >> 4) & 0x07;
                                            int disp = reader.peekI32(6);
                                            return new AssemblerInstruction("STC.W", EXR(), atDispER(disp, 32, rd));
                                        }
                                    default:
                                        return null;
                                }
                            }
                            default:
                                return null;
                        }
                    }
                    case 0x80:
                        /* SLEEP */
                        return new AssemblerInstruction("SLEEP");
                    case 0xC0: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(2));
                        switch (op2 >> 8) {
                            case 0x50: {
                                /* MULXS.B Rs, Rd */
                                int rs = (op2 >> 4) & 0x0F;
                                int rd = op2 & 0x0F;
                                return new AssemblerInstruction("MULXS.B", reg8(rs), reg16(rd));
                            }
                            case 0x52: {
                                if ((op2 & 0x08) != 0) {
                                    return null;
                                } else {
                                    /* MULXS.W Rs, ERd */
                                    int rs = (op2 >> 4) & 0x0F;
                                    int rd = op2 & 0x07;
                                    return new AssemblerInstruction("MULXS.W", reg16(rs), ER(rd));
                                }
                            }
                            default:
                                return null;
                        }
                    }
                    case 0xD0: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(2));
                        switch (op2 >> 8) {
                            case 0x51: {
                                /* DIVXS.B Rs, Rd */
                                int rs = (op2 >> 4) & 0x0F;
                                int rd = op2 & 0x0F;
                                return new AssemblerInstruction("DIVXS.B", reg8(rs), reg16(rd));
                            }
                            case 0x53:
                                if ((op2 & 0x08) != 0) {
                                    return null;
                                } else {
                                    /* DIVXS.W Rs, Rd */
                                    int rs = (op2 >> 4) & 0x0F;
                                    int rd = op2 & 0x07;
                                    return new AssemblerInstruction("DIVXS.W", reg8(rs), reg16(rd));
                                }
                            default:
                                return null;
                        }
                    }
                    case 0xE0: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(2));
                        switch (op2 >> 8) {
                            case 0x7B:
                                if ((op2 & 0x8F) == 0x0C) {
                                    /* TAS @ERd */
                                    int rd = (op2 >> 4) & 0x07;
                                    return new AssemblerInstruction("TAS", atER(rd));
                                } else {
                                    return null;
                                }
                            default:
                                return null;
                        }
                    }
                    case 0xF0: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(2));
                        switch (op2 >> 8) {
                            case 0x64:
                                if ((op2 & 0x88) != 0) {
                                    return null;
                                } else {
                                    /* OR.L ERs, ERd */
                                    int ers = (op2 >> 4) & 0x07;
                                    int erd = op2 & 0x07;
                                    return new AssemblerInstruction("OR.L", ER(ers), ER(erd));
                                }
                            case 0x65:
                                if ((op2 & 0x88) != 0) {
                                    return null;
                                } else {
                                    /* XOR.L ERs, ERd */
                                    int ers = (op2 >> 4) & 0x07;
                                    int erd = op2 & 0x07;
                                    return new AssemblerInstruction("XOR.L", ER(ers), ER(erd));
                                }
                            case 0x66:
                                if ((op2 & 0x88) != 0) {
                                    return null;
                                } else {
                                    /* AND.L ERs, ERd */
                                    int ers = (op2 >> 4) & 0x07;
                                    int erd = op2 & 0x07;
                                    return new AssemblerInstruction("AND.L", ER(ers), ER(erd));
                                }
                            default:
                                return null;
                        }
                    }
                    default:
                        return null;
                }
            case 0x02:
                switch (op & 0xF0) {
                    case 0x00: {
                        /* STC.B CCR, Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("STC.B", CCR(), reg8(rd));
                    }
                    case 0x10: {
                        /* STC.B EXR, Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("STC.B", EXR(), reg8(rd));
                    }
                    default:
                        return null;
                }
            case 0x03:
                switch (op & 0xF8) {
                    case 0x00:
                    case 0x08: {
                        /* LDC.B Rs, CCR */
                        int rs = op & 0x0F;
                        return new AssemblerInstruction("LDC.B", reg8(rs), CCR());
                    }
                    case 0x10:
                    case 0x18: {
                        /* LDC.B Rs, EXR */
                        int rs = op & 0x0F;
                        return new AssemblerInstruction("LDC.B", reg8(rs), EXR());
                    }
                    default:
                        return null;
                }
            case 0x04:
                /* ORC #xx:8, CCR */
                return new AssemblerInstruction("ORC", imm(op & 0xFF, 8), CCR());
            case 0x05:
                /* XORC #xx:8, CCR */
                return new AssemblerInstruction("XORC", imm(op & 0xFF, 8), CCR());
            case 0x06:
                /* ANDC #xx:8, CCR */
                return new AssemblerInstruction("ANDC", imm(op & 0xFF, 8), CCR());
            case 0x07:
                /* LDC.B #xx:8, CCR */
                return new AssemblerInstruction("LDC.B", imm(op & 0xFF, 8), CCR());
            case 0x08: {
                /* ADD.B Rs, Rd */
                int rs = (op >> 4) & 0x0F;
                int rd = op & 0x0F;
                return new AssemblerInstruction("ADD.B", reg8(rs), reg8(rd));
            }
            case 0x09: {
                /* ADD.W Rs, Rd */
                int rs = (op >> 4) & 0x0F;
                int rd = op & 0x0F;
                return new AssemblerInstruction("ADD.B", reg16(rs), reg16(rd));
            }
            case 0x0A:
                if ((op & 0x88) == 0x80) {
                    /* ADD.L Rs, Rd */
                    int rs = (op >> 4) & 0x07;
                    int rd = op & 0x07;
                    return new AssemblerInstruction("ADD.L", ER(rs), ER(rd));
                } else {
                    switch (op & 0xF0) {
                        case 0x00: {
                            /* INC.B Rd */
                            int rd = op & 0x0F;
                            return new AssemblerInstruction("INC.B", reg8(rd));
                        }
                        default:
                            return null;
                    }
                }
            case 0x0B:
                switch (op & 0xF8) {
                    case 0x00: {
                        /* ADDS #1, ERd */
                        int rd = op & 0x07;
                        return new AssemblerInstruction("ADDS", cons(1), ER(rd));
                    }
                    case 0x50:
                    case 0x58: {
                        /* INC.W #1, Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("INC.W", cons(1), reg16(rd));
                    }
                    case 0x70: {
                        /* INC.L #1, ERd */
                        int rd = op & 0x07;
                        return new AssemblerInstruction("INC.L", cons(1), ER(rd));
                    }
                    case 0x80: {
                        /* ADDS #2, ERd */
                        int rd = op & 0x07;
                        return new AssemblerInstruction("ADDS", cons(2), ER(rd));
                    }
                    case 0x90: {
                        /* ADDS #4, ERd */
                        int rd = op & 0x07;
                        return new AssemblerInstruction("ADDS", cons(4), ER(rd));
                    }
                    case 0xD0:
                    case 0xD8: {
                        /* INC.W #2, Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("INC.W", cons(2), reg16(rd));
                    }
                    case 0xF0: {
                        /* INC.L #2, ERd */
                        int rd = op & 0x07;
                        return new AssemblerInstruction("INC.L", cons(2), ER(rd));
                    }
                    default:
                        return null;
                }
            case 0x0C: {
                /* MOV.B Rs, Rd */
                int rs = (op >> 4) & 0x0F;
                int rd = op & 0x0F;
                return new AssemblerInstruction("MOV.B", reg8(rs), reg8(rd));
            }
            case 0x0D: {
                /* MOV.W Rs, Rd */
                int rs = (op >> 4) & 0x0F;
                int rd = op & 0x0F;
                return new AssemblerInstruction("MOV.W", reg16(rs), reg16(rd));
            }
            case 0x0E: {
                /* ADDX Rs, Rd */
                int rs = (op >> 4) & 0x0F;
                int rd = op & 0x0F;
                return new AssemblerInstruction("ADDX", reg8(rs), reg8(rd));
            }
            case 0x0F: {
                if ((op & 0x88) == 0x80) {
                    /* MOV.L ERs, ERd */
                    int rs = (op >> 4) & 0x07;
                    int rd = op & 0x07;
                    return new AssemblerInstruction("MOV.L", ER(rs), ER(rd));
                } else if ((op & 0x80) != 0) {
                    return null;
                } else {
                    switch (op & 0xF0) {
                        case 0x00: {
                            /* DAA Rd */
                            int rd = op & 0x0F;
                            return new AssemblerInstruction("DAA", reg8(rd));
                        }
                        default:
                            return null;
                    }
                }
            }
            case 0x10:
                switch (op & 0xF8) {
                    case 0x00:
                    case 0x08: {
                        /* SHLL.B Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("SHLL.B", reg8(rd));
                    }
                    case 0x10:
                    case 0x18: {
                        /* SHLL.W Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("SHLL.W", reg16(rd));
                    }
                    case 0x30: {
                        /* SHLL.L Rd */
                        int rd = op & 0x07;
                        return new AssemblerInstruction("SHLL.L", ER(rd));
                    }
                    case 0x40:
                    case 0x48: {
                        /* SHLL.B #2, Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("SHLL.B", cons(2), reg8(rd));
                    }
                    case 0x50:
                    case 0x58: {
                        /* SHLL.W #2, Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("SHLL.W", cons(2), reg16(rd));
                    }
                    case 0x70: {
                        /* SHLL.L #2, Rd */
                        int rd = op & 0x07;
                        return new AssemblerInstruction("SHLL.L", cons(2), ER(rd));
                    }
                    case 0x80:
                    case 0x88: {
                        /* SHAL.B Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("SHAL.B", reg8(rd));
                    }
                    case 0x90:
                    case 0x98: {
                        /* SHAL.W Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("SHAL.W", reg16(rd));
                    }
                    case 0xB0: {
                        /* SHAL.L Rd */
                        int rd = op & 0x07;
                        return new AssemblerInstruction("SHAL.L", ER(rd));
                    }
                    case 0xC0:
                    case 0xC8: {
                        /* SHAL.B #2, Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("SHAL.B", cons(2), reg8(rd));
                    }
                    case 0xD0:
                    case 0xD8: {
                        /* SHAL.W #2, Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("SHAL.W", cons(2), reg16(rd));
                    }
                    case 0xF0: {
                        /* SHAL.L #2, Rd */
                        int rd = op & 0x07;
                        return new AssemblerInstruction("SHAL.L", cons(2), ER(rd));
                    }
                    default:
                        return null;
                }
            case 0x11:
                switch (op & 0xF8) {
                    case 0x00:
                    case 0x08: {
                        /* SHLR.B Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("SHLR.B", reg8(rd));
                    }
                    case 0x10:
                    case 0x18: {
                        /* SHLR.W Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("SHLR.W", reg16(rd));
                    }
                    case 0x30: {
                        /* SHLR.L Rd */
                        int rd = op & 0x07;
                        return new AssemblerInstruction("SHLR.L", ER(rd));
                    }
                    case 0x40:
                    case 0x48: {
                        /* SHLR.B #2, Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("SHLR.B", cons(2), reg8(rd));
                    }
                    case 0x50:
                    case 0x58: {
                        /* SHLR.W #2, Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("SHLR.W", cons(2), reg16(rd));
                    }
                    case 0x70: {
                        /* SHLR.L #2, Rd */
                        int rd = op & 0x07;
                        return new AssemblerInstruction("SHLR.L", cons(2), ER(rd));
                    }
                    case 0x80:
                    case 0x88: {
                        /* SHAR.B Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("SHAR.B", reg8(rd));
                    }
                    case 0x90:
                    case 0x98: {
                        /* SHAR.W Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("SHAR.W", reg16(rd));
                    }
                    case 0xB0: {
                        /* SHAR.L Rd */
                        int rd = op & 0x07;
                        return new AssemblerInstruction("SHAR.L", ER(rd));
                    }
                    case 0xC0:
                    case 0xC8: {
                        /* SHAR.B #2, Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("SHAR.B", cons(2), reg8(rd));
                    }
                    case 0xD0:
                    case 0xD8: {
                        /* SHAR.W #2, Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("SHAR.W", cons(2), reg16(rd));
                    }
                    case 0xF0: {
                        /* SHAR.L #2, Rd */
                        int rd = op & 0x07;
                        return new AssemblerInstruction("SHAR.L", cons(2), ER(rd));
                    }
                    default:
                        return null;
                }
            case 0x12:
                switch (op & 0xF0) {
                    case 0x00: {
                        /* ROTXL.B Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("ROTXL.B", reg8(rd));
                    }
                    case 0x10: {
                        /* ROTXL.W Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("ROTXL.W", reg16(rd));
                    }
                    case 0x30: {
                        if ((op & 0x80) != 0) {
                            return null;
                        } else {
                            /* ROTXL.L ERd */
                            int rd = op & 0x07;
                            return new AssemblerInstruction("ROTXL.L", ER(rd));
                        }
                    }
                    case 0x40: {
                        /* ROTXL.B #2, Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("ROTXL.B", cons(2), reg8(rd));
                    }
                    case 0x50: {
                        /* ROTXL.W #2, Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("ROTXL.W", cons(2), reg16(rd));
                    }
                    case 0x70:
                        if ((op & 0x08) != 0) {
                            return null;
                        } else {
                            /* ROTXL.L #2, ERd */
                            int rd = op & 0x07;
                            return new AssemblerInstruction("ROTXL.L", cons(2), ER(rd));
                        }
                    case 0x80: {
                        /* ROTL.B Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("ROTL.B", reg8(rd));
                    }
                    case 0x90: {
                        /* ROTL.W Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("ROTL.W", reg16(rd));
                    }
                    case 0xB0:
                        if ((op & 0x08) != 0) {
                            return null;
                        } else {
                            /* ROTL.L ERd */
                            int rd = op & 0x07;
                            return new AssemblerInstruction("ROTL.L", ER(rd));
                        }
                    case 0xC0: {
                        /* ROTL.B #2, Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("ROTL.B", cons(2), reg8(rd));
                    }
                    case 0xD0: {
                        /* ROTL.W #2, Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("ROTL.W", cons(2), reg16(rd));
                    }
                    case 0xF0:
                        if ((op & 0x08) != 0) {
                            return null;
                        } else {
                            /* ROTL.L #2, ERd */
                            int rd = op & 0x07;
                            return new AssemblerInstruction("ROTL.L", cons(2), ER(rd));
                        }
                    default:
                        return null;
                }
            case 0x13:
                switch (op & 0xF0) {
                    case 0x00: {
                        /* ROTXR.B Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("ROTXR.B", reg8(rd));
                    }
                    case 0x10: {
                        /* ROTXR.W Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("ROTXR.W", reg16(rd));
                    }
                    case 0x30:
                        if ((op & 0x08) != 0) {
                            return null;
                        } else {
                            /* ROTXR.L Rd */
                            int rd = op & 0x07;
                            return new AssemblerInstruction("ROTXR.L", ER(rd));
                        }
                    case 0x40: {
                        /* ROTXR.B #2, Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("ROTXR.B", cons(2), reg8(rd));
                    }
                    case 0x50: {
                        /* ROTXR.W #2, Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("ROTXR.W", cons(2), reg16(rd));
                    }
                    case 0x70:
                        if ((op & 0x08) != 0) {
                            return null;
                        } else {
                            /* ROTXR.L #2, Rd */
                            int rd = op & 0x0F;
                            return new AssemblerInstruction("ROTXR.L", cons(2), ER(rd));
                        }
                    case 0x80: {
                        /* ROTR.B Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("ROTR.B", reg8(rd));
                    }
                    case 0x90: {
                        /* ROTR.W Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("ROTR.W", reg16(rd));
                    }
                    case 0xB0:
                        if ((op & 0x08) != 0) {
                            return null;
                        } else {
                            /* ROTR.L ERd */
                            int rd = op & 0x07;
                            return new AssemblerInstruction("ROTR.L", ER(rd));
                        }
                    case 0xC0: {
                        /* ROTR.B #2, Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("ROTR.B", cons(2), reg8(rd));
                    }
                    case 0xD0: {
                        /* ROTR.W #2, Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("ROTR.W", cons(2), reg16(rd));
                    }
                    case 0xF0:
                        if ((op & 0x08) != 0) {
                            return null;
                        } else {
                            /* ROTR.L #2, ERd */
                            int rd = op & 0x07;
                            return new AssemblerInstruction("ROTR.L", cons(2), ER(rd));
                        }
                    default:
                        return null;
                }
            case 0x14: {
                /* OR.B Rs, Rd */
                int rs = (op >> 4) & 0x0F;
                int rd = op & 0x0F;
                return new AssemblerInstruction("OR.B", reg8(rs), reg8(rd));
            }
            case 0x15: {
                /* XOR.B Rs, Rd */
                int rs = (op >> 4) & 0x0F;
                int rd = op & 0x0F;
                return new AssemblerInstruction("XOR.B", reg8(rs), reg8(rd));
            }
            case 0x16: {
                /* AND.B Rs, Rd */
                int rs = (op >> 4) & 0x0F;
                int rd = op & 0x0F;
                return new AssemblerInstruction("AND.B", reg8(rs), reg8(rd));
            }
            case 0x17:
                switch (op & 0xF8) {
                    case 0x00:
                    case 0x08: {
                        /* NOT.B Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("NOT.B", reg8(rd));
                    }
                    case 0x10:
                    case 0x18: {
                        /* NOT.W Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("NOT.W", reg16(rd));
                    }
                    case 0x30:
                    case 0x38: {
                        /* NOT.L Rd */
                        int rd = op & 0x07;
                        return new AssemblerInstruction("NOT.L", ER(rd));
                    }
                    case 0x50:
                    case 0x58: {
                        /* EXTU.W Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("EXTU.W", reg16(rd));
                    }
                    case 0x70: {
                        /* EXTU.L Rd */
                        int rd = op & 0x07;
                        return new AssemblerInstruction("EXTU.L", ER(rd));
                    }
                    case 0x80:
                    case 0x88: {
                        /* NEG.B Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("NEG.B", reg8(rd));
                    }
                    case 0x90:
                    case 0x98: {
                        /* NEG.W Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("NEG.W", reg16(rd));
                    }
                    case 0xB0: {
                        /* NEG.L ERd */
                        int rd = op & 0x07;
                        return new AssemblerInstruction("NEG.L", ER(rd));
                    }
                    case 0xD0:
                    case 0xD8: {
                        /* EXTS.W Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("EXTS.W", reg16(rd));
                    }
                    case 0xF0: {
                        /* EXTS.L Rd */
                        int rd = op & 0x07;
                        return new AssemblerInstruction("EXTS.L", ER(rd));
                    }
                    default:
                        return null;
                }
            case 0x18: {
                /* SUB.B Rs, Rd */
                int rs = (op >> 4) & 0x0F;
                int rd = op & 0x0F;
                return new AssemblerInstruction("SUB.B", reg8(rs), reg8(rd));
            }
            case 0x19: {
                /* SUB.W Rs, Rd */
                int rs = (op >> 4) & 0x0F;
                int rd = op & 0x0F;
                return new AssemblerInstruction("SUB.W", reg16(rs), reg16(rd));
            }
            case 0x1A:
                if ((op & 0x88) == 0x80) {
                    /* SUB.L Rs, Rd */
                    int rs = (op >> 4) & 0x07;
                    int rd = op & 0x07;
                    return new AssemblerInstruction("SUB.L", ER(rs), ER(rd));
                } else {
                    switch (op & 0xF0) {
                        case 0x00: {
                            /* DEC.B Rd */
                            int rd = op & 0x0F;
                            return new AssemblerInstruction("DEC.B", reg8(rd));
                        }
                        default:
                            return null;
                    }
                }
            case 0x1B:
                switch (op & 0xF8) {
                    case 0x00: {
                        /* SUBS #1, ERd */
                        int rd = op & 0x07;
                        return new AssemblerInstruction("SUBS", cons(1), ER(rd));
                    }
                    case 0x50:
                    case 0x58: {
                        /* DEC.W #1, Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("DEC.W", cons(1), reg16(rd));
                    }
                    case 0x70: {
                        /* DEC.L #1, Rd */
                        int rd = op & 0x07;
                        return new AssemblerInstruction("DEC.L", cons(1), ER(rd));
                    }
                    case 0x80: {
                        /* SUBS #2, ERd */
                        int rd = op & 0x07;
                        return new AssemblerInstruction("SUBS", cons(2), ER(rd));
                    }
                    case 0x90: {
                        /* SUBS #4, ERd */
                        int rd = op & 0x07;
                        return new AssemblerInstruction("SUBS", cons(4), ER(rd));
                    }
                    case 0xD0:
                    case 0xD8: {
                        /* DEC.W #2, Rd */
                        int rd = op & 0x0F;
                        return new AssemblerInstruction("DEC.W", cons(2), reg16(rd));
                    }
                    case 0xF0: {
                        /* DEC.L #2, Rd */
                        int rd = op & 0x07;
                        return new AssemblerInstruction("DEC.L", cons(2), ER(rd));
                    }
                    default:
                        return null;
                }
            case 0x1C: {
                /* CMP.B Rs, Rd */
                int rs = (op >> 4) & 0x0F;
                int rd = op & 0x0F;
                return new AssemblerInstruction("CMP.B", reg8(rs), reg8(rd));
            }
            case 0x1D: {
                /* CMP.W Rs, Rd */
                int rs = (op >> 4) & 0x0F;
                int rd = op & 0x0F;
                return new AssemblerInstruction("CMP.W", reg16(rs), reg16(rd));
            }
            case 0x1E: {
                /* SUBX Rs, Rd */
                int rs = (op >> 4) & 0x0F;
                int rd = op & 0x0F;
                return new AssemblerInstruction("SUBX", reg8(rs), reg8(rd));
            }
            case 0x1F:
                if ((op & 0x88) == 0x80) {
                    /* CMP.L Rs, Rd */
                    int rs = (op >> 4) & 0x07;
                    int rd = op & 0x07;
                    return new AssemblerInstruction("CMP.L", ER(rs), ER(rd));
                } else if ((op & 0x80) != 0) {
                    return null;
                } else {
                    switch (op & 0xF0) {
                        case 0x00: {
                            int rd = op & 0x0F;
                            return new AssemblerInstruction("DAS", reg8(rd));
                        }
                        default:
                            return null;
                    }
                }
            case 0x20:
            case 0x21:
            case 0x22:
            case 0x23:
            case 0x24:
            case 0x25:
            case 0x26:
            case 0x27:
            case 0x28:
            case 0x29:
            case 0x2A:
            case 0x2B:
            case 0x2C:
            case 0x2D:
            case 0x2E:
            case 0x2F: {
                /* MOV.B @aa:8, Rd */
                int rd = (op >> 8) & 0x0F;
                int abs = op & 0xFF;
                return new AssemblerInstruction("MOV.B", atAA(abs, 8), reg8(rd));
            }
            case 0x30:
            case 0x31:
            case 0x32:
            case 0x33:
            case 0x34:
            case 0x35:
            case 0x36:
            case 0x38:
            case 0x39:
            case 0x3A:
            case 0x3B:
            case 0x3C:
            case 0x3D:
            case 0x3E:
            case 0x3F: {
                /* MOV.B Rs, @aa:8 */
                int rs = (op >> 8) & 0x0F;
                int abs = op & 0xFF;
                return new AssemblerInstruction("MOV.B", reg8(rs), atAA(abs, 8));
            }
            case 0x40:
                /* BRA d:8 */
                return new AssemblerInstruction("BRA", bta(reader.getPC() + 2 + (byte) op, 8));
            case 0x41:
                /* BRN d:8 */
                return new AssemblerInstruction("BRN", bta(reader.getPC() + 2 + (byte) op, 8));
            case 0x42:
                /* BHI d:8 */
                return new AssemblerInstruction("BHI", bta(reader.getPC() + 2 + (byte) op, 8));
            case 0x43:
                /* BLS d:8 */
                return new AssemblerInstruction("BLS", bta(reader.getPC() + 2 + (byte) op, 8));
            case 0x44:
                /* BCC d:8 */
                return new AssemblerInstruction("BCC", bta(reader.getPC() + 2 + (byte) op, 8));
            case 0x45:
                /* BCS d:8 */
                return new AssemblerInstruction("BCS", bta(reader.getPC() + 2 + (byte) op, 8));
            case 0x46:
                /* BNE d:8 */
                return new AssemblerInstruction("BNE", bta(reader.getPC() + 2 + (byte) op, 8));
            case 0x47:
                /* BEQ d:8 */
                return new AssemblerInstruction("BEQ", bta(reader.getPC() + 2 + (byte) op, 8));
            case 0x48:
                /* BVC d:8 */
                return new AssemblerInstruction("BVC", bta(reader.getPC() + 2 + (byte) op, 8));
            case 0x49:
                /* BVS d:8 */
                return new AssemblerInstruction("BVS", bta(reader.getPC() + 2 + (byte) op, 8));
            case 0x4A:
                /* BPL d:8 */
                return new AssemblerInstruction("BPL", bta(reader.getPC() + 2 + (byte) op, 8));
            case 0x4B:
                /* BMI d:8 */
                return new AssemblerInstruction("BMI", bta(reader.getPC() + 2 + (byte) op, 8));
            case 0x4C:
                /* BGE d:8 */
                return new AssemblerInstruction("BGE", bta(reader.getPC() + 2 + (byte) op, 8));
            case 0x4D:
                /* BLT d:8 */
                return new AssemblerInstruction("BLT", bta(reader.getPC() + 2 + (byte) op, 8));
            case 0x4E:
                /* BGT d:8 */
                return new AssemblerInstruction("BGT", bta(reader.getPC() + 2 + (byte) op, 8));
            case 0x4F:
                /* BLE d:8 */
                return new AssemblerInstruction("BLE", bta(reader.getPC() + 2 + (byte) op, 8));
            case 0x50: {
                /* MULXU.B Rs, Rd */
                int rs = (op >> 4) & 0x0F;
                int rd = op & 0x0F;
                return new AssemblerInstruction("MULXU.B", reg8(rs), reg16(rd));
            }
            case 0x51: {
                /* DIVXU.B Rs, Rd */
                int rs = (op >> 4) & 0x0F;
                int rd = op & 0x0F;
                return new AssemblerInstruction("DIVXU.B", reg8(rs), reg16(rd));
            }
            case 0x52:
                if ((op & 0x08) != 0) {
                    return null;
                } else {
                    /* MULXU.W Rs, Rd */
                    int rs = (op >> 4) & 0x0F;
                    int rd = op & 0x07;
                    return new AssemblerInstruction("MULXU.W", reg16(rs), ER(rd));
                }
            case 0x53:
                if ((op & 0x08) != 0) {
                    return null;
                } else {
                    /* DIVXU.W Rs, Rd */
                    int rs = (op >> 4) & 0x0F;
                    int rd = op & 0x07;
                    return new AssemblerInstruction("DIVXU.W", reg16(rs), ER(rd));
                }
            case 0x54:
                switch (op & 0xFF) {
                    case 0x70: {
                        /* RTS */
                        return new AssemblerInstruction("RTS");
                    }
                    default:
                        return null;
                }
            case 0x55:
                /* BSR d:8 */
                return new AssemblerInstruction("BSR", bta(reader.getPC() + 2 + (byte) op, 8));
            case 0x56:
                switch (op & 0xFF) {
                    case 0x70:
                        /* RTE */
                        return new AssemblerInstruction("RTE");
                    default:
                        return null;
                }
            case 0x57:
                if ((op & 0xCF) != 0) {
                    return null;
                } else {
                    /* TRAPA #x:2 */
                    int imm = (op >> 4) & 3;
                    return new AssemblerInstruction("TRAPA", imm(imm));
                }
            case 0x58: {
                short disp = reader.peekI16(2);
                switch (op & 0xFF) {
                    case 0x00:
                        /* BRA d:16 */
                        return new AssemblerInstruction("BRA", bta(reader.getPC() + 4 + disp, 16));
                    case 0x10:
                        /* BRN d:16 */
                        return new AssemblerInstruction("BRN", bta(reader.getPC() + 4 + disp, 16));
                    case 0x20:
                        /* BHI d:16 */
                        return new AssemblerInstruction("BHI", bta(reader.getPC() + 4 + disp, 16));
                    case 0x30:
                        /* BLS d:16 */
                        return new AssemblerInstruction("BLS", bta(reader.getPC() + 4 + disp, 16));
                    case 0x40:
                        /* BCC d:16 */
                        return new AssemblerInstruction("BCC", bta(reader.getPC() + 4 + disp, 16));
                    case 0x50:
                        /* BCS d:16 */
                        return new AssemblerInstruction("BCS", bta(reader.getPC() + 4 + disp, 16));
                    case 0x60:
                        /* BNE d:16 */
                        return new AssemblerInstruction("BNE", bta(reader.getPC() + 4 + disp, 16));
                    case 0x70:
                        /* BEQ d:16 */
                        return new AssemblerInstruction("BEQ", bta(reader.getPC() + 4 + disp, 16));
                    case 0x80:
                        /* BVC d:16 */
                        return new AssemblerInstruction("BVC", bta(reader.getPC() + 4 + disp, 16));
                    case 0x90:
                        /* BVS d:16 */
                        return new AssemblerInstruction("BVS", bta(reader.getPC() + 4 + disp, 16));
                    case 0xA0:
                        /* BPL d:16 */
                        return new AssemblerInstruction("BPL", bta(reader.getPC() + 4 + disp, 16));
                    case 0xB0:
                        /* BMI d:16 */
                        return new AssemblerInstruction("BMI", bta(reader.getPC() + 4 + disp, 16));
                    case 0xC0:
                        /* BGE d:16 */
                        return new AssemblerInstruction("BGE", bta(reader.getPC() + 4 + disp, 16));
                    case 0xD0:
                        /* BLT d:16 */
                        return new AssemblerInstruction("BLT", bta(reader.getPC() + 4 + disp, 16));
                    case 0xE0:
                        /* BGT d:16 */
                        return new AssemblerInstruction("BGT", bta(reader.getPC() + 4 + disp, 16));
                    case 0xF0:
                        /* BLE d:16 */
                        return new AssemblerInstruction("BLE", bta(reader.getPC() + 4 + disp, 16));
                    default:
                        return null;
                }
            }
            case 0x59:
                switch (op & 0x8F) {
                    case 0x00: {
                        /* JMP @ERn */
                        int ern = (op >> 4) & 0x07;
                        return new AssemblerInstruction("JMP", atER(ern));
                    }
                    default:
                        return null;
                }
            case 0x5A: {
                /* JMP @aa:24 */
                int op2 = Short.toUnsignedInt(reader.peekI16(2));
                long abs = (((long) op & 0xFF) << 16) | op2;
                return new AssemblerInstruction("JMP", atAA(abs, 24));
            }
            case 0x5C:
                switch (op & 0xFF) {
                    case 0x00: {
                        /* BSR d:16 */
                        short disp = reader.peekI16(2);
                        return new AssemblerInstruction("BSR", bta(reader.getPC() + disp + 4, 16));
                    }
                    default:
                        return null;
                }
            case 0x5D:
                switch (op & 0x8F) {
                    case 0x00: {
                        /* JSR @ERn */
                        int ern = (op >> 4) & 0x07;
                        return new AssemblerInstruction("JSR", atER(ern));
                    }
                    default:
                        return null;
                }
            case 0x5E: {
                /* JSR @aa:24 */
                int op2 = Short.toUnsignedInt(reader.peekI16(2));
                long abs = (((long) op & 0xFF) << 16) | op2;
                return new AssemblerInstruction("JSR", atAA(abs, 24));
            }
            case 0x60: {
                /* BSET Rn, Rd */
                int rn = (op >> 4) & 0x0F;
                int rd = op & 0x0F;
                return new AssemblerInstruction("BSET", reg8(rn), reg8(rd));
            }
            case 0x61: {
                /* BNOT Rn, Rd */
                int rn = (op >> 4) & 0x0F;
                int rd = op & 0x0F;
                return new AssemblerInstruction("BNOT", reg8(rn), reg8(rd));
            }
            case 0x62: {
                /* BCLR Rn, Rd */
                int rn = (op >> 4) & 0x0F;
                int rd = op & 0x0F;
                return new AssemblerInstruction("BCLR", reg8(rn), reg8(rd));
            }
            case 0x63: {
                /* BTST Rn, Rd */
                int rn = (op >> 4) & 0x0F;
                int rd = op & 0x0F;
                return new AssemblerInstruction("BTST", reg8(rn), reg8(rd));
            }
            case 0x64: {
                /* OR.W Rs, Rd */
                int rs = (op >> 4) & 0x0F;
                int rd = op & 0x0F;
                return new AssemblerInstruction("OR.W", reg16(rs), reg16(rd));
            }
            case 0x65: {
                /* XOR.W Rs, Rd */
                int rs = (op >> 4) & 0x0F;
                int rd = op & 0x0F;
                return new AssemblerInstruction("XOR.W", reg16(rs), reg16(rd));
            }
            case 0x66: {
                /* AND.W Rs, Rd */
                int rs = (op >> 4) & 0x0F;
                int rd = op & 0x0F;
                return new AssemblerInstruction("AND.W", reg16(rs), reg16(rd));
            }
            case 0x67:
                if ((op & 0x80) != 0) {
                    /* BIST #xx:3, Rd */
                    int rd = op & 0x0F;
                    int imm = (op >> 4) & 0x07;
                    return new AssemblerInstruction("BIST", imm(imm), reg8(rd));
                } else {
                    /* BST #xx:3, Rd */
                    int rd = op & 0x0F;
                    int imm = (op >> 4) & 0x07;
                    return new AssemblerInstruction("BST", imm(imm), reg8(rd));
                }
            case 0x68:
                if ((op & 0x80) != 0) {
                    /* MOV.B Rs, @ERd */
                    int rs = op & 0x0F;
                    int erd = (op >> 4) & 0x07;
                    return new AssemblerInstruction("MOV.B", reg8(rs), atER(erd));
                } else {
                    /* MOV.B @ERs, Rd */
                    int rd = op & 0x0F;
                    int rs = (op >> 4) & 0x07;
                    return new AssemblerInstruction("MOV.B", atER(rs), reg8(rd));
                }
            case 0x69:
                if ((op & 0x80) != 0) {
                    /* MOV.W Rs, @ERd */
                    int rs = op & 0x0F;
                    int rd = (op >> 4) & 0x07;
                    return new AssemblerInstruction("MOV.W", reg16(rs), atER(rd));
                } else {
                    /* MOV.W @ERs, Rd */
                    int rd = op & 0x0F;
                    int rs = (op >> 4) & 0x07;
                    return new AssemblerInstruction("MOV.W", atER(rs), reg16(rd));
                }
            case 0x6A:
                switch (op & 0xF8) {
                    case 0x00:
                    case 0x08: {
                        /* MOV.B @aa:16, Rd */
                        int rd = op & 0x0F;
                        int abs = Short.toUnsignedInt(reader.peekI16(2));
                        return new AssemblerInstruction("MOV.B", atAA(abs, 16), reg8(rd));
                    }
                    case 0x10: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(4));
                        switch (op2 >> 8) {
                            case 0x63:
                                if ((op2 & 0x0F) != 0) {
                                    return null;
                                } else {
                                    /* BTST Rn, @aa:16 */
                                    int rn = (op2 >> 4) & 0x0F;
                                    int abs = Short.toUnsignedInt(reader.peekI16(2));
                                    return new AssemblerInstruction("BTST", reg8(rn), atAA(abs, 16));
                                }
                            case 0x73:
                                if ((op2 & 0x8F) != 0) {
                                    return null;
                                } else {
                                    /* BTST #xx:3, @aa:16 */
                                    int imm = (op2 >> 4) & 0x07;
                                    int abs = Short.toUnsignedInt(reader.peekI16(2));
                                    return new AssemblerInstruction("BTST", imm(imm), atAA(abs, 16));
                                }
                            case 0x74:
                                switch (op2 & 0x0F) {
                                    case 0x00:
                                        if ((op2 & 0x80) != 0) {
                                            /* BIOR #xx:3, @aa:16 */
                                            int imm = (op2 >> 4) & 0x07;
                                            int abs = Short.toUnsignedInt(reader.peekI16(2));
                                            return new AssemblerInstruction("BIOR", imm(imm), atAA(abs, 16));
                                        } else {
                                            /* BOR #xx:3, @aa:16 */
                                            int imm = (op2 >> 4) & 0x07;
                                            int abs = Short.toUnsignedInt(reader.peekI16(2));
                                            return new AssemblerInstruction("BOR", imm(imm), atAA(abs, 16));
                                        }
                                    default:
                                        return null;
                                }
                            case 0x75:
                                switch (op2 & 0x0F) {
                                    case 0x00:
                                        if ((op2 & 0x80) != 0) {
                                            /* BIXOR #xx:3, @aa:16 */
                                            int imm = (op2 >> 4) & 0x07;
                                            int abs = Short.toUnsignedInt(reader.peekI16(2));
                                            return new AssemblerInstruction("BIXOR", imm(imm), atAA(abs, 16));
                                        } else {
                                            /* BXOR #xx:3, @aa:16 */
                                            int imm = (op2 >> 4) & 0x07;
                                            int abs = Short.toUnsignedInt(reader.peekI16(2));
                                            return new AssemblerInstruction("BXOR", imm(imm), atAA(abs, 16));
                                        }
                                    default:
                                        return null;
                                }
                            case 0x76:
                                switch (op2 & 0x0F) {
                                    case 0x00:
                                        if ((op2 & 0x80) != 0) {
                                            /* BIAND #xx:3, @aa:16 */
                                            int imm = (op2 >> 4) & 0x07;
                                            int abs = Short.toUnsignedInt(reader.peekI16(2));
                                            return new AssemblerInstruction("BIAND", imm(imm), atAA(abs, 16));
                                        } else {
                                            /* BAND #xx:3, @aa:16 */
                                            int imm = (op2 >> 4) & 0x07;
                                            int abs = Short.toUnsignedInt(reader.peekI16(2));
                                            return new AssemblerInstruction("BAND", imm(imm), atAA(abs, 16));
                                        }
                                    default:
                                        return null;
                                }
                            case 0x77:
                                switch (op2 & 0x0F) {
                                    case 0x00:
                                        if ((op2 & 0x80) != 0) {
                                            /* BILD #xx:3, @aa:16 */
                                            int imm = (op2 >> 4) & 0x07;
                                            int abs = Short.toUnsignedInt(reader.peekI16(2));
                                            return new AssemblerInstruction("BILD", imm(imm), atAA(abs, 16));
                                        } else {
                                            /* BLD #xx:3, @aa:16 */
                                            int imm = (op2 >> 4) & 0x07;
                                            int abs = Short.toUnsignedInt(reader.peekI16(2));
                                            return new AssemblerInstruction("BLD", imm(imm), atAA(abs, 16));
                                        }
                                    default:
                                        return null;
                                }
                            default:
                                return null;
                        }
                    }
                    case 0x18: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(4));
                        switch (op2 >> 8) {
                            case 0x60:
                                if ((op2 & 0x0F) != 0) {
                                    return null;
                                } else {
                                    /* BSET Rn, @aa:16 */
                                    int rn = (op2 >> 4) & 0x0F;
                                    int abs = Short.toUnsignedInt(reader.peekI16(2));
                                    return new AssemblerInstruction("BSET", reg8(rn), atAA(abs, 16));
                                }
                            case 0x61:
                                if ((op2 & 0x0F) != 0) {
                                    return null;
                                } else {
                                    /* BNOT Rn, @aa:16 */
                                    int rn = (op2 >> 4) & 0x0F;
                                    int abs = Short.toUnsignedInt(reader.peekI16(2));
                                    return new AssemblerInstruction("BNOT", reg8(rn), atAA(abs, 16));
                                }
                            case 0x62:
                                if ((op2 & 0x0F) != 0) {
                                    return null;
                                } else {
                                    /* BCLR Rn, @aa:16 */
                                    int rn = (op2 >> 4) & 0x0F;
                                    int abs = Short.toUnsignedInt(reader.peekI16(2));
                                    return new AssemblerInstruction("BCLR", reg8(rn), atAA(abs, 16));
                                }
                            case 0x67:
                                switch (op2 & 0x0F) {
                                    case 0x00:
                                        if ((op2 & 0x80) != 0) {
                                            /* BIST #xx:3, @aa:16 */
                                            int imm = (op2 >> 4) & 0x07;
                                            int abs = Short.toUnsignedInt(reader.peekI16(2));
                                            return new AssemblerInstruction("BIST", imm(imm), atAA(abs, 16));
                                        } else {
                                            /* BST #xx:3, @aa:16 */
                                            int imm = (op2 >> 4) & 0x07;
                                            int abs = Short.toUnsignedInt(reader.peekI16(2));
                                            return new AssemblerInstruction("BST", imm(imm), atAA(abs, 16));
                                        }
                                    default:
                                        return null;
                                }
                            case 0x70:
                                if ((op2 & 0x8F) != 0) {
                                    return null;
                                } else {
                                    /* BSET #xx:3, @aa:16 */
                                    int imm = (op2 >> 4) & 0x07;
                                    int abs = Short.toUnsignedInt(reader.peekI16(2));
                                    return new AssemblerInstruction("BSET", imm(imm), atAA(abs, 16));
                                }
                            case 0x71:
                                if ((op2 & 0x8F) != 0) {
                                    return null;
                                } else {
                                    /* BNOT #xx:3, @aa:16 */
                                    int imm = (op2 >> 4) & 0x07;
                                    int abs = Short.toUnsignedInt(reader.peekI16(2));
                                    return new AssemblerInstruction("BNOT", imm(imm), atAA(abs, 16));
                                }
                            case 0x72:
                                if ((op2 & 0x8F) != 0) {
                                    return null;
                                } else {
                                    /* BCLR #xx:3, @aa:16 */
                                    int imm = (op2 >> 4) & 0x07;
                                    int abs = Short.toUnsignedInt(reader.peekI16(2));
                                    return new AssemblerInstruction("BCLR", imm(imm), atAA(abs, 16));
                                }
                            default:
                                return null;
                        }
                    }
                    case 0x20:
                    case 0x28: {
                        /* MOV.B @aa:32, Rd */
                        int rd = op & 0x0F;
                        long abs = Integer.toUnsignedLong(reader.peekI32(2));
                        return new AssemblerInstruction("MOV.B", atAA(abs, 32), reg8(rd));
                    }
                    case 0x30: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(6));
                        switch (op2 >> 8) {
                            case 0x63:
                                if ((op2 & 0x0F) != 0) {
                                    return null;
                                } else {
                                    /* BTST Rn, @aa:32 */
                                    int rn = (op2 >> 4) & 0x0F;
                                    long abs = Integer.toUnsignedLong(reader.peekI32(2));
                                    return new AssemblerInstruction("BTST", reg8(rn), atAA(abs, 32));
                                }
                            case 0x73:
                                if ((op2 & 0x8F) != 0) {
                                    return null;
                                } else {
                                    /* BTST #xx:3, @aa:32 */
                                    int imm = (op2 >> 4) & 0x07;
                                    long abs = Integer.toUnsignedLong(reader.peekI32(2));
                                    return new AssemblerInstruction("BTST", imm(imm), atAA(abs, 32));
                                }
                            case 0x74:
                                switch (op2 & 0x0F) {
                                    case 0x00:
                                        if ((op2 & 0x80) != 0) {
                                            /* BIOR #xx:3, @aa:32 */
                                            int imm = (op2 >> 4) & 0x07;
                                            long abs = Integer.toUnsignedLong(reader.peekI32(2));
                                            return new AssemblerInstruction("BIOR", imm(imm), atAA(abs, 32));
                                        } else {
                                            /* BOR #xx:3, @aa:32 */
                                            int imm = (op2 >> 4) & 0x07;
                                            long abs = Integer.toUnsignedLong(reader.peekI32(2));
                                            return new AssemblerInstruction("BOR", imm(imm), atAA(abs, 32));
                                        }
                                    default:
                                        return null;
                                }
                            case 0x75:
                                switch (op2 & 0x0F) {
                                    case 0x00:
                                        if ((op2 & 0x80) != 0) {
                                            /* BIXOR #xx:3, @aa:32 */
                                            int imm = (op2 >> 4) & 0x07;
                                            long abs = Integer.toUnsignedLong(reader.peekI32(2));
                                            return new AssemblerInstruction("BIXOR", imm(imm), atAA(abs, 32));
                                        } else {
                                            /* BXOR #xx:3, @aa:32 */
                                            int imm = (op2 >> 4) & 0x07;
                                            long abs = Integer.toUnsignedLong(reader.peekI32(2));
                                            return new AssemblerInstruction("BXOR", imm(imm), atAA(abs, 32));
                                        }
                                    default:
                                        return null;
                                }
                            case 0x76:
                                switch (op2 & 0x0F) {
                                    case 0x00:
                                        if ((op2 & 0x80) != 0) {
                                            /* BIAND #xx:3, @aa:32 */
                                            int imm = (op2 >> 4) & 0x07;
                                            long abs = Integer.toUnsignedLong(reader.peekI32(2));
                                            return new AssemblerInstruction("BIAND", imm(imm), atAA(abs, 32));
                                        } else {
                                            /* BAND #xx:3, @aa:32 */
                                            int imm = (op2 >> 4) & 0x07;
                                            long abs = Integer.toUnsignedLong(reader.peekI32(2));
                                            return new AssemblerInstruction("BAND", imm(imm), atAA(abs, 32));
                                        }
                                    default:
                                        return null;
                                }
                            case 0x77:
                                switch (op2 & 0x0F) {
                                    case 0x00:
                                        if ((op2 & 0x80) != 0) {
                                            /* BILD #xx:3, @aa:32 */
                                            int imm = (op2 >> 4) & 0x07;
                                            long abs = Integer.toUnsignedLong(reader.peekI32(2));
                                            return new AssemblerInstruction("BILD", imm(imm), atAA(abs, 32));
                                        } else {
                                            /* BLD #xx:3, @aa:32 */
                                            int imm = (op2 >> 4) & 0x07;
                                            long abs = Integer.toUnsignedLong(reader.peekI32(2));
                                            return new AssemblerInstruction("BLD", imm(imm), atAA(abs, 32));
                                        }
                                    default:
                                        return null;
                                }
                            default:
                                return null;
                        }
                    }
                    case 0x38: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(6));
                        switch (op2 >> 8) {
                            case 0x60:
                                if ((op2 & 0x0F) != 0) {
                                    return null;
                                } else {
                                    /* BSET Rn, @aa:32 */
                                    int rn = (op2 >> 4) & 0x0F;
                                    long abs = Integer.toUnsignedLong(reader.peekI32(2));
                                    return new AssemblerInstruction("BSET", reg8(rn), atAA(abs, 32));
                                }
                            case 0x61:
                                if ((op2 & 0x0F) != 0) {
                                    return null;
                                } else {
                                    /* BNOT Rn, @aa:32 */
                                    int rn = (op2 >> 4) & 0x0F;
                                    long abs = Integer.toUnsignedLong(reader.peekI32(2));
                                    return new AssemblerInstruction("BNOT", reg8(rn), atAA(abs, 32));
                                }
                            case 0x62:
                                if ((op2 & 0x0F) != 0) {
                                    return null;
                                } else {
                                    /* BCLR Rn, @aa:32 */
                                    int rn = (op2 >> 4) & 0x0F;
                                    long abs = Integer.toUnsignedLong(reader.peekI32(2));
                                    return new AssemblerInstruction("BCLR", reg8(rn), atAA(abs, 32));
                                }
                            case 0x67:
                                switch (op2 & 0x0F) {
                                    case 0x00:
                                        if ((op2 & 0x80) != 0) {
                                            /* BIST #xx:3, @aa:32 */
                                            int imm = (op2 >> 4) & 0x07;
                                            long abs = Integer.toUnsignedLong(reader.peekI32(2));
                                            return new AssemblerInstruction("BIST", imm(imm), atAA(abs, 32));
                                        } else {
                                            /* BST #xx:3, @aa:32 */
                                            int imm = (op2 >> 4) & 0x07;
                                            long abs = Integer.toUnsignedLong(reader.peekI32(2));
                                            return new AssemblerInstruction("BST", imm(imm), atAA(abs, 32));
                                        }
                                    default:
                                        return null;
                                }
                            case 0x70:
                                if ((op2 & 0x8F) != 0) {
                                    return null;
                                } else {
                                    /* BSET #xx:3, @aa:32 */
                                    int imm = (op2 >> 4) & 0x07;
                                    long abs = Integer.toUnsignedLong(reader.peekI32(2));
                                    return new AssemblerInstruction("BSET", imm(imm), atAA(abs, 32));
                                }
                            case 0x71:
                                if ((op2 & 0x8F) != 0) {
                                    return null;
                                } else {
                                    /* BNOT #xx:3, @aa:32 */
                                    int imm = (op2 >> 4) & 0x07;
                                    long abs = Integer.toUnsignedLong(reader.peekI32(2));
                                    return new AssemblerInstruction("BNOT", imm(imm), atAA(abs, 32));
                                }
                            case 0x72:
                                if ((op2 & 0x8F) != 0) {
                                    return null;
                                } else {
                                    /* BCLR #xx:3, @aa:32 */
                                    int imm = (op2 >> 4) & 0x07;
                                    long abs = Integer.toUnsignedLong(reader.peekI32(2));
                                    return new AssemblerInstruction("BCLR", imm(imm), atAA(abs, 32));
                                }
                            default:
                                return null;
                        }
                    }
                    case 0x40:
                    case 0x48: {
                        /* MOVFPE @aa:16, Rd */
                        int rd = op & 0x0F;
                        int abs = Short.toUnsignedInt(reader.peekI16(2));
                        return new AssemblerInstruction("MOVFPE", atAA(abs, 16), reg8(rd));
                    }
                    case 0x80:
                    case 0x88: {
                        /* MOV.B Rs, @aa:16 */
                        int rs = op & 0x0F;
                        int abs = Short.toUnsignedInt(reader.peekI16(2));
                        return new AssemblerInstruction("MOV.B", reg8(rs), atAA(abs, 16));
                    }
                    case 0xA0:
                    case 0xA8: {
                        /* MOV.B Rs, @aa:32 */
                        int rs = op & 0x0F;
                        long abs = Integer.toUnsignedLong(reader.peekI32(2));
                        return new AssemblerInstruction("MOV.B", reg8(rs), atAA(abs, 32));
                    }
                    case 0xC0:
                    case 0xC8: {
                        /* MOVTPE Rs, @aa:16 */
                        int rs = op & 0x0F;
                        int abs = Short.toUnsignedInt(reader.peekI16(2));
                        return new AssemblerInstruction("MOVTPE", reg8(rs), atAA(abs, 16));
                    }
                    default:
                        return null;
                }
            case 0x6B:
                switch (op & 0xF0) {
                    case 0x00: {
                        /* MOV.W @aa:16, Rd */
                        int rd = op & 0x0F;
                        int abs = Short.toUnsignedInt(reader.peekI16(2));
                        return new AssemblerInstruction("MOV.W", atAA(abs, 16), reg16(rd));
                    }
                    case 0x20: {
                        /* MOV.W @aa:32, Rd */
                        int rd = op & 0x0F;
                        long abs = Integer.toUnsignedLong(reader.peekI32(2));
                        return new AssemblerInstruction("MOV.W", atAA(abs, 32), reg16(rd));
                    }
                    case 0x80: {
                        /* MOV.W Rs, @aa:16 */
                        int rs = op & 0x0F;
                        int abs = Short.toUnsignedInt(reader.peekI16(2));
                        return new AssemblerInstruction("MOV.W", reg16(rs), atAA(abs, 16));
                    }
                    case 0xA0: {
                        /* MOV.W Rs, @aa:32 */
                        int rs = op & 0x0F;
                        long abs = Integer.toUnsignedLong(reader.peekI32(2));
                        return new AssemblerInstruction("MOV.W", reg16(rs), atAA(abs, 32));
                    }
                    default:
                        return null;
                }
            case 0x6C:
                if ((op & 0x80) != 0) {
                    /* MOV.B Rs, @-ERd */
                    int rs = op & 0x0F;
                    int rd = (op >> 4) & 0x07;
                    if (rd == REG_SP) {
                        return new AssemblerInstruction("PUSH.B", reg8(rs));
                    } else {
                        return new AssemblerInstruction("MOV.B", reg8(rs), atDecER(rd));
                    }
                } else {
                    /* MOV.B @ERs+, Rd */
                    int rd = op & 0x0F;
                    int rs = (op >> 4) & 0x07;
                    if (rs == REG_SP) {
                        return new AssemblerInstruction("POP.B", reg8(rd));
                    } else {
                        return new AssemblerInstruction("MOV.B", atERinc(rs), reg8(rd));
                    }
                }
            case 0x6D:
                if ((op & 0x80) != 0) {
                    /* MOV.W Rs, @-ERd */
                    int rs = op & 0x0F;
                    int rd = (op >> 4) & 0x07;
                    if (rd == REG_SP) {
                        return new AssemblerInstruction("PUSH.W", reg16(rs));
                    } else {
                        return new AssemblerInstruction("MOV.W", reg16(rs), atDecER(rd));
                    }
                } else {
                    /* MOV.W @ERs+, Rd */
                    int rd = op & 0x0F;
                    int rs = (op >> 4) & 0x07;
                    if (rs == REG_SP) {
                        return new AssemblerInstruction("POP.W", reg16(rd));
                    } else {
                        return new AssemblerInstruction("MOV.W", atERinc(rs), reg16(rd));
                    }
                }
            case 0x6E:
                if ((op & 0x80) != 0) {
                    /* MOV.B Rs, @(d:16, ERd) */
                    int rs = op & 0x0F;
                    int erd = (op >> 4) & 0x07;
                    short disp = reader.peekI16(2);
                    return new AssemblerInstruction("MOV.B", reg8(rs), atDispER(disp, 16, erd));
                } else {
                    /* MOV.B @(d:16, ERs), Rd */
                    int rd = op & 0x0F;
                    int rs = (op >> 4) & 0x07;
                    short disp = reader.peekI16(2);
                    return new AssemblerInstruction("MOV.B", atDispER(disp, 16, rs), reg8(rd));
                }
            case 0x6F:
                if ((op & 0x80) != 0) {
                    /* MOV.W Rs, @(d:16, ERd) */
                    int rs = op & 0x0F;
                    int rd = (op >> 4) & 0x07;
                    short disp = reader.peekI16(2);
                    return new AssemblerInstruction("MOV.W", reg16(rs), atDispER(disp, 16, rd));
                } else {
                    /* MOV.W @(d:16, ERs), Rd */
                    int rd = op & 0x0F;
                    int rs = (op >> 4) & 0x07;
                    short disp = reader.peekI16(2);
                    return new AssemblerInstruction("MOV.W", atDispER(disp, 16, rs), reg16(rd));
                }
            case 0x70:
                if ((op & 0x80) != 0) {
                    return null;
                } else {
                    /* BSET #xx:3, Rd */
                    int imm = (op >> 4) & 0x07;
                    int rd = op & 0x0F;
                    return new AssemblerInstruction("BSET", imm(imm), reg8(rd));
                }
            case 0x71:
                if ((op & 0x80) != 0) {
                    return null;
                } else {
                    /* BNOT #xx:3, Rd */
                    int imm = (op >> 4) & 0x07;
                    int rd = op & 0x0F;
                    return new AssemblerInstruction("BNOT", imm(imm), reg8(rd));
                }
            case 0x72:
                if ((op & 0x80) != 0) {
                    return null;
                } else {
                    /* BCLR #xx:3, Rd */
                    int imm = (op >> 4) & 0x07;
                    int rd = op & 0x0F;
                    return new AssemblerInstruction("BCLR", imm(imm), reg8(rd));
                }
            case 0x73:
                if ((op & 0x80) != 0) {
                    return null;
                } else {
                    /* BTST #xx:3, Rd */
                    int imm = (op >> 4) & 0x07;
                    int rd = op & 0x0F;
                    return new AssemblerInstruction("BTST", imm(imm), reg8(rd));
                }
            case 0x74:
                if ((op & 0x80) != 0) {
                    /* BIOR #xx:3, Rd */
                    int imm = (op >> 4) & 0x07;
                    int rd = op & 0x0F;
                    return new AssemblerInstruction("BIOR", imm(imm), reg8(rd));
                } else {
                    /* BOR #xx:3, Rd */
                    int imm = (op >> 4) & 0x07;
                    int rd = op & 0x0F;
                    return new AssemblerInstruction("BOR", imm(imm), reg8(rd));
                }
            case 0x75:
                if ((op & 0x80) != 0) {
                    /* BIXOR #xx:3, Rd */
                    int imm = (op >> 4) & 0x07;
                    int rd = op & 0x0F;
                    return new AssemblerInstruction("BIXOR", imm(imm), reg8(rd));
                } else {
                    /* BXOR #xx:3, Rd */
                    int imm = (op >> 4) & 0x07;
                    int rd = op & 0x0F;
                    return new AssemblerInstruction("BXOR", imm(imm), reg8(rd));
                }
            case 0x76:
                if ((op & 0x80) != 0) {
                    /* BIAND #xx:3, Rd */
                    int imm = (op >> 4) & 0x07;
                    int rd = op & 0x0F;
                    return new AssemblerInstruction("BIAND", imm(imm), reg8(rd));
                } else {
                    /* BAND #xx:3, Rd */
                    int imm = (op >> 4) & 0x07;
                    int rd = op & 0x0F;
                    return new AssemblerInstruction("BAND", imm(imm), reg8(rd));
                }
            case 0x77:
                if ((op & 0x80) != 0) {
                    /* BILD #xx:3, Rd */
                    int imm = (op >> 4) & 0x07;
                    int rd = op & 0x0F;
                    return new AssemblerInstruction("BILD", imm(imm), reg8(rd));
                } else {
                    /* BLD #xx:3, Rd */
                    int imm = (op >> 4) & 0x07;
                    int rd = op & 0x0F;
                    return new AssemblerInstruction("BLD", imm(imm), reg8(rd));
                }
            case 0x78:
                if ((op & 0x80) != 0) {
                    return null;
                } else {
                    int op2 = Short.toUnsignedInt(reader.peekI16(2));
                    switch (op2 >> 8) {
                        case 0x6A:
                            switch (op2 & 0xF0) {
                                case 0x20: {
                                    if ((op & 0x0F) == 0x00) {
                                        /* MOV.B @(d:32, ERs), Rd */
                                        int rd = op2 & 0x0F;
                                        int rs = (op >> 4) & 0x07;
                                        int disp = reader.peekI32(4);
                                        return new AssemblerInstruction("MOV.B", atDispER(disp, 32, rs), reg8(rd));
                                    } else {
                                        return null;
                                    }
                                }
                                case 0xA0: {
                                    if ((op & 0x0F) == 0x00) {
                                        /* MOV.B Rs, @(d:32, ERd) */
                                        int rs = op2 & 0x0F;
                                        int rd = (op >> 4) & 0x07;
                                        int disp = reader.peekI32(4);
                                        return new AssemblerInstruction("MOV.B", reg8(rs), atDispER(disp, 32, rd));
                                    } else {
                                        return null;
                                    }
                                }
                                default:
                                    return null;
                            }
                        case 0x6B:
                            switch (op2 & 0xF0) {
                                case 0x20: {
                                    if ((op & 0x0F) == 0x00) {
                                        /* MOV.W @(d:32, ERs), Rd */
                                        int rd = op2 & 0x0F;
                                        int rs = (op >> 4) & 0x07;
                                        int disp = reader.peekI32(4);
                                        return new AssemblerInstruction("MOV.W", atDispER(disp, 32, rs), reg16(rd));
                                    } else {
                                        return null;
                                    }
                                }
                                case 0xA0: {
                                    if ((op & 0x0F) == 0x00) {
                                        /* MOV.W Rd, @(d:32, ERs) */
                                        int rs = op2 & 0x0F;
                                        int rd = (op >> 4) & 0x07;
                                        int disp = reader.peekI32(4);
                                        return new AssemblerInstruction("MOV.W", reg16(rs), atDispER(disp, 32, rd));
                                    } else {
                                        return null;
                                    }
                                }
                                default:
                                    return null;
                            }
                        default:
                            return null;
                    }
                }
            case 0x79:
                switch (op & 0xF0) {
                    case 0x00: {
                        /* MOV.W #xx:16, Rd */
                        int rd = op & 0x0F;
                        int imm = Short.toUnsignedInt(reader.peekI16(2));
                        return new AssemblerInstruction("MOV.W", imm(imm, 16), reg16(rd));
                    }
                    case 0x10: {
                        /* ADD.W #xx:16, Rd */
                        int rd = op & 0x0F;
                        int imm = Short.toUnsignedInt(reader.peekI16(2));
                        return new AssemblerInstruction("ADD.W", imm(imm, 16), reg16(rd));
                    }
                    case 0x20: {
                        /* CMP.W #xx:16, Rd */
                        int rd = op & 0x0F;
                        int imm = Short.toUnsignedInt(reader.peekI16(2));
                        return new AssemblerInstruction("CMP.W", imm(imm, 16), reg16(rd));
                    }
                    case 0x30: {
                        /* SUB.W #xx:16, Rd */
                        int rd = op & 0x0F;
                        int imm = Short.toUnsignedInt(reader.peekI16(2));
                        return new AssemblerInstruction("SUB.W", imm(imm, 16), reg16(rd));
                    }
                    case 0x40: {
                        /* OR.W #xx:16, Rd */
                        int rd = op & 0x0F;
                        int imm = Short.toUnsignedInt(reader.peekI16(2));
                        return new AssemblerInstruction("OR.W", imm(imm, 16), reg16(rd));
                    }
                    case 0x50: {
                        /* XOR.W #xx:16, Rd */
                        int rd = op & 0x0F;
                        int imm = Short.toUnsignedInt(reader.peekI16(2));
                        return new AssemblerInstruction("XOR.W", imm(imm, 16), reg16(rd));
                    }
                    case 0x60: {
                        /* AND.W #xx:16, Rd */
                        int rd = op & 0x0F;
                        int imm = Short.toUnsignedInt(reader.peekI16(2));
                        return new AssemblerInstruction("AND.W", imm(imm, 16), reg16(rd));
                    }
                    default:
                        return null;
                }
            case 0x7A:
                switch (op & 0xF8) {
                    case 0x00: {
                        /* MOV.L #xx:32, ERd */
                        int erd = op & 0x07;
                        long imm = Integer.toUnsignedLong(reader.peekI32(2));
                        return new AssemblerInstruction("MOV.L", imm(imm, 32), ER(erd));
                    }
                    case 0x10: {
                        /* ADD.L #xx:32, ERd */
                        int rd = op & 0x07;
                        long imm = Integer.toUnsignedLong(reader.peekI32(2));
                        return new AssemblerInstruction("ADD.L", imm(imm, 32), ER(rd));
                    }
                    case 0x20: {
                        /* CMP.L #xx:32, ERd */
                        int rd = op & 0x07;
                        long imm = Integer.toUnsignedLong(reader.peekI32(2));
                        return new AssemblerInstruction("CMP.L", imm(imm, 32), ER(rd));
                    }
                    case 0x30: {
                        /* SUB.L #xx:32, Rd */
                        int rd = op & 0x07;
                        long imm = Integer.toUnsignedLong(reader.peekI32(2));
                        return new AssemblerInstruction("SUB.L", imm(imm, 32), ER(rd));
                    }
                    case 0x40: {
                        /* OR.L #xx:32, ERd */
                        int rd = op & 0x07;
                        long imm = Integer.toUnsignedLong(reader.peekI32(2));
                        return new AssemblerInstruction("OR.L", imm(imm, 32), ER(rd));
                    }
                    case 0x50: {
                        /* XOR.L #xx:32, ERd */
                        int rd = op & 0x07;
                        long imm = Integer.toUnsignedLong(reader.peekI32(2));
                        return new AssemblerInstruction("XOR.L", imm(imm, 32), ER(rd));
                    }
                    case 0x60: {
                        /* AND.L #xx:32, ERd */
                        int rd = op & 0x07;
                        long imm = Integer.toUnsignedLong(reader.peekI32(2));
                        return new AssemblerInstruction("AND.L", imm(imm, 32), ER(rd));
                    }
                    default:
                        return null;
                }
            case 0x7B:
                switch (op & 0xFF) {
                    case 0x5C: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(2));
                        if (op2 == 0x598F) {
                            /* EEPMOV.B */
                            return new AssemblerInstruction("EEPMOV.B");
                        } else {
                            return null;
                        }
                    }
                    case 0xD4: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(2));
                        if (op2 == 0x598F) {
                            /* EEPMOV.W */
                            return new AssemblerInstruction("EEPMOV.W");
                        } else {
                            return null;
                        }
                    }
                    default:
                        return null;
                }
            case 0x7C:
                switch (op & 0x8F) {
                    case 0x00: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(2));
                        switch (op2 >> 8) {
                            case 0x63:
                                if ((op2 & 0x0F) != 0) {
                                    return null;
                                } else {
                                    /* BTST Rn, @ERd */
                                    int rd = (op >> 4) & 0x07;
                                    int rn = (op2 >> 4) & 0x0F;
                                    return new AssemblerInstruction("BTST", reg8(rn), atER(rd));
                                }
                            case 0x73:
                                if ((op2 & 0x8F) != 0) {
                                    return null;
                                } else {
                                    /* BTST #xx:3, @ERd */
                                    int rd = (op >> 4) & 0x07;
                                    int imm = (op2 >> 4) & 0x07;
                                    return new AssemblerInstruction("BTST", imm(imm), atER(rd));
                                }
                            case 0x74:
                                switch (op2 & 0x0F) {
                                    case 0x00:
                                        if ((op2 & 0x80) != 0) {
                                            /* BIOR #xx:3, @ERd */
                                            int rd = (op >> 4) & 0x07;
                                            int imm = (op2 >> 4) & 0x07;
                                            return new AssemblerInstruction("BIOR", imm(imm), atER(rd));
                                        } else {
                                            /* BOR #xx:3, @ERd */
                                            int rd = (op >> 4) & 0x07;
                                            int imm = (op2 >> 4) & 0x07;
                                            return new AssemblerInstruction("BOR", imm(imm), atER(rd));
                                        }
                                    default:
                                        return null;
                                }
                            case 0x75:
                                switch (op2 & 0x0F) {
                                    case 0x00:
                                        if ((op2 & 0x80) != 0) {
                                            /* BIXOR #xx:3, @ERd */
                                            int rd = (op >> 4) & 0x07;
                                            int imm = (op2 >> 4) & 0x07;
                                            return new AssemblerInstruction("BIXOR", imm(imm), atER(rd));
                                        } else {
                                            /* BXOR #xx:3, @ERd */
                                            int rd = (op >> 4) & 0x07;
                                            int imm = (op2 >> 4) & 0x07;
                                            return new AssemblerInstruction("BXOR", imm(imm), atER(rd));
                                        }
                                    default:
                                        return null;
                                }
                            case 0x76:
                                switch (op2 & 0x0F) {
                                    case 0x00:
                                        if ((op2 & 0x80) != 0) {
                                            /* BIAND #xx:3, @ERd */
                                            int rd = (op >> 4) & 0x07;
                                            int imm = (op2 >> 4) & 0x07;
                                            return new AssemblerInstruction("BIAND", imm(imm), atER(rd));
                                        } else {
                                            /* BAND #xx:3, @ERd */
                                            int rd = (op >> 4) & 0x07;
                                            int imm = (op2 >> 4) & 0x07;
                                            return new AssemblerInstruction("BAND", imm(imm), atER(rd));
                                        }
                                    default:
                                        return null;
                                }
                            case 0x77:
                                switch (op2 & 0x0F) {
                                    case 0x00:
                                        if ((op2 & 0x80) != 0) {
                                            /* BILD #xx:3, @ERd */
                                            int rd = (op >> 4) & 0x07;
                                            int imm = (op2 >> 4) & 0x07;
                                            return new AssemblerInstruction("BILD", imm(imm), atER(rd));
                                        } else {
                                            /* BLD #xx:3, @ERd */
                                            int rd = (op >> 4) & 0x07;
                                            int imm = (op2 >> 4) & 0x07;
                                            return new AssemblerInstruction("BLD", imm(imm), atER(rd));
                                        }
                                    default:
                                        return null;
                                }
                            default:
                                return null;
                        }
                    }
                    default:
                        return null;
                }
            case 0x7D:
                switch (op & 0x8F) {
                    case 0x00: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(2));
                        switch (op2 >> 8) {
                            case 0x60:
                                if ((op2 & 0x0F) != 0) {
                                    return null;
                                } else {
                                    /* BSET Rn, @ERd */
                                    int erd = (op >> 4) & 0x07;
                                    int rn = (op2 >> 4) & 0x0F;
                                    return new AssemblerInstruction("BSET", reg8(rn), atER(erd));
                                }
                            case 0x61:
                                if ((op2 & 0x0F) != 0) {
                                    return null;
                                } else {
                                    /* BNOT Rn, @ERd */
                                    int erd = (op >> 4) & 0x07;
                                    int rn = (op2 >> 4) & 0x0F;
                                    return new AssemblerInstruction("BNOT", reg8(rn), atER(erd));
                                }
                            case 0x62:
                                if ((op2 & 0x0F) != 0) {
                                    return null;
                                } else {
                                    /* BCLR Rn, @ERd */
                                    int erd = (op >> 4) & 0x07;
                                    int rn = (op2 >> 4) & 0x0F;
                                    return new AssemblerInstruction("BCLR", reg8(rn), atER(erd));
                                }
                            case 0x67:
                                switch (op2 & 0x0F) {
                                    case 0x00:
                                        if ((op2 & 0x80) != 0) {
                                            /* BIST #xx:3, @ERd */
                                            int erd = (op >> 4) & 0x07;
                                            int imm = (op2 >> 4) & 0x07;
                                            return new AssemblerInstruction("BIST", imm(imm), atER(erd));
                                        } else {
                                            /* BST #xx:3, @ERd */
                                            int erd = (op >> 4) & 0x07;
                                            int imm = (op2 >> 4) & 0x07;
                                            return new AssemblerInstruction("BST", imm(imm), atER(erd));
                                        }
                                    default:
                                        return null;
                                }
                            case 0x70:
                                if ((op2 & 0x8F) != 0) {
                                    return null;
                                } else {
                                    /* BSET #xx:3, @ERd */
                                    int erd = (op >> 4) & 0x07;
                                    int imm = (op2 >> 4) & 0x07;
                                    return new AssemblerInstruction("BSET", imm(imm), atER(erd));
                                }
                            case 0x71:
                                if ((op2 & 0x8F) != 0) {
                                    return null;
                                } else {
                                    /* BNOT #xx:3, @ERd */
                                    int erd = (op >> 4) & 0x07;
                                    int imm = (op2 >> 4) & 0x07;
                                    return new AssemblerInstruction("BNOT", imm(imm), atER(erd));
                                }
                            case 0x72:
                                if ((op2 & 0x8F) != 0) {
                                    return null;
                                } else {
                                    /* BCLR #xx:3, @ERd */
                                    int erd = (op >> 4) & 0x07;
                                    int imm = (op2 >> 4) & 0x07;
                                    return new AssemblerInstruction("BCLR", imm(imm), atER(erd));
                                }
                            default:
                                return null;
                        }
                    }
                    default:
                        return null;
                }
            case 0x7E: {
                int op2 = Short.toUnsignedInt(reader.peekI16(2));
                switch (op2 >> 8) {
                    case 0x63:
                        if ((op2 & 0x0F) != 0) {
                            return null;
                        } else {
                            /* BTST Rn, @aa:8 */
                            int rn = (op2 >> 4) & 0x0F;
                            int abs = op & 0xFF;
                            return new AssemblerInstruction("BTST", reg8(rn), atAA(abs, 8));
                        }
                    case 0x73:
                        if ((op2 & 0x8F) != 0) {
                            return null;
                        } else {
                            /* BTST #xx:3, @aa:8 */
                            int imm = (op2 >> 4) & 0x07;
                            int abs = op & 0xFF;
                            return new AssemblerInstruction("BTST", imm(imm), atAA(abs, 8));
                        }
                    case 0x74:
                        switch (op2 & 0x0F) {
                            case 0x00:
                                if ((op2 & 0x80) != 0) {
                                    /* BIOR #xx:3, @aa:8 */
                                    int imm = (op2 >> 4) & 0x07;
                                    int abs = op & 0xFF;
                                    return new AssemblerInstruction("BIOR", imm(imm), atAA(abs, 8));
                                } else {
                                    /* BOR #xx:3, @aa:8 */
                                    int imm = (op2 >> 4) & 0x07;
                                    int abs = op & 0xFF;
                                    return new AssemblerInstruction("BOR", imm(imm), atAA(abs, 8));
                                }
                            default:
                                return null;
                        }
                    case 0x75:
                        switch (op2 & 0x0F) {
                            case 0x00:
                                if ((op2 & 0x80) != 0) {
                                    /* BIXOR #xx:3, @aa:8 */
                                    int imm = (op2 >> 4) & 0x07;
                                    int abs = op & 0xFF;
                                    return new AssemblerInstruction("BIXOR", imm(imm), atAA(abs, 8));
                                } else {
                                    /* BXOR #xx:3, @aa:8 */
                                    int imm = (op2 >> 4) & 0x07;
                                    int abs = op & 0xFF;
                                    return new AssemblerInstruction("BXOR", imm(imm), atAA(abs, 8));
                                }
                            default:
                                return null;
                        }
                    case 0x76:
                        switch (op2 & 0x0F) {
                            case 0x00:
                                if ((op2 & 0x80) != 0) {
                                    /* BIAND #xx:3, @aa:8 */
                                    int imm = (op2 >> 4) & 0x07;
                                    int abs = op & 0xFF;
                                    return new AssemblerInstruction("BIAND", imm(imm), atAA(abs, 8));
                                } else {
                                    /* BAND #xx:3, @aa:8 */
                                    int imm = (op2 >> 4) & 0x07;
                                    int abs = op & 0xFF;
                                    return new AssemblerInstruction("BAND", imm(imm), atAA(abs, 8));
                                }
                            default:
                                return null;
                        }
                    case 0x77:
                        switch (op2 & 0x0F) {
                            case 0x00:
                                if ((op2 & 0x80) != 0) {
                                    /* BILD #xx:3, @aa:8 */
                                    int imm = (op2 >> 4) & 0x07;
                                    int abs = op & 0xFF;
                                    return new AssemblerInstruction("BILD", imm(imm), atAA(abs, 8));
                                } else {
                                    /* BLD #xx:3, @aa:8 */
                                    int imm = (op2 >> 4) & 0x07;
                                    int abs = op & 0xFF;
                                    return new AssemblerInstruction("BLD", imm(imm), atAA(abs, 8));
                                }
                            default:
                                return null;
                        }
                    default:
                        return null;
                }
            }
            case 0x7F: {
                int op2 = Short.toUnsignedInt(reader.peekI16(2));
                switch (op2 >> 8) {
                    case 0x60:
                        if ((op2 & 0x0F) != 0) {
                            return null;
                        } else {
                            /* BSET Rn, @aa:8 */
                            int abs = op & 0xFF;
                            int rn = (op2 >> 4) & 0x0F;
                            return new AssemblerInstruction("BSET", reg8(rn), atAA(abs, 8));
                        }
                    case 0x61:
                        if ((op2 & 0x0F) != 0) {
                            return null;
                        } else {
                            /* BNOT Rn, @aa:8 */
                            int abs = op & 0xFF;
                            int rn = (op2 >> 4) & 0x0F;
                            return new AssemblerInstruction("BNOT", reg8(rn), atAA(abs, 8));
                        }
                    case 0x62:
                        if ((op2 & 0x0F) != 0) {
                            return null;
                        } else {
                            /* BCLR Rn, @aa:8 */
                            int abs = op & 0xFF;
                            int rn = (op2 >> 4) & 0x0F;
                            return new AssemblerInstruction("BCLR", reg8(rn), atAA(abs, 8));
                        }
                    case 0x67:
                        switch (op2 & 0x0F) {
                            case 0x00:
                                if ((op2 & 0x80) != 0) {
                                    /* BIST #xx:3, @aa:8 */
                                    int abs = op & 0xFF;
                                    int rn = (op2 >> 4) & 0x0F;
                                    return new AssemblerInstruction("BIST", reg8(rn), atAA(abs, 8));
                                } else {
                                    /* BST #xx:3, @aa:8 */
                                    int abs = op & 0xFF;
                                    int rn = (op2 >> 4) & 0x0F;
                                    return new AssemblerInstruction("BST", reg8(rn), atAA(abs, 8));
                                }
                            default:
                                return null;
                        }
                    case 0x70:
                        if ((op2 & 0x8F) != 0) {
                            return null;
                        } else {
                            /* BSET #xx:3, @aa:8 */
                            int abs = op & 0xFF;
                            int rn = (op2 >> 4) & 0x0F;
                            return new AssemblerInstruction("BSET", reg8(rn), atAA(abs, 8));
                        }
                    case 0x71:
                        if ((op2 & 0x8F) != 0) {
                            return null;
                        } else {
                            /* BNOT #xx:3, @aa:8 */
                            int abs = op & 0xFF;
                            int rn = (op2 >> 4) & 0x0F;
                            return new AssemblerInstruction("BNOT", reg8(rn), atAA(abs, 8));
                        }
                    case 0x72:
                        if ((op2 & 0x8F) != 0) {
                            return null;
                        } else {
                            /* BCLR #xx:3, @aa:8 */
                            int abs = op & 0xFF;
                            int rn = (op2 >> 4) & 0x0F;
                            return new AssemblerInstruction("BCLR", reg8(rn), atAA(abs, 8));
                        }
                    default:
                        return null;
                }
            }
            case 0x80:
            case 0x81:
            case 0x82:
            case 0x83:
            case 0x84:
            case 0x85:
            case 0x86:
            case 0x87:
            case 0x88:
            case 0x89:
            case 0x8A:
            case 0x8B:
            case 0x8C:
            case 0x8D:
            case 0x8E:
            case 0x8F: {
                /* ADD.B #xx:8, Rd */
                int imm = op & 0xFF;
                int rd = (op >> 8) & 0x0F;
                return new AssemblerInstruction("ADD.B", imm(imm, 8), reg8(rd));
            }
            case 0x90:
            case 0x91:
            case 0x92:
            case 0x93:
            case 0x94:
            case 0x95:
            case 0x96:
            case 0x97:
            case 0x98:
            case 0x99:
            case 0x9A:
            case 0x9B:
            case 0x9C:
            case 0x9D:
            case 0x9E:
            case 0x9F: {
                /* ADDX #xx:8, Rd */
                int imm = op & 0xFF;
                int rd = (op >> 8) & 0x0F;
                return new AssemblerInstruction("ADDX", imm(imm, 8), reg8(rd));
            }
            case 0xA0:
            case 0xA1:
            case 0xA2:
            case 0xA3:
            case 0xA4:
            case 0xA5:
            case 0xA6:
            case 0xA7:
            case 0xA8:
            case 0xA9:
            case 0xAA:
            case 0xAB:
            case 0xAC:
            case 0xAD:
            case 0xAE:
            case 0xAF: {
                /* CMP.B #xx:8, Rd */
                int imm = op & 0xFF;
                int rd = (op >> 8) & 0x0F;
                return new AssemblerInstruction("CMP.B", imm(imm, 8), reg8(rd));
            }
            case 0xB0:
            case 0xB1:
            case 0xB2:
            case 0xB3:
            case 0xB4:
            case 0xB5:
            case 0xB6:
            case 0xB7:
            case 0xB8:
            case 0xB9:
            case 0xBA:
            case 0xBB:
            case 0xBC:
            case 0xBD:
            case 0xBE:
            case 0xBF: {
                /* SUBX #xx:8, Rd */
                int imm = op & 0xFF;
                int rd = (op >> 8) & 0x0F;
                return new AssemblerInstruction("SUBX", imm(imm, 8), reg8(rd));
            }
            case 0xC0:
            case 0xC1:
            case 0xC2:
            case 0xC3:
            case 0xC4:
            case 0xC5:
            case 0xC6:
            case 0xC7:
            case 0xC8:
            case 0xC9:
            case 0xCA:
            case 0xCB:
            case 0xCC:
            case 0xCD:
            case 0xCE:
            case 0xCF: {
                /* OR.B #xx:8, Rd */
                int imm = op & 0xFF;
                int rd = (op >> 8) & 0x0F;
                return new AssemblerInstruction("OR.B", imm(imm, 8), reg8(rd));
            }
            case 0xD0:
            case 0xD1:
            case 0xD2:
            case 0xD3:
            case 0xD4:
            case 0xD5:
            case 0xD6:
            case 0xD7:
            case 0xD8:
            case 0xD9:
            case 0xDA:
            case 0xDB:
            case 0xDC:
            case 0xDD:
            case 0xDE:
            case 0xDF: {
                /* XOR.B #xx:8, Rd */
                int imm = op & 0xFF;
                int rd = (op >> 8) & 0x0F;
                return new AssemblerInstruction("XOR.B", imm(imm, 8), reg8(rd));
            }
            case 0xE0:
            case 0xE1:
            case 0xE2:
            case 0xE3:
            case 0xE4:
            case 0xE5:
            case 0xE6:
            case 0xE7:
            case 0xE8:
            case 0xE9:
            case 0xEA:
            case 0xEB:
            case 0xEC:
            case 0xED:
            case 0xEE:
            case 0xEF: {
                /* AND.B #xx:8, Rd */
                int imm = op & 0xFF;
                int rd = (op >> 8) & 0x0F;
                return new AssemblerInstruction("AND.B", imm(imm, 8), reg8(rd));
            }
            case 0xF0:
            case 0xF1:
            case 0xF2:
            case 0xF3:
            case 0xF4:
            case 0xF5:
            case 0xF6:
            case 0xF7:
            case 0xF8:
            case 0xF9:
            case 0xFA:
            case 0xFB:
            case 0xFC:
            case 0xFD:
            case 0xFE:
            case 0xFF: {
                /* MOV.B #xx:8, Rd */
                int imm = op & 0xFF;
                int rd = (op >> 8) & 0x0F;
                return new AssemblerInstruction("MOV.B", imm(imm, 8), reg8(rd));
            }
            default:
                return null;
        }
    }
}
