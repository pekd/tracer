package org.graalvm.vm.trcview.script.ast.expr;

import org.graalvm.vm.trcview.script.ast.Expression;
import org.graalvm.vm.trcview.script.rt.Context;

public class ConstantNode extends Expression {
    private final long value;

    public ConstantNode(long value) {
        this.value = value;
    }

    @Override
    public long execute(Context ctx) {
        return value;
    }
}
