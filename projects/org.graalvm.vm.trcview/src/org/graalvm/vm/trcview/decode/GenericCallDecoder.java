package org.graalvm.vm.trcview.decode;

import org.graalvm.vm.trcview.analysis.type.Function;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public class GenericCallDecoder extends CallDecoder {
    @Override
    public String decode(Function function, CpuState state, CpuState nextState, TraceAnalyzer trc) {
        ABI abi = trc.getABI();
        return CallingConventionDecoder.decode(function, state, nextState, trc, abi != null ? abi.getCall() : null);
    }
}
