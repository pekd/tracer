package org.graalvm.vm.trcview.analysis;

public interface SymbolRenameListener {
    void symbolRenamed(ComputedSymbol sym);
}
