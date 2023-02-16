package org.graalvm.vm.trcview.arch.x86.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.trcview.analysis.Analyzer;
import org.graalvm.vm.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.trcview.analysis.memory.MemoryTrace;
import org.graalvm.vm.trcview.arch.io.ArchTraceReader;
import org.graalvm.vm.trcview.arch.io.BrkEvent;
import org.graalvm.vm.trcview.arch.io.EofEvent;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.GenericMemoryEvent;
import org.graalvm.vm.trcview.arch.io.IoEvent;
import org.graalvm.vm.trcview.arch.io.MemoryDumpEvent;
import org.graalvm.vm.trcview.arch.io.MemoryEventI128;
import org.graalvm.vm.trcview.arch.io.MemoryEventI16;
import org.graalvm.vm.trcview.arch.io.MemoryEventI32;
import org.graalvm.vm.trcview.arch.io.MemoryEventI64;
import org.graalvm.vm.trcview.arch.io.MemoryEventI8;
import org.graalvm.vm.trcview.arch.io.MmapEvent;
import org.graalvm.vm.trcview.arch.io.MprotectEvent;
import org.graalvm.vm.trcview.arch.io.MunmapEvent;
import org.graalvm.vm.trcview.arch.io.SymbolTableEvent;
import org.graalvm.vm.trcview.arch.io.TraceSymbol;
import org.graalvm.vm.trcview.arch.x86.decode.Syscalls;
import org.graalvm.vm.trcview.io.Node;
import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.util.Vector128;
import org.graalvm.vm.util.io.BEInputStream;
import org.graalvm.vm.util.io.WordInputStream;

public class AMD64TraceReader extends ArchTraceReader implements Analyzer {
    public static final byte TYPE_FULL_STATE = 0x01;
    public static final byte TYPE_DELTA_STATE = 0x02;
    public static final byte TYPE_SMALL_DELTA_STATE = 0x03;
    public static final byte TYPE_TINY_DELTA_STATE = 0x04;
    public static final byte TYPE_MEMORY = 0x10;
    public static final byte TYPE_MEMORY_DUMP = 0x20;
    public static final byte TYPE_MMAP = 0x30;
    public static final byte TYPE_MUNMAP = 0x31;
    public static final byte TYPE_MPROTECT = 0x32;
    public static final byte TYPE_BRK = 0x33;
    public static final byte TYPE_SYMBOLS = 0x40;
    public static final byte TYPE_SYSLOG = 0x41;
    public static final byte TYPE_EOF = -1;

    private static final byte FLAG_DATA = 1;
    private static final byte FLAG_WRITE = 2;

    private static final int MAX_STR_SIZE = 16 * 1024 * 1024; // 16MiB

    private static final int MAX_DELTA_LENGTH = 1000;

    private WordInputStream in;

    private MemoryTrace mem;
    private AMD64StepEvent last;
    private AMD64StepEvent stored;

    private long steps;
    private AMD64StepEvent lastStep;

    public AMD64TraceReader(InputStream in) {
        this(new BEInputStream(in));
    }

    public AMD64TraceReader(WordInputStream in) {
        this.in = in;
    }

    @Override
    public Analyzer getAnalyzer() {
        return this;
    }

    private String str(long addr, long sz, long step) {
        if (sz > 0) {
            int size = (int) sz;
            if (size > MAX_STR_SIZE) {
                size = MAX_STR_SIZE;
            }
            byte[] buf = new byte[size];
            for (int i = 0; i < size; i++) {
                try {
                    buf[i] = mem.getByte(addr + i, step);
                } catch (MemoryNotMappedException e) {
                    return new String(buf, 0, i, StandardCharsets.ISO_8859_1);
                }
            }
            return new String(buf, StandardCharsets.ISO_8859_1);
        } else {
            return null;
        }
    }

    private byte[] readMachinecode() throws IOException {
        int len = in.read8bit();
        byte[] code = new byte[len];
        in.read(code);
        return code;
    }

    public static final String readString(WordInputStream in) throws IOException {
        int length = in.read16bit();
        if (length == 0) {
            return null;
        } else {
            byte[] bytes = new byte[length];
            in.read(bytes);
            return new String(bytes);
        }
    }

    public static final byte[] readArray(WordInputStream in) throws IOException {
        int length = in.read32bit();
        if (length == 0) {
            return null;
        } else {
            byte[] data = new byte[length];
            in.read(data);
            return data;
        }
    }

    private Event step(AMD64StepEvent record) {
        steps++;
        if (steps % MAX_DELTA_LENGTH == 0) {
            stored = new AMD64FullCpuState(record.getState());
        } else {
            stored = record;
        }
        lastStep = stored;

        if (mem != null && last != null) {
            AMD64CpuState state = last.getState();
            last = null;
            long rax = state.getRAX();
            if (rax == Syscalls.SYS_read) { // read(int fd, long addr, long sz)
                long step = stored.getStep();
                int file = (int) state.getRDI();
                if (file >= 0 && file <= 2) {
                    long addr = state.getRSI();
                    long size = stored.getState().getRAX();
                    if (size > 0) {
                        String s = str(addr, size, step);
                        if (s != null) {
                            int ch = file == 2 ? 1 : 0;
                            return new IoEvent(state.getTid(), step, ch, true, s);
                        }
                    }
                }
            } else if (rax == Syscalls.SYS_write) { // write(int fd, long addr, long sz)
                long step = state.getStep();
                int file = (int) state.getRDI();
                if (file >= 0 && file <= 2) {
                    long addr = state.getRSI();
                    long size = stored.getState().getRAX();
                    if (size > 0) {
                        String s = str(addr, size, step);
                        if (s != null) {
                            int ch = file == 2 ? 1 : 0;
                            return new IoEvent(state.getTid(), step, ch, false, s);
                        }
                    }
                }
            } else if (rax == Syscalls.SYS_readv) { // readv(int fd, iov* iov, int cnt)
                long step = stored.getStep();
                int file = (int) state.getRDI();
                if (file >= 0 && file <= 2) {
                    long iov = state.getRSI();
                    long size = stored.getState().getRAX();
                    if (size > 0) {
                        StringBuilder buf = new StringBuilder();
                        for (long sz = 0, i = 0; sz < size; i += 16) {
                            try {
                                long base = mem.getWord(iov + i, step);
                                long len = mem.getWord(iov + i + 8, step);
                                long remaining = size - sz;
                                String s = str(base, remaining < len ? remaining : len, step);
                                if (s == null) {
                                    break;
                                } else {
                                    buf.append(s);
                                    sz += len;
                                }
                            } catch (MemoryNotMappedException e) {
                                break;
                            }
                        }
                        if (buf.length() > 0) {
                            int ch = file == 2 ? 1 : 0;
                            return new IoEvent(state.getTid(), step, ch, true, buf.toString());
                        }
                    }
                }
            } else if (rax == Syscalls.SYS_writev) { // writev(int fd, iov* iov, int cnt)
                long step = state.getStep();
                int file = (int) state.getRDI();
                if (file >= 0 && file <= 2) {
                    long iov = state.getRSI();
                    long size = stored.getState().getRAX();
                    if (size > 0) {
                        StringBuilder buf = new StringBuilder();
                        for (long sz = 0, i = 0; sz < size; i += 16) {
                            try {
                                long base = mem.getWord(iov + i, step);
                                long len = mem.getWord(iov + i + 8, step);
                                long remaining = size - sz;
                                String s = str(base, remaining < len ? remaining : len, step);
                                if (s == null) {
                                    break;
                                } else {
                                    buf.append(s);
                                    sz += len;
                                }
                            } catch (MemoryNotMappedException e) {
                                break;
                            }
                        }
                        if (buf.length() > 0) {
                            int ch = file == 2 ? 1 : 0;
                            return new IoEvent(state.getTid(), step, ch, false, buf.toString());
                        }
                    }
                }
            }
        }

        AMD64StepEvent evt = stored;
        stored = null;
        if (mem != null && evt.isSyscall()) {
            last = evt;
        }
        return evt;
    }

    @Override
    public Event read() throws IOException {
        if (stored != null) {
            AMD64StepEvent evt = stored;
            stored = null;
            return evt;
        }

        int type = (byte) in.read8bit();
        int tid = in.read32bit();

        switch (type) {
            case TYPE_FULL_STATE: {
                byte[] machinecode = readMachinecode();
                AMD64FullCpuState record = new AMD64FullCpuState(in, tid, machinecode);
                return step(record);
            }
            case TYPE_DELTA_STATE: {
                byte[] machinecode = readMachinecode();
                AMD64DeltaCpuState record = new AMD64DeltaCpuState(in, tid, machinecode, lastStep);
                return step(record);
            }
            case TYPE_SMALL_DELTA_STATE: {
                byte[] machinecode = readMachinecode();
                AMD64SmallDeltaCpuState record = new AMD64SmallDeltaCpuState(in, tid, machinecode, lastStep);
                return step(record);
            }
            case TYPE_TINY_DELTA_STATE: {
                byte[] machinecode = readMachinecode();
                AMD64TinyDeltaCpuState record = new AMD64TinyDeltaCpuState(in, tid, machinecode, lastStep);
                return step(record);
            }
            case TYPE_MEMORY: {
                int flags = in.read8bit();
                boolean write = (flags & FLAG_WRITE) != 0;
                boolean data = (flags & FLAG_DATA) != 0;
                byte size = (byte) in.read8bit();
                long address = in.read64bit();
                if (data) {
                    switch (size) {
                        case 1:
                            return new MemoryEventI8(false, tid, address, write, (byte) in.read8bit());
                        case 2:
                            return new MemoryEventI16(false, tid, address, write, in.read16bit());
                        case 4:
                            return new MemoryEventI32(false, tid, address, write, in.read32bit());
                        case 8:
                            return new MemoryEventI64(false, tid, address, write, in.read64bit());
                        case 16: {
                            long hi = in.read64bit();
                            long lo = in.read64bit();
                            Vector128 value128 = new Vector128(hi, lo);
                            return new MemoryEventI128(false, tid, address, write, value128);
                        }
                        default:
                            throw new IOException("unknown size: " + size);
                    }
                } else {
                    return new GenericMemoryEvent(false, tid, address, size, write);
                }
            }
            case TYPE_MEMORY_DUMP: {
                long address = in.read64bit();
                byte[] data = readArray(in);
                return new MemoryDumpEvent(tid, address, data);
            }
            case TYPE_MMAP: {
                long addr = in.read64bit();
                long len = in.read64bit();
                int prot = in.read32bit();
                int flags = in.read32bit();
                int fildes = in.read32bit();
                long off = in.read64bit();
                long result = in.read64bit();
                String filename = readString(in);
                byte[] data = readArray(in);
                return new MmapEvent(tid, addr, len, prot, flags, fildes, off, filename, result, data);
            }
            case TYPE_MUNMAP: {
                long addr = in.read64bit();
                long len = in.read64bit();
                int result = in.read32bit();
                return new MunmapEvent(tid, addr, len, result);
            }
            case TYPE_MPROTECT: {
                long addr = in.read64bit();
                long len = in.read64bit();
                int prot = in.read32bit();
                int result = in.read32bit();
                return new MprotectEvent(tid, addr, len, prot, result);
            }
            case TYPE_BRK: {
                long brk = in.read64bit();
                long result = in.read64bit();
                return new BrkEvent(tid, brk, result);
            }
            case TYPE_SYMBOLS: {
                NavigableMap<Long, Symbol> symbols = new TreeMap<>();
                long loadBias = in.read64bit();
                long address = in.read64bit();
                long size = in.read64bit();
                String filename = readString(in);
                int count = in.read32bit();
                for (int i = 0; i < count; i++) {
                    long value = in.read64bit();
                    long sz = in.read64bit();
                    short shndx = in.read16bit();
                    int bind = in.read();
                    int typ = in.read();
                    int visibility = in.read();
                    String name = readString(in);
                    Symbol sym = new TraceSymbol(name, value, sz, bind, typ, visibility, shndx);
                    symbols.put(sym.getValue(), sym);
                }
                return new SymbolTableEvent(tid, symbols, filename, loadBias, address, size);
            }
            case TYPE_SYSLOG: {
                return new AMD64SystemLogEvent(in, tid);
            }
            case TYPE_EOF:
                return new EofEvent();
            default:
                throw new IOException("unknown record type: 0x" + HexFormatter.tohex(type & 0xFF, 2));
        }
    }

    @Override
    public long tell() {
        return in.tell();
    }

    // Analyzer functions
    @Override
    public void start(MemoryTrace memory) {
        mem = memory;
    }

    @Override
    public void process(Event event, Node node) {
        // nothing
    }

    @Override
    public void finish() {
        // nothing
    }
}
