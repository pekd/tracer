package org.graalvm.vm.x86.trcview.net.protocol.cmd;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class GetInstruction extends Command {
    private long insn;

    public GetInstruction() {
        super(GET_INSTRUCTION);
    }

    public GetInstruction(long insn) {
        super(GET_INSTRUCTION);
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
