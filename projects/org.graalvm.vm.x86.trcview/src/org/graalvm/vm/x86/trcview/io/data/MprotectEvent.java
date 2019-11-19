package org.graalvm.vm.x86.trcview.io.data;

import java.io.IOException;

import org.graalvm.vm.posix.api.mem.Mman;
import org.graalvm.vm.posix.elf.Elf;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class MprotectEvent extends Event {
    private final long addr;
    private final long len;
    private final int prot;
    private final int result;

    public MprotectEvent(int tid, long addr, long len, int prot, int result) {
        super(Elf.EM_NONE, MPROTECT, tid);
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

    public int getResult() {
        return result;
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        out.write64bit(addr);
        out.write64bit(len);
        out.write32bit(prot);
        out.write32bit(result);
    }

    public static MprotectEvent readRecord(WordInputStream in, int tid) throws IOException {
        long addr = in.read64bit();
        long len = in.read64bit();
        int prot = in.read32bit();
        int result = in.read32bit();
        return new MprotectEvent(tid, addr, len, prot, result);
    }

    @Override
    public String toString() {
        return String.format("mprotect(0x%016x, %d, %s) = 0x%08x", addr, len, Mman.prot(prot), result);
    }
}
