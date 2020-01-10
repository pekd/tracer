package org.graalvm.vm.trcview.script.rt;

import org.graalvm.vm.trcview.script.type.BasicType;
import org.graalvm.vm.trcview.script.type.PointerType;
import org.graalvm.vm.trcview.script.type.PrimitiveType;
import org.graalvm.vm.trcview.script.type.Type;

public class JavaPointer extends Pointer {
    private static final Type TYPE = new PointerType(new PrimitiveType(BasicType.VOID));

    private final Object data;

    public JavaPointer(Object data) {
        super(TYPE, 0, new Record(TYPE));
        this.data = data;
    }

    @SuppressWarnings("unchecked")
    public <T> T getObject() {
        return (T) data;
    }
}
