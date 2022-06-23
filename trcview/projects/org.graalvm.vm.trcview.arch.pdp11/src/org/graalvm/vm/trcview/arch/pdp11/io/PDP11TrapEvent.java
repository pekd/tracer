package org.graalvm.vm.trcview.arch.pdp11.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.DeviceEvent;
import org.graalvm.vm.trcview.arch.pdp11.device.KD11;
import org.graalvm.vm.trcview.arch.pdp11.device.PDP11Devices;
import org.graalvm.vm.util.io.WordInputStream;

public class PDP11TrapEvent extends DeviceEvent {
    public static final int TRAP = 0;
    public static final int TRAP_ILL = 1;
    public static final int TRAP_RADDR = 2;
    public static final int TRAP_T = 3;

    private final short trap;
    private final short cause;

    public PDP11TrapEvent(WordInputStream in, int tid) throws IOException {
        super(tid);
        trap = in.read16bit();
        cause = in.read16bit();
    }

    @Override
    public int getDeviceId() {
        return PDP11Devices.CPU;
    }

    @Override
    public String getMessage() {
        String strap = Integer.toString(Short.toUnsignedInt(trap), 8);
        String name;
        switch (cause) {
            default:
            case TRAP:
                name = KD11.getTrapName(trap);
                if (name != null) {
                    return "TRAP " + strap + ": " + name;
                } else {
                    return "TRAP " + strap;
                }
            case TRAP_T:
                return "TRAP " + strap + ": T bit";
            case TRAP_RADDR:
                return "TRAP " + strap + ": get address on mode 0";
            case TRAP_ILL:
                return "TRAP " + strap + ": illegal instruction";
        }
    }
}
