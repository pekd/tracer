package org.graalvm.vm.trcview.arch;

import java.util.logging.Logger;

import org.graalvm.vm.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;

public class TraceCodeReader extends CodeReader {
    private static final Logger log = Trace.create(TraceCodeReader.class);

    private final TraceAnalyzer trc;
    private final long step;
    private long off;

    public TraceCodeReader(TraceAnalyzer trc, long pc, boolean isBE, long step) {
        super(pc, isBE);
        this.trc = trc;
        this.step = step;
        off = pc;
    }

    @Override
    public byte nextI8() {
        long offset = off++;
        n++;
        try {
            return trc.getI8(offset, step);
        } catch (MemoryNotMappedException e) {
            log.log(Levels.WARNING, "Memory not mapped at 0x" + HexFormatter.tohex(pc + offset));
            return 0;
        }
    }

    @Override
    public byte peekI8(int offset) {
        long o = off + offset;
        try {
            return trc.getI8(o, step);
        } catch (MemoryNotMappedException e) {
            log.log(Levels.WARNING, "Memory not mapped at 0x" + HexFormatter.tohex(pc + o));
            return 0;
        }
    }

    @Override
    public TraceCodeReader clone() {
        return new TraceCodeReader(trc, getPC(), isBE(), step);
    }
}
