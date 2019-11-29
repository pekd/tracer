package org.graalvm.vm.trcview.expression.ast;

import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.expression.ExpressionContext;

public class AddNode extends Expression {
    public final Expression left;
    public final Expression right;

    public AddNode(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public long evaluate(ExpressionContext ctx) throws EvaluationException {
        return left.evaluate(ctx) + right.evaluate(ctx);
    }

    @Override
    public String toString() {
        return "(" + left + " + " + right + ")";
    }
}
