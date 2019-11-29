package org.graalvm.vm.trcview.net.protocol.cmdresult;

import java.io.IOException;

import org.graalvm.vm.trcview.net.protocol.cmd.Command;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class AddListenerResult extends Result {
    public AddListenerResult() {
        super(Command.ADD_LISTENER);
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
