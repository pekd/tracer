package org.graalvm.vm.trcview.net.protocol.cmdresult;

import java.io.IOException;

import org.graalvm.vm.trcview.net.protocol.cmd.Command;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class GetI8Result extends Result {
    private byte value;
    private int error;

    public GetI8Result() {
        super(Command.GET_I8);
    }

    public GetI8Result(byte value, int error) {
        super(Command.GET_I8);
        this.value = value;
        this.error = error;
    }

    public byte getValue() {
        return value;
    }

    public int getError() {
        return error;
    }

    @Override
    public void read(WordInputStream in) throws IOException {
        value = (byte) in.read();
        error = in.read();
    }

    @Override
    public void write(WordOutputStream out) throws IOException {
        out.write(value);
        out.write(error);
    }
}
