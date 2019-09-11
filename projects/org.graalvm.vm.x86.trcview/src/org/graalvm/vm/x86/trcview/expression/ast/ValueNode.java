package org.graalvm.vm.x86.trcview.expression.ast;

import org.graalvm.vm.x86.trcview.expression.ExpressionContext;

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
    public String toString() {
        return Long.toString(value);
    }
}
