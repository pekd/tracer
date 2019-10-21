package org.graalvm.vm.x86.trcview.net.protocol.cmdresult;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.Command;

public class GetLoadBiasResult extends Result {
    private long loadBias;

    public GetLoadBiasResult() {
        super(Command.GET_LOAD_BIAS);
    }

    public GetLoadBiasResult(long loadBias) {
        super(Command.GET_LOAD_BIAS);
        this.loadBias = loadBias;
    }

    public long getLoadBias() {
        return loadBias;
    }

    @Override
    public void read(WordInputStream in) throws IOException {
        loadBias = in.read64bit();
    }

    @Override
    public void write(WordOutputStream out) throws IOException {
        out.write64bit(loadBias);
    }
}
