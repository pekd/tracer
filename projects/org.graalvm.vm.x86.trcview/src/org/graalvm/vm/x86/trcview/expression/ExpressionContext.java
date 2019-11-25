package org.graalvm.vm.x86.trcview.expression;

import org.graalvm.vm.x86.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.x86.trcview.io.data.CpuState;
import org.graalvm.vm.x86.trcview.net.TraceAnalyzer;

public class ExpressionContext {
    public final CpuState state;
    public final TraceAnalyzer trc;

    public ExpressionContext(CpuState state, TraceAnalyzer trc) {
        this.state = state;
        this.trc = trc;
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
