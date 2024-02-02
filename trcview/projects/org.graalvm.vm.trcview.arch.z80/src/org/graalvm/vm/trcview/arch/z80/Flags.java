package org.graalvm.vm.trcview.arch.z80;

import org.graalvm.vm.trcview.arch.z80.disasm.Z80Instruction;

public class Flags {
    public static final int FLAG_S = 0x80;
    public static final int FLAG_Z = 0x40;
    public static final int FLAG_H = 0x10;
    public static final int FLAG_V = 0x04;
    public static final int FLAG_N = 0x02;
    public static final int FLAG_C = 0x01;

    public static boolean condition(int cond, int flags) {
        switch (cond) {
            case Z80Instruction.Z:
                return (flags & FLAG_Z) != 0;
            case Z80Instruction.NZ:
                return (flags & FLAG_Z) == 0;
            case Z80Instruction.C:
                return (flags & FLAG_C) != 0;
            case Z80Instruction.NC:
                return (flags & FLAG_C) == 0;
            default:
                return true;
        }
    }
}
