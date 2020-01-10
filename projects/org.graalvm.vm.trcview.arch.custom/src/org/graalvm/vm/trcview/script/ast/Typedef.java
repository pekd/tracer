package org.graalvm.vm.trcview.script.ast;

import org.graalvm.vm.trcview.script.type.Type;

public class Typedef {
    private Type type;
    private String name;

    public Typedef(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "typedef " + type + " " + name;
    }
}
