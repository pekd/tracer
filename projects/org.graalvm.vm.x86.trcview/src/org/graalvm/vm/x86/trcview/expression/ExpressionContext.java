package org.graalvm.vm.x86.trcview.expression;

import org.graalvm.vm.x86.node.debug.trace.StepRecord;

public class ExpressionContext {
    public final StepRecord step;

    public ExpressionContext(StepRecord step) {
        this.step = step;
    }
}
