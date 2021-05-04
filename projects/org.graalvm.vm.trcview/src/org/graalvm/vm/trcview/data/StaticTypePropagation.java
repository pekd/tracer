package org.graalvm.vm.trcview.data;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.graalvm.vm.trcview.analysis.ComputedSymbol;
import org.graalvm.vm.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.trcview.analysis.type.ArchitectureTypeInfo;
import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.analysis.type.Field;
import org.graalvm.vm.trcview.analysis.type.Prototype;
import org.graalvm.vm.trcview.analysis.type.Struct;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.decode.CallDecoder;
import org.graalvm.vm.trcview.io.BlockNode;
import org.graalvm.vm.trcview.io.Node;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public class StaticTypePropagation {
    private final TraceAnalyzer trc;
    private final ArchitectureTypeInfo info;
    private final CallDecoder call;

    public StaticTypePropagation(TraceAnalyzer trc) {
        this.trc = trc;
        this.info = trc.getTypeDatabase().getTypeInfo();
        this.call = trc.getArchitecture().getCallDecoder();
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

    public long getPointer(long address) {
        switch (info.getPointerSize()) {
            case 1:
                return Byte.toUnsignedLong((byte) address);
            case 2:
                return Short.toUnsignedLong((short) address);
            case 4:
                return Integer.toUnsignedLong((int) address);
            case 8:
                return address;
            default:
                throw new AssertionError("invalid pointer size: " + info.getPointerSize());
        }
    }

    private void setString(Type pointee, long value, long step) {
        TypedMemory mem = trc.getTypedMemory();

        Type string = mem.findString(value, step, trc);

        // add derived type
        if (string != null) {
            // found a string
            mem.setDerivedType(value, string);
        } else {
            // cannot find a string, use original type instead
            mem.setDerivedType(value, pointee);
        }
    }

    public void propagate(long step) {
        TypedMemory mem = trc.getTypedMemory();
        // mem.clearDerivedTypes();

        Deque<Variable> vars = new LinkedList<>();
        vars.addAll(mem.getAllTypes());

        propagate(step, vars);
    }

    private void guess(long step, long addr) {
        TypedMemory mem = trc.getTypedMemory();

        // is this a string?
        Type str = mem.guessString(addr, step, trc);
        if (str != null) {
            mem.setDerivedType(addr, str);
        }
    }

    private void propagate(long step, Deque<Variable> vars) {
        TypedMemory mem = trc.getTypedMemory();
        Set<Long> visited = new HashSet<>();

        while (!vars.isEmpty()) {
            Variable var = vars.remove();

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
                            setString(pointee, val, step);
                        } else if (pointee.getType() != DataType.VOID) {
                            // add derived type if we are not following a void*
                            // TODO: add some kind of auto analysis
                            Variable v = mem.setDerivedType(val, pointee);
                            // also add it to the processing queue so we can follow pointers
                            vars.add(v);
                        } else if (pointee.getType() == DataType.VOID) {
                            // auto analysis
                            guess(step, val);
                        }
                    } else if (pointee.getType() == DataType.VOID && type.getType() != null) {
                        // we are a void*, but there is type information at the target
                        // TODO: check if this is derived type information
                        // Type ptr = new Type(type.getType().getElementType(), info);
                        // String name = var.getRawName();
                        // mem.set(addr, ptr, name); // TODO: this should be derived info
                        // mem.setDerivedType(addr, ptr);
                    }
                } catch (MemoryNotMappedException e) {
                    // nothing
                }
            } else if (var.getType().getType() == DataType.STRUCT) {
                // recursively follow all fields
                Struct struct = var.getType().getStruct();
                for (Field field : struct.getFields()) {
                    long addr = var.getAddress() + field.getOffset();
                    Type type = field.getType();
                    if (type.getType() == DataType.STRUCT || type.getType() == DataType.PTR) {
                        if (!visited.contains(addr)) {
                            vars.add(new Variable(addr, type));
                        }
                    }
                }
            } else { // scalar value
                long addr = var.getAddress();
                // check if this location is typed
                Variable type = mem.get(addr);
                try {
                    if (type == null) {
                        // not typed
                        // now try to access this location
                        // TODO: only check if mapped, don't read value
                        trc.getI8(addr, step);

                        // is this a string?
                        if (var.getType().getType() == DataType.S8 || var.getType().getType() == DataType.U8) {
                            // find string
                            setString(var.getType(), addr, step);
                        } else if (var.getType().getType() != DataType.VOID) {
                            // add derived type if we are not following a void*
                            // TODO: add some kind of auto analysis
                            mem.setDerivedType(addr, var.getType());
                        }
                    }
                } catch (MemoryNotMappedException e) {
                    // nothing
                }
            }
        }
    }

    public void propagate(Prototype proto, CpuState state) {
        int id = 0;
        long step = state.getStep();
        Deque<Variable> vars = new LinkedList<>();

        for (Type type : proto.args) {
            if (type.getType() == DataType.PTR) {
                // this is a pointer, follow it
                Type pointee = type.getPointee();
                long value = getPointer(call.getArgument(state, id, proto, trc));
                if (pointee.getType() != DataType.VOID) {
                    vars.add(new Variable(value, pointee));
                }
            }
            id++;
        }

        propagate(step, vars);
    }

    public StepEvent getStep(Node node) {
        if (node instanceof StepEvent) {
            return (StepEvent) node;
        } else if (node instanceof BlockNode) {
            BlockNode block = (BlockNode) node;
            return block.getHead();
        } else {
            throw new IllegalArgumentException("Not a StepEvent or BlockNode");
        }
    }

    public void propagateTypes() {
        // clear existing type information
        trc.getTypedMemory().clearDerivedTypes();

        // propagate the call arguments
        Set<Long> steps = new HashSet<>();
        for (ComputedSymbol sym : trc.getSymbols()) {
            if (sym.prototype != null) {
                for (Node visit : sym.visits) {
                    StepEvent step = getStep(visit);
                    steps.add(step.getStep());
                    propagate(sym.prototype, step.getState());
                }
            }
        }

        // now propagate everything
        // TODO: improve efficiency
        for (long step : steps) {
            propagate(step);
        }
    }
}
