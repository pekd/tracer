package org.graalvm.vm.trcview.data.ir;

import org.graalvm.vm.util.HexFormatter;

public class IndexedMemoryOperand extends Operand {
    private final int register;
    private final int offsetreg;
    private final long offset;

    public IndexedMemoryOperand(int register) {
        this.register = register;
        this.offsetreg = -1;
        this.offset = 0;
    }

    public IndexedMemoryOperand(int register, long offset) {
        this.register = register;
        this.offsetreg = -1;
        this.offset = offset;
    }

    public IndexedMemoryOperand(int register, int offsetreg) {
        this.register = register;
        this.offsetreg = offsetreg;
        this.offset = 0;
    }

    public IndexedMemoryOperand(int register, int offsetreg, long offset) {
        this.register = register;
        this.offsetreg = offsetreg;
        this.offset = offset;
    }

    public int getRegister() {
        return register;
    }

    public int getOffsetRegister() {
        return offsetreg;
    }

    public long getOffset() {
        return offset;
    }

    @Override
    public int hashCode() {
        return (int) (offset ^ (offset >> 32)) ^ register ^ offsetreg;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof IndexedMemoryOperand)) {
            return false;
        }
        IndexedMemoryOperand m = (IndexedMemoryOperand) o;
        return m.offset == offset && m.register == register && m.offsetreg == offsetreg;
    }

    @Override
    public String toString() {
        String reg;
        if (offsetreg == -1) {
            reg = "(R" + register + ")";
        } else {
            reg = "(R" + register + ",R" + offsetreg + ")";
        }
        if (offset == 0) {
            return reg;
        } else if (offset < 0) {
            return "-0x" + HexFormatter.tohex(-offset) + reg;
        } else {
            return "0x" + HexFormatter.tohex(offset) + reg;
        }
    }
}
