package org.graalvm.vm.trcview.data;

import org.graalvm.vm.trcview.analysis.SymbolTable;
import org.graalvm.vm.trcview.analysis.type.ArchitectureTypeInfo;
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

        // update code fields
        for (StepEvent evt : memory.getCode()) {
            mem.setDerivedType(evt.getPC(), DefaultTypes.getCodeType(evt.getMachinecode().length));
        }

        // transfer final data types in memory
        for (long addr : semantics.getUsedAddresses()) {
            long bits = semantics.getMemory(addr, laststep);
            VariableType vartype = VariableType.resolve(bits, info.getPointerSize());
            if (vartype != null && !VariableType.UNKNOWN.equals(vartype) && !VariableType.CONFLICT.equals(vartype)) {
                Type type = vartype.toType(info);
                if (type != null) {
                    mem.setDerivedType(addr, type);
                }
            }
        }
    }
}
