package org.graalvm.vm.x86.trcview.disasm;

public class Field {
    public final int from;
    public final int to;
    public final int lo;
    public final int hi;
    public final int mask;
    public final int imask;
    public final boolean signed;

    public Field(int from, int to) {
        this(from, to, false);
    }

    public Field(int from, int to, boolean signed) {
        if (from > to) {
            throw new IllegalArgumentException("from > to");
        }
        this.from = from;
        this.to = to;
        this.lo = bit(to);
        this.hi = bit(from);
        this.signed = signed;
        this.mask = mask();
        this.imask = ~mask;
    }

    public static Field getLE(int from, int to) {
        return getLE(from, to, false);
    }

    public static Field getLE(int from, int to, boolean signed) {
        return new Field(31 - from, 31 - to, signed);
    }

    private static int bit(int i) {
        return 31 - i;
    }

    private int mask() {
        int result = 0;
        for (int i = from; i <= to; i++) {
            result |= 1 << bit(i);
        }
        return result;
    }

    public int get(int mw) {
        if (signed) {
            return (mw << (31 - hi)) >> ((31 - hi) + lo);
        } else {
            return (mw >>> lo) & (mask >> lo);
        }
    }

    public int set(int insn, int value) {
        return (insn & imask) | ((value << lo) & mask);
    }

    public boolean getBit(int value) {
        if (from != to) {
            throw new IllegalStateException("not a single bit");
        }
        return get(value) != 0;
    }

    public int setBit(int insn, boolean value) {
        if (from != to) {
            throw new IllegalStateException("not a single bit");
        }
        return set(insn, value ? 1 : 0);
    }

    public int size() {
        return to - from + 1;
    }

    @Override
    public String toString() {
        return String.format("Field[%d;%d;mask=%08X]", from, to, mask);
    }
}
