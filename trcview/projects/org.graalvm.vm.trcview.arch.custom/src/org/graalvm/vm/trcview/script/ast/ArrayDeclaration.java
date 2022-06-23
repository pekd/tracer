package org.graalvm.vm.trcview.script.ast;

import org.graalvm.vm.trcview.script.rt.Context;
import org.graalvm.vm.trcview.script.rt.Pointer;
import org.graalvm.vm.trcview.script.rt.Record;
import org.graalvm.vm.trcview.script.type.ArrayType;
import org.graalvm.vm.trcview.script.type.Type;

public class ArrayDeclaration extends Statement {
    private final Variable var;
    private final Type type;

    public ArrayDeclaration(Variable var) {
        this.var = var;
        if (var.getType() instanceof ArrayType) {
            ArrayType t = (ArrayType) var.getType();
            type = t.getType();
        } else {
            throw new IllegalArgumentException("not an array");
        }
    }

    @Override
    public long execute(Context ctx) {
        Record record = new Record(var.getType());
        Pointer ptr = new Pointer(type, 0, record);
        ctx.setPointer(var, ptr);
        return 0;
    }
}
