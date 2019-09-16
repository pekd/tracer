package org.graalvm.vm.x86.trcview.analysis;

import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.posix.elf.SymbolResolver;

public class AugmentingSymbolResolver extends SymbolResolver {
    private SymbolResolver root;
    private SymbolTable augmentation;

    public AugmentingSymbolResolver(SymbolResolver root, SymbolTable augmentation) {
        this.root = root;
        this.augmentation = augmentation;
    }

    private Symbol augment(Symbol sym) {
        if (sym != null) {
            ComputedSymbol s = augmentation.get(sym.getValue());
            if (s != null) {
                return new AugmentedSymbol(sym, s.name, s.prototype);
            }
        }
        return sym;
    }

    @Override
    public Symbol getSymbol(long pc) {
        Symbol sym = root.getSymbol(pc);
        return augment(sym);
    }

    @Override
    public Symbol getSymbol(String name) {
        Symbol sym = root.getSymbol(name);
        return augment(sym);
    }

    @Override
    public Symbol getSymbolExact(long pc) {
        Symbol sym = root.getSymbolExact(pc);
        return augment(sym);
    }
}
