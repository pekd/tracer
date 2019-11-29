package org.graalvm.vm.trcview.net.protocol.cmdresult;

import java.io.IOException;

import org.graalvm.vm.trcview.net.protocol.cmd.Command;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class GetI16Result extends Result {
    private short value;
    private int error;

    public GetI16Result() {
        super(Command.GET_I16);
    }

    public GetI16Result(short value, int error) {
        super(Command.GET_I16);
        this.value = value;
        this.error = error;
    }

    public short getValue() {
        return value;
    }

    public int getError() {
        return error;
    }

    @Override
    public void read(WordInputStream in) throws IOException {
        value = in.read16bit();
        error = in.read();
    }

    @Override
    public void write(WordOutputStream out) throws IOException {
        out.write16bit(value);
        out.write(error);
    }
}
