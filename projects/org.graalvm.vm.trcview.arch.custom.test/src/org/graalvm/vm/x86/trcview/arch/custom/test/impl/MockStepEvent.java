package org.graalvm.vm.x86.trcview.arch.custom.test.impl;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.util.io.WordOutputStream;

public class MockStepEvent extends StepEvent {
    public byte[] machinecode = new byte[0];
    public String[] disassembly = new String[0];
    public long pc;
    public InstructionType type = InstructionType.OTHER;
    public long step;
    public StepFormat format = new StepFormat(StepFormat.NUMBERFMT_HEX, 16, 16, 8, false);
    public CpuState state;

    public MockStepEvent(short arch, int tid) {
        super(arch, tid);
    }

    public MockStepEvent(short arch, int tid, long step, long pc, String[] disasm, byte[] code) {
        super(arch, tid);
        this.step = step;
        this.pc = pc;
        this.disassembly = disasm;
        this.machinecode = code;
        this.state = new MockCpuState(arch, tid, step, pc);
    }

    public MockStepEvent(short arch, int tid, long step, long pc, String[] disasm, byte[] code, Object... values) {
        super(arch, tid);
        this.step = step;
        this.pc = pc;
        this.disassembly = disasm;
        this.machinecode = code;
        this.state = new MockCpuState(arch, tid, step, pc, values);
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
    public CpuState getState() {
        return state;
    }

    @Override
    public StepFormat getFormat() {
        return format;
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        // nothing
    }
}
