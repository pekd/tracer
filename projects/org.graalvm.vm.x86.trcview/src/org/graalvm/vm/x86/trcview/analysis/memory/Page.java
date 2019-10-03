package org.graalvm.vm.x86.trcview.analysis.memory;

import org.graalvm.vm.x86.trcview.io.Node;

public interface Page {
    long getAddress();

    byte[] getData();

    void addUpdate(long addr, byte size, long value, long pc, long instructionCount, Node node);

    void addRead(long addr, byte size, long pc, long instructionCount, Node node);

    void clear(long pc, long instructionCount, Node node);

    void overwrite(byte[] update, long pc, long instructionCount, Node node);

    byte getByte(long addr, long instructionCount) throws MemoryNotMappedException;

    long getWord(long addr, long instructionCount) throws MemoryNotMappedException;

    MemoryUpdate getLastUpdate(long addr, long instructionCount) throws MemoryNotMappedException;

    MemoryRead getLastRead(long addr, long instructionCount) throws MemoryNotMappedException;

    MemoryRead getNextRead(long addr, long instructionCount) throws MemoryNotMappedException;

    long getInitialPC();

    long getInitialInstruction();

    Node getInitialNode();
}
