package org.graalvm.vm.trcview.arch.h8s.io;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.graalvm.vm.trcview.arch.ByteCodeReader;
import org.graalvm.vm.trcview.arch.CodeReader;
import org.graalvm.vm.trcview.arch.h8s.H8S;
import org.graalvm.vm.trcview.arch.h8s.disasm.H8SDisassembler;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.MemoryEvent;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.util.HexFormatter;

public abstract class H8SStepEvent extends StepEvent {
    private final long step;

    protected H8SStepEvent(int tid, long step) {
        super(tid);
        this.step = step;
    }

    @Override
    public long getStep() {
        return step;
    }

    @Override
    public InstructionType getType() {
        return new H8SDisassembler().getType(new ByteCodeReader(getMachinecode(), getPC(), true));
    }

    @Override
    public byte[] getMachinecode() {
        return getState().getMachinecode();
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
        CodeReader reader = new ByteCodeReader(getMachinecode(), getPC(), true);
        return asm(new H8SDisassembler().getDisassembly(reader));
    }

    @Override
    public String[] getDisassemblyComponents(TraceAnalyzer trc) {
        CodeReader reader = new ByteCodeReader(getMachinecode(), getPC(), true);
        return asm(new H8SDisassembler(trc).getDisassembly(reader));
    }

    private String[] asm(String[] disasm) {
        if (disasm != null) {
            return disasm;
        } else {
            StringBuilder buf = new StringBuilder();
            buf.append("; unknown [");
            byte[] machinecode = getMachinecode();
            if (machinecode.length > 0) {
                buf.append(HexFormatter.tohex(Byte.toUnsignedInt(machinecode[0]), 2));
            } else {
                buf.append("???");
            }
            buf.append(']');
            return new String[]{buf.toString()};
        }
    }

    @Override
    public String getMnemonic() {
        return getDisassemblyComponents()[0];
    }

    @Override
    public long getPC() {
        return getState().getPC();
    }

    @Override
    public List<MemoryEvent> getDataReads() {
        List<MemoryEvent> reads = super.getDataReads();
        List<MemoryEvent> data = new ArrayList<>();
        int len = new H8SDisassembler().getLength(new ByteCodeReader(getMachinecode(), getPC(), true));
        for (MemoryEvent evt : reads) {
            long addr = evt.getAddress();
            if ((addr < getPC()) || (addr >= getPC() + len)) {
                data.add(evt);
            }
        }
        return data;
    }

    @Override
    public abstract H8SCpuState getState();

    @Override
    public StepFormat getFormat() {
        return H8S.FORMAT;
    }
}
