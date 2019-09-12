package org.graalvm.vm.x86.trcview.analysis.memory;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;
import org.graalvm.vm.x86.trcview.io.Node;

public class MemoryTrace {
    private static final Logger log = Trace.create(MemoryTrace.class);

    private final Map<Long, Page> pages = new HashMap<>();

    private long brk = -1;

    private static long getPageAddress(long address) {
        return address & 0xFFFFFFFFFFFFF000L;
    }

    public void mmap(long address, long size, long pc, long instructionCount, Node node) {
        long addr = getPageAddress(address);
        long sz = size;
        sz += address - addr;
        while (sz > 0) {
            Page page = pages.get(addr);
            if (page == null) {
                pages.put(addr, new Page(addr, pc, instructionCount, node));
            } else {
                page.clear(pc, instructionCount, node);
            }
            sz -= 4096;
            addr += 4096;
        }
        if (brk == -1) {
            brk = addr;
        }
    }

    public void mmap(long address, long size, byte[] data, long pc, long instructionCount, Node node) {
        long addr = getPageAddress(address);
        long sz = size;
        sz += address - addr;
        int offset = (int) (addr - address);
        while (sz > 0) {
            byte[] pageData = new byte[4096];
            int length = data.length - offset;
            if (length > 4096) {
                length = 4096;
            }
            if (length > 0) {
                if (offset < 0) {
                    System.arraycopy(data, 0, pageData, 4096 + offset, -offset);
                } else {
                    System.arraycopy(data, offset, pageData, 0, length);
                }
            }
            Page page = pages.get(addr);
            if (page == null) {
                if (length > 0) {
                    pages.put(addr, new Page(addr, pageData, pc, instructionCount, node));
                } else {
                    pages.put(addr, new Page(addr, pc, instructionCount, node));
                }
            } else {
                if (length > 0) {
                    page.overwrite(pageData, pc, instructionCount, node);
                } else {
                    page.clear(pc, instructionCount, node);
                }
            }
            sz -= 4096;
            addr += 4096;
            offset += 4096;
        }
        if (brk == -1) {
            brk = addr;
        }
    }

    public void brk(long newbrk, long pc, long instructionCount, Node node) {
        while (this.brk < newbrk) {
            Page page = pages.get(this.brk);
            if (page == null) {
                pages.put(this.brk, new Page(this.brk, pc, instructionCount, node));
            } else {
                page.clear(pc, instructionCount, node);
            }
            this.brk += 4096;
        }
        this.brk = newbrk;
    }

    public void write(long addr, byte size, long value, long pc, long instructionCount, Node node) {
        Page page = pages.get(getPageAddress(addr));
        if (page == null) {
            throw new AssertionError(String.format("no memory mapped to 0x%x [0x%x]", addr, getPageAddress(addr)));
        }
        long offset = addr - page.getAddress();
        if (offset + size >= 4096) {
            // write across page boundary
            long val = value;
            for (int i = 0; i < size; i++) {
                long a = addr + i;
                if (a < page.getAddress() + 4096) {
                    page.addUpdate(a, (byte) 1, (byte) val, pc, instructionCount, node);
                } else {
                    page = pages.get(page.getAddress() + 4096);
                    page.addUpdate(a, (byte) 1, (byte) val, pc, instructionCount, node);
                }
                val >>= 8;
            }
        } else {
            page.addUpdate(addr, size, value, pc, instructionCount, node);
        }
    }

    public byte getByte(long addr, long instructionCount) throws MemoryNotMappedException {
        Page page = pages.get(getPageAddress(addr));
        if (page == null) {
            throw new MemoryNotMappedException(String.format("no memory mapped to 0x%x [0x%x]", addr, getPageAddress(addr)));
        }
        return page.getByte(addr, instructionCount);
    }

    public MemoryUpdate getLastWrite(long addr, long instructionCount) throws MemoryNotMappedException {
        Page page = pages.get(getPageAddress(addr));
        if (page == null) {
            throw new MemoryNotMappedException(String.format("no memory mapped to 0x%x [0x%x]", addr, getPageAddress(addr)));
        }
        return page.getLastUpdate(addr, instructionCount);
    }

    public void printStats() {
        log.log(Levels.INFO, "Memory consists of " + pages.size() + " pages (4k/page)");
    }
}
