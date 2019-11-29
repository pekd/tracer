package org.graalvm.vm.trcview.net.protocol.cmdresult;

import java.io.IOException;

import org.graalvm.vm.trcview.analysis.memory.MemoryRead;
import org.graalvm.vm.trcview.io.Node;
import org.graalvm.vm.trcview.net.protocol.IO;
import org.graalvm.vm.trcview.net.protocol.cmd.Command;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class GetNextReadResult extends Result {
    private MemoryRead read;

    public GetNextReadResult() {
        super(Command.GET_NEXT_READ);
    }

    public GetNextReadResult(MemoryRead read) {
        super(Command.GET_NEXT_READ);
        this.read = read;
    }

    public MemoryRead getRead() {
        return read;
    }

    @Override
    public void read(WordInputStream in) throws IOException {
        if (in.read() == 0) {
            read = null;
            return;
        }
        long insn = in.read64bit();
        long addr = in.read64bit();
        long pc = in.read64bit();
        int size = in.read();
        Node node = IO.readNode(in);
        read = new MemoryRead(addr, (byte) size, pc, insn, node);
    }

    @Override
    public void write(WordOutputStream out) throws IOException {
        if (read == null) {
            out.write(0);
            return;
        }
        out.write(1);
        out.write64bit(read.instructionCount);
        out.write64bit(read.address);
        out.write64bit(read.pc);
        out.write(read.size);
        IO.writeNode(out, read.node);
    }
}
