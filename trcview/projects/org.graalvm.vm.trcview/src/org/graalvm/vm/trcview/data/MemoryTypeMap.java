package org.graalvm.vm.trcview.data;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import org.graalvm.vm.trcview.data.ir.MemoryOperand;
import org.graalvm.vm.trcview.data.type.VariableType;

public class MemoryTypeMap {
    private final NavigableMap<Long, MemoryPageTypeMap> pages = new TreeMap<>();

    private MemoryPageTypeMap getPage(long pc) {
        long addr = getBase(pc);
        return pages.get(addr);
    }

    private static long getBase(long addr) {
        return addr & ~4095L;
    }

    private static int getOffset(long addr) {
        return (int) (addr & 4095);
    }

    private MemoryPageTypeMap getMap(long addr) {
        MemoryPageTypeMap map = getPage(addr);
        if (map == null) {
            map = new MemoryPageTypeMap(4096);
            pages.put(getBase(addr), map);
        }
        return map;
    }

    public void step(MemoryOperand op, long step) {
        step(op.getAddress(), step);
    }

    public void step(long addr, long step) {
        getMap(addr).step(getOffset(addr), step);
    }

    public long getStep(long addr) {
        return getMap(addr).getStep(getOffset(addr));
    }

    public long get(MemoryOperand op) {
        return get(op.getAddress());
    }

    public long get(long addr) {
        return getMap(addr).get(getOffset(addr));
    }

    public long get(long addr, long step) {
        return getMap(addr).get(getOffset(addr), step);
    }

    public void set(MemoryOperand addr, VariableType type) {
        set(addr.getAddress(), type.getMask());
    }

    public void set(MemoryOperand addr, long bits) {
        set(addr.getAddress(), bits);
    }

    public void set(long addr, long bits) {
        getMap(addr).set(getOffset(addr), bits);
    }

    public void set(long addr, long step, long bits) {
        getMap(addr).set(getOffset(addr), step, bits);
    }

    public void constrain(MemoryOperand addr, VariableType type) {
        constrain(addr.getAddress(), type.getMask());
    }

    public void constrain(MemoryOperand addr, long bits) {
        constrain(addr.getAddress(), bits);
    }

    public void constrain(long addr, long bits) {
        getMap(addr).constrain(getOffset(addr), bits);
    }

    public Set<ChainTarget> getReverseChain(long addr, long step) {
        return getMap(addr).getReverseChain(getOffset(addr), step);
    }

    public Set<ChainTarget> getForwardChain(long addr, long step) {
        return getMap(addr).getForwardChain(getOffset(addr), step);
    }

    public void forwardChain(long addr, ChainTarget target) {
        getMap(addr).forwardChain(getOffset(addr), target);
    }

    public void reverseChain(long addr, ChainTarget target) {
        getMap(addr).reverseChain(getOffset(addr), target);
    }

    public void setBit(MemoryOperand op, long bit) {
        setBit(op.getAddress(), bit);
    }

    public void setBit(long addr, long bit) {
        getMap(addr).setBit(getOffset(addr), bit);
    }

    public Set<Long> getUsedAddresses() {
        Set<Long> result = new HashSet<>();
        for (Entry<Long, MemoryPageTypeMap> entry : pages.entrySet()) {
            int[] offsets = entry.getValue().getUsedOffsets();
            long base = entry.getKey();
            for (int offset : offsets) {
                result.add(base + offset);
            }
        }
        return result;
    }
}
