package org.graalvm.vm.trcview.arch.vm.analysis;

import org.graalvm.vm.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.arch.vm.VMArchitecture;
import org.graalvm.vm.trcview.io.Node;

public abstract class VMTransformer {
    private final VMArchitecture arch;
    private VMAnalyzer analyzer;
    private Architecture hostarch;

    protected VMTransformer(short id, String name, String description, StepFormat format) {
        arch = new VMArchitecture(id, name, description, format);
    }

    public VMArchitecture getArchitecture() {
        return arch;
    }

    public Architecture getHostArchitecture() {
        return hostarch;
    }

    public boolean isApplicable(Architecture host) {
        return host != arch;
    }

    public void start() {
        // nothing
    }

    public abstract void process(Event event, Node node, CpuState state);

    public void finish() {
        // nothing
    }

    void setAnalyzer(VMAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    void setHostArchitecture(Architecture hostarch) {
        this.hostarch = hostarch;
    }

    protected void createEvent(Event event) {
        analyzer.createEvent(event);
    }

    protected long getCurrentStep() {
        return analyzer.getCurrentStep();
    }

    protected byte getI8(long addr) throws MemoryNotMappedException {
        return analyzer.getMemoryTrace().getLastByte(addr);
    }

    protected short getI16(long addr) throws MemoryNotMappedException {
        return analyzer.getMemoryTrace().getLastShort(addr);
    }

    protected int getI32(long addr) throws MemoryNotMappedException {
        return analyzer.getMemoryTrace().getLastInt(addr);
    }

    protected long getI64(long addr) throws MemoryNotMappedException {
        return analyzer.getMemoryTrace().getLastWord(addr);
    }
}
