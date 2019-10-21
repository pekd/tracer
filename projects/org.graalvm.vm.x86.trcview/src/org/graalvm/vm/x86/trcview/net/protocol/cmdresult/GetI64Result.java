package org.graalvm.vm.x86.trcview.net.protocol.cmdresult;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.Command;

public class GetI64Result extends Result {
    private long value;
    private int error;

    public GetI64Result() {
        super(Command.GET_I64);
    }

    public GetI64Result(long value, int error) {
        super(Command.GET_I64);
        this.value = value;
        this.error = error;
    }

    public long getValue() {
        return value;
    }

    public int getError() {
        return error;
    }

    @Override
    public void read(WordInputStream in) throws IOException {
        value = in.read64bit();
        error = in.read();
    }

    @Override
    public void write(WordOutputStream out) throws IOException {
        out.write64bit(value);
        out.write(error);
    }
}
