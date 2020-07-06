package org.graalvm.vm.trcview.arch.pdp11.io;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.graalvm.vm.trcview.analysis.device.RegisterValue;
import org.graalvm.vm.trcview.arch.io.DeviceRegisterEvent;
import org.graalvm.vm.trcview.arch.pdp11.PDP11;
import org.graalvm.vm.util.io.WordOutputStream;

public class PDP11DeviceRegisterEvent extends DeviceRegisterEvent {
    private final int dev;
    private final int reg;
    private final short value;
    private final boolean read;
    private final boolean write;

    public PDP11DeviceRegisterEvent(int tid, int dev, int reg, short value) {
        super(PDP11.ID, tid);
        this.dev = dev;
        this.reg = reg;
        this.value = value;
        this.write = false;
        this.read = false;
    }

    public PDP11DeviceRegisterEvent(int tid, int dev, int reg, short value, boolean write) {
        super(PDP11.ID, tid);
        this.dev = dev;
        this.reg = reg;
        this.value = value;
        this.write = write;
        this.read = !write;
    }

    @Override
    public int getDeviceId() {
        return dev;
    }

    @Override
    public List<RegisterValue> getValues() {
        return Collections.singletonList(new RegisterValue(reg, Short.toUnsignedLong(value)));
    }

    @Override
    public List<RegisterValue> getWrites() {
        if (write) {
            return getValues();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<RegisterValue> getReads() {
        if (read) {
            return getValues();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        // TODO Auto-generated method stub
    }
}
