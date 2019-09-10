package org.graalvm.vm.x86.trcview.analysis.memory;

import org.graalvm.vm.x86.trcview.io.Node;

public class MemoryUpdate {
    public final long address;
    public final byte size;
    public final long value;
    public final long pc;
    public final long instructionCount;
    public final Node node;

    public MemoryUpdate(long address, byte size, long value, long pc, long instructionCount, Node node) {
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
        return (byte) (value >> (off * 8));
    }

    public boolean contains(long addr) {
        return address <= addr && address + size > addr;
    }

    @Override
    public String toString() {
        return String.format("[0x%08x] = 0x%x [%d bytes] @ PC=0x%x/%d", address, value, size, pc, instructionCount);
    }
}
