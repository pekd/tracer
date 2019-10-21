package org.graalvm.vm.x86.trcview.net.protocol.cmdresult;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.Command;

public class GetOffsetResult extends Result {
    private long offset;

    public GetOffsetResult() {
        super(Command.GET_OFFSET);
    }

    public GetOffsetResult(long offset) {
        super(Command.GET_OFFSET);
        this.offset = offset;
    }

    public long getOffset() {
        return offset;
    }

    @Override
    public void read(WordInputStream in) throws IOException {
        offset = in.read64bit();
    }

    @Override
    public void write(WordOutputStream out) throws IOException {
        out.write64bit(offset);
    }
}
