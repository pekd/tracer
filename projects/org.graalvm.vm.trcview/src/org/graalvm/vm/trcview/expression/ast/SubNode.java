package org.graalvm.vm.trcview.expression.ast;

import java.util.Map;
import java.util.Objects;

import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.expression.ExpressionContext;

public class SubNode extends Expression {
    public final Expression left;
    public final Expression right;

    public SubNode(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public long evaluate(ExpressionContext ctx) throws EvaluationException {
        return left.evaluate(ctx) - right.evaluate(ctx);
    }

    @Override
    public Expression materialize(Map<String, Long> vars) {
        Expression l = left.materialize(vars);
        Expression r = right.materialize(vars);
        if (l != left || r != right) {
            return new SubNode(l, r);
        } else {
            return this;
        }
    }

    @Override
    public String toString() {
        return "(" + left + " - " + right + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof SubNode)) {
            return false;
        }
        SubNode n = (SubNode) o;
        return left.equals(n.left) && right.equals(n.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }
}
