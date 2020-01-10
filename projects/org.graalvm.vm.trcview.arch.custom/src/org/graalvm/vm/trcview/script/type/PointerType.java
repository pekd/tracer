package org.graalvm.vm.trcview.script.type;

public class PointerType extends Type {
    private Type pointee;

    public PointerType(Type pointee) {
        this.pointee = pointee;
    }

    public Type getType() {
        return pointee;
    }

    @Override
    public String toString() {
        return pointee + "*";
    }

    @Override
    public int size() {
        return 8;
    }
}
