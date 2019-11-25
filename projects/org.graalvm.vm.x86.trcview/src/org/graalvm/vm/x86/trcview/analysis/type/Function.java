package org.graalvm.vm.x86.trcview.analysis.type;

import java.util.stream.Collectors;

public class Function {
    private final String name;
    private final Prototype type;

    public Function(String name, Prototype type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Prototype getPrototype() {
        return type;
    }

    @Override
    public String toString() {
        if (type.expr != null) {
            return type.returnType.toString() + " " + name + "<" + type.expr + ">(" + type.args.stream().map(Type::toString).collect(Collectors.joining(", ")) + ")";
        } else {
            return type.returnType.toString() + " " + name + "(" + type.args.stream().map(Type::toString).collect(Collectors.joining(", ")) + ")";
        }
    }
}
