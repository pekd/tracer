package org.graalvm.vm.trcview.storage;

public class MemoryAccess {
    public final int tid;
    public final long step;
    public final long address;
    public final byte value;
    public final long base;
    public final int size;

    public MemoryAccess(int tid, long step, long address, byte value, long base, int size) {
        this.tid = tid;
        this.step = step;
        this.address = address;
        this.value = value;
        this.base = base;
        this.size = size;
    }
}
