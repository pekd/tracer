package org.graalvm.vm.trcview.ui.event;

public interface TraceListenable {
    void addTraceListener(TraceListener listener);

    void removeTraceListener(TraceListener listener);
}
