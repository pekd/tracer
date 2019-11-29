package org.graalvm.vm.trcview.expression.ast;

import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.expression.ExpressionContext;

public class LogicOrNode extends Expression {
    public final Expression left;
    public final Expression right;

    public LogicOrNode(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public long evaluate(ExpressionContext ctx) throws EvaluationException {
        return left.evaluate(ctx) != 0 || right.evaluate(ctx) != 0 ? 1 : 0;
    }

    @Override
    public String toString() {
        return "(" + left + " || " + right + ")";
    }
}
