package org.graalvm.vm.trcview.arch.custom.decode;

import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.decode.SyscallDecoder;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public class CustomSyscallDecoder extends SyscallDecoder {
    @Override
    public String decode(CpuState state, CpuState next, TraceAnalyzer trc) {
        return null;
    }

    @Override
    public String decodeResult(int sc, CpuState state, TraceAnalyzer trc) {
        return null;
    }

    @Override
    public String decode(CpuState state, TraceAnalyzer trc) {
        return null;
    }
}
