package org.graalvm.vm.trcview.arch.io;

import java.io.IOException;

import org.graalvm.vm.trcview.analysis.Analyzer;
import org.graalvm.vm.trcview.arch.Architecture;

public abstract class TraceReader {
    private Architecture arch;

    protected TraceReader() {
    }

    protected void setArchitecture(Architecture arch) {
        this.arch = arch;
    }

    public Analyzer getAnalyzer() {
        return null;
    }

    public abstract Event read() throws IOException;

    public abstract long tell();

    public Architecture getArchitecture() {
        return arch;
    }
}
