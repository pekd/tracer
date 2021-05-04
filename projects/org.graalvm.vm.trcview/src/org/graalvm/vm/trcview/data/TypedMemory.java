package org.graalvm.vm.trcview.data;

import java.util.ArrayList;
import java.util.Collections;
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
    private NavigableMap<Long, Variable> derivedTypes;

    public TypedMemory() {
        types = new TreeMap<>();
        derivedTypes = new TreeMap<>();
    }

    public Variable get(long addr) {
        Variable var = get(addr, types);
        if (var != null) {
            return var;
        } else {
            return get(addr, derivedTypes);
        }
    }

    private static Variable get(long addr, NavigableMap<Long, Variable> map) {
        Entry<Long, Variable> entry = map.floorEntry(addr);
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

    public Variable setDerivedType(long addr, Type var) {
        if (var == null) {
            derivedTypes.remove(addr);
            return null;
        } else {
            Variable v = new Variable(addr, var);
            clean(addr, var.getSize(), derivedTypes);
            derivedTypes.put(addr, v);
            return v;
        }
    }

    public void clearDerivedTypes() {
        derivedTypes.clear();
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
        clean(addr, size, types);
        clean(addr, size, derivedTypes);
    }

    private static void clean(long addr, long size, NavigableMap<Long, Variable> map) {
        Entry<Long, Variable> var = map.floorEntry(addr);
        if (var != null) {
            if (var.getValue().contains(addr)) {
                map.remove(var.getKey());
            }
        }

        for (long ptr = addr; ptr < addr + size;) {
            var = map.ceilingEntry(ptr);
            if (var == null) {
                break;
            }
            Variable v = var.getValue();
            if (v.getAddress() >= addr + size) {
                break;
            }
            map.remove(v.getAddress());
            ptr = v.getAddress();
        }
    }

    public List<Variable> getTypes() {
        return new ArrayList<>(types.values());
    }

    public List<Variable> getDerivedTypes() {
        return new ArrayList<>(derivedTypes.values());
    }

    public List<Variable> getAllTypes() {
        List<Variable> result = new ArrayList<>(types.values());
        result.addAll(derivedTypes.values());
        return result;
    }

    public List<Variable> getTypes(long addrStart, long addrEnd) {
        List<Variable> result = new ArrayList<>();

        // known types
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

        // derived types
        addr = addrStart;
        while (addr <= addrEnd) {
            Entry<Long, Variable> next = derivedTypes.ceilingEntry(addr);
            if (next == null) {
                break;
            }
            long size = next.getValue().getSize();
            assert size > 0;
            assert addr <= next.getKey();
            addr = next.getKey() + size;
            result.add(next.getValue());
        }

        // sort result
        Collections.sort(result, (a, b) -> Long.compareUnsigned(a.getAddress(), b.getAddress()));

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

    private static boolean isASCII(byte val) {
        if (val < 0) {
            return false;
        }
        if (val < 32) {
            return val == '\r' || val == '\n' || val == '\t' || val == 0x1B;
        }
        return true;
    }

    public Type guessString(long addr, long step, TraceAnalyzer trc) {
        long ptr = addr;
        try {
            // search end of string
            while (true) {
                byte val = trc.getI8(ptr++, step);
                if (val == 0) {
                    break;
                } else if ((ptr - addr) > MAX_STRING_LENGTH) {
                    break;
                } else if (!isASCII(val)) {
                    return null;
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
