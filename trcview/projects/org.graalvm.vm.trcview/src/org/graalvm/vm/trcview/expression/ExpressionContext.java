package org.graalvm.vm.trcview.expression;

import java.util.Collections;
import java.util.Map;

import org.graalvm.vm.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public class ExpressionContext {
    public final CpuState state;
    public final TraceAnalyzer trc;
    public final Map<String, Long> constants;

    public ExpressionContext(CpuState state, TraceAnalyzer trc) {
        this.state = state;
        this.trc = trc;
        this.constants = Collections.emptyMap();
    }

    public ExpressionContext(CpuState state, TraceAnalyzer trc, Map<String, Long> constants) {
        this.state = state;
        this.trc = trc;
        this.constants = constants;
    }

    public byte getI8(long address) throws MemoryNotMappedException {
        return trc.getI8(address, state.getStep());
    }

    public short getI16(long address) throws MemoryNotMappedException {
        return (short) trc.getI64(address, state.getStep());
    }

    public int getI32(long address) throws MemoryNotMappedException {
        return (int) trc.getI64(address, state.getStep());
    }

    public long getI64(long address) throws MemoryNotMappedException {
        return trc.getI64(address, state.getStep());
    }
}
