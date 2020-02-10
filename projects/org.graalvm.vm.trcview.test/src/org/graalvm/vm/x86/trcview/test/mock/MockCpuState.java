package org.graalvm.vm.x86.trcview.test.mock;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.net.protocol.IO;
import org.graalvm.vm.util.io.WordOutputStream;

public class MockCpuState extends CpuState {
    public long step;
    public long pc;
    public byte[] data;

    public MockCpuState(short arch, int tid) {
        super(arch, tid);
    }

    @Override
    public long getStep() {
        return step;
    }

    @Override
    public long getPC() {
        return pc;
    }

    @Override
    public long get(String name) {
        return 0;
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        IO.writeArray(out, data);
    }
}
