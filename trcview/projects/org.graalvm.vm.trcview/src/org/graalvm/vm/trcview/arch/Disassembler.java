package org.graalvm.vm.trcview.arch;

import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.data.Variable;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public abstract class Disassembler {
    protected final TraceAnalyzer trc;

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
            Symbol sym = trc.getSymbol(addr);
            if (sym != null) {
                return sym.getName();
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
            Symbol sym = trc.getSymbol(addr);
            if (sym != null) {
                return sym.getName();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
