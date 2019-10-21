package org.graalvm.vm.x86.trcview.net.protocol.cmd;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class GetChildren extends Command {
    private long insn;

    public GetChildren() {
        super(GET_CHILDREN);
    }

    public GetChildren(long insn) {
        super(GET_CHILDREN);
        this.insn = insn;
    }

    public long getInstructionCount() {
        return insn;
    }

    @Override
    public void read(WordInputStream in) throws IOException {
        insn = in.read64bit();
    }

    @Override
    public void write(WordOutputStream out) throws IOException {
        out.write64bit(insn);
    }
}
