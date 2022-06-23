package org.graalvm.vm.trcview.arch.pdp11.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.MemoryDumpEvent;
import org.graalvm.vm.util.io.WordInputStream;

public class PDP11MemoryDumpEvent extends Event {
    private final short address;
    private final short length;
    private final byte[] data;

    public PDP11MemoryDumpEvent(WordInputStream in, int tid) throws IOException {
        super(tid);
        address = in.read16bit();
        length = in.read16bit();
        data = new byte[Short.toUnsignedInt(length)];
        in.read(data);
    }

    public MemoryDumpEvent getMemoryDumpEvent() {
        return new MemoryDumpEvent(getTid(), Short.toUnsignedLong(address), data);
    }
}
