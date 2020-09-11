package org.graalvm.vm.trcview.decode;

public class Radix50 {
    public static final String R50CHARS = " ABCDEFGHIJKLMNOPQRSTUVWXYZ$.%0123456789";

    public static String decode(short rad50) {
        int r50 = Short.toUnsignedInt(rad50);
        char[] result = {R50CHARS.charAt((r50 / 1600) % 40),
                        R50CHARS.charAt((r50 / 40) % 40),
                        R50CHARS.charAt(r50 % 40)};
        return new String(result);
    }
}
