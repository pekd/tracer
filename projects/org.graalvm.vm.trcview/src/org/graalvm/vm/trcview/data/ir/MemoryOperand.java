package org.graalvm.vm.trcview.data.ir;

public class MemoryOperand extends Operand {
    private long address;

    public MemoryOperand(long address) {
        this.address = address;
    }

    public long getAddress() {
        return address;
    }
}
