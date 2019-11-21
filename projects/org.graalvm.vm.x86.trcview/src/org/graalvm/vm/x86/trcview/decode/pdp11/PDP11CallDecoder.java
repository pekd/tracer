package org.graalvm.vm.x86.trcview.decode.pdp11;

import org.graalvm.vm.x86.trcview.analysis.type.Function;
import org.graalvm.vm.x86.trcview.decode.CallDecoder;
import org.graalvm.vm.x86.trcview.io.data.CpuState;
import org.graalvm.vm.x86.trcview.net.TraceAnalyzer;

public class PDP11CallDecoder extends CallDecoder {
    @Override
    public String decode(Function function, CpuState state, CpuState nextState, TraceAnalyzer trc) {
        return null;
    }
}
