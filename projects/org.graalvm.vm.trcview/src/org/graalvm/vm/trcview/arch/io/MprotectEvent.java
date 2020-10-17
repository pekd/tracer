package org.graalvm.vm.trcview.arch.io;

import org.graalvm.vm.posix.api.mem.Mman;

public class MprotectEvent extends Event {
    private final long addr;
    private final long len;
    private final int prot;
    private final int result;

    public MprotectEvent(int tid, long addr, long len, int prot, int result) {
        super(tid);
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
    public String toString() {
        return String.format("mprotect(0x%016x, %d, %s) = 0x%08x", addr, len, Mman.prot(prot), result);
    }
}
