package org.graalvm.vm.trcview.analysis;

import org.graalvm.vm.trcview.analysis.memory.MemoryTrace;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.io.Node;

public interface Analyzer {
    void start(MemoryTrace mem);

    void process(Event event, Node node);

    void finish();
}
