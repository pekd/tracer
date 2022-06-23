package org.graalvm.vm.trcview.analysis.type;

@SuppressWarnings("serial")
public class NameAlreadyUsedException extends Exception {
    public NameAlreadyUsedException(String name) {
        super("Name already used: " + name);
    }
}
