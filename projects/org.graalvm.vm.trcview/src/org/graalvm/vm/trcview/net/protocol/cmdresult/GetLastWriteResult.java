package org.graalvm.vm.trcview.net.protocol.cmdresult;

import java.io.IOException;

import org.graalvm.vm.trcview.analysis.memory.MemoryUpdate;
import org.graalvm.vm.trcview.io.Node;
import org.graalvm.vm.trcview.net.protocol.IO;
import org.graalvm.vm.trcview.net.protocol.cmd.Command;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class GetLastWriteResult extends Result {
    private MemoryUpdate write;

    public GetLastWriteResult() {
        super(Command.GET_LAST_WRITE);
    }

    public GetLastWriteResult(MemoryUpdate write) {
        super(Command.GET_LAST_WRITE);
        this.write = write;
    }

    public MemoryUpdate getWrite() {
        return write;
    }

    @Override
    public void read(WordInputStream in) throws IOException {
        if (in.read() == 0) {
            return;
        }
        long insn = in.read64bit();
        long addr = in.read64bit();
        long pc = in.read64bit();
        long value = in.read64bit();
        int size = in.read();
        Node node = IO.readNode(in);
        write = new MemoryUpdate(addr, (byte) size, value, pc, insn, node);
    }

    @Override
    public void write(WordOutputStream out) throws IOException {
        if (write == null) {
            out.write(0);
            return;
        } else {
            out.write(1);
        }
        out.write64bit(write.instructionCount);
        out.write64bit(write.address);
        out.write64bit(write.pc);
        out.write64bit(write.value);
        out.write(write.size);
        IO.writeNode(out, write.node);
    }
}
