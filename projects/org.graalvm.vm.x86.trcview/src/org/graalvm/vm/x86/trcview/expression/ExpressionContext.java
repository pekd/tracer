package org.graalvm.vm.x86.trcview.expression;

import org.graalvm.vm.x86.node.debug.trace.StepRecord;
import org.graalvm.vm.x86.trcview.analysis.memory.VirtualMemorySnapshot;

public class ExpressionContext {
    public final StepRecord step;
    public final VirtualMemorySnapshot mem;

    public ExpressionContext(StepRecord step, VirtualMemorySnapshot mem) {
        this.step = step;
        this.mem = mem;
    }
}
