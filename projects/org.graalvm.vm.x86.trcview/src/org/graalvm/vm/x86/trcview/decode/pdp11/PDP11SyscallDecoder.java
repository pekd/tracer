package org.graalvm.vm.x86.trcview.decode.pdp11;

import org.graalvm.vm.x86.trcview.decode.SyscallDecoder;
import org.graalvm.vm.x86.trcview.io.data.CpuState;
import org.graalvm.vm.x86.trcview.net.TraceAnalyzer;

public class PDP11SyscallDecoder extends SyscallDecoder {
    @Override
    public String decode(CpuState state, CpuState next, TraceAnalyzer trc) {
        return null;
    }

    @Override
    public String decodeResult(int sc, CpuState state) {
        return null;
    }

    @Override
    public String decode(CpuState state, TraceAnalyzer trc) {
        return null;
    }
}
