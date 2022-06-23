package org.graalvm.vm.trcview.expression.ast;

import java.util.Map;
import java.util.Objects;

import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.expression.ExpressionContext;

public class GtNode extends Expression {
    public final Expression left;
    public final Expression right;

    public GtNode(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public long evaluate(ExpressionContext ctx) throws EvaluationException {
        return left.evaluate(ctx) > right.evaluate(ctx) ? 1 : 0;
    }

    @Override
    public Expression materialize(Map<String, Long> vars) {
        Expression l = left.materialize(vars);
        Expression r = right.materialize(vars);
        if (l != left || r != right) {
            return new GtNode(l, r);
        } else {
            return this;
        }
    }

    @Override
    protected String str(boolean par) {
        if (par) {
            return "(" + left.str(true) + " > " + right.str(true) + ")";
        } else {
            return left.str(true) + " > " + right.str(true);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof GtNode)) {
            return false;
        }
        GtNode n = (GtNode) o;
        return left.equals(n.left) && right.equals(n.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }
}
