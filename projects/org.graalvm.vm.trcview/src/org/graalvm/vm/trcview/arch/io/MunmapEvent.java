package org.graalvm.vm.trcview.arch.io;

import java.io.IOException;

import org.graalvm.vm.posix.elf.Elf;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class MunmapEvent extends Event {
    private final long addr;
    private final long len;
    private final int result;

    public MunmapEvent(int tid, long addr, long len, int result) {
        super(Elf.EM_NONE, MUNMAP, tid);
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
    protected void writeRecord(WordOutputStream out) throws IOException {
        out.write64bit(addr);
        out.write64bit(len);
        out.write32bit(result);
    }

    public static MunmapEvent readRecord(WordInputStream in, int tid) throws IOException {
        long addr = in.read64bit();
        long len = in.read64bit();
        int result = in.read32bit();
        return new MunmapEvent(tid, addr, len, result);
    }

    @Override
    public String toString() {
        return String.format("munmap(0x%016x, %d) = 0x%08x", addr, len, result);
    }
}
