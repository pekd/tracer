package org.graalvm.vm.trcview.analysis;

import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.trcview.analysis.type.Prototype;

public class AugmentedSymbol implements Symbol, Subroutine {
    private final Symbol symbol;
    private final String name;
    private final Prototype prototype;

    public AugmentedSymbol(Symbol symbol, String name, Prototype prototype) {
        this.symbol = symbol;
        this.name = name;
        this.prototype = prototype;
    }

    @Override
    public Prototype getPrototype() {
        return prototype;
    }

    @Override
    public String getName() {
        return name != null ? name : symbol.getName();
    }

    @Override
    public int getBind() {
        return symbol.getBind();
    }

    @Override
    public int getType() {
        return symbol.getType();
    }

    @Override
    public int getVisibility() {
        return symbol.getVisibility();
    }

    @Override
    public long getSize() {
        return symbol.getSize();
    }

    @Override
    public long getValue() {
        return symbol.getValue();
    }

    @Override
    public short getSectionIndex() {
        return symbol.getSectionIndex();
    }

    @Override
    public Symbol offset(long off) {
        throw new UnsupportedOperationException();
    }
}
