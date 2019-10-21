package org.graalvm.vm.x86.trcview.net.protocol.cmd;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class GetNextPC extends Command {
    private long insn;
    private long pc;

    public GetNextPC() {
        super(GET_NEXT_PC);
    }

    public GetNextPC(long insn, long pc) {
        super(GET_NEXT_PC);
        this.insn = insn;
        this.pc = pc;
    }

    public long getInstructionCount() {
        return insn;
    }

    public long getPC() {
        return pc;
    }

    @Override
    public void read(WordInputStream in) throws IOException {
        insn = in.read64bit();
        pc = in.read64bit();
    }

    @Override
    public void write(WordOutputStream out) throws IOException {
        out.write64bit(insn);
        out.write64bit(pc);
    }
}
