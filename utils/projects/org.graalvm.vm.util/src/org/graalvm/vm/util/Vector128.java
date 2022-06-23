package org.graalvm.vm.util;

import org.graalvm.vm.util.io.Endianess;

public class Vector128 implements Cloneable {
    private static final int SIZE = 2;

    public static final Vector128 ZERO = new Vector128();

    protected long data0;
    protected long data1;

    public Vector128() {
    }

    public Vector128(long[] data) {
        assert data.length == 2;
        this.data0 = data[0];
        this.data1 = data[1];
    }

    public Vector128(long high, long low) {
        this.data0 = high;
        this.data1 = low;
    }

    public Vector128(int a1, int a2, int a3, int a4) {
        this(Integer.toUnsignedLong(a1) << 32 | Integer.toUnsignedLong(a2), Integer.toUnsignedLong(a3) << 32 | Integer.toUnsignedLong(a4));
    }

    public Vector128(byte[] data) {
        this(Endianess.get64bitBE(data, 0), Endianess.get64bitBE(data, 8));
        assert data.length == 16;
    }

    public long getI64(int i) {
        assert i >= 0 && i < 2;
        switch (i) {
            case 0:
                return data0;
            case 1:
                return data1;
            default:
                throw new ArrayIndexOutOfBoundsException(i);
        }
    }

    public void setI64(int i, long val) {
        assert i >= 0 && i < 2;
        switch (i) {
            case 0:
                data0 = val;
                break;
            case 1:
                data1 = val;
                break;
            default:
                throw new ArrayIndexOutOfBoundsException(i);
        }
    }

    @Override
    public Vector128 clone() {
        return new Vector128(data0, data1);
    }

    public String hex() {
        return HexFormatter.tohex(data0, 16) + HexFormatter.tohex(data1, 16);
    }

    @Override
    public String toString() {
        return "0x" + hex();
    }
}
