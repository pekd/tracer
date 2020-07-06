package org.graalvm.vm.trcview.arch.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.graalvm.vm.trcview.analysis.device.Device;
import org.graalvm.vm.util.io.WordOutputStream;

public class DeviceDefinitionEvent extends Event {
    private List<Device> devices = new ArrayList<>();

    public DeviceDefinitionEvent(short arch) {
        super(arch, (byte) -1, -1);
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        // TODO Auto-generated method stub
    }

    public void addDevice(Device device) {
        devices.add(device);
    }

    public List<Device> getDevices() {
        return Collections.unmodifiableList(devices);
    }
}
