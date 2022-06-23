package org.graalvm.vm.x86.el.ast;

import org.graalvm.vm.x86.isa.Register;
import org.graalvm.vm.x86.node.AMD64Node;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.VirtualFrame;

public class BooleanExpression extends AMD64Node {
    @Child private Expression expr;

    public BooleanExpression(Expression expr) {
        this.expr = expr;
    }

    public boolean execute(VirtualFrame frame, long pc) {
        return expr.execute(frame, pc) != 0;
    }

    public Register[] getUsedGPRRead() {
        CompilerAsserts.neverPartOfCompilation();
        return expr.getUsedGPRRead();
    }

    @Override
    public BooleanExpression clone() {
        CompilerAsserts.neverPartOfCompilation();
        return new BooleanExpression(expr.clone());
    }
}
