package org.graalvm.vm.trcview.expression.ast;

import java.util.Map;

import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.expression.ExpressionContext;

public class NotNode extends Expression {
    public final Expression child;

    public NotNode(Expression child) {
        this.child = child;
    }

    @Override
    public long evaluate(ExpressionContext ctx) throws EvaluationException {
        long value = child.evaluate(ctx);
        if (value == 0) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public Expression materialize(Map<String, Long> vars) {
        Expression c = child.materialize(vars);
        if (c != child) {
            return new NotNode(c);
        } else {
            return c;
        }
    }

    @Override
    public String toString() {
        return "!" + child;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof NotNode)) {
            return false;
        }
        NotNode n = (NotNode) o;
        return child.equals(n.child);
    }

    @Override
    public int hashCode() {
        return child.hashCode();
    }
}
