package org.graalvm.vm.trcview.data;

import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.util.HexFormatter;

public class Variable {
    private String name;
    private Type type;
    private long address;

    public Variable(long address, Type type, String name) {
        this.name = name;
        this.type = type;
        this.address = address;
    }

    public Variable(long address, Type type) {
        this(address, type, null);
    }

    public Variable(long address) {
        this(address, null, null);
    }

    public String getRawName() {
        return name;
    }

    public String getName() {
        if (name == null) {
            return "unk_" + HexFormatter.tohex(address);
        } else {
            return name;
        }
    }

    public long getAddress() {
        return address;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public long getSize() {
        if (type == null) {
            return 1;
        } else {
            long size = type.getSize();
            if (size < 1) {
                return 1;
            } else {
                return type.getSize();
            }
        }
    }

    public boolean contains(long addr) {
        long size = type != null ? type.getSize() : 0;
        return address == addr || (addr >= address && addr < (address + size));
    }
}
