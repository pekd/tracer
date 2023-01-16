package org.graalvm.vm.trcview.data.ir;

import org.graalvm.vm.trcview.data.type.VariableType;

public class ConstOperand extends Operand {
    private long type = 0;

    public void constrain(VariableType bits) {
        constrain(bits.getMask());
    }

    public void constrain(long bits) {
        this.type |= bits;
    }

    public void set(VariableType type) {
        set(type.getMask());
    }

    public void set(long type) {
        this.type = type;
    }

    public long get() {
        return type;
    }

    @Override
    public int hashCode() {
        return (int) type ^ (int) (type >> 32);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (!(o instanceof ConstOperand)) {
            return false;
        }

        ConstOperand c = (ConstOperand) o;
        return c.type == type;
    }

    @Override
    public String toString() {
        return "CONST";
    }
}
