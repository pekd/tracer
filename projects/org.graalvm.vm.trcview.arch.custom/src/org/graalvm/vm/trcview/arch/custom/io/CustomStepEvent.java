package org.graalvm.vm.trcview.arch.custom.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.custom.GenericArchitecture;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.util.io.WordOutputStream;

public class CustomStepEvent extends StepEvent {
    private final CustomCpuState state;

    public CustomStepEvent(short arch, int tid, CustomCpuState state) {
        super(arch, tid);
        this.state = state;
    }

    @Override
    public byte[] getMachinecode() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDisassembly() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getDisassemblyComponents() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getMnemonic() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getPC() {
        return state.getPC();
    }

    @Override
    public boolean isCall() {
        return getType() == InstructionType.CALL;
    }

    @Override
    public boolean isReturn() {
        return getType() == InstructionType.RET;
    }

    @Override
    public boolean isSyscall() {
        return getType() == InstructionType.SYSCALL;
    }

    @Override
    public boolean isReturnFromSyscall() {
        return getType() == InstructionType.RTI;
    }

    @Override
    public InstructionType getType() {
        return InstructionType.OTHER;
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
        // TODO Auto-generated method stub
    }
}
