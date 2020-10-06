package org.graalvm.vm.trcview.decode;

public class Fx16 {
    public static final int SHIFT = 12;
    public static final int INT_SIZE = 3;
    public static final int DEC_SIZE = 12;

    // fx16: s3.12 fixed point number
    public static double toDouble(short fx16) {
        return (double) fx16 / (1L << SHIFT);
    }
}
