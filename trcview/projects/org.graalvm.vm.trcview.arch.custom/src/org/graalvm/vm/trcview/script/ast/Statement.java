package org.graalvm.vm.trcview.script.ast;

import org.graalvm.vm.trcview.script.rt.Context;

public abstract class Statement {
    public abstract long execute(Context ctx);
}
