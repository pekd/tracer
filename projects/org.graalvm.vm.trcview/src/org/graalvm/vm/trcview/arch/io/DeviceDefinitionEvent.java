package org.graalvm.vm.trcview.arch.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.graalvm.vm.trcview.analysis.device.Device;

public class DeviceDefinitionEvent extends Event {
    private List<Device> devices = new ArrayList<>();

    public DeviceDefinitionEvent() {
        super(-1);
    }

    public void addDevice(Device device) {
        devices.add(device);
    }

    public List<Device> getDevices() {
        return Collections.unmodifiableList(devices);
    }
}
