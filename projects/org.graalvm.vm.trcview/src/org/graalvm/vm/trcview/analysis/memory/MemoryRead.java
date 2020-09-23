package org.graalvm.vm.trcview.analysis.memory;

import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.io.Node;

public class MemoryRead {
    public final long address;
    public final byte size;
    public final long instructionCount;
    public final Node node;
    public final StepEvent step;

    public MemoryRead(long address, byte size, long instructionCount, Node node, StepEvent step) {
        this.address = address;
        this.size = size;
        this.instructionCount = instructionCount;
        this.node = node;
        this.step = step;
    }

    public boolean contains(long addr) {
        return address <= addr && address + size > addr;
    }

    @Override
    public String toString() {
        return String.format("[0x%08x] %d bytes @ #%d", address, size, instructionCount);
    }
}
