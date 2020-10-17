package org.graalvm.vm.trcview.arch.pdp11.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.DeviceEvent;
import org.graalvm.vm.trcview.arch.pdp11.device.PDP11Devices;
import org.graalvm.vm.util.io.WordInputStream;

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
        super(tid);
        type = in.read16bit();
        value = in.read16bit();
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

    @Override
    public int getDeviceId() {
        return PDP11Devices.CPU;
    }

    @Override
    public String getMessage() {
        switch (type) {
            case CPU_TRAP:
                return "TRAP";
            case CPU_HALT:
                return "HALT";
            case CPU_WAIT:
                return "WAIT";
            case CPU_RUN:
                return "RUN";
            case CPU_DBLBUS:
                return "DOUBLE BUS TRAP";
            case CPU_ODT_P:
                return "CONTINUE FROM ODT";
            default:
                return "???";
        }
    }
}
