package org.graalvm.vm.trcview.analysis;

import org.graalvm.vm.posix.elf.Symbol;

public class SyntheticSymbol implements Symbol {
    private ComputedSymbol sym;

    public SyntheticSymbol(ComputedSymbol sym) {
        this.sym = sym;
    }

    @Override
    public String getName() {
        return sym.name;
    }

    @Override
    public int getBind() {
        return Symbol.COMMON;
    }

    @Override
    public int getType() {
        switch (sym.type) {
            default:
            case DATA:
            case UNKNOWN:
            case LOCATION:
                return Symbol.NOTYPE;
            case SUBROUTINE:
                return Symbol.FUNC;
        }
    }

    @Override
    public int getVisibility() {
        return Symbol.GLOBAL;
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public long getValue() {
        return sym.address;
    }

    @Override
    public short getSectionIndex() {
        return 1; // dummy value != SHN_UNDEF
    }

    @Override
    public Symbol offset(long off) {
        throw new UnsupportedOperationException();
    }
}
