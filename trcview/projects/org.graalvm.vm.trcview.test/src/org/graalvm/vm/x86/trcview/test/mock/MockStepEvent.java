package org.graalvm.vm.x86.trcview.test.mock;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.net.protocol.IO;
import org.graalvm.vm.util.io.WordInputStream;

public class MockStepEvent extends StepEvent implements CpuState {
    private byte[] machinecode;
    private InstructionType type;

    public long step;
    public long pc;
    public byte[] data;

    public MockStepEvent(int tid, byte[] machinecode, InstructionType type) {
        super(tid);
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
        return pc;
    }

    @Override
    public InstructionType getType() {
        return type;
    }

    @Override
    public long getStep() {
        return step;
    }

    @Override
    public long get(String name) {
        return 0;
    }

    @Override
    public MockStepEvent getState() {
        return this;
    }

    @Override
    public StepFormat getFormat() {
        return null;
    }

    public static MockStepEvent create(int tid, long step, long pc, InstructionType type, byte[] machinecode, WordInputStream in) throws IOException {
        MockStepEvent evt = new MockStepEvent(tid, machinecode, type);
        evt.step = step;
        evt.pc = pc;
        evt.data = IO.readArray(in);
        return evt;
    }
}
