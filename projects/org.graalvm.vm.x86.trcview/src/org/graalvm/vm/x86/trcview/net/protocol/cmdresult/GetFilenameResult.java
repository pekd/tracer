package org.graalvm.vm.x86.trcview.net.protocol.cmdresult;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;
import org.graalvm.vm.x86.trcview.net.protocol.IO;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.Command;

public class GetFilenameResult extends Result {
    private String filename;

    public GetFilenameResult() {
        super(Command.GET_FILENAME);
    }

    public GetFilenameResult(String filename) {
        super(Command.GET_FILENAME);
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    @Override
    public void read(WordInputStream in) throws IOException {
        filename = IO.readString(in);
    }

    @Override
    public void write(WordOutputStream out) throws IOException {
        IO.writeString(out, filename);
    }
}
