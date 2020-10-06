package org.graalvm.vm.trcview.decode;

public class Fx32 {
    public static final int SHIFT = 12;
    public static final int INT_SIZE = 19;
    public static final int DEC_SIZE = 12;

    // fx32: s19.12 fixed point number
    public static double toDouble(int fx32) {
        return (double) fx32 / (1L << SHIFT);
    }
}
