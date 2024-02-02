package org.graalvm.vm.trcview.arch.z80.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.util.io.WordInputStream;

public class Z80CpuState extends Z80StepEvent implements CpuState {
    private final short pc;
    private final short af;
    private final short bc;
    private final short de;
    private final short hl;
    private final short ix;
    private final short iy;
    private final short sp;
    private final byte[] machinecode;

    public Z80CpuState(int tid, long step, WordInputStream in) throws IOException {
        super(tid, step);
        int codelen = in.read8bit();
        pc = in.read16bit();
        af = in.read16bit();
        bc = in.read16bit();
        de = in.read16bit();
        hl = in.read16bit();
        ix = in.read16bit();
        iy = in.read16bit();
        sp = in.read16bit();
        machinecode = new byte[codelen];
        in.read(machinecode);
    }

    @Override
    public byte[] getMachinecode() {
        return machinecode;
    }

    public long get(String name) {
        switch (name.toLowerCase()) {
            case "af":
                return Short.toUnsignedInt(getAF());
            case "a":
                return Byte.toUnsignedInt(getA());
            case "f":
                return Byte.toUnsignedInt(getF());
            case "bc":
                return Short.toUnsignedInt(getBC());
            case "b":
                return Byte.toUnsignedInt(getB());
            case "c":
                return Byte.toUnsignedInt(getC());
            case "de":
                return Short.toUnsignedInt(getDE());
            case "d":
                return Byte.toUnsignedInt(getD());
            case "e":
                return Byte.toUnsignedInt(getE());
            case "hl":
                return Short.toUnsignedInt(getHL());
            case "h":
                return Byte.toUnsignedInt(getH());
            case "l":
                return Byte.toUnsignedInt(getL());
            case "ix":
                return Short.toUnsignedInt(getIX());
            case "iy":
                return Short.toUnsignedInt(getIY());
            case "sp":
                return Short.toUnsignedInt(getSP());
            case "pc":
                return getPC();
            default:
                throw new IllegalArgumentException("unknown field " + name);
        }
    }

    public short getAF() {
        return af;
    }

    public byte getA() {
        return (byte) (af >>> 8);
    }

    public byte getF() {
        return (byte) af;
    }

    public short getBC() {
        return bc;
    }

    public byte getB() {
        return (byte) (bc >>> 8);
    }

    public byte getC() {
        return (byte) bc;
    }

    public short getDE() {
        return de;
    }

    public byte getD() {
        return (byte) (de >>> 8);
    }

    public byte getE() {
        return (byte) de;
    }

    public short getHL() {
        return hl;
    }

    public byte getH() {
        return (byte) (hl >>> 8);
    }

    public byte getL() {
        return (byte) hl;
    }

    public short getIX() {
        return ix;
    }

    public short getIY() {
        return iy;
    }

    public short getSP() {
        return sp;
    }

    @Override
    public Z80CpuState getState() {
        return this;
    }

    @Override
    public long getPC() {
        return Short.toUnsignedInt(pc);
    }

    private static void hex(StringBuilder buf, byte val) {
        buf.append("{{");
        String hex = Integer.toString(Byte.toUnsignedInt(val), 16).toUpperCase();
        if (hex.length() < 2) {
            buf.append('0');
        }
        buf.append(hex);
        buf.append("}}x");
    }

    private static void hex(StringBuilder buf, short val) {
        buf.append("{{");
        String hex = Integer.toString(Short.toUnsignedInt(val), 16).toUpperCase();
        for (int i = hex.length(); i < 4; i++) {
            buf.append('0');
        }
        buf.append(hex);
        buf.append("}}x");
    }

    private static void reg(StringBuilder buf, String name, short val) {
        buf.append("{{");
        buf.append(name);
        buf.append("}}S=");
        hex(buf, val);
    }

    private void psw(StringBuilder buf, char c, int bit) {
        if ((getF() & bit) != 0) {
            buf.append(c);
        } else {
            buf.append('-');
        }
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        reg(buf, "AF", getAF());
        buf.append(' ');
        reg(buf, "BC", getBC());
        buf.append('\n');
        reg(buf, "DE", getDE());
        buf.append(' ');
        reg(buf, "HL", getHL());
        buf.append('\n');
        reg(buf, "IX", getIX());
        buf.append(' ');
        reg(buf, "IY", getIY());
        buf.append('\n');
        reg(buf, "PC", (short) getPC());
        buf.append(' ');
        reg(buf, "SP", getSP());
        buf.append("\nPSW=");
        hex(buf, getF());
        buf.append(" [");
        psw(buf, 'S', 0x80);
        psw(buf, 'Z', 0x40);
        psw(buf, 'H', 0x10);
        psw(buf, 'V', 0x04);
        psw(buf, 'N', 0x02);
        psw(buf, 'C', 0x01);
        buf.append("]\n");
        return buf.toString();
    }
}
