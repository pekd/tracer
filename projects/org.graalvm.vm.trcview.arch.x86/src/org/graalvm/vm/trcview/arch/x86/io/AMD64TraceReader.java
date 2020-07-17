package org.graalvm.vm.trcview.arch.x86.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.graalvm.vm.trcview.analysis.Analyzer;
import org.graalvm.vm.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.trcview.analysis.memory.MemoryTrace;
import org.graalvm.vm.trcview.arch.io.ArchTraceReader;
import org.graalvm.vm.trcview.arch.io.BrkEvent;
import org.graalvm.vm.trcview.arch.io.EofEvent;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.IoEvent;
import org.graalvm.vm.trcview.arch.io.MemoryDumpEvent;
import org.graalvm.vm.trcview.arch.io.MemoryEvent;
import org.graalvm.vm.trcview.arch.io.MmapEvent;
import org.graalvm.vm.trcview.arch.io.MprotectEvent;
import org.graalvm.vm.trcview.arch.io.MunmapEvent;
import org.graalvm.vm.trcview.arch.io.SymbolTableEvent;
import org.graalvm.vm.trcview.arch.x86.AMD64;
import org.graalvm.vm.trcview.io.Node;
import org.graalvm.vm.x86.node.debug.trace.BrkRecord;
import org.graalvm.vm.x86.node.debug.trace.CallArgsRecord;
import org.graalvm.vm.x86.node.debug.trace.EofRecord;
import org.graalvm.vm.x86.node.debug.trace.ExecutionTraceReader;
import org.graalvm.vm.x86.node.debug.trace.MemoryDumpRecord;
import org.graalvm.vm.x86.node.debug.trace.MemoryEventRecord;
import org.graalvm.vm.x86.node.debug.trace.MmapRecord;
import org.graalvm.vm.x86.node.debug.trace.MprotectRecord;
import org.graalvm.vm.x86.node.debug.trace.MunmapRecord;
import org.graalvm.vm.x86.node.debug.trace.Record;
import org.graalvm.vm.x86.node.debug.trace.StepRecord;
import org.graalvm.vm.x86.node.debug.trace.SymbolTableRecord;
import org.graalvm.vm.x86.node.debug.trace.SystemLogRecord;
import org.graalvm.vm.x86.posix.Syscalls;

public class AMD64TraceReader extends ArchTraceReader implements Analyzer {
    private static final int MAX_STR_SIZE = 16 * 1024 * 1024; // 16MiB

    private final ExecutionTraceReader in;
    private MemoryTrace mem;
    private AMD64StepEvent last;
    private AMD64StepEvent stored;

    private long steps;
    private AMD64CpuState lastState;
    private StepRecord lastStep;

    public AMD64TraceReader(InputStream in) {
        this(new ExecutionTraceReader(in));
    }

    public AMD64TraceReader(ExecutionTraceReader in) {
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

    @Override
    public Event read() throws IOException {
        if (stored != null) {
            AMD64StepEvent evt = stored;
            stored = null;
            return evt;
        }

        Record record = in.read();
        if (record instanceof StepRecord) {
            steps++;
            if (lastStep == null || (steps % 1000 == 0)) {
                stored = new AMD64FullCpuState((StepRecord) record);
            } else {
                stored = AMD64DeltaCpuState.deltaState(lastState, lastStep, (StepRecord) record);
            }
            lastState = (AMD64CpuState) stored;
            lastStep = (StepRecord) record;

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
                                return new IoEvent(AMD64.ID, state.getTid(), step, ch, true, s);
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
                                return new IoEvent(AMD64.ID, state.getTid(), step, ch, false, s);
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
                                return new IoEvent(AMD64.ID, state.getTid(), step, ch, true, buf.toString());
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
                                return new IoEvent(AMD64.ID, state.getTid(), step, ch, false, buf.toString());
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
        } else if (record instanceof BrkRecord) {
            BrkRecord brk = (BrkRecord) record;
            return new BrkEvent(brk.getTid(), brk.getBrk(), brk.getResult());
        } else if (record instanceof MemoryDumpRecord) {
            MemoryDumpRecord dump = (MemoryDumpRecord) record;
            return new MemoryDumpEvent(dump.getTid(), dump.getAddress(), dump.getData());
        } else if (record instanceof MemoryEventRecord) {
            MemoryEventRecord event = (MemoryEventRecord) record;
            if (event.hasData()) {
                if (event.getSize() > 8) {
                    return new MemoryEvent(false, event.getTid(), event.getAddress(), (byte) event.getSize(), event.isWrite(), event.getVector());
                } else {
                    return new MemoryEvent(false, event.getTid(), event.getAddress(), (byte) event.getSize(), event.isWrite(), event.getValue());
                }
            } else {
                return new MemoryEvent(false, event.getTid(), event.getAddress(), (byte) event.getSize(), event.isWrite());
            }
        } else if (record instanceof MmapRecord) {
            MmapRecord mmap = (MmapRecord) record;
            return new MmapEvent(mmap.getTid(), mmap.getAddress(), mmap.getLength(), mmap.getProtection(), mmap.getFlags(), mmap.getFileDescriptor(), mmap.getOffset(), mmap.getFilename(),
                            mmap.getResult(), mmap.getData());
        } else if (record instanceof MunmapRecord) {
            MunmapRecord munmap = (MunmapRecord) record;
            return new MunmapEvent(munmap.getTid(), munmap.getAddress(), munmap.getLength(), munmap.getResult());
        } else if (record instanceof MprotectRecord) {
            MprotectRecord mprotect = (MprotectRecord) record;
            return new MprotectEvent(mprotect.getTid(), mprotect.getAddress(), mprotect.getLength(), mprotect.getProtection(), mprotect.getResult());
        } else if (record instanceof SymbolTableRecord) {
            SymbolTableRecord symtab = (SymbolTableRecord) record;
            return new SymbolTableEvent(symtab.getTid(), symtab.getSymbols(), symtab.getFilename(), symtab.getLoadBias(), symtab.getAddress(), symtab.getSize());
        } else if (record instanceof SystemLogRecord) {
            SystemLogRecord log = (SystemLogRecord) record;
            return new AMD64SystemLogEvent(log);
        } else if (record instanceof EofRecord) {
            return new EofEvent();
        } else if (record instanceof CallArgsRecord) {
            return read();
        } else if (record == null) {
            return null;
        } else {
            throw new IOException("unknown record: " + record.getClass().getCanonicalName());
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
