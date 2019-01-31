package org.graalvm.vm.x86.node.debug.trace;

import java.io.IOException;

import org.graalvm.vm.posix.api.mem.Mman;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class MprotectRecord extends Record {
    public static final int MAGIC = 0x50524f54;

    private long addr;
    private long len;
    private int prot;
    private int result;

    MprotectRecord() {
        super(MAGIC);
    }

    public MprotectRecord(long addr, long len, int prot, int result) {
        this();
        this.addr = addr;
        this.len = len;
        this.prot = prot;
        this.result = result;
    }

    public long getAddress() {
        return addr;
    }

    public long getLength() {
        return len;
    }

    public int getProtection() {
        return prot;
    }

    public long getResult() {
        return result;
    }

    @Override
    protected int size() {
        return 2 * 8 + 2 * 4;
    }

    @Override
    protected void readRecord(WordInputStream in) throws IOException {
        addr = in.read64bit();
        len = in.read64bit();
        prot = in.read32bit();
        result = in.read32bit();
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        out.write64bit(addr);
        out.write64bit(len);
        out.write32bit(prot);
        out.write32bit(result);
    }

    @Override
    public String toString() {
        return String.format("mprotect(0x%016x, %d, %s) = 0x%08x", addr, len, Mman.prot(prot), result);
    }
}
