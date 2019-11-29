package org.graalvm.vm.trcview.arch.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import org.graalvm.vm.posix.elf.ElfStrings;
import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.util.io.BEInputStream;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.log.Trace;

public class TraceReader {
    private static final Logger log = Trace.create(TraceReader.class);

    private static final int MAGIC = 0x58545243;

    private final WordInputStream in;
    private final ArchTraceReader reader;

    private Architecture arch;

    public TraceReader(InputStream in) throws IOException {
        this.in = new BEInputStream(in);
        int magic = this.in.read32bit();
        if (magic != MAGIC) {
            throw new IOException("not a trace file");
        }

        short archid = this.in.read16bit();
        arch = Architecture.getArchitecture(archid);
        if (arch == null) {
            throw new IOException("unknown architecture " + ElfStrings.getElfMachine(archid));
        } else {
            log.info("Loading trace for " + ElfStrings.getElfMachine(archid));
        }
        reader = arch.getTraceReader(this.in);
    }

    public Event read() throws IOException {
        return reader.read();
    }

    public long tell() {
        return in.tell();
    }

    public Architecture getArchitecture() {
        return arch;
    }
}
