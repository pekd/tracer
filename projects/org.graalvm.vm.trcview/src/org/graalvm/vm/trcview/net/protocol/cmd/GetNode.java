package org.graalvm.vm.trcview.net.protocol.cmd;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class GetNode extends Command {
    private long insn;

    public GetNode() {
        super(GET_NODE);
    }

    public GetNode(long insn) {
        super(GET_NODE);
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
