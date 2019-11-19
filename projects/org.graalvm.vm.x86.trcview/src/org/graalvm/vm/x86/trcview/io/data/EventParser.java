package org.graalvm.vm.x86.trcview.io.data;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;

public abstract class EventParser {
    public abstract <T extends Event> T parse(WordInputStream in, byte id, int tid) throws IOException;
}
