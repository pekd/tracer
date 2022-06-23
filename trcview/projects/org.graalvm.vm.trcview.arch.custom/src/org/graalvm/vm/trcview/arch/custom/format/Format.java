package org.graalvm.vm.trcview.arch.custom.format;

import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.util.OctFormatter;

public class Format {
    public static final int FORMAT_OCT = 0;
    public static final int FORMAT_DEC = 1;
    public static final int FORMAT_HEX = 2;

    public final int numberfmt;
    public final int pad;

    public Format(int numberfmt, int pad) {
        this.numberfmt = numberfmt;
        this.pad = pad;
    }

    public String format(long x) {
        switch (numberfmt) {
            case FORMAT_OCT:
                return OctFormatter.tooct(x, pad);
            case FORMAT_HEX:
                return HexFormatter.tohex(x, pad);
            case FORMAT_DEC:
            default:
                return Long.toUnsignedString(x);
        }
    }
}
