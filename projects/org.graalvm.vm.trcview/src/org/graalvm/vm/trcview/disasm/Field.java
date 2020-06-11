package org.graalvm.vm.trcview.disasm;

public class Field {
    public final int from;
    public final int to;
    public final int lo;
    public final int hi;
    public final int mask;
    public final int imask;
    public final boolean signed;

    private final Value value;

    public Field(Value value, int from, int to) {
        this(value, from, to, false);
    }

    public Field(Value value, int from, int to, boolean signed) {
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

    public Field(int from, int to) {
        this(null, from, to, false);
    }

    public Field(int from, int to, boolean signed) {
        this(null, from, to, signed);
    }

    public static Field getLE(int bit) {
        return getLE(bit, bit, false);
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

    public int get() {
        if (value == null) {
            throw new IllegalStateException("no value set");
        }
        return get(value.get());
    }

    public void set(int val) {
        if (value == null) {
            throw new IllegalStateException("no value set");
        }
        value.set(set(value.get(), val));
    }

    public boolean getBit(int val) {
        if (from != to) {
            throw new IllegalStateException("not a single bit");
        }
        return get(val) != 0;
    }

    public int setBit(int insn, boolean value) {
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
        return String.format("Field[%d;%d;mask=%08X]", from, to, mask);
    }
}
