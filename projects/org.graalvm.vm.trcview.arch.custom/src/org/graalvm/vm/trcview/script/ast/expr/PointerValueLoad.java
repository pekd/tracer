package org.graalvm.vm.trcview.script.ast.expr;

import org.graalvm.vm.trcview.script.ast.Expression;
import org.graalvm.vm.trcview.script.ast.PointerOperation;
import org.graalvm.vm.trcview.script.rt.Context;
import org.graalvm.vm.trcview.script.rt.Pointer;
import org.graalvm.vm.trcview.script.type.PrimitiveType;

public class PointerValueLoad extends Expression {
    private final PointerOperation op;
    private final PrimitiveType type;

    public PointerValueLoad(PointerOperation op) {
        this.op = op;
        this.type = (PrimitiveType) op.getType();
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
