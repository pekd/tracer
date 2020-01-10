package org.graalvm.vm.trcview.script.ast;

import org.graalvm.vm.trcview.script.rt.Context;
import org.graalvm.vm.trcview.script.rt.Pointer;

public class PointerLoad extends PointerOperation {
    private final Variable var;

    public PointerLoad(Variable var) {
        super(var.getType());
        this.var = var;
    }

    @Override
    public Pointer execute(Context ctx) {
        return ctx.getPointer(var);
    }
}
