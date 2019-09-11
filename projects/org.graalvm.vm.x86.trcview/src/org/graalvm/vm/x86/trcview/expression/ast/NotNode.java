package org.graalvm.vm.x86.trcview.expression.ast;

import org.graalvm.vm.x86.trcview.expression.EvaluationException;
import org.graalvm.vm.x86.trcview.expression.ExpressionContext;

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
    public String toString() {
        return "!" + child;
    }
}
