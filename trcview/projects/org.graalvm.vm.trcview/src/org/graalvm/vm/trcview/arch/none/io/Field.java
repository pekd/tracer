package org.graalvm.vm.trcview.arch.none.io;

import org.graalvm.vm.util.io.Endianess;

public class Field {
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
    private final int offset;

    private final boolean be;

    public Field(String name, int offset, int type, boolean be) {
        this.name = name;
        this.offset = offset;
        this.type = type;
        this.be = be;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type & 0x0F;
    }

    public int getFormat() {
        return (type >> 4) & 0x0F;
    }

    private long getI8(byte[] data) {
        return Byte.toUnsignedLong(data[offset]);
    }

    private long getI16(byte[] data) {
        if (be) {
            return Endianess.get16bitBE(data, offset);
        } else {
            return Endianess.get16bitLE(data, offset);
        }
    }

    private long getI32(byte[] data) {
        if (be) {
            return Endianess.get32bitBE(data, offset);
        } else {
            return Endianess.get32bitLE(data, offset);
        }
    }

    private long getI64(byte[] data) {
        if (be) {
            return Endianess.get64bitBE(data, offset);
        } else {
            return Endianess.get64bitLE(data, offset);
        }
    }

    private static double log2(double x) {
        return Math.log(x) / Math.log(2);
    }

    private static int length(int radix, int size) {
        return (int) Math.ceil(size / log2(radix));
    }

    private static String pad(String s, int len) {
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

    private String str(long val, int len) {
        int radix;
        switch (getFormat()) {
            default:
            case FORMAT_HEX:
                radix = 16;
                break;
            case FORMAT_BIN:
                radix = 2;
                break;
            case FORMAT_OCT:
                radix = 8;
                break;
            case FORMAT_DEC:
                radix = 10;
                break;
        }
        return pad(Long.toUnsignedString(val, radix).toUpperCase(), length(radix, len));
    }

    public long getValue(byte[] data) {
        switch (getType()) {
            default:
            case TYPE_I8:
                return getI8(data);
            case TYPE_I16:
                return getI16(data);
            case TYPE_I32:
            case TYPE_F32:
                return getI32(data);
            case TYPE_I64:
            case TYPE_F64:
                return getI64(data);
        }
    }

    public int getLength() {
        switch (getType()) {
            default:
            case TYPE_I8:
                return 8;
            case TYPE_I16:
                return 16;
            case TYPE_I32:
            case TYPE_F32:
                return 32;
            case TYPE_I64:
            case TYPE_F64:
                return 64;
        }
    }

    public String format(byte[] data) {
        long val = getValue(data);
        int len = getLength();
        switch (getFormat()) {
            case FORMAT_HEX:
                return "{{" + str(val, len) + "}}x";
            case FORMAT_DEC:
                return "{{" + str(val, len) + "}}d";
            case FORMAT_OCT:
                return "{{" + str(val, len) + "}}o";
            default:
                return str(val, len);
        }
    }
}
