package org.graalvm.vm.trcview.arch.io;

public abstract class SystemLogEvent extends Event {
    protected SystemLogEvent(short arch, int tid) {
        super(arch, SYSTEM_LOG, tid);
    }
}
