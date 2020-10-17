package org.graalvm.vm.trcview.arch.io;

import org.graalvm.vm.posix.api.mem.Mman;

public class MmapEvent extends Event {
    private final long addr;
    private final long len;
    private final int prot;
    private final int flags;
    private final int fildes;
    private final long off;
    private final String filename;
    private final long result;
    private final byte[] data;

    public MmapEvent(int tid, long address, long length, int protection, int flags, int fd, long offset, String filename, long result, byte[] data) {
        super(tid);
        this.addr = address;
        this.len = length;
        this.prot = protection;
        this.flags = flags;
        this.fildes = fd;
        this.off = offset;
        this.filename = filename;
        this.result = result;
        this.data = data;
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

    public int getFlags() {
        return flags;
    }

    public int getFileDescriptor() {
        return fildes;
    }

    public long getOffset() {
        return off;
    }

    public String getFilename() {
        return filename;
    }

    public long getResult() {
        return result;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return String.format("mmap(0x%016x, %d, %s, %s, %d, %d) = 0x%016x", addr, len, Mman.prot(prot), Mman.flags(flags), fildes, off, result);
    }
}
