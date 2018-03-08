package org.graalvm.vm.x86.isa;

import java.util.Arrays;

import org.graalvm.vm.x86.isa.instruction.Add.Addb;
import org.graalvm.vm.x86.isa.instruction.Add.Addl;
import org.graalvm.vm.x86.isa.instruction.Add.Addq;
import org.graalvm.vm.x86.isa.instruction.Add.Addw;
import org.graalvm.vm.x86.isa.instruction.And.Andb;
import org.graalvm.vm.x86.isa.instruction.And.Andl;
import org.graalvm.vm.x86.isa.instruction.And.Andq;
import org.graalvm.vm.x86.isa.instruction.And.Andw;
import org.graalvm.vm.x86.isa.instruction.Bsf.Bsfl;
import org.graalvm.vm.x86.isa.instruction.Bsf.Bsfq;
import org.graalvm.vm.x86.isa.instruction.Bsf.Bsfw;
import org.graalvm.vm.x86.isa.instruction.Bsr.Bsrl;
import org.graalvm.vm.x86.isa.instruction.Bsr.Bsrq;
import org.graalvm.vm.x86.isa.instruction.Bsr.Bsrw;
import org.graalvm.vm.x86.isa.instruction.Bt.Btl;
import org.graalvm.vm.x86.isa.instruction.Bt.Btq;
import org.graalvm.vm.x86.isa.instruction.Bt.Btw;
import org.graalvm.vm.x86.isa.instruction.Call.CallAbsolute;
import org.graalvm.vm.x86.isa.instruction.Call.CallRelative;
import org.graalvm.vm.x86.isa.instruction.Cdq;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovael;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovaeq;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovaew;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmoval;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovaq;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovaw;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovbel;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovbeq;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovbew;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovbl;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovbq;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovbw;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovel;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmoveq;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovew;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovgel;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovgeq;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovgew;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovgl;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovgq;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovgw;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovlel;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovleq;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovlew;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovll;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovlq;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovlw;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovnel;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovneq;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovnew;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovnol;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovnoq;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovnow;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovnpl;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovnpq;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovnpw;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovnsl;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovnsq;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovnsw;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovol;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovoq;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovow;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovpl;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovpq;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovpw;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovsl;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovsq;
import org.graalvm.vm.x86.isa.instruction.Cmov.Cmovsw;
import org.graalvm.vm.x86.isa.instruction.Cmp.Cmpb;
import org.graalvm.vm.x86.isa.instruction.Cmp.Cmpl;
import org.graalvm.vm.x86.isa.instruction.Cmp.Cmpq;
import org.graalvm.vm.x86.isa.instruction.Cmp.Cmpw;
import org.graalvm.vm.x86.isa.instruction.Cmps.Cmpsb;
import org.graalvm.vm.x86.isa.instruction.Cmps.Cmpsd;
import org.graalvm.vm.x86.isa.instruction.Cmps.Cmpsq;
import org.graalvm.vm.x86.isa.instruction.Cmps.Cmpsw;
import org.graalvm.vm.x86.isa.instruction.Cmpxchg.Cmpxchgl;
import org.graalvm.vm.x86.isa.instruction.Cmpxchg.Cmpxchgq;
import org.graalvm.vm.x86.isa.instruction.Cmpxchg.Cmpxchgw;
import org.graalvm.vm.x86.isa.instruction.Cpuid;
import org.graalvm.vm.x86.isa.instruction.Cqo;
import org.graalvm.vm.x86.isa.instruction.Cwd;
import org.graalvm.vm.x86.isa.instruction.Cxe.Cbw;
import org.graalvm.vm.x86.isa.instruction.Cxe.Cdqe;
import org.graalvm.vm.x86.isa.instruction.Cxe.Cwde;
import org.graalvm.vm.x86.isa.instruction.Dec.Decl;
import org.graalvm.vm.x86.isa.instruction.Dec.Decq;
import org.graalvm.vm.x86.isa.instruction.Dec.Decw;
import org.graalvm.vm.x86.isa.instruction.Div.Divb;
import org.graalvm.vm.x86.isa.instruction.Div.Divl;
import org.graalvm.vm.x86.isa.instruction.Div.Divq;
import org.graalvm.vm.x86.isa.instruction.Div.Divw;
import org.graalvm.vm.x86.isa.instruction.Idiv.Idivb;
import org.graalvm.vm.x86.isa.instruction.Idiv.Idivl;
import org.graalvm.vm.x86.isa.instruction.Idiv.Idivq;
import org.graalvm.vm.x86.isa.instruction.Idiv.Idivw;
import org.graalvm.vm.x86.isa.instruction.Imul.Imul2l;
import org.graalvm.vm.x86.isa.instruction.Imul.Imul2q;
import org.graalvm.vm.x86.isa.instruction.Imul.Imul2w;
import org.graalvm.vm.x86.isa.instruction.Inc.Incl;
import org.graalvm.vm.x86.isa.instruction.Inc.Incq;
import org.graalvm.vm.x86.isa.instruction.Inc.Incw;
import org.graalvm.vm.x86.isa.instruction.Jcc.Ja;
import org.graalvm.vm.x86.isa.instruction.Jcc.Jae;
import org.graalvm.vm.x86.isa.instruction.Jcc.Jb;
import org.graalvm.vm.x86.isa.instruction.Jcc.Jbe;
import org.graalvm.vm.x86.isa.instruction.Jcc.Je;
import org.graalvm.vm.x86.isa.instruction.Jcc.Jg;
import org.graalvm.vm.x86.isa.instruction.Jcc.Jge;
import org.graalvm.vm.x86.isa.instruction.Jcc.Jl;
import org.graalvm.vm.x86.isa.instruction.Jcc.Jle;
import org.graalvm.vm.x86.isa.instruction.Jcc.Jne;
import org.graalvm.vm.x86.isa.instruction.Jcc.Jno;
import org.graalvm.vm.x86.isa.instruction.Jcc.Jnp;
import org.graalvm.vm.x86.isa.instruction.Jcc.Jns;
import org.graalvm.vm.x86.isa.instruction.Jcc.Jo;
import org.graalvm.vm.x86.isa.instruction.Jcc.Jp;
import org.graalvm.vm.x86.isa.instruction.Jcc.Jrcxz;
import org.graalvm.vm.x86.isa.instruction.Jcc.Js;
import org.graalvm.vm.x86.isa.instruction.Jmp.JmpDirect;
import org.graalvm.vm.x86.isa.instruction.Jmp.JmpIndirect;
import org.graalvm.vm.x86.isa.instruction.Lea.Leal;
import org.graalvm.vm.x86.isa.instruction.Lea.Leaq;
import org.graalvm.vm.x86.isa.instruction.Lea.Leaw;
import org.graalvm.vm.x86.isa.instruction.Lods.Lodsb;
import org.graalvm.vm.x86.isa.instruction.Mov.Movb;
import org.graalvm.vm.x86.isa.instruction.Mov.Movl;
import org.graalvm.vm.x86.isa.instruction.Mov.Movq;
import org.graalvm.vm.x86.isa.instruction.Mov.Movw;
import org.graalvm.vm.x86.isa.instruction.Movd.MovdToReg;
import org.graalvm.vm.x86.isa.instruction.Movd.MovqToReg;
import org.graalvm.vm.x86.isa.instruction.Movdqa.MovdqaToReg;
import org.graalvm.vm.x86.isa.instruction.Movdqu.MovdquToReg;
import org.graalvm.vm.x86.isa.instruction.Movsx.Movsbl;
import org.graalvm.vm.x86.isa.instruction.Movsx.Movsbq;
import org.graalvm.vm.x86.isa.instruction.Movsx.Movsbw;
import org.graalvm.vm.x86.isa.instruction.Movsx.Movswl;
import org.graalvm.vm.x86.isa.instruction.Movsx.Movswq;
import org.graalvm.vm.x86.isa.instruction.Movsxd.Movslq;
import org.graalvm.vm.x86.isa.instruction.Movups.MovupsToRM;
import org.graalvm.vm.x86.isa.instruction.Movups.MovupsToReg;
import org.graalvm.vm.x86.isa.instruction.Movzx.Movzbl;
import org.graalvm.vm.x86.isa.instruction.Movzx.Movzbq;
import org.graalvm.vm.x86.isa.instruction.Movzx.Movzbw;
import org.graalvm.vm.x86.isa.instruction.Movzx.Movzwl;
import org.graalvm.vm.x86.isa.instruction.Movzx.Movzwq;
import org.graalvm.vm.x86.isa.instruction.Mul.Mulb;
import org.graalvm.vm.x86.isa.instruction.Mul.Mull;
import org.graalvm.vm.x86.isa.instruction.Mul.Mulq;
import org.graalvm.vm.x86.isa.instruction.Mul.Mulw;
import org.graalvm.vm.x86.isa.instruction.Neg.Negb;
import org.graalvm.vm.x86.isa.instruction.Neg.Negl;
import org.graalvm.vm.x86.isa.instruction.Neg.Negq;
import org.graalvm.vm.x86.isa.instruction.Neg.Negw;
import org.graalvm.vm.x86.isa.instruction.Nop;
import org.graalvm.vm.x86.isa.instruction.Not.Notb;
import org.graalvm.vm.x86.isa.instruction.Not.Notl;
import org.graalvm.vm.x86.isa.instruction.Not.Notq;
import org.graalvm.vm.x86.isa.instruction.Not.Notw;
import org.graalvm.vm.x86.isa.instruction.Or.Orb;
import org.graalvm.vm.x86.isa.instruction.Or.Orl;
import org.graalvm.vm.x86.isa.instruction.Or.Orq;
import org.graalvm.vm.x86.isa.instruction.Or.Orw;
import org.graalvm.vm.x86.isa.instruction.Pcmpeq.Pcmpeq128b;
import org.graalvm.vm.x86.isa.instruction.Pcmpeq.Pcmpeq128d;
import org.graalvm.vm.x86.isa.instruction.Pcmpeq.Pcmpeq128w;
import org.graalvm.vm.x86.isa.instruction.Pmovmskb;
import org.graalvm.vm.x86.isa.instruction.Pop.Popq;
import org.graalvm.vm.x86.isa.instruction.Pop.Popw;
import org.graalvm.vm.x86.isa.instruction.Por;
import org.graalvm.vm.x86.isa.instruction.Pshufd;
import org.graalvm.vm.x86.isa.instruction.Punpckl.Punpcklbw;
import org.graalvm.vm.x86.isa.instruction.Punpckl.Punpcklwd;
import org.graalvm.vm.x86.isa.instruction.Push.Pushb;
import org.graalvm.vm.x86.isa.instruction.Push.Pushq;
import org.graalvm.vm.x86.isa.instruction.Push.Pushw;
import org.graalvm.vm.x86.isa.instruction.Pxor;
import org.graalvm.vm.x86.isa.instruction.Rdtsc;
import org.graalvm.vm.x86.isa.instruction.Rep;
import org.graalvm.vm.x86.isa.instruction.Rep.Repz;
import org.graalvm.vm.x86.isa.instruction.Ret;
import org.graalvm.vm.x86.isa.instruction.Rol.Roll;
import org.graalvm.vm.x86.isa.instruction.Rol.Rolq;
import org.graalvm.vm.x86.isa.instruction.Rol.Rolw;
import org.graalvm.vm.x86.isa.instruction.Ror.Rorl;
import org.graalvm.vm.x86.isa.instruction.Ror.Rorq;
import org.graalvm.vm.x86.isa.instruction.Ror.Rorw;
import org.graalvm.vm.x86.isa.instruction.Sar.Sarl;
import org.graalvm.vm.x86.isa.instruction.Sar.Sarq;
import org.graalvm.vm.x86.isa.instruction.Sar.Sarw;
import org.graalvm.vm.x86.isa.instruction.Setcc.Seta;
import org.graalvm.vm.x86.isa.instruction.Setcc.Setae;
import org.graalvm.vm.x86.isa.instruction.Setcc.Setb;
import org.graalvm.vm.x86.isa.instruction.Setcc.Setbe;
import org.graalvm.vm.x86.isa.instruction.Setcc.Sete;
import org.graalvm.vm.x86.isa.instruction.Setcc.Setg;
import org.graalvm.vm.x86.isa.instruction.Setcc.Setge;
import org.graalvm.vm.x86.isa.instruction.Setcc.Setl;
import org.graalvm.vm.x86.isa.instruction.Setcc.Setle;
import org.graalvm.vm.x86.isa.instruction.Setcc.Setne;
import org.graalvm.vm.x86.isa.instruction.Setcc.Setno;
import org.graalvm.vm.x86.isa.instruction.Setcc.Setnp;
import org.graalvm.vm.x86.isa.instruction.Setcc.Setns;
import org.graalvm.vm.x86.isa.instruction.Setcc.Seto;
import org.graalvm.vm.x86.isa.instruction.Setcc.Setp;
import org.graalvm.vm.x86.isa.instruction.Setcc.Sets;
import org.graalvm.vm.x86.isa.instruction.Shl.Shll;
import org.graalvm.vm.x86.isa.instruction.Shl.Shlq;
import org.graalvm.vm.x86.isa.instruction.Shl.Shlw;
import org.graalvm.vm.x86.isa.instruction.Shr.Shrl;
import org.graalvm.vm.x86.isa.instruction.Shr.Shrq;
import org.graalvm.vm.x86.isa.instruction.Shr.Shrw;
import org.graalvm.vm.x86.isa.instruction.Stos.Stosb;
import org.graalvm.vm.x86.isa.instruction.Stos.Stosd;
import org.graalvm.vm.x86.isa.instruction.Stos.Stosq;
import org.graalvm.vm.x86.isa.instruction.Stos.Stosw;
import org.graalvm.vm.x86.isa.instruction.Sub.Subb;
import org.graalvm.vm.x86.isa.instruction.Sub.Subl;
import org.graalvm.vm.x86.isa.instruction.Sub.Subq;
import org.graalvm.vm.x86.isa.instruction.Sub.Subw;
import org.graalvm.vm.x86.isa.instruction.Syscall;
import org.graalvm.vm.x86.isa.instruction.Test.Testb;
import org.graalvm.vm.x86.isa.instruction.Test.Testl;
import org.graalvm.vm.x86.isa.instruction.Test.Testq;
import org.graalvm.vm.x86.isa.instruction.Test.Testw;
import org.graalvm.vm.x86.isa.instruction.Xor.Xorb;
import org.graalvm.vm.x86.isa.instruction.Xor.Xorl;
import org.graalvm.vm.x86.isa.instruction.Xor.Xorq;
import org.graalvm.vm.x86.isa.instruction.Xor.Xorw;

public class AMD64InstructionDecoder {
    private static final Register[] REG16 = {Register.AX, Register.CX, Register.DX, Register.BX, Register.SP, Register.BP, Register.SI, Register.DI, Register.R8W, Register.R9W, Register.R10W,
                    Register.R11W, Register.R12W, Register.R13W, Register.R14W, Register.R15W};
    private static final Register[] REG32 = {Register.EAX, Register.ECX, Register.EDX, Register.EBX, Register.ESP, Register.EBP, Register.ESI, Register.EDI, Register.R8D, Register.R9D, Register.R10D,
                    Register.R11D, Register.R12D, Register.R13D, Register.R14D, Register.R15D};
    private static final Register[] REG64 = {Register.RAX, Register.RCX, Register.RDX, Register.RBX, Register.RSP, Register.RBP, Register.RSI, Register.RDI, Register.R8, Register.R9, Register.R10,
                    Register.R11, Register.R12, Register.R13, Register.R14, Register.R15};

    public static AMD64Instruction decode(long pc, CodeReader code) {
        byte[] instruction = new byte[16];
        int instructionLength = 0;
        byte op = code.read8();
        instruction[instructionLength++] = op;
        boolean sizeOverride = false;
        // boolean addressOverride = false;
        boolean isREPZ = false;
        SegmentRegister segment = null;
        AMD64RexPrefix rex = null;
        boolean decode = true;
        while (decode) {
            switch (op) {
                case AMD64InstructionPrefix.OPERAND_SIZE_OVERRIDE:
                    sizeOverride = true;
                    op = code.read8();
                    instruction[instructionLength++] = op;
                    break;
                case AMD64InstructionPrefix.ADDRESS_SIZE_OVERRIDE:
                    // addressOverride = true;
                    op = code.read8();
                    instruction[instructionLength++] = op;
                    break;
                case AMD64InstructionPrefix.REPZ:
                    isREPZ = true;
                    op = code.read8();
                    instruction[instructionLength++] = op;
                    break;
                case AMD64InstructionPrefix.SEGMENT_OVERRIDE_CS:
                    segment = SegmentRegister.CS;
                    op = code.read8();
                    instruction[instructionLength++] = op;
                    break;
                case AMD64InstructionPrefix.SEGMENT_OVERRIDE_DS:
                    segment = SegmentRegister.DS;
                    op = code.read8();
                    instruction[instructionLength++] = op;
                    break;
                case AMD64InstructionPrefix.SEGMENT_OVERRIDE_ES:
                    segment = SegmentRegister.ES;
                    op = code.read8();
                    instruction[instructionLength++] = op;
                    break;
                case AMD64InstructionPrefix.SEGMENT_OVERRIDE_FS:
                    segment = SegmentRegister.FS;
                    op = code.read8();
                    instruction[instructionLength++] = op;
                    break;
                case AMD64InstructionPrefix.SEGMENT_OVERRIDE_GS:
                    segment = SegmentRegister.GS;
                    op = code.read8();
                    instruction[instructionLength++] = op;
                    break;
                case AMD64InstructionPrefix.SEGMENT_OVERRIDE_SS:
                    segment = SegmentRegister.SS;
                    op = code.read8();
                    instruction[instructionLength++] = op;
                    break;
                default:
                    decode = false;
            }
        }
        if (AMD64RexPrefix.isREX(op)) {
            rex = new AMD64RexPrefix(op);
            op = code.read8();
            instruction[instructionLength++] = op;
        }
        switch (op) {
            case AMD64Opcode.ADD_A_I: {
                if (rex != null && rex.w) {
                    int imm = code.read32();
                    instruction[instructionLength++] = (byte) imm;
                    instruction[instructionLength++] = (byte) (imm >> 8);
                    instruction[instructionLength++] = (byte) (imm >> 16);
                    instruction[instructionLength++] = (byte) (imm >> 24);
                    return new Addq(pc, Arrays.copyOf(instruction, instructionLength), new RegisterOperand(Register.RAX), imm);
                } else if (sizeOverride) {
                    short imm = code.read16();
                    instruction[instructionLength++] = (byte) imm;
                    instruction[instructionLength++] = (byte) (imm >> 8);
                    return new Addw(pc, Arrays.copyOf(instruction, instructionLength), new RegisterOperand(Register.AX), imm);
                } else {
                    int imm = code.read32();
                    instruction[instructionLength++] = (byte) imm;
                    instruction[instructionLength++] = (byte) (imm >> 8);
                    instruction[instructionLength++] = (byte) (imm >> 16);
                    instruction[instructionLength++] = (byte) (imm >> 24);
                    return new Addl(pc, Arrays.copyOf(instruction, instructionLength), new RegisterOperand(Register.EAX), imm);
                }
            }
            case AMD64Opcode.ADD_RM8_R: {
                Args args = new Args(code, rex, segment);
                return new Addb(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
            }
            case AMD64Opcode.ADD_RM_R: {
                Args args = new Args(code, rex, segment);
                if (rex != null && rex.w) {
                    return new Addq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                } else if (sizeOverride) {
                    return new Addw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                } else {
                    return new Addl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                }
            }
            case AMD64Opcode.ADD_R_RM: {
                Args args = new Args(code, rex, segment);
                if (rex != null && rex.w) {
                    return new Addq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), true);
                }
                if (sizeOverride) {
                    return new Addw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), true);
                } else {
                    return new Addl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), true);
                }
            }
            case AMD64Opcode.AND_A_I: {
                if (rex != null && rex.w) {
                    int imm = code.read32();
                    instruction[instructionLength++] = (byte) imm;
                    instruction[instructionLength++] = (byte) (imm >> 8);
                    instruction[instructionLength++] = (byte) (imm >> 16);
                    instruction[instructionLength++] = (byte) (imm >> 24);
                    return new Andq(pc, Arrays.copyOf(instruction, instructionLength), new RegisterOperand(Register.RAX), imm);
                } else if (sizeOverride) {
                    short imm = code.read16();
                    instruction[instructionLength++] = (byte) imm;
                    instruction[instructionLength++] = (byte) (imm >> 8);
                    return new Andw(pc, Arrays.copyOf(instruction, instructionLength), new RegisterOperand(Register.AX), imm);
                } else {
                    int imm = code.read32();
                    instruction[instructionLength++] = (byte) imm;
                    instruction[instructionLength++] = (byte) (imm >> 8);
                    instruction[instructionLength++] = (byte) (imm >> 16);
                    instruction[instructionLength++] = (byte) (imm >> 24);
                    return new Andl(pc, Arrays.copyOf(instruction, instructionLength), new RegisterOperand(Register.EAX), imm);
                }
            }
            case AMD64Opcode.AND_RM_R: {
                Args args = new Args(code, rex, segment);
                if (rex != null && rex.w) {
                    return new Andq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                } else if (sizeOverride) {
                    return new Andw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                } else {
                    return new Andl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                }
            }
            case AMD64Opcode.AND_R_RM: {
                Args args = new Args(code, rex, segment);
                if (rex != null && rex.w) {
                    return new Andq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), true);
                } else if (sizeOverride) {
                    return new Andw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), true);
                } else {
                    return new Andl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), true);
                }
            }
            case AMD64Opcode.CALL_REL: {
                int rel32 = code.read32();
                instruction[instructionLength++] = (byte) rel32;
                instruction[instructionLength++] = (byte) (rel32 >> 8);
                instruction[instructionLength++] = (byte) (rel32 >> 16);
                instruction[instructionLength++] = (byte) (rel32 >> 24);
                return new CallRelative(pc, Arrays.copyOf(instruction, instructionLength), new ImmediateOperand(rel32));
            }
            case AMD64Opcode.CDQ:
                if (rex != null && rex.w) {
                    return new Cqo(pc, Arrays.copyOf(instruction, instructionLength));
                } else if (sizeOverride) {
                    return new Cwd(pc, Arrays.copyOf(instruction, instructionLength));
                } else {
                    return new Cdq(pc, Arrays.copyOf(instruction, instructionLength));
                }
            case AMD64Opcode.CDQE:
                if (rex != null && rex.w) {
                    return new Cdqe(pc, Arrays.copyOf(instruction, instructionLength));
                } else if (sizeOverride) {
                    return new Cbw(pc, Arrays.copyOf(instruction, instructionLength));
                } else {
                    return new Cwde(pc, Arrays.copyOf(instruction, instructionLength));
                }
            case AMD64Opcode.CMP_AL_I: {
                byte imm = code.read8();
                instruction[instructionLength++] = imm;
                return new Cmpb(pc, Arrays.copyOf(instruction, instructionLength), new RegisterOperand(Register.AL), imm);
            }
            case AMD64Opcode.CMP_A_I: {
                if (rex != null && rex.w) {
                    int imm = code.read32();
                    instruction[instructionLength++] = (byte) imm;
                    instruction[instructionLength++] = (byte) (imm >> 8);
                    instruction[instructionLength++] = (byte) (imm >> 16);
                    instruction[instructionLength++] = (byte) (imm >> 24);
                    return new Cmpq(pc, Arrays.copyOf(instruction, instructionLength), new RegisterOperand(Register.RAX), imm);
                } else if (sizeOverride) {
                    short imm = code.read16();
                    instruction[instructionLength++] = (byte) imm;
                    instruction[instructionLength++] = (byte) (imm >> 8);
                    return new Cmpw(pc, Arrays.copyOf(instruction, instructionLength), new RegisterOperand(Register.AX), imm);
                } else {
                    int imm = code.read32();
                    instruction[instructionLength++] = (byte) imm;
                    instruction[instructionLength++] = (byte) (imm >> 8);
                    instruction[instructionLength++] = (byte) (imm >> 16);
                    instruction[instructionLength++] = (byte) (imm >> 24);
                    return new Cmpl(pc, Arrays.copyOf(instruction, instructionLength), new RegisterOperand(Register.EAX), imm);
                }
            }
            case AMD64Opcode.CMP_RM_I8: {
                Args args = new Args(code, rex, segment);
                switch (args.modrm.getReg()) {
                    case 1: {
                        byte imm = code.read8();
                        return new Orb(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                    }
                    case 4: {
                        byte imm = code.read8();
                        return new Andb(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                    }
                    case 7: {
                        byte imm = code.read8();
                        return new Cmpb(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                    }
                }
                return new IllegalInstruction(pc, Arrays.copyOf(instruction, instructionLength));
            }
            case AMD64Opcode.CMP_RM_R8: {
                Args args = new Args(code, rex, segment);
                return new Cmpb(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
            }
            case AMD64Opcode.CMP_RM_R: {
                Args args = new Args(code, rex, segment);
                if (rex != null && rex.w) {
                    return new Cmpq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                } else if (sizeOverride) {
                    return new Cmpw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                } else {
                    return new Cmpl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                }
            }
            case AMD64Opcode.CMP_R_RM: {
                Args args = new Args(code, rex, segment);
                if (rex != null && rex.w) {
                    return new Cmpq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), true);
                } else if (sizeOverride) {
                    return new Cmpw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), true);
                } else {
                    return new Cmpl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), true);
                }
            }
            case AMD64Opcode.CMPSB: {
                AMD64Instruction cmpsb = new Cmpsb(pc, Arrays.copyOf(instruction, instructionLength));
                if (isREPZ) {
                    return new Repz(pc, Arrays.copyOf(instruction, instructionLength), cmpsb);
                } else {
                    return cmpsb;
                }
            }
            case AMD64Opcode.CMPSD: {
                AMD64Instruction cmp;
                if (rex != null && rex.w) {
                    cmp = new Cmpsq(pc, Arrays.copyOf(instruction, instructionLength));
                } else if (sizeOverride) {
                    cmp = new Cmpsd(pc, Arrays.copyOf(instruction, instructionLength));
                } else {
                    cmp = new Cmpsw(pc, Arrays.copyOf(instruction, instructionLength));
                }
                if (isREPZ) {
                    return new Repz(pc, Arrays.copyOf(instruction, instructionLength), cmp);
                } else {
                    return cmp;
                }
            }
            case AMD64Opcode.INC_RM: { // or: DEC_RM
                Args args = new Args(code, rex, segment);
                switch (args.modrm.getReg()) {
                    case 0: // INC R/M
                        if (rex != null && rex.w) {
                            return new Incq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else if (sizeOverride) {
                            return new Incw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new Incl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                    case 1: // DEC R/M
                        if (rex != null && rex.w) {
                            return new Decq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else if (sizeOverride) {
                            return new Decw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new Decl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                    case 2: // CALL R/M
                        return new CallAbsolute(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                    case 4: // JMP R/M
                        return new JmpIndirect(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                    case 6: // PUSH R/M
                        assert rex == null;
                        assert !sizeOverride;
                        return new Pushq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder().getOperand1(OperandDecoder.R64));
                    default:
                        return new IllegalInstruction(pc, Arrays.copyOf(instruction, instructionLength));
                }
            }
            case AMD64Opcode.JA: {
                byte rel8 = code.read8();
                instruction[instructionLength++] = rel8;
                return new Ja(pc, Arrays.copyOf(instruction, instructionLength), rel8);
            }
            case AMD64Opcode.JAE: {
                byte rel8 = code.read8();
                instruction[instructionLength++] = rel8;
                return new Jae(pc, Arrays.copyOf(instruction, instructionLength), rel8);
            }
            case AMD64Opcode.JB: {
                byte rel8 = code.read8();
                instruction[instructionLength++] = rel8;
                return new Jb(pc, Arrays.copyOf(instruction, instructionLength), rel8);
            }
            case AMD64Opcode.JBE: {
                byte rel8 = code.read8();
                instruction[instructionLength++] = rel8;
                return new Jbe(pc, Arrays.copyOf(instruction, instructionLength), rel8);
            }
            case AMD64Opcode.JRCXZ: {
                byte rel8 = code.read8();
                instruction[instructionLength++] = rel8;
                return new Jrcxz(pc, Arrays.copyOf(instruction, instructionLength), rel8);
            }
            case AMD64Opcode.JE: {
                byte rel8 = code.read8();
                instruction[instructionLength++] = rel8;
                return new Je(pc, Arrays.copyOf(instruction, instructionLength), rel8);
            }
            case AMD64Opcode.JG: {
                byte rel8 = code.read8();
                instruction[instructionLength++] = rel8;
                return new Jg(pc, Arrays.copyOf(instruction, instructionLength), rel8);
            }
            case AMD64Opcode.JGE: {
                byte rel8 = code.read8();
                instruction[instructionLength++] = rel8;
                return new Jge(pc, Arrays.copyOf(instruction, instructionLength), rel8);
            }
            case AMD64Opcode.JL: {
                byte rel8 = code.read8();
                instruction[instructionLength++] = rel8;
                return new Jl(pc, Arrays.copyOf(instruction, instructionLength), rel8);
            }
            case AMD64Opcode.JLE: {
                byte rel8 = code.read8();
                instruction[instructionLength++] = rel8;
                return new Jle(pc, Arrays.copyOf(instruction, instructionLength), rel8);
            }
            case AMD64Opcode.JNE: {
                byte rel8 = code.read8();
                instruction[instructionLength++] = rel8;
                return new Jne(pc, Arrays.copyOf(instruction, instructionLength), rel8);
            }
            case AMD64Opcode.JNO: {
                byte rel8 = code.read8();
                instruction[instructionLength++] = rel8;
                return new Jno(pc, Arrays.copyOf(instruction, instructionLength), rel8);
            }
            case AMD64Opcode.JNP: {
                byte rel8 = code.read8();
                instruction[instructionLength++] = rel8;
                return new Jnp(pc, Arrays.copyOf(instruction, instructionLength), rel8);
            }
            case AMD64Opcode.JNS: {
                byte rel8 = code.read8();
                instruction[instructionLength++] = rel8;
                return new Jns(pc, Arrays.copyOf(instruction, instructionLength), rel8);
            }
            case AMD64Opcode.JO: {
                byte rel8 = code.read8();
                instruction[instructionLength++] = rel8;
                return new Jo(pc, Arrays.copyOf(instruction, instructionLength), rel8);
            }
            case AMD64Opcode.JP: {
                byte rel8 = code.read8();
                instruction[instructionLength++] = rel8;
                return new Jp(pc, Arrays.copyOf(instruction, instructionLength), rel8);
            }
            case AMD64Opcode.JS: {
                byte rel8 = code.read8();
                instruction[instructionLength++] = rel8;
                return new Js(pc, Arrays.copyOf(instruction, instructionLength), rel8);
            }
            case AMD64Opcode.JMP_REL8: {
                byte rel8 = code.read8();
                instruction[instructionLength++] = rel8;
                return new JmpDirect(pc, Arrays.copyOf(instruction, instructionLength), rel8);
            }
            case AMD64Opcode.JMP_REL32: {
                int rel32 = code.read32();
                instruction[instructionLength++] = (byte) rel32;
                instruction[instructionLength++] = (byte) (rel32 >> 8);
                instruction[instructionLength++] = (byte) (rel32 >> 16);
                instruction[instructionLength++] = (byte) (rel32 >> 24);
                return new JmpDirect(pc, Arrays.copyOf(instruction, instructionLength), rel32);
            }
            case AMD64Opcode.LEA: {
                Args args = new Args(code, rex, segment);
                if (rex != null && rex.w) {
                    return new Leaq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                }
                if (sizeOverride) {
                    return new Leaw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                } else {
                    return new Leal(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                }
            }
            case AMD64Opcode.LODSB:
                return new Lodsb(pc, Arrays.copyOf(instruction, instructionLength));
            case AMD64Opcode.MOV_RM_R: {
                Args args = new Args(code, rex, segment);
                if (rex != null && rex.w) {
                    return new Movq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                }
                if (sizeOverride) {
                    return new Movw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                } else {
                    return new Movl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                }
            }
            case AMD64Opcode.MOV_RM_I: {
                Args args = new Args(code, rex, segment);
                if (rex != null && rex.w) {
                    int imm = code.read32();
                    return new Movq(pc, args.getOp2(instruction, instructionLength, new byte[]{(byte) imm, (byte) (imm >> 8), (byte) (imm >> 16), (byte) (imm >> 24)}, 4), args.getOperandDecoder(),
                                    imm);
                }
                if (sizeOverride) {
                    short imm = code.read16();
                    return new Movw(pc, args.getOp2(instruction, instructionLength, new byte[]{(byte) imm, (byte) (imm >> 8)}, 4), args.getOperandDecoder(), imm);
                } else {
                    int imm = code.read32();
                    return new Movl(pc, args.getOp2(instruction, instructionLength, new byte[]{(byte) imm, (byte) (imm >> 8), (byte) (imm >> 16), (byte) (imm >> 24)}, 4), args.getOperandDecoder(),
                                    imm);
                }
            }
            case AMD64Opcode.MOV_RM_I8: {
                Args args = new Args(code, rex, segment);
                byte imm = code.read8();
                return new Movb(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
            }
            case AMD64Opcode.MOV_R_I + 0:
            case AMD64Opcode.MOV_R_I + 1:
            case AMD64Opcode.MOV_R_I + 2:
            case AMD64Opcode.MOV_R_I + 3:
            case AMD64Opcode.MOV_R_I + 4:
            case AMD64Opcode.MOV_R_I + 5:
            case AMD64Opcode.MOV_R_I + 6:
            case AMD64Opcode.MOV_R_I + 7: {
                if (rex != null && rex.w) {
                    long imm = code.read64();
                    instruction[instructionLength++] = (byte) imm;
                    instruction[instructionLength++] = (byte) (imm >> 8);
                    instruction[instructionLength++] = (byte) (imm >> 16);
                    instruction[instructionLength++] = (byte) (imm >> 24);
                    instruction[instructionLength++] = (byte) (imm >> 32);
                    instruction[instructionLength++] = (byte) (imm >> 40);
                    instruction[instructionLength++] = (byte) (imm >> 48);
                    instruction[instructionLength++] = (byte) (imm >> 56);
                    Register reg = getRegister64(op, rex != null ? rex.b : false);
                    return new Movq(pc, Arrays.copyOf(instruction, instructionLength), new RegisterOperand(reg), imm);
                }
                if (sizeOverride) {
                    short imm = code.read16();
                    instruction[instructionLength++] = (byte) imm;
                    instruction[instructionLength++] = (byte) (imm >> 8);
                    Register reg = getRegister16(op, rex != null ? rex.b : false);
                    return new Movw(pc, Arrays.copyOf(instruction, instructionLength), new RegisterOperand(reg), imm);
                } else {
                    int imm = code.read32();
                    instruction[instructionLength++] = (byte) imm;
                    instruction[instructionLength++] = (byte) (imm >> 8);
                    instruction[instructionLength++] = (byte) (imm >> 16);
                    instruction[instructionLength++] = (byte) (imm >> 24);
                    Register reg = getRegister32(op, rex != null ? rex.b : false);
                    return new Movl(pc, Arrays.copyOf(instruction, instructionLength), new RegisterOperand(reg), imm);
                }
            }
            case AMD64Opcode.MOV_RM_R8: {
                Args args = new Args(code, rex, segment);
                return new Movb(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
            }
            case AMD64Opcode.MOV_R_RM: {
                Args args = new Args(code, rex, segment);
                if (rex != null && rex.w) {
                    return new Movq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), true);
                }
                if (sizeOverride) {
                    return new Movw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), true);
                } else {
                    return new Movl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), true);
                }
            }
            case AMD64Opcode.MOVSXD_R_RM: {
                Args args = new Args(code, rex, segment);
                if (rex != null && rex.w) {
                    return new Movslq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                }
                return new IllegalInstruction(pc, Arrays.copyOf(instruction, instructionLength));
            }
            case AMD64Opcode.MUL_RM8: {
                Args args = new Args(code, rex, segment);
                switch (args.modrm.getReg()) {
                    case 0: {
                        byte imm = code.read8();
                        return new Testb(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                    }
                    case 2:
                        return new Notb(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                    case 3:
                        return new Negb(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                    case 4:
                        return new Mulb(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                    case 6:
                        return new Divb(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                    case 7:
                        return new Idivb(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                }
                return new IllegalInstruction(pc, Arrays.copyOf(instruction, instructionLength));
            }
            case AMD64Opcode.MUL_RM: {
                Args args = new Args(code, rex, segment);
                switch (args.modrm.getReg()) {
                    case 0:
                        if (rex != null && rex.w) {
                            int imm = code.read32();
                            byte[] suffix = new byte[]{(byte) imm, (byte) (imm >> 8), (byte) (imm >> 16), (byte) (imm >> 24)};
                            return new Testq(pc, args.getOp2(instruction, instructionLength, suffix, suffix.length), args.getOperandDecoder(), imm);
                        } else if (sizeOverride) {
                            short imm = code.read16();
                            byte[] suffix = new byte[]{(byte) imm, (byte) (imm >> 8)};
                            return new Testw(pc, args.getOp2(instruction, instructionLength, suffix, suffix.length), args.getOperandDecoder(), imm);
                        } else {
                            int imm = code.read32();
                            byte[] suffix = new byte[]{(byte) imm, (byte) (imm >> 8), (byte) (imm >> 16), (byte) (imm >> 24)};
                            return new Testl(pc, args.getOp2(instruction, instructionLength, suffix, suffix.length), args.getOperandDecoder(), imm);
                        }
                    case 2: {
                        if (rex != null && rex.w) {
                            return new Notq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else if (sizeOverride) {
                            return new Notw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new Notl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                    }
                    case 3: {
                        if (rex != null && rex.w) {
                            return new Negq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else if (sizeOverride) {
                            return new Negw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new Negl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                    }
                    case 4:
                        if (rex != null && rex.w) {
                            return new Mulq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else if (sizeOverride) {
                            return new Mulw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new Mull(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                    case 6:
                        if (rex != null && rex.w) {
                            return new Divq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else if (sizeOverride) {
                            return new Divw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new Divl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                    case 7:
                        if (rex != null && rex.w) {
                            return new Idivq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else if (sizeOverride) {
                            return new Idivw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new Idivl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                }
                return new IllegalInstruction(pc, Arrays.copyOf(instruction, instructionLength));
            }
            case AMD64Opcode.NOP:
                return new Nop(pc, Arrays.copyOf(instruction, instructionLength));
            case AMD64Opcode.OR_A_I8: {
                byte imm = code.read8();
                instruction[instructionLength++] = imm;
                return new Orb(pc, Arrays.copyOf(instruction, instructionLength), new RegisterOperand(Register.AL), imm);
            }
            case AMD64Opcode.OR_RM_R: {
                Args args = new Args(code, rex, segment);
                if (rex != null && rex.w) {
                    return new Orq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                } else if (sizeOverride) {
                    return new Orw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                } else {
                    return new Orl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                }
            }
            case AMD64Opcode.OR_R_RM: {
                Args args = new Args(code, rex, segment);
                if (rex != null && rex.w) {
                    return new Orq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), true);
                } else if (sizeOverride) {
                    return new Orw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), true);
                } else {
                    return new Orl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), true);
                }
            }
            case AMD64Opcode.POP_R + 0:
            case AMD64Opcode.POP_R + 1:
            case AMD64Opcode.POP_R + 2:
            case AMD64Opcode.POP_R + 3:
            case AMD64Opcode.POP_R + 4:
            case AMD64Opcode.POP_R + 5:
            case AMD64Opcode.POP_R + 6:
            case AMD64Opcode.POP_R + 7: {
                if (sizeOverride) {
                    Register reg = getRegister16(op, rex != null ? rex.b : false);
                    return new Popw(pc, Arrays.copyOf(instruction, instructionLength), new RegisterOperand(reg));
                } else {
                    Register reg = getRegister64(op, rex != null ? rex.b : false);
                    return new Popq(pc, Arrays.copyOf(instruction, instructionLength), new RegisterOperand(reg));
                }
            }
            case AMD64Opcode.PUSH_R + 0:
            case AMD64Opcode.PUSH_R + 1:
            case AMD64Opcode.PUSH_R + 2:
            case AMD64Opcode.PUSH_R + 3:
            case AMD64Opcode.PUSH_R + 4:
            case AMD64Opcode.PUSH_R + 5:
            case AMD64Opcode.PUSH_R + 6:
            case AMD64Opcode.PUSH_R + 7: {
                if (sizeOverride) {
                    Register reg = getRegister16(op, rex != null ? rex.b : false);
                    return new Pushw(pc, Arrays.copyOf(instruction, instructionLength), new RegisterOperand(reg));
                } else {
                    Register reg = getRegister64(op, rex != null ? rex.b : false);
                    return new Pushq(pc, Arrays.copyOf(instruction, instructionLength), new RegisterOperand(reg));
                }
            }
            case AMD64Opcode.PUSH_I8: {
                byte imm = code.read8();
                instruction[instructionLength++] = imm;
                return new Pushb(pc, Arrays.copyOf(instruction, instructionLength), new ImmediateOperand(imm));
            }
            case AMD64Opcode.RET_NEAR:
                return new Ret(pc, Arrays.copyOf(instruction, instructionLength));
            case AMD64Opcode.SHL_RM_1: {
                Args args = new Args(code, rex, segment);
                switch (args.modrm.getReg()) {
                    case 0: { // ROL r/m,1
                        if (rex != null && rex.w) {
                            return new Rolq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), (byte) 1);
                        } else if (sizeOverride) {
                            return new Rolw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), (byte) 1);
                        } else {
                            return new Roll(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), (byte) 1);
                        }
                    }
                    case 4: {
                        if (rex != null && rex.w) {
                            return new Shlq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), (byte) 1);
                        } else if (sizeOverride) {
                            return new Shlw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), (byte) 1);
                        } else {
                            return new Shll(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), (byte) 1);
                        }
                    }
                    case 5: {
                        if (rex != null && rex.w) {
                            return new Shrq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), (byte) 1);
                        } else if (sizeOverride) {
                            return new Shrw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), (byte) 1);
                        } else {
                            return new Shrl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), (byte) 1);
                        }
                    }
                    case 7: {
                        if (rex != null && rex.w) {
                            return new Sarq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), (byte) 1);
                        } else if (sizeOverride) {
                            return new Sarw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), (byte) 1);
                        } else {
                            return new Sarl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), (byte) 1);
                        }
                    }
                }
                return new IllegalInstruction(pc, args.getOp(instruction, instructionLength));
            }
            case AMD64Opcode.SHL_RM_I: {
                Args args = new Args(code, rex, segment);
                switch (args.modrm.getReg()) {
                    case 0: { // ROL r/m,i8
                        byte imm = code.read8();
                        if (rex != null && rex.w) {
                            return new Rolq(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                        } else if (sizeOverride) {
                            return new Rolw(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                        } else {
                            return new Roll(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                        }
                    }
                    case 1: { // ROR r/m,i8
                        byte imm = code.read8();
                        if (rex != null && rex.w) {
                            return new Rorq(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                        } else if (sizeOverride) {
                            return new Rorw(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                        } else {
                            return new Rorl(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                        }
                    }
                    case 4: {
                        byte imm = code.read8();
                        if (rex != null && rex.w) {
                            return new Shlq(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                        } else if (sizeOverride) {
                            return new Shlw(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                        } else {
                            return new Shll(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                        }
                    }
                    case 5: {
                        byte imm = code.read8();
                        if (rex != null && rex.w) {
                            return new Shrq(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                        } else if (sizeOverride) {
                            return new Shrw(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                        } else {
                            return new Shrl(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                        }
                    }
                    case 7: {
                        byte imm = code.read8();
                        if (rex != null && rex.w) {
                            return new Sarq(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                        } else if (sizeOverride) {
                            return new Sarw(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                        } else {
                            return new Sarl(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                        }
                    }
                }
                return new IllegalInstruction(pc, args.getOp(instruction, instructionLength));
            }
            case AMD64Opcode.SHL_RM_C: {
                Args args = new Args(code, rex, segment);
                switch (args.modrm.getReg()) {
                    case 4: {
                        if (rex != null && rex.w) {
                            return new Shlq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), new RegisterOperand(Register.CL));
                        } else if (sizeOverride) {
                            return new Shlw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), new RegisterOperand(Register.CL));
                        } else {
                            return new Shll(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), new RegisterOperand(Register.CL));
                        }
                    }
                    case 7: {
                        if (rex != null && rex.w) {
                            return new Sarq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), new RegisterOperand(Register.CL));
                        } else if (sizeOverride) {
                            return new Sarw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), new RegisterOperand(Register.CL));
                        } else {
                            return new Sarl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), new RegisterOperand(Register.CL));
                        }
                    }
                }
                return new IllegalInstruction(pc, Arrays.copyOf(instruction, instructionLength));
            }
            case AMD64Opcode.STOSB: {
                AMD64Instruction stosb = new Stosb(pc, Arrays.copyOf(instruction, instructionLength));
                if (isREPZ) {
                    return new Rep(pc, Arrays.copyOf(instruction, instructionLength), stosb);
                } else {
                    return stosb;
                }
            }
            case AMD64Opcode.STOS: {
                AMD64Instruction stos;
                if (rex != null && rex.w) {
                    stos = new Stosq(pc, Arrays.copyOf(instruction, instructionLength));
                } else if (sizeOverride) {
                    stos = new Stosw(pc, Arrays.copyOf(instruction, instructionLength));
                } else {
                    stos = new Stosd(pc, Arrays.copyOf(instruction, instructionLength));
                }
                if (isREPZ) {
                    return new Rep(pc, Arrays.copyOf(instruction, instructionLength), stos);
                } else {
                    return stos;
                }
            }
            case AMD64Opcode.SUB_A_I8: {
                byte imm = code.read8();
                instruction[instructionLength++] = imm;
                return new Subb(pc, Arrays.copyOf(instruction, instructionLength), new RegisterOperand(Register.AL), imm);
            }
            case AMD64Opcode.SUB_A_I: {
                if (rex != null && rex.w) {
                    int imm = code.read32();
                    instruction[instructionLength++] = (byte) imm;
                    instruction[instructionLength++] = (byte) (imm >> 8);
                    instruction[instructionLength++] = (byte) (imm >> 16);
                    instruction[instructionLength++] = (byte) (imm >> 24);
                    return new Subq(pc, Arrays.copyOf(instruction, instructionLength), new RegisterOperand(Register.RAX), imm);
                } else if (sizeOverride) {
                    short imm = code.read16();
                    instruction[instructionLength++] = (byte) imm;
                    instruction[instructionLength++] = (byte) (imm >> 8);
                    return new Subw(pc, Arrays.copyOf(instruction, instructionLength), new RegisterOperand(Register.AX), imm);
                } else {
                    int imm = code.read32();
                    instruction[instructionLength++] = (byte) imm;
                    instruction[instructionLength++] = (byte) (imm >> 8);
                    instruction[instructionLength++] = (byte) (imm >> 16);
                    instruction[instructionLength++] = (byte) (imm >> 24);
                    return new Subl(pc, Arrays.copyOf(instruction, instructionLength), new RegisterOperand(Register.EAX), imm);
                }
            }
            case AMD64Opcode.SUB_RM_R: {
                Args args = new Args(code, rex, segment);
                if (rex != null && rex.w) {
                    return new Subq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                }
                if (sizeOverride) {
                    return new Subw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                } else {
                    return new Subl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                }
            }
            case AMD64Opcode.SUB_R_RM8: {
                Args args = new Args(code, rex, segment);
                return new Subb(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), true);
            }
            case AMD64Opcode.SUB_R_RM: {
                Args args = new Args(code, rex, segment);
                if (rex != null && rex.w) {
                    return new Subq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), true);
                }
                if (sizeOverride) {
                    return new Subw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), true);
                } else {
                    return new Subl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), true);
                }
            }
            case AMD64Opcode.SUB_RM_I8: {
                Args args = new Args(code, rex, segment);
                switch (args.modrm.getReg()) {
                    case 0: { // ADD r/m32 i8
                        byte imm = code.read8();
                        if (rex != null && rex.w) {
                            return new Addq(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                        } else if (sizeOverride) {
                            return new Addw(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                        } else {
                            return new Addl(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                        }
                    }
                    case 1: { // OR r/m32 i8
                        byte imm = code.read8();
                        if (rex != null && rex.w) {
                            return new Orq(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                        } else if (sizeOverride) {
                            return new Orw(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                        } else {
                            return new Orl(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                        }
                    }
                    case 4: { // AND r/m32 i8
                        byte imm = code.read8();
                        if (rex != null && rex.w) {
                            return new Andq(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                        } else if (sizeOverride) {
                            return new Andw(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                        } else {
                            return new Andl(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                        }
                    }
                    case 5: { // SUB r/m32 i8
                        byte imm = code.read8();
                        if (rex != null && rex.w) {
                            return new Subq(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                        } else if (sizeOverride) {
                            return new Subw(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                        } else {
                            return new Subl(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                        }
                    }
                    case 7: { // CMP r/m32 i8
                        byte imm = code.read8();
                        if (rex != null && rex.w) {
                            return new Cmpq(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                        } else if (sizeOverride) {
                            return new Cmpw(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                        } else {
                            return new Cmpl(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                        }
                    }
                }
                return new IllegalInstruction(pc, Arrays.copyOf(instruction, instructionLength));
            }
            case AMD64Opcode.SUB_RM_I: {
                Args args = new Args(code, rex, segment);
                switch (args.modrm.getReg()) {
                    case 0: { // ADD r/m32 i
                        if (rex != null && rex.w) {
                            int imm = code.read32();
                            byte[] suffix = new byte[]{(byte) imm, (byte) (imm >> 8), (byte) (imm >> 16), (byte) (imm >> 24)};
                            return new Addq(pc, args.getOp2(instruction, instructionLength, suffix, suffix.length), args.getOperandDecoder(), imm);
                        } else if (sizeOverride) {
                            short imm = code.read16();
                            byte[] suffix = new byte[]{(byte) imm, (byte) (imm >> 8)};
                            return new Addw(pc, args.getOp2(instruction, instructionLength, suffix, suffix.length), args.getOperandDecoder(), imm);
                        } else {
                            int imm = code.read32();
                            byte[] suffix = new byte[]{(byte) imm, (byte) (imm >> 8), (byte) (imm >> 16), (byte) (imm >> 24)};
                            return new Addl(pc, args.getOp2(instruction, instructionLength, suffix, suffix.length), args.getOperandDecoder(), imm);
                        }
                    }
                    case 1: { // OR r/m32 i
                        if (rex != null && rex.w) {
                            int imm = code.read32();
                            byte[] suffix = new byte[]{(byte) imm, (byte) (imm >> 8), (byte) (imm >> 16), (byte) (imm >> 24)};
                            return new Orq(pc, args.getOp2(instruction, instructionLength, suffix, suffix.length), args.getOperandDecoder(), imm);
                        } else if (sizeOverride) {
                            short imm = code.read16();
                            byte[] suffix = new byte[]{(byte) imm, (byte) (imm >> 8)};
                            return new Orw(pc, args.getOp2(instruction, instructionLength, suffix, suffix.length), args.getOperandDecoder(), imm);
                        } else {
                            int imm = code.read32();
                            byte[] suffix = new byte[]{(byte) imm, (byte) (imm >> 8), (byte) (imm >> 16), (byte) (imm >> 24)};
                            return new Orl(pc, args.getOp2(instruction, instructionLength, suffix, suffix.length), args.getOperandDecoder(), imm);
                        }
                    }
                    case 4: { // AND r/m32 i
                        if (rex != null && rex.w) {
                            int imm = code.read32();
                            byte[] suffix = new byte[]{(byte) imm, (byte) (imm >> 8), (byte) (imm >> 16), (byte) (imm >> 24)};
                            return new Andq(pc, args.getOp2(instruction, instructionLength, suffix, suffix.length), args.getOperandDecoder(), imm);
                        } else if (sizeOverride) {
                            short imm = code.read16();
                            byte[] suffix = new byte[]{(byte) imm, (byte) (imm >> 8)};
                            return new Andw(pc, args.getOp2(instruction, instructionLength, suffix, suffix.length), args.getOperandDecoder(), imm);
                        } else {
                            int imm = code.read32();
                            byte[] suffix = new byte[]{(byte) imm, (byte) (imm >> 8), (byte) (imm >> 16), (byte) (imm >> 24)};
                            return new Andl(pc, args.getOp2(instruction, instructionLength, suffix, suffix.length), args.getOperandDecoder(), imm);
                        }
                    }
                    case 5: { // SUB r/m32 i
                        if (rex != null && rex.w) {
                            int imm = code.read32();
                            byte[] suffix = new byte[]{(byte) imm, (byte) (imm >> 8), (byte) (imm >> 16), (byte) (imm >> 24)};
                            return new Subq(pc, args.getOp2(instruction, instructionLength, suffix, suffix.length), args.getOperandDecoder(), imm);
                        }
                        assert rex == null;
                        if (sizeOverride) {
                            short imm = code.read16();
                            byte[] suffix = new byte[]{(byte) imm, (byte) (imm >> 8)};
                            return new Subw(pc, args.getOp2(instruction, instructionLength, suffix, suffix.length), args.getOperandDecoder(), imm);
                        } else {
                            int imm = code.read32();
                            byte[] suffix = new byte[]{(byte) imm, (byte) (imm >> 8), (byte) (imm >> 16), (byte) (imm >> 24)};
                            return new Subl(pc, args.getOp2(instruction, instructionLength, suffix, suffix.length), args.getOperandDecoder(), imm);
                        }
                    }
                    case 7: { // CMP r/m32 i
                        if (rex != null && rex.w) {
                            int imm = code.read32();
                            byte[] suffix = new byte[]{(byte) imm, (byte) (imm >> 8), (byte) (imm >> 16), (byte) (imm >> 24)};
                            return new Cmpq(pc, args.getOp2(instruction, instructionLength, suffix, suffix.length), args.getOperandDecoder(), imm);
                        } else if (sizeOverride) {
                            short imm = code.read16();
                            byte[] suffix = new byte[]{(byte) imm, (byte) (imm >> 8)};
                            return new Cmpw(pc, args.getOp2(instruction, instructionLength, suffix, suffix.length), args.getOperandDecoder(), imm);
                        } else {
                            int imm = code.read32();
                            byte[] suffix = new byte[]{(byte) imm, (byte) (imm >> 8), (byte) (imm >> 16), (byte) (imm >> 24)};
                            return new Cmpl(pc, args.getOp2(instruction, instructionLength, suffix, suffix.length), args.getOperandDecoder(), imm);
                        }
                    }
                }
                return new IllegalInstruction(pc, Arrays.copyOf(instruction, instructionLength));
            }
            case AMD64Opcode.TEST_AL_I: {
                byte imm = code.read8();
                return new Testb(pc, Arrays.copyOf(instruction, instructionLength), new RegisterOperand(Register.AL), imm);
            }
            case AMD64Opcode.TEST_A_I: {
                if (rex != null && rex.w) {
                    int imm = code.read32();
                    instruction[instructionLength++] = (byte) imm;
                    instruction[instructionLength++] = (byte) (imm >> 8);
                    instruction[instructionLength++] = (byte) (imm >> 16);
                    instruction[instructionLength++] = (byte) (imm >> 24);
                    return new Testq(pc, Arrays.copyOf(instruction, instructionLength), new RegisterOperand(Register.RAX), imm);
                } else if (sizeOverride) {
                    short imm = code.read16();
                    instruction[instructionLength++] = (byte) imm;
                    instruction[instructionLength++] = (byte) (imm >> 8);
                    return new Testw(pc, Arrays.copyOf(instruction, instructionLength), new RegisterOperand(Register.AX), imm);
                } else {
                    int imm = code.read32();
                    instruction[instructionLength++] = (byte) imm;
                    instruction[instructionLength++] = (byte) (imm >> 8);
                    instruction[instructionLength++] = (byte) (imm >> 16);
                    instruction[instructionLength++] = (byte) (imm >> 24);
                    return new Testl(pc, Arrays.copyOf(instruction, instructionLength), new RegisterOperand(Register.EAX), imm);
                }
            }
            case AMD64Opcode.TEST_RM_R8: {
                Args args = new Args(code, rex, segment);
                return new Testb(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
            }
            case AMD64Opcode.TEST_RM_R: {
                Args args = new Args(code, rex, segment);
                if (rex != null && rex.w) {
                    return new Testq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                }
                if (sizeOverride) {
                    return new Testw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                } else {
                    return new Testl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                }
            }
            case AMD64Opcode.XOR_RM_R: {
                Args args = new Args(code, rex, segment);
                if (rex != null && rex.w) {
                    return new Xorq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                }
                if (sizeOverride) {
                    return new Xorw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                } else {
                    return new Xorl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                }
            }
            case AMD64Opcode.XOR_RM8_R8: {
                Args args = new Args(code, rex, segment);
                return new Xorb(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
            }
            case AMD64Opcode.XOR_R_RM: {
                Args args = new Args(code, rex, segment);
                if (rex != null && rex.w) {
                    return new Xorq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), true);
                } else if (sizeOverride) {
                    return new Xorw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), true);
                } else {
                    return new Xorl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), true);
                }
            }
            case AMD64Opcode.ESCAPE: {
                byte op2 = code.read8();
                instruction[instructionLength++] = op2;
                switch (op2) {
                    case AMD64Opcode.BSF_R_RM: {
                        Args args = new Args(code, rex, segment);
                        if (rex != null && rex.w) {
                            return new Bsfq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else if (sizeOverride) {
                            return new Bsfw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new Bsfl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                    }
                    case AMD64Opcode.BSR_R_RM: {
                        Args args = new Args(code, rex, segment);
                        if (rex != null && rex.w) {
                            return new Bsrq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else if (sizeOverride) {
                            return new Bsrw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new Bsrl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                    }
                    case AMD64Opcode.BT_RM_R: {
                        Args args = new Args(code, rex, segment);
                        if (rex != null && rex.w) {
                            return new Btq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else if (sizeOverride) {
                            return new Btw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new Btl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                    }
                    case AMD64Opcode.CMOVA: {
                        Args args = new Args(code, rex, segment);
                        if (rex != null && rex.w) {
                            return new Cmovaq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else if (sizeOverride) {
                            return new Cmovaw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new Cmoval(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                    }
                    case AMD64Opcode.CMOVAE: {
                        Args args = new Args(code, rex, segment);
                        if (rex != null && rex.w) {
                            return new Cmovaeq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else if (sizeOverride) {
                            return new Cmovaew(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new Cmovael(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                    }
                    case AMD64Opcode.CMOVB: {
                        Args args = new Args(code, rex, segment);
                        if (rex != null && rex.w) {
                            return new Cmovbq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else if (sizeOverride) {
                            return new Cmovbw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new Cmovbl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                    }
                    case AMD64Opcode.CMOVBE: {
                        Args args = new Args(code, rex, segment);
                        if (rex != null && rex.w) {
                            return new Cmovbeq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else if (sizeOverride) {
                            return new Cmovbew(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new Cmovbel(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                    }
                    case AMD64Opcode.CMOVE: {
                        Args args = new Args(code, rex, segment);
                        if (rex != null && rex.w) {
                            return new Cmoveq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else if (sizeOverride) {
                            return new Cmovew(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new Cmovel(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                    }
                    case AMD64Opcode.CMOVG: {
                        Args args = new Args(code, rex, segment);
                        if (rex != null && rex.w) {
                            return new Cmovgq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else if (sizeOverride) {
                            return new Cmovgw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new Cmovgl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                    }
                    case AMD64Opcode.CMOVGE: {
                        Args args = new Args(code, rex, segment);
                        if (rex != null && rex.w) {
                            return new Cmovgeq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else if (sizeOverride) {
                            return new Cmovgew(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new Cmovgel(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                    }
                    case AMD64Opcode.CMOVL: {
                        Args args = new Args(code, rex, segment);
                        if (rex != null && rex.w) {
                            return new Cmovlq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else if (sizeOverride) {
                            return new Cmovlw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new Cmovll(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                    }
                    case AMD64Opcode.CMOVLE: {
                        Args args = new Args(code, rex, segment);
                        if (rex != null && rex.w) {
                            return new Cmovleq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else if (sizeOverride) {
                            return new Cmovlew(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new Cmovlel(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                    }
                    case AMD64Opcode.CMOVNE: {
                        Args args = new Args(code, rex, segment);
                        if (rex != null && rex.w) {
                            return new Cmovneq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else if (sizeOverride) {
                            return new Cmovnew(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new Cmovnel(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                    }
                    case AMD64Opcode.CMOVNO: {
                        Args args = new Args(code, rex, segment);
                        if (rex != null && rex.w) {
                            return new Cmovnoq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else if (sizeOverride) {
                            return new Cmovnow(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new Cmovnol(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                    }
                    case AMD64Opcode.CMOVNP: {
                        Args args = new Args(code, rex, segment);
                        if (rex != null && rex.w) {
                            return new Cmovnpq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else if (sizeOverride) {
                            return new Cmovnpw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new Cmovnpl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                    }
                    case AMD64Opcode.CMOVNS: {
                        Args args = new Args(code, rex, segment);
                        if (rex != null && rex.w) {
                            return new Cmovnsq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else if (sizeOverride) {
                            return new Cmovnsw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new Cmovnsl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                    }
                    case AMD64Opcode.CMOVO: {
                        Args args = new Args(code, rex, segment);
                        if (rex != null && rex.w) {
                            return new Cmovoq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else if (sizeOverride) {
                            return new Cmovow(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new Cmovol(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                    }
                    case AMD64Opcode.CMOVP: {
                        Args args = new Args(code, rex, segment);
                        if (rex != null && rex.w) {
                            return new Cmovpq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else if (sizeOverride) {
                            return new Cmovpw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new Cmovpl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                    }
                    case AMD64Opcode.CMOVS: {
                        Args args = new Args(code, rex, segment);
                        if (rex != null && rex.w) {
                            return new Cmovsq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else if (sizeOverride) {
                            return new Cmovsw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new Cmovsl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                    }
                    case AMD64Opcode.CMPXCHG_RM_R: {
                        Args args = new Args(code, rex, segment);
                        if (rex != null && rex.w) {
                            return new Cmpxchgq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else if (sizeOverride) {
                            return new Cmpxchgw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new Cmpxchgl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                    }
                    case AMD64Opcode.CPUID:
                        return new Cpuid(pc, Arrays.copyOf(instruction, instructionLength));
                    case AMD64Opcode.IMUL_R_RM: {
                        Args args = new Args(code, rex, segment);
                        if (rex != null && rex.w) {
                            return new Imul2q(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else if (sizeOverride) {
                            return new Imul2w(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new Imul2l(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                    }
                    case AMD64Opcode.JA32: {
                        int rel32 = code.read32();
                        instruction[instructionLength++] = (byte) rel32;
                        instruction[instructionLength++] = (byte) (rel32 >> 8);
                        instruction[instructionLength++] = (byte) (rel32 >> 16);
                        instruction[instructionLength++] = (byte) (rel32 >> 24);
                        return new Ja(pc, Arrays.copyOf(instruction, instructionLength), rel32);
                    }
                    case AMD64Opcode.JAE32: {
                        int rel32 = code.read32();
                        instruction[instructionLength++] = (byte) rel32;
                        instruction[instructionLength++] = (byte) (rel32 >> 8);
                        instruction[instructionLength++] = (byte) (rel32 >> 16);
                        instruction[instructionLength++] = (byte) (rel32 >> 24);
                        return new Jae(pc, Arrays.copyOf(instruction, instructionLength), rel32);
                    }
                    case AMD64Opcode.JB32: {
                        int rel32 = code.read32();
                        instruction[instructionLength++] = (byte) rel32;
                        instruction[instructionLength++] = (byte) (rel32 >> 8);
                        instruction[instructionLength++] = (byte) (rel32 >> 16);
                        instruction[instructionLength++] = (byte) (rel32 >> 24);
                        return new Jb(pc, Arrays.copyOf(instruction, instructionLength), rel32);
                    }
                    case AMD64Opcode.JBE32: {
                        int rel32 = code.read32();
                        instruction[instructionLength++] = (byte) rel32;
                        instruction[instructionLength++] = (byte) (rel32 >> 8);
                        instruction[instructionLength++] = (byte) (rel32 >> 16);
                        instruction[instructionLength++] = (byte) (rel32 >> 24);
                        return new Jbe(pc, Arrays.copyOf(instruction, instructionLength), rel32);
                    }
                    case AMD64Opcode.JE32: {
                        int rel32 = code.read32();
                        instruction[instructionLength++] = (byte) rel32;
                        instruction[instructionLength++] = (byte) (rel32 >> 8);
                        instruction[instructionLength++] = (byte) (rel32 >> 16);
                        instruction[instructionLength++] = (byte) (rel32 >> 24);
                        return new Je(pc, Arrays.copyOf(instruction, instructionLength), rel32);
                    }
                    case AMD64Opcode.JG32: {
                        int rel32 = code.read32();
                        instruction[instructionLength++] = (byte) rel32;
                        instruction[instructionLength++] = (byte) (rel32 >> 8);
                        instruction[instructionLength++] = (byte) (rel32 >> 16);
                        instruction[instructionLength++] = (byte) (rel32 >> 24);
                        return new Jg(pc, Arrays.copyOf(instruction, instructionLength), rel32);
                    }
                    case AMD64Opcode.JGE32: {
                        int rel32 = code.read32();
                        instruction[instructionLength++] = (byte) rel32;
                        instruction[instructionLength++] = (byte) (rel32 >> 8);
                        instruction[instructionLength++] = (byte) (rel32 >> 16);
                        instruction[instructionLength++] = (byte) (rel32 >> 24);
                        return new Jge(pc, Arrays.copyOf(instruction, instructionLength), rel32);
                    }
                    case AMD64Opcode.JL32: {
                        int rel32 = code.read32();
                        instruction[instructionLength++] = (byte) rel32;
                        instruction[instructionLength++] = (byte) (rel32 >> 8);
                        instruction[instructionLength++] = (byte) (rel32 >> 16);
                        instruction[instructionLength++] = (byte) (rel32 >> 24);
                        return new Jl(pc, Arrays.copyOf(instruction, instructionLength), rel32);
                    }
                    case AMD64Opcode.JLE32: {
                        int rel32 = code.read32();
                        instruction[instructionLength++] = (byte) rel32;
                        instruction[instructionLength++] = (byte) (rel32 >> 8);
                        instruction[instructionLength++] = (byte) (rel32 >> 16);
                        instruction[instructionLength++] = (byte) (rel32 >> 24);
                        return new Jle(pc, Arrays.copyOf(instruction, instructionLength), rel32);
                    }
                    case AMD64Opcode.JNE32: {
                        int rel32 = code.read32();
                        instruction[instructionLength++] = (byte) rel32;
                        instruction[instructionLength++] = (byte) (rel32 >> 8);
                        instruction[instructionLength++] = (byte) (rel32 >> 16);
                        instruction[instructionLength++] = (byte) (rel32 >> 24);
                        return new Jne(pc, Arrays.copyOf(instruction, instructionLength), rel32);
                    }
                    case AMD64Opcode.JNO32: {
                        int rel32 = code.read32();
                        instruction[instructionLength++] = (byte) rel32;
                        instruction[instructionLength++] = (byte) (rel32 >> 8);
                        instruction[instructionLength++] = (byte) (rel32 >> 16);
                        instruction[instructionLength++] = (byte) (rel32 >> 24);
                        return new Jno(pc, Arrays.copyOf(instruction, instructionLength), rel32);
                    }
                    case AMD64Opcode.JNP32: {
                        int rel32 = code.read32();
                        instruction[instructionLength++] = (byte) rel32;
                        instruction[instructionLength++] = (byte) (rel32 >> 8);
                        instruction[instructionLength++] = (byte) (rel32 >> 16);
                        instruction[instructionLength++] = (byte) (rel32 >> 24);
                        return new Jnp(pc, Arrays.copyOf(instruction, instructionLength), rel32);
                    }
                    case AMD64Opcode.JNS32: {
                        int rel32 = code.read32();
                        instruction[instructionLength++] = (byte) rel32;
                        instruction[instructionLength++] = (byte) (rel32 >> 8);
                        instruction[instructionLength++] = (byte) (rel32 >> 16);
                        instruction[instructionLength++] = (byte) (rel32 >> 24);
                        return new Jns(pc, Arrays.copyOf(instruction, instructionLength), rel32);
                    }
                    case AMD64Opcode.JO32: {
                        int rel32 = code.read32();
                        instruction[instructionLength++] = (byte) rel32;
                        instruction[instructionLength++] = (byte) (rel32 >> 8);
                        instruction[instructionLength++] = (byte) (rel32 >> 16);
                        instruction[instructionLength++] = (byte) (rel32 >> 24);
                        return new Jo(pc, Arrays.copyOf(instruction, instructionLength), rel32);
                    }
                    case AMD64Opcode.JP32: {
                        int rel32 = code.read32();
                        instruction[instructionLength++] = (byte) rel32;
                        instruction[instructionLength++] = (byte) (rel32 >> 8);
                        instruction[instructionLength++] = (byte) (rel32 >> 16);
                        instruction[instructionLength++] = (byte) (rel32 >> 24);
                        return new Jp(pc, Arrays.copyOf(instruction, instructionLength), rel32);
                    }
                    case AMD64Opcode.JS32: {
                        int rel32 = code.read32();
                        instruction[instructionLength++] = (byte) rel32;
                        instruction[instructionLength++] = (byte) (rel32 >> 8);
                        instruction[instructionLength++] = (byte) (rel32 >> 16);
                        instruction[instructionLength++] = (byte) (rel32 >> 24);
                        return new Js(pc, Arrays.copyOf(instruction, instructionLength), rel32);
                    }
                    case AMD64Opcode.MOVD_X_RM: {
                        if (sizeOverride) {
                            Args args = new Args(code, rex, segment);
                            if (rex != null && rex.w) {
                                return new MovqToReg(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                            } else {
                                return new MovdToReg(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                            }
                        } else {
                            return new IllegalInstruction(pc, Arrays.copyOf(instruction, instructionLength));
                        }
                    }
                    case AMD64Opcode.MOVDQA_X_XM: {
                        Args args = new Args(code, rex, segment);
                        if (sizeOverride) {
                            return new MovdqaToReg(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else if (isREPZ) {
                            return new MovdquToReg(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new IllegalInstruction(pc, Arrays.copyOf(instruction, instructionLength));
                        }
                    }
                    case AMD64Opcode.MOVDQA_XM_X: {
                        Args args = new Args(code, rex, segment);
                        if (sizeOverride) {
                            return new MovdqaToReg(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), true);
                        } else if (isREPZ) {
                            return new MovdquToReg(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder(), true);
                        } else {
                            return new IllegalInstruction(pc, Arrays.copyOf(instruction, instructionLength));
                        }
                    }
                    case AMD64Opcode.MOVSX_R_RM8: {
                        Args args = new Args(code, rex, segment);
                        if (rex != null && rex.w) {
                            return new Movsbq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                        if (sizeOverride) {
                            return new Movsbw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new Movsbl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                    }
                    case AMD64Opcode.MOVSX_R_RM16: {
                        Args args = new Args(code, rex, segment);
                        if (rex != null && rex.w) {
                            return new Movswq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                        return new Movswl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                    }
                    case AMD64Opcode.MOVUPS_X_XM: {
                        Args args = new Args(code, rex, segment);
                        return new MovupsToReg(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                    }
                    case AMD64Opcode.MOVUPS_XM_X: {
                        Args args = new Args(code, rex, segment);
                        return new MovupsToRM(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                    }
                    case AMD64Opcode.MOVZX_R_RM8: {
                        Args args = new Args(code, rex, segment);
                        if (rex != null && rex.w) {
                            return new Movzbq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                        if (sizeOverride) {
                            return new Movzbw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new Movzbl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                    }
                    case AMD64Opcode.MOVZX_R_RM16: {
                        Args args = new Args(code, rex, segment);
                        if (rex != null && rex.w) {
                            return new Movzwq(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        }
                        return new Movzwl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                    }
                    case AMD64Opcode.NOP_RM: {
                        Args args = new Args(code, segment);
                        return new Nop(pc, args.getOp(instruction, instructionLength));
                    }
                    case AMD64Opcode.PCMPEQB_X_XM:
                        if (sizeOverride) {
                            Args args = new Args(code, rex, segment);
                            return new Pcmpeq128b(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new IllegalInstruction(pc, Arrays.copyOf(instruction, instructionLength));
                        }
                    case AMD64Opcode.PCMPEQW_X_XM:
                        if (sizeOverride) {
                            Args args = new Args(code, rex, segment);
                            return new Pcmpeq128w(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new IllegalInstruction(pc, Arrays.copyOf(instruction, instructionLength));
                        }
                    case AMD64Opcode.PCMPEQD_X_XM:
                        if (sizeOverride) {
                            Args args = new Args(code, rex, segment);
                            return new Pcmpeq128d(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new IllegalInstruction(pc, Arrays.copyOf(instruction, instructionLength));
                        }
                    case AMD64Opcode.PMOVMSKB_R_X:
                        if (sizeOverride) {
                            Args args = new Args(code, rex, segment);
                            return new Pmovmskb(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new IllegalInstruction(pc, Arrays.copyOf(instruction, instructionLength));
                        }
                    case AMD64Opcode.POR_X_XM:
                        if (sizeOverride) {
                            Args args = new Args(code, segment);
                            return new Por(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new IllegalInstruction(pc, Arrays.copyOf(instruction, instructionLength));
                        }
                    case AMD64Opcode.PSHUFD: {
                        if (sizeOverride) {
                            Args args = new Args(code, rex, segment);
                            byte imm = code.read8();
                            return new Pshufd(pc, args.getOp2(instruction, instructionLength, new byte[]{imm}, 1), args.getOperandDecoder(), imm);
                        } else {
                            return new IllegalInstruction(pc, Arrays.copyOf(instruction, instructionLength));
                        }
                    }
                    case AMD64Opcode.PUNPCKLBW: {
                        if (sizeOverride) {
                            Args args = new Args(code, rex, segment);
                            return new Punpcklbw(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new IllegalInstruction(pc, Arrays.copyOf(instruction, instructionLength));
                        }
                    }
                    case AMD64Opcode.PUNPCKLWD: {
                        if (sizeOverride) {
                            Args args = new Args(code, rex, segment);
                            return new Punpcklwd(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new IllegalInstruction(pc, Arrays.copyOf(instruction, instructionLength));
                        }
                    }
                    case AMD64Opcode.PUSH_FS:
                        return new Pushq(pc, Arrays.copyOf(instruction, instructionLength), new SegmentRegisterOperand(SegmentRegister.FS));
                    case AMD64Opcode.PUSH_GS:
                        return new Pushq(pc, Arrays.copyOf(instruction, instructionLength), new SegmentRegisterOperand(SegmentRegister.FS));
                    case AMD64Opcode.PXOR_X_XM:
                        if (sizeOverride) {
                            Args args = new Args(code, segment);
                            return new Pxor(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                        } else {
                            return new IllegalInstruction(pc, Arrays.copyOf(instruction, instructionLength));
                        }
                    case AMD64Opcode.RDTSC:
                        return new Rdtsc(pc, Arrays.copyOf(instruction, instructionLength));
                    case AMD64Opcode.SETA: {
                        Args args = new Args(code, rex, segment);
                        return new Seta(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                    }
                    case AMD64Opcode.SETAE: {
                        Args args = new Args(code, rex, segment);
                        return new Setae(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                    }
                    case AMD64Opcode.SETB: {
                        Args args = new Args(code, rex, segment);
                        return new Setb(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                    }
                    case AMD64Opcode.SETBE: {
                        Args args = new Args(code, rex, segment);
                        return new Setbe(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                    }
                    case AMD64Opcode.SETE: {
                        Args args = new Args(code, rex, segment);
                        return new Sete(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                    }
                    case AMD64Opcode.SETG: {
                        Args args = new Args(code, rex, segment);
                        return new Setg(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                    }
                    case AMD64Opcode.SETGE: {
                        Args args = new Args(code, rex, segment);
                        return new Setge(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                    }
                    case AMD64Opcode.SETL: {
                        Args args = new Args(code, rex, segment);
                        return new Setl(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                    }
                    case AMD64Opcode.SETLE: {
                        Args args = new Args(code, rex, segment);
                        return new Setle(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                    }
                    case AMD64Opcode.SETNE: {
                        Args args = new Args(code, rex, segment);
                        return new Setne(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                    }
                    case AMD64Opcode.SETNO: {
                        Args args = new Args(code, rex, segment);
                        return new Setno(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                    }
                    case AMD64Opcode.SETNP: {
                        Args args = new Args(code, rex, segment);
                        return new Setnp(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                    }
                    case AMD64Opcode.SETNS: {
                        Args args = new Args(code, rex, segment);
                        return new Setns(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                    }
                    case AMD64Opcode.SETO: {
                        Args args = new Args(code, rex, segment);
                        return new Seto(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                    }
                    case AMD64Opcode.SETP: {
                        Args args = new Args(code, rex, segment);
                        return new Setp(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                    }
                    case AMD64Opcode.SETS: {
                        Args args = new Args(code, rex, segment);
                        return new Sets(pc, args.getOp(instruction, instructionLength), args.getOperandDecoder());
                    }
                    case AMD64Opcode.SYSCALL:
                        return new Syscall(pc, Arrays.copyOf(instruction, instructionLength));
                    default:
                        return new IllegalInstruction(pc, Arrays.copyOf(instruction, instructionLength));
                }
            }
            default:
                return new IllegalInstruction(pc, Arrays.copyOf(instruction, instructionLength));
        }
    }

    private static Register getRegister16(byte op, boolean r) {
        int reg = (op & 0x7) + (r ? 8 : 0);
        return REG16[reg];
    }

    private static Register getRegister32(byte op, boolean r) {
        int reg = (op & 0x7) + (r ? 8 : 0);
        return REG32[reg];
    }

    private static Register getRegister64(byte op, boolean r) {
        int reg = (op & 0x7) + (r ? 8 : 0);
        return REG64[reg];
    }

    private static class Args {
        public final AMD64RexPrefix rex;
        public final ModRM modrm;
        public final SIB sib;
        public final long displacement;
        public final SegmentRegister segment;

        public final byte[] bytes;

        public Args(CodeReader code, SegmentRegister segment) {
            this(code, null, segment);
        }

        public Args(CodeReader code, AMD64RexPrefix rex, SegmentRegister segment) {
            this.rex = rex;
            this.segment = segment;
            modrm = new ModRM(code.read8());
            if (modrm.hasSIB()) {
                sib = new SIB(code.read8());
            } else {
                sib = null;
            }
            int size = modrm.getDisplacementSize();
            if (modrm.hasSIB() && sib.base == 0b101) {
                switch (modrm.getMod()) {
                    case 0b00:
                    case 0b10:
                        size = 4;
                        break;
                    case 0b01:
                        size = 1;
                        break;
                }
            }
            switch (size) {
                case 1:
                    displacement = code.read8();
                    if (sib == null) {
                        bytes = new byte[]{modrm.getModRM(), (byte) displacement};
                    } else {
                        bytes = new byte[]{modrm.getModRM(), sib.getSIB(), (byte) displacement};
                    }
                    break;
                case 2:
                    displacement = code.read16();
                    if (sib == null) {
                        bytes = new byte[]{modrm.getModRM(), (byte) displacement, (byte) (displacement >> 8)};
                    } else {
                        bytes = new byte[]{modrm.getModRM(), sib.getSIB(), (byte) displacement, (byte) (displacement >> 8)};
                    }
                    break;
                case 4:
                    displacement = code.read32();
                    if (sib == null) {
                        bytes = new byte[]{modrm.getModRM(), (byte) displacement, (byte) (displacement >> 8), (byte) (displacement >> 16), (byte) (displacement >> 24)};
                    } else {
                        bytes = new byte[]{modrm.getModRM(), sib.getSIB(), (byte) displacement, (byte) (displacement >> 8), (byte) (displacement >> 16), (byte) (displacement >> 24)};
                    }
                    break;
                default:
                    displacement = 0;
                    if (sib == null) {
                        bytes = new byte[]{modrm.getModRM()};
                    } else {
                        bytes = new byte[]{modrm.getModRM(), sib.getSIB()};
                    }
                    break;
            }

        }

        public OperandDecoder getOperandDecoder() {
            return new OperandDecoder(modrm, sib, displacement, rex, segment);
        }

        public byte[] getOp(byte[] prefix, int prefixLength) {
            byte[] result = new byte[prefixLength + bytes.length];
            System.arraycopy(prefix, 0, result, 0, prefixLength);
            System.arraycopy(bytes, 0, result, prefixLength, bytes.length);
            return result;
        }

        public byte[] getOp2(byte[] prefix, int prefixLength, byte[] suffix, int suffixLength) {
            byte[] result = new byte[prefixLength + bytes.length + suffixLength];
            System.arraycopy(prefix, 0, result, 0, prefixLength);
            System.arraycopy(bytes, 0, result, prefixLength, bytes.length);
            System.arraycopy(suffix, 0, result, prefixLength + bytes.length, suffixLength);
            return result;
        }

        @Override
        public String toString() {
            return "Args[rex=" + rex + ";modrm=" + modrm + ";sib=" + sib + ";displacement=" + displacement + "]";
        }
    }
}
