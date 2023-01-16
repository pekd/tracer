package org.graalvm.vm.trcview.expression;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.data.Variable;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public class ExpressionContext {
    public final CpuState state;
    public final TraceAnalyzer trc;
    public final Map<String, Long> constants;

    private Map<String, Long> symbols;

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

    public long resolve(String name) {
        if (symbols == null) {
            // TODO: use some more global caching to avoid costly rebuilds of
            // the symbol map
            symbols = new HashMap<>();

            for (Symbol sym : trc.getTraceSymbols().values()) {
                symbols.put(sym.getName(), sym.getValue());
            }

            StepFormat fmt = trc.getArchitecture().getFormat();
            for (Variable var : trc.getTypedMemory().getAllTypes()) {
                symbols.put(var.getName(fmt), var.getAddress());
            }
        }

        Long addr = symbols.get(name);
        if (addr != null) {
            return addr;
        }

        throw new IllegalArgumentException("unknown symbol " + name);
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
