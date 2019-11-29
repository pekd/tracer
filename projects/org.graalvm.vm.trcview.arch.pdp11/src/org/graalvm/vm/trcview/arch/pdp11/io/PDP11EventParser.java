package org.graalvm.vm.trcview.arch.pdp11.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.EventParser;
import org.graalvm.vm.util.io.WordInputStream;

public class PDP11EventParser extends EventParser {
    @Override
    public <T extends Event> T parse(WordInputStream in, byte id, int tid) throws IOException {
        return null;
    }
}
