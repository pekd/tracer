package org.graalvm.vm.trcview.expression.ast;

import java.util.Map;

import org.graalvm.vm.trcview.expression.ExpressionContext;

public class ValueNode extends Expression {
    public long value;

    public ValueNode(long value) {
        this.value = value;
    }

    @Override
    public long evaluate(ExpressionContext ctx) {
        return value;
    }

    @Override
    public Expression materialize(Map<String, Long> vars) {
        return this;
    }

    @Override
    protected String str(boolean par) {
        return Long.toString(value);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof ValueNode)) {
            return false;
        }
        ValueNode n = (ValueNode) o;
        return value == n.value;
    }

    @Override
    public int hashCode() {
        return (int) (value ^ (value >> 32));
    }
}
