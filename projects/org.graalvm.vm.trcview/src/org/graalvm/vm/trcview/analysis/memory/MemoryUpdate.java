package org.graalvm.vm.trcview.analysis.memory;

import org.graalvm.vm.trcview.io.Node;

public class MemoryUpdate {
    public final boolean be;
    public final long address;
    public final byte size;
    public final long value;
    public final long pc;
    public final long instructionCount;
    public final Node node;

    public MemoryUpdate(boolean be, long address, byte size, long value, long pc, long instructionCount, Node node) {
        this.be = be;
        this.address = address;
        this.size = size;
        this.value = value;
        this.pc = pc;
        this.instructionCount = instructionCount;
        this.node = node;
    }

    public byte getByte(long addr) {
        assert addr >= address && addr < address + size;
        int off = (int) (addr - address);
        if (size == 1) {
            return (byte) value;
        } else if (be) {
            return (byte) (value >> ((size - 1 - off) * 8));
        } else {
            return (byte) (value >> (off * 8));
        }
    }

    public boolean contains(long addr) {
        return address <= addr && address + size > addr;
    }

    @Override
    public String toString() {
        return String.format("[0x%08x] = 0x%x [%d bytes] @ PC=0x%x/%d", address, value, size, pc, instructionCount);
    }
}
