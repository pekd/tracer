package org.graalvm.vm.trcview.arch.pdp11.io;

import org.graalvm.vm.trcview.arch.io.CpuState;

public abstract class PDP11CpuState extends PDP11StepEvent implements CpuState {
    protected PDP11CpuState(int tid) {
        super(tid);
    }

    public abstract short getRegister(int id);

    public abstract short getPSW();

    public abstract short[] getMachinecodeWords();

    public short getSP() {
        return getRegister(6);
    }

    @Override
    public long getPC() {
        return Short.toUnsignedLong(getRegister(7));
    }

    @Override
    public long getRegisterById(int id) {
        if (id < 8) {
            return Short.toUnsignedLong(getRegister(id));
        } else {
            return Short.toUnsignedLong(getPSW());
        }
    }

    @Override
    public long get(String name) {
        switch (name) {
            case "r0":
            case "R0":
                return Short.toUnsignedLong(getRegister(0));
            case "r1":
            case "R1":
                return Short.toUnsignedLong(getRegister(1));
            case "r2":
            case "R2":
                return Short.toUnsignedLong(getRegister(2));
            case "r3":
            case "R3":
                return Short.toUnsignedLong(getRegister(3));
            case "r4":
            case "R4":
                return Short.toUnsignedLong(getRegister(4));
            case "r5":
            case "R5":
                return Short.toUnsignedLong(getRegister(5));
            case "r6":
            case "R6":
            case "sp":
            case "SP":
                return Short.toUnsignedLong(getRegister(6));
            case "r7":
            case "R7":
            case "pc":
            case "PC":
                return Short.toUnsignedLong(getRegister(7));
            case "psw":
            case "PSW":
                return Short.toUnsignedLong(getPSW());
            default:
                throw new IllegalArgumentException("unknown field " + name);
        }
    }

    private static void oct(StringBuilder buf, short val) {
        buf.append("{{");
        String oct = Integer.toString(Short.toUnsignedInt(val), 8);
        for (int i = oct.length(); i < 6; i++) {
            buf.append('0');
        }
        buf.append(oct);
        buf.append("}}o");
    }

    private void reg(StringBuilder buf, int i) {
        buf.append("{{R");
        buf.append((char) (i + '0'));
        buf.append("}}S=");
        oct(buf, getRegister(i));
    }

    private void psw(StringBuilder buf, char c, int bit) {
        if ((getPSW() & bit) != 0) {
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
        buf.append("{{SP}}S=");
        oct(buf, getRegister(6));
        buf.append(" {{PC}}S=");
        oct(buf, getRegister(7));
        buf.append("\nPSW=");
        oct(buf, getPSW());
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

    @Override
    public PDP11CpuState getState() {
        return this;
    }
}
