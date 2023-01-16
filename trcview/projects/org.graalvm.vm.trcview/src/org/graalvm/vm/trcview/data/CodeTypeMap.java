package org.graalvm.vm.trcview.data;

import java.util.BitSet;
import java.util.Collections;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import org.graalvm.vm.trcview.data.ir.RegisterOperand;
import org.graalvm.vm.trcview.data.type.VariableType;

public class CodeTypeMap {
    private final NavigableMap<Long, CodePageTypeMap> pages = new TreeMap<>();
    private final int size;

    public CodeTypeMap(int size) {
        this.size = size;
    }

    private CodePageTypeMap getPage(long pc) {
        long addr = getBase(pc);
        return pages.get(addr);
    }

    private static long getBase(long pc) {
        return pc & ~4095L;
    }

    private static int getOffset(long pc) {
        return (int) (pc & 4095);
    }

    private CodePageTypeMap getMap(long pc) {
        CodePageTypeMap map = getPage(pc);
        if (map == null) {
            map = new CodePageTypeMap(4096, size, getBase(pc));
            pages.put(getBase(pc), map);
        }
        return map;
    }

    public void setLive(long pc, int reg, boolean value) {
        CodePageTypeMap map = getMap(pc);
        int offset = getOffset(pc);
        map.setLive(offset, reg, value);
    }

    public boolean isLive(long pc, int reg) {
        CodePageTypeMap map = getMap(pc);
        int offset = getOffset(pc);
        return map.isLive(offset, reg);
    }

    public void setResolvedType(long pc, int reg, long type) {
        CodePageTypeMap map = getMap(pc);
        int offset = getOffset(pc);
        map.setResolvedType(offset, reg, type);
    }

    public RegisterTypeMap get(long pc) {
        return getMap(pc).getMap(getOffset(pc));
    }

    public long getChain(long pc) {
        CodePageTypeMap map = getPage(pc);
        int offset = getOffset(pc);
        if (map == null) {
            return -1;
        } else {
            return map.getChain(offset);
        }
    }

    public Set<RegisterTypeMap> getExtraChain(long pc) {
        CodePageTypeMap map = getPage(pc);
        int offset = getOffset(pc);
        if (map == null) {
            return Collections.emptySet();
        } else {
            return map.getExtraChain(offset);
        }
    }

    public Set<RegisterTypeMap> getForwardChain(long pc) {
        CodePageTypeMap map = getPage(pc);
        int offset = getOffset(pc);
        if (map == null) {
            return Collections.emptySet();
        } else {
            return map.getForwardChain(offset);
        }
    }

    public long get(long pc, RegisterOperand op) {
        CodePageTypeMap map = getPage(pc);
        int offset = getOffset(pc);
        if (map == null) {
            return 0;
        } else {
            return map.get(offset, op);
        }
    }

    public long getDirect(long pc, RegisterOperand op) {
        CodePageTypeMap map = getPage(pc);
        int offset = getOffset(pc);
        if (map == null) {
            return 0;
        } else {
            return map.getDirect(offset, op);
        }
    }

    public void set(long pc, RegisterOperand op, VariableType type) {
        CodePageTypeMap map = getMap(pc);
        int offset = getOffset(pc);
        map.set(offset, op, type);
    }

    public void constrain(long pc, RegisterOperand op, VariableType type) {
        CodePageTypeMap map = getMap(pc);
        int offset = getOffset(pc);
        map.constrain(offset, op, type);
    }

    public void set(long pc, RegisterOperand op, long type) {
        CodePageTypeMap map = getMap(pc);
        int offset = getOffset(pc);
        map.set(offset, op, type);
    }

    public void constrain(long pc, RegisterOperand op, long type) {
        CodePageTypeMap map = getMap(pc);
        int offset = getOffset(pc);
        map.constrain(offset, op, type);
    }

    public void clear(long pc) {
        CodePageTypeMap map = getMap(pc);
        int offset = getOffset(pc);
        map.clear(offset);
    }

    public void chain(long pc, long last) {
        CodePageTypeMap map = getMap(pc);
        CodePageTypeMap lastMap = getMap(last);
        int offset = getOffset(pc);

        if (map.useOptimizedChain(offset, last)) {
            // we can use the optimized PC + chain bit based chain
            map.chain(offset, last);
            lastMap.forwardChain(getOffset(last), map.getMap(offset));
        } else {
            // use the more complex chain method with an object
            map.chain(offset, lastMap.getMap(getOffset(last)));
            lastMap.forwardChain(getOffset(last), map.getMap(offset));
        }
    }

    public void chain(long pc, RegisterOperand a, RegisterOperand b) {
        CodePageTypeMap map = getMap(pc);
        map.forwardChain(getOffset(pc), a, getChainTarget(pc, b));
        map.forwardChain(getOffset(pc), b, getChainTarget(pc, a));
    }

    public void chain(long pc, RegisterOperand a, ChainTarget tgt) {
        CodePageTypeMap map = getMap(pc);
        map.forwardChain(getOffset(pc), a, tgt);
    }

    public ChainTarget getChainTarget(long pc, RegisterOperand op) {
        return new RegisterChainTarget(get(pc), op.getRegister());
    }

    public void breakChain(long addr, int register) {
        getMap(addr).breakChain(getOffset(addr), register);
    }

    public void breakChain(long addr, BitSet registers) {
        getMap(addr).breakChain(getOffset(addr), registers);
    }

    public void setBit(long addr, RegisterOperand op, long bit) {
        getMap(addr).setBit(getOffset(addr), op, bit);
    }

    public String getStatistics() {
        return "CodeTypeMap has " + pages.size() + " pages for " + size + " registers";
    }
}
