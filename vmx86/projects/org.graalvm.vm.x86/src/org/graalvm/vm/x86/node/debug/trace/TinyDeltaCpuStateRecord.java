package org.graalvm.vm.x86.node.debug.trace;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;
import org.graalvm.vm.x86.isa.CpuState;

public class TinyDeltaCpuStateRecord extends CpuStateRecord {
    public static final byte ID = 0x04;

    private long rip;
    private long step;

    private CpuState state;

    public TinyDeltaCpuStateRecord() {
        super(ID);
    }

    public TinyDeltaCpuStateRecord(byte[] machinecode, CpuState state) {
        super(ID, machinecode);
        this.state = state;
        rip = state.rip;
        step = state.instructionCount;
    }

    @Override
    public CpuState getState() {
        return state;
    }

    @Override
    public long getPC() {
        return rip;
    }

    @Override
    public long getInstructionCount() {
        return step;
    }

    @Override
    protected int getDataSize() {
        return super.getDataSize() + 2 * 8;
    }

    @Override
    protected void readRecord(WordInputStream in) throws IOException {
        super.readRecord(in);
        rip = in.read64bit();
        step = in.read64bit();
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        super.writeRecord(out);
        out.write64bit(rip);
        out.write64bit(step);
    }
}
