package org.graalvm.vm.trcview.arch.io;

import org.graalvm.vm.util.Vector128;

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

    @Override
    public MemoryEventI128 add(long offset) {
        boolean be = isBigEndian();
        int tid = getTid();
        long address = getAddress();
        boolean write = isWrite();
        if (hasData()) {
            return new MemoryEventI128(be, tid, address + offset, write, value);
        } else {
            return new MemoryEventI128(be, tid, address + offset, write);
        }
    }
}
