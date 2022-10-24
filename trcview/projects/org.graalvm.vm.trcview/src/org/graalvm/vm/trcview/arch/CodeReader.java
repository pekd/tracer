package org.graalvm.vm.trcview.arch;

public abstract class CodeReader {
    public long pc;
    protected int n;
    private boolean isBE;

    protected CodeReader(long pc, boolean isBE) {
        this.pc = pc;
        this.isBE = isBE;
    }

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
            return (Byte.toUnsignedInt(b1) << 24) | (Byte.toUnsignedInt(b2) << 16) | (Byte.toUnsignedInt(b3) << 8) | Byte.toUnsignedInt(b2);
        } else {
            return Byte.toUnsignedInt(b1) | (Byte.toUnsignedInt(b2) << 8) | (Byte.toUnsignedInt(b3) << 16) | (Byte.toUnsignedInt(b4) << 24);
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
}
