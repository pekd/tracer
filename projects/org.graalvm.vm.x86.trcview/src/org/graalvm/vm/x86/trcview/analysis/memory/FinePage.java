package org.graalvm.vm.x86.trcview.analysis.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.graalvm.vm.util.io.Endianess;
import org.graalvm.vm.x86.trcview.io.Node;

public class FinePage implements Page {
    private final long firstPC;
    private final long firstInstructionCount;
    private final Node firstNode;

    private final long address;
    @SuppressWarnings("unchecked") private final List<MemoryUpdate>[] updates = new ArrayList[4096];
    @SuppressWarnings("unchecked") private final List<MemoryRead>[] reads = new ArrayList[4096];
    private final byte[] data = new byte[4096]; // initial data

    public FinePage(long address, long pc, long instructionCount, Node node) {
        this.address = address;
        this.firstPC = pc;
        this.firstInstructionCount = instructionCount;
        this.firstNode = node;
    }

    public FinePage(long address, byte[] data, long pc, long instructionCount, Node node) {
        this(address, pc, instructionCount, node);
        assert data.length == 4096;
        System.arraycopy(data, 0, this.data, 0, 4096);
    }

    @Override
    public long getAddress() {
        return address;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public long getInitialPC() {
        return firstPC;
    }

    @Override
    public long getInitialInstruction() {
        return firstInstructionCount;
    }

    @Override
    public Node getInitialNode() {
        return firstNode;
    }

    @Override
    public void addUpdate(long addr, byte size, long value, long pc, long instructionCount, Node node) {
        assert addr >= address && addr < (address + data.length);
        assert addr + size <= (address + data.length);
        int off = (int) (addr - address);
        if (updates[off] == null) {
            updates[off] = new ArrayList<>();
        }
        updates[off].add(new MemoryUpdate(addr, size, value, pc, instructionCount, node));
    }

    @Override
    public void addRead(long addr, byte size, long pc, long instructionCount, Node node) {
        assert addr >= address && addr < (address + data.length);
        assert addr + size <= (address + data.length);
        int off = (int) (addr - address);
        if (reads[off] == null) {
            reads[off] = new ArrayList<>();
        }
        reads[off].add(new MemoryRead(addr, size, pc, instructionCount, node));
    }

    public void addUpdate(MemoryUpdate update) {
        long addr = update.address;
        assert addr >= address && addr < (address + data.length);
        assert addr + update.size <= (address + data.length);
        int off = (int) (addr - address);
        if (updates[off] == null) {
            updates[off] = new ArrayList<>();
        }
        updates[off].add(update);
    }

    public void addRead(MemoryRead read) {
        long addr = read.address;
        assert addr >= address && addr < (address + data.length);
        assert addr + read.size <= (address + data.length);
        int off = (int) (addr - address);
        if (reads[off] == null) {
            reads[off] = new ArrayList<>();
        }
        reads[off].add(read);
    }

    @Override
    public void clear(long pc, long instructionCount, Node node) {
        for (int i = 0; i < 4096; i += 8) {
            addUpdate(address + i, (byte) 8, 0, pc, instructionCount, node);
        }
    }

    @Override
    public void overwrite(byte[] update, long pc, long instructionCount, Node node) {
        for (int i = 0; i < 4096; i += 8) {
            long value = Endianess.get64bitLE(update, i);
            addUpdate(address + i, (byte) 8, value, pc, instructionCount, node);
        }
    }

    @Override
    public byte getByte(long addr, long instructionCount) throws MemoryNotMappedException {
        MemoryUpdate update = getLastUpdate(addr, instructionCount);
        if (update == null) {
            return data[(int) (addr - address)];
        } else {
            return update.getByte(addr);
        }
    }

    @Override
    public MemoryUpdate getLastUpdate(long addr, long instructionCount) throws MemoryNotMappedException {
        if (addr < address || addr >= address + 4096) {
            throw new AssertionError(String.format("wrong page for address 0x%x", addr));
        }

        if (instructionCount < firstInstructionCount) {
            throw new MemoryNotMappedException(String.format("no memory mapped to 0x%x", addr));
        }

        int off = (int) (addr - address);
        if (updates[off] == null) {
            return null;
        }

        if (updates[off].isEmpty() || instructionCount == firstInstructionCount) {
            // no update until now
            return null;
        } else if (updates[off].get(0).instructionCount > instructionCount) {
            // first update is after instructionCount
            return null;
        } else if (updates[off].get(0).instructionCount == instructionCount) {
            // updates start at our timestamp; find last update
            MemoryUpdate result = null;
            for (MemoryUpdate update : updates[off]) {
                if (update.instructionCount > instructionCount) {
                    return result;
                }
                if (update.contains(addr)) {
                    result = update;
                }
            }
            return result;
        } else if (instructionCount < firstInstructionCount) {
            throw new MemoryNotMappedException("memory is not mapped at this time");
        }

        // find update timestamp
        MemoryUpdate target = new MemoryUpdate(addr, (byte) 1, 0, 0, instructionCount, null);
        int idx = Collections.binarySearch(updates[off], target, (a, b) -> {
            return Long.compareUnsigned(a.instructionCount, b.instructionCount);
        });

        if (idx > 0) {
            return updates[off].get(idx);
        } else {
            idx = ~idx;
            if (idx == 0) {
                return null;
            } else {
                return updates[off].get(idx - 1);
            }
        }
    }

    @Override
    public MemoryRead getLastRead(long addr, long instructionCount) throws MemoryNotMappedException {
        if (addr < address || addr >= address + 4096) {
            throw new AssertionError(String.format("wrong page for address 0x%x", addr));
        }

        int off = (int) (addr - address);
        if (reads[off] == null) {
            return null;
        }

        if (reads[off].isEmpty() || instructionCount == firstInstructionCount) {
            // no read until now
            return null;
        } else if (reads[off].get(0).instructionCount > instructionCount) {
            // first read is after instructionCount
            return null;
        } else if (reads[off].get(0).instructionCount == instructionCount) {
            // reads start at our timestamp; find last update
            MemoryRead result = null;
            for (MemoryRead read : reads[off]) {
                if (read.instructionCount > instructionCount) {
                    return result;
                }
                if (read.contains(addr)) {
                    result = read;
                }
            }
            return result;
        } else if (instructionCount < firstInstructionCount) {
            throw new MemoryNotMappedException("memory is not mapped at this time");
        }

        // find read timestamp
        MemoryRead target = new MemoryRead(addr, (byte) 1, 0, instructionCount, null);
        int idx = Collections.binarySearch(reads[off], target, (a, b) -> {
            return Long.compareUnsigned(a.instructionCount, b.instructionCount);
        });

        if (idx > 0) {
            return reads[off].get(idx);
        } else {
            idx = ~idx;
            if (idx == 0) {
                return null;
            } else {
                return reads[off].get(idx - 1);
            }
        }
    }

    @Override
    public MemoryRead getNextRead(long addr, long instructionCount) throws MemoryNotMappedException {
        if (addr < address || addr >= address + 4096) {
            throw new AssertionError(String.format("wrong page for address 0x%x", addr));
        }

        int off = (int) (addr - address);
        if (reads[off] == null) {
            return null;
        }

        if (reads[off].isEmpty()) {
            // no reads
            return null;
        } else if (reads[off].get(0).instructionCount >= instructionCount) {
            // first read is after instructionCount
            return reads[off].get(0);
        } else if (instructionCount < firstInstructionCount) {
            throw new MemoryNotMappedException("memory is not mapped at this time");
        }

        // find read timestamp
        MemoryRead target = new MemoryRead(addr, (byte) 1, 0, instructionCount, null);
        int idx = Collections.binarySearch(reads[off], target, (a, b) -> {
            return Long.compareUnsigned(a.instructionCount, b.instructionCount);
        });

        if (idx > 0) {
            return reads[off].get(idx);
        } else {
            idx = ~idx;
            if (idx >= reads[off].size()) {
                return null;
            } else {
                return reads[off].get(idx);
            }
        }
    }

    @Override
    public long getWord(long addr, long instructionCount) throws MemoryNotMappedException {
        // TODO: this could be implemented more efficiently
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result >>>= 8;
            result |= Byte.toUnsignedLong(getByte(addr + i, instructionCount)) << 56;
        }
        return result;
    }
}
