package org.graalvm.vm.trcview.arch.io;

import org.graalvm.vm.memory.vector.Vector128;

public class MemoryEventI64 extends MemoryEvent {
    private final long value;

    public MemoryEventI64(boolean be, int tid, long address, boolean write) {
        super(be, tid, address, write, false);
        value = 0;
    }

    public MemoryEventI64(boolean be, int tid, long address, boolean write, long value) {
        super(be, tid, address, write, true);
        this.value = value;
    }

    @Override
    public int getSize() {
        return 8;
    }

    @Override
    public long getValue() {
        if (!hasData()) {
            throw new IllegalStateException("no data available");
        }
        return value;
    }

    @Override
    public Vector128 getVector() {
        throw new UnsupportedOperationException();
    }
}
