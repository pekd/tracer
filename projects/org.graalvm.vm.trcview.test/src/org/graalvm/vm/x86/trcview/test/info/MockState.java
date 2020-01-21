package org.graalvm.vm.x86.trcview.test.info;

import java.io.IOException;

import org.graalvm.vm.posix.elf.Elf;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.util.io.WordOutputStream;

public class MockState extends CpuState {

    protected MockState() {
        super(Elf.EM_NONE, 0);
    }

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

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        // TODO Auto-generated method stub
    }
}
