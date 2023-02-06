package org.graalvm.vm.trcview.arch.pdp11.disasm;

import static org.graalvm.vm.trcview.disasm.Type.ADDRESS;
import static org.graalvm.vm.trcview.disasm.Type.LABEL;
import static org.graalvm.vm.trcview.disasm.Type.NUMBER;
import static org.graalvm.vm.trcview.disasm.Type.OFFSET;
import static org.graalvm.vm.trcview.disasm.Type.OTHER;
import static org.graalvm.vm.trcview.disasm.Type.REGISTER;

import java.util.ArrayList;
import java.util.List;

import org.graalvm.vm.trcview.arch.CodeReader;
import org.graalvm.vm.trcview.arch.Disassembler;
import org.graalvm.vm.trcview.arch.ShortCodeReader;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.disasm.AssemblerInstruction;
import org.graalvm.vm.trcview.disasm.Field;
import org.graalvm.vm.trcview.disasm.Operand;
import org.graalvm.vm.trcview.disasm.Token;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public class PDP11Disassembler extends Disassembler {
    private static final Field RN = Field.getLE(2, 0);
    private static final Field MODE = Field.getLE(5, 3);

    private static final Field SRC_RN = Field.getLE(8, 6);
    private static final Field SRC_MODE = Field.getLE(11, 9);

    private static final Field BR_OFFSET = Field.getLE(7, 0, true);

    private static final Field JSR_R = Field.getLE(8, 6);

    private static final Field SOB_OFFSET = Field.getLE(5, 0);

    public PDP11Disassembler() {
        super();
    }

    public PDP11Disassembler(TraceAnalyzer trc) {
        super(trc);
    }

    private static String writeN(short val) {
        return Integer.toOctalString(Short.toUnsignedInt(val));
    }

    private static String writeO(short val) {
        if (val < 0 && val > -128) {
            return '-' + writeN((short) -val);
        } else {
            return writeN(val);
        }
    }

    private static String writeRN(int r) {
        StringBuilder buf = new StringBuilder();
        if (r < 0 || r > 7) {
            throw new IllegalArgumentException("invalid register");
        }
        if (r == 6) {
            buf.append("SP");
        } else if (r == 7) {
            buf.append("PC");
        } else {
            buf.append('R');
            buf.append((char) (r + '0'));
        }
        return buf.toString();
    }

    private Operand disassemblePCOperand(int mode, CodeReader code) {
        int addr;
        String name;
        switch (mode) {
            case 2: // #<imm>
                code.pc += 2;
                return new Operand(new Token(OTHER, "#"), new Token(NUMBER, writeN(code.nextI16())));
            case 3: // @#<addr>
                code.pc += 2;
                addr = Short.toUnsignedInt(code.nextI16());
                name = getName(addr);
                if (name != null) {
                    return new Operand(new Token(LABEL, name));
                } else {
                    return new Operand(new Token(OTHER, "@#"), new Token(ADDRESS, writeN((short) addr)));
                }
            case 6: // <offset>(PC)
                code.pc += 2;
                addr = (int) (code.pc + code.nextI16()) & 0xFFFF;
                name = getName(addr);
                if (name != null) {
                    return new Operand(new Token(LABEL, name));
                } else {
                    return new Operand(new Token(ADDRESS, writeN((short) addr)));
                }
            case 7: // @<offset>(PC)
                code.pc += 2;
                addr = (int) (code.pc + code.nextI16()) & 0xFFFF;
                name = getName(addr);
                if (name != null) {
                    return new Operand(new Token(OTHER, "@"), new Token(LABEL, name));
                } else {
                    return new Operand(new Token(OTHER, "@#"), new Token(ADDRESS, writeN((short) addr)));
                }
            default:
                throw new IllegalArgumentException("invalid operand mode pc with mode=" + mode);
        }
    }

    private Operand disassembleOperand(int rn, int mode, CodeReader code) {
        if (rn == 7 && ((mode & 6) == 2 || (mode & 6) == 6)) {
            return disassemblePCOperand(mode, code);
        }
        switch (mode) {
            case 0: // Rn
                return new Operand(new Token(REGISTER, writeRN(rn)));
            case 1: // (Rn)
                return new Operand(new Token(OTHER, "("), new Token(REGISTER, writeRN(rn)), new Token(OTHER, ")"));
            case 3: // @(Rn)+
                return new Operand(new Token(OTHER, "@("), new Token(REGISTER, writeRN(rn)), new Token(OTHER, ")+"));
            case 2: // (Rn)+
                return new Operand(new Token(OTHER, "("), new Token(REGISTER, writeRN(rn)), new Token(OTHER, ")+"));
            case 5: // @-(Rn)
                return new Operand(new Token(OTHER, "@-("), new Token(REGISTER, writeRN(rn)), new Token(OTHER, ")"));
            case 4: // -(Rn)
                return new Operand(new Token(OTHER, "-("), new Token(REGISTER, writeRN(rn)), new Token(OTHER, ")"));
            case 7: // @<offset>(Rn)
                code.pc += 2;
                return new Operand(new Token(OTHER, "@"), new Token(OFFSET, writeO(code.nextI16())), new Token(OTHER, "("), new Token(REGISTER, writeRN(rn)), new Token(OTHER, ")"));
            case 6: // <offset>(Rn)
                code.pc += 2;
                return new Operand(new Token(OFFSET, writeO(code.nextI16())), new Token(OTHER, "("), new Token(REGISTER, writeRN(rn)), new Token(OTHER, ")"));
            default:
                throw new IllegalArgumentException("invalid operand mode " + mode);
        }
    }

    private Operand disassembleBranch(short offset, short pc) {
        short off = (short) (offset * 2);
        List<Token> tokens = new ArrayList<>();
        if (pc == 0xFFFF) {
            tokens.add(new Token(OTHER, "."));
            if (offset >= 0) {
                tokens.add(new Token(OFFSET, "+" + writeN(off)));
            } else {
                tokens.add(new Token(OFFSET, "-" + writeN((short) -off)));
            }
        } else {
            int addr = (pc + off) & 0xFFFF;
            String name = getLocation(addr);
            if (name != null) {
                tokens.add(new Token(LABEL, name));
            } else {
                tokens.add(new Token(LABEL, "L" + writeN((short) addr)));
            }
        }
        return new Operand(tokens.toArray(new Token[tokens.size()]));
    }

    private Operand[] op1(short opcd, CodeReader code) {
        return new Operand[]{disassembleOperand(RN.get(opcd), MODE.get(opcd), code)};
    }

    private Operand[] op2(short opcd, CodeReader code) {
        return new Operand[]{
                        disassembleOperand(SRC_RN.get(opcd), SRC_MODE.get(opcd), code),
                        disassembleOperand(RN.get(opcd), MODE.get(opcd), code)
        };
    }

    private Operand br(short opcd, CodeReader code) {
        return disassembleBranch((short) BR_OFFSET.get(opcd), (short) code.pc);
    }

    private static class DisasmResult {
        public final String mnemonic;
        public final Operand[] operands;
        public final int len;

        DisasmResult(String mnemonic, int len) {
            this(mnemonic, new Operand[0], len);
        }

        DisasmResult(String mnemonic, Operand[] operands, int len) {
            this.mnemonic = mnemonic;
            this.operands = operands;
            this.len = len;
        }
    }

    private DisasmResult disasm(CodeReader code) {
        short opcd = code.nextI16();

        short pc = (short) code.pc;
        code.pc += 2;

        switch (opcd & 0177700) {
            case 0005000: /* CLR */
                return new DisasmResult("CLR", op1(opcd, code), code.n() >> 1);
            case 0105000: /* CLRB */
                return new DisasmResult("CLRB", op1(opcd, code), code.n() >> 1);
            case 0005100: /* COM */
                return new DisasmResult("COM", op1(opcd, code), code.n() >> 1);
            case 0105100: /* COMB */
                return new DisasmResult("COMB", op1(opcd, code), code.n() >> 1);
            case 0005200: /* INC */
                return new DisasmResult("INC", op1(opcd, code), code.n() >> 1);
            case 0105200: /* INCB */
                return new DisasmResult("INCB", op1(opcd, code), code.n() >> 1);
            case 0005300: /* DEC */
                return new DisasmResult("DEC", op1(opcd, code), code.n() >> 1);
            case 0105300: /* DECB */
                return new DisasmResult("DECB", op1(opcd, code), code.n() >> 1);
            case 0005400: /* NEG */
                return new DisasmResult("NEG", op1(opcd, code), code.n() >> 1);
            case 0105400: /* NEGB */
                return new DisasmResult("NEGB", op1(opcd, code), code.n() >> 1);
            case 0005700: /* TST */
                return new DisasmResult("TST", op1(opcd, code), code.n() >> 1);
            case 0105700: /* TSTB */
                return new DisasmResult("TSTB", op1(opcd, code), code.n() >> 1);
            case 0006200: /* ASR */
                return new DisasmResult("ASR", op1(opcd, code), code.n() >> 1);
            case 0106200: /* ASRB */
                return new DisasmResult("ASRB", op1(opcd, code), code.n() >> 1);
            case 0006300: /* ASL */
                return new DisasmResult("ASL", op1(opcd, code), code.n() >> 1);
            case 0106300: /* ASLB */
                return new DisasmResult("ASLB", op1(opcd, code), code.n() >> 1);
            case 0006000: /* ROR */
                return new DisasmResult("ROR", op1(opcd, code), code.n() >> 1);
            case 0106000: /* RORB */
                return new DisasmResult("RORB", op1(opcd, code), code.n() >> 1);
            case 0006100: /* ROL */
                return new DisasmResult("ROL", op1(opcd, code), code.n() >> 1);
            case 0106100: /* ROLB */
                return new DisasmResult("ROLB", op1(opcd, code), code.n() >> 1);
            case 0000300: /* SWAB */
                return new DisasmResult("SWAB", op1(opcd, code), code.n() >> 1);
            case 0005500: /* ADC */
                return new DisasmResult("ADC", op1(opcd, code), code.n() >> 1);
            case 0105500: /* ADCB */
                return new DisasmResult("ADCB", op1(opcd, code), code.n() >> 1);
            case 0005600: /* SBC */
                return new DisasmResult("SBC", op1(opcd, code), code.n() >> 1);
            case 0105600: /* SBCB */
                return new DisasmResult("SBCB", op1(opcd, code), code.n() >> 1);
            case 0006700: /* SXT */
                return new DisasmResult("SXT", op1(opcd, code), code.n() >> 1);
            case 0106700: /* MFPS */
                return new DisasmResult("MFPS", op1(opcd, code), code.n() >> 1);
            case 0106400: /* MTPS */
                return new DisasmResult("MTPS", op1(opcd, code), code.n() >> 1);
            case 0000100: /* JMP */
                return new DisasmResult("JMP", op1(opcd, code), code.n() >> 1);
            case 0006400: /* MARK */
                return new DisasmResult("MARK", new Operand[]{new Operand(NUMBER, writeN((short) (opcd & 077)))}, code.n() >> 1);
        }

        switch (opcd & 0170000) {
            case 0010000: /* MOV */
                return new DisasmResult("MOV", op2(opcd, code), code.n() >> 1);
            case 0110000: /* MOVB */
                return new DisasmResult("MOVB", op2(opcd, code), code.n() >> 1);
            case 0020000: /* CMP */
                return new DisasmResult("CMP", op2(opcd, code), code.n() >> 1);
            case 0120000: /* CMPB */
                return new DisasmResult("CMPB", op2(opcd, code), code.n() >> 1);
            case 0060000: /* ADD */
                return new DisasmResult("ADD", op2(opcd, code), code.n() >> 1);
            case 0160000: /* SUB */
                return new DisasmResult("SUB", op2(opcd, code), code.n() >> 1);
            case 0030000: /* BIT */
                return new DisasmResult("BIT", op2(opcd, code), code.n() >> 1);
            case 0130000: /* BITB */
                return new DisasmResult("BITB", op2(opcd, code), code.n() >> 1);
            case 0040000: /* BIC */
                return new DisasmResult("BIC", op2(opcd, code), code.n() >> 1);
            case 0140000: /* BICB */
                return new DisasmResult("BICB", op2(opcd, code), code.n() >> 1);
            case 0050000: /* BIS */
                return new DisasmResult("BIS", op2(opcd, code), code.n() >> 1);
            case 0150000: /* BISB */
                return new DisasmResult("BISB", op2(opcd, code), code.n() >> 1);
        }

        switch (opcd & 0177000) {
            case 0074000: /* XOR */
                return new DisasmResult("XOR", new Operand[]{new Operand(REGISTER, writeRN((short) JSR_R.get(opcd))), op1(opcd, code)[0]}, code.n() >> 1);
            case 0004000: /* JSR */
                return new DisasmResult("JSR", new Operand[]{new Operand(REGISTER, writeRN((short) JSR_R.get(opcd))), op1(opcd, code)[0]}, code.n() >> 1);
            case 0077000: { /* SOB */
                Operand reg = new Operand(REGISTER, writeRN((short) JSR_R.get(opcd)));
                int addr = (pc - SOB_OFFSET.get(opcd) * 2 + 2) & 0xFFFF;
                String loc = getLocation(addr);
                if (loc != null) {
                    return new DisasmResult("SOB", new Operand[]{reg, new Operand(LABEL, loc)}, code.n() >> 1);
                } else {
                    return new DisasmResult("SOB", new Operand[]{reg, new Operand(LABEL, "L" + writeN((short) addr))}, code.n() >> 1);
                }
            }
            case 0070000: /* MUL */
                return new DisasmResult("MUL", new Operand[]{op1(opcd, code)[0], new Operand(REGISTER, writeRN((short) JSR_R.get(opcd)))}, code.n() >> 1);
            case 0071000: /* DIV */
                return new DisasmResult("DIV", new Operand[]{op1(opcd, code)[0], new Operand(REGISTER, writeRN((short) JSR_R.get(opcd)))}, code.n() >> 1);
            case 0072000: /* ASH */
                return new DisasmResult("ASH", new Operand[]{op1(opcd, code)[0], new Operand(REGISTER, writeRN((short) JSR_R.get(opcd)))}, code.n() >> 1);
            case 0073000: /* ASHC */
                return new DisasmResult("ASHC", new Operand[]{op1(opcd, code)[0], new Operand(REGISTER, writeRN((short) JSR_R.get(opcd)))}, code.n() >> 1);
        }

        switch (opcd & 0177770) {
            case 0000200: /* RTS */
                return new DisasmResult("RTS", new Operand[]{new Operand(REGISTER, writeRN((short) RN.get(opcd)))}, code.n() >> 1);
            case 0075000: /* FADD */
                return new DisasmResult("FADD", new Operand[]{new Operand(REGISTER, writeRN((short) RN.get(opcd)))}, code.n() >> 1);
            case 0075010: /* FSUB */
                return new DisasmResult("FSUB", new Operand[]{new Operand(REGISTER, writeRN((short) RN.get(opcd)))}, code.n() >> 1);
            case 0075020: /* FMUL */
                return new DisasmResult("FMUL", new Operand[]{new Operand(REGISTER, writeRN((short) RN.get(opcd)))}, code.n() >> 1);
            case 0075030: /* FDIV */
                return new DisasmResult("FDIV", new Operand[]{new Operand(REGISTER, writeRN((short) RN.get(opcd)))}, code.n() >> 1);
        }

        switch (opcd & 0177400) {
            case 0000400: /* BR */
                return new DisasmResult("BR", new Operand[]{br(opcd, code)}, code.n() >> 1);
            case 0001000: /* BNE */
                return new DisasmResult("BNE", new Operand[]{br(opcd, code)}, code.n() >> 1);
            case 0001400: /* BEQ */
                return new DisasmResult("BEQ", new Operand[]{br(opcd, code)}, code.n() >> 1);
            case 0100000: /* BPL */
                return new DisasmResult("BPL", new Operand[]{br(opcd, code)}, code.n() >> 1);
            case 0100400: /* BMI */
                return new DisasmResult("BMI", new Operand[]{br(opcd, code)}, code.n() >> 1);
            case 0102000: /* BVC */
                return new DisasmResult("BVC", new Operand[]{br(opcd, code)}, code.n() >> 1);
            case 0102400: /* BVS */
                return new DisasmResult("BVS", new Operand[]{br(opcd, code)}, code.n() >> 1);
            case 0103000: /* BCC */
                return new DisasmResult("BCC", new Operand[]{br(opcd, code)}, code.n() >> 1);
            case 0103400: /* BCS */
                return new DisasmResult("BCS", new Operand[]{br(opcd, code)}, code.n() >> 1);
            case 0002000: /* BGE */
                return new DisasmResult("BGE", new Operand[]{br(opcd, code)}, code.n() >> 1);
            case 0002400: /* BLT */
                return new DisasmResult("BLT", new Operand[]{br(opcd, code)}, code.n() >> 1);
            case 0003000: /* BGT */
                return new DisasmResult("BGT", new Operand[]{br(opcd, code)}, code.n() >> 1);
            case 0003400: /* BLE */
                return new DisasmResult("BLE", new Operand[]{br(opcd, code)}, code.n() >> 1);
            case 0101000: /* BHI */
                return new DisasmResult("BHI", new Operand[]{br(opcd, code)}, code.n() >> 1);
            case 0101400: /* BLOS */
                return new DisasmResult("BLOS", new Operand[]{br(opcd, code)}, code.n() >> 1);
            case 0104000: /* EMT */
                if ((opcd & 0377) != 0) {
                    return new DisasmResult("EMT", new Operand[]{new Operand(NUMBER, writeN((short) (opcd & 0377)))}, code.n() >> 1);
                } else {
                    return new DisasmResult("EMT", code.n() >> 1);
                }
            case 0104400: /* TRAP */
                if ((opcd & 0377) != 0) {
                    return new DisasmResult("TRAP", new Operand[]{new Operand(NUMBER, writeN((short) (opcd & 0377)))}, code.n() >> 1);
                } else {
                    return new DisasmResult("TRAP", code.n() >> 1);
                }
        }

        switch (opcd) {
            case 0000003: /* BPT */
                return new DisasmResult("BPT", code.n() >> 1);
            case 0000004: /* IOT */
                return new DisasmResult("IOT", code.n() >> 1);
            case 0000002: /* RTI */
                return new DisasmResult("RTI", code.n() >> 1);
            case 0000006: /* RTT */
                return new DisasmResult("RTT", code.n() >> 1);
            case 0000000: /* HALT */
                return new DisasmResult("HALT", code.n() >> 1);
            case 0000001: /* WAIT */
                return new DisasmResult("WAIT", code.n() >> 1);
            case 0000005: /* RESET */
                return new DisasmResult("RESET", code.n() >> 1);
            case 0000240: /* NOP */
                return new DisasmResult("NOP", code.n() >> 1);
            case 0000241: /* CLC */
                return new DisasmResult("CLC", code.n() >> 1);
            case 0000242: /* CLV */
                return new DisasmResult("CLV", code.n() >> 1);
            case 0000243: /* CLVC */
                return new DisasmResult("CLVC", code.n() >> 1);
            case 0000244: /* CLZ */
                return new DisasmResult("CLZ", code.n() >> 1);
            case 0000250: /* CLN */
                return new DisasmResult("CLN", code.n() >> 1);
            case 0000257: /* CCC */
                return new DisasmResult("CCC", code.n() >> 1);
            case 0000260: /* NOP1 */
                return new DisasmResult("NOP1", code.n() >> 1);
            case 0000261: /* SEC */
                return new DisasmResult("SEC", code.n() >> 1);
            case 0000262: /* SEV */
                return new DisasmResult("SEV", code.n() >> 1);
            case 0000263: /* SEVC */
                return new DisasmResult("SEVC", code.n() >> 1);
            case 0000264: /* SEZ */
                return new DisasmResult("SEZ", code.n() >> 1);
            case 0000270: /* SEN */
                return new DisasmResult("SEN", code.n() >> 1);
            case 0000277: /* SCC */
                return new DisasmResult("SCC", code.n() >> 1);
        }

        return new DisasmResult(null, 1);
    }

    @Override
    public String[] getDisassembly(CodeReader code) {
        return getDisassembly(disassemble(code));
    }

    @Override
    public AssemblerInstruction disassemble(CodeReader code) {
        DisasmResult result = disasm(code);
        if (result.mnemonic == null) {
            return null;
        } else {
            return new AssemblerInstruction(result.mnemonic, result.operands);
        }
    }

    @Override
    public int getLength(CodeReader code) {
        return disasm(code).len << 1;
    }

    public String[] getDisassembly(short[] code, short pc) {
        String[] disasm = getDisassembly(new ShortCodeReader(code, pc));
        if (disasm != null) {
            return disasm;
        } else {
            StringBuilder buf = new StringBuilder();
            buf.append("; unknown [");
            buf.append(writeN(code[0]));
            buf.append(']');
            return new String[]{buf.toString()};
        }
    }

    public int getLength(short[] code) {
        return getLength(new ShortCodeReader(code, 0)) >> 1;
    }

    @Override
    public InstructionType getType(CodeReader code) {
        return getType(code.nextI16());
    }

    public static InstructionType getType(short opcd) {
        switch (opcd & 0177000) {
            case 0004000: /* JSR */
                return InstructionType.CALL;
            case 0077000: /* SOB */
                return InstructionType.JCC;
        }
        switch (opcd & 0177400) {
            case 0000400: /* BR */
            case 0001000: /* BNE */
            case 0001400: /* BEQ */
            case 0100000: /* BPL */
            case 0100400: /* BMI */
            case 0102000: /* BVC */
            case 0102400: /* BVS */
            case 0103000: /* BCC */
            case 0103400: /* BCS */
            case 0002000: /* BGE */
            case 0002400: /* BLT */
            case 0003000: /* BGT */
            case 0003400: /* BLE */
            case 0101000: /* BHI */
            case 0101400: /* BLOS */
                return InstructionType.JCC;
            case 0104000: /* EMT */
            case 0104400: /* TRAP */
                return InstructionType.SYSCALL;
        }
        switch (opcd & 0177700) {
            case 0000100: /* JMP */
                return InstructionType.JMP;
        }
        switch (opcd & 0177770) {
            case 0000200: /* RTS */
                return InstructionType.RET;
        }
        switch (opcd) {
            case 0000003: /* BPT */
            case 0000004: /* IOT */
                return InstructionType.SYSCALL;
            case 0000002: /* RTI */
            case 0000006: /* RTT */
                return InstructionType.RTI;
        }
        return InstructionType.OTHER;
    }
}
