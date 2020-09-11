package org.graalvm.vm.trcview.decode;

public class Fx32 {
    public static final int SHIFT = 12;
    public static final int INT_SIZE = 19;
    public static final int DEC_SIZE = 12;

    // 19.12 fixed precision
    public static double toDouble(int fx32) {
        long fx = Integer.toUnsignedLong(fx32);
        return (double) fx / (1L << SHIFT);
    }
}
