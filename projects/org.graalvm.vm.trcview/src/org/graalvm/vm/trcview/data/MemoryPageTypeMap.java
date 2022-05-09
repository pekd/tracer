package org.graalvm.vm.trcview.data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.graalvm.vm.trcview.data.type.VariableType;

public class MemoryPageTypeMap {
    private final long[] memory;
    private final Set<ChainTarget>[] reverseChain;
    private final Set<ChainTarget>[] forwardChain;

    @SuppressWarnings("unchecked")
    public MemoryPageTypeMap(int size) {
        memory = new long[size];
        for (int i = 0; i < memory.length; i++) {
            memory[i] = 0;
        }
        reverseChain = new Set[size];
        forwardChain = new Set[size];
    }

    public void set(int offset, VariableType type) {
        memory[offset] = type.getMask();
    }

    public void constrain(int offset, VariableType type) {
        memory[offset] |= type.getMask();
    }

    public void set(int offset, long type) {
        memory[offset] = type;
    }

    public void constrain(int offset, long type) {
        memory[offset] |= type;
    }

    public long get(int offset) {
        return memory[offset];
    }

    public Set<ChainTarget> getForwardChain(int offset) {
        return forwardChain[offset] == null ? Collections.emptySet() : forwardChain[offset];
    }

    public Set<ChainTarget> getReverseChain(int offset) {
        return reverseChain[offset] == null ? Collections.emptySet() : reverseChain[offset];
    }

    public void clear(int offset) {
        memory[offset] = 0;
    }

    public void forwardChain(int offset, ChainTarget target) {
        if (forwardChain[offset] == null) {
            forwardChain[offset] = new HashSet<>();
        }
        forwardChain[offset].add(target);
    }

    public void reverseChain(int offset, ChainTarget target) {
        if (reverseChain[offset] == null) {
            reverseChain[offset] = new HashSet<>();
        }
        reverseChain[offset].add(target);
    }

    public void setBit(int offset, long bit) {
        memory[offset] |= bit;
    }
}
