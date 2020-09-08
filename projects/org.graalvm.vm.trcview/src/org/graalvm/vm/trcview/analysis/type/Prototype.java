package org.graalvm.vm.trcview.analysis.type;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Prototype {
    public final Type returnType;
    public final List<Type> args;
    public final List<String> names;

    public Prototype() {
        this(new Type(DataType.VOID), Collections.emptyList(), Collections.emptyList());
    }

    public Prototype(Type returnType) {
        this(returnType, Collections.emptyList(), Collections.emptyList());
    }

    public Prototype(Type returnType, List<Type> args, List<String> names) {
        if (args.size() != names.size()) {
            throw new IllegalArgumentException("length of args and arg names don't match");
        }
        this.returnType = returnType;
        this.args = args;
        this.names = names;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof Prototype)) {
            return false;
        }

        Prototype p = (Prototype) o;
        return returnType.equals(p.returnType) && args.equals(p.args);
    }

    @Override
    public int hashCode() {
        return Objects.hash(returnType, args);
    }
}
