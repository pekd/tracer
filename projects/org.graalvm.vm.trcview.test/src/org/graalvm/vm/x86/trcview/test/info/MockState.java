package org.graalvm.vm.x86.trcview.test.info;

import org.graalvm.vm.trcview.arch.io.CpuState;

public class MockState implements CpuState {
    @Override
    public long getStep() {
        return 0;
    }

    @Override
    public long getPC() {
        return 0;
    }

    @Override
    public long get(String name) {
        return 0;
    }

    public int getTid() {
        return 0;
    }
}
