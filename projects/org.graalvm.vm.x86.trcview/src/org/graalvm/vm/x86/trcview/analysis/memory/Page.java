package org.graalvm.vm.x86.trcview.analysis.memory;

import java.util.ArrayList;
import java.util.List;

import org.graalvm.vm.util.io.Endianess;
import org.graalvm.vm.x86.trcview.io.Node;

public class Page {
    private final long firstPC;
    private final long firstInstructionCount;
    private final Node firstNode;

    private final long address;
    private final byte[] data = new byte[4096]; // initial data
    private final List<MemoryUpdate> updates = new ArrayList<>();

    public Page(long address, long pc, long instructionCount, Node node) {
        this.address = address;
        this.firstPC = pc;
        this.firstInstructionCount = instructionCount;
        this.firstNode = node;
    }

    public Page(long address, byte[] data, long pc, long instructionCount, Node node) {
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

    public void addUpdate(long addr, byte size, long value, long pc, long instructionCount, Node node) {
        assert addr >= address && addr < (address + data.length);
        assert addr + size <= (address + data.length);
        updates.add(new MemoryUpdate(addr, size, value, pc, instructionCount, node));
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

    public byte getByte(long addr, long instructionCount) {
        if (addr < address || addr >= address + 4096) {
            throw new AssertionError(String.format("wrong page for address 0x%x", addr));
        }

        MemoryUpdate last = null;
        for (MemoryUpdate update : updates) {
            if (update.address <= addr && update.address + update.size > addr) {
                if (update.instructionCount == instructionCount) {
                    return update.getByte(addr);
                } else if (update.instructionCount > instructionCount) {
                    if (last == null) {
                        return data[(int) (addr - address)];
                    } else {
                        return last.getByte(addr);
                    }
                } else {
                    last = update;
                }
            }
        }

        if (last != null) {
            return last.getByte(addr);
        }

        return data[(int) (addr - address)];
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
}
