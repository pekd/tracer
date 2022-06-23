package org.graalvm.vm.x86.el.ast;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.VirtualFrame;

public class NegNode extends Expression {
    @Child private Expression child;

    public NegNode(Expression child) {
        this.child = child;
    }

    @Override
    public long execute(VirtualFrame frame, long pc) {
        return -child.execute(frame, pc);
    }

    @Override
    public String toString() {
        CompilerAsserts.neverPartOfCompilation();
        return "-" + child;
    }

    @Override
    public NegNode clone() {
        CompilerAsserts.neverPartOfCompilation();
        return new NegNode(child.clone());
    }
}
