package org.graalvm.vm.trcview.script.ast;

import java.util.Collections;
import java.util.List;

import org.graalvm.vm.trcview.script.rt.Context;
import org.graalvm.vm.trcview.script.rt.Pointer;
import org.graalvm.vm.trcview.script.rt.ReturnException;
import org.graalvm.vm.trcview.script.type.PointerType;
import org.graalvm.vm.trcview.script.type.Type;

public class Function {
    private final String name;
    private List<Variable> args;
    private List<Statement> body;
    private final Type returnType;
    private final List<Type> argTypes;

    public Function(String name, Type returnType, List<Type> argTypes, List<Variable> args, List<Statement> body) {
        this.name = name;
        this.returnType = returnType;
        this.argTypes = argTypes;
        this.args = args;
        this.body = body;
    }

    public Function(String name, Type returnType, List<Type> argTypes, List<Variable> args) {
        this.name = name;
        this.returnType = returnType;
        this.argTypes = argTypes;
        this.args = args;
        this.body = Collections.emptyList();
    }

    public String getName() {
        return name;
    }

    public Type getReturnType() {
        return returnType;
    }

    public List<Type> getArgumentTypes() {
        return argTypes;
    }

    public List<Variable> getArguments() {
        return args;
    }

    public void setArguments(List<Variable> args) {
        if (args.size() != this.args.size()) {
            throw new IllegalArgumentException(name + ": invalid number of arguments");
        }
        // TODO: check types
        this.args = args;
    }

    public long execute(Context ctx, Object... arguments) {
        Context nctx = new Context(ctx);
        if (arguments.length != args.size()) {
            throw new IllegalArgumentException(name + ": invalid number of arguments");
        }
        for (int i = 0; i < arguments.length; i++) {
            Type type = argTypes.get(i);
            if (type instanceof PointerType) {
                nctx.setPointer(args.get(i), (Pointer) arguments[i]);
            } else {
                nctx.set(args.get(i), (long) arguments[i]);
            }
        }
        long result = 0;
        try {
            for (Statement stmt : body) {
                result = stmt.execute(nctx);
            }
            return result;
        } catch (ReturnException e) {
            return e.getValue();
        }
    }

    public Pointer executePointer(Context ctx, Object... arguments) {
        Context nctx = new Context(ctx);
        if (arguments.length != args.size()) {
            throw new IllegalArgumentException(name + ": invalid number of arguments");
        }
        for (int i = 0; i < arguments.length; i++) {
            Type type = argTypes.get(i);
            if (type instanceof PointerType) {
                nctx.setPointer(args.get(i), (Pointer) arguments[i]);
            } else {
                nctx.set(args.get(i), (long) arguments[i]);
            }
        }
        try {
            for (Statement stmt : body) {
                stmt.execute(nctx);
            }
            return null;
        } catch (ReturnException e) {
            return e.getPointer();
        }
    }
}
