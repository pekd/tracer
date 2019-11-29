package org.graalvm.vm.trcview.arch.pdp11.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.DeviceEvent;
import org.graalvm.vm.trcview.arch.io.MemoryDumpEvent;
import org.graalvm.vm.trcview.arch.pdp11.PDP11;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class PDP11MemoryDumpEvent extends DeviceEvent {
    private final short address;
    private final short length;
    private final byte[] data;

    public PDP11MemoryDumpEvent(WordInputStream in, int tid) throws IOException {
        super(PDP11.ID, tid);
        address = in.read16bit();
        length = in.read16bit();
        data = new byte[Short.toUnsignedInt(length)];
        in.read(data);
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        throw new AssertionError("this function should not be called");
    }

    public MemoryDumpEvent getMemoryDumpEvent() {
        return new MemoryDumpEvent(getTid(), Short.toUnsignedLong(address), data);
    }
}
