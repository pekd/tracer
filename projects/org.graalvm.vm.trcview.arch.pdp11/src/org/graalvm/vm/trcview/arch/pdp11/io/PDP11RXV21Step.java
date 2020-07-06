package org.graalvm.vm.trcview.arch.pdp11.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.graalvm.vm.trcview.analysis.device.RegisterValue;
import org.graalvm.vm.trcview.arch.io.DeviceEvent;
import org.graalvm.vm.trcview.arch.pdp11.PDP11;
import org.graalvm.vm.trcview.arch.pdp11.device.PDP11Devices;
import org.graalvm.vm.trcview.arch.pdp11.device.RXV21;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class PDP11RXV21Step extends DeviceEvent {
    private final byte type;
    private final byte step;
    private final short rx2db;

    public PDP11RXV21Step(WordInputStream in, int tid) throws IOException {
        super(PDP11.ID, tid);
        type = (byte) in.read8bit();
        step = (byte) in.read8bit();
        rx2db = in.read16bit();
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        out.write8bit(type);
        out.write8bit(step);
        out.write16bit(rx2db);
    }

    @Override
    public int getDeviceId() {
        return PDP11Devices.RXV21;
    }

    @Override
    public String getMessage() {
        return String.format("internal processing step [%s, step %d]: RX2DB=%06o", RXV21.getName(type), Byte.toUnsignedInt(step), Short.toUnsignedInt(rx2db));
    }

    @Override
    public List<RegisterValue> getValues() {
        List<RegisterValue> result = new ArrayList<>();
        result.add(new RegisterValue(PDP11Devices.RXV21_RX2DB, Short.toUnsignedLong(rx2db)));
        switch (type) {
            case RXV21.FILL:
            case RXV21.EMPTY:
                switch (step) {
                    case 1:
                        result.add(new RegisterValue(PDP11Devices.RXV21_RX2WC, Short.toUnsignedLong(rx2db)));
                        break;
                    case 2:
                        result.add(new RegisterValue(PDP11Devices.RXV21_RX2BA, Short.toUnsignedLong(rx2db)));
                        break;
                }
                break;
            case RXV21.READ:
            case RXV21.WRITE:
                switch (step) {
                    case 1:
                        result.add(new RegisterValue(PDP11Devices.RXV21_RX2SA, Short.toUnsignedLong(rx2db)));
                        break;
                    case 2:
                        result.add(new RegisterValue(PDP11Devices.RXV21_RX2TA, Short.toUnsignedLong(rx2db)));
                        break;
                }
                break;
        }
        return result;
    }

    @Override
    public List<RegisterValue> getWrites() {
        return getValues();
    }
}
