package org.graalvm.vm.trcview.arch.x86.io;

import org.graalvm.vm.trcview.arch.ByteCodeReader;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.arch.x86.AMD64;
import org.graalvm.vm.trcview.arch.x86.decode.isa.AMD64Instruction;
import org.graalvm.vm.trcview.arch.x86.decode.isa.AMD64InstructionDecoder;
import org.graalvm.vm.trcview.arch.x86.decode.isa.AMD64InstructionQuickInfo;
import org.graalvm.vm.util.HexFormatter;

public abstract class AMD64StepEvent extends StepEvent {
    private final byte[] machinecode;

    protected AMD64StepEvent(int tid, byte[] machinecode) {
        super(tid);
        this.machinecode = machinecode;
    }

    @Override
    public byte[] getMachinecode() {
        return machinecode;
    }

    @Override
    public boolean isCall() {
        return AMD64InstructionQuickInfo.isCall(getMachinecode());
    }

    @Override
    public boolean isReturn() {
        return AMD64InstructionQuickInfo.isRet(getMachinecode());
    }

    @Override
    public boolean isSyscall() {
        return AMD64InstructionQuickInfo.isSyscall(getMachinecode());
    }

    @Override
    public boolean isReturnFromSyscall() {
        return false;
    }

    @Override
    public InstructionType getType() {
        if (getMachinecode() == null) {
            return InstructionType.OTHER;
        }
        switch (AMD64InstructionQuickInfo.getType(getMachinecode())) {
            case CALL:
                return InstructionType.CALL;
            case RET:
                return InstructionType.RET;
            case JMP:
                return InstructionType.JMP;
            case JMP_INDIRECT:
                return InstructionType.JMP_INDIRECT;
            case JCC:
                return InstructionType.JCC;
            case SYSCALL:
                return InstructionType.SYSCALL;
            default:
                return InstructionType.OTHER;
        }
    }

    public AMD64Instruction getInstruction() {
        try {
            return AMD64InstructionDecoder.decode(getPC(), new ByteCodeReader(machinecode, 0, false));
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    private String code() {
        StringBuilder buf = new StringBuilder(machinecode.length * 4);
        for (byte b : machinecode) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append("0x" + HexFormatter.tohex(b & 0xFF, 2));
        }
        return buf.toString();
    }

    @Override
    public String getDisassembly() {
        try {
            AMD64Instruction insn = getInstruction();
            if (insn == null) {
                return "db\t" + code();
            } else {
                return insn.getDisassembly();
            }
        } catch (Throwable t) {
            return "db\t" + code();
        }
    }

    @Override
    public String[] getDisassemblyComponents() {
        if (machinecode != null) {
            try {
                AMD64Instruction insn = getInstruction();
                if (insn == null) {
                    return new String[]{"db", code()};
                } else {
                    return insn.getDisassemblyComponents();
                }
            } catch (Throwable t) {
                return new String[]{"db", code()};
            }
        } else {
            return null;
        }
    }

    @Override
    public String getMnemonic() {
        if (machinecode == null) {
            return null;
        } else {
            AMD64Instruction insn = getInstruction();
            if (insn == null) {
                return "db";
            } else {
                String[] parts = insn.getDisassemblyComponents();
                return parts[0];
            }
        }
    }

    @Override
    public abstract AMD64CpuState getState();

    @Override
    public StepFormat getFormat() {
        return AMD64.FORMAT;
    }
}
