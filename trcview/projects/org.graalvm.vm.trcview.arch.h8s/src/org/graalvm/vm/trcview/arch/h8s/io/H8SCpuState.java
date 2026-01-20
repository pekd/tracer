package org.graalvm.vm.trcview.arch.h8s.io;

import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.util.HexFormatter;

public abstract class H8SCpuState extends H8SStepEvent implements CpuState {
    protected int pc;
    protected byte[] machinecode;

    protected static final int BIT_ER0 = 0x0001;
    protected static final int BIT_ER1 = 0x0002;
    protected static final int BIT_ER2 = 0x0004;
    protected static final int BIT_ER3 = 0x0008;
    protected static final int BIT_ER4 = 0x0010;
    protected static final int BIT_ER5 = 0x0020;
    protected static final int BIT_ER6 = 0x0040;
    protected static final int BIT_ER7 = 0x0080;
    protected static final int BIT_CCR = 0x0100;
    protected static final int BIT_EXR = 0x0200;

    public H8SCpuState(long step) {
        super(0, step);
    }

    @Override
    public long getPC() {
        return pc;
    }

    public abstract int getER(int i);

    public abstract byte getCCR();

    public abstract byte getEXR();

    @Override
    public long get(String name) {
        switch (name.toLowerCase()) {
            case "pc":
                return getPC();
            case "er0":
                return Integer.toUnsignedLong(getER(0));
            case "e0":
                return Short.toUnsignedInt((short) (getER(0) >> 16));
            case "r0":
                return Short.toUnsignedInt((short) getER(0));
            case "r0l":
                return Byte.toUnsignedInt((byte) getER(0));
            case "r0h":
                return Byte.toUnsignedInt((byte) (getER(0) >> 8));
            case "er1":
                return Integer.toUnsignedLong(getER(1));
            case "e1":
                return Short.toUnsignedInt((short) (getER(1) >> 16));
            case "r1":
                return Short.toUnsignedInt((short) getER(1));
            case "r1l":
                return Byte.toUnsignedInt((byte) getER(1));
            case "r1h":
                return Byte.toUnsignedInt((byte) (getER(1) >> 8));
            case "er2":
                return Integer.toUnsignedLong(getER(2));
            case "e2":
                return Short.toUnsignedInt((short) (getER(2) >> 16));
            case "r2":
                return Short.toUnsignedInt((short) getER(2));
            case "r2l":
                return Byte.toUnsignedInt((byte) getER(2));
            case "r2h":
                return Byte.toUnsignedInt((byte) (getER(2) >> 8));
            case "er3":
                return Integer.toUnsignedLong(getER(3));
            case "e3":
                return Short.toUnsignedInt((short) (getER(3) >> 16));
            case "r3":
                return Short.toUnsignedInt((short) getER(3));
            case "r3l":
                return Byte.toUnsignedInt((byte) getER(3));
            case "r3h":
                return Byte.toUnsignedInt((byte) (getER(3) >> 8));
            case "er4":
                return Integer.toUnsignedLong(getER(4));
            case "e4":
                return Short.toUnsignedInt((short) (getER(4) >> 16));
            case "r4":
                return Short.toUnsignedInt((short) getER(4));
            case "r4l":
                return Byte.toUnsignedInt((byte) getER(4));
            case "r4h":
                return Byte.toUnsignedInt((byte) (getER(4) >> 8));
            case "er5":
                return Integer.toUnsignedLong(getER(5));
            case "e5":
                return Short.toUnsignedInt((short) (getER(5) >> 16));
            case "r5":
                return Short.toUnsignedInt((short) getER(5));
            case "r5l":
                return Byte.toUnsignedInt((byte) getER(5));
            case "r5h":
                return Byte.toUnsignedInt((byte) (getER(5) >> 8));
            case "er6":
                return Integer.toUnsignedLong(getER(6));
            case "e6":
                return Short.toUnsignedInt((short) (getER(6) >> 16));
            case "r6":
                return Short.toUnsignedInt((short) getER(6));
            case "r6l":
                return Byte.toUnsignedInt((byte) getER(6));
            case "r6h":
                return Byte.toUnsignedInt((byte) (getER(6) >> 8));
            case "er7":
                return Integer.toUnsignedLong(getER(7));
            case "e7":
                return Short.toUnsignedInt((short) (getER(7) >> 16));
            case "r7":
                return Short.toUnsignedInt((short) getER(7));
            case "r7l":
                return Byte.toUnsignedInt((byte) getER(7));
            case "r7h":
                return Byte.toUnsignedInt((byte) (getER(7) >> 8));
            case "ccr":
                return Byte.toUnsignedInt(getCCR());
            case "exr":
                return Byte.toUnsignedInt(getEXR());
            default:
                throw new IllegalArgumentException("unknown register " + name);
        }
    }

    @Override
    public H8SCpuState getState() {
        return this;
    }

    @Override
    public byte[] getMachinecode() {
        return machinecode;
    }

    private static void psw(StringBuilder buf, byte word, char c, int bit) {
        if ((word & bit) != 0) {
            buf.append(c);
        } else {
            buf.append('-');
        }
    }

    private static void psw(StringBuilder buf, byte word, String s, int bit) {
        if ((word & bit) != 0) {
            buf.append(s);
        } else {
            buf.append("--");
        }
    }

    private void er(StringBuilder buf, int i) {
        int er = getER(i);
        buf.append("{{ER");
        buf.append((char) ('0' + i));
        buf.append("}}S={{");
        buf.append(HexFormatter.tohex(er & 0xFFFFFFFFL, 8));
        buf.append("}}x");
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        er(buf, 0);
        buf.append(' ');
        er(buf, 1);
        buf.append('\n');
        er(buf, 2);
        buf.append(' ');
        er(buf, 3);
        buf.append('\n');
        er(buf, 4);
        buf.append(' ');
        er(buf, 5);
        buf.append('\n');
        er(buf, 6);
        buf.append(' ');
        er(buf, 7);
        buf.append("\n\n");

        byte ccr = getCCR();
        byte exr = getEXR();
        buf.append("{{PC}}S={{");
        buf.append(HexFormatter.tohex(getPC(), 6));
        buf.append("}}x {{EXR}}S={{");
        buf.append(HexFormatter.tohex(Byte.toUnsignedInt(exr), 2));
        buf.append("}}x [");
        psw(buf, exr, 'T', 1 << 7);
        buf.append(' ');
        psw(buf, exr, "I2", 1 << 2);
        buf.append(' ');
        psw(buf, exr, "I1", 1 << 1);
        buf.append(' ');
        psw(buf, exr, "I0", 1 << 0);
        buf.append("]\n");

        buf.append("{{CCR}}S={{");
        buf.append(HexFormatter.tohex(Byte.toUnsignedInt(ccr), 2));
        buf.append("}}x [");
        psw(buf, ccr, 'I', 1 << 7);
        buf.append(' ');
        psw(buf, ccr, "UI", 1 << 6);
        buf.append(' ');
        psw(buf, ccr, 'H', 1 << 5);
        buf.append(' ');
        psw(buf, ccr, 'U', 1 << 4);
        buf.append(' ');
        psw(buf, ccr, 'N', 1 << 3);
        buf.append(' ');
        psw(buf, ccr, 'Z', 1 << 2);
        buf.append(' ');
        psw(buf, ccr, 'V', 1 << 1);
        buf.append(' ');
        psw(buf, ccr, 'C', 1 << 0);
        buf.append(']');

        return buf.toString();
    }
}
