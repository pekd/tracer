package org.graalvm.vm.trcview.arch.pdp11.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.DeviceEvent;
import org.graalvm.vm.trcview.arch.pdp11.device.KD11;
import org.graalvm.vm.trcview.arch.pdp11.device.PDP11Devices;
import org.graalvm.vm.util.io.WordInputStream;

public class PDP11IrqEvent extends DeviceEvent {
    public static final int IRQ_OK = 0;
    public static final int IRQ_FAIL = 1;
    public static final int IRQ_SIG = 2;

    private final short trap;
    private final short type;

    public PDP11IrqEvent(WordInputStream in, int tid) throws IOException {
        super(tid);
        trap = in.read16bit();
        type = in.read16bit();
    }

    @Override
    public int getDeviceId() {
        return PDP11Devices.CPU;
    }

    @Override
    public String getMessage() {
        String strap = Integer.toString(Short.toUnsignedInt(trap), 8);
        String name = KD11.getTrapName(trap);
        String sname = name == null ? "" : (" (" + name + ")");
        switch (type) {
            case IRQ_OK:
                return "interrupt request " + strap + sname;
            case IRQ_FAIL:
                return "interrupt request " + strap + sname + " denied";
            case IRQ_SIG:
                return "signaling IRQ " + strap + sname;
            default:
                return "???";
        }
    }
}
