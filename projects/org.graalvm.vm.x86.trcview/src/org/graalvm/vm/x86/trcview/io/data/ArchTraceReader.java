package org.graalvm.vm.x86.trcview.io.data;

import java.io.IOException;

public abstract class ArchTraceReader {
    public abstract Event read() throws IOException;

    public abstract long tell();
}
