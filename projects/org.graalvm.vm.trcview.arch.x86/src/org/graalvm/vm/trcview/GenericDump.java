package org.graalvm.vm.trcview;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.graalvm.vm.memory.vector.Vector128;
import org.graalvm.vm.trcview.libtrc.GenericTrace;
import org.graalvm.vm.trcview.libtrc.ProgramCounter;
import org.graalvm.vm.trcview.libtrc.Register;
import org.graalvm.vm.trcview.libtrc.StateFormat;
import org.graalvm.vm.x86.isa.AMD64InstructionQuickInfo;
import org.graalvm.vm.x86.isa.CpuState;
import org.graalvm.vm.x86.node.debug.trace.EofRecord;
import org.graalvm.vm.x86.node.debug.trace.ExecutionTraceReader;
import org.graalvm.vm.x86.node.debug.trace.MemoryEventRecord;
import org.graalvm.vm.x86.node.debug.trace.MmapRecord;
import org.graalvm.vm.x86.node.debug.trace.MunmapRecord;
import org.graalvm.vm.x86.node.debug.trace.Record;
import org.graalvm.vm.x86.node.debug.trace.StepRecord;

public class GenericDump {
    @StateFormat("RAX=${rax;x16} RBX=${rbx;x16} RCX=${rcx;x16} RDX=${rdx;x16}\n" +
                    "RSI=${rsi;x16} RDI=${rdi;x16} RBP=${rbp;x16} RSP=${rsp;x16}\n" +
                    "R8 =${r8;x16} R9 =${r9;x16} R10=${r10;x16} R11=${r11;x16}\n" +
                    "R12=${r12;x16} R13=${r13;x16} R14=${r14;x16} R15=${r15;x16}\n" +
                    "RIP=${rip;x16}")
    public static class State {
        @Register public long rax;
        @Register public long rbx;
        @Register public long rcx;
        @Register public long rdx;
        @Register public long rbp;
        @Register public long rsp;
        @Register public long rsi;
        @Register public long rdi;
        @Register public long r8;
        @Register public long r9;
        @Register public long r10;
        @Register public long r11;
        @Register public long r12;
        @Register public long r13;
        @Register public long r14;
        @Register public long r15;
        @Register public long rfl;

        @ProgramCounter @Register public long rip;
    }

    public static void main(String[] args) throws IOException {
        try (InputStream in = new FileInputStream(args[0]);
                        OutputStream out = new FileOutputStream(args[1])) {
            byte[] header = new byte[6];
            in.read(header);
            ExecutionTraceReader reader = new ExecutionTraceReader(in);
            GenericTrace<State> trc = new GenericTrace<>(out, State.class);
            trc.setLittleEndian();
            while (true) {
                Record record = reader.read();
                if (record == null) {
                    break;
                }
                if (record instanceof StepRecord) {
                    StepRecord step = (StepRecord) record;
                    State state = new State();
                    CpuState cpustate = step.getState().getState();
                    state.rax = cpustate.rax;
                    state.rbx = cpustate.rbx;
                    state.rcx = cpustate.rcx;
                    state.rdx = cpustate.rdx;
                    state.rbp = cpustate.rbp;
                    state.rsp = cpustate.rsp;
                    state.rsi = cpustate.rsi;
                    state.rdi = cpustate.rdi;
                    state.r8 = cpustate.r8;
                    state.r9 = cpustate.r9;
                    state.r10 = cpustate.r10;
                    state.r11 = cpustate.r11;
                    state.r12 = cpustate.r12;
                    state.r13 = cpustate.r13;
                    state.r14 = cpustate.r14;
                    state.r15 = cpustate.r15;
                    state.rip = cpustate.rip;
                    state.rfl = cpustate.getRFL();
                    byte type;
                    switch (AMD64InstructionQuickInfo.getType(step.getMachinecode())) {
                        default:
                        case OTHER:
                            type = GenericTrace.TYPE_OTHER;
                            break;
                        case JCC:
                            type = GenericTrace.TYPE_JCC;
                            break;
                        case JMP:
                            type = GenericTrace.TYPE_JMP;
                            break;
                        case JMP_INDIRECT:
                            type = GenericTrace.TYPE_JMP_INDIRECT;
                            break;
                        case CALL:
                            type = GenericTrace.TYPE_CALL;
                            break;
                        case RET:
                            type = GenericTrace.TYPE_RET;
                            break;
                        case SYSCALL:
                            type = GenericTrace.TYPE_SYSCALL;
                            break;
                    }
                    trc.step(step.getTid(), step.getInstructionCount(), state, step.getDisassemblyComponents(), step.getMachinecode(), type);
                } else if (record instanceof MmapRecord) {
                    MmapRecord mmap = (MmapRecord) record;
                    trc.mmap(mmap.getTid(), mmap.getAddress(), mmap.getLength(), mmap.getProtection(), mmap.getFlags(), mmap.getFlags(), mmap.getFileDescriptor(), mmap.getResult(),
                                    mmap.getFilename());
                } else if (record instanceof MunmapRecord) {
                    MunmapRecord munmap = (MunmapRecord) record;
                    trc.munmap(munmap.getTid(), munmap.getAddress(), munmap.getLength(), munmap.getResult());
                } else if (record instanceof MemoryEventRecord) {
                    MemoryEventRecord evt = (MemoryEventRecord) record;
                    switch (evt.getSize()) {
                        case 1:
                            if (evt.isWrite()) {
                                trc.writeI8(evt.getTid(), evt.getAddress(), (byte) evt.getValue());
                            } else if (evt.hasData()) {
                                trc.readI8(evt.getTid(), evt.getAddress(), (byte) evt.getValue());
                            } else {
                                trc.readI8(evt.getTid(), evt.getAddress());
                            }
                            break;
                        case 2:
                            if (evt.isWrite()) {
                                trc.writeI16(evt.getTid(), evt.getAddress(), (short) evt.getValue());
                            } else if (evt.hasData()) {
                                trc.readI16(evt.getTid(), evt.getAddress(), (short) evt.getValue());
                            } else {
                                trc.readI16(evt.getTid(), evt.getAddress());
                            }
                            break;
                        case 4:
                            if (evt.isWrite()) {
                                trc.writeI32(evt.getTid(), evt.getAddress(), (int) evt.getValue());
                            } else if (evt.hasData()) {
                                trc.readI32(evt.getTid(), evt.getAddress(), (int) evt.getValue());
                            } else {
                                trc.readI32(evt.getTid(), evt.getAddress());
                            }
                            break;
                        case 8:
                            if (evt.isWrite()) {
                                trc.writeI64(evt.getTid(), evt.getAddress(), evt.getValue());
                            } else if (evt.hasData()) {
                                trc.readI64(evt.getTid(), evt.getAddress(), evt.getValue());
                            } else {
                                trc.readI64(evt.getTid(), evt.getAddress());
                            }
                            break;
                        case 16:
                            if (evt.isWrite()) {
                                Vector128 vec = evt.getVector();
                                trc.writeI64(evt.getTid(), evt.getAddress(), vec.getI64(1));
                                trc.writeI64(evt.getTid(), evt.getAddress() + 8, vec.getI64(0));
                            } else if (evt.hasData()) {
                                Vector128 vec = evt.getVector();
                                trc.readI64(evt.getTid(), evt.getAddress(), vec.getI16(1));
                                trc.readI64(evt.getTid(), evt.getAddress() + 8, vec.getI16(0));
                            } else {
                                trc.readI64(evt.getTid(), evt.getAddress());
                                trc.readI64(evt.getTid(), evt.getAddress() + 8);
                            }
                            break;
                    }
                } else if (record instanceof EofRecord) {
                    break;
                }
            }
        }
    }
}
