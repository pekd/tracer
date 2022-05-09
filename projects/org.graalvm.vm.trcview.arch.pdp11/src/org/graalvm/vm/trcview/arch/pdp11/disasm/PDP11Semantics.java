package org.graalvm.vm.trcview.arch.pdp11.disasm;

import org.graalvm.vm.trcview.data.Semantics;
import org.graalvm.vm.trcview.data.ir.IndexedMemoryOperand;
import org.graalvm.vm.trcview.data.ir.MemoryOperand;
import org.graalvm.vm.trcview.data.ir.Operand;
import org.graalvm.vm.trcview.data.ir.RegisterOperand;
import org.graalvm.vm.trcview.data.type.VariableType;
import org.graalvm.vm.trcview.disasm.Field;

public class PDP11Semantics {
    private static final Field RN = Field.getLE(2, 0);
    private static final Field MODE = Field.getLE(5, 3);

    private static final Field SRC_RN = Field.getLE(8, 6);
    private static final Field SRC_MODE = Field.getLE(11, 9);

    private static final Field JSR_R = Field.getLE(8, 6);

    private static final class Code {
        short[] code;
        short pc;
        short x;

        Code(short[] code, short pc) {
            this.code = code;
            this.pc = pc;
            this.x = 0;
        }

        short next() {
            return code[x++];
        }
    }

    private static Operand getPCOperand(int mode, Code code) {
        switch (mode) {
            case 2: // #<imm>
                code.pc += 2;
                return null;
            case 3: // @#<addr>
                code.pc += 2;
                return new MemoryOperand(Short.toUnsignedLong(code.next()));
            case 6: // <offset>(PC)
                code.pc += 2;
                return new MemoryOperand(Short.toUnsignedLong((short) (code.pc + code.next())));
            case 7: // @<offset>(PC)
                code.pc += 2;
                // TODO: pointer!
                return new MemoryOperand(Short.toUnsignedLong((short) (code.pc + code.next())));
        }
        return null;
    }

    private static Operand getOperand(int rn, int mode, Code code, Semantics semantics, boolean is8bit) {
        VariableType ptrType = is8bit ? VariableType.POINTER_I8 : VariableType.POINTER_I16;
        if (rn == 7 && ((mode & 6) == 2 || (mode & 6) == 6)) {
            return getPCOperand(mode, code);
        }
        switch (mode) {
            case 0: // Rn
                return new RegisterOperand(rn);
            case 1: // (Rn)
                semantics.constraint(new RegisterOperand(rn), ptrType);
                return new IndexedMemoryOperand(rn, 0);
            case 2: // (Rn)+
                semantics.constraint(new RegisterOperand(rn), ptrType);
                return new IndexedMemoryOperand(rn, 0);
            case 3: // @(Rn)+
                semantics.constraint(new RegisterOperand(rn), ptrType);
                // TODO
                return null;
            case 4: // -(Rn)
                semantics.constraint(new RegisterOperand(rn), ptrType);
                return new IndexedMemoryOperand(rn, 0);
            case 5: // @-(Rn)
                semantics.constraint(new RegisterOperand(rn), ptrType);
                // TODO
                return null;
            case 6: // <offset>(Rn)
                code.pc += 2;
                semantics.constraint(new RegisterOperand(rn), ptrType);
                return new IndexedMemoryOperand(rn, code.next());
            case 7: // @<offset>(Rn)
                code.pc += 2;
                semantics.constraint(new RegisterOperand(rn), ptrType);
                // TODO
                return null;
        }
        return null;
    }

    private static Operand op1(short opcd, Code code, Semantics semantics, boolean is8bit) {
        return getOperand(RN.get(opcd), MODE.get(opcd), code, semantics, is8bit);
    }

    private static Operand[] op2(short opcd, Code code, Semantics semantics, boolean is8bit) {
        Operand op1 = getOperand(SRC_RN.get(opcd), SRC_MODE.get(opcd), code, semantics, is8bit);
        Operand op2 = getOperand(RN.get(opcd), MODE.get(opcd), code, semantics, is8bit);
        return new Operand[]{op1, op2};
    }

    private static void add(Semantics semantics, Operand op, VariableType type) {
        if (op != null) {
            semantics.constraint(op, type);
        }
    }

    private static void set(Semantics semantics, Operand op, VariableType type) {
        if (op != null) {
            semantics.set(op, type);
        }
    }

    public static void move(Semantics semantics, Operand src, Operand dst, VariableType type) {
        if (src == null) {
            semantics.set(dst, type);
        } else {
            if (type != VariableType.I16 || !(src instanceof RegisterOperand) || !(dst instanceof RegisterOperand)) {
                semantics.constraint(src, type);
            }
            semantics.move(dst, src);
        }
    }

    public static void getSemantics(Semantics semantics, short[] insn, short pc) {
        Code code = new Code(insn, pc);
        short opcd = code.next();

        code.pc += 2;

        switch (opcd & 0177700) {
            case 0005000: /* CLR */
                set(semantics, op1(opcd, code, semantics, false), VariableType.I16);
                return;
            case 0105000: /* CLRB */
                set(semantics, op1(opcd, code, semantics, true), VariableType.I8);
                return;
            case 0005100: /* COM */
                add(semantics, op1(opcd, code, semantics, false), VariableType.I16);
                return;
            case 0105100: /* COMB */
                add(semantics, op1(opcd, code, semantics, true), VariableType.I8);
                return;
            case 0005200: /* INC */
                add(semantics, op1(opcd, code, semantics, false), VariableType.I16);
                return;
            case 0105200: /* INCB */
                add(semantics, op1(opcd, code, semantics, true), VariableType.I8);
                return;
            case 0005300: /* DEC */
                add(semantics, op1(opcd, code, semantics, false), VariableType.I16);
                return;
            case 0105300: /* DECB */
                add(semantics, op1(opcd, code, semantics, true), VariableType.I8);
                return;
            case 0005400: /* NEG */
                add(semantics, op1(opcd, code, semantics, false), VariableType.S16);
                return;
            case 0105400: /* NEGB */
                add(semantics, op1(opcd, code, semantics, true), VariableType.S8);
                return;
            case 0005700: /* TST */
                add(semantics, op1(opcd, code, semantics, false), VariableType.I16);
                return;
            case 0105700: /* TSTB */
                add(semantics, op1(opcd, code, semantics, true), VariableType.I8);
                return;
            case 0006200: /* ASR */
                add(semantics, op1(opcd, code, semantics, false), VariableType.S16);
                return;
            case 0106200: /* ASRB */
                add(semantics, op1(opcd, code, semantics, true), VariableType.S8);
                return;
            case 0006300: /* ASL */
                add(semantics, op1(opcd, code, semantics, false), VariableType.I16);
                semantics.arithmetic(op1(opcd, code, semantics, false), true);
                return;
            case 0106300: /* ASLB */
                add(semantics, op1(opcd, code, semantics, true), VariableType.I8);
                semantics.arithmetic(op1(opcd, code, semantics, false), true);
                return;
            case 0006000: /* ROR */
                add(semantics, op1(opcd, code, semantics, false), VariableType.I16);
                return;
            case 0106000: /* RORB */
                add(semantics, op1(opcd, code, semantics, true), VariableType.I8);
                return;
            case 0006100: /* ROL */
                add(semantics, op1(opcd, code, semantics, false), VariableType.I16);
                return;
            case 0106100: /* ROLB */
                add(semantics, op1(opcd, code, semantics, true), VariableType.I8);
                return;
            case 0000300: /* SWAB */
                add(semantics, op1(opcd, code, semantics, false), VariableType.I16);
                return;
            case 0005500: /* ADC */
                add(semantics, op1(opcd, code, semantics, false), VariableType.I16);
                return;
            case 0105500: /* ADCB */
                add(semantics, op1(opcd, code, semantics, true), VariableType.I8);
                return;
            case 0005600: /* SBC */
                add(semantics, op1(opcd, code, semantics, false), VariableType.I16);
                return;
            case 0105600: /* SBCB */
                add(semantics, op1(opcd, code, semantics, true), VariableType.I8);
                return;
            case 0006700: /* SXT */
                add(semantics, op1(opcd, code, semantics, false), VariableType.S16);
                return;
            case 0106700: /* MFPS */
                add(semantics, op1(opcd, code, semantics, false), VariableType.U16);
                return;
            case 0106400: /* MTPS */
                add(semantics, op1(opcd, code, semantics, false), VariableType.U16);
                return;
            case 0000100: /* JMP */
                // TODO
                // op1(opcd, code, semantics, false);
                return;
            case 0006400: /* MARK */
                return;
        }

        Operand[] operands;
        switch (opcd & 0170000) {
            case 0010000: /* MOV */
                operands = op2(opcd, code, semantics, false);
                move(semantics, operands[0], operands[1], VariableType.I16);
                return;
            case 0110000: /* MOVB */
                operands = op2(opcd, code, semantics, true);
                move(semantics, operands[0], operands[1], VariableType.I8);
                return;
            case 0020000: /* CMP */
                operands = op2(opcd, code, semantics, false);
                if (operands[0] != null && operands[1] != null) {
                    semantics.constraint(operands[0], VariableType.I16);
                    semantics.constraint(operands[1], VariableType.I16);
                    semantics.unify(operands[0], operands[1]);
                }
                return;
            case 0120000: /* CMPB */
                operands = op2(opcd, code, semantics, true);
                if (operands[0] != null && operands[1] != null) {
                    semantics.constraint(operands[0], VariableType.I8);
                    semantics.constraint(operands[1], VariableType.I8);
                    semantics.unify(operands[0], operands[1]);
                }
                return;
            case 0060000: /* ADD */
                operands = op2(opcd, code, semantics, false);
                if (operands[0] != null && operands[1] != null) {
                    semantics.constraint(operands[0], VariableType.I16);
                    semantics.constraint(operands[1], VariableType.I16);
                    semantics.unify(operands[0], operands[1]);
                    semantics.arithmetic(operands[1], false);
                }
                return;
            case 0160000: /* SUB */
                operands = op2(opcd, code, semantics, false);
                if (operands[0] != null && operands[1] != null) {
                    semantics.constraint(operands[0], VariableType.I16);
                    semantics.constraint(operands[1], VariableType.I16);
                    semantics.unify(operands[0], operands[1]);
                    semantics.arithmetic(operands[1], false);
                }
                return;
            case 0030000: /* BIT */
                operands = op2(opcd, code, semantics, false);
                if (operands[0] != null && operands[1] != null) {
                    semantics.constraint(operands[0], VariableType.I16);
                    semantics.constraint(operands[1], VariableType.I16);
                }
                return;
            case 0130000: /* BITB */
                operands = op2(opcd, code, semantics, true);
                if (operands[0] != null && operands[1] != null) {
                    semantics.constraint(operands[0], VariableType.I8);
                    semantics.constraint(operands[1], VariableType.I8);
                }
                return;
            case 0040000: /* BIC */
                operands = op2(opcd, code, semantics, false);
                if (operands[0] != null && operands[1] != null) {
                    semantics.constraint(operands[0], VariableType.I16);
                    semantics.constraint(operands[1], VariableType.I16);
                }
                return;
            case 0140000: /* BICB */
                operands = op2(opcd, code, semantics, true);
                if (operands[0] != null && operands[1] != null) {
                    semantics.constraint(operands[0], VariableType.I8);
                    semantics.constraint(operands[1], VariableType.I8);
                }
                return;
            case 0050000: /* BIS */
                operands = op2(opcd, code, semantics, false);
                if (operands[0] != null && operands[1] != null) {
                    semantics.constraint(operands[0], VariableType.I16);
                    semantics.constraint(operands[1], VariableType.I16);
                }
                return;
            case 0150000: /* BISB */
                operands = op2(opcd, code, semantics, true);
                if (operands[0] != null && operands[1] != null) {
                    semantics.constraint(operands[0], VariableType.I8);
                    semantics.constraint(operands[1], VariableType.I8);
                }
                return;
        }

        switch (opcd & 0177000) {
            case 0074000: /* XOR */
                semantics.constraint(new RegisterOperand(JSR_R.get(opcd)), VariableType.I16);
                add(semantics, op1(opcd, code, semantics, false), VariableType.I16);
                return;
            case 0004000: /* JSR */
                semantics.constraint(new RegisterOperand(JSR_R.get(opcd)), VariableType.POINTER_I16);
                // op1(opcd, code, semantics, false);
                return;
            case 0077000: /* SOB */
                semantics.constraint(new RegisterOperand(JSR_R.get(opcd)), VariableType.U16);
                return;
            case 0070000: /* MUL */
                // TODO
                add(semantics, op1(opcd, code, semantics, false), VariableType.S16);
                semantics.arithmetic(op1(opcd, code, semantics, false), true);
                semantics.constraint(new RegisterOperand(JSR_R.get(opcd)), VariableType.S16);
                return;
            case 0071000: /* DIV */
                // TODO
                add(semantics, op1(opcd, code, semantics, false), VariableType.S16);
                semantics.constraint(new RegisterOperand(JSR_R.get(opcd)), VariableType.S16);
                return;
            case 0072000: /* ASH */
                // TODO
                add(semantics, op1(opcd, code, semantics, false), VariableType.S16);
                semantics.arithmetic(op1(opcd, code, semantics, false), true);
                // writeRN(buf, (short) JSR_R.get(opcd));
                return;
            case 0073000: /* ASHC */
                // TODO
                add(semantics, op1(opcd, code, semantics, false), VariableType.S16);
                semantics.arithmetic(op1(opcd, code, semantics, false), true);
                // writeRN(buf, (short) JSR_R.get(opcd));
                return;
        }

        switch (opcd & 0177770) {
            case 0000200: /* RTS */
                semantics.constraint(new RegisterOperand(RN.get(opcd)), VariableType.POINTER_I16);
                return;
            case 0075000: /* FADD */
                semantics.constraint(new RegisterOperand(RN.get(opcd)), VariableType.POINTER_F32);
                return;
            case 0075010: /* FSUB */
                semantics.constraint(new RegisterOperand(RN.get(opcd)), VariableType.POINTER_F32);
                return;
            case 0075020: /* FMUL */
                semantics.constraint(new RegisterOperand(RN.get(opcd)), VariableType.POINTER_F32);
                return;
            case 0075030: /* FDIV */
                semantics.constraint(new RegisterOperand(RN.get(opcd)), VariableType.POINTER_F32);
                return;
        }

        switch (opcd & 0177400) {
            // branches are irrelevant for types
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
                return;
            case 0104000: /* EMT */
                // TODO
                return;
            case 0104400: /* TRAP */
                // TODO
                return;
        }

        switch (opcd) {
            case 0000003: /* BPT */
                // TODO
                return;
            case 0000004: /* IOT */
                // TODO
                return;
            case 0000002: /* RTI */
                // TODO
                return;
            case 0000006: /* RTT */
                // TODO
                return;
            case 0000000: /* HALT */
            case 0000001: /* WAIT */
            case 0000005: /* RESET */
            case 0000240: /* NOP */
            case 0000241: /* CLC */
            case 0000242: /* CLV */
            case 0000243: /* CLVC */
            case 0000244: /* CLZ */
            case 0000250: /* CLN */
            case 0000257: /* CCC */
            case 0000260: /* NOP1 */
            case 0000261: /* SEC */
            case 0000262: /* SEV */
            case 0000263: /* SEVC */
            case 0000264: /* SEZ */
            case 0000270: /* SEN */
            case 0000277: /* SCC */
                return;
        }

        // unknown opcode
        return;
    }
}
