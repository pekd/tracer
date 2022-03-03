package org.graalvm.vm.trcview.arch.riscv.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.graalvm.vm.posix.api.mem.Mman;
import org.graalvm.vm.trcview.arch.io.ArchTraceReader;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.GenericMemoryEvent;
import org.graalvm.vm.trcview.arch.io.MemoryDumpEvent;
import org.graalvm.vm.trcview.arch.io.MemoryEventI16;
import org.graalvm.vm.trcview.arch.io.MemoryEventI32;
import org.graalvm.vm.trcview.arch.io.MemoryEventI8;
import org.graalvm.vm.trcview.arch.io.MmapEvent;
import org.graalvm.vm.trcview.net.protocol.IO;
import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.util.io.LEInputStream;
import org.graalvm.vm.util.io.WordInputStream;

public class RiscVTraceReader extends ArchTraceReader {
    public static final int TYPE_STEP32 = 0x0;
    public static final int TYPE_STEP64 = 0x1;
    public static final int TYPE_TRAP = 0x2;
    public static final int TYPE_READ_8 = 0x3;
    public static final int TYPE_READ_16 = 0x4;
    public static final int TYPE_READ_32 = 0x5;
    public static final int TYPE_READ_64 = 0x6;
    public static final int TYPE_WRITE_8 = 0x7;
    public static final int TYPE_WRITE_16 = 0x8;
    public static final int TYPE_WRITE_32 = 0x9;
    public static final int TYPE_WRITE_64 = 0xA;
    public static final int TYPE_MEMR = 0xB;
    public static final int TYPE_MEMW = 0xC;
    public static final int TYPE_DUMP = 0xD;
    public static final int TYPE_MMAP = 0x0E;

    private static final int FULL_STEPS = 1000;

    private final WordInputStream in;
    private RiscVStepEvent lastStep;
    private int tid = 0;

    private int fullstate = 0;

    public RiscVTraceReader(InputStream in) {
        this(new LEInputStream(in));
    }

    private RiscVTraceReader(WordInputStream in) {
        this.in = in;
        lastStep = null;
    }

    private static MmapEvent mmap(long start, long end, String name) {
        return new MmapEvent(0, start, end - start + 1,
                        Mman.PROT_READ | Mman.PROT_WRITE | Mman.PROT_EXEC,
                        Mman.MAP_FIXED | Mman.MAP_PRIVATE | Mman.MAP_ANONYMOUS, -1, 0, name,
                        start, null);
    }

    @Override
    public Event read() throws IOException {
        int magic;
        try {
            magic = in.read8bit();
        } catch (EOFException e) {
            return null;
        }
        switch (magic) {
            case TYPE_STEP32: {
                RiscVCpuState lastState = lastStep == null ? new RiscVZeroCpuState(0) : lastStep.getState();
                RiscVCpuState cpu = new RiscVDeltaCpuState32(in, tid, lastState);
                if ((fullstate % FULL_STEPS) == 0) {
                    lastStep = new RiscVFullCpuState(cpu);
                } else {
                    lastStep = cpu;
                }
                fullstate++;
                fullstate %= FULL_STEPS;
                return lastStep;
            }
            case TYPE_STEP64: {
                RiscVCpuState lastState = lastStep == null ? new RiscVZeroCpuState(0) : lastStep.getState();
                RiscVCpuState cpu = new RiscVDeltaCpuState64(in, tid, lastState);
                if ((fullstate % FULL_STEPS) == 0) {
                    lastStep = new RiscVFullCpuState(cpu);
                } else {
                    lastStep = cpu;
                }
                fullstate++;
                fullstate %= FULL_STEPS;
                return lastStep;
            }
            case TYPE_TRAP:
                RiscVExceptionEvent trap = new RiscVExceptionEvent(in, tid, lastStep);
                return trap;
            case TYPE_READ_8: {
                byte value = (byte) in.read8bit();
                long address = Integer.toUnsignedLong(in.read32bit());
                return new MemoryEventI8(false, tid, address, false, value);
            }
            case TYPE_READ_16: {
                short value = in.read16bit();
                long address = Integer.toUnsignedLong(in.read32bit());
                return new MemoryEventI16(false, tid, address, false, value);
            }
            case TYPE_READ_32: {
                int value = in.read32bit();
                long address = Integer.toUnsignedLong(in.read32bit());
                return new MemoryEventI32(false, tid, address, false, value);
            }
            case TYPE_WRITE_8: {
                byte value = (byte) in.read8bit();
                long address = Integer.toUnsignedLong(in.read32bit());
                return new MemoryEventI8(false, tid, address, true, value);
            }
            case TYPE_WRITE_16: {
                short value = in.read16bit();
                long address = Integer.toUnsignedLong(in.read32bit());
                return new MemoryEventI16(false, tid, address, true, value);
            }
            case TYPE_WRITE_32: {
                int value = in.read32bit();
                long address = Integer.toUnsignedLong(in.read32bit());
                return new MemoryEventI32(false, tid, address, true, value);
            }
            case TYPE_MEMR: {
                long address = in.read64bit();
                long value = in.read64bit();
                byte size = (byte) in.read8bit();
                return new GenericMemoryEvent(true, 0, address, size, false, value);
            }
            case TYPE_MEMW: {
                long address = in.read64bit();
                long value = in.read64bit();
                byte size = (byte) in.read8bit();
                return new GenericMemoryEvent(true, 0, address, size, true, value);
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
            default:
                throw new IOException("unknown record: " + HexFormatter.tohex(magic, 8) +
                                " [position " + tell() + "]");
        }
    }

    @Override
    public long tell() {
        return in.tell();
    }
}
