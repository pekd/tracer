package org.graalvm.vm.x86.trcview.expression;

import org.graalvm.vm.x86.trcview.analysis.memory.VirtualMemorySnapshot;
import org.graalvm.vm.x86.trcview.io.data.StepEvent;

public class ExpressionContext {
    public final StepEvent step;
    public final VirtualMemorySnapshot mem;

    public ExpressionContext(StepEvent step, VirtualMemorySnapshot mem) {
        this.step = step;
        this.mem = mem;
    }
}
