package org.graalvm.vm.trcview.arch.x86.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.x86.decode.isa.Flags;
import org.graalvm.vm.trcview.net.protocol.IO;
import org.graalvm.vm.util.BitTest;
import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.util.Vector128;
import org.graalvm.vm.util.io.WordInputStream;

public abstract class AMD64CpuState extends AMD64StepEvent implements CpuState {
    protected static final int RAX = 0;
    protected static final int RCX = 1;
    protected static final int RDX = 2;
    protected static final int RBX = 3;
    protected static final int RSP = 4;
    protected static final int RBP = 5;
    protected static final int RSI = 6;
    protected static final int RDI = 7;
    protected static final int R8 = 8;
    protected static final int R9 = 9;
    protected static final int R10 = 10;
    protected static final int R11 = 11;
    protected static final int R12 = 12;
    protected static final int R13 = 13;
    protected static final int R14 = 14;
    protected static final int R15 = 15;

    protected static final int ID_RAX = 0;
    protected static final int ID_RCX = 1;
    protected static final int ID_RDX = 2;
    protected static final int ID_RBX = 3;
    protected static final int ID_RSP = 4;
    protected static final int ID_RBP = 5;
    protected static final int ID_RSI = 6;
    protected static final int ID_RDI = 7;
    protected static final int ID_R8 = 8;
    protected static final int ID_R9 = 9;
    protected static final int ID_R10 = 10;
    protected static final int ID_R11 = 11;
    protected static final int ID_R12 = 12;
    protected static final int ID_R13 = 13;
    protected static final int ID_R14 = 14;
    protected static final int ID_R15 = 15;
    protected static final int ID_FS = 16;
    protected static final int ID_GS = 17;
    protected static final int ID_RFL = 18;

    protected long step;

    protected AMD64CpuState(int tid, byte[] machinecode) {
        super(tid, machinecode);
    }

    @Override
    public long getStep() {
        return step;
    }

    @Override
    public long getPC() {
        return getRIP();
    }

    public abstract long getRAX();

    public abstract long getRBX();

    public abstract long getRCX();

    public abstract long getRDX();

    public abstract long getRBP();

    public abstract long getRSP();

    public abstract long getRIP();

    public abstract long getRSI();

    public abstract long getRDI();

    public abstract long getR8();

    public abstract long getR9();

    public abstract long getR10();

    public abstract long getR11();

    public abstract long getR12();

    public abstract long getR13();

    public abstract long getR14();

    public abstract long getR15();

    public abstract long getRFL();

    public abstract long getFS();

    public abstract long getGS();

    public abstract Vector128 getXMM(int i);

    public static AMD64CpuState readRecord(WordInputStream in, int tid) throws IOException {
        byte[] machinecode = IO.readArray(in);
        return new AMD64FullCpuState(in, tid, machinecode);
    }

    @Override
    public AMD64CpuState getState() {
        return this;
    }

    @Override
    public long get(String name) {
        switch (name) {
            case "step":
                return getStep();
            case "pc":
                return getPC();
            case "al":
                return getRAX() & 0xFF;
            case "ax":
                return getRAX() & 0xFFFF;
            case "eax":
                return getRAX() & 0xFFFFFFFFL;
            case "rax":
                return getRAX();
            case "bl":
                return getRBX() & 0xFF;
            case "bx":
                return getRBX() & 0xFFFF;
            case "ebx":
                return getRBX() & 0xFFFFFFFFL;
            case "rbx":
                return getRBX();
            case "cl":
                return getRCX() & 0xFF;
            case "cx":
                return getRCX() & 0xFFFF;
            case "ecx":
                return getRCX() & 0xFFFFFFFFL;
            case "rcx":
                return getRCX();
            case "dl":
                return getRDX() & 0xFF;
            case "dx":
                return getRDX() & 0xFFFF;
            case "edx":
                return getRDX() & 0xFFFFFFFFL;
            case "rdx":
                return getRDX();
            case "bpl":
                return getRBP() & 0xFF;
            case "bp":
                return getRBP() & 0xFFFF;
            case "ebp":
                return getRBP() & 0xFFFFFFFFL;
            case "rbp":
                return getRBP();
            case "spl":
                return getRSP() & 0xFF;
            case "sp":
                return getRSP() & 0xFFFF;
            case "esp":
                return getRSP() & 0xFFFFFFFFL;
            case "rsp":
                return getRSP();
            case "ip":
                return getRIP() & 0xFFFF;
            case "eip":
                return getRIP() & 0xFFFFFFFFL;
            case "rip":
                return getRIP();
            case "sil":
                return getRSI() & 0xFF;
            case "si":
                return getRSI() & 0xFFFF;
            case "esi":
                return getRSI() & 0xFFFFFFFFL;
            case "rsi":
                return getRSI();
            case "dil":
                return getRDI() & 0xFF;
            case "di":
                return getRDI() & 0xFFFF;
            case "edi":
                return getRDI() & 0xFFFFFFFFL;
            case "rdi":
                return getRDI();
            case "r8b":
                return getR8() & 0xFF;
            case "r8w":
                return getR8() & 0xFFFF;
            case "r8d":
                return getR8() & 0xFFFFFFFFL;
            case "r8":
                return getR8();
            case "r9b":
                return getR9() & 0xFF;
            case "r9w":
                return getR9() & 0xFFFF;
            case "r9d":
                return getR9() & 0xFFFFFFFFL;
            case "r9":
                return getR9();
            case "r10b":
                return getR10() & 0xFF;
            case "r10w":
                return getR10() & 0xFFFF;
            case "r10d":
                return getR10() & 0xFFFFFFFFL;
            case "r10":
                return getR10();
            case "r11b":
                return getR11() & 0xFF;
            case "r11w":
                return getR11() & 0xFFFF;
            case "r11d":
                return getR11() & 0xFFFFFFFFL;
            case "r11":
                return getR11();
            case "r12b":
                return getR12() & 0xFF;
            case "r12w":
                return getR12() & 0xFFFF;
            case "r12d":
                return getR12() & 0xFFFFFFFFL;
            case "r12":
                return getR12();
            case "r13b":
                return getR13() & 0xFF;
            case "r13w":
                return getR13() & 0xFFFF;
            case "r13d":
                return getR13() & 0xFFFFFFFFL;
            case "r13":
                return getR13();
            case "r14b":
                return getR14() & 0xFF;
            case "r14w":
                return getR14() & 0xFFFF;
            case "r14d":
                return getR14() & 0xFFFFFFFFL;
            case "r14":
                return getR14();
            case "r15b":
                return getR15() & 0xFF;
            case "r15w":
                return getR15() & 0xFFFF;
            case "r15d":
                return getR15() & 0xFFFFFFFFL;
            case "r15":
                return getR15();
            case "flags":
                return getRFL() & 0xFFFF;
            case "eflags":
                return getRFL() & 0xFFFFFFFFL;
            case "rflags":
                return getRFL();
            default:
                if (name.startsWith("xmm") && (name.endsWith(".l") || name.endsWith(".h"))) {
                    try {
                        int id = Integer.parseInt(name.substring(3, name.length() - 2));
                        Vector128 reg = getXMM(id);
                        if (name.endsWith(".l")) {
                            return reg.getI64(1);
                        } else {
                            return reg.getI64(0);
                        }
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("unknown field " + name);
                    }
                }
                throw new IllegalArgumentException("unknown field " + name);
        }
    }

    private static StringBuilder formatRegLine(StringBuilder buf, String[] names, long[] values) {
        for (int i = 0; i < names.length; i++) {
            if (i > 0) {
                buf.append(' ');
            }
            buf.append("{{");
            buf.append(names[i]);
            buf.append("}}S={{");
            buf.append(HexFormatter.tohex(values[i], 16));
            buf.append("}}x");
        }
        buf.append('\n');
        return buf;
    }

    private static void addFlag(StringBuilder buf, long rfl, long flag, char name) {
        if (BitTest.test(rfl, 1L << flag)) {
            buf.append(name);
        } else {
            buf.append('-');
        }
    }

    private static void addSegment(StringBuilder buf, String name, long segment) {
        buf.append(name);
        buf.append(" =0000 {{");
        buf.append(HexFormatter.tohex(segment, 16));
        buf.append("}}x 00000000 00000000\n");
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        long rfl = getRFL();
        formatRegLine(buf, new String[]{"RAX", "RBX", "RCX", "RDX"}, new long[]{getRAX(), getRBX(), getRCX(), getRDX()});
        formatRegLine(buf, new String[]{"RSI", "RDI", "RBP", "RSP"}, new long[]{getRSI(), getRDI(), getRBP(), getRSP()});
        formatRegLine(buf, new String[]{"R8 ", "R9 ", "R10", "R11"}, new long[]{getR8(), getR9(), getR10(), getR11()});
        formatRegLine(buf, new String[]{"R12", "R13", "R14", "R15"}, new long[]{getR12(), getR13(), getR14(), getR15()});
        buf.append("{{RIP}}S={{").append(HexFormatter.tohex(getRIP(), 16)).append("}}x");
        buf.append(" RFL=").append(HexFormatter.tohex(rfl, 8));
        buf.append(" [");
        addFlag(buf, rfl, Flags.OF, 'O');
        addFlag(buf, rfl, Flags.DF, 'D');
        addFlag(buf, rfl, Flags.SF, 'S');
        addFlag(buf, rfl, Flags.ZF, 'Z');
        addFlag(buf, rfl, Flags.AF, 'A');
        addFlag(buf, rfl, Flags.PF, 'P');
        addFlag(buf, rfl, Flags.CF, 'C');
        buf.append("]\n");
        addSegment(buf, "FS", getFS());
        addSegment(buf, "GS", getGS());
        for (int i = 0; i < 16; i++) {
            buf.append("XMM").append(i);
            if (i < 10) {
                buf.append(' ');
            }
            buf.append("={{");
            buf.append(getXMM(i).hex());
            if (i % 2 == 0) {
                buf.append("}}x ");
            } else {
                buf.append("}}x\n");
            }
        }
        return buf.toString();
    }
}
