package org.graalvm.vm.x86.trcview.net.protocol.cmdresult;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.Command;

public class AddSubroutineResult extends Result {
    public AddSubroutineResult() {
        super(Command.ADD_SUBROUTINE);
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
