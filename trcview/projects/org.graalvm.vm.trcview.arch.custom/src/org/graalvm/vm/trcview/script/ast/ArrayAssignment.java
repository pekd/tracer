package org.graalvm.vm.trcview.script.ast;

import org.graalvm.vm.trcview.script.rt.Context;
import org.graalvm.vm.trcview.script.rt.Pointer;
import org.graalvm.vm.trcview.script.type.ArrayType;
import org.graalvm.vm.trcview.script.type.PointerType;
import org.graalvm.vm.trcview.script.type.PrimitiveType;
import org.graalvm.vm.trcview.script.type.Type;

public class ArrayAssignment extends Statement {
    private final PointerOperation op;
    private final Expression index;
    private final Expression expr;
    private final PrimitiveType type;

    public ArrayAssignment(PointerOperation op, Expression index, Expression expr) {
        this.op = op;
        this.index = index;
        this.expr = expr;
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
        long value = expr.execute(ctx);
        Pointer ptr = op.execute(ctx);
        ptr = ptr.add(type, (int) (type.size() * idx));
        switch (type.getBasicType()) {
            case CHAR:
                ptr.setI8((byte) value);
                break;
            case SHORT:
                ptr.setI16((short) value);
                break;
            case INT:
                ptr.setI32((int) value);
                break;
            case LONG:
                ptr.setI64(value);
                break;
            default:
                throw new IllegalStateException("invalid/unsupported type");
        }
        return value;
    }
}
