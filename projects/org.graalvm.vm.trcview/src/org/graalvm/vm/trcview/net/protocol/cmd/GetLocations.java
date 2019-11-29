package org.graalvm.vm.trcview.net.protocol.cmd;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class GetLocations extends Command {
    public GetLocations() {
        super(GET_LOCATIONS);
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
