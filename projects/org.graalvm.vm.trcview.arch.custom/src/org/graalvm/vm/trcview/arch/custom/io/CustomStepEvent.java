package org.graalvm.vm.trcview.arch.custom.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.custom.analysis.CustomAnalyzer;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.io.DerivedStepEvent;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.script.rt.Pointer;
import org.graalvm.vm.trcview.script.type.PrimitiveType;
import org.graalvm.vm.trcview.script.type.Struct;
import org.graalvm.vm.trcview.script.type.Struct.Member;
import org.graalvm.vm.util.io.WordOutputStream;

public class CustomStepEvent extends DerivedStepEvent {
    private final CustomCpuState state;
    private final CustomAnalyzer analyzer;
    private final Pointer data;
    private final long parentStep;

    public CustomStepEvent(CustomAnalyzer analyzer, short arch, int tid, Pointer data, CustomCpuState state, long parentStep) {
        super(arch, tid);
        this.data = data;
        this.state = state;
        this.analyzer = analyzer;
        this.parentStep = parentStep;
    }

    @Override
    public byte[] getMachinecode() {
        Struct struct = (Struct) data.getType();
        Member insn = struct.getMember(analyzer.getArchitecture().getInsnName());
        if (insn == null) {
            throw new IllegalStateException("cannot find insn field \"" + analyzer.getArchitecture().getInsnName() + "\"");
        }
        Member insnLen = struct.getMember(analyzer.getArchitecture().getInsnLength());
        if (insnLen == null) {
            throw new IllegalStateException("cannot find insnLen field \"" + analyzer.getArchitecture().getInsnLength() + "\"");
        }
        int insnLength = Byte.toUnsignedInt(data.add(PrimitiveType.UCHAR, insnLen.offset).getI8());
        byte[] result = new byte[insnLength];
        Pointer ptr = data.add(PrimitiveType.UCHAR, insn.offset);
        for (int i = 0; i < result.length; i++) {
            result[i] = ptr.getI8(i);
        }
        return result;
    }

    @Override
    public String[] getDisassemblyComponents() {
        return analyzer.disassemble(data).split("\t");
    }

    @Override
    public String getMnemonic() {
        return getDisassemblyComponents()[0];
    }

    @Override
    public long getPC() {
        return state.getPC();
    }

    @Override
    public boolean isCall() {
        return getType() == InstructionType.CALL;
    }

    @Override
    public boolean isReturn() {
        return getType() == InstructionType.RET;
    }

    @Override
    public boolean isSyscall() {
        return getType() == InstructionType.SYSCALL;
    }

    @Override
    public boolean isReturnFromSyscall() {
        return getType() == InstructionType.RTI;
    }

    @Override
    public InstructionType getType() {
        return analyzer.getType(data);
    }

    @Override
    public long getStep() {
        return state.getStep();
    }

    @Override
    public CpuState getState() {
        return state;
    }

    @Override
    public StepFormat getFormat() {
        return analyzer.getArchitecture().getFormat();
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        // TODO Auto-generated method stub
    }

    @Override
    public long getParentStep() {
        return parentStep;
    }
}
