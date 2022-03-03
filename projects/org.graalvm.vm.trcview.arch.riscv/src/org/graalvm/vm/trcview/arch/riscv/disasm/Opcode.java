package org.graalvm.vm.trcview.arch.riscv.disasm;

public class Opcode {
    // opcode field
    public static final int OP_LOAD = 0b0000011;
    public static final int OP_LOAD_FP = 0b0000111;
    public static final int OP_MISC_MEM = 0b0001111;
    public static final int OP_IMM = 0b0010011;
    public static final int OP_AUIPC = 0b0010111;
    public static final int OP_IMM_32 = 0b0011011;
    public static final int OP_STORE = 0b0100011;
    public static final int OP_STORE_FP = 0b0100111;
    public static final int OP_AMO = 0b0101111;
    public static final int OP = 0b0110011;
    public static final int OP_LUI = 0b0110111;
    public static final int OP_32 = 0b0111011;
    public static final int OP_MADD = 0b1000011;
    public static final int OP_MSUB = 0b1000111;
    public static final int OP_NMSUB = 0b1001011;
    public static final int OP_NMADD = 0b1001111;
    public static final int OP_FP = 0b1010011;
    public static final int OP_BRANCH = 0b1100011;
    public static final int OP_JALR = 0b1100111;
    public static final int OP_JAL = 0b1101111;
    public static final int OP_SYSTEM = 0b1110011;

    // RV32I
    public static final int F3_BEQ = 0b000;
    public static final int F3_BNE = 0b001;
    public static final int F3_BLT = 0b100;
    public static final int F3_BGE = 0b101;
    public static final int F3_BLTU = 0b110;
    public static final int F3_BGEU = 0b111;

    public static final int F3_LB = 0b000;
    public static final int F3_LH = 0b001;
    public static final int F3_LW = 0b010;
    public static final int F3_LBU = 0b100;
    public static final int F3_LHU = 0b101;

    public static final int F3_SB = 0b000;
    public static final int F3_SH = 0b001;
    public static final int F3_SW = 0b010;

    public static final int F3_ADDI = 0b000;
    public static final int F3_SLTI = 0b010;
    public static final int F3_SLTIU = 0b011;
    public static final int F3_XORI = 0b100;
    public static final int F3_ORI = 0b110;
    public static final int F3_ANDI = 0b111;
    public static final int F3_SLLI = 0b001;
    public static final int F3_SRLI = 0b101;

    public static final int F3_ADD = 0b000;
    public static final int F3_SLL = 0b001;
    public static final int F3_SLT = 0b010;
    public static final int F3_SLTU = 0b011;
    public static final int F3_XOR = 0b100;
    public static final int F3_SRL = 0b101;
    public static final int F3_OR = 0b110;
    public static final int F3_AND = 0b111;

    // RV64I
    public static final int F3_LWU = 0b110;
    public static final int F3_LD = 0b011;
    public static final int F3_SD = 0b011;
}
