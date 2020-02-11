package org.graalvm.vm.trcview.net.protocol.cmdresult;

import java.io.IOException;

import org.graalvm.vm.trcview.analysis.memory.MemoryRead;
import org.graalvm.vm.trcview.io.Node;
import org.graalvm.vm.trcview.net.protocol.IO;
import org.graalvm.vm.trcview.net.protocol.cmd.Command;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class GetLastReadResult extends Result {
    private MemoryRead read;

    public GetLastReadResult() {
        super(Command.GET_LAST_READ);
    }

    public GetLastReadResult(MemoryRead read) {
        super(Command.GET_LAST_READ);
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
        int size = in.read();
        Node node = IO.readNode(in);
        Node step = IO.readNode(in);
        read = new MemoryRead(addr, (byte) size, insn, node, step);
    }

    @Override
    public void write(WordOutputStream out) throws IOException {
        if (read == null) {
            out.write(0);
            return;
        } else {
            out.write(1);
        }
        out.write64bit(read.instructionCount);
        out.write64bit(read.address);
        out.write(read.size);
        IO.writeNode(out, read.node);
        IO.writeNode(out, read.step);
    }
}
