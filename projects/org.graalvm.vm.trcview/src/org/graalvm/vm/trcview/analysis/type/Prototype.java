package org.graalvm.vm.trcview.analysis.type;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Prototype {
    public final Type returnType;
    public final List<Type> args;

    public Prototype() {
        this(new Type(DataType.VOID), Collections.emptyList());
    }

    public Prototype(Type returnType) {
        this(returnType, Collections.emptyList());
    }

    public Prototype(Type returnType, List<Type> args) {
        this.returnType = returnType;
        this.args = args;
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
