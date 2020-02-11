package org.graalvm.vm.trcview.analysis.memory;

import org.graalvm.vm.trcview.io.Node;

public interface Page {
    long getAddress();

    byte[] getData();

    void addUpdate(long addr, byte size, long value, long instructionCount, Node node, Node step, boolean be);

    void addRead(long addr, byte size, long instructionCount, Node node, Node step);

    void clear(long instructionCount, Node node, Node step);

    void overwrite(byte[] update, long instructionCount, Node node, Node step);

    byte getByte(long addr, long instructionCount) throws MemoryNotMappedException;

    long getWord(long addr, long instructionCount) throws MemoryNotMappedException;

    MemoryUpdate getLastUpdate(long addr, long instructionCount) throws MemoryNotMappedException;

    MemoryRead getLastRead(long addr, long instructionCount) throws MemoryNotMappedException;

    MemoryRead getNextRead(long addr, long instructionCount) throws MemoryNotMappedException;

    long getInitialPC();

    long getInitialInstruction();

    Node getInitialNode();
}
