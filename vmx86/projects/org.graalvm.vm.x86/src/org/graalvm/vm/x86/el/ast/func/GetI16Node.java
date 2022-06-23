package org.graalvm.vm.x86.el.ast.func;

import org.graalvm.vm.memory.exception.SegmentationViolation;
import org.graalvm.vm.x86.ArchitecturalState;
import org.graalvm.vm.x86.el.ast.Expression;
import org.graalvm.vm.x86.node.MemoryReadNode;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.VirtualFrame;

public class GetI16Node extends Expression {
    @Child private Expression address;
    @Child private MemoryReadNode mem;

    public GetI16Node(Expression address, ArchitecturalState state) {
        this.address = address;
        mem = state.createMemoryRead();
    }

    @Override
    public long execute(VirtualFrame frame, long pc) {
        long addr = address.execute(frame, pc);
        try {
            return mem.executeI16(addr);
        } catch (SegmentationViolation e) {
            return 0;
        }
    }

    @Override
    public String toString() {
        CompilerAsserts.neverPartOfCompilation();
        return "getI16(" + address + ")";
    }

    @Override
    public GetI16Node clone() {
        CompilerAsserts.neverPartOfCompilation();
        ArchitecturalState state = getContextReference().get().getState();
        return new GetI16Node(address.clone(), state);
    }
}
