package org.graalvm.vm.trcview.expression.ast;

import java.util.Map;

import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.expression.ExpressionContext;

public abstract class Expression {
    public abstract long evaluate(ExpressionContext ctx) throws EvaluationException;

    public abstract Expression materialize(Map<String, Long> vars);

    @Override
    public final String toString() {
        return str(false);
    }

    protected abstract String str(boolean needPar);
}
