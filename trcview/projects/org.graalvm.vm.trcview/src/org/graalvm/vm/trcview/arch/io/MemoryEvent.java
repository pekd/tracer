package org.graalvm.vm.trcview.arch.io;

import org.graalvm.vm.util.BitTest;
import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.util.OctFormatter;
import org.graalvm.vm.util.Stringify;
import org.graalvm.vm.util.Vector128;

public abstract class MemoryEvent extends Event {
    private static final byte FLAG_WRITE = 1;
    private static final byte FLAG_DATA = 2;
    private static final byte FLAG_BE = 4;

    private final byte flags;
    private final long address;

    private MemoryEvent next = null;

    private static byte be(boolean value) {
        return value ? FLAG_BE : 0;
    }

    private static byte data(boolean value) {
        return value ? FLAG_DATA : 0;
    }

    private static byte write(boolean value) {
        return value ? FLAG_WRITE : 0;
    }

    protected MemoryEvent(boolean be, int tid, long address, boolean write, boolean data) {
        super(tid);
        this.flags = (byte) (be(be) | write(write) | data(data));
        this.address = address;
    }

    public boolean isBigEndian() {
        return BitTest.test(flags, FLAG_BE);
    }

    public boolean hasData() {
        return BitTest.test(flags, FLAG_DATA);
    }

    public boolean isWrite() {
        return BitTest.test(flags, FLAG_WRITE);
    }

    public long getAddress() {
        return address;
    }

    public abstract int getSize();

    public abstract long getValue();

    public abstract Vector128 getVector();

    public void setNext(MemoryEvent next) {
        assert next != this;
        this.next = next;
    }

    public MemoryEvent getNext() {
        return next;
    }

    @Override
    public String toString() {
        String str = null;
        StringBuilder val = new StringBuilder("0x");
        int size = getSize();
        if (hasData()) {
            switch (size) {
                case 1:
                    str = Stringify.i8((byte) getValue());
                    val.append(HexFormatter.tohex(getValue(), 2));
                    break;
                case 2:
                    str = Stringify.i16((short) getValue());
                    val.append(HexFormatter.tohex(getValue(), 4));
                    break;
                case 4:
                    str = Stringify.i32((int) getValue());
                    val.append(HexFormatter.tohex(getValue(), 8));
                    break;
                case 8:
                    str = Stringify.i64(getValue());
                    val.append(HexFormatter.tohex(getValue(), 16));
                    break;
                case 16:
                    str = Stringify.i128(getVector());
                    val.append(HexFormatter.tohex(getVector().getI64(0), 16));
                    val.append(HexFormatter.tohex(getVector().getI64(1), 16));
                    break;
            }
        }
        if (str != null) {
            val.append(", '").append(str).append("'");
        }
        return "Memory access to 0x" + HexFormatter.tohex(address, 16) + ": " + (!isWrite() ? "read" : "write") + " " + size + (size > 1 ? " bytes" : " byte") + (hasData() ? " (" + val + ")" : "");
    }

    public String info(StepFormat fmt) {
        StringBuilder val = new StringBuilder();
        String addr = fmt.formatAddress(address);
        int size = getSize();
        if (fmt.numberfmt == StepFormat.NUMBERFMT_OCT) {
            String str = null;
            val.append("0");
            if (hasData()) {
                switch (size) {
                    case 1:
                        str = Stringify.i8((byte) getValue());
                        val.append(OctFormatter.tooct(getValue(), 2));
                        break;
                    case 2:
                        str = Stringify.i16((short) getValue());
                        val.append(OctFormatter.tooct(getValue(), 4));
                        break;
                    case 4:
                        str = Stringify.i32((int) getValue());
                        val.append(OctFormatter.tooct(getValue(), 8));
                        break;
                    case 8:
                        str = Stringify.i64(getValue());
                        val.append(OctFormatter.tooct(getValue(), 16));
                        break;
                    case 16:
                        str = Stringify.i128(getVector());
                        val.append(OctFormatter.tooct(getVector().getI64(0), 16));
                        val.append(" ");
                        val.append(OctFormatter.tooct(getVector().getI64(1), 16));
                        break;
                }
            }
            if (str != null) {
                val.append(", '").append(str).append("'");
            }
        } else {
            String str = null;
            val.append("0x");
            if (hasData()) {
                switch (size) {
                    case 1:
                        str = Stringify.i8((byte) getValue());
                        val.append(HexFormatter.tohex(getValue(), 2));
                        break;
                    case 2:
                        str = Stringify.i16((short) getValue());
                        val.append(HexFormatter.tohex(getValue(), 4));
                        break;
                    case 4:
                        str = Stringify.i32((int) getValue());
                        val.append(HexFormatter.tohex(getValue(), 8));
                        break;
                    case 8:
                        str = Stringify.i64(getValue());
                        val.append(HexFormatter.tohex(getValue(), 16));
                        break;
                    case 16:
                        str = Stringify.i128(getVector());
                        val.append(HexFormatter.tohex(getVector().getI64(0), 16));
                        val.append(HexFormatter.tohex(getVector().getI64(1), 16));
                        break;
                }
            }
            if (str != null) {
                val.append(", '").append(str).append("'");
            }
        }
        return addr + ": " + (!isWrite() ? "READ " : "WRITE") + " " + size + (size > 1 ? " bytes" : " byte ") + (hasData() ? " (" + val + ")" : "");
    }
}
