package org.graalvm.vm.trcview.analysis.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.io.Node;
import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;

public class MemoryTrace {
    private static final Logger log = Trace.create(MemoryTrace.class);

    private static final int SIZE_THRESHOLD = 4096 * 10;
    private static final Protection PROT_RW = new Protection(true, true, false);

    private final NavigableMap<Long, Page> pages = new TreeMap<>();

    private long brk = -1;

    private static long getPageAddress(long address) {
        return address & 0xFFFFFFFFFFFFF000L;
    }

    public void mmap(long address, long size, Protection prot, String name, long pc, long instructionCount, Node node, StepEvent step) {
        long addr = getPageAddress(address);
        long sz = size;
        sz += address - addr;
        while (sz > 0) {
            Page page = pages.get(addr);
            if (page == null) {
                // pages.put(addr, new CoarsePage(addr, pc, instructionCount, node));
                page = new FinePage(addr, pc, instructionCount, node, prot);
                if (name != null) {
                    page.setName(instructionCount, name);
                }
                pages.put(addr, page);
            } else {
                if (page instanceof CoarsePage && ((CoarsePage) page).getSize() > SIZE_THRESHOLD) {
                    page = ((CoarsePage) page).transformToFine();
                    pages.put(page.getAddress(), page);
                }
                if (name != null) {
                    page.setName(instructionCount, name);
                }
                page.clear(instructionCount, node, step);
            }
            sz -= 4096;
            addr += 4096;
        }
    }

    public void mmap(long address, long size, Protection prot, String name, byte[] data, long pc, long instructionCount, Node node, StepEvent step) {
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
                    // pages.put(addr, new CoarsePage(addr, pageData, pc, instructionCount, node));
                    page = new FinePage(addr, pageData, pc, instructionCount, node, prot);
                    pages.put(addr, page);
                } else {
                    // pages.put(addr, new CoarsePage(addr, pc, instructionCount, node));
                    page = new FinePage(addr, pc, instructionCount, node, prot);
                    pages.put(addr, page);
                }
            } else {
                if (page instanceof CoarsePage && ((CoarsePage) page).getSize() > SIZE_THRESHOLD) {
                    page = ((CoarsePage) page).transformToFine();
                    pages.put(page.getAddress(), page);
                }
                if (length > 0) {
                    page.overwrite(pageData, instructionCount, node, step);
                } else {
                    page.clear(instructionCount, node, step);
                }
            }
            if (name != null) {
                page.setName(instructionCount, name);
            }
            sz -= 4096;
            addr += 4096;
            offset += 4096;
        }
    }

    public void brk(long newbrk, long pc, long instructionCount, Node node, StepEvent step) {
        if (brk == -1) {
            brk = getPageAddress(newbrk);
            if (brk != newbrk) {
                brk += 4096;
            }
            long p = brk;
            while (p > 0) {
                Page page = pages.get(p);
                if (page == null) {
                    // pages.put(this.brk, new CoarsePage(p, pc, instructionCount, node));
                    page = new FinePage(p, pc, instructionCount, node, PROT_RW);
                    page.setName(instructionCount, "[heap]");
                    pages.put(this.brk, page);
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
                // pages.put(this.brk, new CoarsePage(this.brk, pc, instructionCount, node));
                page = new FinePage(this.brk, pc, instructionCount, node, PROT_RW);
                pages.put(this.brk, page);
            } else {
                if (page instanceof CoarsePage && ((CoarsePage) page).getSize() > SIZE_THRESHOLD) {
                    page = ((CoarsePage) page).transformToFine();
                    pages.put(page.getAddress(), page);
                }
                page.clear(instructionCount, node, step);
            }
            page.setName(instructionCount, "[heap]");
            this.brk += 4096;
        }
    }

    public void write(long addr, byte size, long value, long instructionCount, Node node, StepEvent step, boolean be) {
        Page page = pages.get(getPageAddress(addr));
        if (page == null) {
            // segfault
            return;
        }
        if (getPageAddress(addr) != getPageAddress(addr + size - 1)) {
            // write across page boundary
            long val = value;
            for (int i = 0; i < size; i++) {
                long a = addr + i;
                if (a < page.getAddress() + 4096) {
                    if (page instanceof CoarsePage && ((CoarsePage) page).getSize() > SIZE_THRESHOLD) {
                        page = ((CoarsePage) page).transformToFine();
                        pages.put(page.getAddress(), page);
                    }
                    page.addUpdate(a, (byte) 1, (byte) val, instructionCount, node, step, be);
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
                    page.addUpdate(a, (byte) 1, (byte) val, instructionCount, node, step, be);
                }
                val >>= 8;
            }
        } else {
            if (page instanceof CoarsePage && ((CoarsePage) page).getSize() > SIZE_THRESHOLD) {
                page = ((CoarsePage) page).transformToFine();
                pages.put(page.getAddress(), page);
            }
            page.addUpdate(addr, size, value, instructionCount, node, step, be);
        }
    }

    public void read(long addr, byte size, long instructionCount, Node node, StepEvent step) {
        Page page = pages.get(getPageAddress(addr));
        if (page == null) {
            // segfault
            return;
        }
        if (getPageAddress(addr) != getPageAddress(addr + size - 1)) {
            // write across page boundary
            for (int i = 0; i < size; i++) {
                long a = addr + i;
                if (a < page.getAddress() + 4096) {
                    if (page instanceof CoarsePage && ((CoarsePage) page).getSize() > SIZE_THRESHOLD) {
                        page = ((CoarsePage) page).transformToFine();
                        pages.put(page.getAddress(), page);
                    }
                    page.addRead(a, (byte) 1, instructionCount, node, step);
                } else {
                    long oldaddr = page.getAddress();
                    page = pages.get(page.getAddress() + 4096);
                    if (page == null) {
                        log.log(Levels.WARNING, String.format("no memory mapped to 0x%x", oldaddr + 4096));
                        return;
                    }
                    if (page instanceof CoarsePage && ((CoarsePage) page).getSize() > SIZE_THRESHOLD) {
                        page = ((CoarsePage) page).transformToFine();
                        pages.put(page.getAddress(), page);
                    }
                    page.addRead(a, (byte) 1, instructionCount, node, step);
                }
            }
        } else {
            if (page instanceof CoarsePage && ((CoarsePage) page).getSize() > SIZE_THRESHOLD) {
                page = ((CoarsePage) page).transformToFine();
                pages.put(page.getAddress(), page);
            }
            page.addRead(addr, size, instructionCount, node, step);
        }
    }

    public byte getByte(long addr, long instructionCount) throws MemoryNotMappedException {
        Page page = pages.get(getPageAddress(addr));
        if (page == null) {
            throw new MemoryNotMappedException(String.format("no memory mapped to 0x%x [0x%x]", addr, getPageAddress(addr)));
        }
        return page.getByte(addr, instructionCount);
    }

    public short getShort(long addr, long instructionCount) throws MemoryNotMappedException {
        return (short) (Byte.toUnsignedInt(getByte(addr, instructionCount)) | (Byte.toUnsignedInt(getByte(addr + 1, instructionCount)) << 8));
    }

    public int getInt(long addr, long instructionCount) throws MemoryNotMappedException {
        long value = 0;
        for (int i = 0; i < 4; i++) {
            value >>>= 8;
            value |= Byte.toUnsignedLong(getByte(addr + i, instructionCount)) << 24;
        }
        return (int) value;
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
                value >>>= 8;
                value |= Byte.toUnsignedLong(getByte(addr + i, instructionCount)) << 56;
            }
            return value;
        } else {
            return page.getWord(addr, instructionCount);
        }
    }

    public byte getLastByte(long addr) throws MemoryNotMappedException {
        Page page = pages.get(getPageAddress(addr));
        if (page == null) {
            throw new MemoryNotMappedException(String.format("no memory mapped to 0x%x [0x%x]", addr, getPageAddress(addr)));
        }
        return page.getLastByte(addr);
    }

    public short getLastShort(long addr) throws MemoryNotMappedException {
        return (short) (Byte.toUnsignedInt(getLastByte(addr)) | (Byte.toUnsignedInt(getLastByte(addr + 1)) << 8));
    }

    public int getLastInt(long addr) throws MemoryNotMappedException {
        long value = 0;
        for (int i = 0; i < 4; i++) {
            value >>>= 8;
            value |= Byte.toUnsignedLong(getLastByte(addr + i)) << 24;
        }
        return (int) value;
    }

    public long getLastWord(long addr) throws MemoryNotMappedException {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value >>>= 8;
            value |= Byte.toUnsignedLong(getLastByte(addr + i)) << 56;
        }
        return value;
    }

    public MemoryUpdate getLastWrite(long addr, long instructionCount) throws MemoryNotMappedException {
        Page page = pages.get(getPageAddress(addr));
        if (page == null) {
            throw new MemoryNotMappedException(String.format("no memory mapped to 0x%x [0x%x]", addr, getPageAddress(addr)));
        }
        return page.getLastUpdate(addr, instructionCount);
    }

    public MemoryUpdate getNextWrite(long addr, long instructionCount) throws MemoryNotMappedException {
        Page page = pages.get(getPageAddress(addr));
        if (page == null) {
            throw new MemoryNotMappedException(String.format("no memory mapped to 0x%x [0x%x]", addr, getPageAddress(addr)));
        }
        return page.getNextUpdate(addr, instructionCount);
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

    public List<MemoryUpdate> getPreviousWrites(long addr, long instructionCount, long count) throws MemoryNotMappedException {
        Page page = pages.get(getPageAddress(addr));
        if (page == null) {
            throw new MemoryNotMappedException(String.format("no memory mapped to 0x%x [0x%x]", addr, getPageAddress(addr)));
        }
        return page.getPreviousUpdates(addr, instructionCount, count);
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

    public List<MemorySegment> getRegions(long step) {
        if (pages.isEmpty()) {
            return Collections.emptyList();
        }

        List<MemorySegment> result = new ArrayList<>();
        Page page = null;
        long addr = 0;

        while (page == null) {
            Entry<Long, Page> entry = pages.ceilingEntry(addr);
            if (entry == null) {
                return Collections.emptyList();
            }
            Page p = entry.getValue();
            if (p.getInitialInstruction() <= step) {
                page = p;
                addr = p.getAddress();
            } else {
                addr = p.getAddress() + Page.SIZE;
            }
        }

        while (true) {
            long next = addr + Page.SIZE;
            Entry<Long, Page> nextPage = pages.ceilingEntry(next);
            if (nextPage == null) {
                break;
            }
            Page p = nextPage.getValue();
            try {
                if (p.getAddress() != next || !Objects.equals(page.getName(step), p.getName(step)) || !page.getProtection(step).equals(p.getProtection(step))) {
                    MemorySegment range = new MemorySegment(page.getAddress(), next - 1, page.getProtection(step), page.getName(step));
                    result.add(range);
                    page = p;
                }
            } catch (MemoryNotMappedException e) {
                // ignore
            }
            addr = p.getAddress();
        }

        try {
            if (page != null) {
                MemorySegment range = new MemorySegment(page.getAddress(), addr + Page.SIZE - 1, page.getProtection(step), page.getName(step));
                result.add(range);
            }
        } catch (MemoryNotMappedException e) {
            // ignore
        }

        return result;
    }

    public void trim() {
        for (Page page : pages.values()) {
            page.trim();
        }
    }
}
