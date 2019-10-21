package org.graalvm.vm.x86.trcview.net.protocol.cmd;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class GetSymbols extends Command {
    public GetSymbols() {
        super(GET_SYMBOLS);
    }

    @Override
    public void read(WordInputStream in) throws IOException {
        // nothing
    }

    @Override
    public void write(WordOutputStream out) throws IOException {
        // nothing
    }
}
