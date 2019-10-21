package org.graalvm.vm.x86.trcview.net.protocol.cmd;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class GetI64 extends Command {
    private long address;
    private long insn;

    public GetI64() {
        super(Command.GET_I64);
    }

    public GetI64(long address, long insn) {
        super(Command.GET_I64);
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
