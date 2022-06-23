package org.graalvm.vm.trcview.analysis.device;

import org.graalvm.vm.trcview.disasm.Field64;

public class IntegerFieldFormat extends FieldFormat {
    private final FieldNumberType type;

    // single bit
    public IntegerFieldFormat(String name, int bit) {
        this(name, bit, bit, false, FieldNumberType.BIT);
    }

    // multiple bits
    public IntegerFieldFormat(String name, int hi, int lo, FieldNumberType type) {
        this(name, hi, lo, false, type);
    }

    // multiple bits
    public IntegerFieldFormat(String name, int hi, int lo, boolean signed, FieldNumberType type) {
        super(name, Field64.getLE(hi, lo, signed));
        this.type = type;
    }

    private String str(long reg, int radix) {
        return pad(Long.toUnsignedString(reg, radix).toUpperCase(), length(radix));
    }

    @Override
    public String format(long reg) {
        switch (type) {
            case BIT:
                return field.getBit(reg) ? "1" : "0";
            default:
            case BIN:
                return str(get(reg), 2);
            case OCT:
                return str(get(reg), 8);
            case DEC:
                return str(get(reg), 10);
            case HEX:
                return str(get(reg), 16);
        }
    }
}
