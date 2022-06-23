package org.graalvm.vm.x86.el.ast;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.VirtualFrame;

public class NotNode extends Expression {
    @Child private Expression child;

    public NotNode(Expression child) {
        this.child = child;
    }

    @Override
    public long execute(VirtualFrame frame, long pc) {
        long value = child.execute(frame, pc);
        if (value == 0) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        CompilerAsserts.neverPartOfCompilation();
        return "!" + child;
    }

    @Override
    public NotNode clone() {
        CompilerAsserts.neverPartOfCompilation();
        return new NotNode(child.clone());
    }
}
