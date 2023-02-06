package org.graalvm.vm.trcview.arch.io;

import static org.graalvm.vm.trcview.disasm.Type.OTHER;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.graalvm.vm.trcview.data.Semantics;
import org.graalvm.vm.trcview.disasm.AssemblerInstruction;
import org.graalvm.vm.trcview.disasm.Operand;
import org.graalvm.vm.trcview.disasm.Token;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public abstract class StepEvent extends Event {
    public MemoryEvent read;
    public MemoryEvent write;

    protected StepEvent(int tid) {
        super(tid);
    }

    public abstract byte[] getMachinecode();

    public String getDisassembly() {
        String[] code = getDisassemblyComponents();
        if (code.length == 1) {
            return code[0];
        } else {
            return code[0] + "\t" + Stream.of(code).skip(1).collect(Collectors.joining(",\t"));
        }
    }

    public abstract String[] getDisassemblyComponents();

    public String[] getDisassemblyComponents(@SuppressWarnings("unused") TraceAnalyzer trc) {
        return getDisassemblyComponents();
    }

    public abstract String getMnemonic();

    public AssemblerInstruction disassemble(TraceAnalyzer trc) {
        String[] parts = getDisassemblyComponents(trc);
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

    public abstract long getPC();

    public boolean isCall() {
        return getType() == InstructionType.CALL;
    }

    public boolean isReturn() {
        return getType() == InstructionType.RET;
    }

    public boolean isSyscall() {
        return getType() == InstructionType.SYSCALL;
    }

    public boolean isReturnFromSyscall() {
        return getType() == InstructionType.RTI;
    }

    public abstract InstructionType getType();

    public abstract long getStep();

    public abstract CpuState getState();

    public abstract StepFormat getFormat();

    public void getSemantics(@SuppressWarnings("unused") Semantics s) {
        // override this in subclasses to supply semantic information
    }

    public final void setRead(MemoryEvent evt) {
        assert !evt.isWrite();
        read = evt;
    }

    public final void addRead(MemoryEvent evt) {
        assert !evt.isWrite();
        if (read != evt) {
            evt.setNext(read);
            read = evt;
        }
    }

    public final MemoryEvent getRead() {
        return read;
    }

    public List<MemoryEvent> getDataReads() {
        List<MemoryEvent> result = new ArrayList<>();
        MemoryEvent evt = getRead();
        while (evt != null) {
            result.add(evt);
            evt = evt.getNext();
        }
        return result;
    }

    public List<MemoryEvent> getDataWrites() {
        List<MemoryEvent> result = new ArrayList<>();
        MemoryEvent evt = getWrite();
        while (evt != null) {
            result.add(evt);
            evt = evt.getNext();
        }
        return result;
    }

    public final void setWrite(MemoryEvent evt) {
        assert evt.isWrite();
        write = evt;
    }

    public final void addWrite(MemoryEvent evt) {
        assert evt.isWrite();
        if (write != evt) {
            evt.setNext(write);
            write = evt;
        }
    }

    public final MemoryEvent getWrite() {
        return write;
    }

    public int[] getRegisterReads() {
        return new int[0];
    }

    public int[] getRegisterWrites() {
        return new int[0];
    }

    @Override
    public int hashCode() {
        long hash = (getStep() ^ getPC());
        return (int) (hash ^ (hash >>> 32));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof StepEvent)) {
            return false;
        }
        StepEvent s = (StepEvent) o;
        return s.getStep() == getStep() && s.getPC() == getPC();
    }
}
