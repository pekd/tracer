package org.graalvm.vm.trcview.data;

import org.graalvm.vm.trcview.analysis.SymbolTable;
import org.graalvm.vm.trcview.analysis.type.ArchitectureTypeInfo;
import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.analysis.type.DefaultTypes;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.data.type.VariableType;
import org.graalvm.vm.trcview.decode.CallDecoder;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public class DynamicTypePropagation {
    private final ArchitectureTypeInfo info;
    private final CallDecoder call;
    private final Semantics semantics;

    private final MemoryAccessMap memory;

    private long last;
    private long laststep;

    public DynamicTypePropagation(Architecture arch, ArchitectureTypeInfo info, SymbolTable symbols) {
        this.info = info;
        this.call = arch.getCallDecoder();
        CodeTypeMap codeMap = new CodeTypeMap(arch.getRegisterCount());
        MemoryTypeMap memoryMap = new MemoryTypeMap();
        memory = new MemoryAccessMap();
        this.semantics = new Semantics(codeMap, memoryMap, symbols, memory);
        last = -1;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void step(StepEvent event, CpuState state) {
        long pc = state.getPC();
        semantics.setPC(pc);
        semantics.setState(state);

        laststep = event.getStep();

        // chain registers?
        if (last != -1) {
            semantics.chain(last);
        }
        last = pc;

        event.getSemantics(semantics);

        memory.access(pc, event);
    }

    public void finish() {
        semantics.finish();
        System.out.println("DynamicTypePropagation result: " + semantics.getStatistics());
    }

    public Semantics getSemantics() {
        return semantics;
    }

    // transfer recovered data to typed memory model
    public void transfer(TraceAnalyzer trc) {
        TypedMemory mem = trc.getTypedMemory();

        // transfer final data types in memory
        for (long addr : semantics.getUsedAddresses()) {
            long bits = semantics.resolveMemory(addr, laststep);
            VariableType vartype = VariableType.resolve(bits, info.getPointerSize());
            if (vartype != null && !VariableType.UNKNOWN.equals(vartype) && !VariableType.CONFLICT.equals(vartype)) {
                Type type = vartype.toType(info);
                if (type != null) {
                    mem.setRecoveredType(addr, type);
                }
            }
        }

        // update code fields
        loop: for (StepEvent evt : memory.getCode()) {
            long pc = evt.getPC();

            mem.setRecoveredType(pc, DefaultTypes.getCodeType(evt.getMachinecode().length));

            ArrayInfo array = ArrayStructRecovery.recoverArray(semantics, pc, true);
            if (array != null) {
                long[] addresses = array.getAddresses();
                if (addresses.length == 0) {
                    continue;
                }

                Type lasttype = null;
                for (long addr : addresses) {
                    Variable var = mem.getRecoveredType(addr);
                    if (var != null && var.getAddress() == addr) {
                        if (lasttype != null && !lasttype.equals(var.getType())) {
                            System.out.println("array inconsistency at " + trc.getArchitecture().getFormat().formatShortAddress(addr));
                            continue loop;
                        }
                        lasttype = var.getType();
                    }
                }

                if (lasttype == null) {
                    switch (array.getElementSize()) {
                        case 1:
                            lasttype = new Type(DataType.S8);
                            break;
                        case 2:
                            lasttype = new Type(DataType.S16);
                            break;
                        case 4:
                            lasttype = new Type(DataType.S32);
                            break;
                        case 8:
                            lasttype = new Type(DataType.S64);
                            break;
                    }
                }

                if (lasttype == null) {
                    continue loop;
                }

                if (lasttype.getSize() != array.getElementSize()) {
                    continue loop;
                }

                int lastidx = 0;
                for (int i = 0; i < addresses.length - 1; i++) {
                    if (addresses[i + 1] - addresses[i] != array.getElementSize()) {
                        // split here
                        if (lastidx != -1) {
                            defineArray(mem, lasttype, addresses[lastidx], addresses[i]);
                        }
                        lastidx = i + 1;
                    }
                }
                if (lastidx != -1 && lastidx != addresses.length - 1) {
                    defineArray(mem, lasttype, addresses[lastidx], addresses[addresses.length - 1]);
                }
            }
        }
    }

    private static void defineArray(TypedMemory mem, Type type, long first, long last) {
        System.out.printf("defining array at %x-%x\n", first, last);
        int elements = (int) ((last - first) / type.getSize()) + 1;
        Type t = new Type(type.getType(), false, elements, type.getRepresentation());
        mem.setRecoveredType(first, t);
    }
}
