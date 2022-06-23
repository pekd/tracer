package org.graalvm.vm.posix.api.linux;

public class Random {
    public static final int GRND_NONBLOCK = 0x0001;
    public static final int GRND_RANDOM = 0x0002;

    public static String toString(int flags) {
        if (flags == 0) {
            return "0";
        }

        StringBuilder buf = new StringBuilder();
        if ((flags & GRND_NONBLOCK) != 0) {
            buf.append("GRND_NONBLOCK");
        }
        if ((flags & GRND_RANDOM) != 0) {
            if (buf.length() > 0) {
                buf.append('|');
            }
            buf.append("GRND_NONBLOCK");
        }
        if ((flags & ~(GRND_NONBLOCK | GRND_RANDOM)) != 0) {
            int remainder = flags & ~(GRND_NONBLOCK | GRND_RANDOM);
            if (buf.length() > 0) {
                buf.append('|');
            }
            buf.append(remainder);
        }
        return buf.toString();
    }
}
