package org.graalvm.vm.trcview.data;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.graalvm.vm.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.trcview.analysis.type.ArchitectureTypeInfo;
import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public class StaticTypePropagation {
    private final TraceAnalyzer trc;
    private final ArchitectureTypeInfo info;

    public StaticTypePropagation(TraceAnalyzer trc) {
        this.trc = trc;
        this.info = trc.getTypeDatabase().getTypeInfo();
    }

    public long getPointer(long address, long step) throws MemoryNotMappedException {
        switch (info.getPointerSize()) {
            case 1:
                return Byte.toUnsignedLong(trc.getI8(address, step));
            case 2:
                return Short.toUnsignedLong(trc.getI16(address, step));
            case 4:
                return Integer.toUnsignedLong(trc.getI32(address, step));
            case 8:
                return trc.getI64(address, step);
            default:
                throw new AssertionError("invalid pointer size: " + info.getPointerSize());
        }
    }

    public void propagate(long step) {
        TypedMemory mem = trc.getTypedMemory();
        mem.clearDerivedTypes();

        Set<Long> visited = new HashSet<>();
        Deque<Variable> vars = new LinkedList<>();
        vars.addAll(mem.getTypes());

        while (!vars.isEmpty()) {
            Variable var = vars.remove();
            System.out.printf("processing: 0x%x [%s]\n", var.getAddress(), var.getName());

            if (visited.contains(var.getAddress())) {
                // already processed
                continue;
            } else {
                // not yet processed -> mark it
                visited.add(var.getAddress());
            }

            if (var.getType().getType() == DataType.PTR) {
                Type pointee = var.getType().getPointee();
                try {
                    // resolve pointer
                    long addr = var.getAddress();
                    long val = getPointer(addr, step);

                    // check if this location is typed
                    Variable type = mem.get(val);
                    if (type == null) {
                        // not typed
                        // now try to access this location
                        // TODO: only check if mapped, don't read value
                        trc.getI8(val, step);

                        // is this a string?
                        if (pointee.getType() == DataType.S8 || pointee.getType() == DataType.U8) {
                            // find string
                            Type string = mem.findString(val, step, trc);

                            // add derived type
                            mem.setDerivedType(val, string);
                        } else if (pointee.getType() != DataType.VOID) {
                            // add derived type if we are not following a void*
                            // TODO: add some kind of auto analysis
                            Variable v = mem.setDerivedType(val, pointee);
                            // also add it to the processing queue so we can follow pointers
                            vars.add(v);
                        }
                    } else if (pointee.getType() == DataType.VOID && type.getType() != null) {
                        // we are a void*, but there is type information at the target
                        // TODO: check if this is derived type information
                        Type ptr = new Type(type.getType().getElementType(), info);
                        String name = var.getRawName();
                        mem.set(addr, ptr, name); // TODO: this should be derived info
                    }
                } catch (MemoryNotMappedException e) {
                    // nothing
                }
            }
        }
    }
}
