package org.graalvm.vm.x86.trcview.analysis.memory;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;
import org.graalvm.vm.x86.trcview.io.Node;

public class MemoryTrace {
    private static final Logger log = Trace.create(MemoryTrace.class);

    private static final int SIZE_THRESHOLD = 4096 * 10;

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
                pages.put(addr, new CoarsePage(addr, pc, instructionCount, node));
            } else {
                if (page instanceof CoarsePage && ((CoarsePage) page).getSize() > SIZE_THRESHOLD) {
                    page = ((CoarsePage) page).transformToFine();
                    pages.put(page.getAddress(), page);
                }
                page.clear(pc, instructionCount, node);
            }
            sz -= 4096;
            addr += 4096;
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
                    pages.put(addr, new CoarsePage(addr, pageData, pc, instructionCount, node));
                } else {
                    pages.put(addr, new CoarsePage(addr, pc, instructionCount, node));
                }
            } else {
                if (page instanceof CoarsePage && ((CoarsePage) page).getSize() > SIZE_THRESHOLD) {
                    page = ((CoarsePage) page).transformToFine();
                    pages.put(page.getAddress(), page);
                }
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
    }

    public void brk(long newbrk, long pc, long instructionCount, Node node) {
        if (brk == -1) {
            brk = getPageAddress(newbrk);
            if (brk != newbrk) {
                brk += 4096;
            }
            long p = newbrk;
            while (p > 0) {
                Page page = pages.get(p);
                if (page == null) {
                    pages.put(this.brk, new CoarsePage(p, pc, instructionCount, node));
                } else {
                    break;
                }
                p -= 4096;
            }
            return;
        }

        long end = getPageAddress(newbrk);
        if (end != newbrk) {
            end += 4096;
        }
        while (this.brk < end) {
            Page page = pages.get(this.brk);
            if (page == null) {
                pages.put(this.brk, new CoarsePage(this.brk, pc, instructionCount, node));
            } else {
                if (page instanceof CoarsePage && ((CoarsePage) page).getSize() > SIZE_THRESHOLD) {
                    page = ((CoarsePage) page).transformToFine();
                    pages.put(page.getAddress(), page);
                }
                page.clear(pc, instructionCount, node);
            }
            this.brk += 4096;
        }
    }

    public void write(long addr, byte size, long value, long pc, long instructionCount, Node node) {
        Page page = pages.get(getPageAddress(addr));
        if (page == null) {
            throw new AssertionError(String.format("no memory mapped to 0x%x [0x%x]", addr, getPageAddress(addr)));
        }
        if (getPageAddress(addr) != getPageAddress(addr + 8)) {
            // write across page boundary
            long val = value;
            for (int i = 0; i < size; i++) {
                long a = addr + i;
                if (a < page.getAddress() + 4096) {
                    if (page instanceof CoarsePage && ((CoarsePage) page).getSize() > SIZE_THRESHOLD) {
                        page = ((CoarsePage) page).transformToFine();
                        pages.put(page.getAddress(), page);
                    }
                    page.addUpdate(a, (byte) 1, (byte) val, pc, instructionCount, node);
                } else {
                    long oldaddr = page.getAddress();
                    page = pages.get(page.getAddress() + 4096);
                    if (page == null) {
                        throw new AssertionError(String.format("no memory mapped to 0x%x", oldaddr + 4096));
                    }
                    if (page instanceof CoarsePage && ((CoarsePage) page).getSize() > SIZE_THRESHOLD) {
                        page = ((CoarsePage) page).transformToFine();
                        pages.put(page.getAddress(), page);
                    }
                    page.addUpdate(a, (byte) 1, (byte) val, pc, instructionCount, node);
                }
                val >>= 8;
            }
        } else {
            if (page instanceof CoarsePage && ((CoarsePage) page).getSize() > SIZE_THRESHOLD) {
                page = ((CoarsePage) page).transformToFine();
                pages.put(page.getAddress(), page);
            }
            page.addUpdate(addr, size, value, pc, instructionCount, node);
        }
    }

    public void read(long addr, byte size, long pc, long instructionCount, Node node) {
        Page page = pages.get(getPageAddress(addr));
        if (page == null) {
            throw new AssertionError(String.format("no memory mapped to 0x%x [0x%x]", addr, getPageAddress(addr)));
        }
        if (getPageAddress(addr) != getPageAddress(addr + 8)) {
            // write across page boundary
            for (int i = 0; i < size; i++) {
                long a = addr + i;
                if (a < page.getAddress() + 4096) {
                    if (page instanceof CoarsePage && ((CoarsePage) page).getSize() > SIZE_THRESHOLD) {
                        page = ((CoarsePage) page).transformToFine();
                        pages.put(page.getAddress(), page);
                    }
                    page.addRead(a, (byte) 1, pc, instructionCount, node);
                } else {
                    long oldaddr = page.getAddress();
                    page = pages.get(page.getAddress() + 4096);
                    if (page == null) {
                        throw new AssertionError(String.format("no memory mapped to 0x%x", oldaddr + 4096));
                    }
                    if (page instanceof CoarsePage && ((CoarsePage) page).getSize() > SIZE_THRESHOLD) {
                        page = ((CoarsePage) page).transformToFine();
                        pages.put(page.getAddress(), page);
                    }
                    page.addRead(a, (byte) 1, pc, instructionCount, node);
                }
            }
        } else {
            if (page instanceof CoarsePage && ((CoarsePage) page).getSize() > SIZE_THRESHOLD) {
                page = ((CoarsePage) page).transformToFine();
                pages.put(page.getAddress(), page);
            }
            page.addRead(addr, size, pc, instructionCount, node);
        }
    }

    public byte getByte(long addr, long instructionCount) throws MemoryNotMappedException {
        Page page = pages.get(getPageAddress(addr));
        if (page == null) {
            throw new MemoryNotMappedException(String.format("no memory mapped to 0x%x [0x%x]", addr, getPageAddress(addr)));
        }
        return page.getByte(addr, instructionCount);
    }

    public long getWord(long addr, long instructionCount) throws MemoryNotMappedException {
        Page page = pages.get(getPageAddress(addr));
        if (page == null) {
            throw new MemoryNotMappedException(String.format("no memory mapped to 0x%x [0x%x]", addr, getPageAddress(addr)));
        }
        if (getPageAddress(addr) != getPageAddress(addr + 8)) {
            // write across page boundary
            long value = 0;
            for (int i = 0; i < 8; i++) {
                long a = addr + i;
                value <<= 8;
                if (a < page.getAddress() + 4096) {
                    long oldaddr = page.getAddress();
                    page = pages.get(page.getAddress() + 4096);
                    if (page == null) {
                        throw new MemoryNotMappedException(String.format("no memory mapped to 0x%x", oldaddr + 4096));
                    }
                }
                value |= page.getByte(addr, instructionCount);
            }
            return value;
        } else {
            return page.getWord(addr, instructionCount);
        }
    }

    public MemoryUpdate getLastWrite(long addr, long instructionCount) throws MemoryNotMappedException {
        Page page = pages.get(getPageAddress(addr));
        if (page == null) {
            throw new MemoryNotMappedException(String.format("no memory mapped to 0x%x [0x%x]", addr, getPageAddress(addr)));
        }
        return page.getLastUpdate(addr, instructionCount);
    }

    public MemoryRead getLastRead(long addr, long instructionCount) throws MemoryNotMappedException {
        Page page = pages.get(getPageAddress(addr));
        if (page == null) {
            throw new MemoryNotMappedException(String.format("no memory mapped to 0x%x [0x%x]", addr, getPageAddress(addr)));
        }
        return page.getLastRead(addr, instructionCount);
    }

    public MemoryRead getNextRead(long addr, long instructionCount) throws MemoryNotMappedException {
        Page page = pages.get(getPageAddress(addr));
        if (page == null) {
            throw new MemoryNotMappedException(String.format("no memory mapped to 0x%x [0x%x]", addr, getPageAddress(addr)));
        }
        return page.getNextRead(addr, instructionCount);
    }

    // TODO: use instructionCount to find *last* map time
    public Node getMapNode(long addr, @SuppressWarnings("unused") long instructionCount) throws MemoryNotMappedException {
        Page page = pages.get(getPageAddress(addr));
        if (page == null) {
            throw new MemoryNotMappedException(String.format("no memory mapped to 0x%x [0x%x]", addr, getPageAddress(addr)));
        }
        if (page.getInitialInstruction() <= instructionCount) {
            return page.getInitialNode();
        } else {
            throw new MemoryNotMappedException(String.format("no memory mapped to 0x%x [0x%x]", addr, getPageAddress(addr)));
        }
    }

    public void printStats() {
        int fine = 0;
        for (Page page : pages.values()) {
            if (page instanceof FinePage) {
                fine++;
            }
        }
        if (pages.size() > 0) {
            log.log(Levels.INFO, "Memory consists of " + pages.size() + " pages (4k/page); " + fine + " fine pages [" + ((double) fine / pages.size() * 100.0) + "%]");
        }
    }
}
