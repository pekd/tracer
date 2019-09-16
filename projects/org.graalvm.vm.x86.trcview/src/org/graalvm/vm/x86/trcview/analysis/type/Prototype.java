package org.graalvm.vm.x86.trcview.analysis.type;

import java.util.Collections;
import java.util.List;

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
}
