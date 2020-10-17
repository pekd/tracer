package org.graalvm.vm.trcview.arch.io;

import org.graalvm.vm.trcview.arch.none.None;

public class IncompleteTraceStep extends StepEvent implements CpuState {
    public IncompleteTraceStep(int tid) {
        super(tid);
    }

    @Override
    public byte[] getMachinecode() {
        return new byte[0];
    }

    @Override
    public String getDisassembly() {
        return null;
    }

    @Override
    public String[] getDisassemblyComponents() {
        return null;
    }

    @Override
    public String getMnemonic() {
        return null;
    }

    @Override
    public long getPC() {
        return 0;
    }

    @Override
    public boolean isCall() {
        return false;
    }

    @Override
    public boolean isReturn() {
        return false;
    }

    @Override
    public boolean isSyscall() {
        return false;
    }

    @Override
    public boolean isReturnFromSyscall() {
        return false;
    }

    @Override
    public InstructionType getType() {
        return InstructionType.OTHER;
    }

    @Override
    public long getStep() {
        return 0;
    }

    @Override
    public long get(String name) {
        return 0;
    }

    @Override
    public String toString() {
        return "<unavailable>\n";
    }

    @Override
    public CpuState getState() {
        return this;
    }

    @Override
    public StepFormat getFormat() {
        return None.FORMAT;
    }
}
