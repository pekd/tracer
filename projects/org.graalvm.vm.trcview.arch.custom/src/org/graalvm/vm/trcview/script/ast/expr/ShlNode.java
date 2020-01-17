package org.graalvm.vm.trcview.script.ast.expr;

import org.graalvm.vm.trcview.script.ast.Expression;
import org.graalvm.vm.trcview.script.rt.Context;

public class ShlNode extends Expression {
    private final Expression left;
    private final Expression right;

    public ShlNode(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public long execute(Context ctx) {
        long val = left.execute(ctx);
        long shift = right.execute(ctx);
        return val << shift;
    }

}
