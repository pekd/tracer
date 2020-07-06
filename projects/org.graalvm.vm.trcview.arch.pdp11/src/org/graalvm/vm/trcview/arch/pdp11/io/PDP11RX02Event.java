package org.graalvm.vm.trcview.arch.pdp11.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.graalvm.vm.trcview.analysis.device.RegisterValue;
import org.graalvm.vm.trcview.arch.io.DeviceRegisterEvent;
import org.graalvm.vm.trcview.arch.pdp11.PDP11;
import org.graalvm.vm.trcview.arch.pdp11.device.PDP11Devices;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class PDP11RX02Event extends DeviceRegisterEvent {
    private final short rx2cs;
    private final short rx2ta;
    private final short rx2sa;
    private final short rx2wc;
    private final short rx2ba;
    private final short rx2es;
    private final short command;
    private final short status;

    public PDP11RX02Event(WordInputStream in, int tid) throws IOException {
        super(PDP11.ID, tid);
        rx2cs = in.read16bit();
        rx2ta = in.read16bit();
        rx2sa = in.read16bit();
        rx2wc = in.read16bit();
        rx2ba = in.read16bit();
        rx2es = in.read16bit();
        command = in.read16bit();
        status = in.read16bit();
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        out.write16bit(rx2cs);
        out.write16bit(rx2ta);
        out.write16bit(rx2sa);
        out.write16bit(rx2wc);
        out.write16bit(rx2ba);
        out.write16bit(rx2es);
        out.write16bit(command);
        out.write16bit(status);
    }

    @Override
    public int getDeviceId() {
        return PDP11Devices.RXV21;
    }

    @Override
    public List<RegisterValue> getValues() {
        List<RegisterValue> values = new ArrayList<>();
        values.add(new RegisterValue(PDP11Devices.RXV21_RX2CS, Short.toUnsignedLong(rx2cs)));
        values.add(new RegisterValue(PDP11Devices.RXV21_RX2TA, Short.toUnsignedLong(rx2ta)));
        values.add(new RegisterValue(PDP11Devices.RXV21_RX2SA, Short.toUnsignedLong(rx2sa)));
        values.add(new RegisterValue(PDP11Devices.RXV21_RX2WC, Short.toUnsignedLong(rx2wc)));
        values.add(new RegisterValue(PDP11Devices.RXV21_RX2BA, Short.toUnsignedLong(rx2ba)));
        values.add(new RegisterValue(PDP11Devices.RXV21_RX2ES, Short.toUnsignedLong(rx2es)));
        return values;
    }
}
