package org.graalvm.vm.trcview.data;

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

    public long get(MemoryOperand op) {
        return get(op.getAddress());
    }

    public long get(long addr) {
        return getMap(addr).get(getOffset(addr));
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

    public void constrain(MemoryOperand addr, VariableType type) {
        constrain(addr.getAddress(), type.getMask());
    }

    public void constrain(MemoryOperand addr, long bits) {
        constrain(addr.getAddress(), bits);
    }

    public void constrain(long addr, long bits) {
        getMap(addr).constrain(getOffset(addr), bits);
    }

    public Set<ChainTarget> getReverseChain(long addr) {
        return getMap(addr).getReverseChain(getOffset(addr));
    }

    public Set<ChainTarget> getForwardChain(long addr) {
        return getMap(addr).getForwardChain(getOffset(addr));
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
}
