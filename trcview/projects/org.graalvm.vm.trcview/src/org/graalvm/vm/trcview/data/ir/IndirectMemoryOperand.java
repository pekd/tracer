package org.graalvm.vm.trcview.data.ir;

import org.graalvm.vm.util.HexFormatter;

// this is really a pointer to a pointer
public class IndirectMemoryOperand extends Operand {
    private long address;

    public IndirectMemoryOperand(long address) {
        this.address = address;
    }

    public long getAddress() {
        return address;
    }

    @Override
    public int hashCode() {
        return (int) (address ^ (address >>> 32));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof IndirectMemoryOperand)) {
            return false;
        }
        IndirectMemoryOperand m = (IndirectMemoryOperand) o;
        return m.address == address;
    }

    @Override
    public String toString() {
        return "@(0x" + HexFormatter.tohex(address) + ")";
    }
}
