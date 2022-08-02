package org.graalvm.vm.trcview.data.ir;

import org.graalvm.vm.util.HexFormatter;

// this is really a pointer to a pointer
public class IndirectIndexedMemoryOperand extends Operand {
    private final int register;
    private final long offset;

    public IndirectIndexedMemoryOperand(int register) {
        this.register = register;
        this.offset = 0;
    }

    public IndirectIndexedMemoryOperand(int register, long offset) {
        this.register = register;
        this.offset = offset;
    }

    public int getRegister() {
        return register;
    }

    public long getOffset() {
        return offset;
    }

    @Override
    public int hashCode() {
        return (int) (offset ^ (offset >> 32)) ^ register;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof IndirectIndexedMemoryOperand)) {
            return false;
        }
        IndirectIndexedMemoryOperand m = (IndirectIndexedMemoryOperand) o;
        return m.offset == offset && m.register == register;
    }

    @Override
    public String toString() {
        if (offset == 0) {
            return "@(R" + register + ")";
        } else if (offset < 0) {
            return "@-0x" + HexFormatter.tohex(-offset) + "(R" + register + ")";
        } else {
            return "@0x" + HexFormatter.tohex(offset) + "(R" + register + ")";
        }
    }
}
