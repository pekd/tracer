package org.graalvm.vm.trcview.arch.none.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.arch.none.None;
import org.graalvm.vm.trcview.net.protocol.IO;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class GenericStepEvent extends StepEvent {
    private final GenericCpuState state;
    private final InstructionType type;
    private final String[] disassembly;
    private final byte[] machinecode;

    protected GenericStepEvent(WordInputStream in, int tid) throws IOException {
        super(None.ID, tid);
        state = new GenericCpuState(in, tid);
        machinecode = IO.readArray(in);
        switch (in.read8bit()) {
            default:
            case 0:
                type = InstructionType.OTHER;
                break;
            case 1:
                type = InstructionType.JCC;
                break;
            case 2:
                type = InstructionType.JMP;
                break;
            case 3:
                type = InstructionType.JMP_INDIRECT;
                break;
            case 4:
                type = InstructionType.CALL;
                break;
            case 5:
                type = InstructionType.RET;
                break;
            case 6:
                type = InstructionType.SYSCALL;
                break;
            case 7:
                type = InstructionType.RTI;
                break;
        }
        disassembly = IO.readStringArray(in);
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
        return disassembly[0];
    }

    @Override
    public long getPC() {
        return state.getPC();
    }

    @Override
    public InstructionType getType() {
        return type;
    }

    @Override
    public long getStep() {
        return state.getStep();
    }

    @Override
    public CpuState getState() {
        return state;
    }

    @Override
    public StepFormat getFormat() {
        return None.FORMAT;
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        // TODO Auto-generated method stub
    }
}
