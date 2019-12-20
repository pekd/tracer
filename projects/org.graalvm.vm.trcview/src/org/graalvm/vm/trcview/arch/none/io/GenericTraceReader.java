package org.graalvm.vm.trcview.arch.none.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.graalvm.vm.trcview.arch.io.ArchTraceReader;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.MemoryEvent;
import org.graalvm.vm.trcview.arch.io.MmapEvent;
import org.graalvm.vm.trcview.arch.io.MunmapEvent;
import org.graalvm.vm.trcview.net.protocol.IO;
import org.graalvm.vm.util.BitTest;
import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.util.io.BEInputStream;
import org.graalvm.vm.util.io.WordInputStream;

public class GenericTraceReader extends ArchTraceReader {
    public static final int STEP = 0x53544550;
    public static final int MMAP = 0x4D4D4150;
    public static final int UMAP = 0x554D4150;
    public static final int MEMR = 0x4D454D52;
    public static final int MEMW = 0x4D454D57;

    private final WordInputStream in;

    public GenericTraceReader(InputStream in) {
        this(new BEInputStream(in));
    }

    public GenericTraceReader(WordInputStream in) {
        this.in = in;
    }

    @Override
    public Event read() throws IOException {
        int magic;
        try {
            magic = in.read32bit();
        } catch (EOFException e) {
            return null;
        }
        int tid = in.read32bit();
        switch (magic) {
            case STEP:
                return new GenericStepEvent(in, tid);
            case MMAP: {
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
            case UMAP: {
                long address = in.read64bit();
                long length = in.read64bit();
                int result = in.read32bit();
                return new MunmapEvent(tid, address, length, result);
            }
            case MEMR: {
                long address = in.read64bit();
                long value = in.read64bit();
                byte size = (byte) in.read8bit();
                int flags = in.read8bit();
                boolean be = BitTest.test(flags, 1);
                boolean hasvalue = BitTest.test(flags, 2);
                if (hasvalue) {
                    return new MemoryEvent(be, tid, address, size, false, value);
                } else {
                    return new MemoryEvent(be, tid, address, size, false);
                }
            }
            case MEMW: {
                long address = in.read64bit();
                long value = in.read64bit();
                byte size = (byte) in.read8bit();
                int flags = in.read8bit();
                boolean be = BitTest.test(flags, 1);
                boolean hasvalue = BitTest.test(flags, 2);
                if (hasvalue) {
                    return new MemoryEvent(be, tid, address, size, true, value);
                } else {
                    return new MemoryEvent(be, tid, address, size, true);
                }
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
