package org.graalvm.vm.trcview.data.ir;

public class RegisterOperand extends Operand {
    private final int register;

    public RegisterOperand(int register) {
        this.register = register;
    }

    public int getRegister() {
        return register;
    }
}
