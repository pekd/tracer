package org.graalvm.vm.x86.trcview.net.protocol.cmd;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class GetFileOffset extends Command {
    private long pc;

    public GetFileOffset() {
        super(GET_FILE_OFFSET);
    }

    public GetFileOffset(long pc) {
        super(GET_FILE_OFFSET);
        this.pc = pc;
    }

    public long getPC() {
        return pc;
    }

    @Override
    public void read(WordInputStream in) throws IOException {
        pc = in.read64bit();
    }

    @Override
    public void write(WordOutputStream out) throws IOException {
        out.write64bit(pc);
    }
}
