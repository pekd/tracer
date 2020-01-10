package org.graalvm.vm.trcview.script.ast;

import org.graalvm.vm.trcview.script.rt.Context;
import org.graalvm.vm.trcview.script.rt.Pointer;
import org.graalvm.vm.trcview.script.type.Struct.Member;

public class GetStructMember extends PointerOperation {
    private final PointerOperation src;
    private final int offset;

    public GetStructMember(PointerOperation src, Member member) {
        super(member.type);
        assert src != null && member != null;
        this.src = src;
        offset = member.offset;
    }

    @Override
    public Pointer execute(Context ctx) {
        Pointer ptr = src.execute(ctx);
        return ptr.add(getType(), offset);
    }
}
