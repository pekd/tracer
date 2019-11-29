package org.graalvm.vm.trcview.net.protocol.cmd;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class GetNextStep extends Command {
    private long insn;

    public GetNextStep() {
        super(GET_NEXT_STEP);
    }

    public GetNextStep(long insn) {
        super(GET_NEXT_STEP);
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
