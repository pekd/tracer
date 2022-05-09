package org.graalvm.vm.trcview.ui.event;

import org.graalvm.vm.trcview.net.TraceAnalyzer;

@FunctionalInterface
public interface TraceListener {
    void setTraceAnalyzer(TraceAnalyzer trc);
}
