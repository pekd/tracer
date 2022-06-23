package org.graalvm.vm.trcview.disasm;

public interface Value {
    int get();

    default long get64() {
        return get();
    }

    void set(int value);

    default void set64(long value) {
        set((int) value);
    }
}
