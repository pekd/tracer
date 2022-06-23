package org.graalvm.vm.trcview.script.ast;

import org.graalvm.vm.trcview.script.rt.Context;
import org.graalvm.vm.trcview.script.rt.ReturnException;

public class Return extends Statement {
    private final Expression value;

    public Return() {
        value = null;
    }

    public Return(Expression value) {
        this.value = value;
    }

    @Override
    public long execute(Context ctx) {
        throw new ReturnException(value == null ? 0 : value.execute(ctx));
    }
}
