package org.graalvm.vm.trcview.net.protocol.cmd;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class GetI16 extends Command {
    private long address;
    private long insn;

    public GetI16() {
        super(Command.GET_I16);
    }

    public GetI16(long address, long insn) {
        super(Command.GET_I16);
        this.address = address;
        this.insn = insn;
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
