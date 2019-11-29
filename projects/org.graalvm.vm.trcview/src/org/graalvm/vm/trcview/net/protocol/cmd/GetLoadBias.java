package org.graalvm.vm.trcview.net.protocol.cmd;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class GetLoadBias extends Command {
    private long pc;

    public GetLoadBias() {
        super(GET_LOAD_BIAS);
    }

    public GetLoadBias(long pc) {
        super(GET_LOAD_BIAS);
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
