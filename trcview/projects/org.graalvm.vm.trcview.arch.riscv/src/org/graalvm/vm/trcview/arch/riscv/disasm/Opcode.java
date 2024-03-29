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
    public static final int F3_ADDIW = 0b000;
    public static final int F3_SLLIW = 0b001;
    public static final int F3_SRLIW = 0b101;
    public static final int F3_SRAIW = 0b101;
    public static final int F3_ADDW = 0b000;
    public static final int F3_SUBW = 0b000;
    public static final int F3_SLLW = 0b001;
    public static final int F3_SRLW = 0b101;
    public static final int F3_SRAW = 0b101;

    // RV32/RV64 Zifencei
    public static final int F3_FENCEI = 0b001;

    // RV32/RV64 Zicsr
    public static final int F3_CSRRW = 0b001;
    public static final int F3_CSRRS = 0b010;
    public static final int F3_CSRRC = 0b011;
    public static final int F3_CSRRWI = 0b101;
    public static final int F3_CSRRSI = 0b110;
    public static final int F3_CSRRCI = 0b111;

    // RV32M
    public static final int F3_MUL = 0b000;
    public static final int F3_MULH = 0b001;
    public static final int F3_MULHSU = 0b010;
    public static final int F3_MULHU = 0b011;
    public static final int F3_DIV = 0b100;
    public static final int F3_DIVU = 0b101;
    public static final int F3_REM = 0b110;
    public static final int F3_REMU = 0b111;

    // RV64M
    public static final int F3_MULW = 0b000;
    public static final int F3_DIVW = 0b100;
    public static final int F3_DIVuW = 0b101;
    public static final int F3_REMW = 0b110;
    public static final int F3_REMUW = 0b111;

    // RVC
    public static final int F3_CADDI4SPN = 0b000;
    public static final int F3_CFLD = 0b001; // RV32/64
    public static final int F3_CLQ = 0b001; // RV128
    public static final int F3_CLW = 0b010;
    public static final int F3_CFLW = 0b011; // RV32
    public static final int F3_CLD = 0b011; // RV64/128
    public static final int F3_CFSD = 0b101; // RV32/64
    public static final int F3_CSQ = 0b101; // RV128
    public static final int F3_CSW = 0b110;
    public static final int F3_CFSW = 0b111; // RV32
    public static final int F3_CSD = 0b111; // RV64/128

    public static final int F3_CNOP = 0b000;
    public static final int F3_CADDI = 0b000;
    public static final int F3_CJAL = 0b001; // RV32
    public static final int F3_CADDIW = 0b001; // RV64/128
    public static final int F3_CLI = 0b010;
    public static final int F3_CADDI16SP = 0b011;
    public static final int F3_CLUI = 0b011;
    public static final int F3_CARITHMETIC = 0b100;
    public static final int XOP_CSRLI = 0b00;
    public static final int XOP_CSRLI64 = 0b00;
    public static final int XOP_CSRAI = 0b01;
    public static final int XOP_CSRAI64 = 0b01;
    public static final int XOP_CANDI = 0b10;
    public static final int XOP_CARITH = 0b11;
    public static final int F2_CSUB = 0b00;
    public static final int F2_CXOR = 0b01;
    public static final int F2_COR = 0b10;
    public static final int F2_CAND = 0b11;
    public static final int F2_CSUBW = 0b00;
    public static final int F2_CADDW = 0b01;
    public static final int F3_CJ = 0b101;
    public static final int F3_CBEQZ = 0b110;
    public static final int F3_CBNEZ = 0b111;

    public static final int F3_CSLLI = 0b000;
    public static final int F3_CSLLI64 = 0b000;
    public static final int F3_CFLDSP = 0b001;
    public static final int F3_CLQSP = 0b001;
    public static final int F3_CLWSP = 0b010;
    public static final int F3_CFLWSP = 0b011;
    public static final int F3_CLDSP = 0b011;
    public static final int F3_CJR = 0b100;
    public static final int F3_CMV = 0b100;
    public static final int F3_CEBREAK = 0b100;
    public static final int F3_CJALR = 0b100;
    public static final int F3_CADD = 0b100;
    public static final int F3_CFSDSP = 0b101;
    public static final int F3_CSQSP = 0b101;
    public static final int F3_CSWSP = 0b110;
    public static final int F3_CFSWSP = 0b111;
    public static final int F3_CSDSP = 0b111;
}
