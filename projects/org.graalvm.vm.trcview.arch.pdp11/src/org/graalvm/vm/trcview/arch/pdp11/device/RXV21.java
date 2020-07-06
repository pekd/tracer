package org.graalvm.vm.trcview.arch.pdp11.device;

public class RXV21 {
    public static final int FILL = 0;
    public static final int EMPTY = 1;
    public static final int WRITE = 2;
    public static final int READ = 3;
    public static final int SET_MD = 4;
    public static final int STATUS = 5;
    public static final int WRITE_DD = 6;
    public static final int READ_ERR = 7;

    public static String getName(int cmd) {
        switch (cmd) {
            case FILL:
                return "fill buffer";
            case EMPTY:
                return "empty buffer";
            case WRITE:
                return "write sector";
            case READ:
                return "read sector";
            case SET_MD:
                return "set media density";
            case STATUS:
                return "read status";
            case WRITE_DD:
                return "write (deleted data) sector";
            case READ_ERR:
                return "read error code";
            default:
                return "???";
        }
    }
}
