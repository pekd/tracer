package org.graalvm.vm.trcview.net.protocol.cmd;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class GetBase extends Command {
    private long pc;

    public GetBase() {
        super(GET_BASE);
    }

    public GetBase(long pc) {
        super(GET_BASE);
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
