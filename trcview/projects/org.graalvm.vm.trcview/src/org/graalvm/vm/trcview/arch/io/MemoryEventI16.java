package org.graalvm.vm.trcview.arch.io;

import org.graalvm.vm.util.Vector128;

public class MemoryEventI16 extends MemoryEvent {
    private final short value;

    public MemoryEventI16(boolean be, int tid, long address, boolean write) {
        super(be, tid, address, write, false);
        value = 0;
    }

    public MemoryEventI16(boolean be, int tid, long address, boolean write, short value) {
        super(be, tid, address, write, true);
        this.value = value;
    }

    @Override
    public int getSize() {
        return 2;
    }

    @Override
    public long getValue() {
        if (!hasData()) {
            throw new IllegalStateException("no data available");
        }
        return Short.toUnsignedLong(value);
    }

    @Override
    public Vector128 getVector() {
        throw new UnsupportedOperationException();
    }
}
