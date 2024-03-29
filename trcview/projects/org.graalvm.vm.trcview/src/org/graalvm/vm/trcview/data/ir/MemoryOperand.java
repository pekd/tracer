package org.graalvm.vm.trcview.data.ir;

import org.graalvm.vm.util.HexFormatter;

public class MemoryOperand extends Operand {
    private long address;

    public MemoryOperand(long address) {
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
        if (!(o instanceof MemoryOperand)) {
            return false;
        }
        MemoryOperand m = (MemoryOperand) o;
        return m.address == address;
    }

    @Override
    public String toString() {
        return "(0x" + HexFormatter.tohex(address) + ")";
    }
}
