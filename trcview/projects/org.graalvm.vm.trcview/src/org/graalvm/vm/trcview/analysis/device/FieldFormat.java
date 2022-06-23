package org.graalvm.vm.trcview.analysis.device;

import org.graalvm.vm.trcview.disasm.Field64;

public abstract class FieldFormat {
    protected final Field64 field;
    protected final String name;

    protected FieldFormat(String name, Field64 field) {
        this.name = name;
        this.field = field;
    }

    public String getName() {
        return name;
    }

    public long get(long reg) {
        return field.get(reg);
    }

    public abstract String format(long reg);

    protected static double log2(double x) {
        return Math.log(x) / Math.log(2);
    }

    protected int length(int radix) {
        return (int) Math.ceil(field.size() / log2(radix));
    }

    protected static String pad(String s, int len) {
        if (s.length() >= len) {
            return s;
        } else {
            StringBuilder buf = new StringBuilder(len);
            for (int i = s.length(); i < len; i++) {
                buf.append('0');
            }
            buf.append(s);
            return buf.toString();
        }
    }
}
