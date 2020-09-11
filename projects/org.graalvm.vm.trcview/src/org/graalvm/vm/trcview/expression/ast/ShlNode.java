package org.graalvm.vm.trcview.expression.ast;

import java.util.Objects;

import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.expression.ExpressionContext;

public class ShlNode extends Expression {
    public final Expression left;
    public final Expression right;

    public ShlNode(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public long evaluate(ExpressionContext ctx) throws EvaluationException {
        return left.evaluate(ctx) << right.evaluate(ctx);
    }

    @Override
    public String toString() {
        return "(" + left + " << " + right + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof ShlNode)) {
            return false;
        }
        ShlNode n = (ShlNode) o;
        return left.equals(n.left) && right.equals(n.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }
}