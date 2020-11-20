package org.graalvm.vm.trcview.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.graalvm.vm.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.analysis.type.Representation;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public class TypedMemory {
    private static final long MAX_STRING_LENGTH = 32768; // 32k

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
            clean(addr, var.getSize());
            types.put(addr, new Variable(addr, var));
        }
    }

    public void set(long addr, Type var, String name) {
        if (var == null) {
            types.remove(addr);
        } else {
            clean(addr, var.getSize());
            types.put(addr, new Variable(addr, var, name));
        }
    }

    private void clean(long addr, long size) {
        Entry<Long, Variable> var = types.floorEntry(addr);
        if (var != null) {
            if (var.getValue().contains(addr)) {
                types.remove(var.getKey());
            }
        }

        for (long ptr = addr; ptr < addr + size;) {
            var = types.ceilingEntry(ptr);
            if (var == null) {
                break;
            }
            Variable v = var.getValue();
            if (v.getAddress() >= addr + size) {
                break;
            }
            types.remove(v.getAddress());
            ptr = v.getAddress();
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

    public Type findString(long addr, long step, TraceAnalyzer trc) {
        long ptr = addr;
        try {
            // search end of string
            while (trc.getI8(ptr++, step) != 0) {
                if ((ptr - addr) > MAX_STRING_LENGTH) {
                    break;
                }
            }
            long len = ptr - addr;
            return new Type(DataType.S8, false, (int) len, Representation.CHAR);
        } catch (MemoryNotMappedException e) {
            long len = ptr - addr;
            if (len > 0) {
                return new Type(DataType.S8, false, (int) len, Representation.CHAR);
            } else {
                return null;
            }
        }
    }
}
