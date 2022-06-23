package org.graalvm.vm.trcview.arch.io;

import org.graalvm.vm.util.Vector128;

public class GenericMemoryEvent extends MemoryEvent {
    private final byte size;
    private final long value64;
    private final Vector128 value128;

    public GenericMemoryEvent(boolean be, int tid, long address, byte size, boolean write) {
        super(be, tid, address, write, false);
        this.size = size;
        this.value64 = 0;
        this.value128 = null;
    }

    public GenericMemoryEvent(boolean be, int tid, long address, byte size, boolean write, long value) {
        super(be, tid, address, write, true);
        this.size = size;
        this.value64 = value;
        this.value128 = null;
    }

    public GenericMemoryEvent(boolean be, int tid, long address, byte size, boolean write, Vector128 value) {
        super(be, tid, address, write, true);
        this.size = size;
        this.value64 = 0;
        this.value128 = value;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public long getValue() {
        return value64;
    }

    @Override
    public Vector128 getVector() {
        return value128;
    }
}
