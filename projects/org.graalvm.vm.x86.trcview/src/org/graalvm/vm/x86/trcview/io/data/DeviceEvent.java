package org.graalvm.vm.x86.trcview.io.data;

public abstract class DeviceEvent extends Event {
    protected DeviceEvent(short arch, int tid) {
        super(arch, DEVICE, tid);
    }
}
