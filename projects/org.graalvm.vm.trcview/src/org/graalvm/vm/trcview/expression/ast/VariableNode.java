package org.graalvm.vm.trcview.expression.ast;

import java.util.Map;

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
        Long val = ctx.constants.get(name);
        if (val != null) {
            return val;
        }

        try {
            return ctx.state.get(name);
        } catch (IllegalArgumentException e) {
            throw new UnknownVariableException(name);
        }
    }

    @Override
    public Expression materialize(Map<String, Long> vars) {
        Long val = vars.get(name);
        if (val != null) {
            return new ValueNode(val);
        } else {
            return this;
        }
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof VariableNode)) {
            return false;
        }
        VariableNode n = (VariableNode) o;
        return name.equals(n.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
