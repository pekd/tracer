package org.graalvm.vm.trcview.decode;

import org.graalvm.vm.trcview.expression.ast.Expression;

public abstract class CallingConvention {
    public abstract int getFixedArgumentCount();

    public abstract Expression getArgument(int i);

    public abstract Expression getReturn();
}
