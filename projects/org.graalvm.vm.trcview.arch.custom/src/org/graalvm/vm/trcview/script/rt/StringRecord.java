package org.graalvm.vm.trcview.script.rt;

import org.graalvm.vm.trcview.script.type.ArrayType;
import org.graalvm.vm.trcview.script.type.BasicType;
import org.graalvm.vm.trcview.script.type.PointerType;
import org.graalvm.vm.trcview.script.type.PrimitiveType;
import org.graalvm.vm.trcview.script.type.Type;

public class StringRecord extends Record {
    public static final Type TYPE = new PointerType(new PrimitiveType(BasicType.CHAR));

    private StringRecord(Type type, byte[] data) {
        super(type, data);
    }

    public static StringRecord create(String s) {
        byte[] b = s.getBytes();
        byte[] data = new byte[b.length + 1];
        System.arraycopy(b, 0, data, 0, b.length);
        data[data.length - 1] = 0;
        return new StringRecord(new ArrayType(new PrimitiveType(BasicType.CHAR), data.length), data);
    }
}
