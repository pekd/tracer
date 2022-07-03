package org.graalvm.vm.trcview.script.ast.expr;

import org.graalvm.vm.trcview.script.ast.Expression;
import org.graalvm.vm.trcview.script.ast.Variable;
import org.graalvm.vm.trcview.script.rt.Context;
import org.graalvm.vm.trcview.script.rt.Pointer;
import org.graalvm.vm.trcview.script.type.PrimitiveType;
import org.graalvm.vm.trcview.script.type.Type;

public class VariableNode extends Expression {
    private final Variable var;

    public VariableNode(Variable var) {
        this.var = var;
    }

    public Type getType() {
        return var.getType();
    }

    @Override
    public long execute(Context ctx) {
        PrimitiveType type = (PrimitiveType) var.getType();
        long val = ctx.get(var);
        switch (type.getBasicType()) {
            case CHAR:
                if (type.isUnsigned()) {
                    return Byte.toUnsignedLong((byte) val);
                } else {
                    return (byte) val;
                }
            case SHORT:
                if (type.isUnsigned()) {
                    return Short.toUnsignedLong((short) val);
                } else {
                    return (short) val;
                }
            case INT:
                if (type.isUnsigned()) {
                    return Integer.toUnsignedLong((int) val);
                } else {
                    return (int) val;
                }
            case LONG:
            default:
                return val;
        }
    }

    @Override
    public Pointer executePointer(Context ctx) {
        return ctx.getPointer(var);
    }
}
