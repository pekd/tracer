package org.graalvm.vm.trcview.arch.pdp11.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.pdp11.PDP11;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class PDP11CpuState extends CpuState {
    private final short[] registers = new short[8];
    private final short psw;
    private final long step;
    private final short[] insn = new short[3];

    public PDP11CpuState(WordInputStream in, int tid) throws IOException {
        super(PDP11.ID, tid);
        for (int i = 0; i < 8; i++) {
            registers[i] = in.read16bit();
        }
        psw = in.read16bit();
        for (int i = 0; i < 3; i++) {
            insn[i] = in.read16bit();
        }
        in.read32bit();
        step = in.read64bit();
    }

    @Override
    public long getStep() {
        return step;
    }

    @Override
    public long getPC() {
        return Short.toUnsignedLong(registers[7]);
    }

    @Override
    public long get(String name) {
        switch (name) {
            case "r0":
            case "R0":
                return Short.toUnsignedLong(registers[0]);
            case "r1":
            case "R1":
                return Short.toUnsignedLong(registers[1]);
            case "r2":
            case "R2":
                return Short.toUnsignedLong(registers[2]);
            case "r3":
            case "R3":
                return Short.toUnsignedLong(registers[3]);
            case "r4":
            case "R4":
                return Short.toUnsignedLong(registers[4]);
            case "r5":
            case "R5":
                return Short.toUnsignedLong(registers[5]);
            case "r6":
            case "R6":
                return Short.toUnsignedLong(registers[6]);
            case "r7":
            case "R7":
                return Short.toUnsignedLong(registers[7]);
            case "sp":
            case "SP":
                return Short.toUnsignedLong(registers[6]);
            case "pc":
            case "PC":
                return Short.toUnsignedLong(registers[7]);
            case "psw":
            case "PSW":
                return Short.toUnsignedLong(psw);
            default:
                throw new IllegalArgumentException("unknown field " + name);
        }
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {

    }

    public short[] getMachinecode() {
        return insn;
    }

    public short getSP() {
        return registers[6];
    }

    public short getPSW() {
        return psw;
    }

    public short getRegister(int i) {
        return registers[i];
    }

    private static void oct(StringBuilder buf, short val) {
        String oct = Integer.toString(Short.toUnsignedInt(val), 8);
        for (int i = oct.length(); i < 6; i++) {
            buf.append('0');
        }
        buf.append(oct);
    }

    private void reg(StringBuilder buf, int i) {
        buf.append('R');
        buf.append((char) (i + '0'));
        buf.append('=');
        oct(buf, registers[i]);
    }

    private void psw(StringBuilder buf, char c, int bit) {
        if ((psw & bit) != 0) {
            buf.append(c);
        } else {
            buf.append('-');
        }
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        reg(buf, 0);
        buf.append(' ');
        reg(buf, 1);
        buf.append('\n');
        reg(buf, 2);
        buf.append(' ');
        reg(buf, 3);
        buf.append('\n');
        reg(buf, 4);
        buf.append(' ');
        reg(buf, 5);
        buf.append('\n');
        buf.append("SP=");
        oct(buf, registers[6]);
        buf.append(" PC=");
        oct(buf, registers[7]);
        buf.append("\nPSW=");
        oct(buf, psw);
        buf.append(" [");
        psw(buf, 'P', 0x80);
        psw(buf, 'T', 0x10);
        psw(buf, 'N', 0x08);
        psw(buf, 'Z', 0x04);
        psw(buf, 'V', 0x02);
        psw(buf, 'C', 0x01);
        buf.append("]\n");
        return buf.toString();
    }
}
