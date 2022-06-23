package org.graalvm.vm.trcview.script.rt;

import java.util.IdentityHashMap;
import java.util.Map;

import org.graalvm.vm.trcview.script.SymbolTable;
import org.graalvm.vm.trcview.script.ast.Variable;

public class Context {
    private final Context parent;
    private final SymbolTable symtab;
    private final Map<Variable, Long> vars = new IdentityHashMap<>();
    private final Map<Variable, Pointer> structvars = new IdentityHashMap<>();
    private final Map<Variable, Long> globals = new IdentityHashMap<>();

    public Context() {
        parent = null;
        symtab = null;
    }

    public Context(SymbolTable symtab) {
        this.parent = null;
        this.symtab = symtab;
    }

    public Context(Context parent) {
        this.parent = parent;
        this.symtab = parent.symtab;
    }

    public long getGlobal(Variable var) {
        if (parent == null) {
            return globals.get(var);
        } else {
            return parent.getGlobal(var);
        }
    }

    public void setGlobal(Variable var, long value) {
        if (parent == null) {
            globals.put(var, value);
        } else {
            parent.setGlobal(var, value);
        }
    }

    public long get(Variable var) {
        return vars.get(var);
    }

    public Pointer getPointer(Variable var) {
        return structvars.get(var);
    }

    public void setPointer(Variable var, Pointer ptr) {
        structvars.put(var, ptr);
    }

    public void set(Variable var, long value) {
        vars.put(var, value);
    }
}
