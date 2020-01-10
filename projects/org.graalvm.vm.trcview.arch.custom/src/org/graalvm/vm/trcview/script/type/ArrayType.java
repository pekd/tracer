package org.graalvm.vm.trcview.script.type;

import java.util.Objects;

public class ArrayType extends Type {
    private Type type;
    private long size;

    public ArrayType(Type type, long size) {
        this.type = type;
        this.size = size;
    }

    public Type getType() {
        return type;
    }

    public long getSize() {
        return size;
    }

    public int getOffset(int idx) {
        return type.size() * idx;
    }

    @Override
    public String toString() {
        return type.toString() + "[" + size + "]";
    }

    public Type getPrefix() {
        if (type instanceof ArrayType) {
            return ((ArrayType) type).getPrefix();
        } else {
            return type;
        }
    }

    public String getSuffix() {
        if (type instanceof ArrayType) {
            return ((ArrayType) type).getSuffix() + "[" + size + "]";
        } else {
            return "[" + size + "]";
        }
    }

    @Override
    public String vardecl(String name) {
        return getPrefix() + " " + name + getSuffix();
    }

    @Override
    public int size() {
        return (int) (type.size() * size);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ArrayType)) {
            return false;
        }
        ArrayType t = (ArrayType) o;
        return t.size == size && t.type.equals(type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, size);
    }
}
