package org.graalvm.vm.trcview.data.ir;

public class RegisterOperand extends Operand {
    private final int register;

    public RegisterOperand(int register) {
        this.register = register;
    }

    public int getRegister() {
        return register;
    }

    @Override
    public int hashCode() {
        return register;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof RegisterOperand)) {
            return false;
        }
        RegisterOperand r = (RegisterOperand) o;
        return r.register == register;
    }

    @Override
    public String toString() {
        return "R" + register;
    }
}
