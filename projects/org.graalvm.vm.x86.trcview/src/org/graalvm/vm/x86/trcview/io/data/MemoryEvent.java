package org.graalvm.vm.x86.trcview.io.data;

import java.io.IOException;

import org.graalvm.vm.memory.util.Stringify;
import org.graalvm.vm.memory.vector.Vector128;
import org.graalvm.vm.posix.elf.Elf;
import org.graalvm.vm.util.BitTest;
import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class MemoryEvent extends Event {
    private final boolean be;
    private final boolean write;
    private final boolean data;
    private final long address;
    private final byte size;
    private final long value64;
    private final Vector128 value128;

    public MemoryEvent(boolean be, int tid, long address, byte size, boolean write) {
        super(Elf.EM_NONE, MEMORY, tid);
        this.be = be;
        this.write = write;
        this.data = false;
        this.address = address;
        this.size = size;
        this.value64 = 0;
        this.value128 = null;
    }

    public MemoryEvent(boolean be, int tid, long address, byte size, boolean write, long value) {
        super(Elf.EM_NONE, MEMORY, tid);
        this.be = be;
        this.write = write;
        this.data = true;
        this.address = address;
        this.size = size;
        this.value64 = value;
        this.value128 = null;
    }

    public MemoryEvent(boolean be, int tid, long address, byte size, boolean write, Vector128 value) {
        super(Elf.EM_NONE, MEMORY, tid);
        this.be = be;
        this.write = write;
        this.data = true;
        this.address = address;
        this.size = size;
        this.value64 = 0;
        this.value128 = value;
    }

    private MemoryEvent(boolean be, int tid, long address, byte size, boolean write, boolean data, long value64, Vector128 value) {
        super(Elf.EM_NONE, MEMORY, tid);
        this.be = be;
        this.write = write;
        this.data = data;
        this.address = address;
        this.size = size;
        this.value64 = value64;
        this.value128 = value;
    }

    public boolean isBigEndian() {
        return be;
    }

    public boolean hasData() {
        return data;
    }

    public boolean isWrite() {
        return write;
    }

    public long getAddress() {
        return address;
    }

    public int getSize() {
        return size;
    }

    public long getValue() {
        return value64;
    }

    public Vector128 getVector() {
        return value128;
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        byte bits = 0;
        if (write) {
            bits |= 1;
        }
        if (data) {
            bits |= 2;
        }
        if (be) {
            bits |= 4;
        }
        out.write64bit(address);
        if (value128 == null) {
            out.write64bit(value64);
            out.write64bit(0);
        } else {
            out.write64bit(value128.getI64(0));
            out.write64bit(value128.getI64(1));
        }
        out.write8bit(bits);
        out.write8bit(size);
    }

    public static MemoryEvent readRecord(WordInputStream in, int tid) throws IOException {
        long address = in.read64bit();
        long v0 = in.read64bit();
        long v1 = in.read64bit();
        int bits = in.read8bit();
        int size = in.read8bit();
        boolean write = BitTest.test(bits, 1);
        boolean data = BitTest.test(bits, 2);
        boolean be = BitTest.test(bits, 4);
        if (size > 8) {
            Vector128 value = size > 8 ? new Vector128(v0, v1) : null;
            return new MemoryEvent(be, tid, address, (byte) size, write, data, 0, value);
        } else {
            return new MemoryEvent(be, tid, address, (byte) size, write, data, v0, null);
        }
    }

    @Override
    public String toString() {
        String str = null;
        StringBuilder val = new StringBuilder("0x");
        if (data) {
            switch (size) {
                case 1:
                    str = Stringify.i8((byte) value64);
                    val.append(HexFormatter.tohex(value64, 2));
                    break;
                case 2:
                    str = Stringify.i16((short) value64);
                    val.append(HexFormatter.tohex(value64, 4));
                    break;
                case 4:
                    str = Stringify.i32((int) value64);
                    val.append(HexFormatter.tohex(value64, 8));
                    break;
                case 8:
                    str = Stringify.i64(value64);
                    val.append(HexFormatter.tohex(value64, 16));
                    break;
                case 16:
                    str = Stringify.i128(value128);
                    val.append(HexFormatter.tohex(value128.getI64(0), 16));
                    val.append(HexFormatter.tohex(value128.getI64(1), 16));
                    break;
            }
        }
        if (str != null) {
            val.append(", '").append(str).append("'");
        }
        return "Memory access to 0x" + HexFormatter.tohex(address, 16) + ": " + (!write ? "read" : "write") + " " + size + (size > 1 ? " bytes" : " byte") + (data ? " (" + val + ")" : "");
    }
}
