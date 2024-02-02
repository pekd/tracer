package org.graalvm.vm.trcview.arch;

public abstract class CodeReader {
    public long pc;
    protected int n;
    private final boolean isBE;

    protected CodeReader(long pc, boolean isBE) {
        this.pc = pc;
        this.isBE = isBE;
    }

    public abstract byte peekI8(int offset);

    public abstract byte nextI8();

    public short nextI16() {
        byte b1 = nextI8();
        byte b2 = nextI8();
        if (isBE) {
            return (short) ((Byte.toUnsignedInt(b1) << 8) | Byte.toUnsignedInt(b2));
        } else {
            return (short) (Byte.toUnsignedInt(b1) | (Byte.toUnsignedInt(b2) << 8));
        }
    }

    public int nextI32() {
        byte b1 = nextI8();
        byte b2 = nextI8();
        byte b3 = nextI8();
        byte b4 = nextI8();
        if (isBE) {
            return (Byte.toUnsignedInt(b1) << 24) | (Byte.toUnsignedInt(b2) << 16) | (Byte.toUnsignedInt(b3) << 8) | Byte.toUnsignedInt(b4);
        } else {
            return Byte.toUnsignedInt(b1) | (Byte.toUnsignedInt(b2) << 8) | (Byte.toUnsignedInt(b3) << 16) | (Byte.toUnsignedInt(b4) << 24);
        }
    }

    public long nextI64() {
        byte a = nextI8();
        byte b = nextI8();
        byte c = nextI8();
        byte d = nextI8();
        byte e = nextI8();
        byte f = nextI8();
        byte g = nextI8();
        byte h = nextI8();
        if (isBE) {
            return (Byte.toUnsignedLong(a) << 56) | (Byte.toUnsignedLong(b) << 48) | (Byte.toUnsignedLong(c) << 40) | (Byte.toUnsignedLong(d) << 32) | (Byte.toUnsignedLong(e) << 24) |
                            (Byte.toUnsignedLong(f) << 16) | (Byte.toUnsignedLong(g) << 8) | Byte.toUnsignedLong(h);
        } else {
            return Byte.toUnsignedLong(a) | (Byte.toUnsignedLong(b) << 8) | (Byte.toUnsignedLong(c) << 16) | (Byte.toUnsignedLong(d) << 24) | (Byte.toUnsignedLong(e) << 32) |
                            (Byte.toUnsignedLong(f) << 40) | (Byte.toUnsignedLong(g) << 48) | (Byte.toUnsignedLong(h) << 56);
        }
    }

    public int n() {
        return n;
    }

    public long getPC() {
        return pc;
    }

    public boolean isBE() {
        return isBE;
    }

    @Override
    public abstract CodeReader clone();
}
