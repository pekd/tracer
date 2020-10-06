package org.graalvm.vm.trcview.analysis.type;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class UserTypeDatabase {
    private final Map<String, UserDefinedType> types = new HashMap<>();

    public void add(UserDefinedType type) throws NameAlreadyUsedException {
        if (types.containsKey(type.getName())) {
            throw new NameAlreadyUsedException(type.getName());
        }
        types.put(type.getName(), type);
    }

    public void rename(UserDefinedType type, String name) throws NameAlreadyUsedException {
        if (name.equals(type.getName())) {
            return;
        }

        if (types.containsKey(name)) {
            throw new NameAlreadyUsedException(name);
        }

        types.remove(type.getName());
        type.setName(name);
        types.put(type.getName(), type);
    }

    public UserDefinedType get(String name) {
        return types.get(name);
    }

    public void undefine(String name) {
        types.remove(name);
    }

    public Collection<UserDefinedType> getTypes() {
        return types.values();
    }
}
