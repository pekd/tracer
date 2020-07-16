package org.graalvm.vm.trcview.arch.x86.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.arch.x86.AMD64;
import org.graalvm.vm.trcview.net.protocol.IO;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;
import org.graalvm.vm.x86.isa.AMD64InstructionQuickInfo;
import org.graalvm.vm.x86.node.debug.trace.StepRecord;

public class AMD64StepEvent extends StepEvent {
    private final StepRecord record;
    private final AMD64CpuState cpuState;

    public AMD64StepEvent(StepRecord record) {
        super(AMD64.ID, record.getTid());
        this.record = record;
        this.cpuState = null;
    }

    public AMD64StepEvent(int tid, byte[] machinecode, AMD64CpuState state) {
        super(AMD64.ID, tid);
        this.record = new StepRecord(machinecode, null) {
            @Override
            public long getPC() {
                return state.getPC();
            }

            @Override
            public long getInstructionCount() {
                return state.getStep();
            }
        };
        this.cpuState = state;
    }

    @Override
    public byte[] getMachinecode() {
        return record.getMachinecode();
    }

    @Override
    public long getPC() {
        if (cpuState != null) {
            return cpuState.getPC();
        } else {
            return record.getPC();
        }
    }

    @Override
    public boolean isCall() {
        return AMD64InstructionQuickInfo.isCall(getMachinecode());
    }

    @Override
    public boolean isReturn() {
        return AMD64InstructionQuickInfo.isRet(getMachinecode());
    }

    @Override
    public boolean isSyscall() {
        return AMD64InstructionQuickInfo.isSyscall(getMachinecode());
    }

    @Override
    public boolean isReturnFromSyscall() {
        return false;
    }

    @Override
    public InstructionType getType() {
        if (getMachinecode() == null) {
            return InstructionType.OTHER;
        }
        switch (AMD64InstructionQuickInfo.getType(getMachinecode())) {
            case CALL:
                return InstructionType.CALL;
            case RET:
                return InstructionType.RET;
            case JMP:
                return InstructionType.JMP;
            case JMP_INDIRECT:
                return InstructionType.JMP_INDIRECT;
            case JCC:
                return InstructionType.JCC;
            case SYSCALL:
                return InstructionType.SYSCALL;
            default:
                return InstructionType.OTHER;
        }
    }

    @Override
    public long getStep() {
        if (cpuState != null) {
            return cpuState.getStep();
        } else {
            return record.getInstructionCount();
        }
    }

    @Override
    public String getDisassembly() {
        return record.getDisassembly();
    }

    @Override
    public String[] getDisassemblyComponents() {
        return record.getDisassemblyComponents();
    }

    @Override
    public String getMnemonic() {
        return record.getMnemonic();
    }

    @Override
    public AMD64CpuState getState() {
        if (cpuState != null) {
            return cpuState;
        } else {
            return new AMD64CpuState(record.getState());
        }
    }

    @Override
    public StepFormat getFormat() {
        return AMD64.FORMAT;
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        IO.writeArray(out, getMachinecode());
        getState().writeRecord(out);
    }

    public static AMD64StepEvent readRecord(WordInputStream in, int tid) throws IOException {
        byte[] machinecode = IO.readArray(in);
        AMD64CpuState state = AMD64CpuState.readRecord(in, tid);
        return new AMD64StepEvent(tid, machinecode, state);
    }

    public static AMD64StepEvent create(WordInputStream in, int tid, byte[] machinecode) throws IOException {
        AMD64CpuState state = AMD64CpuState.readRecord(in, tid);
        return new AMD64StepEvent(tid, machinecode, state);
    }

    @Override
    public String toString() {
        return record.toString();
    }
}
