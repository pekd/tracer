package org.graalvm.vm.trcview.disasm;

public interface Value {
    int get();

    void set(int value);
}
