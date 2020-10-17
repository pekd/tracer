package org.graalvm.vm.trcview.arch.io;

import java.util.Collections;
import java.util.List;

import org.graalvm.vm.trcview.analysis.device.RegisterValue;

public abstract class DeviceEvent extends Event {
    private long step; // store step id here for memory efficiency reasons

    protected DeviceEvent(int tid) {
        super(tid);
    }

    public void setStep(long step) {
        this.step = step;
    }

    public long getStep() {
        return step;
    }

    public abstract int getDeviceId();

    public abstract String getMessage();

    public List<RegisterValue> getValues() {
        return Collections.emptyList();
    }

    public List<RegisterValue> getWrites() {
        return Collections.emptyList();
    }
}
