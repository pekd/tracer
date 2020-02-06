package org.graalvm.vm.trcview.expression.ast;

import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.expression.ExpressionContext;

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

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof NegNode)) {
            return false;
        }
        NegNode n = (NegNode) o;
        return child.equals(n.child);
    }

    @Override
    public int hashCode() {
        return child.hashCode();
    }
}
