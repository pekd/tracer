package org.graalvm.vm.trcview.script.ast.expr;

import org.graalvm.vm.trcview.script.ast.Expression;
import org.graalvm.vm.trcview.script.rt.Context;
import org.graalvm.vm.trcview.script.rt.Pointer;
import org.graalvm.vm.trcview.script.rt.StringRecord;

public class ConstantStringNode extends Expression {
    private final Pointer ptr;

    public ConstantStringNode(String str) {
        StringRecord record = StringRecord.create(str);
        ptr = new Pointer(StringRecord.TYPE, 0, record);
    }

    @Override
    public long execute(Context ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Pointer executePointer(Context ctx) {
        return ptr;
    }
}
