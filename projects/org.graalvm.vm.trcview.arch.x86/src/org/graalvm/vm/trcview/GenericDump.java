package org.graalvm.vm.trcview;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
import org.graalvm.vm.x86.node.debug.trace.MemoryDumpRecord;
import org.graalvm.vm.x86.node.debug.trace.MemoryEventRecord;
import org.graalvm.vm.x86.node.debug.trace.MmapRecord;
import org.graalvm.vm.x86.node.debug.trace.MunmapRecord;
import org.graalvm.vm.x86.node.debug.trace.Record;
import org.graalvm.vm.x86.node.debug.trace.StepRecord;
import org.graalvm.vm.x86.node.debug.trace.SymbolTableRecord;

public class GenericDump {
    @StateFormat("RAX=${rax} RBX=${rbx} RCX=${rcx} RDX=${rdx}\n" +
                    "RSI=${rsi} RDI=${rdi} RBP=${rbp} RSP=${rsp}\n" +
                    "R8 =${r8} R9 =${r9} R10=${r10} R11=${r11}\n" +
                    "R12=${r12} R13=${r13} R14=${r14} R15=${r15}\n" +
                    "RIP=${rip}\n" +
                    "XMM0=${xmm0H}${xmm0L} XMM8 =${xmm8H}${xmm8L}\n" +
                    "XMM1=${xmm1H}${xmm1L} XMM9 =${xmm9H}${xmm9L}\n" +
                    "XMM2=${xmm2H}${xmm2L} XMM10=${xmm10H}${xmm10L}\n" +
                    "XMM3=${xmm3H}${xmm3L} XMM11=${xmm11H}${xmm11L}\n" +
                    "XMM4=${xmm4H}${xmm4L} XMM12=${xmm12H}${xmm12L}\n" +
                    "XMM5=${xmm5H}${xmm5L} XMM13=${xmm13H}${xmm13L}\n" +
                    "XMM6=${xmm6H}${xmm6L} XMM14=${xmm14H}${xmm14L}\n" +
                    "XMM7=${xmm7H}${xmm7L} XMM15=${xmm15H}${xmm15L}")
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
        @Register public long xmm0H;
        @Register public long xmm0L;
        @Register public long xmm1H;
        @Register public long xmm1L;
        @Register public long xmm2H;
        @Register public long xmm2L;
        @Register public long xmm3H;
        @Register public long xmm3L;
        @Register public long xmm4H;
        @Register public long xmm4L;
        @Register public long xmm5H;
        @Register public long xmm5L;
        @Register public long xmm6H;
        @Register public long xmm6L;
        @Register public long xmm7H;
        @Register public long xmm7L;
        @Register public long xmm8H;
        @Register public long xmm8L;
        @Register public long xmm9H;
        @Register public long xmm9L;
        @Register public long xmm10H;
        @Register public long xmm10L;
        @Register public long xmm11H;
        @Register public long xmm11L;
        @Register public long xmm12H;
        @Register public long xmm12L;
        @Register public long xmm13H;
        @Register public long xmm13L;
        @Register public long xmm14H;
        @Register public long xmm14L;
        @Register public long xmm15H;
        @Register public long xmm15L;

        @ProgramCounter @Register public long rip;
    }

    public static void main(String[] args) throws IOException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(args[0]));
                        OutputStream out = new BufferedOutputStream(new FileOutputStream(args[1]))) {
            byte[] header = new byte[6];
            in.read(header);
            ExecutionTraceReader reader = new ExecutionTraceReader(in);
            GenericTrace<State> trc = new GenericTrace<>(out, State.class);
            trc.setLittleEndian();
            long steps = 0;
            while (true) {
                Record record = reader.read();
                if (record == null) {
                    break;
                }
                if (record instanceof StepRecord) {
                    steps++;
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
                    state.xmm0H = cpustate.xmm[0].getI64(0);
                    state.xmm0L = cpustate.xmm[0].getI64(1);
                    state.xmm1H = cpustate.xmm[1].getI64(0);
                    state.xmm1L = cpustate.xmm[1].getI64(1);
                    state.xmm2H = cpustate.xmm[2].getI64(0);
                    state.xmm2L = cpustate.xmm[2].getI64(1);
                    state.xmm3H = cpustate.xmm[3].getI64(0);
                    state.xmm3L = cpustate.xmm[3].getI64(1);
                    state.xmm4H = cpustate.xmm[4].getI64(0);
                    state.xmm4L = cpustate.xmm[4].getI64(1);
                    state.xmm5H = cpustate.xmm[5].getI64(0);
                    state.xmm5L = cpustate.xmm[5].getI64(1);
                    state.xmm6H = cpustate.xmm[6].getI64(0);
                    state.xmm6L = cpustate.xmm[6].getI64(1);
                    state.xmm7H = cpustate.xmm[7].getI64(0);
                    state.xmm7L = cpustate.xmm[7].getI64(1);
                    state.xmm8H = cpustate.xmm[8].getI64(0);
                    state.xmm8L = cpustate.xmm[8].getI64(1);
                    state.xmm9H = cpustate.xmm[9].getI64(0);
                    state.xmm9L = cpustate.xmm[9].getI64(1);
                    state.xmm10H = cpustate.xmm[10].getI64(0);
                    state.xmm10L = cpustate.xmm[10].getI64(1);
                    state.xmm11H = cpustate.xmm[11].getI64(0);
                    state.xmm11L = cpustate.xmm[11].getI64(1);
                    state.xmm12H = cpustate.xmm[12].getI64(0);
                    state.xmm12L = cpustate.xmm[12].getI64(1);
                    state.xmm13H = cpustate.xmm[13].getI64(0);
                    state.xmm13L = cpustate.xmm[13].getI64(1);
                    state.xmm14H = cpustate.xmm[14].getI64(0);
                    state.xmm14L = cpustate.xmm[14].getI64(1);
                    state.xmm15H = cpustate.xmm[15].getI64(0);
                    state.xmm15L = cpustate.xmm[15].getI64(1);
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
                    if (mmap.getData() != null && mmap.getResult() != -1) {
                        trc.dump(mmap.getTid(), mmap.getResult(), mmap.getData());
                    }
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
                } else if (record instanceof SymbolTableRecord) {
                    SymbolTableRecord symbols = (SymbolTableRecord) record;
                    trc.symbols(symbols.getTid(), symbols.getLoadBias(), symbols.getAddress(), symbols.getSize(), symbols.getFilename(), symbols.getSymbols().values());
                } else if (record instanceof MemoryDumpRecord) {
                    MemoryDumpRecord dump = (MemoryDumpRecord) record;
                    trc.dump(dump.getTid(), dump.getAddress(), dump.getData());
                } else if (record instanceof EofRecord) {
                    break;
                }
            }
            System.out.println(steps + " steps");
        }
    }
}
