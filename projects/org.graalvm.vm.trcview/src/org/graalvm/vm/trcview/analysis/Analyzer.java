package org.graalvm.vm.trcview.analysis;

import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.io.Node;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public interface Analyzer {
    void start(TraceAnalyzer trc);

    void process(Event event, Node node);

    void finish();
}
