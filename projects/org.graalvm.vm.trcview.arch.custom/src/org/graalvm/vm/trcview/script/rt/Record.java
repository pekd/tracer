package org.graalvm.vm.trcview.script.rt;

import org.graalvm.vm.trcview.script.type.Type;

public class Record {
    private final Type type;
    private final byte[] data;

    public Record(Type type) {
        this.type = type;
        data = new byte[type.size()];
    }

    Record(Type type, byte[] data) {
        this.type = type;
        this.data = data;
    }

    public Type getType() {
        return type;
    }

    public byte[] getData() {
        return data;
    }
}
