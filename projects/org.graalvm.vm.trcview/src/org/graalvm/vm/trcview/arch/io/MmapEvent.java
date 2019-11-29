package org.graalvm.vm.trcview.arch.io;

import java.io.IOException;

import org.graalvm.vm.posix.api.mem.Mman;
import org.graalvm.vm.posix.elf.Elf;
import org.graalvm.vm.trcview.net.protocol.IO;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

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
        super(Elf.EM_NONE, MMAP, tid);
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
    protected void writeRecord(WordOutputStream out) throws IOException {
        out.write64bit(addr);
        out.write64bit(len);
        out.write64bit(off);
        out.write64bit(result);
        out.write32bit(prot);
        out.write32bit(flags);
        out.write32bit(fildes);
        IO.writeString(out, filename);
        IO.writeArray(out, data);
    }

    public static MmapEvent readRecord(WordInputStream in, int tid) throws IOException {
        long addr = in.read64bit();
        long len = in.read64bit();
        long off = in.read64bit();
        long result = in.read64bit();
        int prot = in.read32bit();
        int flags = in.read32bit();
        int fildes = in.read32bit();
        String filename = IO.readString(in);
        byte[] data = IO.readArray(in);
        return new MmapEvent(tid, addr, len, prot, flags, fildes, off, filename, result, data);
    }

    @Override
    public String toString() {
        return String.format("mmap(0x%016x, %d, %s, %s, %d, %d) = 0x%016x", addr, len, Mman.prot(prot), Mman.flags(flags), fildes, off, result);
    }
}
