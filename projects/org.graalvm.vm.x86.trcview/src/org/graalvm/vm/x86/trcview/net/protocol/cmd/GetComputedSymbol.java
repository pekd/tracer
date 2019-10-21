package org.graalvm.vm.x86.trcview.net.protocol.cmd;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class GetComputedSymbol extends Command {
    private long pc;

    public GetComputedSymbol() {
        super(GET_COMPUTED_SYMBOL);
    }

    public GetComputedSymbol(long pc) {
        super(GET_COMPUTED_SYMBOL);
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
