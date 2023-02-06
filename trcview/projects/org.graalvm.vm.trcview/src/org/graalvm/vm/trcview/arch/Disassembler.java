package org.graalvm.vm.trcview.arch;

import static org.graalvm.vm.trcview.disasm.Type.OTHER;

import org.graalvm.vm.trcview.analysis.ComputedSymbol;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.data.Variable;
import org.graalvm.vm.trcview.disasm.AssemblerInstruction;
import org.graalvm.vm.trcview.disasm.Operand;
import org.graalvm.vm.trcview.disasm.Token;
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

    public AssemblerInstruction disassemble(CodeReader code) {
        String[] parts = getDisassembly(code);
        if (parts.length == 1) {
            return new AssemblerInstruction(parts[0]);
        } else {
            Operand[] operands = new Operand[parts.length - 1];
            for (int i = 0; i < operands.length; i++) {
                Token token = new Token(OTHER, parts[i + 1]);
                operands[i] = new Operand(token);
            }
            return new AssemblerInstruction(parts[0], operands);
        }
    }

    protected static String[] getDisassembly(AssemblerInstruction asm) {
        Operand[] operands = asm.getOperands();
        String[] result = new String[operands.length + 1];
        result[0] = asm.getMnemonic();
        for (int i = 0; i < operands.length; i++) {
            result[i + 1] = operands[i].toString();
        }
        return result;
    }

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
