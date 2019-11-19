package org.graalvm.vm.x86.trcview.decode;

import org.graalvm.vm.x86.trcview.analysis.type.Function;
import org.graalvm.vm.x86.trcview.io.data.CpuState;
import org.graalvm.vm.x86.trcview.net.TraceAnalyzer;

public abstract class CallDecoder {
    public abstract String decode(Function function, CpuState state, CpuState nextState, TraceAnalyzer trc);
}
