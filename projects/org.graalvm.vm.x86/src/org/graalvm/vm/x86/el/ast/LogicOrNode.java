package org.graalvm.vm.x86.el.ast;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.VirtualFrame;

public class LogicOrNode extends Expression {
    @Child private Expression left;
    @Child private Expression right;

    public LogicOrNode(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public long execute(VirtualFrame frame, long pc) {
        return left.execute(frame, pc) != 0 || right.execute(frame, pc) != 0 ? 1 : 0;
    }

    @Override
    public String toString() {
        CompilerAsserts.neverPartOfCompilation();
        return "(" + left + " || " + right + ")";
    }

    @Override
    public LogicOrNode clone() {
        CompilerAsserts.neverPartOfCompilation();
        return new LogicOrNode(left.clone(), right.clone());
    }
}
