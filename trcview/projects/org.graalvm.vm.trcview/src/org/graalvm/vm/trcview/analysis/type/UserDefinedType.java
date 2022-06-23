package org.graalvm.vm.trcview.analysis.type;

public abstract class UserDefinedType {
    private String name;

    protected UserDefinedType(String name) {
        this.name = name;
    }

    void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public abstract String toString();
}
