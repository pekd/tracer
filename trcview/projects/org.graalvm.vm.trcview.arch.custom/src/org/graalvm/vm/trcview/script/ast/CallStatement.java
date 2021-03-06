package org.graalvm.vm.trcview.script.ast;

import java.util.List;

import org.graalvm.vm.trcview.script.ast.expr.VariableNode;
import org.graalvm.vm.trcview.script.rt.Context;
import org.graalvm.vm.trcview.script.type.PointerType;
import org.graalvm.vm.trcview.script.type.Type;

public class CallStatement extends Statement {
    private final Function func;
    private final List<Expression> args;

    public CallStatement(Function func, List<Expression> args) {
        this.func = func;
        this.args = args;
    }

    @Override
    public long execute(Context ctx) {
        Object[] values = new Object[args.size()];
        List<Type> argtypes = func.getArgumentTypes();
        for (int i = 0; i < values.length; i++) {
            if (i < argtypes.size() && argtypes.get(i) instanceof PointerType) {
                values[i] = args.get(i).executePointer(ctx);
            } else if (args.get(i) instanceof VariableNode) {
                VariableNode v = (VariableNode) args.get(i);
                Type type = v.getType();
                if (type instanceof PointerType) {
                    values[i] = v.executePointer(ctx);
                } else {
                    values[i] = v.execute(ctx);
                }
            } else {
                values[i] = args.get(i).execute(ctx);
            }
        }
        return func.execute(ctx, values);
    }
}
