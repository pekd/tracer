package org.graalvm.vm.trcview.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.graalvm.vm.trcview.data.type.VariableType;

public class MemoryPageTypeMap {
    private final List<TypedMemoryCell>[] memory;

    @SuppressWarnings("unchecked")
    public MemoryPageTypeMap(int size) {
        memory = new ArrayList[size];
    }

    public void step(int offset, long step) {
        if (memory[offset] == null) {
            memory[offset] = new ArrayList<>();
        }
        memory[offset].add(new TypedMemoryCell(step));
    }

    public long getStep(int offset) {
        return last(offset).getStep();
    }

    private TypedMemoryCell last(int offset) {
        if (memory[offset] == null) {
            memory[offset] = new ArrayList<>();
        }
        if (memory[offset].isEmpty()) {
            TypedMemoryCell cell = new TypedMemoryCell(0);
            memory[offset].add(cell);
            return cell;
        } else {
            return memory[offset].get(memory[offset].size() - 1);
        }
    }

    private TypedMemoryCell getStep(int offset, long step, boolean write) {
        if (memory[offset] == null || memory[offset].isEmpty()) {
            if (write) {
                // allocate memory cell if it doesn't exist already
                if (memory[offset] == null) {
                    memory[offset] = new ArrayList<>();
                }
                TypedMemoryCell cell = new TypedMemoryCell(0);
                memory[offset].add(cell);
            } else {
                return new TypedMemoryCell(0);
            }
        }

        int idx = Collections.binarySearch(memory[offset], new TypedMemoryCell(step), (a, b) -> Long.compareUnsigned(a.getStep(), b.getStep()));
        if (idx >= 0) {
            return memory[offset].get(idx);
        } else {
            int off = ~idx - 1;
            if (off >= memory[offset].size() || off < 0) {
                return new TypedMemoryCell();
            } else {
                return memory[offset].get(off);
            }
        }
    }

    public void set(int offset, VariableType type) {
        last(offset).set(type);
    }

    public void constrain(int offset, VariableType type) {
        last(offset).constrain(type);
    }

    public void set(int offset, long type) {
        last(offset).set(type);
    }

    public void set(int offset, long step, long bits) {
        getStep(offset, step, true).set(bits);
    }

    public void constrain(int offset, long type) {
        last(offset).constrain(type);
    }

    public long get(int offset) {
        return last(offset).get();
    }

    public long get(int offset, long step) {
        return getStep(offset, step, false).get();
    }

    public Set<ChainTarget> getForwardChain(int offset, long step) {
        return memory[offset] == null || memory[offset].isEmpty() ? Collections.emptySet() : getStep(offset, step, false).getForwardChain();
    }

    public Set<ChainTarget> getReverseChain(int offset, long step) {
        return memory[offset] == null || memory[offset].isEmpty() ? Collections.emptySet() : getStep(offset, step, false).getReverseChain();
    }

    public void clear(int offset) {
        if (memory[offset] != null) {
            memory[offset].get(memory[offset].size() - 1).clear();
        }
    }

    public void forwardChain(int offset, ChainTarget target) {
        last(offset).forwardChain(target);
    }

    public void reverseChain(int offset, ChainTarget target) {
        last(offset).reverseChain(target);
    }

    public void setBit(int offset, long bit) {
        last(offset).setBit(bit);
    }

    public int[] getUsedOffsets() {
        int n = 0;
        int[] used = new int[memory.length];

        for (int i = 0; i < memory.length; i++) {
            if (memory[i] != null && !memory[i].isEmpty()) {
                used[n++] = i;
            }
        }

        if (n == 0) {
            return new int[0];
        } else {
            return Arrays.copyOf(used, n);
        }
    }
}
