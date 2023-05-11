package org.graalvm.vm.trcview.arch.riscv.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.MemoryEvent;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.arch.riscv.RiscV;
import org.graalvm.vm.trcview.arch.riscv.disasm.RiscVDisassembler;
import org.graalvm.vm.util.io.Endianess;

public abstract class RiscVStepEvent extends StepEvent {
    InstructionType type = null;

    protected RiscVStepEvent(int tid) {
        super(tid);
    }

    @Override
    public byte[] getMachinecode() {
        int insn = getState().getInstruction();
        int len = RiscVDisassembler.getSize(insn);

        byte[] opcd = new byte[len];
        if (len == 2) {
            Endianess.set16bitBE(opcd, (short) insn);
        } else if (len == 4) {
            Endianess.set32bitBE(opcd, insn);
        } else {
            throw new IllegalStateException("invalid instruction size");
        }
        return opcd;
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
        return new RiscVDisassembler().disassemble((int) getState().getPC(), getState().getInstruction()).getComponents();
    }

    @Override
    public String getMnemonic() {
        return getDisassemblyComponents()[0];
    }

    @Override
    public InstructionType getType() {
        if (type != null) {
            return type;
        } else {
            return RiscVDisassembler.getType(getState().getInstruction());
        }
    }

    @Override
    public abstract RiscVCpuState getState();

    @Override
    public StepFormat getFormat() {
        return RiscV.FORMAT;
    }

    @Override
    public List<MemoryEvent> getDataReads() {
        if (RiscVDisassembler.getType(getState().getInstruction()) != InstructionType.OTHER) {
            // only "OTHER" instructions can be load/store
            return Collections.emptyList();
        }

        List<MemoryEvent> reads = super.getDataReads();
        List<MemoryEvent> data = new ArrayList<>();
        int len = RiscVDisassembler.getSize(getState().getInstruction());
        long npc = getPC() + len;
        for (MemoryEvent evt : reads) {
            long addr = evt.getAddress();
            if (addr != npc) {
                data.add(evt);
            }
        }
        return data;
    }
}
