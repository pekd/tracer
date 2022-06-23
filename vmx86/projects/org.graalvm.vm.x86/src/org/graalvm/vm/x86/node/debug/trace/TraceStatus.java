package org.graalvm.vm.x86.node.debug.trace;

public interface TraceStatus {
    boolean getTraceStatus();

    void setTraceStatus(boolean status);
}
