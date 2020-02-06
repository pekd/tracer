package org.graalvm.vm.trcview.expression.ast;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof LogicOrNode)) {
            return false;
        }
        LogicOrNode n = (LogicOrNode) o;
        return left.equals(n.left) && right.equals(n.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }
}
