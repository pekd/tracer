package org.graalvm.vm.trcview.analysis.device;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.graalvm.vm.trcview.arch.io.DeviceEvent;

public class Device {
    private final String name;
    private final int id;
    private final DeviceType type;

    private final Map<Integer, DeviceRegister> registers;
    private final List<DeviceEvent> events;

    public Device(int id, String name, DeviceType type) {
        this.id = id;
        this.name = name;
        this.type = type;
        registers = new HashMap<>();
        events = new ArrayList<>();
    }

    public void add(DeviceRegister reg) {
        registers.put(reg.getId(), reg);
    }

    public void addEvent(DeviceEvent event) {
        events.add(event);
    }

    public void addValue(long step, RegisterValue value) {
        DeviceRegister reg = registers.get(value.id);
        if (reg != null) {
            reg.value(step, value.value);
        }
    }

    public void addRead(long step, RegisterValue value) {
        DeviceRegister reg = registers.get(value.id);
        if (reg != null) {
            reg.read(step, value.value);
        }
    }

    public void addWrite(long step, RegisterValue value) {
        DeviceRegister reg = registers.get(value.id);
        if (reg != null) {
            reg.write(step, value.value);
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public DeviceType getType() {
        return type;
    }

    public List<DeviceEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public Map<Integer, DeviceRegister> getRegisters() {
        return Collections.unmodifiableMap(registers);
    }

    @Override
    public String toString() {
        return name;
    }
}
