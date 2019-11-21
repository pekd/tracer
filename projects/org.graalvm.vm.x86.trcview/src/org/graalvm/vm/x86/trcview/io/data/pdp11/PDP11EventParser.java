package org.graalvm.vm.x86.trcview.io.data.pdp11;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.x86.trcview.io.data.Event;
import org.graalvm.vm.x86.trcview.io.data.EventParser;

public class PDP11EventParser extends EventParser {
    @Override
    public <T extends Event> T parse(WordInputStream in, byte id, int tid) throws IOException {
        return null;
    }
}
