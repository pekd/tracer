package org.graalvm.vm.trcview.analysis.device;

import org.graalvm.vm.trcview.disasm.Field64;

public class EnumFieldFormat extends FieldFormat {
    private final String[] names;

    // single bit
    public EnumFieldFormat(String name, int bit, String[] names) {
        this(name, bit, bit, names);
    }

    // multiple bits
    public EnumFieldFormat(String name, int hi, int lo, String[] names) {
        super(name, Field64.getLE(hi, lo, false));
        this.names = names;
    }

    @Override
    public String format(long reg) {
        long value = field.get(reg);
        if (value < names.length) {
            return names[(int) value];
        } else {
            return "UNKNOWN[" + Long.toUnsignedString(value, 16) + "]";
        }
    }
}
