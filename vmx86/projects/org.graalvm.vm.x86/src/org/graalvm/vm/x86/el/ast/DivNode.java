package org.graalvm.vm.x86.el.ast;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.VirtualFrame;

public class DivNode extends Expression {
    @Child private Expression left;
    @Child private Expression right;

    public DivNode(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public long execute(VirtualFrame frame, long pc) {
        long l = left.execute(frame, pc);
        long r = right.execute(frame, pc);
        if (r == 0) {
            return 0xFFFFFFFF_FFFFFFFFL;
        } else {
            return l / r;
        }
    }

    @Override
    public String toString() {
        CompilerAsserts.neverPartOfCompilation();
        return "(" + left + " / " + right + ")";
    }

    @Override
    public DivNode clone() {
        CompilerAsserts.neverPartOfCompilation();
        return new DivNode(left.clone(), right.clone());
    }
}
