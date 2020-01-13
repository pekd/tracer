package org.graalvm.vm.x86.trcview.arch.custom.test.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.util.io.WordOutputStream;

public class MockCpuState extends CpuState {
    public long step;
    public long pc;
    public Map<String, Long> values = new HashMap<>();

    public MockCpuState(short arch, int tid) {
        super(arch, tid);
    }

    public MockCpuState(short arch, int tid, long step, long pc) {
        super(arch, tid);
        this.step = step;
        this.pc = pc;
    }

    public MockCpuState(short arch, int tid, long step, long pc, Object... vals) {
        super(arch, tid);
        this.step = step;
        this.pc = pc;
        if (vals.length % 2 != 0) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < vals.length; i += 2) {
            String name = (String) vals[i];
            Long value = (Long) vals[i + 1];
            values.put(name, value);
        }
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
        return values.get(name);
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        // nothing
    }
}
