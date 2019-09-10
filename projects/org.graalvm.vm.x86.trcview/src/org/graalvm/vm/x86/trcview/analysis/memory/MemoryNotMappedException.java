package org.graalvm.vm.x86.trcview.analysis.memory;

@SuppressWarnings("serial")
public class MemoryNotMappedException extends Exception {
    public MemoryNotMappedException(String msg) {
        super(msg);
    }
}
