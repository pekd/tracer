package org.graalvm.vm.trcview.analysis.memory;

import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.io.Node;

public abstract class Page {
    public static final int SIZE = 4096;

    protected final long address;
    protected final long firstPC;
    protected final long firstInstructionCount;
    protected final Node firstNode;
    protected final Protection firstProtection;

    private final NavigableMap<Long, String> names = new TreeMap<>();
    private final NavigableMap<Long, Protection> protection = new TreeMap<>();

    public Page(long address, long pc, long instructionCount, Node node, Protection prot) {
        assert (address & 0xFFF) == 0;
        this.address = address;
        this.firstPC = pc;
        this.firstInstructionCount = instructionCount;
        this.firstNode = node;
        this.firstProtection = prot;
        protection.put(instructionCount, prot);
    }

    public final long getAddress() {
        return address;
    }

    public final long getInitialPC() {
        return firstPC;
    }

    public final long getInitialInstruction() {
        return firstInstructionCount;
    }

    public final Node getInitialNode() {
        return firstNode;
    }

    public void setName(long step, String name) {
        names.put(step, name);
    }

    public void setProtection(long step, Protection prot) {
        protection.put(step, prot);
    }

    public String getName(long step) throws MemoryNotMappedException {
        if (step < firstInstructionCount) {
            throw new MemoryNotMappedException("memory not mapped in step " + step);
        }
        Entry<Long, String> name = names.floorEntry(step);
        if (name != null) {
            return name.getValue();
        } else {
            return null;
        }
    }

    public Protection getProtection(long step) throws MemoryNotMappedException {
        if (step < firstInstructionCount) {
            throw new MemoryNotMappedException("memory not mapped in step " + step);
        }
        Entry<Long, Protection> prot = protection.floorEntry(step);
        if (prot != null) {
            return prot.getValue();
        } else {
            return null;
        }
    }

    public abstract byte[] getData();

    public abstract void addUpdate(long addr, byte size, long value, long instructionCount, Node node, StepEvent step, boolean be);

    public abstract void addRead(long addr, byte size, long instructionCount, Node node, StepEvent step);

    public abstract void clear(long instructionCount, Node node, StepEvent step);

    public abstract void overwrite(byte[] update, long instructionCount, Node node, StepEvent step);

    public abstract byte getByte(long addr, long instructionCount) throws MemoryNotMappedException;

    public abstract long getWord(long addr, long instructionCount) throws MemoryNotMappedException;

    public abstract MemoryUpdate getLastUpdate(long addr, long instructionCount) throws MemoryNotMappedException;

    public abstract MemoryUpdate getNextUpdate(long addr, long instructionCount) throws MemoryNotMappedException;

    public abstract MemoryRead getLastRead(long addr, long instructionCount) throws MemoryNotMappedException;

    public abstract MemoryRead getNextRead(long addr, long instructionCount) throws MemoryNotMappedException;

    public abstract List<MemoryUpdate> getPreviousUpdates(long addr, long instructionCount, long max) throws MemoryNotMappedException;
}
