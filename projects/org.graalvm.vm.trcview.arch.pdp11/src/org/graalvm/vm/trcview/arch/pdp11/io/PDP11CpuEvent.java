package org.graalvm.vm.trcview.arch.pdp11.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.DeviceEvent;
import org.graalvm.vm.trcview.arch.pdp11.PDP11;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class PDP11CpuEvent extends DeviceEvent {
    public static final short CPU_TRAP = 0;
    public static final short CPU_HALT = 1;
    public static final short CPU_WAIT = 2;
    public static final short CPU_RUN = 3;
    public static final short CPU_DBLBUS = 4;
    public static final short CPU_ODT_P = 5;
    public static final short CPU_ODT_G = 5;

    private final short type;
    private final short value;

    public PDP11CpuEvent(WordInputStream in, int tid) throws IOException {
        super(PDP11.ID, tid);
        type = in.read16bit();
        value = in.read16bit();
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        out.write16bit(type);
        out.write16bit(value);
    }

    public short getType() {
        return type;
    }

    public short getValue() {
        return value;
    }

    public PDP11CpuTrapEvent getTrapEvent(PDP11StepEvent lastStep) {
        return new PDP11CpuTrapEvent(getTid(), value, lastStep);
    }
}
