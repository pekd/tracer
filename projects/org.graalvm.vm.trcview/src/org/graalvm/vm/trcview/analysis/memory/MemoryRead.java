package org.graalvm.vm.trcview.analysis.memory;

import org.graalvm.vm.trcview.io.Node;

public class MemoryRead {
    public final long address;
    public final byte size;
    public final long pc;
    public final long instructionCount;
    public final Node node;

    public MemoryRead(long address, byte size, long pc, long instructionCount, Node node) {
        this.address = address;
        this.size = size;
        this.pc = pc;
        this.instructionCount = instructionCount;
        this.node = node;
    }

    public boolean contains(long addr) {
        return address <= addr && address + size > addr;
    }

    @Override
    public String toString() {
        return String.format("[0x%08x] %d bytes @ PC=0x%x/%d", address, size, pc, instructionCount);
    }
}
