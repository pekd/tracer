package org.graalvm.vm.trcview.script.ast.expr;

import org.graalvm.vm.trcview.script.ast.Expression;
import org.graalvm.vm.trcview.script.ast.PointerOperation;
import org.graalvm.vm.trcview.script.rt.Context;
import org.graalvm.vm.trcview.script.rt.Pointer;
import org.graalvm.vm.trcview.script.type.ArrayType;
import org.graalvm.vm.trcview.script.type.PointerType;
import org.graalvm.vm.trcview.script.type.PrimitiveType;
import org.graalvm.vm.trcview.script.type.Type;

public class ArrayRead extends Expression {
    private final PointerOperation op;
    private final Expression index;
    private final PrimitiveType type;

    public ArrayRead(PointerOperation op, Expression index) {
        this.op = op;
        this.index = index;
        Type typ;
        if (op.getType() instanceof ArrayType) {
            ArrayType t = (ArrayType) op.getType();
            typ = t.getType();
        } else if (op.getType() instanceof PointerType) {
            PointerType t = (PointerType) op.getType();
            typ = t.getType();
        } else {
            throw new IllegalArgumentException("not a pointer/array");
        }
        if (!(typ instanceof PrimitiveType)) {
            throw new IllegalArgumentException("not a primitive type");
        }
        type = (PrimitiveType) typ;
    }

    @Override
    public long execute(Context ctx) {
        long idx = index.execute(ctx);
        Pointer ptr = op.execute(ctx);
        ptr = ptr.add(type, (int) (type.size() * idx));
        switch (type.getBasicType()) {
            case CHAR:
                return ptr.getI8();
            case SHORT:
                return ptr.getI16();
            case INT:
                return ptr.getI32();
            case LONG:
                return ptr.getI64();
            default:
                throw new IllegalStateException("invalid/unsupported type");
        }
    }
}
