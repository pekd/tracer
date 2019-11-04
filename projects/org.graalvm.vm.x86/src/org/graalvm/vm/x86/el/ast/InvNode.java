package org.graalvm.vm.x86.el.ast;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.VirtualFrame;

public class InvNode extends Expression {
    @Child private Expression child;

    public InvNode(Expression child) {
        this.child = child;
    }

    @Override
    public long execute(VirtualFrame frame, long pc) {
        return ~child.execute(frame, pc);
    }

    @Override
    public String toString() {
        CompilerAsserts.neverPartOfCompilation();
        return "~" + child;
    }

    @Override
    public InvNode clone() {
        CompilerAsserts.neverPartOfCompilation();
        return new InvNode(child.clone());
    }
}
