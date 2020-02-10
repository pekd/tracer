package org.graalvm.vm.x86.trcview.test.mock;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.net.protocol.IO;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class MockStepEvent extends StepEvent {
    private MockCpuState state;
    private byte[] machinecode;
    private InstructionType type;

    public MockStepEvent(MockCpuState state, byte[] machinecode, InstructionType type) {
        super(state.getArchitectureId(), state.getTid());
        this.state = state;
        this.machinecode = machinecode;
        this.type = type;
    }

    @Override
    public byte[] getMachinecode() {
        return machinecode;
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
    public MockCpuState getState() {
        return state;
    }

    @Override
    public StepFormat getFormat() {
        return null;
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        // TODO Auto-generated method stub
    }

    public static MockStepEvent create(int tid, long step, long pc, InstructionType type, byte[] machinecode, WordInputStream in) throws IOException {
        MockCpuState state = new MockCpuState(MockArchitecture.ID, tid);
        state.step = step;
        state.pc = pc;
        state.data = IO.readArray(in);
        return new MockStepEvent(state, machinecode, type);
    }
}
