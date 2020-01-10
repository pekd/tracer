package org.graalvm.vm.trcview.script.ast.expr;

import java.util.List;

import org.graalvm.vm.trcview.script.ast.Expression;
import org.graalvm.vm.trcview.script.ast.Function;
import org.graalvm.vm.trcview.script.rt.Context;
import org.graalvm.vm.trcview.script.rt.Pointer;
import org.graalvm.vm.trcview.script.type.PointerType;
import org.graalvm.vm.trcview.script.type.Type;

public class CallNode extends Expression {
    private final Function func;
    private final List<Expression> args;

    public CallNode(Function func, List<Expression> args) {
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

    @Override
    public Pointer executePointer(Context ctx) {
        Object[] values = new Object[args.size()];
        for (int i = 0; i < values.length; i++) {
            Type type = func.getArgumentTypes().get(i);
            if (type instanceof PointerType) {
                values[i] = args.get(i).executePointer(ctx);
            } else {
                values[i] = args.get(i).execute(ctx);
            }
        }
        return func.executePointer(ctx, values);
    }
}
