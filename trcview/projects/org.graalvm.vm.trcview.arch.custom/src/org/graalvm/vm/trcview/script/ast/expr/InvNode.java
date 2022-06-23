package org.graalvm.vm.trcview.script.ast.expr;

import org.graalvm.vm.trcview.script.ast.Expression;
import org.graalvm.vm.trcview.script.rt.Context;

public class InvNode extends Expression {
    private final Expression expr;

    public InvNode(Expression expr) {
        this.expr = expr;
    }

    @Override
    public long execute(Context ctx) {
        return ~expr.execute(ctx);
    }
}
