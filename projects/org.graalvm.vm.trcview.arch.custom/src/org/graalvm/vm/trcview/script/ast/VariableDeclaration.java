package org.graalvm.vm.trcview.script.ast;

import org.graalvm.vm.trcview.script.type.Type;

public class VariableDeclaration {
    private Type type;
    private String name;

    public VariableDeclaration(Type type, String name) {
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
        return type.vardecl(name);
    }
}
