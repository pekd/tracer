package org.graalvm.vm.trcview.expression.ast;

import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.expression.ExpressionContext;
import org.graalvm.vm.trcview.expression.UnknownVariableException;

public class VariableNode extends Expression {
    public final String name;

    public VariableNode(String name) {
        this.name = name;
    }

    @Override
    public long evaluate(ExpressionContext ctx) throws EvaluationException {
        try {
            return ctx.state.get(name);
        } catch (IllegalArgumentException e) {
            throw new UnknownVariableException(name);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
