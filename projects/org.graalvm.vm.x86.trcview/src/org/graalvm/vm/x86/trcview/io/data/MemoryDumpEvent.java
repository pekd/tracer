package org.graalvm.vm.x86.trcview.io.data;

import java.io.IOException;

import org.graalvm.vm.posix.elf.Elf;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;
import org.graalvm.vm.x86.trcview.net.protocol.IO;

public class MemoryDumpEvent extends Event {
    private final long address;
    private final byte[] data;

    public MemoryDumpEvent(int tid, long address, byte[] data) {
        super(Elf.EM_NONE, MEMORY_DUMP, tid);
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
    protected void writeRecord(WordOutputStream out) throws IOException {
        out.write64bit(address);
        IO.writeArray(out, data);
    }

    public static MemoryDumpEvent readRecord(WordInputStream in, int tid) throws IOException {
        long address = in.read64bit();
        byte[] data = IO.readArray(in);
        return new MemoryDumpEvent(tid, address, data);
    }

    @Override
    public String toString() {
        return String.format("Memory Snapshot [0x%016x-0x%016x, %s bytes]", address, address + data.length - 1, data.length);
    }
}
