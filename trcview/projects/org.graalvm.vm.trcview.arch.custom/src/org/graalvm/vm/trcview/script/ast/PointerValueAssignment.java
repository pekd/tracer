package org.graalvm.vm.trcview.script.ast;

import org.graalvm.vm.trcview.script.rt.Context;
import org.graalvm.vm.trcview.script.rt.Pointer;
import org.graalvm.vm.trcview.script.type.PrimitiveType;

public class PointerValueAssignment extends Statement {
    private final PointerOperation op;
    private final Expression expr;
    private final PrimitiveType type;

    public PointerValueAssignment(PointerOperation op, Expression expr) {
        this.op = op;
        this.expr = expr;
        this.type = (PrimitiveType) op.getType();
    }

    @Override
    public long execute(Context ctx) {
        Pointer ptr = op.execute(ctx);
        long value = expr.execute(ctx);
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
