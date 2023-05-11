package org.graalvm.vm.trcview.analysis.type;

public class Union extends UserDefinedType {
    public Union(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return "union " + getName() + " {}";
    }
}
