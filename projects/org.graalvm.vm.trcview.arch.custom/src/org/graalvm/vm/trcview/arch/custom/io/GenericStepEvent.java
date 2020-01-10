package org.graalvm.vm.trcview.arch.custom.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.custom.GenericArchitecture;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.util.io.WordOutputStream;

public class GenericStepEvent extends StepEvent {
    private final GenericCpuState state;
    private final byte[] machinecode;
    private final String[] asm;
    private final InstructionType type;

    protected GenericStepEvent(int tid, GenericCpuState state, byte[] code, String[] asm, InstructionType type) {
        super(GenericArchitecture.ID, tid);
        this.state = state;
        this.machinecode = code;
        this.asm = asm;
        this.type = type;
    }

    @Override
    public byte[] getMachinecode() {
        return machinecode;
    }

    @Override
    public String[] getDisassemblyComponents() {
        return asm;
    }

    @Override
    public String getMnemonic() {
        return getDisassemblyComponents()[0];
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
        return GenericArchitecture.FORMAT;
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        // nothing for now
    }
}
