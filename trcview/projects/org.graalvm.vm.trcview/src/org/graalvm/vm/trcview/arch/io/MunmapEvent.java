package org.graalvm.vm.trcview.arch.io;

public class MunmapEvent extends Event {
    private final long addr;
    private final long len;
    private final int result;

    public MunmapEvent(int tid, long addr, long len, int result) {
        super(tid);
        this.addr = addr;
        this.len = len;
        this.result = result;
    }

    public long getAddress() {
        return addr;
    }

    public long getLength() {
        return len;
    }

    public int getResult() {
        return result;
    }

    @Override
    public String toString() {
        return String.format("munmap(0x%016x, %d) = 0x%08x", addr, len, result);
    }
}
