package org.graalvm.vm.trcview.arch.io;

public abstract class SystemLogEvent extends Event {
    protected SystemLogEvent(int tid) {
        super(tid);
    }
}
