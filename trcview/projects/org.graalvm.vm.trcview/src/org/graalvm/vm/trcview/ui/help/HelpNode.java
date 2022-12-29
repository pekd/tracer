package org.graalvm.vm.trcview.ui.help;

public abstract class HelpNode {
    private final String name;

    protected HelpNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }
}
