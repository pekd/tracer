package org.graalvm.vm.x86.trcview.expression.ast;

import org.graalvm.vm.x86.trcview.expression.EvaluationException;
import org.graalvm.vm.x86.trcview.expression.ExpressionContext;

public abstract class Expression {
    public abstract long evaluate(ExpressionContext ctx) throws EvaluationException;
}
