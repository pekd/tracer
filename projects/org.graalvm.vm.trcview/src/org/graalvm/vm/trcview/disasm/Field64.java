package org.graalvm.vm.trcview.disasm;

public class Field64 {
    public final int from;
    public final int to;
    public final int lo;
    public final int hi;
    public final long mask;
    public final long imask;
    public final boolean signed;

    private final Value value;

    public Field64(Value value, int from, int to) {
        this(value, from, to, false);
    }

    public Field64(Value value, int from, int to, boolean signed) {
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
        this.value = value;
    }

    public Field64(int from, int to) {
        this(null, from, to, false);
    }

    public Field64(int from, int to, boolean signed) {
        this(null, from, to, signed);
    }

    public static Field64 getLE(int bit) {
        return getLE(bit, bit, false);
    }

    public static Field64 getLE(int from, int to) {
        return getLE(from, to, false);
    }

    public static Field64 getLE(int from, int to, boolean signed) {
        return new Field64(bit(from), bit(to), signed);
    }

    private static int bit(int i) {
        return 63 - i;
    }

    private long mask() {
        int result = 0;
        for (int i = from; i <= to; i++) {
            result |= 1L << bit(i);
        }
        return result;
    }

    public long get(long mw) {
        if (signed) {
            return (mw << bit(hi)) >> (bit(hi) + lo);
        } else {
            return (mw >>> lo) & (mask >> lo);
        }
    }

    public long set(long insn, long value) {
        return (insn & imask) | ((value << lo) & mask);
    }

    public long get() {
        if (value == null) {
            throw new IllegalStateException("no value set");
        }
        return get(value.get64());
    }

    public void set(long val) {
        if (value == null) {
            throw new IllegalStateException("no value set");
        }
        value.set64(set(value.get64(), val));
    }

    public boolean getBit(long val) {
        if (from != to) {
            throw new IllegalStateException("not a single bit");
        }
        return get(val) != 0;
    }

    public long setBit(long insn, boolean value) {
        if (from != to) {
            throw new IllegalStateException("not a single bit");
        }
        return set(insn, value ? 1 : 0);
    }

    public boolean getBit() {
        if (value == null) {
            throw new IllegalStateException("no value set");
        }
        if (from != to) {
            throw new IllegalStateException("not a single bit");
        }
        return get() != 0;
    }

    public void getBit(boolean val) {
        if (value == null) {
            throw new IllegalStateException("no value set");
        }
        if (from != to) {
            throw new IllegalStateException("not a single bit");
        }
        set(val ? 1 : 0);
    }

    public int size() {
        return to - from + 1;
    }

    @Override
    public String toString() {
        return String.format("Field64[%d;%d;mask=%08X]", from, to, mask);
    }
}
