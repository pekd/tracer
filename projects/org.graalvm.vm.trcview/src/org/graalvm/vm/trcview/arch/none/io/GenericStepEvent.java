package org.graalvm.vm.trcview.arch.none.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.util.io.WordInputStream;

public abstract class GenericStepEvent extends StepEvent implements CpuState {
    public static final byte TYPE_OTHER = 0;
    public static final byte TYPE_JCC = 1;
    public static final byte TYPE_JMP = 2;
    public static final byte TYPE_JMP_INDIRECT = 3;
    public static final byte TYPE_CALL = 4;
    public static final byte TYPE_RET = 5;
    public static final byte TYPE_SYSCALL = 6;
    public static final byte TYPE_RTI = 7;

    private final byte[] machinecode;
    private final String[] disassembly;
    private final byte type;

    private final long pc;
    private final long step;

    private final GenericStateDescription description;

    protected GenericStepEvent(GenericStateDescription description, int tid, long step, long pc, byte type, byte[] machinecode, String[] disassembly) {
        super(tid);
        this.description = description;
        this.step = step;
        this.pc = pc;
        this.type = type;
        this.machinecode = machinecode;
        this.disassembly = disassembly;
    }

    public GenericStateDescription getDescription() {
        return description;
    }

    public byte getRawType() {
        return type;
    }

    @Override
    public byte[] getMachinecode() {
        return machinecode;
    }

    @Override
    public String[] getDisassemblyComponents() {
        return disassembly;
    }

    @Override
    public String getMnemonic() {
        if (disassembly == null) {
            return null;
        } else {
            return disassembly[0];
        }
    }

    @Override
    public long getPC() {
        return pc;
    }

    @Override
    public InstructionType getType() {
        switch (type) {
            default:
            case TYPE_OTHER:
                return InstructionType.OTHER;
            case TYPE_JCC:
                return InstructionType.JCC;
            case TYPE_JMP:
                return InstructionType.JMP;
            case TYPE_JMP_INDIRECT:
                return InstructionType.JMP_INDIRECT;
            case TYPE_CALL:
                return InstructionType.CALL;
            case TYPE_RET:
                return InstructionType.RET;
            case TYPE_SYSCALL:
                return InstructionType.SYSCALL;
            case TYPE_RTI:
                return InstructionType.RTI;
        }
    }

    @Override
    public long getStep() {
        return step;
    }

    @Override
    public long get(String name) {
        switch (name) {
            case "pc":
                return getPC();
            case "tid":
                return getTid();
            case "step":
                return getStep();
            default:
                Field field = description.getField(name);
                if (field == null) {
                    throw new IllegalArgumentException("unknown variable " + name);
                } else {
                    return field.getValue(getData());
                }
        }
    }

    public abstract byte[] getData();

    @Override
    public CpuState getState() {
        return this;
    }

    @Override
    public StepFormat getFormat() {
        return description.getFormat();
    }

    @Override
    public String toString() {
        return description.format(getData());
    }

    protected static byte[] read8(WordInputStream in) throws IOException {
        int size = in.read8bit();
        byte[] data = new byte[size];
        in.read(data);
        return data;
    }

}
