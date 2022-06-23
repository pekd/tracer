package org.graalvm.vm.x86.el.ast;

import org.graalvm.vm.x86.isa.Register;
import org.graalvm.vm.x86.node.AMD64Node;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class Expression extends AMD64Node {
    private Register[] registers = new Register[0];

    public abstract long execute(VirtualFrame frame, long pc);

    protected void setGPRRead(Register reg) {
        setGPRReads(reg);
    }

    protected void setGPRReads(Register... registers) {
        this.registers = registers;
    }

    public Register[] getUsedGPRRead() {
        CompilerAsserts.neverPartOfCompilation();
        return registers;
    }

    @Override
    public abstract Expression clone();
}
