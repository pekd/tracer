package org.graalvm.vm.trcview.arch.io;

import org.graalvm.vm.memory.vector.Vector128;

public class MemoryEventI128 extends MemoryEvent {
    private final Vector128 value;

    public MemoryEventI128(boolean be, int tid, long address, boolean write) {
        super(be, tid, address, write, false);
        value = null;
    }

    public MemoryEventI128(boolean be, int tid, long address, boolean write, Vector128 value) {
        super(be, tid, address, write, true);
        this.value = value;
    }

    @Override
    public int getSize() {
        return 16;
    }

    @Override
    public long getValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Vector128 getVector() {
        if (!hasData()) {
            throw new IllegalStateException("no data available");
        }
        return value;
    }
}
