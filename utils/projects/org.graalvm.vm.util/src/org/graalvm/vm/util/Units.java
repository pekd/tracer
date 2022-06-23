package org.graalvm.vm.util;

public class Units {
    public static final String[] PREFIXES = {"k", "M", "G", "T", "P", "E", "Z"};

    public static String si(long x) {
        long value = x;
        if (x < 1000) {
            return Long.toString(x);
        }
        int i;
        for (i = 0; i < PREFIXES.length; i++) {
            value /= 1000;
            if (value < 1000) {
                return value + PREFIXES[i];
            }
        }
        return value + PREFIXES[PREFIXES.length - 1];
    }
}
