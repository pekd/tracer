package org.graalvm.vm.trcview.arch.riscv.disasm;

import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.util.HexFormatter;

public class RiscVDisassembler {
    public static final InstructionFormat insnfmt = new InstructionFormat();

    public static InstructionType getType(int insn) {
        int opcd = insnfmt.OPCD.get(insn);
        switch (opcd) {
            case Opcode.OP_JAL:
                if (insnfmt.rd.get(insn) == 0) {
                    return InstructionType.JMP;
                } else {
                    return InstructionType.CALL;
                }
            case Opcode.OP_JALR:
                if (insnfmt.rd.get(insn) == 0 && insnfmt.rs1.get(insn) == 1 &&
                                insnfmt.imm11_0.get(insn) == 0) {
                    return InstructionType.RET;
                } else if (insnfmt.rd.get(insn) == 0) {
                    return InstructionType.JMP_INDIRECT;
                } else {
                    return InstructionType.CALL;
                }
            case Opcode.OP_BRANCH:
                return InstructionType.JCC;
            default:
                return InstructionType.OTHER;
        }
    }

    public static String[] disassemble(long pc, int word) {
        InstructionFormat insn = new InstructionFormat(word);
        switch (insn.OPCD.get()) {
            case Opcode.OP_LUI:
                return lui(insn);
            case Opcode.OP_AUIPC:
                return auipc(insn);
            case Opcode.OP_JAL:
                return jal(insn, pc);
            case Opcode.OP_JALR:
                switch (insn.funct3.get()) {
                    case 0:
                        return jalr(insn);
                }
                break;
            case Opcode.OP_BRANCH:
                switch (insn.funct3.get()) {
                    case Opcode.F3_BEQ:
                        return beq(insn, pc);
                    case Opcode.F3_BNE:
                        return bne(insn, pc);
                    case Opcode.F3_BLT:
                        return blt(insn, pc);
                    case Opcode.F3_BGE:
                        return bge(insn, pc);
                    case Opcode.F3_BLTU:
                        return bltu(insn, pc);
                    case Opcode.F3_BGEU:
                        return bgeu(insn, pc);
                }
                break;
            case Opcode.OP_LOAD:
                switch (insn.funct3.get()) {
                    case Opcode.F3_LB:
                        return load(insn, "lb");
                    case Opcode.F3_LH:
                        return load(insn, "lh");
                    case Opcode.F3_LW:
                        return load(insn, "lw");
                    case Opcode.F3_LD:
                        return load(insn, "ld");
                    case Opcode.F3_LBU:
                        return load(insn, "lbu");
                    case Opcode.F3_LHU:
                        return load(insn, "lhu");
                    case Opcode.F3_LWU:
                        return load(insn, "lwu");
                }
                break;
            case Opcode.OP_STORE:
                switch (insn.funct3.get()) {
                    case Opcode.F3_SB:
                        return store(insn, "sb");
                    case Opcode.F3_SH:
                        return store(insn, "sh");
                    case Opcode.F3_SW:
                        return store(insn, "sw");
                    case Opcode.F3_SD:
                        return store(insn, "sd");
                }
                break;
            case Opcode.OP_IMM:
                switch (insn.funct3.get()) {
                    case Opcode.F3_ADDI:
                        return addi(insn);
                    case Opcode.F3_SLTI:
                        return opimm(insn, "slti");
                    case Opcode.F3_SLTIU:
                        return sltiu(insn);
                    case Opcode.F3_XORI:
                        return xori(insn);
                    case Opcode.F3_ORI:
                        return opimm(insn, "ori");
                    case Opcode.F3_ANDI:
                        return opimm(insn, "andi");
                    case Opcode.F3_SLLI:
                        if (insn.imm11_5.get() == 0) {
                            return shiftimm(insn, "slli");
                        }
                        break;
                    case Opcode.F3_SRLI:
                        if (insn.imm11_5.get() == 0) {
                            return shiftimm(insn, "srli");
                        } else if (insn.imm11_5.get() == 0b0100000) {
                            return shiftimm(insn, "srai");
                        }
                        break;
                }
                break;
            case Opcode.OP:
                switch (insn.funct3.get()) {
                    case Opcode.F3_ADD:
                        if (insn.imm11_5.get() == 0) {
                            return op(insn, "add");
                        } else if (insn.imm11_5.get() == 0b0100000) {
                            return op(insn, "sub");
                        }
                        break;
                    case Opcode.F3_SLL:
                        if (insn.imm11_5.get() == 0) {
                            return op(insn, "sll");
                        }
                        break;
                    case Opcode.F3_SLT:
                        if (insn.imm11_5.get() == 0) {
                            return op(insn, "slt");
                        }
                        break;
                    case Opcode.F3_SLTU:
                        if (insn.imm11_5.get() == 0) {
                            return op(insn, "sltu");
                        }
                        break;
                    case Opcode.F3_XOR:
                        if (insn.imm11_5.get() == 0) {
                            return op(insn, "xor");
                        }
                        break;
                    case Opcode.F3_SRL:
                        if (insn.imm11_5.get() == 0) {
                            return op(insn, "srl");
                        } else if (insn.imm11_5.get() == 0b0100000) {
                            return op(insn, "sra");
                        }
                        break;
                    case Opcode.F3_OR:
                        if (insn.imm11_5.get() == 0) {
                            return op(insn, "or");
                        }
                        break;
                    case Opcode.F3_AND:
                        if (insn.imm11_5.get() == 0) {
                            return op(insn, "and");
                        }
                        break;
                }
                break;
            case Opcode.OP_SYSTEM:
                if (word == Opcode.OP_SYSTEM) {
                    return new String[]{"ecall"};
                } else if (word == (Opcode.OP_SYSTEM | (1 << 20))) {
                    return new String[]{"ebreak"};
                }
                break;
        }

        return new String[]{".int", "0x" + HexFormatter.tohex(word & 0xFFFFFFFFL, 8),
                        "# unknown opcode " + insn.OPCD.get()};
    }

    private static final String[] REGS = {"zero", "ra", "sp", "gp", "tp", "t0", "t1", "t2", "s0", "s1", "a0", "a1",
                    "a2", "a3", "a4", "a5", "a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10",
                    "s11", "t3", "t4", "t5", "t6"};

    protected static String r(int r) {
        return REGS[r];
    }

    protected static String hex(long x) {
        if (x >= 0 && x <= 9) {
            return Long.toString(x);
        } else {
            return "0x" + HexFormatter.tohex(x);
        }
    }

    protected static String off(int x) {
        return Integer.toString(x);
    }

    protected static String imm(int x) {
        return Integer.toUnsignedString(x);
    }

    protected static String[] lui(InstructionFormat insn) {
        return new String[]{"lui", r(insn.rd.get()), hex(insn.imm31_12.get())};
    }

    protected static String[] auipc(InstructionFormat insn) {
        return new String[]{"auipc", r(insn.rd.get()), hex(insn.imm31_12.get())};
    }

    protected static String[] jal(InstructionFormat insn, long pc) {
        int offset20 = (insn.imm20.get() << 20) | (insn.imm10_1.get() << 1) |
                        (insn.imm11_J.get() << 11) | (insn.imm19_12.get() << 12);
        int offset = (offset20 << 11 >> 11);
        long dest = pc + offset;
        int rd = insn.rd.get();
        if (rd == 0) {
            return new String[]{"j", hex(dest)};
        } else if (rd == 1) {
            return new String[]{"jal", hex(dest)};
        } else {
            return new String[]{"jal", r(rd), hex(dest)};
        }
    }

    protected static String[] jalr(InstructionFormat insn) {
        int imm = insn.imm11_0.get() << 20 >> 20;
        int rd = insn.rd.get();
        int rs = insn.rs1.get();
        if (rd == 0 && rs == 1 && imm == 0) {
            return new String[]{"ret"};
        } else if (rd == 0 && imm == 0) {
            return new String[]{"jr", r(rs)};
        } else if (rd == 1 && imm == 0) {
            return new String[]{"jalr", r(rs)};
        } else {
            return new String[]{"jalr", r(insn.rd.get()), r(insn.rs1.get()), off(imm)};
        }
    }

    protected static String[] beq(InstructionFormat insn, long pc) {
        int rs1 = insn.rs1.get();
        int rs2 = insn.rs2.get();
        int offset = (insn.imm4_1.get() << 1) | (insn.imm11_B.get() << 11) | (insn.imm12.get() << 12) |
                        (insn.imm10_5.get() << 5);
        long dest = pc + offset;
        if (rs2 == 0) {
            return new String[]{"beqz", r(rs1), hex(dest)};
        } else {
            return new String[]{"beq", r(rs1), r(rs2), hex(dest)};
        }
    }

    protected static String[] bne(InstructionFormat insn, long pc) {
        int rs1 = insn.rs1.get();
        int rs2 = insn.rs2.get();
        int offset = (insn.imm4_1.get() << 1) | (insn.imm11_B.get() << 11) | (insn.imm12.get() << 12) |
                        (insn.imm10_5.get() << 5);
        long dest = pc + offset;
        if (rs2 == 0) {
            return new String[]{"bnez", r(rs1), hex(dest)};
        } else {
            return new String[]{"bne", r(rs1), r(rs2), hex(dest)};
        }
    }

    protected static String[] blt(InstructionFormat insn, long pc) {
        int rs1 = insn.rs1.get();
        int rs2 = insn.rs2.get();
        int offset = (insn.imm4_1.get() << 1) | (insn.imm11_B.get() << 11) | (insn.imm12.get() << 12) |
                        (insn.imm10_5.get() << 5);
        long dest = pc + offset;
        if (rs2 == 0) {
            return new String[]{"bltz", r(rs1), hex(dest)};
        } else if (rs1 == 0) {
            return new String[]{"bgtz", r(rs2), hex(dest)};
        } else {
            return new String[]{"blt", r(rs1), r(rs2), hex(dest)};
        }
    }

    protected static String[] bge(InstructionFormat insn, long pc) {
        int rs1 = insn.rs1.get();
        int rs2 = insn.rs2.get();
        int offset = (insn.imm4_1.get() << 1) | (insn.imm11_B.get() << 11) | (insn.imm12.get() << 12) |
                        (insn.imm10_5.get() << 5);
        long dest = pc + offset;
        if (rs2 == 0) {
            return new String[]{"bgez", r(rs1), hex(dest)};
        } else if (rs1 == 0) {
            return new String[]{"blez", r(rs2), hex(dest)};
        } else {
            return new String[]{"bge", r(rs1), r(rs2), hex(dest)};
        }
    }

    protected static String[] bltu(InstructionFormat insn, long pc) {
        int rs1 = insn.rs1.get();
        int rs2 = insn.rs2.get();
        int offset = (insn.imm4_1.get() << 1) | (insn.imm11_B.get() << 11) | (insn.imm12.get() << 12) |
                        (insn.imm10_5.get() << 5);
        long dest = pc + offset;
        return new String[]{"bltu", r(rs1), r(rs2), hex(dest)};
    }

    protected static String[] bgeu(InstructionFormat insn, long pc) {
        int rs1 = insn.rs1.get();
        int rs2 = insn.rs2.get();
        int offset = (insn.imm4_1.get() << 1) | (insn.imm11_B.get() << 11) | (insn.imm12.get() << 12) |
                        (insn.imm10_5.get() << 5);
        long dest = pc + offset;
        return new String[]{"bgeu", r(rs1), r(rs2), hex(dest)};
    }

    protected static String[] load(InstructionFormat insn, String mnemonic) {
        int offset = insn.imm11_0.get();
        if (offset != 0) {
            return new String[]{mnemonic, r(insn.rd.get()), off(offset) + "(" + r(insn.rs1.get()) + ")"};
        } else {
            return new String[]{mnemonic, r(insn.rd.get()), "(" + r(insn.rs1.get()) + ")"};
        }
    }

    protected static String[] store(InstructionFormat insn, String mnemonic) {
        int offset = (insn.imm11_5.get() | insn.imm4_0.get()) << 20 >> 20;
        if (offset != 0) {
            return new String[]{mnemonic, r(insn.rs2.get()),
                            off(offset) + "(" + r(insn.rs1.get()) + ")"};
        } else {
            return new String[]{mnemonic, r(insn.rs2.get()), "(" + r(insn.rs1.get()) + ")"};
        }
    }

    protected static String[] addi(InstructionFormat insn) {
        int imm = insn.imm11_0.get();
        int rd = insn.rd.get();
        int rs = insn.rs1.get();
        if (rd == 0) {
            return new String[]{"nop"};
        } else if (rs == 0) {
            return new String[]{"li", r(rd), off(imm)};
        } else if (imm == 0) {
            return new String[]{"mv", r(rd), r(rs)};
        } else {
            return new String[]{"addi", r(rd), r(rs), off(imm)};
        }
    }

    protected static String[] xori(InstructionFormat insn) {
        int imm = insn.imm11_0.get();
        int rd = insn.rd.get();
        int rs = insn.rs1.get();
        if (imm == -1) {
            return new String[]{"not", r(rd), r(rs)};
        } else {
            return new String[]{"xori", r(rd), r(rs), off(imm)};
        }
    }

    protected static String[] sltiu(InstructionFormat insn) {
        int imm = insn.imm11_0u.get();
        int rd = insn.rd.get();
        int rs = insn.rs1.get();
        if (imm == 1) {
            return new String[]{"seqz", r(rd), r(rs)};
        } else {
            return new String[]{"sltiu", r(rd), r(rs), imm(imm)};
        }
    }

    protected static String[] opimm(InstructionFormat insn, String mnemonic) {
        int imm = insn.imm11_0.get();
        int rd = insn.rd.get();
        int rs = insn.rs1.get();
        return new String[]{mnemonic, r(rd), r(rs), imm(imm)};
    }

    protected static String[] shiftimm(InstructionFormat insn, String mnemonic) {
        int shamt = insn.rs2.get();
        int rs = insn.rs1.get();
        int rd = insn.rd.get();
        return new String[]{mnemonic, r(rd), r(rs), imm(shamt)};
    }

    protected static String[] op(InstructionFormat insn, String mnemonic) {
        return new String[]{mnemonic, r(insn.rd.get()), r(insn.rs1.get()), r(insn.rs2.get())};
    }
}
