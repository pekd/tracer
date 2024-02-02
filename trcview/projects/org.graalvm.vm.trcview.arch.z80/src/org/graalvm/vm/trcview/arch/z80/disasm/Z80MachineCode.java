package org.graalvm.vm.trcview.arch.z80.disasm;

import org.graalvm.vm.trcview.arch.CodeReader;
import org.graalvm.vm.trcview.arch.Disassembler;

public class Z80MachineCode {
    public static final boolean IX = false;
    public static final boolean IY = true;

    private final Disassembler disasm;
    private final CodeReader code;
    private boolean parsed = false;

    private boolean index;
    private byte buffer;
    private byte offset;

    private Z80Instruction insn;

    public Z80MachineCode(CodeReader code) {
        this(code, null);
    }

    public Z80MachineCode(CodeReader code, Disassembler disasm) {
        this.code = code;
        this.disasm = disasm;
        insn = parseInstruction();
    }

    private Z80Instruction parseInstruction() {
        if (parsed) {
            throw new IllegalStateException("already parsed");
        } else {
            parsed = true;
        }

        // parse machine code
        buffer = code.nextI8();

        switch (buffer) {
            case (byte) 0xCB:
                return Z80Instruction.INSTRUCTION_CB[Byte.toUnsignedInt(code.nextI8())];
            case (byte) 0xED:
                buffer = code.nextI8();
                return Z80Instruction.INSTRUCTION_ED[Byte.toUnsignedInt(buffer)];
            case (byte) 0xDD:
            case (byte) 0xFD:
                index = buffer == (byte) 0xDD ? IX : IY;
                buffer = code.nextI8();
                switch (buffer) {
                    case (byte) 0xCB:
                        offset = code.nextI8();
                        buffer = code.nextI8();
                        return Z80Instruction.INSTRUCTION_XX_CB[Byte.toUnsignedInt(buffer)];
                    default:
                        return Z80Instruction.INSTRUCTION_XX[Byte.toUnsignedInt(buffer)];
                }
            default:
                return Z80Instruction.INSTRUCTION_MAIN[Byte.toUnsignedInt(buffer)];
        }
    }

    public Z80Instruction getInstruction() {
        return insn;
    }

    public int getPC() {
        return (int) code.getPC();
    }

    public boolean isIX() {
        return index == IX;
    }

    public boolean isIY() {
        return index == IY;
    }

    public byte getOffset() {
        return offset;
    }

    public byte getBuffer() {
        return buffer;
    }

    public byte nextI8() {
        return code.nextI8();
    }

    public short nextI16() {
        return code.nextI16();
    }

    public Disassembler getDisassembler() {
        return disasm;
    }

    public boolean isBranch() {
        switch (insn.getType()) {
            case JCC:
            case JMP:
            case JMP_INDIRECT:
            case CALL:
                return true;
            default:
                return false;
        }
    }
}
