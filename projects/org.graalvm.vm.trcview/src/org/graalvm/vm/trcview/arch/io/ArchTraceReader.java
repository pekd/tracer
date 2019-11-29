package org.graalvm.vm.trcview.arch.io;

import java.io.IOException;

public abstract class ArchTraceReader {
    public abstract Event read() throws IOException;

    public abstract long tell();
}
