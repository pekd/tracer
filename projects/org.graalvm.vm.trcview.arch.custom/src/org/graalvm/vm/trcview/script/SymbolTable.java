package org.graalvm.vm.trcview.script;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.graalvm.vm.trcview.script.ast.Function;
import org.graalvm.vm.trcview.script.ast.Intrinsic;
import org.graalvm.vm.trcview.script.ast.Variable;
import org.graalvm.vm.trcview.script.rt.Context;
import org.graalvm.vm.trcview.script.type.BasicType;
import org.graalvm.vm.trcview.script.type.PrimitiveType;
import org.graalvm.vm.trcview.script.type.Type;

public class SymbolTable {
    public final Variable errorvar = new Variable("$$err$$", new PrimitiveType(BasicType.VOID));
    public final Function errorfunc = new Intrinsic("$$errfunc$$", new PrimitiveType(BasicType.VOID), Collections.emptyList()) {
        @Override
        public long execute(Context ctx, Object... args) {
            throw new AssertionError();
        }
    };

    private final ErrorHandler errors;
    private final Map<String, Function> functions = new HashMap<>();
    private Scope scope = new Scope(null);

    public SymbolTable(ErrorHandler errors) {
        this.errors = errors;
    }

    public Function getFunction(String name) {
        return functions.get(name);
    }

    public void define(Function function) {
        if (functions.containsKey(function.getName())) {
            Function func = functions.get(function.getName());
            func.setArguments(function.getArguments());
        } else {
            functions.put(function.getName(), function);
        }
    }

    public void enter() {
        scope = new Scope(scope);
    }

    public void leave() {
        scope = scope.parent;
    }

    public Variable get(String name) {
        return scope.get(name);
    }

    public Variable define(String name, Type type) {
        Variable var = new Variable(name, type);
        if (scope.syms.containsKey(name)) {
            errors.error(Message.REDEFINE_SYMBOL, name);
            return errorvar;
        }
        scope.syms.put(name, var);
        return var;
    }

    private class Scope {
        final Scope parent;
        Map<String, Variable> syms;

        public Scope(Scope parent) {
            this.parent = parent;
            syms = new HashMap<>();
        }

        public Variable get(String name) {
            Variable sym = syms.get(name);
            if (sym == null) {
                if (parent == null) {
                    errors.error(Message.UNKNOWN_SYMBOL, name);
                    return errorvar;
                } else {
                    return parent.get(name);
                }
            } else {
                return sym;
            }
        }
    }
}
