package org.graalvm.vm.trcview.script.ast.expr;

import org.graalvm.vm.trcview.script.ast.Expression;
import org.graalvm.vm.trcview.script.ast.PointerOperation;
import org.graalvm.vm.trcview.script.rt.Context;
import org.graalvm.vm.trcview.script.rt.Pointer;
import org.graalvm.vm.trcview.script.type.ArrayType;
import org.graalvm.vm.trcview.script.type.PointerType;
import org.graalvm.vm.trcview.script.type.PrimitiveType;
import org.graalvm.vm.trcview.script.type.Type;

public class PointerRead extends Expression {
    private final PointerOperation op;
    private final PrimitiveType type;

    public PointerRead(PointerOperation op) {
        this.op = op;
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
        assert typ != null;
        if (!(typ instanceof PrimitiveType)) {
            throw new IllegalArgumentException("not a primitive type");
        }
        type = (PrimitiveType) typ;
    }

    @Override
    public long execute(Context ctx) {
        Pointer ptr = op.execute(ctx);
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
