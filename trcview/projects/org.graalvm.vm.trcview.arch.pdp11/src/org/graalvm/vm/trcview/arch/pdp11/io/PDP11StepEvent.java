package org.graalvm.vm.trcview.arch.pdp11.io;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.MemoryEvent;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.arch.pdp11.PDP11;
import org.graalvm.vm.trcview.arch.pdp11.disasm.PDP11Disassembler;
import org.graalvm.vm.trcview.arch.pdp11.disasm.PDP11Semantics;
import org.graalvm.vm.trcview.data.Semantics;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.util.io.Endianess;

public abstract class PDP11StepEvent extends StepEvent {
    private byte[] machinecode = null;

    protected PDP11StepEvent(int tid) {
        super(tid);
    }

    @Override
    public byte[] getMachinecode() {
        if (machinecode != null) {
            return machinecode;
        }

        short[] code = getState().getMachinecodeWords();
        int length = new PDP11Disassembler(null).getLength(code);
        machinecode = new byte[length * 2];
        for (int i = 0; i < length; i++) {
            Endianess.set16bitLE(machinecode, 2 * i, code[i]);
        }
        return machinecode;
    }

    @Override
    public String getDisassembly() {
        String[] code = getDisassemblyComponents();
        if (code.length == 1) {
            return code[0];
        } else {
            return code[0] + "\t" + Stream.of(code).skip(1).collect(Collectors.joining(",\t"));
        }
    }

    @Override
    public String[] getDisassemblyComponents() {
        return new PDP11Disassembler(null).getDisassembly(getState().getMachinecodeWords(), (short) getPC());
    }

    @Override
    public String[] getDisassemblyComponents(TraceAnalyzer trc) {
        return new PDP11Disassembler(trc).getDisassembly(getState().getMachinecodeWords(), (short) getPC());
    }

    @Override
    public String getMnemonic() {
        return getDisassemblyComponents()[0];
    }

    @Override
    public void getSemantics(Semantics semantics) {
        PDP11Semantics.getSemantics(semantics, getState().getMachinecodeWords(), (short) getPC());
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
        return PDP11Disassembler.getType(getState().getMachinecodeWords()[0]);
    }

    @Override
    public abstract PDP11CpuState getState();

    @Override
    public StepFormat getFormat() {
        return PDP11.FORMAT;
    }

    @Override
    public List<MemoryEvent> getDataReads() {
        List<MemoryEvent> reads = super.getDataReads();
        List<MemoryEvent> data = new ArrayList<>();
        for (MemoryEvent evt : reads) {
            long addr = evt.getAddress();
            // PC = instruction, PC+n = operands (offsets or immediate)
            // TODO: filter offsets without filtering immediate
            if (addr != getState().getPC()) {
                data.add(evt);
            }
        }
        return data;
    }

    @Override
    public int[] getRegisterReads() {
        return PDP11Semantics.getRegisterReads(getState().getMachinecodeWords(), (short) getPC());
    }

    @Override
    public int[] getRegisterWrites() {
        return PDP11Semantics.getRegisterWrites(getState().getMachinecodeWords(), (short) getPC());
    }

    @Override
    public String toString() {
        return String.format("%06o: %s ; step %d", getPC(), getDisassembly(), getStep());
    }
}
