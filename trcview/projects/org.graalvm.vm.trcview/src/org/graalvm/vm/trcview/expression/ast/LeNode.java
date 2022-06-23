package org.graalvm.vm.trcview.expression.ast;

import java.util.Map;
import java.util.Objects;

import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.expression.ExpressionContext;

public class LeNode extends Expression {
    public final Expression left;
    public final Expression right;

    public LeNode(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public long evaluate(ExpressionContext ctx) throws EvaluationException {
        return left.evaluate(ctx) <= right.evaluate(ctx) ? 1 : 0;
    }

    @Override
    public Expression materialize(Map<String, Long> vars) {
        Expression l = left.materialize(vars);
        Expression r = right.materialize(vars);
        if (l != left || r != right) {
            return new LeNode(l, r);
        } else {
            return this;
        }
    }

    @Override
    protected String str(boolean par) {
        if (par) {
            return "(" + left.str(true) + " <= " + right.str(true) + ")";
        } else {
            return left.str(true) + " <= " + right.str(true);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof LeNode)) {
            return false;
        }
        LeNode n = (LeNode) o;
        return left.equals(n.left) && right.equals(n.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }
}
