package org.graalvm.vm.trcview.io;

import org.graalvm.vm.trcview.arch.io.StepEvent;

public interface Block {
    StepEvent getHead();

    int size();

    Node get(int i);
}
