package org.graalvm.vm.trcview.arch.io;

import org.graalvm.vm.util.Vector128;

public class MemoryEventI32 extends MemoryEvent {
    private final int value;

    public MemoryEventI32(boolean be, int tid, long address, boolean write) {
        super(be, tid, address, write, false);
        value = 0;
    }

    public MemoryEventI32(boolean be, int tid, long address, boolean write, int value) {
        super(be, tid, address, write, true);
        this.value = value;
    }

    @Override
    public int getSize() {
        return 4;
    }

    @Override
    public long getValue() {
        if (!hasData()) {
            throw new IllegalStateException("no data available");
        }
        return Integer.toUnsignedLong(value);
    }

    @Override
    public Vector128 getVector() {
        throw new UnsupportedOperationException();
    }
}
