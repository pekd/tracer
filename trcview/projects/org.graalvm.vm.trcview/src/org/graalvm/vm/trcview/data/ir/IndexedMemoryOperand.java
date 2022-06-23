package org.graalvm.vm.trcview.data.ir;

public class IndexedMemoryOperand extends Operand {
    private final int register;
    private final long offset;

    public IndexedMemoryOperand(int register) {
        this.register = register;
        this.offset = 0;
    }

    public IndexedMemoryOperand(int register, long offset) {
        this.register = register;
        this.offset = offset;
    }

    public int getRegister() {
        return register;
    }

    public long getOffset() {
        return offset;
    }
}
