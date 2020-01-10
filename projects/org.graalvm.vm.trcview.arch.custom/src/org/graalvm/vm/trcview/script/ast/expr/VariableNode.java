package org.graalvm.vm.trcview.script.ast.expr;

import org.graalvm.vm.trcview.script.ast.Expression;
import org.graalvm.vm.trcview.script.ast.Variable;
import org.graalvm.vm.trcview.script.rt.Context;
import org.graalvm.vm.trcview.script.rt.Pointer;

public class VariableNode extends Expression {
    private final Variable var;

    public VariableNode(Variable var) {
        this.var = var;
    }

    @Override
    public long execute(Context ctx) {
        return ctx.get(var);
    }

    @Override
    public Pointer executePointer(Context ctx) {
        return ctx.getPointer(var);
    }
}
