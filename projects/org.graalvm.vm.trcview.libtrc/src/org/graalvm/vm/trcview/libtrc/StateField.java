package org.graalvm.vm.trcview.libtrc;

public class StateField {
    public static final byte TYPE_I8 = 0;
    public static final byte TYPE_I16 = 1;
    public static final byte TYPE_I32 = 2;
    public static final byte TYPE_I64 = 3;
    public static final byte TYPE_F32 = 4;
    public static final byte TYPE_F64 = 5;

    public static final byte FORMAT_HEX = 0;
    public static final byte FORMAT_BIN = 1;
    public static final byte FORMAT_OCT = 2;
    public static final byte FORMAT_DEC = 3;

    private final String name;
    private final int type;
    private final int format;
    private final int offset;

    public StateField(String name, int type, int format, int offset) {
        this.name = name;
        this.type = type;
        this.format = format;
        this.offset = offset;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public int getFormat() {
        return format;
    }

    public int getOffset() {
        return offset;
    }
}
