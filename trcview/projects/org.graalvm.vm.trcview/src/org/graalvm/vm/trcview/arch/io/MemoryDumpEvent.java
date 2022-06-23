package org.graalvm.vm.trcview.arch.io;

public class MemoryDumpEvent extends Event {
    private final long address;
    private final byte[] data;

    public MemoryDumpEvent(int tid, long address, byte[] data) {
        super(tid);
        this.address = address;
        this.data = data;
    }

    public long getAddress() {
        return address;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return String.format("Memory Snapshot [0x%016x-0x%016x, %s bytes]", address, address + data.length - 1, data.length);
    }
}
