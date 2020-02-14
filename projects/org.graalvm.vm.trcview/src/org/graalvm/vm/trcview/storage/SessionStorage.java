package org.graalvm.vm.trcview.storage;

import java.awt.Color;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.trcview.analysis.ComputedSymbol;
import org.graalvm.vm.trcview.analysis.ComputedSymbol.Type;
import org.graalvm.vm.trcview.analysis.type.Prototype;

public abstract class SessionStorage implements Closeable {
    public abstract void setTrace(String trace);

    public abstract Symbol getSymbol(long pc);

    public abstract ComputedSymbol getComputedSymbol(long pc);

    public abstract void renameSymbol(ComputedSymbol sym, String name);

    public abstract void setPrototype(ComputedSymbol sym, Prototype prototype);

    public abstract Set<ComputedSymbol> getSubroutines();

    public abstract Set<ComputedSymbol> getLocations();

    public abstract Collection<ComputedSymbol> getSymbols();

    public Map<String, List<ComputedSymbol>> getNamedSymbols() {
        Collection<ComputedSymbol> symbols = getSymbols();
        Map<String, List<ComputedSymbol>> map = new HashMap<>();
        for (ComputedSymbol sym : symbols) {
            List<ComputedSymbol> entry = map.get(sym.name);
            if (entry == null) {
                entry = new ArrayList<>();
                map.put(sym.name, entry);
            }
            entry.add(sym);
        }
        return map;
    }

    public void addSubroutine(long pc, String name, Prototype prototype) {
        ComputedSymbol sym = new ComputedSymbol(name, pc, Type.SUBROUTINE);
        sym.prototype = prototype;
        createComputedSymbol(sym);
    }

    public abstract void createComputedSymbol(ComputedSymbol sym);

    public abstract void setCommentForPC(long pc, String comment);

    public abstract String getCommentForPC(long pc);

    public abstract void setCommentForInsn(long insn, String comment);

    public abstract String getCommentForInsn(long insn);

    public abstract Map<Long, String> getCommentsForInsns();

    public abstract Map<Long, String> getCommentsForPCs();

    public abstract void setExpression(long pc, String expression);

    public abstract String getExpression(long pc);

    public abstract Map<Long, String> getExpressions();

    public abstract void setColor(long pc, Color color);

    public abstract Color getColor(long pc);

    public abstract Map<Long, Color> getColors();
}
