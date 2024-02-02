package org.graalvm.vm.trcview.arch.z80.io;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.MemoryEvent;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.arch.z80.Flags;
import org.graalvm.vm.trcview.arch.z80.Z80;
import org.graalvm.vm.trcview.arch.z80.disasm.Z80Disassembler;
import org.graalvm.vm.trcview.arch.z80.disasm.Z80Instruction;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public abstract class Z80StepEvent extends StepEvent {
    private final long step;

    protected Z80StepEvent(int tid, long step) {
        super(tid);
        this.step = step;
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
        return new Z80Disassembler().getDisassembly(getState().getMachinecode(), (short) getPC());
    }

    @Override
    public String[] getDisassemblyComponents(TraceAnalyzer trc) {
        return new Z80Disassembler(trc).getDisassembly(getState().getMachinecode(), (short) getPC());
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
    public InstructionType getType() {
        InstructionType type = Z80Disassembler.getType(getState().getMachinecode());
        if (type == InstructionType.RET) {
            // check if this is a conditional RET
            int cond = Z80Disassembler.getCondition(getState().getMachinecode());
            if (cond == Z80Instruction.ALWAYS || Flags.condition(cond, getState().getF())) {
                return type;
            } else {
                return InstructionType.JCC;
            }
        } else if (type == InstructionType.CALL) {
            // check if this is a conditional CALL
            int cond = Z80Disassembler.getCondition(getState().getMachinecode());
            if (cond == Z80Instruction.ALWAYS || Flags.condition(cond, getState().getF())) {
                return type;
            } else {
                return InstructionType.JCC;
            }
        }
        return type;
    }

    @Override
    public long getStep() {
        return step;
    }

    @Override
    public List<MemoryEvent> getDataReads() {
        List<MemoryEvent> reads = super.getDataReads();
        List<MemoryEvent> data = new ArrayList<>();
        int len = Z80Disassembler.getLength(getState().getMachinecode());
        for (MemoryEvent evt : reads) {
            long addr = evt.getAddress();
            if ((addr < getPC()) || (addr >= getPC() + len)) {
                data.add(evt);
            }
        }
        return data;
    }

    @Override
    public abstract Z80CpuState getState();

    @Override
    public StepFormat getFormat() {
        return Z80.FORMAT;
    }

}
