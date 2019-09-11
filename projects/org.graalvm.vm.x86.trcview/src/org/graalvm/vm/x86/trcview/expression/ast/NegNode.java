package org.graalvm.vm.x86.trcview.expression.ast;

import org.graalvm.vm.x86.trcview.expression.EvaluationException;
import org.graalvm.vm.x86.trcview.expression.ExpressionContext;

public class NegNode extends Expression {
    public final Expression child;

    public NegNode(Expression child) {
        this.child = child;
    }

    @Override
    public long evaluate(ExpressionContext ctx) throws EvaluationException {
        return -child.evaluate(ctx);
    }

    @Override
    public String toString() {
        return "-" + child;
    }
}
