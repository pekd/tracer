package org.graalvm.vm.trcview.script.ast;

import org.graalvm.vm.trcview.script.rt.Context;

public class Assignment extends Statement {
    private final Variable var;
    private final Expression expr;

    public Assignment(Variable var, Expression expr) {
        this.var = var;
        this.expr = expr;
    }

    @Override
    public long execute(Context ctx) {
        long value = expr.execute(ctx);
        ctx.set(var, value);
        return value;
    }
}
