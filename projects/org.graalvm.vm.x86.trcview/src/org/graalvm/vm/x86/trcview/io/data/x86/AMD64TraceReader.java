package org.graalvm.vm.x86.trcview.io.data.x86;

import java.io.IOException;
import java.io.InputStream;

import org.graalvm.vm.x86.node.debug.trace.BrkRecord;
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
import org.graalvm.vm.x86.trcview.io.data.ArchTraceReader;
import org.graalvm.vm.x86.trcview.io.data.BrkEvent;
import org.graalvm.vm.x86.trcview.io.data.EofEvent;
import org.graalvm.vm.x86.trcview.io.data.Event;
import org.graalvm.vm.x86.trcview.io.data.MemoryDumpEvent;
import org.graalvm.vm.x86.trcview.io.data.MemoryEvent;
import org.graalvm.vm.x86.trcview.io.data.MmapEvent;
import org.graalvm.vm.x86.trcview.io.data.MprotectEvent;
import org.graalvm.vm.x86.trcview.io.data.MunmapEvent;
import org.graalvm.vm.x86.trcview.io.data.SymbolTableEvent;

public class AMD64TraceReader extends ArchTraceReader {
    private ExecutionTraceReader in;

    public AMD64TraceReader(InputStream in) {
        this(new ExecutionTraceReader(in));
    }

    public AMD64TraceReader(ExecutionTraceReader in) {
        this.in = in;
    }

    @Override
    public Event read() throws IOException {
        Record record = in.read();
        if (record instanceof StepRecord) {
            return new AMD64StepEvent((StepRecord) record);
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
}
