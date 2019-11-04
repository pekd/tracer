package org.graalvm.vm.x86.el.ast.func;

import org.graalvm.vm.x86.el.ast.Expression;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.VirtualFrame;

public class IfNode extends Expression {
    @Child private Expression condition;
    @Child private Expression then;
    @Child private Expression otherwise;

    public IfNode(Expression condition, Expression then, Expression otherwise) {
        this.condition = condition;
        this.then = then;
        this.otherwise = otherwise;
    }

    @Override
    public long execute(VirtualFrame frame, long pc) {
        long value = condition.execute(frame, pc);
        if (value != 0) {
            return then.execute(frame, pc);
        } else {
            return otherwise.execute(frame, pc);
        }
    }

    @Override
    public String toString() {
        CompilerAsserts.neverPartOfCompilation();
        return "if(" + condition + ", " + then + ", " + otherwise + ")";
    }

    @Override
    public IfNode clone() {
        CompilerAsserts.neverPartOfCompilation();
        return new IfNode(condition.clone(), then.clone(), otherwise.clone());
    }
}
