package org.graalvm.vm.trcview.arch.ppc.disasm;

public class Cr {
    public static final String[] FIELDS = {"lt", "gt", "eq", "so"};

    public static final int LT = 0;
    public static final int GT = 1;
    public static final int EQ = 2;
    public static final int SO = 3;

    private final int mask;
    private final int shift;
    private final int invmask;

    public Cr(int bf) {
        mask = 0xf << (32 - bf * 4 - 4);
        invmask = ~mask;
        shift = 32 - bf * 4 - 4;
    }

    public int get(int cr) {
        return (cr >> shift) & 0xf;
    }

    public int set(int cr, int field) {
        return (cr & invmask) | (field << shift);
    }

    public static String getName(int bit) {
        int cr = bit / 4;
        int field = bit & 0x3;
        return "4*cr" + cr + "+" + FIELDS[field];
    }
}
