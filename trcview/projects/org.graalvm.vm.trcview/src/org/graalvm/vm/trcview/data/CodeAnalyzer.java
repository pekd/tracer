package org.graalvm.vm.trcview.data;

import java.util.HashSet;
import java.util.Set;

import org.graalvm.vm.trcview.analysis.type.DefaultTypes;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public class CodeAnalyzer {
    private Set<CodeInfo> code;

    public CodeAnalyzer() {
        code = new HashSet<>();
    }

    public void step(StepEvent event) {
        long pc = event.getPC();
        code.add(new CodeInfo(pc, event.getMachinecode().length));
    }

    public void transfer(TraceAnalyzer trc) {
        TypedMemory mem = trc.getTypedMemory();

        for (CodeInfo info : code) {
            mem.setRecoveredType(info.getPC(), DefaultTypes.getCodeType(info.getSize()));
        }

        code = null;
    }
}
