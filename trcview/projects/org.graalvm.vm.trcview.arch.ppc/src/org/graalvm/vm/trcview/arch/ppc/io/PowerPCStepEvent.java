package org.graalvm.vm.trcview.arch.ppc.io;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.arch.ppc.PowerPC;
import org.graalvm.vm.trcview.arch.ppc.disasm.PowerPCDisassembler;
import org.graalvm.vm.trcview.arch.ppc.disasm.PowerPCRegisterUsage;
import org.graalvm.vm.trcview.arch.ppc.disasm.PowerPCSemantics;
import org.graalvm.vm.trcview.data.Semantics;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.util.io.Endianess;

public abstract class PowerPCStepEvent extends StepEvent {
    InstructionType type = null;

    protected PowerPCStepEvent(int tid) {
        super(tid);
    }

    @Override
    public byte[] getMachinecode() {
        byte[] opcd = new byte[4];
        Endianess.set32bitBE(opcd, getState().getInstruction());
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
        return new PowerPCDisassembler().disassemble((int) getState().getPC(), getState().getInstruction());
    }

    @Override
    public String[] getDisassemblyComponents(TraceAnalyzer trc) {
        return new PowerPCDisassembler(trc).disassemble((int) getState().getPC(), getState().getInstruction());
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
            return PowerPCDisassembler.getType(getState(), getState().getInstruction());
        }
    }

    @Override
    public abstract PowerPCCpuState getState();

    @Override
    public StepFormat getFormat() {
        return PowerPC.FORMAT;
    }

    @Override
    public void getSemantics(Semantics s) {
        PowerPCSemantics.getSemantics(s, getState().getInstruction());
    }

    @Override
    public int[] getRegisterReads() {
        return PowerPCRegisterUsage.getRegisterUsage(getState().getInstruction(), true);
    }

    @Override
    public int[] getRegisterWrites() {
        return PowerPCRegisterUsage.getRegisterUsage(getState().getInstruction(), false);
    }
}
