package org.graalvm.vm.trcview.script.ast.expr;

import org.graalvm.vm.trcview.script.ast.Expression;
import org.graalvm.vm.trcview.script.ast.Variable;
import org.graalvm.vm.trcview.script.rt.Context;
import org.graalvm.vm.trcview.script.rt.Pointer;
import org.graalvm.vm.trcview.script.type.ArrayType;
import org.graalvm.vm.trcview.script.type.PointerType;
import org.graalvm.vm.trcview.script.type.PrimitiveType;
import org.graalvm.vm.trcview.script.type.Type;

public class ArrayNode extends Expression {
    private final Variable var;
    private final Expression index;
    private final Type type;

    public ArrayNode(Variable var, Expression index) {
        this.var = var;
        this.index = index;
        if (var.getType() instanceof ArrayType) {
            ArrayType t = (ArrayType) var.getType();
            type = t.getType();
        } else if (var.getType() instanceof PointerType) {
            PointerType t = (PointerType) var.getType();
            type = t.getType();
        } else {
            throw new IllegalArgumentException("not a pointer/array");
        }
        assert type != null;
        if (!(type instanceof PrimitiveType)) {
            throw new IllegalArgumentException("not a primitive type");
        }
    }

    @Override
    public long execute(Context ctx) {
        long idx = index.execute(ctx);
        Pointer ptr = ctx.getPointer(var);
        ptr = ptr.add(type, (int) (type.size() * idx));
        return ptr.dereferenceScalar();
    }
}
