/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.graalvm.vm.memory;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.graalvm.vm.memory.exception.SegmentationViolation;
import org.graalvm.vm.memory.vector.Vector128;
import org.graalvm.vm.memory.vector.Vector256;
import org.graalvm.vm.memory.vector.Vector512;
import org.graalvm.vm.posix.api.Errno;
import org.graalvm.vm.posix.api.PosixException;
import org.graalvm.vm.util.io.Endianess;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public class JavaVirtualMemory extends VirtualMemory {
    private final NavigableMap<Long, MemoryPage> pages;

    private MemoryPage cache;
    private MemoryPage cache2;
    private long cacheHits;
    private long cacheMisses;

    public JavaVirtualMemory() {
        this(POINTER_BASE, POINTER_END);
    }

    public JavaVirtualMemory(long lo, long hi) {
        super(lo, hi);
        pages = new TreeMap<>(Long::compareUnsigned);
        cache = null;
        cache2 = null;
        cacheHits = 0;
        cacheMisses = 0;
        set64bit();
        setLE();
    }

    @TruffleBoundary
    public Collection<MemoryPage> getPages() {
        return Collections.unmodifiableCollection(pages.values());
    }

    @TruffleBoundary
    @Override
    public void add(MemoryPage page) {
        checkConsistency();
        if (DEBUG) {
            CompilerDirectives.transferToInterpreter();
            System.out.printf("add(MemoryPage): Adding page at 0x%016X-0x%016X\n", page.base, page.end);
            printLayout();
        }
        boolean ok = Long.compareUnsigned(page.end, pointerBase) <= 0 || Long.compareUnsigned(page.end, pointerEnd) > 0;
        if (!ok) {
            if (DEBUG) {
                CompilerDirectives.transferToInterpreter();
                System.out.printf("Allocating memory at 0x%016X (0x%x bytes)\n", page.base, page.size);
            }
            allocator.allocat(page.base, page.size);
        }
        try {
            MemoryPage oldPage = get(page.base);
            if (page.contains(oldPage.base) && page.contains(oldPage.end - 1)) {
                if (DEBUG) {
                    CompilerDirectives.transferToInterpreter();
                    System.out.printf("Removing old page: 0x%016X-0x%016X, new page is 0x%016X-0x%016X\n", oldPage.base, oldPage.end, page.base, page.end);
                }
                pages.remove(oldPage.base);
                cache = null;
                cache2 = null;

                // check if more pages have to be deleted
                long addr = oldPage.end;
                while (true) {
                    Long pageaddr = pages.ceilingKey(addr);
                    if (DEBUG) {
                        CompilerDirectives.transferToInterpreter();
                        if (pageaddr == null) {
                            System.out.println("no more pages");
                        } else {
                            System.out.printf("Examining page at 0x%016X\n", pageaddr);
                        }
                    }
                    if (pageaddr == null) {
                        break;
                    } else {
                        if (pageaddr > page.end) {
                            if (DEBUG) {
                                CompilerDirectives.transferToInterpreter();
                                System.out.printf("Page at 0x%016X does not overlap\n", pageaddr);
                            }
                            break;
                        } else {
                            MemoryPage p = pages.get(pageaddr);
                            if (page.contains(p.end - 1)) {
                                // fully contained
                                if (DEBUG) {
                                    CompilerDirectives.transferToInterpreter();
                                    System.out.printf("Page fully contained, removing page 0x%016X-0x%016X\n", p.base, p.end);
                                }
                                pages.remove(p.base);
                            } else if (page.contains(p.base)) {
                                // split
                                CompilerDirectives.transferToInterpreter();
                                throw new RuntimeException("split not implemented");
                            }
                            addr = p.end;
                        }
                    }
                }
            } else {
                if (DEBUG) {
                    CompilerDirectives.transferToInterpreter();
                    System.out.printf("Splitting old page: 0x%016X-0x%016X, new page is 0x%016X-0x%016X\n", oldPage.base, oldPage.end, page.base, page.end);
                }
                long size1 = page.base - oldPage.base;
                long size2 = oldPage.end - page.end;
                if (DEBUG) {
                    CompilerDirectives.transferToInterpreter();
                    System.out.printf("size1 = 0x%016X, size2 = 0x%016X\n", size1, size2);
                }
                if (size1 > 0) {
                    MemoryPage p = new MemoryPage(oldPage, oldPage.base, size1);
                    pages.put(oldPage.base, p);
                    cache = null;
                    cache2 = null;
                    mapSequence++;
                    if (DEBUG) {
                        CompilerDirectives.transferToInterpreter();
                        System.out.printf("Added new page: 0x%016X[0x%016X;0x%016X]\n", oldPage.base, pages.get(oldPage.base).base, pages.get(oldPage.base).end);
                    }
                }
                if (size2 > 0) {
                    MemoryPage p = new MemoryPage(oldPage, page.end, size2);
                    pages.put(page.end, p);
                    cache = null;
                    cache2 = null;
                    mapSequence++;
                    if (DEBUG) {
                        CompilerDirectives.transferToInterpreter();
                        System.out.printf("Added new page: 0x%016X[0x%016X;0x%016X]\n", page.end, pages.get(page.end).base, pages.get(page.end).end);
                    }
                }
            }
        } catch (SegmentationViolation e) {
            if (DEBUG) {
                CompilerDirectives.transferToInterpreter();
                System.out.println("Segmentation violation: " + e);
            }
        }
        pages.put(page.base, page);
        cache = null;
        cache2 = null;
        mapSequence++;
        if (page.base != pageStart(page.base)) {
            if (DEBUG) {
                System.out.printf("bad page start: 0x%016X, should be 0x%016X\n", page.base,
                                pageStart(page.base));
            }
            long base = pageStart(page.base);
            long size = page.base - base;
            try {
                get(base);
            } catch (SegmentationViolation e) {
                assert size == roundToPageSize(size);
                assert base == pageStart(base);
                Memory buf = new ByteMemory(size, bigEndian);
                MemoryPage bufpage = new MemoryPage(buf, base, size, page.name);
                pages.put(base, bufpage);
                cache = null;
                cache2 = null;
                mapSequence++;
            }
        }
        if (DEBUG) {
            CompilerDirectives.transferToInterpreter();
            printLayout();
        }
        checkConsistency();
    }

    @TruffleBoundary
    @Override
    public void remove(long addr, long len) throws PosixException {
        cache = null;
        cache2 = null;
        mapSequence++;
        long length = roundToPageSize(len);
        long address = addr(addr);
        if ((address & ~PAGE_MASK) != 0) {
            throw new PosixException(Errno.EINVAL);
        }
        try {
            for (long p = address; Long.compareUnsigned(p, address + length) < 0;) {
                MemoryPage page = getFloorEntry(p);
                if (p != page.base) { // split page and remove mapping in the middle
                    assert p > page.base;
                    if (DEBUG) {
                        CompilerDirectives.transferToInterpreter();
                        System.out.printf("Splitting old page: 0x%016X-0x%016X, removed page is 0x%016X-0x%016X\n", page.base, page.end, addr, addr + length);
                    }
                    pages.remove(page.base);
                    allocator.free(page.base, page.size);
                    long size1 = addr - page.base;
                    long size2 = page.end - (addr + length);
                    if (DEBUG) {
                        CompilerDirectives.transferToInterpreter();
                        System.out.printf("size1 = 0x%016X, size2 = 0x%016X\n", size1, size2);
                    }
                    if (size1 > 0) {
                        MemoryPage pag = new MemoryPage(page, page.base, size1);
                        pages.put(page.base, pag);
                        allocator.allocat(page.base, size1);
                        cache = null;
                        cache2 = null;
                        if (DEBUG) {
                            CompilerDirectives.transferToInterpreter();
                            System.out.printf("Added new page: 0x%016X[0x%016X;0x%016X] (off=0x%x)\n", page.base, pages.get(page.base).base, pages.get(page.base).end, pag.getOffset(pag.base));
                        }
                    }
                    if (size2 > 0) {
                        MemoryPage pag = new MemoryPage(page, addr + length, size2);
                        pages.put(addr + length, pag);
                        allocator.allocat(addr + length, size2);
                        cache = null;
                        cache2 = null;
                        if (DEBUG) {
                            CompilerDirectives.transferToInterpreter();
                            System.out.printf("Added new page: 0x%016X[0x%016X;0x%016X]\n", addr + length, pages.get(addr + length).base, pages.get(addr + length).end);
                        }
                    }
                    p = page.end;
                } else if (page.size > length) {
                    long sz = page.size - length;
                    MemoryPage tail = new MemoryPage(page, page.base + length, sz);
                    pages.remove(page.base);
                    allocator.free(page.base, length);
                    pages.put(page.base + length, tail);
                    allocator.allocat(page.base + length, sz);
                    p = page.end;
                } else {
                    pages.remove(page.base);
                    allocator.free(page.base, page.size);
                    p = page.end;
                }
            }
        } catch (SegmentationViolation e) {
            // swallow
        }
        if (DEBUG) {
            printLayout();
        }
    }

    @TruffleBoundary
    @Override
    public MemoryPage allocate(long size, String name) {
        long base = allocator.alloc(size);
        if (base == 0) {
            return null;
        } else {
            assert base == addr(base);
            assert size == roundToPageSize(size);
            Memory mem = new ByteMemory(size, bigEndian);
            MemoryPage page = new MemoryPage(mem, base, size, name);
            add(page);
            return page;
        }
    }

    @TruffleBoundary
    @Override
    public MemoryPage allocate(Memory memory, long size, String name, long offset) {
        long base = allocator.alloc(size);
        if (base == 0) {
            return null;
        } else {
            MemoryPage page = new MemoryPage(memory, base, size, name, offset);
            add(page);
            return page;
        }
    }

    @TruffleBoundary
    @Override
    public void free(long address) {
        MemoryPage page = pages.remove(address);
        allocator.free(address, page.size);
    }

    @TruffleBoundary
    private MemoryPage getFloorEntry(long addr) {
        Map.Entry<Long, MemoryPage> entry = pages.floorEntry(addr);
        if (entry == null) {
            throw new SegmentationViolation(addr);
        }
        MemoryPage page = entry.getValue();
        if (page.contains(addr)) {
            if (cache != null) {
                cache2 = page;
            } else {
                cache = page;
            }
            return page;
        } else {
            throw new SegmentationViolation(addr);
        }
    }

    @TruffleBoundary
    @Override
    public MemoryPage get(long address) {
        long addr = addr(address);
        // TODO: fix this!
        if (cache != null && cache.contains(addr)) {
            cacheHits++;
            return cache;
        } else if (cache2 != null && cache2.contains(addr)) {
            cacheHits++;
            // swap cache entries
            MemoryPage page = cache2;
            cache2 = cache;
            cache = page;
            return page;
        } else {
            cacheMisses++;
        }
        // slow path
        return getFloorEntry(addr);
    }

    @TruffleBoundary
    @Override
    public boolean contains(long address) {
        long addr = addr(address);
        Map.Entry<Long, MemoryPage> entry = pages.floorEntry(addr);
        if (entry == null) {
            return false;
        }
        MemoryPage page = entry.getValue();
        return page.contains(addr);
    }

    @TruffleBoundary
    @Override
    public byte getI8(long address) {
        long ptr = addr(address);
        try {
            MemoryPage page = get(ptr);
            byte val = page.getI8(ptr);
            logMemoryRead(address, 1, val);
            return val;
        } catch (Throwable t) {
            logMemoryRead(address, 1);
            throw t;
        }
    }

    @TruffleBoundary
    @Override
    public short getI16(long address) {
        long ptr = addr(address);
        try {
            MemoryPage page = get(ptr);
            short val = page.getI16(ptr);
            logMemoryRead(address, 2, val);
            return val;
        } catch (SegmentationViolation e) { // unaligned access across page boundary
            try {
                MemoryPage page = get(ptr);
                byte[] bytes = new byte[]{getI8(ptr), getI8(ptr + 1)};
                boolean isBE = page.getMemory().isBE();
                short value = isBE ? Endianess.get16bitBE(bytes) : Endianess.get16bitLE(bytes);
                logMemoryRead(address, 2, value);
                return value;
            } catch (Throwable t) {
                logMemoryRead(address, 2);
                throw t;
            }
        } catch (Throwable t) {
            logMemoryRead(address, 2);
            throw t;
        }
    }

    @TruffleBoundary
    @Override
    public int getI32(long address) {
        long ptr = addr(address);
        try {
            MemoryPage page = get(ptr);
            int v = page.getI32(ptr);
            logMemoryRead(address, 4, v);
            return v;
        } catch (SegmentationViolation e) { // unaligned access across page boundary
            try {
                MemoryPage page = get(ptr);
                byte[] bytes = new byte[]{getI8(ptr), getI8(ptr + 1), getI8(ptr + 2), getI8(ptr + 3)};
                boolean isBE = page.getMemory().isBE();
                int value = isBE ? Endianess.get32bitBE(bytes) : Endianess.get32bitLE(bytes);
                logMemoryRead(address, 4, value);
                return value;
            } catch (Throwable t) {
                logMemoryRead(address, 4);
                throw t;
            }
        } catch (Throwable t) {
            logMemoryRead(address, 4);
            throw t;
        }
    }

    @TruffleBoundary
    @Override
    public long getI64(long address) {
        long ptr = addr(address);
        try {
            MemoryPage page = get(ptr);
            long v = page.getI64(ptr);
            logMemoryRead(address, 8, v);
            return v;
        } catch (SegmentationViolation e) { // unaligned access across page boundary
            try {
                MemoryPage page = get(ptr);
                byte[] bytes = new byte[]{getI8(ptr), getI8(ptr + 1), getI8(ptr + 2), getI8(ptr + 3), getI8(ptr + 4), getI8(ptr + 5), getI8(ptr + 6), getI8(ptr + 7)};
                boolean isBE = page.getMemory().isBE();
                long value = isBE ? Endianess.get64bitBE(bytes) : Endianess.get64bitLE(bytes);
                logMemoryRead(address, 8, value);
                return value;
            } catch (Throwable t) {
                logMemoryRead(address, 8);
                throw t;
            }
        } catch (Throwable t) {
            logMemoryRead(address, 8);
            throw t;
        }
    }

    @TruffleBoundary
    @Override
    public Vector128 getI128(long address) {
        long ptr = addr(address);
        try {
            MemoryPage page = get(ptr);
            Vector128 v = page.getI128(ptr);
            logMemoryRead(address, v);
            return v;
        } catch (SegmentationViolation e) { // unaligned access across page boundary
            try {
                MemoryPage page = get(ptr);
                boolean isBE = page.getMemory().isBE();
                Vector128 value = isBE ? new Vector128(getI64(ptr), getI64(ptr + 8)) : new Vector128(getI64(ptr + 8), getI64(ptr));
                logMemoryRead(address, value);
                return value;
            } catch (Throwable t) {
                logMemoryRead(address, 16);
                throw t;
            }
        } catch (Throwable t) {
            logMemoryRead(address, 16);
            throw t;
        }
    }

    @TruffleBoundary
    @Override
    public Vector256 getI256(long address) {
        long ptr = addr(address);
        try {
            MemoryPage page = get(ptr);
            Vector256 v = page.getI256(ptr);
            logMemoryRead(address, v);
            return v;
        } catch (SegmentationViolation e) { // unaligned access across page boundary
            try {
                MemoryPage page = get(ptr);
                boolean isBE = page.getMemory().isBE();
                Vector256 value = isBE ? new Vector256(getI128(ptr), getI128(ptr + 16)) : new Vector256(getI128(ptr + 16), getI128(ptr));
                logMemoryRead(address, value);
                return value;
            } catch (Throwable t) {
                logMemoryRead(address, 32);
                throw t;
            }
        } catch (Throwable t) {
            logMemoryRead(address, 32);
            throw t;
        }
    }

    @TruffleBoundary
    @Override
    public Vector512 getI512(long address) {
        long ptr = addr(address);
        try {
            MemoryPage page = get(ptr);
            Vector512 v = page.getI512(ptr);
            logMemoryRead(address, v);
            return v;
        } catch (SegmentationViolation e) { // unaligned access across page boundary
            try {
                MemoryPage page = get(ptr);
                boolean isBE = page.getMemory().isBE();
                Vector512 value = isBE ? new Vector512(getI256(ptr), getI256(ptr + 32)) : new Vector512(getI256(ptr + 32), getI256(ptr));
                logMemoryRead(address, value);
                return value;
            } catch (Throwable t) {
                logMemoryRead(address, 64);
                throw t;
            }
        } catch (Throwable t) {
            logMemoryRead(address, 64);
            throw t;
        }
    }

    @TruffleBoundary
    @Override
    public void setI8(long address, byte val) {
        logMemoryWrite(address, 1, val);
        long ptr = addr(address);
        MemoryPage page = get(ptr);
        page.setI8(ptr, val);
    }

    @TruffleBoundary
    @Override
    public void setI16(long address, short val) {
        logMemoryWrite(address, 2, val);
        long ptr = addr(address);
        MemoryPage page = get(ptr);
        try {
            page.setI16(ptr, val);
        } catch (SegmentationViolation e) { // unaligned access across page boundary
            boolean isBE = page.getMemory().isBE();
            byte[] bytes = new byte[2];
            if (isBE) {
                Endianess.set16bitBE(bytes, 0, val);
            } else {
                Endianess.set16bitLE(bytes, 0, val);
            }
            setI8(address, bytes[0]);
            setI8(address + 1, bytes[1]);
        }
    }

    @TruffleBoundary
    @Override
    public void setI32(long address, int val) {
        logMemoryWrite(address, 4, val);
        long ptr = addr(address);
        MemoryPage page = get(ptr);
        try {
            page.setI32(ptr, val);
        } catch (SegmentationViolation e) { // unaligned access across page boundary
            boolean isBE = page.getMemory().isBE();
            byte[] bytes = new byte[4];
            if (isBE) {
                Endianess.set32bitBE(bytes, 0, val);
            } else {
                Endianess.set32bitLE(bytes, 0, val);
            }
            for (int i = 0; i < bytes.length; i++) {
                setI8(address + i, bytes[i]);
            }
        }
    }

    @TruffleBoundary
    @Override
    public void setI64(long address, long val) {
        logMemoryWrite(address, 8, val);
        long ptr = addr(address);
        MemoryPage page = get(ptr);
        try {
            page.setI64(ptr, val);
        } catch (SegmentationViolation e) { // unaligned access across page boundary
            boolean isBE = page.getMemory().isBE();
            byte[] bytes = new byte[8];
            if (isBE) {
                Endianess.set64bitBE(bytes, 0, val);
            } else {
                Endianess.set64bitLE(bytes, 0, val);
            }
            for (int i = 0; i < bytes.length; i++) {
                setI8(address + i, bytes[i]);
            }
        }
    }

    @TruffleBoundary
    @Override
    public void setI128(long address, Vector128 val) {
        logMemoryWrite(address, val);
        long ptr = addr(address);
        MemoryPage page = get(ptr);
        try {
            page.setI128(ptr, val);
        } catch (SegmentationViolation e) { // unaligned access across page boundary
            boolean isBE = page.getMemory().isBE();
            if (isBE) {
                setI64(address, val.getI64(0));
                setI64(address + 8, val.getI64(1));
            } else {
                setI64(address, val.getI64(1));
                setI64(address + 8, val.getI64(0));
            }
        }
    }

    @TruffleBoundary
    @Override
    public void setI128(long address, long hi, long lo) {
        setI128(address, new Vector128(hi, lo));
    }

    @TruffleBoundary
    @Override
    public void setI256(long address, Vector256 val) {
        logMemoryWrite(address, val);
        long ptr = addr(address);
        MemoryPage page = get(ptr);
        try {
            page.setI256(ptr, val);
        } catch (SegmentationViolation e) { // unaligned access across page boundary
            boolean isBE = page.getMemory().isBE();
            if (isBE) {
                setI128(address, val.getI128(0));
                setI128(address + 16, val.getI128(1));
            } else {
                setI128(address, val.getI128(1));
                setI128(address + 16, val.getI128(0));
            }
        }
    }

    @TruffleBoundary
    @Override
    public void setI512(long address, Vector512 val) {
        logMemoryWrite(address, val);
        long ptr = addr(address);
        MemoryPage page = get(ptr);
        try {
            page.setI512(ptr, val);
        } catch (SegmentationViolation e) { // unaligned access across page boundary
            boolean isBE = page.getMemory().isBE();
            if (isBE) {
                setI256(address, val.getI256(0));
                setI256(address + 16, val.getI256(1));
            } else {
                setI256(address, val.getI256(1));
                setI256(address + 16, val.getI256(0));
            }
        }
    }

    // TODO: this is *not* atomic for now!
    @Override
    public boolean cmpxchgI8(long address, byte expected, byte x) {
        byte actual = getI8(address);
        if (actual == expected) {
            setI8(address, x);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean cmpxchgI16(long address, short expected, short x) {
        short actual = getI16(address);
        if (actual == expected) {
            setI16(address, x);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean cmpxchgI32(long address, int expected, int x) {
        int actual = getI32(address);
        if (actual == expected) {
            setI32(address, x);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean cmpxchgI64(long address, long expected, long x) {
        long actual = getI64(address);
        if (actual == expected) {
            setI64(address, x);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean cmpxchgI128(long address, Vector128 expected, Vector128 x) {
        Vector128 actual = getI128(address);
        if (actual == expected) {
            setI128(address, x);
            return true;
        } else {
            return false;
        }
    }

    @TruffleBoundary
    @Override
    public void mprotect(long address, long len, boolean r, boolean w, boolean x) throws PosixException {
        long remaining = roundToPageSize(len);
        long p = pageStart(addr(address));
        while (remaining > 0) {
            assert roundToPageSize(remaining) == remaining;
            assert pageStart(p) == p;
            MemoryPage page = get(p);
            if (page.base == addr(p) && Long.compareUnsigned(page.size, remaining) <= 0) {
                // whole "page"
                page.r = r;
                page.w = w;
                page.x = x;
                p = page.end;
                remaining -= page.size;
            } else if (page.base == addr(p) && Long.compareUnsigned(page.size, remaining) > 0) {
                // split, modify first part
                MemoryPage p1 = new MemoryPage(page, page.base, remaining);
                MemoryPage p2 = new MemoryPage(page, page.base + remaining, page.size - remaining);
                p1.r = r;
                p1.w = w;
                p1.x = x;
                pages.remove(page.base);
                pages.put(p1.base, p1);
                pages.put(p2.base, p2);
                cache = null;
                cache2 = null;
                checkConsistency();
                return;
            } else {
                // split, modify second part
                assert Long.compareUnsigned(page.base, p) < 0;
                long off = p - page.base;
                MemoryPage p1 = new MemoryPage(page, page.base, off);
                MemoryPage p2 = new MemoryPage(page, page.base + off, page.size - off);

                if (Long.compareUnsigned(p2.size, remaining) > 0) {
                    // second part is larger, we cut a piece out of the middle of a bigger page
                    MemoryPage p3 = new MemoryPage(page, page.base + off, remaining);
                    MemoryPage p4 = new MemoryPage(page, page.base + off, page.size - off - remaining);
                    p3.r = r;
                    p3.w = w;
                    p3.x = x;
                    pages.remove(page.base);
                    pages.put(p1.base, p1);
                    pages.put(p3.base, p3);
                    pages.put(p4.base, p4);
                } else {
                    p2.r = r;
                    p2.w = w;
                    p2.x = x;
                    pages.remove(page.base);
                    pages.put(p1.base, p1);
                    pages.put(p2.base, p2);
                }
                cache = null;
                cache2 = null;
                p = page.end;
                remaining -= page.size;
                checkConsistency();
            }
        }
        checkConsistency();
    }

    @Override
    public boolean isExecutable(long address) {
        MemoryPage page = get(address);
        return page.x;
    }

    @Override
    public void printMaps(PrintStream out) {
        CompilerAsserts.neverPartOfCompilation();
        pages.entrySet().stream().map((x) -> x.getValue().toString()).forEachOrdered(out::println);
    }

    public void printStats(PrintStream out) {
        CompilerAsserts.neverPartOfCompilation();
        out.printf("Cache: %d hits, %d misses (%5.3f%% hits)\n", cacheHits, cacheMisses,
                        (double) cacheHits / (double) (cacheHits + cacheMisses));
    }

    @Override
    public void printAddressInfo(long addr, PrintStream out) {
        MemoryPage page = get(addr);
        out.printf("Memory region name: '%s', base = 0x%016x (offset = 0x%016x)\n", page.name, page.base, addr - page.base);
    }

    private static MemorySegment getSegment(MemoryPage page) {
        StringBuilder permissions = new StringBuilder(4);
        if (page.r) {
            permissions.append('r');
        }
        if (page.w) {
            permissions.append('r');
        }
        if (page.x) {
            permissions.append('r');
        }
        return new MemorySegment(page.base, page.end, permissions.toString(), page.fileOffset, page.name);
    }

    @Override
    public Collection<MemorySegment> getSegments() {
        CompilerAsserts.neverPartOfCompilation();
        return pages.entrySet().stream().map(x -> getSegment(x.getValue())).sorted((x, y) -> Long.compareUnsigned(x.start, y.start)).collect(Collectors.toList());
    }

    @TruffleBoundary
    public void checkConsistency() {
        if (pages.size() < 2) {
            return;
        }
        for (long addr : pages.keySet()) {
            MemoryPage page = pages.get(addr);
            Entry<Long, MemoryPage> before = addr > 0 ? pages.floorEntry(addr - 1) : null;
            Entry<Long, MemoryPage> after = pages.ceilingEntry(addr + 1);
            if (before != null) {
                // check overlap
                MemoryPage p = before.getValue();
                if (p.getEnd() > addr) {
                    printLayout();
                    throw new RuntimeException("violation at " + p + " vs " + page);
                }
            }
            if (after != null) {
                // check overlap
                MemoryPage p = after.getValue();
                if (p.getBase() < page.getEnd()) {
                    printLayout();
                    throw new RuntimeException("violation at " + p + " vs " + page);
                }
            }
        }
    }
}
