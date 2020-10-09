package org.graalvm.vm.trcview.analysis;

import org.graalvm.vm.trcview.arch.io.StepFormat;

public class SymbolName {
    private final StepFormat format;

    public SymbolName(StepFormat format) {
        this.format = format;
    }

    public String addr(long pc) {
        return format.formatShortAddress(pc);
    }

    public String loc(long pc) {
        return "loc_" + addr(pc);
    }

    public String sub(long pc) {
        return "sub_" + addr(pc);
    }

    public String sc(long pc) {
        return "sc_" + addr(pc);
    }

    public String unk(long pc) {
        return "unk_" + addr(pc);
    }
}
