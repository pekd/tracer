package org.graalvm.vm.x86.el.ast.func;

import org.graalvm.vm.memory.exception.SegmentationViolation;
import org.graalvm.vm.x86.ArchitecturalState;
import org.graalvm.vm.x86.el.ast.Expression;
import org.graalvm.vm.x86.node.MemoryReadNode;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.VirtualFrame;

public class GetI8Node extends Expression {
    @Child private Expression address;
    @Child private MemoryReadNode mem;

    public GetI8Node(Expression address, ArchitecturalState state) {
        this.address = address;
        mem = state.createMemoryRead();
    }

    @Override
    public long execute(VirtualFrame frame, long pc) {
        long addr = address.execute(frame, pc);
        try {
            return mem.executeI8(addr);
        } catch (SegmentationViolation e) {
            return 0;
        }
    }

    @Override
    public String toString() {
        CompilerAsserts.neverPartOfCompilation();
        return "getI8(" + address + ")";
    }

    @Override
    public GetI8Node clone() {
        CompilerAsserts.neverPartOfCompilation();
        ArchitecturalState state = getContextReference().get(this).getState();
        return new GetI8Node(address.clone(), state);
    }
}
