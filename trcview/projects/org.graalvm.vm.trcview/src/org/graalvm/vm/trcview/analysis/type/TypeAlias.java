package org.graalvm.vm.trcview.analysis.type;

public class TypeAlias extends UserDefinedType {
    private Type type;

    public TypeAlias(String name, Type type) {
        super(name);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "typedef " + type.toString() + " " + getName() + ";";
    }
}
