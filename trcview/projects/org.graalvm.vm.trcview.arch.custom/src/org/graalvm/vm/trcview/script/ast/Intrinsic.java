package org.graalvm.vm.trcview.script.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.graalvm.vm.trcview.script.rt.Context;
import org.graalvm.vm.trcview.script.type.Type;

public abstract class Intrinsic extends Function {
    public Intrinsic(String name, Type returnType, List<Type> argTypes) {
        super(name, returnType, argTypes, createArgs(argTypes), Collections.emptyList());
    }

    // varargs
    public Intrinsic(String name, Type returnType, List<Type> argTypes, boolean vararg) {
        super(name, returnType, argTypes, createArgs(argTypes), vararg);
    }

    private static List<Variable> createArgs(List<Type> argTypes) {
        List<Variable> vars = new ArrayList<>();
        int i = 1;
        for (Type type : argTypes) {
            vars.add(new Variable("a" + i++, type));
        }
        return vars;
    }

    @Override
    public abstract long execute(Context ctx, Object... args);
}
