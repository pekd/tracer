package org.graalvm.vm.trcview.analysis.type;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

public class Struct extends UserDefinedType {
    private final NavigableMap<Long, Field> fields;

    public Struct(String name) {
        super(name);
        fields = new TreeMap<>();
    }

    public void add(String name, Type type) {
        add(getSize(), name, type);
    }

    public void add(long offset, String name, Type type) {
        fields.put(offset, new Field(offset, name, type));
    }

    public long getSize() {
        Entry<Long, Field> last = fields.lastEntry();
        if (last == null) {
            return 0;
        } else {
            return last.getValue().end();
        }
    }

    public Field getField(String name) {
        for (Field field : fields.values()) {
            if (name.equals(field.getName())) {
                return field;
            }
        }
        return null;
    }

    public Field getFieldAt(long offset) {
        return fields.get(offset);
    }

    public Collection<Field> getFields() {
        return fields.values();
    }

    public void set(Struct s) {
        fields.clear();
        for (Field field : s.getFields()) {
            add(field.getOffset(), field.getName(), field.getType());
        }
    }

    @Override
    public String toString() {
        StringBuilder fielddef = new StringBuilder();
        for (Field field : fields.values()) {
            Type type = field.getType();
            fielddef.append(' ');
            fielddef.append(type.toString(false));
            fielddef.append(' ');
            fielddef.append(field.getName());
            if (type.getElements() != -1) {
                fielddef.append('[');
                fielddef.append(type.getElements());
                fielddef.append(']');
            }
            fielddef.append(';');
        }
        return "struct " + getName() + " {" + fielddef + " }";
    }

    public String prettyprint() {
        StringBuilder fielddef = new StringBuilder();
        for (Field field : fields.values()) {
            Type type = field.getType();
            fielddef.append('\t');
            fielddef.append(type.toString(false));
            fielddef.append(' ');
            fielddef.append(field.getName());
            if (type.getElements() != -1) {
                fielddef.append('[');
                fielddef.append(type.getElements());
                fielddef.append(']');
            }
            fielddef.append(";\n");
        }
        return "struct " + getName() + " {\n" + fielddef + "}";
    }

    @Override
    public int hashCode() {
        return fields.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (!(o instanceof Struct)) {
            return false;
        }

        Struct s = (Struct) o;

        if (fields.size() != s.fields.size()) {
            return false;
        }

        for (Entry<Long, Field> entry : fields.entrySet()) {
            Field f = s.getFieldAt(entry.getKey());
            if (!entry.getValue().equals(f)) {
                return false;
            }
        }

        return true;
    }
}
