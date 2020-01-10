package org.graalvm.vm.trcview.script.ast;

import org.graalvm.vm.trcview.script.rt.Context;
import org.graalvm.vm.trcview.script.rt.Pointer;

public class PointerAssignment extends Statement {
    private final Variable var;
    private final Expression expr;

    public PointerAssignment(Variable var, Expression expr) {
        this.var = var;
        this.expr = expr;
    }

    @Override
    public long execute(Context ctx) {
        Pointer value = expr.executePointer(ctx);
        ctx.setPointer(var, value);
        return value != null ? 1 : 0;
    }
}
