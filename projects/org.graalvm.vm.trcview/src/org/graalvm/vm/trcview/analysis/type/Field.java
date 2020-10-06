package org.graalvm.vm.trcview.analysis.type;

import org.graalvm.vm.util.HexFormatter;

public class Field {
    private long offset;
    private String name;
    private Type type;

    public Field(long offset, String name, Type type) {
        this.offset = offset;
        this.name = name;
        this.type = type;
    }

    public long getSize() {
        return type.getSize();
    }

    public long getOffset() {
        return offset;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean hasName() {
        return name != null;
    }

    public String getName() {
        if (name == null) {
            return "field_" + HexFormatter.tohex(offset);
        } else {
            return name;
        }
    }

    public void setName(Struct struct, String name) throws NameAlreadyUsedException {
        if (name == null) {
            // clear name
            this.name = null;
            return;
        }

        if (name.equals(this.name)) {
            // new name is the same as the old name
            return;
        }

        if (struct == null) {
            // this field is not member of a struct
            this.name = name;
            return;
        }

        // check for collisions
        Field field = struct.getField(name);
        if (field != null && field != this) {
            throw new NameAlreadyUsedException(name);
        }
        this.name = name;
    }

    public long end() {
        return offset + type.getSize();
    }

    @Override
    public String toString() {
        return type + " " + getName();
    }
}
