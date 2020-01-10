package org.graalvm.vm.trcview.script.ast;

import java.util.List;

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
        for (int i = 0; i < values.length; i++) {
            Type type = func.getArgumentTypes().get(i);
            if (type instanceof PointerType) {
                values[i] = args.get(i).executePointer(ctx);
            } else {
                values[i] = args.get(i).execute(ctx);
            }
        }
        return func.execute(ctx, values);
    }
}
