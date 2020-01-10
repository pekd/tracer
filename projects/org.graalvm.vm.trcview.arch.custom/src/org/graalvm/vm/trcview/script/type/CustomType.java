package org.graalvm.vm.trcview.script.type;

public class CustomType extends Type {
    private String name;

    public CustomType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int size() {
        return 0; // TODO
    }
}
