package org.graalvm.vm.x86.el.ast;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.VirtualFrame;

public class ValueNode extends Expression {
    private final long value;

    public ValueNode(long value) {
        this.value = value;
    }

    @Override
    public long execute(VirtualFrame frame, long pc) {
        return value;
    }

    @Override
    public String toString() {
        CompilerAsserts.neverPartOfCompilation();
        return Long.toString(value);
    }

    @Override
    public ValueNode clone() {
        CompilerAsserts.neverPartOfCompilation();
        return new ValueNode(value);
    }
}
