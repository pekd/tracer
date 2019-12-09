package org.graalvm.vm.trcview.arch.pdp11.io;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.arch.pdp11.PDP11;
import org.graalvm.vm.trcview.arch.pdp11.disasm.PDP11Disassembler;
import org.graalvm.vm.util.io.Endianess;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class PDP11StepEvent extends StepEvent {
    private final PDP11CpuState cpuState;
    private byte[] machinecode = null;

    public PDP11StepEvent(WordInputStream in, int tid) throws IOException {
        super(PDP11.ID, tid);
        cpuState = new PDP11CpuState(in, tid);
    }

    @Override
    public byte[] getMachinecode() {
        if (machinecode != null) {
            return machinecode;
        }

        short[] code = cpuState.getMachinecode();
        int length = PDP11Disassembler.getLength(code);
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
        return PDP11Disassembler.getDisassembly(cpuState.getMachinecode(), (short) cpuState.getPC());
    }

    @Override
    public String getMnemonic() {
        return getDisassemblyComponents()[0];
    }

    @Override
    public long getPC() {
        return cpuState.getPC();
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
        return PDP11Disassembler.getType(cpuState.getMachinecode()[0]);
    }

    @Override
    public long getStep() {
        return cpuState.getStep();
    }

    @Override
    public CpuState getState() {
        return cpuState;
    }

    @Override
    public StepFormat getFormat() {
        return PDP11.FORMAT;
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        cpuState.writeRecord(out);
    }

    @Override
    public String toString() {
        return String.format("%06o: %s ; step %d", getPC(), getDisassembly(), getStep());
    }
}
