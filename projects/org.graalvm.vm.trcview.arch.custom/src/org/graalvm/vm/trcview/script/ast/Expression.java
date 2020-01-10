package org.graalvm.vm.trcview.script.ast;

import org.graalvm.vm.trcview.script.rt.Context;
import org.graalvm.vm.trcview.script.rt.Pointer;

public abstract class Expression extends Statement {
    public Pointer executePointer(@SuppressWarnings("unused") Context ctx) {
        throw new UnsupportedOperationException("executePointer not supported by " + this.getClass().getCanonicalName());
    }
}
