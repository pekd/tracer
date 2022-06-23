package org.graalvm.vm.trcview.arch.io;

import org.graalvm.vm.trcview.io.Node;

public abstract class Event extends Node {
    private final int tid;

    protected Event(int tid) {
        this.tid = tid;
    }

    @Override
    public final int getTid() {
        return tid;
    }
}
