package org.graalvm.vm.trcview.arch.io;

public interface CpuState {
    long getStep();

    long getPC();

    long get(String name);

    int getTid();

    default long getRegisterById(@SuppressWarnings("unused") int id) {
        throw new UnsupportedOperationException();
    }
}
