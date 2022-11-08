package org.graalvm.vm.trcview.arch.ppc.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;

import org.graalvm.vm.posix.api.mem.Mman;
import org.graalvm.vm.trcview.arch.io.ArchTraceReader;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.GenericMemoryEvent;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.MemoryDumpEvent;
import org.graalvm.vm.trcview.arch.io.MmapEvent;
import org.graalvm.vm.trcview.arch.io.SymbolTableEvent;
import org.graalvm.vm.trcview.arch.ppc.disasm.InstructionFormat;
import org.graalvm.vm.trcview.arch.ppc.disasm.Opcode;
import org.graalvm.vm.trcview.io.StandardSymbolTable;
import org.graalvm.vm.trcview.net.protocol.IO;
import org.graalvm.vm.util.io.BEInputStream;
import org.graalvm.vm.util.io.WordInputStream;

public class PowerPCTraceReader extends ArchTraceReader {
    private static final byte TYPE_SYMBOLS = 0;
    private static final byte TYPE_MMAP = 1;
    private static final byte TYPE_DUMP = 2;
    private static final byte TYPE_MEMR8 = 3;
    private static final byte TYPE_MEMR16 = 4;
    private static final byte TYPE_MEMR32 = 5;
    private static final byte TYPE_MEMR64 = 6;
    private static final byte TYPE_MEMW8 = 7;
    private static final byte TYPE_MEMW16 = 8;
    private static final byte TYPE_MEMW32 = 9;
    private static final byte TYPE_MEMW64 = 10;
    private static final byte TYPE_STEP = 11;
    private static final byte TYPE_TRAP = 12;

    private final WordInputStream in;
    private PowerPCStepEvent lastStep;

    // used to detect mtsrr0/rfi/mtsrr0/rfi sequences
    private static final InstructionFormat insn = new InstructionFormat();
    private Deque<Integer> trapstack = new ArrayDeque<>();

    private int fullstate = 0;

    public PowerPCTraceReader(InputStream in) {
        this(new BEInputStream(in));
    }

    public PowerPCTraceReader(WordInputStream in) {
        this.in = in;
        lastStep = null;
    }

    private static MmapEvent mmap(int start, int end, String name) {
        return new MmapEvent(0, Integer.toUnsignedLong(start), Integer.toUnsignedLong(end - start + 1),
                        Mman.PROT_READ | Mman.PROT_WRITE | Mman.PROT_EXEC,
                        Mman.MAP_FIXED | Mman.MAP_PRIVATE | Mman.MAP_ANONYMOUS, -1, 0, name,
                        Integer.toUnsignedLong(start), null);
    }

    private void checkTrap(PowerPCStepEvent step) {
        int opcd = step.getState().getInstruction();
        switch (insn.OPCD.get(opcd)) {
            case Opcode.CR_OPS:
                switch (insn.XO_1.get(opcd)) {
                    case Opcode.XO_RFI:
                        if (trapstack.isEmpty()) {
                            PowerPCCpuState state = step.getState();
                            if (state.getSRR0() == state.getLR()) {
                                step.type = InstructionType.RET;
                            } else {
                                step.type = InstructionType.JMP_INDIRECT;
                            }
                        } else if (step.getState().getSRR0() == trapstack.peek()) {
                            trapstack.pop();
                        } else {
                            step.type = InstructionType.JMP_INDIRECT;
                        }
                        return;
                }
                break;
        }
    }

    @Override
    public Event read() throws IOException {
        int type;
        try {
            type = in.read8bit();
        } catch (EOFException e) {
            return null;
        }
        switch (type) {
            case TYPE_STEP:
                PowerPCCpuState lastState = lastStep == null ? new PowerPCZeroCpuState(0) : lastStep.getState();
                PowerPCCpuState cpu = new PowerPCDeltaCpuState(in, 0, lastState);
                if ((fullstate % 500) == 0) {
                    lastStep = new PowerPCFullCpuState(cpu);
                } else {
                    lastStep = cpu;
                }
                fullstate++;
                fullstate %= 500;
                checkTrap(lastStep);
                return lastStep;
            case TYPE_TRAP:
                PowerPCExceptionEvent trap = new PowerPCExceptionEvent(in, 0, lastStep);
                trapstack.push(trap.getSRR0());
                return trap;
            case TYPE_MEMR8: {
                long address = Integer.toUnsignedLong(in.read32bit());
                long value = Byte.toUnsignedLong((byte) in.read8bit());
                return new GenericMemoryEvent(true, 0, address, (byte) 1, false, value);
            }
            case TYPE_MEMR16: {
                long address = Integer.toUnsignedLong(in.read32bit());
                long value = Short.toUnsignedLong(in.read16bit());
                return new GenericMemoryEvent(true, 0, address, (byte) 2, false, value);
            }
            case TYPE_MEMR32: {
                long address = Integer.toUnsignedLong(in.read32bit());
                long value = Integer.toUnsignedLong(in.read32bit());
                return new GenericMemoryEvent(true, 0, address, (byte) 4, false, value);
            }
            case TYPE_MEMR64: {
                long address = Integer.toUnsignedLong(in.read32bit());
                long value = in.read64bit();
                return new GenericMemoryEvent(true, 0, address, (byte) 8, false, value);
            }
            case TYPE_MEMW8: {
                long address = Integer.toUnsignedLong(in.read32bit());
                long value = Byte.toUnsignedLong((byte) in.read8bit());
                return new GenericMemoryEvent(true, 0, address, (byte) 1, true, value);
            }
            case TYPE_MEMW16: {
                long address = Integer.toUnsignedLong(in.read32bit());
                long value = Short.toUnsignedLong(in.read16bit());
                return new GenericMemoryEvent(true, 0, address, (byte) 2, true, value);
            }
            case TYPE_MEMW32: {
                long address = Integer.toUnsignedLong(in.read32bit());
                long value = Integer.toUnsignedLong(in.read32bit());
                return new GenericMemoryEvent(true, 0, address, (byte) 4, true, value);
            }
            case TYPE_MEMW64: {
                long address = Integer.toUnsignedLong(in.read32bit());
                long value = in.read64bit();
                return new GenericMemoryEvent(true, 0, address, (byte) 8, true, value);
            }
            case TYPE_DUMP: {
                long address = Integer.toUnsignedLong(in.read32bit());
                byte[] data = IO.readArray(in);
                return new MemoryDumpEvent(0, address, data);
            }
            case TYPE_MMAP: {
                int start = in.read32bit();
                int end = in.read32bit();
                String name = IO.readString(in);
                return mmap(start, end, name);
            }
            case TYPE_SYMBOLS: {
                StandardSymbolTable symtab = new StandardSymbolTable();
                symtab.read32(in);
                return new SymbolTableEvent(0, symtab.getSymbols(), symtab.getFilename(), symtab.getLoadBias(), symtab.getAddress(), symtab.getSize());
            }
            default:
                throw new IOException("unknown record type: " + type + " [position " + tell() + "]");
        }
    }

    @Override
    public long tell() {
        return in.tell();
    }
}
