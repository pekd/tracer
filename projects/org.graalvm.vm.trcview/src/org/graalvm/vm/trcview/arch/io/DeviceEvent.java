package org.graalvm.vm.trcview.arch.io;

public abstract class DeviceEvent extends Event {
    protected DeviceEvent(short arch, int tid) {
        super(arch, DEVICE, tid);
    }
}
