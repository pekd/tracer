package org.graalvm.vm.trcview.net.protocol.cmd;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class GetI8 extends Command {
    private long address;
    private long insn;

    public GetI8() {
        super(Command.GET_I8);
    }

    public GetI8(long address, long insn) {
        super(Command.GET_I8);
        this.address = address;
        this.insn = insn;
    }

    public long getAddress() {
        return address;
    }

    public long getInstructionCount() {
        return insn;
    }

    @Override
    public void read(WordInputStream in) throws IOException {
        address = in.read64bit();
        insn = in.read64bit();
    }

    @Override
    public void write(WordOutputStream out) throws IOException {
        out.write64bit(address);
        out.write64bit(insn);
    }
}
