package org.graalvm.vm.trcview.arch.x86.io;

import java.io.IOException;

import org.graalvm.vm.memory.vector.Vector128;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.x86.AMD64;
import org.graalvm.vm.util.BitTest;
import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;
import org.graalvm.vm.x86.isa.Flags;
import org.graalvm.vm.x86.node.debug.trace.CpuStateRecord;

public class AMD64CpuState extends CpuState {
    public final long rax;
    public final long rbx;
    public final long rcx;
    public final long rdx;
    public final long rsi;
    public final long rdi;
    public final long rbp;
    public final long rsp;
    public final long r8;
    public final long r9;
    public final long r10;
    public final long r11;
    public final long r12;
    public final long r13;
    public final long r14;
    public final long r15;
    public final long rip;

    public final long fs;
    public final long gs;

    public final long rfl;

    public final Vector128[] xmm = new Vector128[16];

    public final long step;

    private AMD64CpuState(WordInputStream in, int tid) throws IOException {
        super(AMD64.ID, tid);
        rax = in.read64bit();
        rbx = in.read64bit();
        rcx = in.read64bit();
        rdx = in.read64bit();
        rsi = in.read64bit();
        rdi = in.read64bit();
        rbp = in.read64bit();
        rsp = in.read64bit();
        r8 = in.read64bit();
        r9 = in.read64bit();
        r10 = in.read64bit();
        r11 = in.read64bit();
        r12 = in.read64bit();
        r13 = in.read64bit();
        r14 = in.read64bit();
        r15 = in.read64bit();
        rip = in.read64bit();
        fs = in.read64bit();
        gs = in.read64bit();
        rfl = in.read64bit();
        step = in.read64bit();
        for (int i = 0; i < xmm.length; i++) {
            long v0 = in.read64bit();
            long v1 = in.read64bit();
            xmm[i] = new Vector128(v0, v1);
        }

    }

    public AMD64CpuState(CpuStateRecord record) {
        super(AMD64.ID, record.getTid());
        org.graalvm.vm.x86.isa.CpuState state = record.getState();
        rax = state.rax;
        rbx = state.rbx;
        rcx = state.rcx;
        rdx = state.rdx;
        rsi = state.rsi;
        rdi = state.rdi;
        rbp = state.rbp;
        rsp = state.rsp;
        r8 = state.r8;
        r9 = state.r9;
        r10 = state.r10;
        r11 = state.r11;
        r12 = state.r12;
        r13 = state.r13;
        r14 = state.r14;
        r15 = state.r15;
        rip = state.rip;
        fs = state.fs;
        gs = state.gs;
        for (int i = 0; i < xmm.length; i++) {
            xmm[i] = state.xmm[i];
        }
        rfl = state.getRFL();
        step = record.getInstructionCount();
    }

    @Override
    public long getStep() {
        return step;
    }

    @Override
    public long getPC() {
        return rip;
    }

    @Override
    public long get(String name) {
        switch (name) {
            case "step":
                return getStep();
            case "pc":
                return getPC();
            case "al":
                return rax & 0xFF;
            case "ax":
                return rax & 0xFFFF;
            case "eax":
                return rax & 0xFFFFFFFFL;
            case "rax":
                return rax;
            case "bl":
                return rbx & 0xFF;
            case "bx":
                return rbx & 0xFFFF;
            case "ebx":
                return rbx & 0xFFFFFFFFL;
            case "rbx":
                return rbx;
            case "cl":
                return rcx & 0xFF;
            case "cx":
                return rcx & 0xFFFF;
            case "ecx":
                return rcx & 0xFFFFFFFFL;
            case "rcx":
                return rcx;
            case "dl":
                return rdx & 0xFF;
            case "dx":
                return rdx & 0xFFFF;
            case "edx":
                return rdx & 0xFFFFFFFFL;
            case "rdx":
                return rdx;
            case "bpl":
                return rbp & 0xFF;
            case "bp":
                return rbp & 0xFFFF;
            case "ebp":
                return rbp & 0xFFFFFFFFL;
            case "rbp":
                return rbp;
            case "spl":
                return rsp & 0xFF;
            case "sp":
                return rsp & 0xFFFF;
            case "esp":
                return rsp & 0xFFFFFFFFL;
            case "rsp":
                return rsp;
            case "ip":
                return rip & 0xFFFF;
            case "eip":
                return rip & 0xFFFFFFFFL;
            case "rip":
                return rip;
            case "sil":
                return rsi & 0xFF;
            case "si":
                return rsi & 0xFFFF;
            case "esi":
                return rsi & 0xFFFFFFFFL;
            case "rsi":
                return rsi;
            case "dil":
                return rdi & 0xFF;
            case "di":
                return rdi & 0xFFFF;
            case "edi":
                return rdi & 0xFFFFFFFFL;
            case "rdi":
                return rdi;
            case "r8b":
                return r8 & 0xFF;
            case "r8w":
                return r8 & 0xFFFF;
            case "r8d":
                return r8 & 0xFFFFFFFFL;
            case "r8":
                return r8;
            case "r9b":
                return r9 & 0xFF;
            case "r9w":
                return r9 & 0xFFFF;
            case "r9d":
                return r9 & 0xFFFFFFFFL;
            case "r9":
                return r9;
            case "r10b":
                return r10 & 0xFF;
            case "r10w":
                return r10 & 0xFFFF;
            case "r10d":
                return r10 & 0xFFFFFFFFL;
            case "r10":
                return r10;
            case "r11b":
                return r11 & 0xFF;
            case "r11w":
                return r11 & 0xFFFF;
            case "r11d":
                return r11 & 0xFFFFFFFFL;
            case "r11":
                return r11;
            case "r12b":
                return r12 & 0xFF;
            case "r12w":
                return r12 & 0xFFFF;
            case "r12d":
                return r12 & 0xFFFFFFFFL;
            case "r12":
                return r12;
            case "r13b":
                return r13 & 0xFF;
            case "r13w":
                return r13 & 0xFFFF;
            case "r13d":
                return r13 & 0xFFFFFFFFL;
            case "r13":
                return r13;
            case "r14b":
                return r14 & 0xFF;
            case "r14w":
                return r14 & 0xFFFF;
            case "r14d":
                return r14 & 0xFFFFFFFFL;
            case "r14":
                return r14;
            case "r15b":
                return r15 & 0xFF;
            case "r15w":
                return r15 & 0xFFFF;
            case "r15d":
                return r15 & 0xFFFFFFFFL;
            case "r15":
                return r15;
            case "flags":
                return rfl & 0xFFFF;
            case "eflags":
                return rfl & 0xFFFFFFFFL;
            case "rflags":
                return rfl;
            default:
                if (name.startsWith("xmm") && (name.endsWith(".l") || name.endsWith(".h"))) {
                    try {
                        int id = Integer.parseInt(name.substring(3, name.length() - 2));
                        Vector128 reg = xmm[id];
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

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        out.write64bit(rax);
        out.write64bit(rbx);
        out.write64bit(rcx);
        out.write64bit(rdx);
        out.write64bit(rsi);
        out.write64bit(rdi);
        out.write64bit(rbp);
        out.write64bit(rsp);
        out.write64bit(r8);
        out.write64bit(r9);
        out.write64bit(r10);
        out.write64bit(r11);
        out.write64bit(r12);
        out.write64bit(r13);
        out.write64bit(r14);
        out.write64bit(r15);
        out.write64bit(rip);
        out.write64bit(fs);
        out.write64bit(gs);
        out.write64bit(rfl);
        out.write64bit(step);
        for (int i = 0; i < xmm.length; i++) {
            out.write64bit(xmm[i].getI64(0));
            out.write64bit(xmm[i].getI64(1));
        }
    }

    public static AMD64CpuState readRecord(WordInputStream in, int tid) throws IOException {
        return new AMD64CpuState(in, tid);
    }

    private static StringBuilder formatRegLine(StringBuilder buf, String[] names, long[] values) {
        for (int i = 0; i < names.length; i++) {
            if (i > 0) {
                buf.append(' ');
            }
            buf.append(names[i]);
            buf.append("={{");
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
        formatRegLine(buf, new String[]{"RAX", "RBX", "RCX", "RDX"}, new long[]{rax, rbx, rcx, rdx});
        formatRegLine(buf, new String[]{"RSI", "RDI", "RBP", "RSP"}, new long[]{rsi, rdi, rbp, rsp});
        formatRegLine(buf, new String[]{"R8 ", "R9 ", "R10", "R11"}, new long[]{r8, r9, r10, r11});
        formatRegLine(buf, new String[]{"R12", "R13", "R14", "R15"}, new long[]{r12, r13, r14, r15});
        buf.append("RIP={{").append(HexFormatter.tohex(rip, 16)).append("}}x");
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
        addSegment(buf, "FS", fs);
        addSegment(buf, "GS", gs);
        for (int i = 0; i < 16; i++) {
            buf.append("XMM").append(i);
            if (i < 10) {
                buf.append(' ');
            }
            buf.append("={{");
            buf.append(xmm[i].hex());
            if (i % 2 == 0) {
                buf.append("}}x ");
            } else {
                buf.append("}}x\n");
            }
        }
        return buf.toString();
    }
}
