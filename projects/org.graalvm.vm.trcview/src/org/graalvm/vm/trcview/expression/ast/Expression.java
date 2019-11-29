package org.graalvm.vm.trcview.expression.ast;

import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.expression.ExpressionContext;

public abstract class Expression {
    public abstract long evaluate(ExpressionContext ctx) throws EvaluationException;
}
