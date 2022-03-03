package org.graalvm.vm.trcview.arch.riscv.io;

import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.util.HexFormatter;

public abstract class RiscVCpuState extends RiscVStepEvent implements CpuState {
    private static final String[] REGS = {"zero", "ra", "sp", "gp", "tp", "t0", "t1", "t2", "s0", "s1", "a0", "a1",
                    "a2", "a3", "a4", "a5", "a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10",
                    "s11", "t3", "t4", "t5", "t6"};

    protected RiscVCpuState(int tid) {
        super(tid);
    }

    public abstract int getInstruction();

    public abstract long getGPR(int reg);

    public abstract long getCSR(int reg);

    @Override
    public long get(String name) {
        switch (name) {
            // GPRs
            case "x0":
            case "x00":
            case "zero":
                return getGPR(0);
            case "x1":
            case "x01":
            case "ra":
                return getGPR(1);
            case "x2":
            case "x02":
            case "sp":
                return getGPR(2);
            case "x3":
            case "x03":
            case "gp":
                return getGPR(3);
            case "x4":
            case "x04":
            case "tp":
                return getGPR(4);
            case "x5":
            case "x05":
            case "t0":
                return getGPR(5);
            case "x6":
            case "x06":
            case "t1":
                return getGPR(6);
            case "x7":
            case "x07":
            case "t2":
                return getGPR(7);
            case "x8":
            case "x08":
            case "s0":
            case "fp":
                return getGPR(8);
            case "x9":
            case "x09":
            case "f1":
                return getGPR(9);
            case "x10":
            case "a0":
                return getGPR(10);
            case "x11":
            case "a1":
                return getGPR(11);
            case "x12":
            case "a2":
                return getGPR(12);
            case "x13":
            case "a3":
                return getGPR(13);
            case "x14":
            case "a4":
                return getGPR(14);
            case "x15":
            case "a5":
                return getGPR(15);
            case "x16":
            case "a6":
                return getGPR(16);
            case "x17":
            case "a7":
                return getGPR(17);
            case "x18":
            case "s2":
                return getGPR(18);
            case "x19":
            case "s3":
                return getGPR(19);
            case "x20":
            case "s4":
                return getGPR(20);
            case "x21":
            case "s5":
                return getGPR(21);
            case "x22":
            case "s6":
                return getGPR(22);
            case "x23":
            case "s7":
                return getGPR(23);
            case "x24":
            case "s8":
                return getGPR(24);
            case "x25":
            case "s9":
                return getGPR(25);
            case "x26":
            case "s10":
                return getGPR(26);
            case "x27":
            case "s11":
                return getGPR(27);
            case "x28":
            case "t3":
                return getGPR(28);
            case "x29":
            case "t4":
                return getGPR(29);
            case "x30":
            case "t5":
                return getGPR(30);
            case "x31":
            case "t6":
                return getGPR(31);
            case "insn":
                return getInstruction();
            case "pc":
                return (int) getPC();
            // CSRs
            case "ustatus":
                return getCSR(0);
            case "uie":
                return getCSR(4);
            case "utvec":
                return getCSR(5);
            case "uscratch":
                return getCSR(0x40);
            case "uepc":
                return getCSR(0x41);
            case "ucause":
                return getCSR(0x42);
            case "utval":
                return getCSR(0x43);
            case "uip":
                return getCSR(0x44);
            case "fflags":
                return getCSR(1);
            case "frm":
                return getCSR(2);
            case "fcsr":
                return getCSR(3);
            case "cycle":
                return getCSR(0xC00);
            case "time":
                return getCSR(0xC01);
            case "instret":
                return getCSR(0xC02);
            case "hpmcounter3":
                return getCSR(0xC03);
            case "hpmcounter4":
                return getCSR(0xC04);
            // ... until C1F (hpmcounter31)
            case "cycleh":
                return getCSR(0xC80);
            case "timeh":
                return getCSR(0xC81);
            case "instreth":
                return getCSR(0xC82);
            case "hpmcounter3h":
                return getCSR(0xC83);
            case "hpmcounter4h":
                return getCSR(0xC84);
            // ... until 0xC9F (hpmcounter31h)
            case "sstatus":
                return getCSR(0x100);
            case "sedeleg":
                return getCSR(0x102);
            case "sideleg":
                return getCSR(0x103);
            case "sie":
                return getCSR(0x104);
            case "stvec":
                return getCSR(0x105);
            case "scounteren":
                return getCSR(0x106);
            case "sscratch":
                return getCSR(0x140);
            case "sepc":
                return getCSR(0x141);
            case "scause":
                return getCSR(0x142);
            case "stval":
                return getCSR(0x143);
            case "sip":
                return getCSR(0x144);
            case "satp":
                return getCSR(0x180);
            default:
                throw new IllegalArgumentException("unknown register " + name);
        }
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 4; j++) {
                int r = i * 4 + j;
                buf.append("{{x");
                if (r < 10) {
                    buf.append('0');
                    buf.append((char) (r + '0'));
                } else {
                    buf.append(r);
                }
                if (r == 0) {
                    buf.append("}}S     ={{");
                } else {
                    buf.append("}}S({{");
                    buf.append(REGS[r]);
                    if (REGS[r].length() == 2) {
                        buf.append("}}S) ={{");
                    } else {
                        buf.append("}}S)={{");
                    }
                }
                buf.append(HexFormatter.tohex(getGPR(r), 16));
                if (j < 3) {
                    buf.append("}}x ");
                }
            }
            buf.append("}}x\n");
        }
        buf.append("\n\n");
        buf.append("{{PC}}S ");
        buf.append(HexFormatter.tohex(getPC(), 8));
        buf.append('\n');
        return buf.toString();
    }

    @Override
    public RiscVCpuState getState() {
        return this;
    }
}
