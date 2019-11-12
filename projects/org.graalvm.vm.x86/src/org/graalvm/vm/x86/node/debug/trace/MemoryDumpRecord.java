package org.graalvm.vm.x86.node.debug.trace;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class MemoryDumpRecord extends Record {
    public static final int MAGIC = 0x4d454d31; // MEM1

    private long address;
    private byte[] data;

    MemoryDumpRecord() {
        super(MAGIC);
    }

    public MemoryDumpRecord(long address, byte[] data) {
        super(MAGIC);
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
    protected int getDataSize() {
        return 8 + sizeArray(data);
    }

    @Override
    protected void readRecord(WordInputStream in) throws IOException {
        address = in.read64bit();
        data = readArray(in);
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        out.write64bit(address);
        writeArray(out, data);
    }

    @Override
    public String toString() {
        return String.format("Memory Snapshot [0x%016x-0x%016x, %s bytes]", address, address + data.length - 1, data.length);
    }
}
