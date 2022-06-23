package org.graalvm.vm.trcview.analysis.device;

import org.graalvm.vm.trcview.disasm.Field64;

public class FXFieldFormat extends FieldFormat {
    private final int shift;

    public FXFieldFormat(String name, int hi, int lo, int shift) {
        super(name, Field64.getLE(hi, lo, true));
        this.shift = shift;
    }

    @Override
    public String format(long reg) {
        // FIXME: implement this conversion without loss of information
        long val = get(reg);
        double d = (double) val / (1L << shift);
        return Double.toString(d);
    }
}
