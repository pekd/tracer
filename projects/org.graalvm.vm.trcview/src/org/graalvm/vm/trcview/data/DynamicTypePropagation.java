package org.graalvm.vm.trcview.data;

import org.graalvm.vm.trcview.analysis.SymbolTable;
import org.graalvm.vm.trcview.analysis.type.ArchitectureTypeInfo;
import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.decode.CallDecoder;

public class DynamicTypePropagation {
    private final ArchitectureTypeInfo info;
    private final CallDecoder call;
    private final Semantics semantics;

    private long last;

    public DynamicTypePropagation(Architecture arch, ArchitectureTypeInfo info, SymbolTable symbols) {
        this.info = info;
        this.call = arch.getCallDecoder();
        CodeTypeMap codeMap = new CodeTypeMap(arch.getRegisterCount());
        MemoryTypeMap memoryMap = new MemoryTypeMap();
        this.semantics = new Semantics(codeMap, memoryMap, symbols);
        last = -1;
    }

    public void step(StepEvent event) {
        semantics.setPC(event.getPC());
        semantics.setStepEvent(event);

        // chain registers?
        if (last != -1) {
            semantics.chain(last);
        }
        last = event.getPC();

        event.getSemantics(semantics);
    }

    public void finish() {
        semantics.finish();
        System.out.println("DynamicTypePropagation result: " + semantics.getStatistics());
    }

    public Semantics getSemantics() {
        return semantics;
    }
}
