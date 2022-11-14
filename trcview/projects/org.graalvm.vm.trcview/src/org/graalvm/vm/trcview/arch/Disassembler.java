package org.graalvm.vm.trcview.arch;

import org.graalvm.vm.trcview.analysis.ComputedSymbol;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.data.Variable;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public abstract class Disassembler {
    protected final TraceAnalyzer trc;

    protected Disassembler() {
        this.trc = null;
    }

    protected Disassembler(TraceAnalyzer trc) {
        this.trc = trc;
    }

    public TraceAnalyzer getTraceAnalyzer() {
        return trc;
    }

    public abstract String[] getDisassembly(CodeReader code);

    public abstract int getLength(CodeReader code);

    public abstract InstructionType getType(CodeReader code);

    protected String getName(long addr) {
        if (trc != null) {
            ComputedSymbol sym = trc.getComputedSymbol(addr);
            if (sym != null) {
                return sym.name;
            } else {
                Variable v = trc.getTypedMemory().get(addr);
                if (v != null) {
                    StepFormat fmt = trc.getArchitecture().getFormat();
                    return v.getName(fmt);
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    protected String getLocation(long addr) {
        if (trc != null) {
            ComputedSymbol sym = trc.getComputedSymbol(addr);
            if (sym != null) {
                return sym.name;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
