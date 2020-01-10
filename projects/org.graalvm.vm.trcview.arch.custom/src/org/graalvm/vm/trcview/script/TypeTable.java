package org.graalvm.vm.trcview.script;

import java.util.HashMap;
import java.util.Map;

import org.graalvm.vm.trcview.script.type.BasicType;
import org.graalvm.vm.trcview.script.type.PrimitiveType;
import org.graalvm.vm.trcview.script.type.Struct;
import org.graalvm.vm.trcview.script.type.Type;

public class TypeTable {
    public static final Type ERROR_TYPE = new PrimitiveType(BasicType.INT);

    private final ErrorHandler errors;
    private Map<String, Type> types;
    private Map<String, Struct> structs;

    public TypeTable(ErrorHandler errors) {
        this.errors = errors;
        types = new HashMap<>();
        structs = new HashMap<>();

        defaultTypes();
    }

    private void defaultTypes() {
        add(new PrimitiveType(BasicType.CHAR, false), "int8_t");
        add(new PrimitiveType(BasicType.CHAR, true), "uint8_t");
        add(new PrimitiveType(BasicType.SHORT, false), "int16_t");
        add(new PrimitiveType(BasicType.SHORT, true), "uint16_t");
        add(new PrimitiveType(BasicType.INT, false), "int32_t");
        add(new PrimitiveType(BasicType.INT, true), "uint32_t");
        add(new PrimitiveType(BasicType.LONG, false), "int64_t");
        add(new PrimitiveType(BasicType.LONG, true), "uint64_t");
    }

    public void add(Type type, String name) {
        if (types.containsKey(name)) {
            errors.error(Message.DUPLICATE_TYPE, name);
        }
        types.put(name, type);
    }

    public Type get(String name) {
        Type type = types.get(name);
        if (type == null) {
            errors.error(Message.UNKNOWN_TYPE, name);
            type = ERROR_TYPE;
        }
        return type;
    }

    public boolean hasType(String name) {
        return types.containsKey(name);
    }

    public void addStruct(Struct struct, String name) {
        if (structs.containsKey(name)) {
            if (structs.get(name).getMembers().size() != 0) {
                errors.error(Message.DUPLICATE_TYPE, name);
            } else {
                structs.get(name).copyof(struct);
            }
        } else {
            structs.put(name, struct);
        }
    }

    public boolean hasStruct(String name) {
        return structs.containsKey(name);
    }

    public Struct getStruct(String name) {
        Struct struct = structs.get(name);
        if (structs == null) {
            errors.error(Message.UNKNOWN_TYPE, name);
            struct = new Struct(name);
        }
        return struct;
    }
}
