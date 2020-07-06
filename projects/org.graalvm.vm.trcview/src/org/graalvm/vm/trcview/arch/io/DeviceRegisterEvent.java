package org.graalvm.vm.trcview.arch.io;

import java.util.Collections;
import java.util.List;

import org.graalvm.vm.trcview.analysis.device.RegisterValue;

public abstract class DeviceRegisterEvent extends Event {
    protected DeviceRegisterEvent(short arch, int tid) {
        super(arch, DEVICEREG, tid);
    }

    public abstract int getDeviceId();

    public List<RegisterValue> getValues() {
        return Collections.emptyList();
    }

    public List<RegisterValue> getReads() {
        return Collections.emptyList();
    }

    public List<RegisterValue> getWrites() {
        return Collections.emptyList();
    }
}
