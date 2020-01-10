package org.graalvm.vm.trcview.script.ast;

import org.graalvm.vm.trcview.script.rt.Context;
import org.graalvm.vm.trcview.script.rt.Pointer;
import org.graalvm.vm.trcview.script.type.Type;

public abstract class PointerOperation {
    private final Type type;

    protected PointerOperation(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public abstract Pointer execute(Context ctx);
}
