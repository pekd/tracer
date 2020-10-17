package org.graalvm.vm.trcview.arch.none.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.trcview.arch.io.ArchTraceReader;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.GenericMemoryEvent;
import org.graalvm.vm.trcview.arch.io.MemoryDumpEvent;
import org.graalvm.vm.trcview.arch.io.MemoryEventI16;
import org.graalvm.vm.trcview.arch.io.MemoryEventI32;
import org.graalvm.vm.trcview.arch.io.MemoryEventI64;
import org.graalvm.vm.trcview.arch.io.MemoryEventI8;
import org.graalvm.vm.trcview.arch.io.MmapEvent;
import org.graalvm.vm.trcview.arch.io.MprotectEvent;
import org.graalvm.vm.trcview.arch.io.MunmapEvent;
import org.graalvm.vm.trcview.arch.io.SymbolTableEvent;
import org.graalvm.vm.trcview.net.protocol.IO;
import org.graalvm.vm.util.BitTest;
import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.util.io.BEInputStream;
import org.graalvm.vm.util.io.WordInputStream;

public class GenericTraceReader extends ArchTraceReader {
    private static final long STEP_THRESHOLD = 5_000;

    public static final byte RECORD_STEP = 0;
    public static final byte RECORD_DELTA_STEP = 1;
    public static final byte RECORD_MMAP = 2;
    public static final byte RECORD_MUNMAP = 3;
    public static final byte RECORD_MPROTECT = 4;
    public static final byte RECORD_READ = 5;
    public static final byte RECORD_WRITE = 6;
    public static final byte RECORD_READ_8 = 7;
    public static final byte RECORD_READ_16 = 8;
    public static final byte RECORD_READ_32 = 9;
    public static final byte RECORD_READ_64 = 10;
    public static final byte RECORD_WRITE_8 = 11;
    public static final byte RECORD_WRITE_16 = 12;
    public static final byte RECORD_WRITE_32 = 13;
    public static final byte RECORD_WRITE_64 = 14;
    public static final byte RECORD_DUMP = 15;
    public static final byte RECORD_TRAP = 16;
    public static final byte RECORD_SYMBOLS = 17;
    public static final byte RECORD_ENDIANESS = 18;

    private final WordInputStream in;

    private GenericStateDescription description;

    private boolean be = true;

    private GenericStepEvent last = null;
    private long stepcnt = 0;

    public GenericTraceReader(InputStream in) {
        this(new BEInputStream(in));
    }

    public GenericTraceReader(WordInputStream in) {
        this.in = in;
    }

    @Override
    public Event read() throws IOException {
        if (description == null) {
            try {
                description = new GenericStateDescription(in);
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            }
            be = description.getFormat().be;
        }

        int magic;
        try {
            magic = in.read8bit() & 0xFF;
        } catch (EOFException e) {
            return null;
        }

        int tid = in.read32bit();
        switch (magic) {
            case RECORD_STEP:
                last = GenericFullStepEvent.parse(in, tid, description);
                stepcnt = 0;
                return last;
            case RECORD_DELTA_STEP:
                last = GenericDeltaStepEvent.parse(in, tid, description, last);
                stepcnt++;
                if (stepcnt >= STEP_THRESHOLD) {
                    last = new GenericFullStepEvent(last);
                    stepcnt = 0;
                }
                return last;
            case RECORD_MMAP: {
                long address = in.read64bit();
                long length = in.read64bit();
                long offset = in.read64bit();
                long result = in.read64bit();
                int protection = in.read32bit();
                int flags = in.read32bit();
                int fd = in.read32bit();
                String filename = IO.readString(in);
                return new MmapEvent(tid, address, length, protection, flags, fd, offset, filename, result, null);
            }
            case RECORD_MUNMAP: {
                long address = in.read64bit();
                long length = in.read64bit();
                int result = in.read32bit();
                return new MunmapEvent(tid, address, length, result);
            }
            case RECORD_MPROTECT: {
                long address = in.read64bit();
                long length = in.read64bit();
                int prot = in.read32bit();
                int result = in.read32bit();
                return new MprotectEvent(tid, address, length, prot, result);
            }
            case RECORD_READ: {
                long address = in.read64bit();
                long value = in.read64bit();
                byte size = (byte) in.read8bit();
                int flags = in.read8bit();
                boolean isbe = BitTest.test(flags, 1);
                boolean hasvalue = BitTest.test(flags, 2);
                if (hasvalue) {
                    return new GenericMemoryEvent(isbe, tid, address, size, false, value);
                } else {
                    return new GenericMemoryEvent(isbe, tid, address, size, false);
                }
            }
            case RECORD_WRITE: {
                long address = in.read64bit();
                long value = in.read64bit();
                byte size = (byte) in.read8bit();
                int flags = in.read8bit();
                boolean isbe = BitTest.test(flags, 1);
                boolean hasvalue = BitTest.test(flags, 2);
                if (hasvalue) {
                    return new GenericMemoryEvent(isbe, tid, address, size, true, value);
                } else {
                    return new GenericMemoryEvent(isbe, tid, address, size, true);
                }
            }
            case RECORD_ENDIANESS:
                be = in.read8bit() != 0;
                return read();
            case RECORD_READ_8: {
                long address = in.read64bit();
                byte value = (byte) in.read8bit();
                return new MemoryEventI8(be, tid, address, false, value);
            }
            case RECORD_READ_16: {
                long address = in.read64bit();
                short value = in.read16bit();
                return new MemoryEventI16(be, tid, address, false, value);
            }
            case RECORD_READ_32: {
                long address = in.read64bit();
                int value = in.read32bit();
                return new MemoryEventI32(be, tid, address, false, value);
            }
            case RECORD_READ_64: {
                long address = in.read64bit();
                long value = in.read64bit();
                return new MemoryEventI64(be, tid, address, false, value);
            }
            case RECORD_WRITE_8: {
                long address = in.read64bit();
                byte value = (byte) in.read8bit();
                return new MemoryEventI8(be, tid, address, true, value);
            }
            case RECORD_WRITE_16: {
                long address = in.read64bit();
                short value = in.read16bit();
                return new MemoryEventI16(be, tid, address, true, value);
            }
            case RECORD_WRITE_32: {
                long address = in.read64bit();
                int value = in.read32bit();
                return new MemoryEventI32(be, tid, address, true, value);
            }
            case RECORD_WRITE_64: {
                long address = in.read64bit();
                long value = in.read64bit();
                return new MemoryEventI64(be, tid, address, true, value);
            }
            case RECORD_DUMP: {
                long address = in.read64bit();
                byte[] data = IO.readArray(in);
                return new MemoryDumpEvent(tid, address, data);
            }
            case RECORD_TRAP: {
                String msg = IO.readString(in);
                return new GenericTrapEvent(tid, msg, last);
            }
            case RECORD_SYMBOLS: {
                NavigableMap<Long, Symbol> symbols = new TreeMap<>();
                long loadbias = in.read64bit();
                long address = in.read64bit();
                long size = in.read64bit();
                String filename = IO.readString(in);
                int count = in.read32bit();
                for (int i = 0; i < count; i++) {
                    Symbol sym = new GenericSymbol(in);
                    symbols.put(sym.getValue(), sym);
                }
                return new SymbolTableEvent(tid, symbols, filename, loadbias, address, size);
            }
            default:
                throw new IOException("unknown record type 0x" + HexFormatter.tohex(magic));
        }
    }

    @Override
    public long tell() {
        return in.tell();
    }
}
