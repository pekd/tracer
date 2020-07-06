package org.graalvm.vm.trcview.analysis.device;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeviceRegister {
    private int id;
    private String name;
    private long address;

    private List<RegisterAccess> reads = new ArrayList<>();
    private List<RegisterAccess> writes = new ArrayList<>();
    private List<RegisterAccess> values = new ArrayList<>();

    private List<FieldFormat> format = new ArrayList<>();

    public DeviceRegister(int id, String name, long address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }

    public DeviceRegister(int id, String name, long address, FieldFormat... fmt) {
        this(id, name, address);

        for (FieldFormat f : fmt) {
            format.add(f);
        }
        sortFormat();
    }

    public void addFormat(FieldFormat fmt) {
        format.add(fmt);
        sortFormat();
    }

    private void sortFormat() {
        Collections.sort(format, (a, b) -> a.field.hi - b.field.hi);
    }

    public List<FieldFormat> getFormat() {
        return Collections.unmodifiableList(format);
    }

    public long getAddress() {
        return address;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void read(long step, long value) {
        reads.add(new RegisterAccess(value, step));
    }

    public void write(long step, long value) {
        writes.add(new RegisterAccess(value, step));
    }

    public void value(long step, long value) {
        values.add(new RegisterAccess(value, step));
    }

    private static RegisterAccess getLast(List<RegisterAccess> accesses, long step) {
        // shortcuts
        if (accesses.isEmpty()) {
            return null; // no reads
        } else if (accesses.get(0).step > step) {
            return null; // first read is after step
        } else if (accesses.get(accesses.size() - 1).step < step) {
            return accesses.get(accesses.size() - 1); // last read is before step
        }

        int idx = Collections.binarySearch(accesses, new RegisterAccess(0, step), (a, b) -> Long.compareUnsigned(a.step, b.step));
        if (idx >= 0) {
            return accesses.get(idx);
        } else {
            idx = ~idx;
            if (idx == 0) {
                throw new AssertionError("idx = 0, this should not happen");
            } else {
                return accesses.get(idx - 1);
            }
        }
    }

    public RegisterAccess getLastWrite(long step) {
        return getLast(writes, step);
    }

    public RegisterAccess getLastRead(long step) {
        return getLast(reads, step);
    }

    public RegisterAccess getLastValue(long step) {
        return getLast(values, step);
    }

    @Override
    public String toString() {
        return getName();
    }
}
