package org.graalvm.vm.trcview.arch.io;

public interface CpuDeltaState<T extends CpuState> {
    T resolve(T last);
}
