package org.graalvm.vm.trcview.analysis;

import org.graalvm.vm.trcview.analysis.memory.MemoryTrace;
import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.io.Node;

public interface Analyzer {
    void start(MemoryTrace mem, Architecture arch);

    void process(Event event, Node node, CpuState state);

    void finish();
}
