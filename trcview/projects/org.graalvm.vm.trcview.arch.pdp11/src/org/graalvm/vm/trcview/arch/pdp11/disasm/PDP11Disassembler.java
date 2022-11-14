package org.graalvm.vm.trcview.arch.pdp11.disasm;

import java.util.ArrayList;
import java.util.List;

import org.graalvm.vm.trcview.arch.CodeReader;
import org.graalvm.vm.trcview.arch.Disassembler;
import org.graalvm.vm.trcview.arch.ShortCodeReader;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.disasm.Field;
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

    private static void writeN(StringBuilder buf, short val) {
        buf.append(Integer.toOctalString(Short.toUnsignedInt(val)));
    }

    private static void writeO(StringBuilder buf, short val) {
        if (val < 0 && val > -128) {
            buf.append('-');
            writeN(buf, (short) -val);
        } else {
            writeN(buf, val);
        }
    }

    private static void writeRN(StringBuilder buf, int r) {
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
    }

    private void disassemblePCOperand(int mode, CodeReader code, StringBuilder buf) {
        int addr;
        String name;
        switch (mode) {
            case 2: // #<imm>
                code.pc += 2;
                buf.append('#');
                writeN(buf, code.nextI16());
                break;
            case 3: // @#<addr>
                code.pc += 2;
                addr = Short.toUnsignedInt(code.nextI16());
                name = getName(addr);
                if (name != null) {
                    buf.append(name);
                } else {
                    buf.append("@#");
                    writeN(buf, (short) addr);
                }
                break;
            case 6: // <offset>(PC)
                code.pc += 2;
                addr = (int) (code.pc + code.nextI16()) & 0xFFFF;
                name = getName(addr);
                if (name != null) {
                    buf.append(name);
                } else {
                    writeN(buf, (short) addr);
                }
                break;
            case 7: // @<offset>(PC)
                code.pc += 2;
                addr = (int) (code.pc + code.nextI16()) & 0xFFFF;
                buf.append('@');
                name = getName(addr);
                if (name != null) {
                    buf.append(name);
                } else {
                    buf.append("@#");
                    writeN(buf, (short) addr);
                }
                break;
        }
    }

    private void disassembleOperand(int rn, int mode, CodeReader code, StringBuilder buf) {
        if (rn == 7 && ((mode & 6) == 2 || (mode & 6) == 6)) {
            disassemblePCOperand(mode, code, buf);
            return;
        }
        switch (mode) {
            case 0: // Rn
                writeRN(buf, rn);
                break;
            case 1: // (Rn)
                buf.append('(');
                writeRN(buf, rn);
                buf.append(')');
                break;
            case 3: // @(Rn)+
                buf.append('@');
            case 2: // (Rn)+
                buf.append('(');
                writeRN(buf, rn);
                buf.append(')');
                buf.append('+');
                break;
            case 5: // @-(Rn)
                buf.append('@');
            case 4: // -(Rn)
                buf.append('-');
                buf.append('(');
                writeRN(buf, rn);
                buf.append(')');
                break;
            case 7: // @<offset>(Rn)
                buf.append('@');
            case 6: // <offset>(Rn)
                code.pc += 2;
                writeO(buf, code.nextI16());
                buf.append('(');
                writeRN(buf, rn);
                buf.append(')');
                break;
        }
    }

    private void disassembleBranch(short offset, short pc, StringBuilder buf) {
        short off = (short) (offset * 2);
        if (pc == 0xFFFF) {
            buf.append('.');
            if (offset >= 0) {
                buf.append('+');
                writeN(buf, off);
            } else {
                buf.append('-');
                writeN(buf, (short) -off);
            }
        } else {
            int addr = (pc + off) & 0xFFFF;
            String name = getLocation(addr);
            if (name != null) {
                buf.append(name);
            } else {
                buf.append('L');
                writeN(buf, (short) addr);
            }
        }
    }

    private void op1(List<String> out, short opcd, CodeReader code) {
        StringBuilder buf = new StringBuilder();
        disassembleOperand(RN.get(opcd), MODE.get(opcd), code, buf);
        out.add(buf.toString());
    }

    private void op2(List<String> out, short opcd, CodeReader code) {
        StringBuilder buf = new StringBuilder();
        disassembleOperand(SRC_RN.get(opcd), SRC_MODE.get(opcd), code, buf);
        out.add(buf.toString());
        buf = new StringBuilder();
        disassembleOperand(RN.get(opcd), MODE.get(opcd), code, buf);
        out.add(buf.toString());
    }

    private void br(List<String> out, short opcd, CodeReader code) {
        StringBuilder buf = new StringBuilder();
        disassembleBranch((short) BR_OFFSET.get(opcd), (short) code.pc, buf);
        out.add(buf.toString());
    }

    private int disassemble(CodeReader code, List<String> out) {
        StringBuilder buf = new StringBuilder();
        short opcd = code.nextI16();

        short pc = (short) code.pc;
        code.pc += 2;

        switch (opcd & 0177700) {
            case 0005000: /* CLR */
                out.add("CLR");
                op1(out, opcd, code);
                return code.n() >> 1;
            case 0105000: /* CLRB */
                out.add("CLRB");
                op1(out, opcd, code);
                return code.n() >> 1;
            case 0005100: /* COM */
                out.add("COM");
                op1(out, opcd, code);
                return code.n() >> 1;
            case 0105100: /* COMB */
                out.add("COMB");
                op1(out, opcd, code);
                return code.n() >> 1;
            case 0005200: /* INC */
                out.add("INC");
                op1(out, opcd, code);
                return code.n() >> 1;
            case 0105200: /* INCB */
                out.add("INCB");
                op1(out, opcd, code);
                return code.n() >> 1;
            case 0005300: /* DEC */
                out.add("DEC");
                op1(out, opcd, code);
                return code.n() >> 1;
            case 0105300: /* DECB */
                out.add("DECB");
                op1(out, opcd, code);
                return code.n() >> 1;
            case 0005400: /* NEG */
                out.add("NEG");
                op1(out, opcd, code);
                return code.n() >> 1;
            case 0105400: /* NEGB */
                out.add("NEGB");
                op1(out, opcd, code);
                return code.n() >> 1;
            case 0005700: /* TST */
                out.add("TST");
                op1(out, opcd, code);
                return code.n() >> 1;
            case 0105700: /* TSTB */
                out.add("TSTB");
                op1(out, opcd, code);
                return code.n() >> 1;
            case 0006200: /* ASR */
                out.add("ASR");
                op1(out, opcd, code);
                return code.n() >> 1;
            case 0106200: /* ASRB */
                out.add("ASRB");
                op1(out, opcd, code);
                return code.n() >> 1;
            case 0006300: /* ASL */
                out.add("ASL");
                op1(out, opcd, code);
                return code.n() >> 1;
            case 0106300: /* ASLB */
                out.add("ASLB");
                op1(out, opcd, code);
                return code.n() >> 1;
            case 0006000: /* ROR */
                out.add("ROR");
                op1(out, opcd, code);
                return code.n() >> 1;
            case 0106000: /* RORB */
                out.add("RORB");
                op1(out, opcd, code);
                return code.n() >> 1;
            case 0006100: /* ROL */
                out.add("ROL");
                op1(out, opcd, code);
                return code.n() >> 1;
            case 0106100: /* ROLB */
                out.add("ROLB");
                op1(out, opcd, code);
                return code.n() >> 1;
            case 0000300: /* SWAB */
                out.add("SWAB");
                op1(out, opcd, code);
                return code.n() >> 1;
            case 0005500: /* ADC */
                out.add("ADC");
                op1(out, opcd, code);
                return code.n() >> 1;
            case 0105500: /* ADCB */
                out.add("ADCB");
                op1(out, opcd, code);
                return code.n() >> 1;
            case 0005600: /* SBC */
                out.add("SBC");
                op1(out, opcd, code);
                return code.n() >> 1;
            case 0105600: /* SBCB */
                out.add("SBCB");
                op1(out, opcd, code);
                return code.n() >> 1;
            case 0006700: /* SXT */
                out.add("SXT");
                op1(out, opcd, code);
                return code.n() >> 1;
            case 0106700: /* MFPS */
                out.add("MFPS");
                op1(out, opcd, code);
                return code.n() >> 1;
            case 0106400: /* MTPS */
                out.add("MTPS");
                op1(out, opcd, code);
                return code.n() >> 1;
            case 0000100: /* JMP */
                out.add("JMP");
                op1(out, opcd, code);
                return code.n() >> 1;
            case 0006400: /* MARK */
                out.add("MARK");
                writeN(buf, (short) (opcd & 077));
                out.add(buf.toString());
                return code.n() >> 1;
        }

        switch (opcd & 0170000) {
            case 0010000: /* MOV */
                out.add("MOV");
                op2(out, opcd, code);
                return code.n() >> 1;
            case 0110000: /* MOVB */
                out.add("MOVB");
                op2(out, opcd, code);
                return code.n() >> 1;
            case 0020000: /* CMP */
                out.add("CMP");
                op2(out, opcd, code);
                return code.n() >> 1;
            case 0120000: /* CMPB */
                out.add("CMPB");
                op2(out, opcd, code);
                return code.n() >> 1;
            case 0060000: /* ADD */
                out.add("ADD");
                op2(out, opcd, code);
                return code.n() >> 1;
            case 0160000: /* SUB */
                out.add("SUB");
                op2(out, opcd, code);
                return code.n() >> 1;
            case 0030000: /* BIT */
                out.add("BIT");
                op2(out, opcd, code);
                return code.n() >> 1;
            case 0130000: /* BITB */
                out.add("BITB");
                op2(out, opcd, code);
                return code.n() >> 1;
            case 0040000: /* BIC */
                out.add("BIC");
                op2(out, opcd, code);
                return code.n() >> 1;
            case 0140000: /* BICB */
                out.add("BICB");
                op2(out, opcd, code);
                return code.n() >> 1;
            case 0050000: /* BIS */
                out.add("BIS");
                op2(out, opcd, code);
                return code.n() >> 1;
            case 0150000: /* BISB */
                out.add("BISB");
                op2(out, opcd, code);
                return code.n() >> 1;
        }

        switch (opcd & 0177000) {
            case 0074000: /* XOR */
                out.add("XOR");
                writeRN(buf, (short) JSR_R.get(opcd));
                out.add(buf.toString());
                op1(out, opcd, code);
                return code.n() >> 1;
            case 0004000: /* JSR */
                out.add("JSR");
                writeRN(buf, (short) JSR_R.get(opcd));
                out.add(buf.toString());
                op1(out, opcd, code);
                return code.n() >> 1;
            case 0077000: { /* SOB */
                out.add("SOB");
                writeRN(buf, (short) JSR_R.get(opcd));
                out.add(buf.toString());
                int addr = (pc - SOB_OFFSET.get(opcd) * 2 + 2) & 0xFFFF;
                String loc = getLocation(addr);
                if (loc != null) {
                    out.add(loc);
                } else {
                    buf = new StringBuilder();
                    buf.append('L');
                    writeN(buf, (short) addr);
                    out.add(buf.toString());
                }
                return code.n() >> 1;
            }
            case 0070000: /* MUL */
                out.add("MUL");
                op1(out, opcd, code);
                writeRN(buf, (short) JSR_R.get(opcd));
                out.add(buf.toString());
                return code.n() >> 1;
            case 0071000: /* DIV */
                out.add("DIV");
                op1(out, opcd, code);
                writeRN(buf, (short) JSR_R.get(opcd));
                out.add(buf.toString());
                return code.n() >> 1;
            case 0072000: /* ASH */
                out.add("ASH");
                op1(out, opcd, code);
                writeRN(buf, (short) JSR_R.get(opcd));
                out.add(buf.toString());
                return code.n() >> 1;
            case 0073000: /* ASHC */
                out.add("ASHC");
                op1(out, opcd, code);
                writeRN(buf, (short) JSR_R.get(opcd));
                out.add(buf.toString());
                return code.n() >> 1;
        }

        switch (opcd & 0177770) {
            case 0000200: /* RTS */
                out.add("RTS");
                writeRN(buf, (short) RN.get(opcd));
                out.add(buf.toString());
                return code.n() >> 1;
            case 0075000: /* FADD */
                out.add("FADD");
                writeRN(buf, (short) RN.get(opcd));
                out.add(buf.toString());
                return code.n() >> 1;
            case 0075010: /* FSUB */
                out.add("FSUB");
                writeRN(buf, (short) RN.get(opcd));
                out.add(buf.toString());
                return code.n() >> 1;
            case 0075020: /* FMUL */
                out.add("FMUL");
                writeRN(buf, (short) RN.get(opcd));
                out.add(buf.toString());
                return code.n() >> 1;
            case 0075030: /* FDIV */
                out.add("FDIV");
                writeRN(buf, (short) RN.get(opcd));
                out.add(buf.toString());
                return code.n() >> 1;
        }

        switch (opcd & 0177400) {
            case 0000400: /* BR */
                out.add("BR");
                br(out, opcd, code);
                return code.n() >> 1;
            case 0001000: /* BNE */
                out.add("BNE");
                br(out, opcd, code);
                return code.n() >> 1;
            case 0001400: /* BEQ */
                out.add("BEQ");
                br(out, opcd, code);
                return code.n() >> 1;
            case 0100000: /* BPL */
                out.add("BPL");
                br(out, opcd, code);
                return code.n() >> 1;
            case 0100400: /* BMI */
                out.add("BMI");
                br(out, opcd, code);
                return code.n() >> 1;
            case 0102000: /* BVC */
                out.add("BVC");
                br(out, opcd, code);
                return code.n() >> 1;
            case 0102400: /* BVS */
                out.add("BVS");
                br(out, opcd, code);
                return code.n() >> 1;
            case 0103000: /* BCC */
                out.add("BCC");
                br(out, opcd, code);
                return code.n() >> 1;
            case 0103400: /* BCS */
                out.add("BCS");
                br(out, opcd, code);
                return code.n() >> 1;
            case 0002000: /* BGE */
                out.add("BGE");
                br(out, opcd, code);
                return code.n() >> 1;
            case 0002400: /* BLT */
                out.add("BLT");
                br(out, opcd, code);
                return code.n() >> 1;
            case 0003000: /* BGT */
                out.add("BGT");
                br(out, opcd, code);
                return code.n() >> 1;
            case 0003400: /* BLE */
                out.add("BLE");
                br(out, opcd, code);
                return code.n() >> 1;
            case 0101000: /* BHI */
                out.add("BHI");
                br(out, opcd, code);
                return code.n() >> 1;
            case 0101400: /* BLOS */
                out.add("BLOS");
                br(out, opcd, code);
                return code.n() >> 1;
            case 0104000: /* EMT */
                if ((opcd & 0377) != 0) {
                    out.add("EMT");
                    writeN(buf, (short) (opcd & 0377));
                    out.add(buf.toString());
                } else {
                    out.add("EMT");
                }
                return code.n() >> 1;
            case 0104400: /* TRAP */
                if ((opcd & 0377) != 0) {
                    out.add("TRAP");
                    writeN(buf, (short) (opcd & 0377));
                    out.add(buf.toString());
                } else {
                    out.add("TRAP");
                }
                return code.n() >> 1;
        }

        switch (opcd) {
            case 0000003: /* BPT */
                out.add("BPT");
                return code.n() >> 1;
            case 0000004: /* IOT */
                out.add("IOT");
                return code.n() >> 1;
            case 0000002: /* RTI */
                out.add("RTI");
                return code.n() >> 1;
            case 0000006: /* RTT */
                out.add("RTT");
                return code.n() >> 1;
            case 0000000: /* HALT */
                out.add("HALT");
                return code.n() >> 1;
            case 0000001: /* WAIT */
                out.add("WAIT");
                return code.n() >> 1;
            case 0000005: /* RESET */
                out.add("RESET");
                return code.n() >> 1;
            case 0000240: /* NOP */
                out.add("NOP");
                return code.n() >> 1;
            case 0000241: /* CLC */
                out.add("CLC");
                return code.n() >> 1;
            case 0000242: /* CLV */
                out.add("CLV");
                return code.n() >> 1;
            case 0000243: /* CLVC */
                out.add("CLVC");
                return code.n() >> 1;
            case 0000244: /* CLZ */
                out.add("CLZ");
                return code.n() >> 1;
            case 0000250: /* CLN */
                out.add("CLN");
                return code.n() >> 1;
            case 0000257: /* CCC */
                out.add("CCC");
                return code.n() >> 1;
            case 0000260: /* NOP1 */
                out.add("NOP1");
                return code.n() >> 1;
            case 0000261: /* SEC */
                out.add("SEC");
                return code.n() >> 1;
            case 0000262: /* SEV */
                out.add("SEV");
                return code.n() >> 1;
            case 0000263: /* SEVC */
                out.add("SEVC");
                return code.n() >> 1;
            case 0000264: /* SEZ */
                out.add("SEZ");
                return code.n() >> 1;
            case 0000270: /* SEN */
                out.add("SEN");
                return code.n() >> 1;
            case 0000277: /* SCC */
                out.add("SCC");
                return code.n() >> 1;
        }

        return 1;
    }

    @Override
    public String[] getDisassembly(CodeReader code) {
        List<String> buf = new ArrayList<>(5);
        disassemble(code, buf);
        if (buf.size() == 0) {
            return null;
        } else {
            return buf.toArray(new String[buf.size()]);
        }
    }

    @Override
    public int getLength(CodeReader code) {
        List<String> tmp = new ArrayList<>(5);
        return disassemble(code, tmp) << 1;
    }

    public String[] getDisassembly(short[] code, short pc) {
        String[] disasm = getDisassembly(new ShortCodeReader(code, pc));
        if (disasm != null) {
            return disasm;
        } else {
            StringBuilder buf = new StringBuilder();
            buf.append("; unknown [");
            writeN(buf, code[0]);
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
