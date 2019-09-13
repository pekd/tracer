package org.graalvm.vm.x86.trcview.analysis;

import org.graalvm.vm.posix.elf.Symbol;

public class AugmentedSymbol implements Symbol {
    private Symbol symbol;
    private String name = null;

    public AugmentedSymbol(Symbol symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    public String getName() {
        return name != null ? name : symbol.getName();
    }

    public int getBind() {
        return symbol.getBind();
    }

    public int getType() {
        return symbol.getType();
    }

    public int getVisibility() {
        return symbol.getVisibility();
    }

    public long getSize() {
        return symbol.getSize();
    }

    public long getValue() {
        return symbol.getValue();
    }

    public short getSectionIndex() {
        return symbol.getSectionIndex();
    }

    public Symbol offset(long off) {
        throw new UnsupportedOperationException();
    }
}
