package org.graalvm.vm.x86.trcview.io.data;

public abstract class SystemLogEvent extends Event {
    protected SystemLogEvent(short arch, int tid) {
        super(arch, SYSTEM_LOG, tid);
    }
}
