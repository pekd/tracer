package org.graalvm.vm.trcview.script.ast;

import java.util.List;

import org.graalvm.vm.trcview.script.rt.Context;

public class Block extends Statement {
    private final List<Statement> body;

    public Block(List<Statement> body) {
        this.body = body;
    }

    @Override
    public long execute(Context ctx) {
        long result = 0;
        for (Statement stmt : body) {
            result = stmt.execute(ctx);
        }
        return result;
    }
}
