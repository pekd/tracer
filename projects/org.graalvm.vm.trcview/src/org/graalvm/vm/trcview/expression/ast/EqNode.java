package org.graalvm.vm.trcview.expression.ast;

import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.expression.ExpressionContext;

public class EqNode extends Expression {
    public final Expression left;
    public final Expression right;

    public EqNode(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public long evaluate(ExpressionContext ctx) throws EvaluationException {
        return left.evaluate(ctx) == right.evaluate(ctx) ? 1 : 0;
    }

    @Override
    public String toString() {
        return "(" + left + " == " + right + ")";
    }
}
