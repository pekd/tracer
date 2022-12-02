package org.graalvm.vm.trcview.arch.ppc.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;

import org.graalvm.vm.posix.api.mem.Mman;
import org.graalvm.vm.trcview.arch.io.ArchTraceReader;
import org.graalvm.vm.trcview.arch.io.BrkEvent;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.GenericMemoryEvent;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.MemoryDumpEvent;
import org.graalvm.vm.trcview.arch.io.MmapEvent;
import org.graalvm.vm.trcview.arch.io.MprotectEvent;
import org.graalvm.vm.trcview.arch.io.MunmapEvent;
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
    private static final byte TYPE_MMAP2 = 2;
    private static final byte TYPE_MUNMAP = 3;
    private static final byte TYPE_MPROTECT = 4;
    private static final byte TYPE_BRK = 5;
    private static final byte TYPE_DUMP = 6;
    private static final byte TYPE_MEM32R8 = 7;
    private static final byte TYPE_MEM32R16 = 8;
    private static final byte TYPE_MEM32R32 = 9;
    private static final byte TYPE_MEM32R64 = 10;
    private static final byte TYPE_MEM32W8 = 11;
    private static final byte TYPE_MEM32W16 = 12;
    private static final byte TYPE_MEM32W32 = 13;
    private static final byte TYPE_MEM32W64 = 14;
    private static final byte TYPE_MEM32R8F = 15;
    private static final byte TYPE_MEM32R16F = 16;
    private static final byte TYPE_MEM32R32F = 17;
    private static final byte TYPE_MEM32R64F = 18;
    private static final byte TYPE_MEM32W8F = 19;
    private static final byte TYPE_MEM32W16F = 20;
    private static final byte TYPE_MEM32W32F = 21;
    private static final byte TYPE_MEM32W64F = 22;
    private static final byte TYPE_MEM64R8 = 23;
    private static final byte TYPE_MEM64R16 = 24;
    private static final byte TYPE_MEM64R32 = 25;
    private static final byte TYPE_MEM64R64 = 26;
    private static final byte TYPE_MEM64W8 = 27;
    private static final byte TYPE_MEM64W16 = 28;
    private static final byte TYPE_MEM64W32 = 29;
    private static final byte TYPE_MEM64W64 = 30;
    private static final byte TYPE_MEM64R8F = 31;
    private static final byte TYPE_MEM64R16F = 32;
    private static final byte TYPE_MEM64R32F = 33;
    private static final byte TYPE_MEM64R64F = 34;
    private static final byte TYPE_MEM64W8F = 35;
    private static final byte TYPE_MEM64W16F = 36;
    private static final byte TYPE_MEM64W32F = 37;
    private static final byte TYPE_MEM64W64F = 38;
    private static final byte TYPE_STEP = 39;
    private static final byte TYPE_TRAP = 40;

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

    private static MmapEvent mmap(long start, long end, String name) {
        return new MmapEvent(0, start, end - start + 1,
                        Mman.PROT_READ | Mman.PROT_WRITE | Mman.PROT_EXEC,
                        Mman.MAP_FIXED | Mman.MAP_PRIVATE | Mman.MAP_ANONYMOUS, -1, 0, name,
                        start, null);
    }

    private void checkTrap(PowerPCStepEvent step) {
        int opcd = step.getState().getInstruction();

        switch (insn.OPCD.get(opcd)) {
            case Opcode.BC: {
                // treat bl .+4 and bl .+8 as normal branch
                int bo = insn.BO.get(opcd);
                int bd = insn.BD.get(opcd) << 2;
                boolean aa = insn.AA.getBit(opcd);
                boolean lk = insn.LK.getBit(opcd);
                long bta = Integer.toUnsignedLong(aa ? bd : ((int) step.getPC() + bd));
                if (lk && (bta == step.getPC() + 4 || bta == step.getPC() + 8) && (bo & 0b10100) == 0b10100) {
                    step.type = InstructionType.JMP;
                }
                break;
            }
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
                        break;
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
            case TYPE_MEM32R8: {
                long address = Integer.toUnsignedLong(in.read32bit());
                long value = Byte.toUnsignedLong((byte) in.read8bit());
                return new GenericMemoryEvent(true, 0, address, (byte) 1, false, value);
            }
            case TYPE_MEM32R16: {
                long address = Integer.toUnsignedLong(in.read32bit());
                long value = Short.toUnsignedLong(in.read16bit());
                return new GenericMemoryEvent(true, 0, address, (byte) 2, false, value);
            }
            case TYPE_MEM32R32: {
                long address = Integer.toUnsignedLong(in.read32bit());
                long value = Integer.toUnsignedLong(in.read32bit());
                return new GenericMemoryEvent(true, 0, address, (byte) 4, false, value);
            }
            case TYPE_MEM32R64: {
                long address = Integer.toUnsignedLong(in.read32bit());
                long value = in.read64bit();
                return new GenericMemoryEvent(true, 0, address, (byte) 8, false, value);
            }
            case TYPE_MEM32W8F:
            case TYPE_MEM32W8: {
                long address = Integer.toUnsignedLong(in.read32bit());
                long value = Byte.toUnsignedLong((byte) in.read8bit());
                return new GenericMemoryEvent(true, 0, address, (byte) 1, true, value);
            }
            case TYPE_MEM32W16F:
            case TYPE_MEM32W16: {
                long address = Integer.toUnsignedLong(in.read32bit());
                long value = Short.toUnsignedLong(in.read16bit());
                return new GenericMemoryEvent(true, 0, address, (byte) 2, true, value);
            }
            case TYPE_MEM32W32F:
            case TYPE_MEM32W32: {
                long address = Integer.toUnsignedLong(in.read32bit());
                long value = Integer.toUnsignedLong(in.read32bit());
                return new GenericMemoryEvent(true, 0, address, (byte) 4, true, value);
            }
            case TYPE_MEM32W64F:
            case TYPE_MEM32W64: {
                long address = Integer.toUnsignedLong(in.read32bit());
                long value = in.read64bit();
                return new GenericMemoryEvent(true, 0, address, (byte) 8, true, value);
            }
            case TYPE_MEM32R8F: {
                long address = Integer.toUnsignedLong(in.read32bit());
                return new GenericMemoryEvent(true, 0, address, (byte) 1, false);
            }
            case TYPE_MEM32R16F: {
                long address = Integer.toUnsignedLong(in.read32bit());
                return new GenericMemoryEvent(true, 0, address, (byte) 2, false);
            }
            case TYPE_MEM32R32F: {
                long address = Integer.toUnsignedLong(in.read32bit());
                return new GenericMemoryEvent(true, 0, address, (byte) 4, false);
            }
            case TYPE_MEM32R64F: {
                long address = Integer.toUnsignedLong(in.read32bit());
                return new GenericMemoryEvent(true, 0, address, (byte) 8, false);
            }
            case TYPE_MEM64R8: {
                long address = in.read64bit();
                long value = Byte.toUnsignedLong((byte) in.read8bit());
                return new GenericMemoryEvent(true, 0, address, (byte) 1, false, value);
            }
            case TYPE_MEM64R16: {
                long address = in.read64bit();
                long value = Short.toUnsignedLong(in.read16bit());
                return new GenericMemoryEvent(true, 0, address, (byte) 2, false, value);
            }
            case TYPE_MEM64R32: {
                long address = in.read64bit();
                long value = Integer.toUnsignedLong(in.read32bit());
                return new GenericMemoryEvent(true, 0, address, (byte) 4, false, value);
            }
            case TYPE_MEM64R64: {
                long address = in.read64bit();
                long value = in.read64bit();
                return new GenericMemoryEvent(true, 0, address, (byte) 8, false, value);
            }
            case TYPE_MEM64W8F:
            case TYPE_MEM64W8: {
                long address = in.read64bit();
                long value = Byte.toUnsignedLong((byte) in.read8bit());
                return new GenericMemoryEvent(true, 0, address, (byte) 1, true, value);
            }
            case TYPE_MEM64W16F:
            case TYPE_MEM64W16: {
                long address = in.read64bit();
                long value = Short.toUnsignedLong(in.read16bit());
                return new GenericMemoryEvent(true, 0, address, (byte) 2, true, value);
            }
            case TYPE_MEM64W32F:
            case TYPE_MEM64W32: {
                long address = in.read64bit();
                long value = Integer.toUnsignedLong(in.read32bit());
                return new GenericMemoryEvent(true, 0, address, (byte) 4, true, value);
            }
            case TYPE_MEM64W64F:
            case TYPE_MEM64W64: {
                long address = in.read64bit();
                long value = in.read64bit();
                return new GenericMemoryEvent(true, 0, address, (byte) 8, true, value);
            }
            case TYPE_MEM64R8F: {
                long address = in.read64bit();
                return new GenericMemoryEvent(true, 0, address, (byte) 1, false);
            }
            case TYPE_MEM64R16F: {
                long address = in.read64bit();
                return new GenericMemoryEvent(true, 0, address, (byte) 2, false);
            }
            case TYPE_MEM64R32F: {
                long address = in.read64bit();
                return new GenericMemoryEvent(true, 0, address, (byte) 4, false);
            }
            case TYPE_MEM64R64F: {
                long address = in.read64bit();
                return new GenericMemoryEvent(true, 0, address, (byte) 8, false);
            }
            case TYPE_DUMP: {
                long address = in.read64bit();
                byte[] data = IO.readArray(in);
                return new MemoryDumpEvent(0, address, data);
            }
            case TYPE_MMAP: {
                long start = in.read64bit();
                long end = in.read64bit();
                String name = IO.readString(in);
                return mmap(start, end, name);
            }
            case TYPE_MMAP2: {
                long addr = in.read64bit();
                long len = in.read64bit();
                int prot = in.read32bit();
                int flags = in.read32bit();
                int fildes = in.read32bit();
                long offset = in.read64bit();
                long result = in.read64bit();
                String name = IO.readString(in);
                byte[] data = IO.readArray(in);
                return new MmapEvent(0, addr, len, prot, flags, fildes, offset, name, result, data);
            }
            case TYPE_MUNMAP: {
                long addr = in.read64bit();
                long len = in.read64bit();
                int result = in.read32bit();
                return new MunmapEvent(0, addr, len, result);
            }
            case TYPE_MPROTECT: {
                long addr = in.read64bit();
                long len = in.read64bit();
                int prot = in.read32bit();
                int result = in.read32bit();
                return new MprotectEvent(0, addr, len, prot, result);
            }
            case TYPE_BRK: {
                long brk = in.read64bit();
                long result = in.read64bit();
                return new BrkEvent(0, brk, result);
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
