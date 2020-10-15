package org.graalvm.vm.trcview.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.graalvm.vm.trcview.analysis.type.Type;

public class TypedMemory {
    private NavigableMap<Long, Variable> types;

    public TypedMemory() {
        types = new TreeMap<>();
    }

    public Variable get(long addr) {
        Entry<Long, Variable> entry = types.floorEntry(addr);
        if (entry == null) {
            return null;
        }
        Variable var = entry.getValue();
        if (var.contains(addr)) {
            return var;
        } else {
            return null;
        }
    }

    public Variable getNext(long addr) {
        Entry<Long, Variable> next = types.ceilingEntry(addr);
        if (next != null) {
            return next.getValue();
        } else {
            return null;
        }
    }

    public void set(long addr, Type var) {
        if (var == null) {
            types.remove(addr);
        } else {
            types.put(addr, new Variable(addr, var));
        }
    }

    public void set(long addr, Type var, String name) {
        if (var == null) {
            types.remove(addr);
        } else {
            types.put(addr, new Variable(addr, var, name));
        }
    }

    public List<Variable> getTypes(long addrStart, long addrEnd) {
        List<Variable> result = new ArrayList<>();
        long addr = addrStart;
        while (addr <= addrEnd) {
            Entry<Long, Variable> next = types.ceilingEntry(addr);
            if (next == null) {
                break;
            }
            long size = next.getValue().getSize();
            assert size > 0;
            assert addr <= next.getKey();
            addr = next.getKey() + size;
            result.add(next.getValue());
        }
        return result;
    }
}
