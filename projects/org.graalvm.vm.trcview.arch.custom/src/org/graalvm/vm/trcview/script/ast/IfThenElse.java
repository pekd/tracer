package org.graalvm.vm.trcview.script.ast;

import org.graalvm.vm.trcview.script.rt.Context;

public class IfThenElse extends Statement {
    private final Expression condition;
    private final Statement then;
    private final Statement otherwise;

    public IfThenElse(Expression condition, Statement then) {
        this.condition = condition;
        this.then = then;
        this.otherwise = null;
    }

    public IfThenElse(Expression condition, Statement then, Statement otherwise) {
        this.condition = condition;
        this.then = then;
        this.otherwise = otherwise;
    }

    @Override
    public long execute(Context ctx) {
        long cond = condition.execute(ctx);
        if (cond != 0) {
            return then.execute(ctx);
        } else if (otherwise != null) {
            return otherwise.execute(ctx);
        } else {
            return 0;
        }
    }
}
