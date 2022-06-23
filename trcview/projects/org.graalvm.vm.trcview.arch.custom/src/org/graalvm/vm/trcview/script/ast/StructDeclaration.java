package org.graalvm.vm.trcview.script.ast;

import org.graalvm.vm.trcview.script.rt.Context;
import org.graalvm.vm.trcview.script.rt.Pointer;
import org.graalvm.vm.trcview.script.rt.Record;
import org.graalvm.vm.trcview.script.type.Struct;
import org.graalvm.vm.trcview.script.type.Type;

public class StructDeclaration extends Statement {
    private final Variable var;
    private final Type type;

    public StructDeclaration(Variable var) {
        this.var = var;
        if (var.getType() instanceof Struct) {
            type = var.getType();
        } else {
            throw new IllegalArgumentException("not a struct");
        }
    }

    @Override
    public long execute(Context ctx) {
        Record record = new Record(type);
        Pointer ptr = new Pointer(type, 0, record);
        ctx.setPointer(var, ptr);
        return 0;
    }
}
