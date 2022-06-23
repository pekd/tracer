package org.graalvm.vm.trcview.analysis.memory;

@SuppressWarnings("serial")
public class MemoryNotMappedException extends Exception {
    public MemoryNotMappedException(String msg) {
        super(msg);
    }
}
