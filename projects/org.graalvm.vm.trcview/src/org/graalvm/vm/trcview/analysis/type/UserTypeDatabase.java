package org.graalvm.vm.trcview.analysis.type;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class UserTypeDatabase {
    private final UserTypeDatabase parent;
    private final Map<String, UserDefinedType> types = new HashMap<>();

    public UserTypeDatabase() {
        parent = null;
    }

    public UserTypeDatabase(UserTypeDatabase parent) {
        this.parent = parent;
    }

    // recursively resolve name
    public boolean contains(String name) {
        if (types.containsKey(name)) {
            return true;
        }
        if (parent != null) {
            return parent.contains(name);
        } else {
            return false;
        }
    }

    public void add(UserDefinedType type) throws NameAlreadyUsedException {
        add(type, false);
    }

    public void add(UserDefinedType type, boolean force) throws NameAlreadyUsedException {
        if (!force && contains(type.getName())) {
            throw new NameAlreadyUsedException(type.getName());
        }
        types.put(type.getName(), type);
    }

    public void rename(UserDefinedType type, String name) throws NameAlreadyUsedException {
        if (name.equals(type.getName())) {
            return;
        }

        if (contains(name)) {
            throw new NameAlreadyUsedException(name);
        }

        types.remove(type.getName());
        type.setName(name);
        types.put(type.getName(), type);
    }

    // recursively resolve name
    public UserDefinedType get(String name) {
        UserDefinedType type = types.get(name);
        if (type == null && parent != null) {
            return parent.get(name);
        } else {
            return type;
        }
    }

    public void undefine(String name) {
        types.remove(name);
    }

    public Collection<UserDefinedType> getTypes() {
        return types.values();
    }
}
