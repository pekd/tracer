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

    public long getAddress() {
        return address;
    }

    public byte[] getData() {
        return data;
    }

    public long getInitialPC() {
        return firstPC;
    }

    public long getInitialInstruction() {
        return firstInstructionCount;
    }

    public Node getInitialNode() {
        return firstNode;
    }

    public void addUpdate(long addr, byte size, long value, long pc, long instructionCount, Node node) {
        assert addr >= address && addr < (address + data.length);
        assert addr + size <= (address + data.length);
        int off = (int) (addr - address);
        if (updates[off] == null) {
            updates[off] = new ArrayList<>();
        }
        updates[off].add(new MemoryUpdate(addr, size, value, pc, instructionCount, node));
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

    public void clear(long pc, long instructionCount, Node node) {
        for (int i = 0; i < 4096; i += 8) {
            addUpdate(address + i, (byte) 8, 0, pc, instructionCount, node);
        }
    }

    public void overwrite(byte[] update, long pc, long instructionCount, Node node) {
        for (int i = 0; i < 4096; i += 8) {
            long value = Endianess.get64bitLE(update, i);
            addUpdate(address + i, (byte) 8, value, pc, instructionCount, node);
        }
    }

    public byte getByte(long addr, long instructionCount) throws MemoryNotMappedException {
        MemoryUpdate update = getLastUpdate(addr, instructionCount);
        if (update == null) {
            return data[(int) (addr - address)];
        } else {
            return update.getByte(addr);
        }
    }

    public MemoryUpdate getLastUpdate(long addr, long instructionCount) throws MemoryNotMappedException {
        if (addr < address || addr >= address + 4096) {
            throw new AssertionError(String.format("wrong page for address 0x%x", addr));
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
