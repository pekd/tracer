package org.graalvm.vm.trcview.data;

import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.util.HexFormatter;

public class Variable {
    private String name;
    private Type type;
    private long address;

    public Variable(long address, Type type, String name) {
        this.type = type;
        this.address = address;
        setName(name);
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
            if (type != null && type.isStringData()) {
                return "asc_" + HexFormatter.tohex(address);
            } else if (type != null) {
                if (type.getType() == DataType.CODE) {
                    return "code_" + HexFormatter.tohex(address);
                } else {
                    return "data_" + HexFormatter.tohex(address);
                }
            } else {
                return "unk_" + HexFormatter.tohex(address);
            }
        } else {
            return name;
        }
    }

    public String getName(StepFormat format) {
        if (name == null) {
            if (type != null && type.isStringData()) {
                return "asc_" + format.formatShortAddress(address);
            } else if (type != null) {
                if (type.getType() == DataType.CODE) {
                    return "code_" + format.formatShortAddress(address);
                } else {
                    return "data_" + format.formatShortAddress(address);
                }
            } else {
                return "unk_" + format.formatShortAddress(address);
            }
        } else {
            return name;
        }
    }

    public long getAddress() {
        return address;
    }

    void setName(String name) {
        // clear name?
        if (name == null) {
            this.name = null;
            return;
        }

        // same name?
        if (getName().equals(name)) {
            return;
        }

        // cannot set name to unk_, data_ or asc_ since these are reserved prefixes
        if (name.startsWith("unk_") || name.startsWith("data_") || name.startsWith("asc_")) {
            return;
        }

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
